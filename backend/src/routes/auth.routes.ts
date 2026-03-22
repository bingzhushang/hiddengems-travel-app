import { Router, Request, Response, NextFunction } from 'express';
import authService from '../services/auth.service';
import { AppError } from '../middleware/error';

const router = Router();

/**
 * @route POST /api/v1/auth/register
 * @desc 用户注册
 */
router.post('/register', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { email, password, nickname } = req.body;

    if (!email || !password || !nickname) {
      throw new AppError({ code: 20001, message: '请填写完整信息' });
    }

    const result = await authService.register({ email, password, nickname });

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
 * @route POST /api/v1/auth/login
 * @desc 用户登录
 */
router.post('/login', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { email, password, deviceToken } = req.body;

    if (!email || !password) {
      throw new AppError({ code: 20001, message: '请填写邮箱和密码' });
    }

    const result = await authService.login({ email, password, deviceToken });

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
 * @route POST /api/v1/auth/refresh
 * @desc 刷新Token
 */
router.post('/refresh', async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      throw new AppError({ code: 20001, message: '缺少refresh token' });
    }

    const result = await authService.refreshToken(refreshToken);

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
