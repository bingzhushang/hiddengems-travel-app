import prisma from '../config/database';
import { AppError } from '../middleware/error';
import { Prisma } from '@prisma/client';

export interface SpotQuery {
  lat?: number;
  lng?: number;
  radius?: number;
  category?: string;
  tags?: string[];
  crowdLevel?: string;
  page?: number;
  pageSize?: number;
  sortBy?: 'rating' | 'distance' | 'popularity';
}

export interface SpotListItem {
  id: string;
  name: string;
  coverImage: string | null;
  rating: number;
  distance?: number;
  crowdLevel: string;
  tags: string[];
  city: string | null;
  province: string | null;
  aiReason?: string;
}

export interface SpotDetail extends SpotListItem {
  nameEn: string | null;
  description: string | null;
  aiSummary: string | null;
  location: {
    lat: number;
    lng: number;
    country: string;
    province: string | null;
    city: string | null;
    district: string | null;
    address: string | null;
  };
  category: {
    id: string;
    name: string;
    slug: string;
  } | null;
  reviewCount: number;
  crowdData: any;
  openingHours: Record<string, any> | null;
  ticketPrice: number | null;
  ticketInfo: string | null;
  suggestedDuration: number | null;
  bestSeasons: string[];
  bestTimeOfDay: string[];
  images: any[];
  isFavorited: boolean;
  nearbySpots: NearbySpot[];
}

export interface NearbySpot {
  id: string;
  name: string;
  rating: number;
  distance: number;
}

export interface PaginatedResult<T> {
  items: T[];
  pagination: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    hasMore: boolean;
  };
}

export class SpotService {
  async getRecommendations(
    userId: string | null,
    lat: number,
    lng: number,
    limit: number = 10
  ): Promise<SpotListItem[]> {
    // Get user preferences if logged in
    let preferences = {};
    if (userId) {
      const user = await prisma.user.findUnique({
        where: { id: userId },
        select: { preferences: true },
      });
      preferences = (user?.preferences as Record<string, any>) || {};
    }

    // Find nearby spots with low crowd - using raw query for PostGIS
    const spots = await prisma.$queryRaw<any[]>`
      SELECT
        id, name, name_en, cover_image, rating,
        tags, crowd_level, city, province,
        ST_Distance(
          ST_MakePoint(longitude, latitude)::geography,
          ST_MakePoint(${lng}, ${lat})::geography
        ) / 1000 as distance
      FROM spots
      WHERE status = 'active'
        AND crowd_level IN ('low', 'medium')
      ORDER BY
        CASE crowd_level
          WHEN 'low' THEN 0
          WHEN 'medium' THEN 1
          ELSE 2
        END,
        rating DESC,
        distance ASC
      LIMIT ${limit * 2}
    `;

    // Apply diversity and generate AI reasons
    const diverseSpots = this.applyDiversity(spots, limit);

    return diverseSpots.map((spot: any) => ({
      id: spot.id,
      name: spot.name,
      coverImage: spot.cover_image,
      rating: spot.rating,
      distance: Math.round(spot.distance * 10) / 10,
      tags: spot.tags || [],
      crowdLevel: spot.crowd_level,
      city: spot.city,
      province: spot.province,
      aiReason: this.generateAIReason(spot, preferences),
    }));
  }

  async getNearby(query: SpotQuery): Promise<PaginatedResult<SpotListItem>> {
    const { lat, lng, radius = 50, page = 1, pageSize = 20, category, crowdLevel } = query;

    if (!lat || !lng) {
      throw new AppError(20001, '缺少位置信息');
    }

    const offset = (page - 1) * pageSize;

    // Build filter conditions
    const crowdFilter = crowdLevel ? `AND crowd_level = '${crowdLevel}'` : '';
    const categoryFilter = category ? `AND category_id = '${category}'` : '';

    // Query spots with distance calculation
    const spots = await prisma.$queryRaw<any[]>`
      SELECT
        s.id, s.name, s.cover_image, s.rating, s.tags, s.crowd_level,
        s.country, s.province, s.city, s.category_id,
        ST_Distance(
          ST_MakePoint(s.longitude, s.latitude)::geography,
          ST_MakePoint(${lng}, ${lat})::geography
        ) / 1000 as distance
      FROM spots s
      WHERE s.status = 'active'
        AND ST_DWithin(
          ST_MakePoint(s.longitude, s.latitude)::geography,
          ST_MakePoint(${lng}, ${lat})::geography,
          ${radius * 1000}
        )
        ${Prisma.raw(crowdFilter)}
        ${Prisma.raw(categoryFilter)}
      ORDER BY distance ASC
      LIMIT ${pageSize}
      OFFSET ${offset}
    `;

    // Get total count
    const countResult = await prisma.$queryRaw<[{ count: bigint }]>`
      SELECT COUNT(*)::int as count
      FROM spots s
      WHERE s.status = 'active'
        AND ST_DWithin(
          ST_MakePoint(s.longitude, s.latitude)::geography,
          ST_MakePoint(${lng}, ${lat})::geography,
          ${radius * 1000}
        )
        ${Prisma.raw(crowdFilter)}
        ${Prisma.raw(categoryFilter)}
    `;

    const total = Number(countResult[0]?.count || 0);

    return {
      items: spots.map((spot: any) => ({
        id: spot.id,
        name: spot.name,
        coverImage: spot.cover_image,
        rating: spot.rating,
        distance: Math.round(spot.distance * 10) / 10,
        tags: spot.tags || [],
        crowdLevel: spot.crowd_level,
        city: spot.city,
        province: spot.province,
        location: {
          country: spot.country,
          province: spot.province,
          city: spot.city,
        },
      })),
      pagination: {
        page,
        pageSize,
        total,
        totalPages: Math.ceil(total / pageSize),
        hasMore: page * pageSize < total,
      },
    };
  }

  async getById(spotId: string, userId?: string): Promise<SpotDetail> {
    const spot = await prisma.spot.findUnique({
      where: { id: spotId },
      include: {
        category: true,
      },
    });

    if (!spot || spot.status !== 'active') {
      throw new AppError(10001, '景点不存在');
    }

    // Check if favorited
    let isFavorited = false;
    if (userId) {
      const favorite = await prisma.favorite.findFirst({
        where: { userId, spotId },
      });
      isFavorited = !!favorite;
    }

    // Increment view count
    await prisma.spot.update({
      where: { id: spotId },
      data: { viewCount: { increment: 1 } },
    });

    // Get nearby spots
    const nearbySpots = await prisma.$queryRaw<NearbySpot[]>`
      SELECT id, name, rating,
        ST_Distance(
          ST_MakePoint(longitude, latitude)::geography,
          ST_MakePoint(${spot.longitude}, ${spot.latitude})::geography
        ) / 1000 as distance
      FROM spots
      WHERE id != ${spotId}
        AND status = 'active'
      ORDER BY distance ASC
      LIMIT 5
    `;

    return {
      id: spot.id,
      name: spot.name,
      nameEn: spot.nameEn,
      description: spot.description,
      aiSummary: spot.aiSummary,
      coverImage: spot.coverImage,
      rating: spot.rating,
      reviewCount: spot.reviewCount,
      crowdLevel: spot.crowdLevel,
      tags: spot.tags,
      city: spot.city,
      province: spot.province,
      location: {
        lat: spot.latitude,
        lng: spot.longitude,
        country: spot.country,
        province: spot.province,
        city: spot.city,
        district: spot.district,
        address: spot.address,
      },
      category: spot.category ? {
        id: spot.category.id,
        name: spot.category.name,
        slug: spot.category.slug,
      } : null,
      crowdData: spot.crowdData,
      openingHours: spot.openingHours as Record<string, any>,
      ticketPrice: spot.ticketPrice,
      ticketInfo: spot.ticketInfo,
      suggestedDuration: spot.suggestedDuration,
      bestSeasons: spot.bestSeasons,
      bestTimeOfDay: spot.bestTimeOfDay,
      images: spot.images as any[],
      isFavorited,
      nearbySpots: nearbySpots.map((s: any) => ({
        id: s.id,
        name: s.name,
        rating: s.rating,
        distance: Math.round(s.distance * 10) / 10,
      })),
    };
  }

  async search(
    query: string,
    filters: Record<string, any>,
    page: number = 1,
    pageSize: number = 20
  ): Promise<PaginatedResult<SpotListItem>> {
    if (!query || query.trim() === '') {
      return {
        items: [],
        pagination: {
          page,
          pageSize,
          total: 0,
          totalPages: 0,
          hasMore: false,
        },
      };
    }

    const offset = (page - 1) * pageSize;

    // Full-text search with ranking
    const spots = await prisma.$queryRaw<any[]>`
      SELECT
        id, name, cover_image, rating, tags, crowd_level,
        city, province,
        ts_rank(
          to_tsvector('simple', name || ' ' || COALESCE(description, '')),
          plainto_tsquery('simple', ${query})
        ) as rank
      FROM spots
      WHERE status = 'active'
        AND to_tsvector('simple', name || ' ' || COALESCE(description, ''))
            @@ plainto_tsquery('simple', ${query})
      ORDER BY rank DESC, rating DESC
      LIMIT ${pageSize}
      OFFSET ${offset}
    `;

    return {
      items: spots.map((spot: any) => ({
        id: spot.id,
        name: spot.name,
        coverImage: spot.cover_image,
        rating: spot.rating,
        tags: spot.tags || [],
        crowdLevel: spot.crowd_level,
        city: spot.city,
        province: spot.province,
      })),
      pagination: {
        page,
        pageSize,
        total: spots.length, // Simplified, would need count query
        totalPages: Math.ceil(spots.length / pageSize),
        hasMore: spots.length === pageSize,
      },
    };
  }

  async toggleFavorite(
    userId: string,
    spotId: string
  ): Promise<{ isFavorited: boolean; message: string }> {
    // Check if spot exists
    const spot = await prisma.spot.findUnique({
      where: { id: spotId },
    });

    if (!spot) {
      throw new AppError(10001, '景点不存在');
    }

    // Check if already favorited
    const existingFavorite = await prisma.favorite.findFirst({
      where: { userId, spotId },
    });

    if (existingFavorite) {
      // Remove favorite
      await prisma.favorite.delete({
        where: { id: existingFavorite.id },
      });

      // Decrement favorite count
      await prisma.spot.update({
        where: { id: spotId },
        data: { favoriteCount: { decrement: 1 } },
      });

      return { isFavorited: false, message: '已取消收藏' };
    } else {
      // Add favorite
      await prisma.favorite.create({
        data: { userId, spotId, targetType: 'spot', targetId: spotId },
      });

      // Increment favorite count
      await prisma.spot.update({
        where: { id: spotId },
        data: { favoriteCount: { increment: 1 } },
      });

      return { isFavorited: true, message: '已收藏' };
    }
  }

  private applyDiversity(spots: any[], limit: number): any[] {
    const seenCategories = new Set();
    const diverse: any[] = [];

    for (const spot of spots) {
      const category = spot.tags?.[0] || 'other';

      // Always include first few, then apply diversity
      if (diverse.length < limit / 2 || !seenCategories.has(category)) {
        diverse.push(spot);
        seenCategories.add(category);
      }

      if (diverse.length >= limit) break;
    }

    return diverse;
  }

  private generateAIReason(spot: any, preferences: any): string {
    const reasons = [];

    if (spot.crowd_level === 'low') {
      reasons.push('人少清静');
    }

    if (spot.rating >= 4.5) {
      reasons.push('口碑很好');
    }

    if (spot.distance < 10) {
      reasons.push('距离较近');
    }

    if (reasons.length === 0) {
      reasons.push('值得一去');
    }

    return `这个景点${reasons.join('，')}，适合周末放松。`;
  }
}

export default new SpotService();
