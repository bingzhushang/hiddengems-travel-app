import { Router, Request, Response, NextFunction } from 'express';
import aiService from '../services/ai.service';
import { authMiddleware } from '../middleware/auth';

const router = Router();

/**
 * @route POST /api/v1/ai/itinerary/generate
 * @desc AI生成行程
 */
router.post('/itinerary/generate', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user!.userId;
    const {
      destination,
      destinationLat,
      destinationLng,
      startDate,
      endDate,
      budgetLevel,
      travelStyles,
      crowdPreference,
      transportation,
      specialRequests,
    } = req.body;

    const result = await aiService.generateItinerary(userId, {
      destination,
      destinationLat,
      destinationLng,
      startDate: new Date(startDate),
      endDate: new Date(endDate),
      budgetLevel,
      travelStyles,
      crowdPreference,
      transportation,
      specialRequests,
    });

    res.json({
      code: 0,
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route POST /api/v1/ai/chat
 * @desc AI智能问答
 */
router.post('/chat', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user!.userId;
    const { message, sessionId } = req.body;

    if (!message) {
      return res.status(400).json({
        code: 20001,
        message: '请输入问题',
      });
    }

    const context = {
      location: req.body.location,
      preferences: req.user,
    };

    const result = await aiService.chat(userId, message, context, sessionId);

    res.json({
      code: 0,
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/ai/usage
 * @desc 获取AI使用次数
 */
router.get('/usage', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user!.userId;
    const usage = await aiService.getUsage(userId);

    res.json({
      code: 0,
      data: usage,
    });
  } catch (error) {
    next(error);
  }
});

export default router;
