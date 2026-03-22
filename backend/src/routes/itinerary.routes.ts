import { Router } from 'express';
import { authMiddleware } from '../middleware/auth';

const router = Router();

/**
 * @route GET /api/v1/itineraries/me
 * @desc 获取我的行程列表
 */
router.get('/me', authMiddleware, async (req, res) => {
  // TODO: Implement
  res.json({
    code: 0,
    data: {
      items: [],
      pagination: { page: 1, pageSize: 10, total: 0 },
    },
  });
});

/**
 * @route POST /api/v1/itineraries
 * @desc 创建行程
 */
router.post('/', authMiddleware, async (req, res) => {
  // TODO: Implement
  res.status(201).json({
    code: 0,
    data: { id: 'new-itinerary-id' },
  });
});

/**
 * @route GET /api/v1/itineraries/:itineraryId
 * @desc 获取行程详情
 */
router.get('/:itineraryId', async (req, res) => {
  // TODO: Implement
  res.json({
    code: 0,
    data: null,
  });
});

export default router;
