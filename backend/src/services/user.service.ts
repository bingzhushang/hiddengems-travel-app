import prisma from '../config/database';
import { AppError } from '../middleware/error';

export interface UpdateUserInput {
  nickname?: string;
  avatar?: string;
  bio?: string;
  preferences?: Record<string, any>;
}

export interface UserStats {
  favoriteCount: number;
  itineraryCount: number;
  visitedCount: number;
  reviewCount: number;
}

export class UserService {
  async getById(userId: string): Promise<Record<string, any>> {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        _count: {
          select: {
            favorites: true,
            itineraries: true,
            reviews: true,
          },
        },
      },
    });

    if (!user) {
      throw new AppError(20003, '用户不存在');
    }

    return this.formatUser(user);
  }

  async update(userId: string, data: UpdateUserInput): Promise<Record<string, any>> {
    const user = await prisma.user.update({
      where: { id: userId },
      data: {
        ...(data.nickname && { nickname: data.nickname }),
        ...(data.avatar && { avatar: data.avatar }),
        ...(data.bio && { bio: data.bio }),
        ...(data.preferences && { preferences: data.preferences }),
        updatedAt: new Date(),
      },
    });

    return this.formatUser(user);
  }

  async getStats(userId: string): Promise<UserStats> {
    const [favoriteCount, itineraryCount, reviewCount] = await Promise.all([
      prisma.favorite.count({ where: { userId } }),
      prisma.itinerary.count({ where: { userId } }),
      prisma.review.count({ where: { userId } }),
    ]);

    // Count visited spots from user's itineraries
    const itineraryIds = await prisma.itinerary.findMany({
      where: { userId },
      select: { id: true },
    });
    const ids = itineraryIds.map(i => i.id);

    const visitedItems = await prisma.itineraryItem.findMany({
      where: {
        itineraryId: { in: ids },
        itemType: 'spot',
        spotId: { not: null },
      },
      select: { spotId: true },
    });
    const visitedCount = new Set(visitedItems.map(item => item.spotId).filter(Boolean)).size;

    return {
      favoriteCount,
      itineraryCount,
      visitedCount,
      reviewCount,
    };
  }

  async getFavorites(
    userId: string,
    page: number = 1,
    pageSize: number = 20
  ): Promise<{ items: any[]; pagination: any }> {
    const skip = (page - 1) * pageSize;

    const [favorites, total] = await Promise.all([
      prisma.favorite.findMany({
        where: { userId, targetType: 'spot', spotId: { not: null } },
        include: {
          spot: {
            select: {
              id: true,
              name: true,
              nameEn: true,
              coverImage: true,
              rating: true,
              city: true,
              tags: true,
              crowdLevel: true,
            },
          },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: pageSize,
      }),
      prisma.favorite.count({ where: { userId, targetType: 'spot' } }),
    ]);

    return {
      items: favorites.map(f => ({
        ...f.spot,
        favoritedAt: f.createdAt,
      })),
      pagination: {
        page,
        pageSize,
        total,
        totalPages: Math.ceil(total / pageSize),
      },
    };
  }

  async getItineraries(
    userId: string,
    status?: string,
    page: number = 1,
    pageSize: number = 10
  ): Promise<{ items: any[]; pagination: any }> {
    const skip = (page - 1) * pageSize;
    const where: any = { userId };
    if (status) {
      where.status = status;
    }

    const [itineraries, total] = await Promise.all([
      prisma.itinerary.findMany({
        where,
        orderBy: { updatedAt: 'desc' },
        skip,
        take: pageSize,
      }),
      prisma.itinerary.count({ where }),
    ]);

    return {
      items: itineraries,
      pagination: {
        page,
        pageSize,
        total,
        totalPages: Math.ceil(total / pageSize),
      },
    };
  }

  private formatUser(user: any): Record<string, any> {
    const { passwordHash, ...userWithoutPassword } = user;
    return {
      ...userWithoutPassword,
      stats: {
        favoriteCount: user._count?.favorites || 0,
        itineraryCount: user._count?.itineraries || 0,
        reviewCount: user._count?.reviews || 0,
      },
    };
  }
}

const userService = new UserService();
export default userService;
