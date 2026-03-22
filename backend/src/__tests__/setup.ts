// Jest setup file
import { beforeAll, afterAll, beforeEach } from '@jest/globals';

// Mock environment variables for testing
process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = 'test-jwt-secret-key';
process.env.JWT_EXPIRES_IN = '2h';
process.env.JWT_REFRESH_EXPIRES_IN = '7d';
process.env.DATABASE_URL = 'postgresql://test:test@localhost:5432/hiddengems_test?schema=public';
process.env.OPENAI_API_KEY = 'test-openai-key';

// Extend Jest matchers if needed
expect.extend({
  toBeValidUUID(received: string) {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    const pass = uuidRegex.test(received);
    return {
      pass,
      message: () => pass
        ? `expected ${received} not to be a valid UUID`
        : `expected ${received} to be a valid UUID`,
    };
  },
});

// Global setup
beforeAll(async () => {
  // Any global setup
});

// Global teardown
afterAll(async () => {
  // Any global teardown
});

// Clean up after each test
beforeEach(() => {
  jest.clearAllMocks();
});

// Export for type declarations
declare global {
  namespace jest {
    interface Matchers<R> {
      toBeValidUUID(): R;
    }
  }
}
