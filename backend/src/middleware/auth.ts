import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config';
import { Errors } from '../middleware/error';

export interface TokenPayload {
  userId: string;
  email: string;
  membershipType: string;
  iat: number;
  exp: number;
}

declare global {
  namespace Express {
    interface Request {
      user?: TokenPayload;
    }
  }
}

export function authMiddleware(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next(Errors.UNAUTHORIZED());
  }

  const token = authHeader.split(' ')[1];

  try {
    const payload = jwt.verify(token, config.jwt.secret) as TokenPayload;
    req.user = payload;
    next();
  } catch (error) {
    return next(Errors.TOKEN_EXPIRED());
  }
}

export function optionalAuth(req: Request, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;

  if (authHeader && authHeader.startsWith('Bearer ')) {
    const token = authHeader.split(' ')[1];

    try {
      const payload = jwt.verify(token, config.jwt.secret) as TokenPayload;
      req.user = payload;
    } catch (error) {
      // Ignore error for optional auth
    }
  }

  next();
}

export function requireMembership(types: string[]) {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      return next(Errors.UNAUTHORIZED());
    }

    if (!types.includes(req.user.membershipType)) {
      return next(Errors.MEMBER_REQUIRED());
    }

    next();
  };
}
