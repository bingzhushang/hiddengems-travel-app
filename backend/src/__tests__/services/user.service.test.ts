// Create mock at module scope BEFORE any imports that use it
const mockPrisma = {
  user: {
    findUnique: jest.fn(),
    update: jest.fn(),
  },
  favorite: {
    count: jest.fn(),
    findMany: jest.fn(),
  },
  itinerary: {
    count: jest.fn(),
    findMany: jest.fn(),
  },
  review: {
    count: jest.fn(),
  },
  itineraryItem: {
    findMany: jest.fn(),
  },
};

// Mock prisma
jest.mock('../../config/database', () => ({
  __esModule: true,
  default: mockPrisma,
}));

// NOW import the service after mocks are set up
import { UserService } from '../../services/user.service';

describe('UserService', () => {
  let userService: UserService;

  const createMockUser = (overrides: Partial<any> = {}) => ({
    id: 'user-123',
    email: 'test@example.com',
    nickname: 'Test User',
    avatar: 'https://example.com/avatar.jpg',
    bio: 'Test bio',
    membershipType: 'free',
    membershipExpireAt: null,
    contributionPoints: 0,
    level: 1,
    preferences: {},
    status: 'active',
    emailVerified: true,
    createdAt: new Date(),
    updatedAt: new Date(),
    lastLoginAt: new Date(),
    _count: {
      favorites: 5,
      itineraries: 3,
      reviews: 2,
    },
    ...overrides,
  });

  beforeEach(() => {
    jest.clearAllMocks();
    userService = new UserService();
  });

  describe('getById', () => {
    it('should return user by id', async () => {
      // Arrange
      const mockUser = createMockUser();
      mockPrisma.user.findUnique.mockResolvedValue(mockUser);

      // Act
      const result = await userService.getById('user-123');

      // Assert
      expect(result.id).toBe('user-123');
      expect(result.nickname).toBe('Test User');
      expect(result.stats).toBeDefined();
      expect(result.stats.favoriteCount).toBe(5);
    });

    it('should throw error when user not found', async () => {
      // Arrange
      mockPrisma.user.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(userService.getById('non-existent')).rejects.toThrow('用户不存在');
    });
  });

  describe('update', () => {
    it('should update user profile', async () => {
      // Arrange
      const mockUser = createMockUser({ nickname: 'Updated Name' });
      mockPrisma.user.update.mockResolvedValue(mockUser);

      // Act
      const result = await userService.update('user-123', { nickname: 'Updated Name' });

      // Assert
      expect(result.nickname).toBe('Updated Name');
      expect(mockPrisma.user.update).toHaveBeenCalledWith(
        expect.objectContaining({
          where: { id: 'user-123' },
          data: expect.objectContaining({ nickname: 'Updated Name' }),
        })
      );
    });
  });

  describe('getStats', () => {
    it('should return user statistics', async () => {
      // Arrange
      mockPrisma.favorite.count.mockResolvedValue(10);
      mockPrisma.itinerary.count.mockResolvedValue(5);
      mockPrisma.review.count.mockResolvedValue(3);
      mockPrisma.itinerary.findMany.mockResolvedValue([{ id: 'it-1' }, { id: 'it-2' }]);
      mockPrisma.itineraryItem.findMany.mockResolvedValue([
        { spotId: 'spot-1' },
        { spotId: 'spot-2' },
        { spotId: 'spot-1' }, // duplicate, should be counted once
      ]);

      // Act
      const result = await userService.getStats('user-123');

      // Assert
      expect(result.favoriteCount).toBe(10);
      expect(result.itineraryCount).toBe(5);
      expect(result.reviewCount).toBe(3);
      expect(result.visitedCount).toBe(2); // unique spots
    });
  });

  describe('getFavorites', () => {
    it('should return paginated favorites', async () => {
      // Arrange
      const mockFavorites = [
        {
          spot: { id: 'spot-1', name: 'Spot 1', rating: 4.5 },
          createdAt: new Date(),
        },
        {
          spot: { id: 'spot-2', name: 'Spot 2', rating: 4.8 },
          createdAt: new Date(),
        },
      ];
      mockPrisma.favorite.findMany.mockResolvedValue(mockFavorites);
      mockPrisma.favorite.count.mockResolvedValue(2);

      // Act
      const result = await userService.getFavorites('user-123', 1, 20);

      // Assert
      expect(result.items).toHaveLength(2);
      expect(result.pagination.total).toBe(2);
    });
  });

  describe('getItineraries', () => {
    it('should return paginated itineraries', async () => {
      // Arrange
      const mockItineraries = [
        { id: 'it-1', title: 'Itinerary 1' },
        { id: 'it-2', title: 'Itinerary 2' },
      ];
      mockPrisma.itinerary.findMany.mockResolvedValue(mockItineraries);
      mockPrisma.itinerary.count.mockResolvedValue(2);

      // Act
      const result = await userService.getItineraries('user-123', undefined, 1, 10);

      // Assert
      expect(result.items).toHaveLength(2);
      expect(result.pagination.total).toBe(2);
    });

    it('should filter by status', async () => {
      // Arrange
      mockPrisma.itinerary.findMany.mockResolvedValue([]);
      mockPrisma.itinerary.count.mockResolvedValue(0);

      // Act
      await userService.getItineraries('user-123', 'published', 1, 10);

      // Assert
      expect(mockPrisma.itinerary.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          where: expect.objectContaining({ status: 'published' }),
        })
      );
    });
  });
});
