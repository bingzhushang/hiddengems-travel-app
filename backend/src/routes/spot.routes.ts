import { Router, Request, Response, NextFunction } from 'express';
import spotService from '../services/spot.service';
import { optionalAuth, authMiddleware } from '../middleware/auth';

const router = Router();

/**
 * @route GET /api/v1/spots/recommendations
 * @desc 获取AI推荐景点
 */
router.get('/recommendations', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { lat, lng, limit = 10 } = req.query;

    if (!lat || !lng) {
      return res.json({
        code: 0,
        data: { items: [] },
      });
    }

    const userId = req.user?.userId || null;
    const items = await spotService.getRecommendations(
      userId,
      parseFloat(lat as string),
      parseFloat(lng as string),
      parseInt(limit as string, 10)
    );

    res.json({
      code: 0,
      data: { items },
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/spots/nearby
 * @desc 获取附近景点
 */
router.get('/nearby', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { lat, lng, radius, category, tags, crowdLevel, page, pageSize, sortBy } = req.query;

    const result = await spotService.getNearby({
      lat: lat ? parseFloat(lat as string) : undefined,
      lng: lng ? parseFloat(lng as string) : undefined,
      radius: radius ? parseFloat(radius as string) : 50,
      category: category as string,
      crowdLevel: crowdLevel as string,
      page: page ? parseInt(page as string, 10) : 1,
      pageSize: pageSize ? parseInt(pageSize as string, 10) : 20,
      sortBy: sortBy as any,
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
 * @route GET /api/v1/spots/search
 * @desc 搜索景点
 */
router.get('/search', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { q, page, pageSize } = req.query;

    if (!q) {
      return res.json({
        code: 0,
        data: { items: [], pagination: { page: 1, pageSize: 20, total: 0 } },
      });
    }

    const result = await spotService.search(
      q as string,
      {},
      page ? parseInt(page as string, 10) : 1,
      pageSize ? parseInt(pageSize as string, 10) : 20
    );

    res.json({
      code: 0,
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/spots/:spotId
 * @desc 获取景点详情
 */
router.get('/:spotId', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { spotId } = req.params;
    const userId = req.user?.userId;

    const spot = await spotService.getById(spotId, userId);

    res.json({
      code: 0,
      data: spot,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route POST /api/v1/spots/:spotId/favorite
 * @desc 收藏景点
 */
router.post('/:spotId/favorite', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { spotId } = req.params;
    const userId = req.user!.userId;

    // TODO: Implement favorite logic

    res.json({
      code: 0,
      message: '已收藏',
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route DELETE /api/v1/spots/:spotId/favorite
 * @desc 取消收藏
 */
router.delete('/:spotId/favorite', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { spotId } = req.params;
    const userId = req.user!.userId;

    // TODO: Implement unfavorite logic

    res.json({
      code: 0,
      message: '已取消收藏',
    });
  } catch (error) {
    next(error);
  }
});

export default router;
