// Create mock at module scope BEFORE any imports that use it
const mockPrisma = {
  itinerary: {
    findMany: jest.fn(),
    findFirst: jest.fn(),
    findUnique: jest.fn(),
    count: jest.fn(),
    update: jest.fn(),
    create: jest.fn(),
  },
  favorite: {
    findFirst: jest.fn(),
    create: jest.fn(),
    delete: jest.fn(),
  },
  post: {
    create: jest.fn(),
  },
};

// Mock prisma
jest.mock('../../config/database', () => ({
  __esModule: true,
  default: mockPrisma,
}));

// NOW import the service after mocks are set up
import { CommunityService } from '../../services/community.service';

describe('CommunityService', () => {
  let communityService: CommunityService;

  const createMockItinerary = (overrides: Partial<any> = {}) => ({
    id: 'itinerary-123',
    userId: 'user-123',
    title: 'Test Itinerary',
    description: 'A test itinerary',
    coverImage: 'https://example.com/cover.jpg',
    destination: '杭州',
    daysCount: 3,
    travelStyle: ['自然', '人文'],
    viewCount: 100,
    favoriteCount: 10,
    copyCount: 5,
    isAiGenerated: true,
    status: 'published',
    isPublic: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    user: {
      id: 'user-123',
      nickname: 'Test User',
      avatar: 'https://example.com/avatar.jpg',
    },
    ...overrides,
  });

  beforeEach(() => {
    jest.clearAllMocks();
    communityService = new CommunityService();
  });

  describe('getFeed', () => {
    it('should return hot feed', async () => {
      // Arrange
      const mockItineraries = [
        createMockItinerary({ id: 'it-1', viewCount: 500 }),
        createMockItinerary({ id: 'it-2', viewCount: 300 }),
      ];
      mockPrisma.itinerary.findMany.mockResolvedValue(mockItineraries);
      mockPrisma.itinerary.count.mockResolvedValue(2);

      // Act
      const result = await communityService.getFeed({ type: 'hot', page: 1, pageSize: 20 });

      // Assert
      expect(result.items).toHaveLength(2);
      expect(result.pagination.total).toBe(2);
    });

    it('should return new feed ordered by date', async () => {
      // Arrange
      mockPrisma.itinerary.findMany.mockResolvedValue([]);
      mockPrisma.itinerary.count.mockResolvedValue(0);

      // Act
      await communityService.getFeed({ type: 'new', page: 1, pageSize: 20 });

      // Assert
      expect(mockPrisma.itinerary.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          orderBy: { createdAt: 'desc' },
        })
      );
    });

    it('should only return public and published itineraries', async () => {
      // Arrange
      mockPrisma.itinerary.findMany.mockResolvedValue([]);
      mockPrisma.itinerary.count.mockResolvedValue(0);

      // Act
      await communityService.getFeed({ type: 'hot' });

      // Assert
      expect(mockPrisma.itinerary.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          where: expect.objectContaining({
            isPublic: true,
            status: 'published',
          }),
        })
      );
    });
  });

  describe('getItineraryDetail', () => {
    it('should return itinerary detail', async () => {
      // Arrange
      const mockItinerary = createMockItinerary({
        items: [],
      });
      mockPrisma.itinerary.findFirst.mockResolvedValue(mockItinerary);
      mockPrisma.itinerary.update.mockResolvedValue(mockItinerary);

      // Act
      const result = await communityService.getItineraryDetail('itinerary-123');

      // Assert
      expect(result.id).toBe('itinerary-123');
      expect(result.title).toBe('Test Itinerary');
    });

    it('should increment view count', async () => {
      // Arrange
      const mockItinerary = createMockItinerary({ items: [] });
      mockPrisma.itinerary.findFirst.mockResolvedValue(mockItinerary);
      mockPrisma.itinerary.update.mockResolvedValue(mockItinerary);

      // Act
      await communityService.getItineraryDetail('itinerary-123');

      // Assert
      expect(mockPrisma.itinerary.update).toHaveBeenCalledWith(
        expect.objectContaining({
          where: { id: 'itinerary-123' },
          data: { viewCount: { increment: 1 } },
        })
      );
    });

    it('should check favorite status for logged in user', async () => {
      // Arrange
      const mockItinerary = createMockItinerary({ items: [] });
      mockPrisma.itinerary.findFirst.mockResolvedValue(mockItinerary);
      mockPrisma.itinerary.update.mockResolvedValue(mockItinerary);
      mockPrisma.favorite.findFirst.mockResolvedValue({ id: 'fav-123' });

      // Act
      const result = await communityService.getItineraryDetail('itinerary-123', 'user-123');

      // Assert
      expect(result.isFavorited).toBe(true);
    });

    it('should throw error when itinerary not found', async () => {
      // Arrange
      mockPrisma.itinerary.findFirst.mockResolvedValue(null);

      // Act & Assert
      await expect(communityService.getItineraryDetail('non-existent')).rejects.toThrow('行程不存在');
    });
  });

  describe('toggleFavorite', () => {
    it('should add favorite when not exists', async () => {
      // Arrange
      const mockItinerary = createMockItinerary();
      mockPrisma.itinerary.findUnique.mockResolvedValue(mockItinerary);
      mockPrisma.favorite.findFirst.mockResolvedValue(null);
      mockPrisma.favorite.create.mockResolvedValue({ id: 'fav-123' });
      mockPrisma.itinerary.update.mockResolvedValue(mockItinerary);

      // Act
      const result = await communityService.toggleFavorite('user-123', 'itinerary-123');

      // Assert
      expect(result.isFavorited).toBe(true);
      expect(mockPrisma.favorite.create).toHaveBeenCalled();
    });

    it('should remove favorite when exists', async () => {
      // Arrange
      const mockItinerary = createMockItinerary();
      mockPrisma.itinerary.findUnique.mockResolvedValue(mockItinerary);
      mockPrisma.favorite.findFirst.mockResolvedValue({ id: 'fav-123' });
      mockPrisma.favorite.delete.mockResolvedValue({ id: 'fav-123' });
      mockPrisma.itinerary.update.mockResolvedValue(mockItinerary);

      // Act
      const result = await communityService.toggleFavorite('user-123', 'itinerary-123');

      // Assert
      expect(result.isFavorited).toBe(false);
      expect(mockPrisma.favorite.delete).toHaveBeenCalled();
    });

    it('should throw error when itinerary not found', async () => {
      // Arrange
      mockPrisma.itinerary.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(communityService.toggleFavorite('user-123', 'non-existent')).rejects.toThrow('行程不存在');
    });
  });

  describe('copyItinerary', () => {
    it('should create a copy of itinerary', async () => {
      // Arrange
      const originalItinerary = createMockItinerary({
        items: [
          { dayNumber: 1, orderInDay: 1, spotId: 'spot-1', spotName: 'Spot 1', itemType: 'spot' },
        ],
      });
      mockPrisma.itinerary.findUnique.mockResolvedValue(originalItinerary);
      mockPrisma.itinerary.create.mockResolvedValue({
        ...originalItinerary,
        id: 'new-itinerary-id',
        title: 'Test Itinerary (副本)',
        status: 'draft',
        isPublic: false,
      });

      // Act
      const result = await communityService.copyItinerary('user-456', 'itinerary-123');

      // Assert
      expect(mockPrisma.itinerary.create).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({
            userId: 'user-456',
            title: 'Test Itinerary (副本)',
            status: 'draft',
            isPublic: false,
          }),
        })
      );
    });

    it('should increment copy count on original', async () => {
      // Arrange
      const originalItinerary = createMockItinerary({ items: [] });
      mockPrisma.itinerary.findUnique.mockResolvedValue(originalItinerary);
      mockPrisma.itinerary.create.mockResolvedValue({ id: 'new-id' });
      mockPrisma.itinerary.update.mockResolvedValue(originalItinerary);

      // Act
      await communityService.copyItinerary('user-456', 'itinerary-123');

      // Assert
      expect(mockPrisma.itinerary.update).toHaveBeenCalledWith(
        expect.objectContaining({
          where: { id: 'itinerary-123' },
          data: { copyCount: { increment: 1 } },
        })
      );
    });

    it('should throw error when original not found', async () => {
      // Arrange
      mockPrisma.itinerary.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(communityService.copyItinerary('user-456', 'non-existent')).rejects.toThrow('行程不存在');
    });
  });

  describe('createPost', () => {
    it('should create a post', async () => {
      // Arrange
      const mockPost = {
        id: 'post-123',
        userId: 'user-123',
        title: 'Test Post',
        content: 'Post content',
        postType: 'article',
      };
      mockPrisma.post.create.mockResolvedValue(mockPost);

      // Act
      const result = await communityService.createPost('user-123', {
        title: 'Test Post',
        content: 'Post content',
        postType: 'article',
      });

      // Assert
      expect(result.title).toBe('Test Post');
      expect(mockPrisma.post.create).toHaveBeenCalled();
    });
  });
});
