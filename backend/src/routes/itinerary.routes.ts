import { Router, Request, Response, NextFunction } from 'express';
import { authMiddleware, optionalAuth } from '../middleware/auth';
import itineraryService from '../services/itinerary.service';

const router = Router();

/**
 * @route GET /api/v1/itineraries/me
 * @desc 获取我的行程列表
 */
router.get('/me', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user!.userId;
    const { page, pageSize, status } = req.query;

    const result = await itineraryService.getByUserId(
      userId,
      page ? parseInt(page as string, 10) : 1,
      pageSize ? parseInt(pageSize as string, 10) : 10,
      status as string
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
 * @route POST /api/v1/itineraries
 * @desc 创建行程
 */
router.post('/', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const userId = req.user!.userId;
    const itinerary = await itineraryService.create(userId, req.body);

    res.status(201).json({
      code: 0,
      data: itinerary,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route GET /api/v1/itineraries/:itineraryId
 * @desc 获取行程详情
 */
router.get('/:itineraryId', optionalAuth, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user?.userId;

    const itinerary = await itineraryService.getById(itineraryId, userId);

    if (!itinerary) {
      return res.status(404).json({
        code: 10001,
        message: '行程不存在',
      });
    }

    res.json({
      code: 0,
      data: itinerary,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route PUT /api/v1/itineraries/:itineraryId
 * @desc 更新行程
 */
router.put('/:itineraryId', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user!.userId;

    const itinerary = await itineraryService.update(itineraryId, userId, req.body);

    res.json({
      code: 0,
      data: itinerary,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route DELETE /api/v1/itineraries/:itineraryId
 * @desc 删除行程
 */
router.delete('/:itineraryId', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user!.userId;

    await itineraryService.delete(itineraryId, userId);

    res.json({
      code: 0,
      message: '删除成功',
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route POST /api/v1/itineraries/:itineraryId/items
 * @desc 添加行程项目
 */
router.post('/:itineraryId/items', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user!.userId;

    const item = await itineraryService.addItem(itineraryId, userId, req.body);

    res.status(201).json({
      code: 0,
      data: item,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route DELETE /api/v1/itineraries/:itineraryId/items/:itemId
 * @desc 删除行程项目
 */
router.delete('/:itineraryId/items/:itemId', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId, itemId } = req.params;
    const userId = req.user!.userId;

    await itineraryService.removeItem(itineraryId, itemId, userId);

    res.json({
      code: 0,
      message: '删除成功',
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route POST /api/v1/itineraries/:itineraryId/publish
 * @desc 发布行程
 */
router.post('/:itineraryId/publish', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user!.userId;

    const itinerary = await itineraryService.publish(itineraryId, userId);

    res.json({
      code: 0,
      data: itinerary,
    });
  } catch (error) {
    next(error);
  }
});

/**
 * @route POST /api/v1/itineraries/:itineraryId/copy
 * @desc 复制行程
 */
router.post('/:itineraryId/copy', authMiddleware, async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { itineraryId } = req.params;
    const userId = req.user!.userId;

    const itinerary = await itineraryService.copy(itineraryId, userId);

    res.status(201).json({
      code: 0,
      data: itinerary,
    });
  } catch (error) {
    next(error);
  }
});

export default router;
