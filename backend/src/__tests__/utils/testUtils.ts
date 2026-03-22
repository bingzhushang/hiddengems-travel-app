// Test utilities and mocks

import { mockDeep, mockReset, DeepMockProxy } from 'jest-mock-extended';
import { PrismaClient } from '@prisma/client';
import { Request, Response, NextFunction } from 'express';

// Mock Prisma Client
export const prismaMock = mockDeep<PrismaClient>() as unknown as DeepMockProxy<PrismaClient>;

// Reset mock between tests
export const resetPrismaMock = () => mockReset(prismaMock);

// Mock Express objects
export const mockRequest = (overrides: Partial<Request> = {}): Request => {
  const req = {
    body: {},
    params: {},
    query: {},
    headers: {},
    ip: '127.0.0.1',
    connection: { remoteAddress: '127.0.0.1' },
    ...overrides,
  } as Request;
  return req;
};

export const mockResponse = (): Response & { json: jest.Mock; status: jest.Mock } => {
  const res = {
    json: jest.fn().mockReturnThis(),
    status: jest.fn().mockReturnThis(),
    send: jest.fn().mockReturnThis(),
    setHeader: jest.fn().mockReturnThis(),
  } as unknown as Response & { json: jest.Mock; status: jest.Mock };
  return res;
};

export const mockNext = (): NextFunction => {
  return jest.fn();
};

// Mock User data factory
export const createMockUser = (overrides: Partial<any> = {}) => ({
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

// Mock Spot data factory
export const createMockSpot = (overrides: Partial<any> = {}) => ({
  id: 'test-spot-id',
  name: 'Test Spot',
  nameEn: 'Test Spot',
  description: 'A test spot description',
  aiSummary: 'AI generated summary',
  country: '中国',
  province: '浙江省',
  city: '杭州市',
  district: '西湖区',
  address: 'Test Address',
  latitude: 30.25,
  longitude: 120.17,
  categoryId: 'category-id',
  tags: ['自然', '徒步'],
  themes: ['户外'],
  rating: 4.5,
  reviewCount: 100,
  viewCount: 1000,
  favoriteCount: 50,
  crowdLevel: 'low',
  crowdData: null,
  openingHours: null,
  ticketPrice: 0,
  ticketInfo: null,
  suggestedDuration: 120,
  bestSeasons: ['春季', '秋季'],
  bestTimeOfDay: ['早晨', '傍晚'],
  coverImage: 'https://example.com/image.jpg',
  images: [],
  source: 'manual',
  sourceUrl: null,
  verified: true,
  verifierId: null,
  status: 'active',
  createdAt: new Date(),
  updatedAt: new Date(),
  publishedAt: new Date(),
  ...overrides,
});

// Mock Itinerary data factory
export const createMockItinerary = (overrides: Partial<any> = {}) => ({
  id: 'test-itinerary-id',
  userId: 'test-user-id',
  title: 'Test Itinerary',
  description: 'Test Description',
  coverImage: null,
  startDate: new Date('2026-04-01'),
  endDate: new Date('2026-04-02'),
  daysCount: 2,
  destination: '杭州',
  destinationLat: 30.25,
  destinationLng: 120.17,
  budgetLevel: 'medium',
  estimatedBudget: 500,
  travelStyle: ['自然'],
  transportation: 'self_drive',
  isAiGenerated: true,
  aiParams: {},
  viewCount: 0,
  favoriteCount: 0,
  copyCount: 0,
  status: 'draft',
  isPublic: false,
  offlineDataUrl: null,
  offlineSize: null,
  createdAt: new Date(),
  updatedAt: new Date(),
  publishedAt: null,
  ...overrides,
});

// Helper to wait for async operations
export const waitFor = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Helper to generate random string
export const randomString = (length: number = 10) =>
  Math.random().toString(36).substring(2, length + 2);
