import OpenAI from 'openai';
import prisma from '../config/database';
import { config } from '../config';
import { AppError } from '../middleware/error';

export interface ItineraryParams {
  destination: string;
  destinationLat?: number;
  destinationLng?: number;
  startDate: Date;
  endDate: Date;
  budgetLevel?: string;
  travelStyles?: string[];
  crowdPreference?: string;
  transportation?: string;
  specialRequests?: string;
}

export interface ChatContext {
  sessionId?: string;
  location?: {
    lat: number;
    lng: number;
  };
  userPreferences?: Record<string, any>;
  history?: ChatMessage[];
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ItineraryResult {
  title: string;
  description?: string;
  days: DaySchedule[];
  isAiGenerated: boolean;
  aiParams: ItineraryParams;
  estimatedBudget?: number;
}

export interface DaySchedule {
  day: number;
  date: string;
  activities: Activity[];
}

export interface Activity {
  order: number;
  time: string;
  type: string;
  spotId?: string;
  title: string;
  description?: string;
  duration?: number;
  estimatedCost?: number;
}

export interface ChatResult {
  reply: string;
  recommendedSpots: string[];
  sessionId: string;
}

export interface UsageInfo {
  used: number;
  limit: number;
  unlimited: boolean;
}

export interface UsageResult {
  recommendation: UsageInfo;
  itinerary: UsageInfo;
  chat: UsageInfo;
}

const USAGE_LIMITS: Record<string, Record<string, number>> = {
  free: { itinerary: 1, chat: 10, recommendation: 3 },
  explorer: { itinerary: 5, chat: 50, recommendation: 100 },
  pro: { itinerary: -1, chat: -1, recommendation: -1 }, // unlimited
};

export class AIService {
  private openai: OpenAI;

  constructor() {
    this.openai = new OpenAI({
      apiKey: process.env.OPENAI_API_KEY,
    });
  }

  async generateItinerary(
    userId: string,
    params: ItineraryParams
  ): Promise<ItineraryResult> {
    // Check usage limit
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: { membershipType: true },
    });

    const canUse = await this.checkUsageLimit(userId, 'itinerary', user?.membershipType || 'free');
    if (!canUse.allowed) {
      throw new AppError(50002, '今日AI使用次数已用完');
    }

    const daysCount = Math.ceil(
      (params.endDate.getTime() - params.startDate.getTime()) / (1000 * 60 * 60 * 24)
    ) + 1;

    // Get spots near destination
    const spots = await this.getSpotsForItinerary(params);

    // Generate itinerary using LLM
    const prompt = this.buildItineraryPrompt(params, spots, daysCount);

    const response = await this.openai.chat.completions.create({
      model: 'gpt-4-turbo',
      messages: [
        {
          role: 'system',
          content: `你是一个专业的旅行规划助手。根据用户需求生成详细的旅行行程。
输出JSON格式，包含:
- title: 行程标题
- description: 行程简介
- days: 数组，每天包含:
  - day: 第几天
  - date: 日期
  - activities: 活动列表，每个包含:
    - order: 顺序
    - time: 时间 (HH:MM)
    - type: 类型 (spot/meal/note)
    - spotId: 景点ID (如果是景点)
    - title: 标题
    - description: 描述
    - duration: 时长(分钟)
    - estimatedCost: 预估费用`,
        },
        {
          role: 'user',
          content: prompt,
        },
      ],
      response_format: { type: 'json_object' },
      temperature: 0.7,
    });

    const result = JSON.parse(response.choices[0].message.content || '{}');

    // Record usage
    await this.recordUsage(userId, 'itinerary');

    return {
      ...result,
      isAiGenerated: true,
      aiParams: params,
    };
  }

  async chat(
    userId: string,
    message: string,
    context: ChatContext,
    sessionId?: string
  ): Promise<ChatResult> {
    if (!message || message.trim() === '') {
      throw new AppError(20001, '消息不能为空');
    }

    // Check usage limit
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: { membershipType: true },
    });

    const canUse = await this.checkUsageLimit(userId, 'chat', user?.membershipType || 'free');
    if (!canUse.allowed) {
      throw new AppError(50002, '今日AI使用次数已用完');
    }

    // Search for relevant spots
    const relevantSpots = await this.searchRelevantSpots(message, context);

    // Build prompt with context
    const systemPrompt = `你是一个专业的旅行助手，帮助用户发现小众旅行目的地。
根据用户问题和参考信息提供建议。

参考景点:
${relevantSpots.map((s: any) => `- ${s.name}: ${s.description?.slice(0, 100) || '暂无描述'}`).join('\n')}

回答要求:
1. 推荐具体的景点时说明理由
2. 回答简洁实用
3. 如果推荐景点，在回答末尾添加JSON格式的推荐列表:
{"recommendedSpots": ["spotId1", "spotId2"]}`;

    const response = await this.openai.chat.completions.create({
      model: 'gpt-4-turbo',
      messages: [
        { role: 'system', content: systemPrompt },
        { role: 'user', content: message },
      ],
      temperature: 0.7,
    });

    const reply = response.choices[0].message.content || '';

    // Extract recommended spots
    let recommendedSpots: string[] = [];
    try {
      const match = reply.match(/\{"recommendedSpots":\s*\[.*?\]\}/);
      if (match) {
        const parsed = JSON.parse(match[0]);
        recommendedSpots = parsed.recommendedSpots;
      }
    } catch (e) {
      // Ignore parsing errors
    }

    // Record usage
    await this.recordUsage(userId, 'chat');

    return {
      reply: reply.replace(/\{"recommendedSpots":\s*\[.*?\]\}/, '').trim(),
      recommendedSpots,
      sessionId: sessionId || `session_${Date.now()}`,
    };
  }

  async getUsage(userId: string): Promise<UsageResult> {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: { membershipType: true },
    });

    const membershipType = user?.membershipType || 'free';
    const limits = USAGE_LIMITS[membershipType];

    const types: Array<'recommendation' | 'itinerary' | 'chat'> = ['recommendation', 'itinerary', 'chat'];
    const result: UsageResult = {} as UsageResult;

    for (const type of types) {
      const limit = limits[type];
      const unlimited = limit === -1;

      let used = 0;
      if (!unlimited) {
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        used = await prisma.behavior.count({
          where: {
            userId,
            action: `ai_${type}`,
            createdAt: { gte: today },
          },
        });
      }

      result[type] = {
        used,
        limit,
        unlimited,
      };
    }

    return result;
  }

  private async getSpotsForItinerary(params: ItineraryParams): Promise<any[]> {
    if (!params.destinationLat || !params.destinationLng) {
      return [];
    }

    const spots = await prisma.$queryRaw`
      SELECT id, name, description, tags, rating, suggested_duration, ticket_price
      FROM spots
      WHERE status = 'active'
        AND crowd_level != 'high'
        AND ST_DWithin(
          ST_MakePoint(longitude, latitude)::geography,
          ST_MakePoint(${params.destinationLng}, ${params.destinationLat})::geography,
          50000
        )
      ORDER BY rating DESC
      LIMIT 30
    `;

    return spots as any[];
  }

  private buildItineraryPrompt(params: ItineraryParams, spots: any[], daysCount: number): string {
    return `
请为以下需求生成旅行行程:

目的地: ${params.destination}
开始日期: ${params.startDate.toISOString().split('T')[0]}
结束日期: ${params.endDate.toISOString().split('T')[0]}
天数: ${daysCount}
预算水平: ${params.budgetLevel || '中等'}
旅行风格: ${params.travelStyles?.join('、') || '休闲'}
人流偏好: ${params.crowdPreference === 'avoid' ? '避开人流' : '不限'}
交通方式: ${params.transportation || '不限'}
特殊要求: ${params.specialRequests || '无'}

可选景点:
${spots.map((s: any) => `- ID: ${s.id}, 名称: ${s.name}, 标签: ${s.tags?.join('/')}, 评分: ${s.rating}, 建议时长: ${s.suggested_duration || 60}分钟`).join('\n')}

请生成详细的每日行程安排。
`;
  }

  private async searchRelevantSpots(message: string, context: ChatContext): Promise<any[]> {
    const keywords = message.toLowerCase();

    const spots = await prisma.spot.findMany({
      where: {
        status: 'active',
        OR: [
          { name: { contains: keywords, mode: 'insensitive' } },
          { description: { contains: keywords, mode: 'insensitive' } },
          { tags: { hasSome: [keywords] } },
        ],
      },
      take: 5,
    });

    return spots;
  }

  private async checkUsageLimit(
    userId: string,
    type: string,
    membershipType: string
  ): Promise<{ allowed: boolean; remaining: number }> {
    const limits = USAGE_LIMITS[membershipType] || USAGE_LIMITS.free;
    const limit = limits[type];

    if (limit === -1) {
      return { allowed: true, remaining: -1 };
    }

    // Check today's usage
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const usage = await prisma.behavior.count({
      where: {
        userId,
        action: `ai_${type}`,
        createdAt: { gte: today },
      },
    });

    return {
      allowed: usage < limit,
      remaining: Math.max(0, limit - usage),
    };
  }

  private async recordUsage(userId: string, type: string): Promise<void> {
    await prisma.behavior.create({
      data: {
        userId,
        action: `ai_${type}`,
        context: { timestamp: new Date() },
      },
    });
  }
}

export default new AIService();
