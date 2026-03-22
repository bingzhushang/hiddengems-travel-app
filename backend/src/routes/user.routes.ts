import { Router } from 'express';
import { authMiddleware } from '../middleware/auth';

const router = Router();

/**
 * @route GET /api/v1/users/me
 * @desc 获取当前用户信息
 */
router.get('/me', authMiddleware, async (req, res) => {
  // TODO: Implement get user info
  res.json({
    code: 0,
    data: {
      id: req.user?.userId,
      email: req.user?.email,
      membershipType: req.user?.membershipType,
    },
  });
});

/**
 * @route PATCH /api/v1/users/me
 * @desc 更新用户信息
 */
router.patch('/me', authMiddleware, async (req, res) => {
  // TODO: Implement update user
  res.json({
    code: 0,
    message: 'success',
  });
});

export default router;
