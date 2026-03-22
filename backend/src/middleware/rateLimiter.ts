import { Request, Response, NextFunction } from 'express';

interface RateLimitStore {
  [key: string]: {
    count: number;
    resetAt: number;
  };
}

const store: RateLimitStore = {};

const WINDOW_MS = 60 * 1000; // 1 minute
const MAX_REQUESTS = {
  default: 100,
  authenticated: 200,
};

export function rateLimiter(req: Request, res: Response, next: NextFunction) {
  const userId = (req as any).user?.userId;
  const ip = req.ip || req.connection.remoteAddress || 'unknown';

  const key = userId ? `user:${userId}` : `ip:${ip}`;
  const maxRequests = userId ? MAX_REQUESTS.authenticated : MAX_REQUESTS.default;

  const now = Date.now();

  if (!store[key] || store[key].resetAt < now) {
    store[key] = {
      count: 1,
      resetAt: now + WINDOW_MS,
    };
    return next();
  }

  store[key].count++;

  if (store[key].count > maxRequests) {
    return res.status(429).json({
      code: 50001,
      message: '请求过于频繁，请稍后再试',
      meta: {
        timestamp: new Date().toISOString(),
        requestId: req.headers['x-request-id'] || 'unknown',
      },
    });
  }

  // Set rate limit headers
  res.setHeader('X-RateLimit-Limit', maxRequests);
  res.setHeader('X-RateLimit-Remaining', maxRequests - store[key].count);
  res.setHeader('X-RateLimit-Reset', store[key].resetAt);

  next();
}

// Cleanup old entries periodically
setInterval(() => {
  const now = Date.now();
  for (const key of Object.keys(store)) {
    if (store[key].resetAt < now) {
      delete store[key];
    }
  }
}, 60 * 1000);
