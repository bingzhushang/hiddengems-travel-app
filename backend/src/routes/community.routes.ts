import { Router } from 'express';
import { authMiddleware, optionalAuth } from '../middleware/auth';

const router = Router();

/**
 * @route GET /api/v1/community/feed
 * @desc 获取社区动态流
 */
router.get('/feed', optionalAuth, async (req, res) => {
  // TODO: Implement
  res.json({
    code: 0,
    data: {
      items: [],
      pagination: { page: 1, pageSize: 20, total: 0 },
    },
  });
});

/**
 * @route POST /api/v1/community/posts
 * @desc 发布帖子
 */
router.post('/posts', authMiddleware, async (req, res) => {
  // TODO: Implement
  res.status(201).json({
    code: 0,
    data: { id: 'new-post-id' },
  });
});

export default router;
