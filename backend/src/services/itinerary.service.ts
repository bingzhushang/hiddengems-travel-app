import prisma from '../config/database';
import { AppError } from '../middleware/error';
import { Prisma } from '@prisma/client';

export interface CreateItineraryInput {
  title: string;
  description?: string;
  coverImage?: string;
  startDate: Date;
  endDate: Date;
  destination?: string;
  destinationLat?: number;
  destinationLng?: number;
  budgetLevel?: string;
  travelStyle?: string[];
  transportation?: string;
}

export interface UpdateItineraryInput {
  title?: string;
  description?: string;
  coverImage?: string;
  startDate?: Date;
  endDate?: Date;
  status?: string;
  isPublic?: boolean;
}

export interface AddItemInput {
  dayNumber: number;
  orderInDay: number;
  spotId?: string;
  plannedDate?: Date;
  startTime?: string;
  endTime?: string;
  duration?: number;
  itemType: string;
  customTitle?: string;
  customContent?: string;
  estimatedCost?: number;
  notes?: string;
}

export interface ItineraryItem {
  id: string;
  itineraryId: string;
  dayNumber: number;
  orderInDay: number;
  spotId?: string;
  spotName?: string;
  spot?: {
    id: string;
    name: string;
    coverImage: string | null;
    rating: number;
  } | null;
  plannedDate?: Date;
  startTime?: string;
  endTime?: string;
  duration?: number;
  itemType: string;
  customTitle?: string;
  customContent?: string;
  estimatedCost?: number;
  notes?: string;
  status: string;
}

export interface ItineraryWithItems {
  id: string;
  userId: string;
  title: string;
  description?: string;
  coverImage?: string;
  startDate: Date;
  endDate: Date;
  daysCount: number;
  destination?: string;
  budgetLevel?: string;
  estimatedBudget?: number;
  travelStyle: string[];
  isAiGenerated: boolean;
  status: string;
  isPublic: boolean;
  viewCount: number;
  favoriteCount: number;
  copyCount: number;
  items: ItineraryItem[];
  createdAt: Date;
  updatedAt: Date;
}

export interface PaginatedItineraries {
  items: ItineraryWithItems[];
  pagination: {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    hasMore: boolean;
  };
}

export class ItineraryService {
  async create(
    userId: string,
    data: CreateItineraryInput
  ): Promise<ItineraryWithItems> {
    if (!data.title) {
      throw new AppError(20001, '标题不能为空');
    }

    // Calculate days count
    const daysCount = Math.ceil(
      (data.endDate.getTime() - data.startDate.getTime()) / (1000 * 60 * 60 * 24)
    ) + 1;

    const itinerary = await prisma.itinerary.create({
      data: {
        userId,
        title: data.title,
        description: data.description,
        coverImage: data.coverImage,
        startDate: data.startDate,
        endDate: data.endDate,
        daysCount,
        destination: data.destination,
        destinationLat: data.destinationLat,
        destinationLng: data.destinationLng,
        budgetLevel: data.budgetLevel,
        travelStyle: data.travelStyle || [],
        transportation: data.transportation,
      },
      include: {
        items: {
          include: {
            spot: {
              select: {
                id: true,
                name: true,
                coverImage: true,
                rating: true,
              },
            },
          },
          orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
        },
      },
    });

    return this.formatItinerary(itinerary);
  }

  async getById(
    itineraryId: string,
    requestingUserId?: string
  ): Promise<ItineraryWithItems | null> {
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
      include: {
        items: {
          include: {
            spot: {
              select: {
                id: true,
                name: true,
                coverImage: true,
                rating: true,
              },
            },
          },
          orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
        },
      },
    });

    if (!itinerary) {
      return null;
    }

    // Check permission for private itineraries
    if (!itinerary.isPublic && requestingUserId !== itinerary.userId) {
      throw new AppError(40001, '无权访问此行程');
    }

    return this.formatItinerary(itinerary);
  }

  async getByUserId(
    userId: string,
    page: number = 1,
    pageSize: number = 10,
    status?: string
  ): Promise<PaginatedItineraries> {
    const skip = (page - 1) * pageSize;

    const where: Prisma.ItineraryWhereInput = {
      userId,
      ...(status && { status }),
    };

    const [itineraries, total] = await Promise.all([
      prisma.itinerary.findMany({
        where,
        include: {
          items: {
            include: {
              spot: {
                select: {
                  id: true,
                  name: true,
                  coverImage: true,
                  rating: true,
                },
              },
            },
            orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
          },
        },
        orderBy: { updatedAt: 'desc' },
        skip,
        take: pageSize,
      }),
      prisma.itinerary.count({ where }),
    ]);

    return {
      items: itineraries.map(this.formatItinerary),
      pagination: {
        page,
        pageSize,
        total,
        totalPages: Math.ceil(total / pageSize),
        hasMore: page * pageSize < total,
      },
    };
  }

  async update(
    itineraryId: string,
    userId: string,
    data: UpdateItineraryInput
  ): Promise<ItineraryWithItems> {
    // Check ownership
    const existing = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
    });

    if (!existing) {
      throw new AppError(10001, '行程不存在');
    }

    if (existing.userId !== userId) {
      throw new AppError(40001, '无权修改此行程');
    }

    const itinerary = await prisma.itinerary.update({
      where: { id: itineraryId },
      data: {
        ...data,
        ...(data.startDate && data.endDate && {
          daysCount: Math.ceil(
            (data.endDate.getTime() - data.startDate.getTime()) / (1000 * 60 * 60 * 24)
          ) + 1,
        }),
      },
      include: {
        items: {
          include: {
            spot: {
              select: {
                id: true,
                name: true,
                coverImage: true,
                rating: true,
              },
            },
          },
          orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
        },
      },
    });

    return this.formatItinerary(itinerary);
  }

  async addItem(
    itineraryId: string,
    userId: string,
    data: AddItemInput
  ): Promise<ItineraryItem> {
    // Check ownership
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
    });

    if (!itinerary) {
      throw new AppError(10001, '行程不存在');
    }

    if (itinerary.userId !== userId) {
      throw new AppError(40001, '无权修改此行程');
    }

    // Get spot info if provided
    let spotName: string | undefined;
    if (data.spotId) {
      const spot = await prisma.spot.findUnique({
        where: { id: data.spotId },
      });
      if (spot) {
        spotName = spot.name;
      }
    }

    const item = await prisma.itineraryItem.create({
      data: {
        itineraryId,
        dayNumber: data.dayNumber,
        orderInDay: data.orderInDay,
        spotId: data.spotId,
        spotName: spotName,
        plannedDate: data.plannedDate,
        startTime: data.startTime,
        endTime: data.endTime,
        duration: data.duration,
        itemType: data.itemType,
        customTitle: data.customTitle,
        customContent: data.customContent,
        estimatedCost: data.estimatedCost,
        notes: data.notes,
      },
      include: {
        spot: {
          select: {
            id: true,
            name: true,
            coverImage: true,
            rating: true,
          },
        },
      },
    });

    return this.formatItem(item);
  }

  async removeItem(
    itineraryId: string,
    itemId: string,
    userId: string
  ): Promise<void> {
    // Check ownership
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
    });

    if (!itinerary) {
      throw new AppError(10001, '行程不存在');
    }

    if (itinerary.userId !== userId) {
      throw new AppError(40001, '无权修改此行程');
    }

    // Check item belongs to this itinerary
    const item = await prisma.itineraryItem.findUnique({
      where: { id: itemId },
    });

    if (!item || item.itineraryId !== itineraryId) {
      throw new AppError(10001, '行程项目不存在');
    }

    await prisma.itineraryItem.delete({
      where: { id: itemId },
    });
  }

  async publish(
    itineraryId: string,
    userId: string
  ): Promise<ItineraryWithItems> {
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
    });

    if (!itinerary) {
      throw new AppError(10001, '行程不存在');
    }

    if (itinerary.userId !== userId) {
      throw new AppError(40001, '无权修改此行程');
    }

    const updated = await prisma.itinerary.update({
      where: { id: itineraryId },
      data: {
        status: 'published',
        isPublic: true,
        publishedAt: new Date(),
      },
      include: {
        items: {
          include: {
            spot: {
              select: {
                id: true,
                name: true,
                coverImage: true,
                rating: true,
              },
            },
          },
          orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
        },
      },
    });

    return this.formatItinerary(updated);
  }

  async copy(
    itineraryId: string,
    userId: string
  ): Promise<ItineraryWithItems> {
    const original = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
      include: {
        items: true,
      },
    });

    if (!original) {
      throw new AppError(10001, '行程不存在');
    }

    if (!original.isPublic && original.userId !== userId) {
      throw new AppError(40001, '无权复制此行程');
    }

    // Create new itinerary
    const newItinerary = await prisma.itinerary.create({
      data: {
        userId,
        title: `复制: ${original.title}`,
        description: original.description,
        coverImage: original.coverImage,
        startDate: original.startDate,
        endDate: original.endDate,
        daysCount: original.daysCount,
        destination: original.destination,
        destinationLat: original.destinationLat,
        destinationLng: original.destinationLng,
        budgetLevel: original.budgetLevel,
        estimatedBudget: original.estimatedBudget,
        travelStyle: original.travelStyle,
        transportation: original.transportation,
        isAiGenerated: original.isAiGenerated,
        items: {
          create: original.items.map((item) => ({
            dayNumber: item.dayNumber,
            orderInDay: item.orderInDay,
            spotId: item.spotId,
            spotName: item.spotName,
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
      include: {
        items: {
          include: {
            spot: {
              select: {
                id: true,
                name: true,
                coverImage: true,
                rating: true,
              },
            },
          },
          orderBy: [{ dayNumber: 'asc' }, { orderInDay: 'asc' }],
        },
      },
    });

    // Increment copy count
    await prisma.itinerary.update({
      where: { id: itineraryId },
      data: { copyCount: { increment: 1 } },
    });

    return this.formatItinerary(newItinerary);
  }

  async delete(itineraryId: string, userId: string): Promise<void> {
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: itineraryId },
    });

    if (!itinerary) {
      throw new AppError(10001, '行程不存在');
    }

    if (itinerary.userId !== userId) {
      throw new AppError(40001, '无权删除此行程');
    }

    await prisma.itinerary.delete({
      where: { id: itineraryId },
    });
  }

  private formatItinerary(itinerary: any): ItineraryWithItems {
    return {
      id: itinerary.id,
      userId: itinerary.userId,
      title: itinerary.title,
      description: itinerary.description,
      coverImage: itinerary.coverImage,
      startDate: itinerary.startDate,
      endDate: itinerary.endDate,
      daysCount: itinerary.daysCount,
      destination: itinerary.destination,
      budgetLevel: itinerary.budgetLevel,
      estimatedBudget: itinerary.estimatedBudget,
      travelStyle: itinerary.travelStyle,
      isAiGenerated: itinerary.isAiGenerated,
      status: itinerary.status,
      isPublic: itinerary.isPublic,
      viewCount: itinerary.viewCount,
      favoriteCount: itinerary.favoriteCount,
      copyCount: itinerary.copyCount,
      items: itinerary.items?.map((item: any) => this.formatItem(item)) || [],
      createdAt: itinerary.createdAt,
      updatedAt: itinerary.updatedAt,
    };
  }

  private formatItem(item: any): ItineraryItem {
    return {
      id: item.id,
      itineraryId: item.itineraryId,
      dayNumber: item.dayNumber,
      orderInDay: item.orderInDay,
      spotId: item.spotId,
      spotName: item.spotName,
      spot: item.spot,
      plannedDate: item.plannedDate,
      startTime: item.startTime,
      endTime: item.endTime,
      duration: item.duration,
      itemType: item.itemType,
      customTitle: item.customTitle,
      customContent: item.customContent,
      estimatedCost: item.estimatedCost,
      notes: item.notes,
      status: item.status,
    };
  }
}

export default new ItineraryService();
