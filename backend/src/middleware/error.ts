import { Request, Response, NextFunction } from 'express';
import { ApiResponse, ApiError } from '../types';

export class AppError extends Error {
  constructor(
    public code: number,
    public message: string,
    public detail?: string
  ) {
    super(message);
  }
}

export function errorHandler(
  err: Error | AppError,
  req: Request,
  res: Response,
  _next: NextFunction
) {
  console.error('Error:', err);

  if (err instanceof AppError) {
    const response: ApiResponse = {
      code: err.code,
      message: err.message,
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.headers['x-request-id'] as string || 'unknown',
      },
    };

    if (err.detail) {
      response.data = {
        error: {
          type: err.constructor.name,
          details: err.detail,
        },
      };
    }

    return res.status(getStatusCode(err.code)).json(response);
  }

  // Unknown error
  const response: ApiResponse = {
    code: 99999,
    message: 'Internal Server Error',
    meta: {
      timestamp: new Date().toISOString(),
      requestId: req.headers['x-request-id'] as string || 'unknown',
    },
  };

  if (process.env.NODE_ENV === 'development') {
    response.data = {
      error: {
        type: 'UnknownError',
        details: err.message,
        stack: err.stack,
      },
    };
  }

  return res.status(500).json(response);
}

function getStatusCode(code: number): number {
  if (code >= 10000 && code < 11000) return 404;
  if (code >= 20000 && code < 21000) return 400;
  if (code >= 30000 && code < 31000) return 401;
  if (code >= 40000 && code < 41000) return 403;
  if (code >= 50000 && code < 51000) return 400;
  return 500;
}

// Common errors
export const Errors = {
  NOT_FOUND: (resource: string) => new AppError(10001, `${resource}不存在`),
  INVALID_PARAMS: (detail: string) => new AppError(20001, '参数错误', detail),
  UNAUTHORIZED: () => new AppError(30001, '请先登录'),
  TOKEN_EXPIRED: () => new AppError(30002, '登录已过期，请重新登录'),
  FORBIDDEN: () => new AppError(40001, '无权访问'),
  MEMBER_REQUIRED: () => new AppError(40002, '此功能需要会员'),
  RATE_LIMIT: () => new AppError(50001, '请求过于频繁，请稍后再试'),
  AI_LIMIT: () => new AppError(50002, '今日AI使用次数已用完'),
};
