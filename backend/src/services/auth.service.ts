import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import prisma from '../config/database';
import { config } from '../config';
import { AppError } from '../middleware/error';

export interface RegisterInput {
  email: string;
  password: string;
  nickname: string;
}

export interface LoginInput {
  email: string;
  password: string;
  deviceToken?: string;
}

export interface AuthResult {
  user: Record<string, any>;
  token: string;
  refreshToken: string;
}

export class AuthService {
  async register(data: RegisterInput): Promise<AuthResult> {
    // Check if user exists
    const existingUser = await prisma.user.findUnique({
      where: { email: data.email },
    });

    if (existingUser) {
      throw new AppError(20001, '该邮箱已被注册');
    }

    // Hash password
    const passwordHash = await bcrypt.hash(data.password, 10);

    // Create user
    const user = await prisma.user.create({
      data: {
        email: data.email,
        passwordHash,
        nickname: data.nickname,
      },
    });

    // Generate tokens
    const { token, refreshToken } = this.generateTokens(user);

    return {
      user: this.sanitizeUser(user),
      token,
      refreshToken,
    };
  }

  async login(data: LoginInput): Promise<AuthResult> {
    // Find user
    const user = await prisma.user.findUnique({
      where: { email: data.email },
    });

    if (!user) {
      throw new AppError(20002, '邮箱或密码错误');
    }

    // Verify password
    const isValid = await bcrypt.compare(data.password, user.passwordHash);
    if (!isValid) {
      throw new AppError(20002, '邮箱或密码错误');
    }

    // Update last login
    await prisma.user.update({
      where: { id: user.id },
      data: { lastLoginAt: new Date() },
    });

    // Generate tokens
    const { token, refreshToken } = this.generateTokens(user);

    return {
      user: this.sanitizeUser(user),
      token,
      refreshToken,
    };
  }

  async refreshToken(refreshToken: string): Promise<{ token: string; refreshToken: string }> {
    try {
      const payload = jwt.verify(refreshToken, config.jwt.secret) as { userId: string };

      const user = await prisma.user.findUnique({
        where: { id: payload.userId },
      });

      if (!user) {
        throw new AppError(30001, '请先登录');
      }

      return this.generateTokens(user);
    } catch (error) {
      throw new AppError(30002, '登录已过期，请重新登录');
    }
  }

  private generateTokens(user: { id: string; email: string; membershipType: string }): { token: string; refreshToken: string } {
    const payload = {
      userId: user.id,
      email: user.email,
      membershipType: user.membershipType,
    };

    // Parse expiresIn string (e.g., "2h", "7d") to seconds
    const parseExpiresIn = (value: string): number => {
      const match = value.match(/^(\d+)([hd])$/);
      if (!match) return 86400; // default 1 day
      const num = parseInt(match[1]);
      const unit = match[2];
      return unit === 'h' ? num * 3600 : num * 24 * 3600;
    };

    const token = jwt.sign(payload, config.jwt.secret as string, {
      expiresIn: parseExpiresIn(config.jwt.expiresIn as string)
    });

    const refreshToken = jwt.sign(payload, config.jwt.secret as string, {
      expiresIn: parseExpiresIn(config.jwt.refreshExpiresIn as string)
    });

    return { token, refreshToken };
  }

  private sanitizeUser(user: Record<string, any>): Record<string, any> {
    const { passwordHash, ...userWithoutPassword } = user;
    return userWithoutPassword;
  }
}

// Export singleton instance for production use
const authService = new AuthService();
export default authService;
