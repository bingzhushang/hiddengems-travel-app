import { ItineraryService } from '../../services/itinerary.service';
import { prismaMock, createMockItinerary, createMockSpot, createMockUser, resetPrismaMock } from '../utils/testUtils';

// Mock prisma
jest.mock('../../config/database', () => ({
  __esModule: true,
  default: prismaMock,
}));

describe('ItineraryService', () => {
  let itineraryService: ItineraryService;

  beforeEach(() => {
    resetPrismaMock();
    itineraryService = new ItineraryService();
    jest.clearAllMocks();
  });

  describe('create', () => {
    // TEST 1: 成功创建行程
    it('should create itinerary successfully', async () => {
      // Arrange
      const userId = 'user-123';
      const data = {
        title: '杭州2日游',
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-02'),
        destination: '杭州',
      };

      const mockItinerary = createMockItinerary({
        userId,
        title: data.title,
      });

      prismaMock.itinerary.create.mockResolvedValue(mockItinerary);

      // Act
      const result = await itineraryService.create(userId, data);

      // Assert
      expect(result.title).toBe(data.title);
      expect(result.userId).toBe(userId);
    });

    // TEST 2: 自动计算天数
    it('should calculate days count automatically', async () => {
      // Arrange
      const userId = 'user-123';
      const data = {
        title: '杭州3日游',
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-03'),
        destination: '杭州',
      };

      prismaMock.itinerary.create.mockImplementation(async (args) => {
        return createMockItinerary({
          ...args.data,
          daysCount: 3,
        } as any);
      });

      // Act
      const result = await itineraryService.create(userId, data);

      // Assert
      expect(prismaMock.itinerary.create).toHaveBeenCalledWith(
        expect.objectContaining({
          data: expect.objectContaining({
            daysCount: 3,
          }),
        })
      );
    });

    // TEST 3: 标题必填
    it('should require title', async () => {
      // Arrange
      const userId = 'user-123';
      const data = {
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-02'),
      };

      // Act & Assert
      await expect(itineraryService.create(userId, data as any)).rejects.toThrow();
    });
  });

  describe('getById', () => {
    // TEST 4: 返回行程详情
    it('should return itinerary details', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const mockItinerary = createMockItinerary({ id: itineraryId });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);

      // Act
      const result = await itineraryService.getById(itineraryId);

      // Assert
      expect(result.id).toBe(itineraryId);
    });

    // TEST 5: 包含行程项目
    it('should include itinerary items', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const mockItinerary = {
        ...createMockItinerary({ id: itineraryId }),
        items: [
          { id: 'item-1', dayNumber: 1, orderInDay: 1, spotId: 'spot-1', itemType: 'spot' },
          { id: 'item-2', dayNumber: 1, orderInDay: 2, spotId: 'spot-2', itemType: 'spot' },
        ],
      };

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary as any);

      // Act
      const result = await itineraryService.getById(itineraryId);

      // Assert
      expect(result.items).toHaveLength(2);
    });

    // TEST 6: 行程不存在时返回null
    it('should return null when itinerary not found', async () => {
      // Arrange
      prismaMock.itinerary.findUnique.mockResolvedValue(null);

      // Act
      const result = await itineraryService.getById('non-existent');

      // Assert
      expect(result).toBeNull();
    });

    // TEST 7: 私有行程检查权限
    it('should check permission for private itinerary', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const requestingUserId = 'user-456';
      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId: 'user-123',
        isPublic: false,
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);

      // Act & Assert
      await expect(
        itineraryService.getById(itineraryId, requestingUserId)
      ).rejects.toThrow();
    });
  });

  describe('getByUserId', () => {
    // TEST 8: 返回用户行程列表
    it('should return user itineraries', async () => {
      // Arrange
      const userId = 'user-123';
      const mockItineraries = [
        createMockItinerary({ userId, title: '行程1' }),
        createMockItinerary({ userId, title: '行程2' }),
      ];

      prismaMock.itinerary.findMany.mockResolvedValue(mockItineraries);
      prismaMock.itinerary.count.mockResolvedValue(2);

      // Act
      const result = await itineraryService.getByUserId(userId, 1, 10);

      // Assert
      expect(result.items).toHaveLength(2);
    });

    // TEST 9: 分页正确
    it('should paginate correctly', async () => {
      // Arrange
      const userId = 'user-123';
      prismaMock.itinerary.findMany.mockResolvedValue([]);
      prismaMock.itinerary.count.mockResolvedValue(25);

      // Act
      const result = await itineraryService.getByUserId(userId, 2, 10);

      // Assert
      expect(result.pagination.page).toBe(2);
      expect(result.pagination.totalPages).toBe(3);
    });

    // TEST 10: 按状态筛选
    it('should filter by status', async () => {
      // Arrange
      const userId = 'user-123';
      prismaMock.itinerary.findMany.mockResolvedValue([]);
      prismaMock.itinerary.count.mockResolvedValue(0);

      // Act
      await itineraryService.getByUserId(userId, 1, 10, 'draft');

      // Assert
      expect(prismaMock.itinerary.findMany).toHaveBeenCalledWith(
        expect.objectContaining({
          where: expect.objectContaining({
            status: 'draft',
          }),
        })
      );
    });
  });

  describe('update', () => {
    // TEST 11: 成功更新行程
    it('should update itinerary successfully', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-123';
      const updateData = { title: '更新后的标题' };

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId,
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);
      prismaMock.itinerary.update.mockResolvedValue({
        ...mockItinerary,
        ...updateData,
      });

      // Act
      const result = await itineraryService.update(itineraryId, userId, updateData);

      // Assert
      expect(result.title).toBe('更新后的标题');
    });

    // TEST 12: 非所有者无法更新
    it('should not allow update by non-owner', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-456';
      const updateData = { title: '更新后的标题' };

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId: 'user-123',
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);

      // Act & Assert
      await expect(
        itineraryService.update(itineraryId, userId, updateData)
      ).rejects.toThrow();
    });
  });

  describe('addItem', () => {
    // TEST 13: 添加行程项目
    it('should add item to itinerary', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-123';
      const itemData = {
        dayNumber: 1,
        orderInDay: 1,
        spotId: 'spot-456',
        itemType: 'spot',
      };

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId,
      });

      const mockSpot = createMockSpot({ id: 'spot-456' });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);
      prismaMock.spot.findUnique.mockResolvedValue(mockSpot);
      prismaMock.itineraryItem.create.mockResolvedValue({
        id: 'item-123',
        ...itemData,
        itineraryId,
      } as any);

      // Act
      const result = await itineraryService.addItem(itineraryId, userId, itemData);

      // Assert
      expect(result.spotId).toBe('spot-456');
    });

    // TEST 14: 自动填充景点名称
    it('should auto-fill spot name', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-123';
      const itemData = {
        dayNumber: 1,
        orderInDay: 1,
        spotId: 'spot-456',
        itemType: 'spot',
      };

      const mockItinerary = createMockItinerary({ id: itineraryId, userId });
      const mockSpot = createMockSpot({ id: 'spot-456', name: '九溪十八涧' });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);
      prismaMock.spot.findUnique.mockResolvedValue(mockSpot);
      prismaMock.itineraryItem.create.mockResolvedValue({
        id: 'item-123',
        ...itemData,
        itineraryId,
        spotName: '九溪十八涧',
      } as any);

      // Act
      const result = await itineraryService.addItem(itineraryId, userId, itemData);

      // Assert
      expect(result.spotName).toBe('九溪十八涧');
    });
  });

  describe('removeItem', () => {
    // TEST 15: 删除行程项目
    it('should remove item from itinerary', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const itemId = 'item-456';
      const userId = 'user-123';

      const mockItinerary = createMockItinerary({ id: itineraryId, userId });
      const mockItem = { id: itemId, itineraryId };

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);
      prismaMock.itineraryItem.findUnique.mockResolvedValue(mockItem as any);
      prismaMock.itineraryItem.delete.mockResolvedValue(mockItem as any);

      // Act
      await itineraryService.removeItem(itineraryId, itemId, userId);

      // Assert
      expect(prismaMock.itineraryItem.delete).toHaveBeenCalledWith({
        where: { id: itemId },
      });
    });
  });

  describe('publish', () => {
    // TEST 16: 发布行程
    it('should publish itinerary', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-123';

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId,
        status: 'draft',
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);
      prismaMock.itinerary.update.mockResolvedValue({
        ...mockItinerary,
        status: 'published',
        isPublic: true,
        publishedAt: new Date(),
      });

      // Act
      const result = await itineraryService.publish(itineraryId, userId);

      // Assert
      expect(result.status).toBe('published');
      expect(result.isPublic).toBe(true);
    });
  });

  describe('copy', () => {
    // TEST 17: 复制行程
    it('should copy itinerary', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-456';

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId: 'user-123',
        isPublic: true,
        items: [
          { id: 'item-1', dayNumber: 1, orderInDay: 1, spotId: 'spot-1' },
        ],
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary as any);
      prismaMock.itinerary.create.mockResolvedValue({
        ...mockItinerary,
        id: 'new-itinerary-id',
        userId,
      } as any);
      prismaMock.itinerary.update.mockResolvedValue({} as any);

      // Act
      const result = await itineraryService.copy(itineraryId, userId);

      // Assert
      expect(result).toBeDefined();
      expect(prismaMock.itinerary.create).toHaveBeenCalled();
    });

    // TEST 18: 增加复制次数
    it('should increment copy count', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-456';

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        isPublic: true,
        items: [],
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary as any);
      prismaMock.itinerary.create.mockResolvedValue(mockItinerary as any);
      prismaMock.itinerary.update.mockResolvedValue(mockItinerary as any);

      // Act
      await itineraryService.copy(itineraryId, userId);

      // Assert
      expect(prismaMock.itinerary.update).toHaveBeenCalledWith({
        where: { id: itineraryId },
        data: { copyCount: { increment: 1 } },
      });
    });
  });

  describe('delete', () => {
    // TEST 19: 删除行程
    it('should delete itinerary', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-123';

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId,
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);
      prismaMock.itinerary.delete.mockResolvedValue(mockItinerary);

      // Act
      await itineraryService.delete(itineraryId, userId);

      // Assert
      expect(prismaMock.itinerary.delete).toHaveBeenCalledWith({
        where: { id: itineraryId },
      });
    });

    // TEST 20: 非所有者无法删除
    it('should not allow delete by non-owner', async () => {
      // Arrange
      const itineraryId = 'itinerary-123';
      const userId = 'user-456';

      const mockItinerary = createMockItinerary({
        id: itineraryId,
        userId: 'user-123',
      });

      prismaMock.itinerary.findUnique.mockResolvedValue(mockItinerary);

      // Act & Assert
      await expect(
        itineraryService.delete(itineraryId, userId)
      ).rejects.toThrow();
    });
  });
});
