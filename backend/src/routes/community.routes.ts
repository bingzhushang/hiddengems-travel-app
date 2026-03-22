import { Router, Request, Response, NextFunction } from 'express';
import communityService from '../services/community.service';
import { AppError } from '../middleware/error';
import { authMiddleware, optionalAuth } from '../middleware/auth';

const router = Router();

/**
 * @route GET /api/v1/community/feed
 * @desc 获取社区动态
 */
router.get('/feed', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const type = req.query.type as 'hot' | 'new' | 'following' || 'hot';
    const page = parseInt(req.query.page as string) || 1;
    const pageSize = parseInt(req.query.pageSize as string) || 20;
    const userId = req.user?.userId;

    const result = await communityService.getFeed({
      type,
      page,
      pageSize,
      userId,
    });

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
 * @route GET /api/v1/community/itineraries/:itineraryId
 * @desc 获取社区行程详情
 */
router.get('/itineraries/:itineraryId', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user?.userId;

    const result = await communityService.getItineraryDetail(itineraryId, userId);
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
 * @route POST /api/v1/community/itineraries/:itineraryId/favorite
 * @desc 收藏/取消收藏行程
 */
router.post('/itineraries/:itineraryId/favorite', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const { itineraryId } = req.params;
    const result = await communityService.toggleFavorite(userId, itineraryId);

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
 * @route POST /api/v1/community/itineraries/:itineraryId/copy
 * @desc 复制行程到自己的账户
 */
router.post('/itineraries/:itineraryId/copy', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const { itineraryId } = req.params;
    const result = await communityService.copyItinerary(userId, itineraryId);

    res.status(201).json({
      code: 0,
      message: 'success',
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route POST /api/v1/community/posts
 * @desc 发布帖子
 */
router.post('/posts', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user?.userId;
    if (!userId) {
      throw new AppError(30001, '请先登录');
    }

    const { title, content, postType, spotId, spotIds, images, tags } = req.body;

    // Create post
    const result = await communityService.createPost(userId, {
      title,
      content,
      postType: postType || 'article',
      spotId,
      spotIds,
      images,
      tags,
    });

    res.status(201).json({
      code: 0,
      message: 'success',
      data: result,
    });
  } catch (error) {
    next(error);
  }
});

export default router;
