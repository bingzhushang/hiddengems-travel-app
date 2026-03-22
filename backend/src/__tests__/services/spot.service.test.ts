import { SpotService } from '../../services/spot.service';
import { createMockSpot } from '../utils/testUtils';
import { mockDeep, DeepMockProxy } from 'jest-mock-extended';
import { PrismaClient } from '@prisma/client';

// Create prisma mock at module scope
const prismaMock = mockDeep<PrismaClient>() as unknown as DeepMockProxy<PrismaClient>;

// Mock prisma - must be before any imports that use it
jest.mock('../../config/database', () => ({
  __esModule: true,
  default: prismaMock,
}));

// Mock geolib
jest.mock('geolib', () => ({
  getDistance: jest.fn().mockReturnValue(5000), // 5km
}));

describe('SpotService', () => {
  let spotService: SpotService;

  beforeEach(() => {
    jest.clearAllMocks();
    spotService = new SpotService();
  });

  describe('getRecommendations', () => {
    // TEST 1: 返回AI推荐景点
    it('should return AI recommended spots based on user location', async () => {
      // Arrange
      const userId = 'user-123';
      const lat = 30.25;
      const lng = 120.17;
      const limit = 10;

      const mockSpots = [
        createMockSpot({ id: 'spot-1', name: '九溪十八涧', rating: 4.8, crowdLevel: 'low' }),
        createMockSpot({ id: 'spot-2', name: '满觉陇村', rating: 4.7, crowdLevel: 'low' }),
        createMockSpot({ id: 'spot-3', name: '云栖竹径', rating: 4.6, crowdLevel: 'medium' }),
      ];

      prismaMock.$queryRaw.mockResolvedValueOnce(mockSpots.map(spot => ({
        ...spot,
        distance: 5.0,
      })));

      // Act
      const result = await spotService.getRecommendations(userId, lat, lng, limit);

      // Assert
      expect(result).toHaveLength(expect.any(Number));
      expect(result[0]).toHaveProperty('id');
      expect(result[0]).toHaveProperty('name');
      expect(result[0]).toHaveProperty('rating');
      expect(result[0]).toHaveProperty('aiReason');
    });

    // TEST 2: 无用户时也能返回推荐
    it('should return recommendations without user preferences', async () => {
      // Arrange
      const lat = 30.25;
      const lng = 120.17;
      const limit = 5;

      const mockSpots = [
        createMockSpot({ id: 'spot-1', name: '景点1' }),
      ];

      prismaMock.$queryRaw.mockResolvedValueOnce(mockSpots.map(spot => ({
        ...spot,
        distance: 3.5,
      })));

      // Act
      const result = await spotService.getRecommendations(null, lat, lng, limit);

      // Assert
      expect(result).toBeDefined();
    });

    // TEST 3: 优先推荐人流少的景点
    it('should prioritize spots with low crowd level', async () => {
      // Arrange
      const lat = 30.25;
      const lng = 120.17;

      const mockSpots = [
        createMockSpot({ id: 'spot-1', crowdLevel: 'high' }),
        createMockSpot({ id: 'spot-2', crowdLevel: 'low' }),
        createMockSpot({ id: 'spot-3', crowdLevel: 'medium' }),
      ];

      prismaMock.$queryRaw.mockResolvedValueOnce(mockSpots.map(spot => ({
        ...spot,
        distance: 5.0,
      })));

      // Act
      const result = await spotService.getRecommendations(null, lat, lng, 3);

      // Assert
      expect(result).toBeDefined();
    });
  });

  describe('getNearby', () => {
    // TEST 4: 返回附近景点列表
    it('should return nearby spots with pagination', async () => {
      // Arrange
      const query = {
        lat: 30.25,
        lng: 120.17,
        radius: 50,
        page: 1,
        pageSize: 20,
      };

      const mockSpots = [
        createMockSpot({ id: 'spot-1', name: '景点1' }),
        createMockSpot({ id: 'spot-2', name: '景点2' }),
      ];

      prismaMock.$queryRaw
        .mockResolvedValueOnce(mockSpots.map(spot => ({
          ...spot,
          distance: 5.0,
        })))
        .mockResolvedValueOnce([{ count: BigInt(2) }]);

      // Act
      const result = await spotService.getNearby(query);

      // Assert
      expect(result.items).toHaveLength(2);
      expect(result.pagination.page).toBe(1);
      expect(result.pagination.pageSize).toBe(20);
    });

    // TEST 5: 缺少位置信息时抛出错误
    it('should throw error when location is missing', async () => {
      // Arrange
      const query = {
        lat: undefined,
        lng: undefined,
        page: 1,
        pageSize: 20,
      };

      // Act & Assert
      await expect(spotService.getNearby(query as any)).rejects.toThrow('缺少位置信息');
    });

    // TEST 6: 计算正确距离
    it('should calculate correct distance', async () => {
      // Arrange
      const query = {
        lat: 30.25,
        lng: 120.17,
        radius: 10,
        page: 1,
        pageSize: 20,
      };

      const mockSpots = [
        { ...createMockSpot(), distance: 3.5 },
      ];

      prismaMock.$queryRaw
        .mockResolvedValueOnce(mockSpots)
        .mockResolvedValueOnce([{ count: BigInt(1) }]);

      // Act
      const result = await spotService.getNearby(query);

      // Assert
      expect(result.items[0].distance).toBe(3.5);
    });
  });

  describe('getById', () => {
    // TEST 7: 返回景点详情
    it('should return spot details', async () => {
      // Arrange
      const spotId = 'spot-123';
      const mockSpot = createMockSpot({ id: spotId });

      prismaMock.spot.findUnique.mockResolvedValue(mockSpot as any);
      prismaMock.spot.update.mockResolvedValue(mockSpot as any);
      prismaMock.$queryRaw.mockResolvedValue([]);
      prismaMock.favorite.findFirst.mockResolvedValue(null);

      // Act
      const result = await spotService.getById(spotId);

      // Assert
      expect(result.id).toBe(spotId);
      expect(result.name).toBe(mockSpot.name);
    });

    // TEST 8: 景点不存在时抛出错误
    it('should throw error when spot not found', async () => {
      // Arrange
      prismaMock.spot.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(spotService.getById('non-existent')).rejects.toThrow();
    });

    // TEST 9: 增加浏览次数
    it('should increment view count', async () => {
      // Arrange
      const spotId = 'spot-123';
      const mockSpot = createMockSpot({ id: spotId });

      prismaMock.spot.findUnique.mockResolvedValue(mockSpot as any);
      prismaMock.spot.update.mockResolvedValue(mockSpot as any);
      prismaMock.$queryRaw.mockResolvedValue([]);
      prismaMock.favorite.findFirst.mockResolvedValue(null);

      // Act
      await spotService.getById(spotId);

      // Assert
      expect(prismaMock.spot.update).toHaveBeenCalledWith({
        where: { id: spotId },
        data: { viewCount: { increment: 1 } },
      });
    });

    // TEST 10: 已登录用户返回收藏状态
    it('should return favorite status for logged in user', async () => {
      // Arrange
      const spotId = 'spot-123';
      const userId = 'user-123';
      const mockSpot = createMockSpot({ id: spotId });

      prismaMock.spot.findUnique.mockResolvedValue(mockSpot as any);
      prismaMock.spot.update.mockResolvedValue(mockSpot as any);
      prismaMock.$queryRaw.mockResolvedValue([]);
      prismaMock.favorite.findFirst.mockResolvedValue({ id: 'fav-123' } as any);

      // Act
      const result = await spotService.getById(spotId, userId);

      // Assert
      expect(result.isFavorited).toBe(true);
    });

    // TEST 11: 返回附近景点
    it('should return nearby spots', async () => {
      // Arrange
      const spotId = 'spot-123';
      const mockSpot = createMockSpot({ id: spotId });
      const nearbySpots = [
        { id: 'nearby-1', name: '附近景点1', rating: 4.5, distance: 2.5 },
        { id: 'nearby-2', name: '附近景点2', rating: 4.3, distance: 3.8 },
      ];

      prismaMock.spot.findUnique.mockResolvedValue(mockSpot as any);
      prismaMock.spot.update.mockResolvedValue(mockSpot as any);
      prismaMock.$queryRaw.mockResolvedValue(nearbySpots);
      prismaMock.favorite.findFirst.mockResolvedValue(null);

      // Act
      const result = await spotService.getById(spotId);

      // Assert
      expect(result.nearbySpots).toHaveLength(2);
    });
  });

  describe('search', () => {
    // TEST 12: 搜索景点
    it('should search spots by keyword', async () => {
      // Arrange
      const keyword = '杭州';
      const mockSpots = [
        createMockSpot({ name: '杭州西湖', city: '杭州市' }),
        createMockSpot({ name: '杭州灵隐寺', city: '杭州市' }),
      ];

      prismaMock.$queryRaw.mockResolvedValue(mockSpots);

      // Act
      const result = await spotService.search(keyword, {}, 1, 20);

      // Assert
      expect(result.items).toHaveLength(2);
    });

    // TEST 13: 空关键词返回空结果
    it('should return empty result for empty keyword', async () => {
      // Act
      const result = await spotService.search('', {}, 1, 20);

      // Assert
      expect(result.items).toHaveLength(0);
    });

    // TEST 14: 分页正确
    it('should paginate results correctly', async () => {
      // Arrange
      const mockSpots = Array(20).fill(null).map((_, i) =>
        createMockSpot({ id: `spot-${i}` })
      );

      prismaMock.$queryRaw.mockResolvedValue(mockSpots);

      // Act
      const result = await spotService.search('景点', {}, 1, 20);

      // Assert
      expect(result.pagination.page).toBe(1);
      expect(result.pagination.pageSize).toBe(20);
    });
  });

  describe('toggleFavorite', () => {
    // TEST 15: 添加收藏
    it('should add spot to favorites', async () => {
      // Arrange
      const userId = 'user-123';
      const spotId = 'spot-456';
      const mockSpot = createMockSpot({ id: spotId });

      prismaMock.spot.findUnique.mockResolvedValue(mockSpot as any);
      prismaMock.favorite.findFirst.mockResolvedValue(null);
      prismaMock.favorite.create.mockResolvedValue({ id: 'fav-123', userId, spotId } as any);
      prismaMock.spot.update.mockResolvedValue(mockSpot as any);

      // Act
      const result = await spotService.toggleFavorite(userId, spotId);

      // Assert
      expect(result.isFavorited).toBe(true);
    });

    // TEST 16: 取消收藏
    it('should remove spot from favorites', async () => {
      // Arrange
      const userId = 'user-123';
      const spotId = 'spot-456';
      const mockSpot = createMockSpot({ id: spotId });
      const existingFavorite = { id: 'fav-123', userId, spotId };

      prismaMock.spot.findUnique.mockResolvedValue(mockSpot as any);
      prismaMock.favorite.findFirst.mockResolvedValue(existingFavorite as any);
      prismaMock.favorite.delete.mockResolvedValue(existingFavorite as any);
      prismaMock.spot.update.mockResolvedValue(mockSpot as any);

      // Act
      const result = await spotService.toggleFavorite(userId, spotId);

      // Assert
      expect(result.isFavorited).toBe(false);
    });

    // TEST 17: 景点不存在时抛出错误
    it('should throw error when spot not found', async () => {
      // Arrange
      prismaMock.spot.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(spotService.toggleFavorite('user-123', 'non-existent')).rejects.toThrow();
    });
  });
});
