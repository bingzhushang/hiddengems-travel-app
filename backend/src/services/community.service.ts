import prisma from '../config/database';
import { AppError } from '../middleware/error';

export interface FeedQuery {
  type?: 'hot' | 'new' | 'following';
  page?: number;
  pageSize?: number;
  userId?: string;
}

export interface CreatePostInput {
  title?: string;
  content: string;
  postType?: string;
  spotId?: string;
  spotIds?: string[];
  images?: string[];
  tags?: string[];
}

export class CommunityService {
  async getFeed(query: FeedQuery): Promise<{ items: any[]; pagination: any }> {
    const { type = 'hot', page = 1, pageSize = 20 } = query;
    const skip = (page - 1) * pageSize;

    let orderBy: any = { createdAt: 'desc' };
    let where: any = { isPublic: true, status: 'published' };

    if (type === 'hot') {
      orderBy = [
        { viewCount: 'desc' },
        { favoriteCount: 'desc' },
        { createdAt: 'desc' },
      ];
    }

    const [itineraries, total] = await Promise.all([
      prisma.itinerary.findMany({
        where,
        include: {
          user: {
            select: {
              id: true,
              nickname: true,
              avatar: true,
            },
          },
        },
        orderBy,
        skip,
        take: pageSize,
      }),
      prisma.itinerary.count({ where }),
    ]);

    const items = itineraries.map(itinerary => ({
      id: itinerary.id,
      title: itinerary.title,
      description: itinerary.description,
      coverImage: itinerary.coverImage,
      destination: itinerary.destination,
      daysCount: itinerary.daysCount,
      travelStyle: itinerary.travelStyle,
      viewCount: itinerary.viewCount,
      favoriteCount: itinerary.favoriteCount,
      copyCount: itinerary.copyCount,
      isAiGenerated: itinerary.isAiGenerated,
      createdAt: itinerary.createdAt,
      user: itinerary.user,
    }));

    return {
      items,
      pagination: {
        page,
        pageSize,
        total,
        totalPages: Math.ceil(total / pageSize),
      },
    };
  }

  async getItineraryDetail(itineraryId: string, userId?: string): Promise<any> {
    const itinerary = await prisma.itinerary.findFirst({
      where: {
        id: itineraryId,
        OR: [
          { isPublic: true },
          { userId },
        ],
      },
      include: {
        user: {
          select: {
            id: true,
            nickname: true,
            avatar: true,
            bio: true,
          },
        },
        items: {
          orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
          include: {
            spot: {
              select: {
                id: true,
                name: true,
                nameEn: true,
                coverImage: true,
                rating: true,
                address: true,
              },
            },
          },
        },
      },
    });

    if (!itinerary) {
      throw new AppError(40001, '行程不存在');
    }

    // Increment view count
    await prisma.itinerary.update({
      where: { id: itineraryId },
      data: { viewCount: { increment: 1 } },
    });

    // Check if user has favorited
    let isFavorited = false;
    if (userId) {
      const favorite = await prisma.favorite.findFirst({
        where: { userId, targetType: 'itinerary', targetId: itineraryId },
      });
      isFavorited = !!favorite;
    }

    return {
      ...itinerary,
      isFavorited,
    };
  }

  async toggleFavorite(userId: string, itineraryId: string): Promise<{ isFavorited: boolean }> {
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
    });

    if (!itinerary) {
      throw new AppError(40001, '行程不存在');
    }

    const existing = await prisma.favorite.findFirst({
      where: { userId, targetType: 'itinerary', targetId: itineraryId },
    });

    if (existing) {
      await prisma.favorite.delete({
        where: { id: existing.id },
      });
      await prisma.itinerary.update({
        where: { id: itineraryId },
        data: { favoriteCount: { decrement: 1 } },
      });
      return { isFavorited: false };
    } else {
      await prisma.favorite.create({
        data: {
          userId,
          targetType: 'itinerary',
          targetId: itineraryId,
        },
      });
      await prisma.itinerary.update({
        where: { id: itineraryId },
        data: { favoriteCount: { increment: 1 } },
      });
      return { isFavorited: true };
    }
  }

  async copyItinerary(userId: string, itineraryId: string): Promise<any> {
    const original = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
      include: { items: true },
    });

    if (!original) {
      throw new AppError(40001, '行程不存在');
    }

    // Create copy
    const copy = await prisma.itinerary.create({
      data: {
        userId,
        title: `${original.title} (副本)`,
        description: original.description,
        coverImage: original.coverImage,
        startDate: original.startDate,
        endDate: original.endDate,
        daysCount: original.daysCount,
        destination: original.destination,
        travelStyle: original.travelStyle,
        budgetLevel: original.budgetLevel,
        transportation: original.transportation,
        status: 'draft',
        isPublic: false,
        items: {
          create: original.items.map(item => ({
            dayNumber: item.dayNumber,
            orderInDay: item.orderInDay,
            spotId: item.spotId,
            spotName: item.spotName,
            plannedDate: item.plannedDate,
            startTime: item.startTime,
            endTime: item.endTime,
            duration: item.duration,
            itemType: item.itemType,
            customTitle: item.customTitle,
            customContent: item.customContent,
            estimatedCost: item.estimatedCost,
            notes: item.notes,
          })),
        },
      },
    });

    // Increment copy count on original
    await prisma.itinerary.update({
      where: { id: itineraryId },
      data: { copyCount: { increment: 1 } },
    });

    return copy;
  }

  async createPost(userId: string, data: CreatePostInput): Promise<any> {
    const post = await prisma.post.create({
      data: {
        userId,
        title: data.title,
        content: data.content,
        postType: data.postType || 'article',
        spotId: data.spotId,
        spotIds: data.spotIds || [],
        images: data.images ? JSON.parse(JSON.stringify(data.images)) : null,
        tags: data.tags || [],
      },
    });

    return post;
  }
}

const communityService = new CommunityService();
export default communityService;
