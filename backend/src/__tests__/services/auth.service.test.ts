import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { AppError } from '../../middleware/error';

// Create mock at module scope BEFORE any imports that use it
const mockPrisma = {
  user: {
    findUnique: jest.fn(),
    create: jest.fn(),
    update: jest.fn(),
  },
};

// Mock prisma
jest.mock('../../config/database', () => ({
  __esModule: true,
  default: mockPrisma,
}));

// Mock bcrypt
jest.mock('bcryptjs', () => ({
  hash: jest.fn().mockResolvedValue('$2a$10$hashedpassword'),
  compare: jest.fn(),
}));

// Mock jsonwebtoken
jest.mock('jsonwebtoken', () => ({
  sign: jest.fn().mockReturnValue('mock.jwt.token'),
  verify: jest.fn(),
}));

// Mock config
jest.mock('../../config', () => ({
  config: {
    jwt: {
      secret: 'test-jwt-secret-key',
      expiresIn: '2h',
      refreshExpiresIn: '7d',
    },
  },
}));

// NOW import the service after mocks are set up
import { AuthService } from '../../services/auth.service';

// Helper to create mock user
const createMockUser = (overrides: Partial<any> = {}) => ({
  id: 'test-user-id',
  email: 'test@example.com',
  passwordHash: '$2a$10$hashedpassword',
  nickname: 'Test User',
  avatar: null,
  phone: null,
  gender: null,
  birthday: null,
  bio: null,
  membershipType: 'free',
  membershipExpireAt: null,
  contributionPoints: 0,
  level: 1,
  preferences: {},
  status: 'active',
  emailVerified: false,
  createdAt: new Date(),
  updatedAt: new Date(),
  lastLoginAt: null,
  ...overrides,
});

describe('AuthService', () => {
  let authService: AuthService;

  beforeEach(() => {
    jest.clearAllMocks();
    authService = new AuthService();
  });

  describe('register', () => {
    // TEST 1: 成功注册新用户
    it('should register a new user successfully', async () => {
      // Arrange
      const input = {
        email: 'test@example.com',
        password: 'SecurePass123!',
        nickname: 'Test User',
      };

      const mockUser = createMockUser({
        email: input.email,
        nickname: input.nickname,
      });

      mockPrisma.user.findUnique.mockResolvedValue(null);
      mockPrisma.user.create.mockResolvedValue(mockUser);

      // Act
      const result = await authService.register(input);

      // Assert
      expect(result.user.email).toBe(input.email);
      expect(result.user.nickname).toBe(input.nickname);
      expect(result.token).toBeDefined();
      expect(result.refreshToken).toBeDefined();
      expect(bcrypt.hash).toHaveBeenCalledWith(input.password, 10);
    });

    // TEST 2: 邮箱已存在时抛出错误
    it('should throw error when email already exists', async () => {
      // Arrange
      const input = {
        email: 'existing@example.com',
        password: 'SecurePass123!',
        nickname: 'Test User',
      };

      mockPrisma.user.findUnique.mockResolvedValue(createMockUser({ email: input.email }));

      // Act & Assert
      await expect(authService.register(input)).rejects.toThrow('该邮箱已被注册');
    });

    // TEST 3: 密码应该被哈希
    it('should hash the password before storing', async () => {
      // Arrange
      const input = {
        email: 'test@example.com',
        password: 'plainPassword',
        nickname: 'Test User',
      };

      mockPrisma.user.findUnique.mockResolvedValue(null);
      mockPrisma.user.create.mockResolvedValue(createMockUser());

      // Act
      await authService.register(input);

      // Assert
      expect(bcrypt.hash).toHaveBeenCalledWith('plainPassword', 10);
    });

    // TEST 4: 返回的用户不应该包含密码哈希
    it('should not return password hash in user object', async () => {
      // Arrange
      const input = {
        email: 'test@example.com',
        password: 'SecurePass123!',
        nickname: 'Test User',
      };

      mockPrisma.user.findUnique.mockResolvedValue(null);
      mockPrisma.user.create.mockResolvedValue(createMockUser());

      // Act
      const result = await authService.register(input);

      // Assert
      expect(result.user).not.toHaveProperty('passwordHash');
    });
  });

  describe('login', () => {
    // TEST 5: 成功登录
    it('should login successfully with correct credentials', async () => {
      // Arrange
      const input = {
        email: 'test@example.com',
        password: 'correctPassword',
      };

      const mockUser = createMockUser({
        email: input.email,
        passwordHash: '$2a$10$hashedpassword',
      });

      mockPrisma.user.findUnique.mockResolvedValue(mockUser);
      mockPrisma.user.update.mockResolvedValue(mockUser);
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);

      // Act
      const result = await authService.login(input);

      // Assert
      expect(result.user.email).toBe(input.email);
      expect(result.token).toBeDefined();
      expect(result.refreshToken).toBeDefined();
    });

    // TEST 6: 用户不存在时抛出错误
    it('should throw error when user does not exist', async () => {
      // Arrange
      const input = {
        email: 'nonexistent@example.com',
        password: 'password',
      };

      mockPrisma.user.findUnique.mockResolvedValue(null);

      // Act & Assert
      await expect(authService.login(input)).rejects.toThrow('邮箱或密码错误');
    });

    // TEST 7: 密码错误时抛出错误
    it('should throw error when password is incorrect', async () => {
      // Arrange
      const input = {
        email: 'test@example.com',
        password: 'wrongPassword',
      };

      const mockUser = createMockUser();
      mockPrisma.user.findUnique.mockResolvedValue(mockUser);
      (bcrypt.compare as jest.Mock).mockResolvedValue(false);

      // Act & Assert
      await expect(authService.login(input)).rejects.toThrow('邮箱或密码错误');
    });

    // TEST 8: 登录成功后更新lastLoginAt
    it('should update lastLoginAt on successful login', async () => {
      // Arrange
      const input = {
        email: 'test@example.com',
        password: 'correctPassword',
      };

      const mockUser = createMockUser();
      mockPrisma.user.findUnique.mockResolvedValue(mockUser);
      mockPrisma.user.update.mockResolvedValue(mockUser);
      (bcrypt.compare as jest.Mock).mockResolvedValue(true);

      // Act
      await authService.login(input);

      // Assert
      expect(mockPrisma.user.update).toHaveBeenCalledWith({
        where: { id: mockUser.id },
        data: { lastLoginAt: expect.any(Date) },
      });
    });
  });

  describe('refreshToken', () => {
    // TEST 9: 刷新token成功
    it('should refresh tokens successfully', async () => {
      // Arrange
      const mockUser = createMockUser();
      const validRefreshToken = 'valid.refresh.token';

      (jwt.verify as jest.Mock).mockReturnValue({ userId: mockUser.id });
      mockPrisma.user.findUnique.mockResolvedValue(mockUser);

      // Act
      const result = await authService.refreshToken(validRefreshToken);

      // Assert
      expect(result.token).toBeDefined();
      expect(result.refreshToken).toBeDefined();
    });

    // TEST 10: 无效token时抛出错误
    it('should throw error for invalid refresh token', async () => {
      // Arrange
      (jwt.verify as jest.Mock).mockImplementation(() => {
        throw new Error('Invalid token');
      });

      // Act & Assert
      await expect(authService.refreshToken('invalid.token')).rejects.toThrow('登录已过期');
    });

    // TEST 11: 用户不存在时抛出错误
    it('should throw error when user not found for refresh token', async () => {
      // Arrange
      (jwt.verify as jest.Mock).mockReturnValue({ userId: 'non-existent-user' });
      mockPrisma.user.findUnique.mockResolvedValue(null);

      // Act & Assert - The service wraps all errors in a generic message
      await expect(authService.refreshToken('valid.token')).rejects.toThrow('登录已过期');
    });
  });

  describe('token generation', () => {
    // TEST 12: token应该包含正确的用户信息
    it('should generate token with correct user payload', async () => {
      // Arrange
      const mockUser = createMockUser({
        id: 'user-123',
        email: 'test@example.com',
        membershipType: 'pro',
      });

      mockPrisma.user.findUnique.mockResolvedValue(null);
      mockPrisma.user.create.mockResolvedValue(mockUser);

      // Act
      const result = await authService.register({
        email: 'test@example.com',
        password: 'password',
        nickname: 'Test',
      });

      // Assert
      expect(jwt.sign).toHaveBeenCalledWith(
        expect.objectContaining({
          userId: 'user-123',
          email: 'test@example.com',
          membershipType: 'pro',
        }),
        'test-jwt-secret-key',
        { expiresIn: 7200 } // 2h in seconds
      );
    });
  });
});
