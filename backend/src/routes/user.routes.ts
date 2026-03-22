import { Router, Request, Response, NextFunction } from 'express';
import userService from '../services/user.service';
import { AppError } from '../middleware/error';
import { authMiddleware } from '../middleware/auth';

const router = Router();

// All user routes require authentication
router.use(authMiddleware);

/**
 * @route GET /api/v1/users/me
 * @desc 获取当前用户信息
 */
router.get('/me', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const user = await userService.getById(userId);
    res.json({
      code: 0,
      message: 'success',
      data: user,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route PATCH /api/v1/users/me
 * @desc 更新当前用户信息
 */
router.patch('/me', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const { nickname, avatar, bio, preferences } = req.body;

    const user = await userService.update(userId, {
      nickname,
      avatar,
      bio,
      preferences,
    });

    res.json({
      code: 0,
      message: 'success',
      data: user,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/users/me/stats
 * @desc 获取当前用户统计数据
 */
router.get('/me/stats', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const stats = await userService.getStats(userId);
    res.json({
      code: 0,
      message: 'success',
      data: stats,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/users/me/favorites
 * @desc 获取当前用户收藏的景点
 */
router.get('/me/favorites', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const page = parseInt(req.query.page as string) || 1;
    const pageSize = parseInt(req.query.pageSize as string) || 20;

    const result = await userService.getFavorites(userId, page, pageSize);
    res.json({
      code: 0,
      message: 'success',
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/users/me/itineraries
 * @desc 获取当前用户的行程
 */
router.get('/me/itineraries', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const status = req.query.status as string | undefined;
    const page = parseInt(req.query.page as string) || 1;
    const pageSize = parseInt(req.query.pageSize as string) || 10;

    const result = await userService.getItineraries(userId, status, page, pageSize);
    res.json({
      code: 0,
      message: 'success',
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

export default router;
