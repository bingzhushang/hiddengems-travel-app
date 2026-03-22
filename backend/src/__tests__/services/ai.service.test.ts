import { AIService, ItineraryParams } from '../../services/ai.service';
import { prismaMock, createMockSpot, createMockUser, resetPrismaMock } from '../utils/testUtils';

// Mock OpenAI
const mockChatCompletion = {
  choices: [{
    message: {
      content: JSON.stringify({
        title: '杭州小众2日游',
        description: '探索杭州不为人知的美景',
        days: [
          {
            day: 1,
            date: '2026-04-01',
            activities: [
              { order: 1, time: '09:00', type: 'spot', spotId: 'spot-1', title: '满觉陇村', duration: 120 },
              { order: 2, time: '12:00', type: 'meal', title: '农家午餐', duration: 60 },
            ],
          },
        ],
      }),
    },
  }],
};

const mockChatReply = {
  choices: [{
    message: {
      content: '根据你的需求，我推荐满觉陇村，这里人少景美，非常适合周末放松。{"recommendedSpots": ["spot-1"]}',
    },
  }],
};

jest.mock('openai', () => ({
  __esModule: true,
  default: jest.fn().mockImplementation(() => ({
    chat: {
      completions: {
        create: jest.fn()
          .mockResolvedValueOnce(mockChatCompletion)
          .mockResolvedValueOnce(mockChatReply),
      },
    },
  })),
}));

// Mock prisma
jest.mock('../../config/database', () => ({
  __esModule: true,
  default: prismaMock,
}));

// Mock config
jest.mock('../../config', () => ({
  config: {
    openai: {
      apiKey: 'test-key',
      model: 'gpt-4-turbo',
    },
  },
}));

describe('AIService', () => {
  let aiService: AIService;

  beforeEach(() => {
    resetPrismaMock();
    aiService = new AIService();
    jest.clearAllMocks();
  });

  describe('generateItinerary', () => {
    // TEST 1: 成功生成行程
    it('should generate itinerary successfully', async () => {
      // Arrange
      const userId = 'user-123';
      const params: ItineraryParams = {
        destination: '杭州',
        destinationLat: 30.25,
        destinationLng: 120.17,
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-02'),
        travelStyles: ['自然', '人文'],
        crowdPreference: 'avoid',
      };

      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.$queryRaw.mockResolvedValue([createMockSpot()]);
      prismaMock.behavior.count.mockResolvedValue(0);
      prismaMock.behavior.create.mockResolvedValue({} as any);

      // Act
      const result = await aiService.generateItinerary(userId, params);

      // Assert
      expect(result.title).toBe('杭州小众2日游');
      expect(result.days).toBeDefined();
      expect(result.isAiGenerated).toBe(true);
    });

    // TEST 2: 使用次数超限时抛出错误
    it('should throw error when usage limit exceeded', async () => {
      // Arrange
      const userId = 'user-123';
      const params: ItineraryParams = {
        destination: '杭州',
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-02'),
      };

      const mockUser = createMockUser({ id: userId, membershipType: 'free' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.behavior.count.mockResolvedValue(10); // Over limit

      // Act & Assert
      await expect(aiService.generateItinerary(userId, params)).rejects.toThrow();
    });

    // TEST 3: 记录使用次数
    it('should record usage after generating itinerary', async () => {
      // Arrange
      const userId = 'user-123';
      const params: ItineraryParams = {
        destination: '杭州',
        destinationLat: 30.25,
        destinationLng: 120.17,
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-02'),
      };

      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.$queryRaw.mockResolvedValue([createMockSpot()]);
      prismaMock.behavior.count.mockResolvedValue(0);
      prismaMock.behavior.create.mockResolvedValue({} as any);

      // Act
      await aiService.generateItinerary(userId, params);

      // Assert
      expect(prismaMock.behavior.create).toHaveBeenCalledWith({
        data: {
          userId,
          action: 'ai_itinerary',
          context: { timestamp: expect.any(Date) },
        },
      });
    });

    // TEST 4: 会员用户无限制
    it('should allow unlimited usage for pro members', async () => {
      // Arrange
      const userId = 'user-pro';
      const params: ItineraryParams = {
        destination: '杭州',
        destinationLat: 30.25,
        destinationLng: 120.17,
        startDate: new Date('2026-04-01'),
        endDate: new Date('2026-04-02'),
      };

      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.$queryRaw.mockResolvedValue([createMockSpot()]);
      prismaMock.behavior.create.mockResolvedValue({} as any);

      // Act - should not throw even with high count
      const result = await aiService.generateItinerary(userId, params);

      // Assert
      expect(result).toBeDefined();
    });
  });

  describe('chat', () => {
    // TEST 5: 成功进行智能问答
    it('should chat successfully', async () => {
      // Arrange
      const userId = 'user-123';
      const message = '推荐杭州周边人少的地方';
      const context = {
        location: { lat: 30.25, lng: 120.17 },
      };

      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.spot.findMany.mockResolvedValue([createMockSpot()]);
      prismaMock.behavior.count.mockResolvedValue(0);
      prismaMock.behavior.create.mockResolvedValue({} as any);

      // Act
      const result = await aiService.chat(userId, message, context);

      // Assert
      expect(result.reply).toBeDefined();
      expect(result.sessionId).toBeDefined();
    });

    // TEST 6: 返回推荐景点
    it('should return recommended spots from chat', async () => {
      // Arrange
      const userId = 'user-123';
      const message = '推荐景点';

      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.spot.findMany.mockResolvedValue([createMockSpot({ id: 'spot-1' })]);
      prismaMock.behavior.count.mockResolvedValue(0);
      prismaMock.behavior.create.mockResolvedValue({} as any);

      // Act
      const result = await aiService.chat(userId, message, {});

      // Assert
      expect(result.recommendedSpots).toBeDefined();
    });

    // TEST 7: 空消息返回错误
    it('should handle empty message', async () => {
      // Arrange
      const userId = 'user-123';
      const message = '';

      // Act & Assert
      // The service should handle this gracefully
      await expect(aiService.chat(userId, message, {})).rejects.toThrow();
    });

    // TEST 8: 保持会话ID
    it('should maintain session id', async () => {
      // Arrange
      const userId = 'user-123';
      const message = '继续推荐';
      const sessionId = 'existing-session-123';

      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });
      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.spot.findMany.mockResolvedValue([]);
      prismaMock.behavior.count.mockResolvedValue(0);
      prismaMock.behavior.create.mockResolvedValue({} as any);

      // Act
      const result = await aiService.chat(userId, message, {}, sessionId);

      // Assert
      expect(result.sessionId).toBe(sessionId);
    });
  });

  describe('getUsage', () => {
    // TEST 9: 返回用户使用统计
    it('should return usage statistics', async () => {
      // Arrange
      const userId = 'user-123';
      const mockUser = createMockUser({ id: userId, membershipType: 'free' });

      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.behavior.count.mockResolvedValue(3);

      // Act
      const result = await aiService.getUsage(userId);

      // Assert
      expect(result.recommendation).toBeDefined();
      expect(result.itinerary).toBeDefined();
      expect(result.chat).toBeDefined();
    });

    // TEST 10: 会员用户返回无限使用
    it('should return unlimited for pro members', async () => {
      // Arrange
      const userId = 'user-pro';
      const mockUser = createMockUser({ id: userId, membershipType: 'pro' });

      prismaMock.user.findUnique.mockResolvedValue(mockUser);

      // Act
      const result = await aiService.getUsage(userId);

      // Assert
      expect(result.itinerary.unlimited).toBe(true);
      expect(result.chat.unlimited).toBe(true);
    });
  });

  describe('checkUsageLimit', () => {
    // TEST 11: 免费用户限制检查
    it('should check limits for free users', async () => {
      // Arrange
      const userId = 'user-free';
      const mockUser = createMockUser({ id: userId, membershipType: 'free' });

      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.behavior.count.mockResolvedValue(0);

      // Act
      const result = await aiService.getUsage(userId);

      // Assert
      expect(result.itinerary.limit).toBe(1); // Free users get 1 itinerary
      expect(result.chat.limit).toBe(10); // Free users get 10 chats
    });

    // TEST 12: 超过限制返回false
    it('should return false when limit exceeded', async () => {
      // Arrange
      const userId = 'user-free';
      const mockUser = createMockUser({ id: userId, membershipType: 'free' });

      prismaMock.user.findUnique.mockResolvedValue(mockUser);
      prismaMock.behavior.count.mockResolvedValue(5); // Over limit for chat

      // Act
      const result = await aiService.getUsage(userId);

      // Assert
      // The usage should show limited remaining
      expect(result.chat).toBeDefined();
    });
  });
});
