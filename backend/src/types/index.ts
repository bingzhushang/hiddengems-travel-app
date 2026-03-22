// API Response Types

export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data?: T;
  meta?: ResponseMeta;
}

export interface ResponseMeta {
  timestamp: string;
  requestId: string;
}

export interface PaginatedResponse<T> {
  items: T[];
  pagination: Pagination;
}

export interface Pagination {
  page: number;
  pageSize: number;
  total: number;
  totalPages: number;
  hasMore: boolean;
}

// Error Types

export interface ApiError {
  code: number;
  message: string;
  error?: ErrorDetail;
}

export interface ErrorDetail {
  type: string;
  details?: string;
  fields?: Record<string, string>;
}

// User Types

export interface UserPayload {
  userId: string;
  email: string;
  membershipType: string;
}

export interface UserPreferences {
  tags?: string[];
  budget?: 'low' | 'medium' | 'high';
  crowdPreference?: 'avoid' | 'neutral';
}

// Spot Types

export interface SpotListItem {
  id: string;
  name: string;
  coverImage: string | null;
  rating: number;
  distance?: number;
  crowdLevel: string;
  tags: string[];
  city: string | null;
  province: string | null;
  aiReason?: string;
}

export interface SpotDetail extends SpotListItem {
  nameEn: string | null;
  description: string | null;
  aiSummary: string | null;
  location: {
    lat: number;
    lng: number;
    country: string;
    province: string | null;
    city: string | null;
    district: string | null;
    address: string | null;
  };
  category: {
    id: string;
    name: string;
    slug: string;
  } | null;
  reviewCount: number;
  crowdData: CrowdData | null;
  openingHours: Record<string, OpeningHours> | null;
  ticketPrice: number | null;
  ticketInfo: string | null;
  suggestedDuration: number | null;
  bestSeasons: string[];
  bestTimeOfDay: string[];
  images: SpotImage[];
  isFavorited: boolean;
  nearbySpots: NearbySpot[];
}

export interface CrowdData {
  current: string;
  forecast?: Record<string, string>;
}

export interface OpeningHours {
  open: string;
  close: string;
}

export interface SpotImage {
  url: string;
  caption?: string;
}

export interface NearbySpot {
  id: string;
  name: string;
  distance: number;
}

// Itinerary Types

export interface ItineraryGenerateParams {
  destination: string;
  destinationLocation?: {
    lat: number;
    lng: number;
  };
  startDate: string;
  endDate: string;
  budgetLevel?: 'low' | 'medium' | 'high';
  travelStyles?: string[];
  crowdPreference?: 'avoid' | 'neutral';
  transportation?: 'self_drive' | 'public' | 'mixed';
  specialRequests?: string;
}

export interface ItineraryItem {
  id: string;
  dayNumber: number;
  orderInDay: number;
  spot?: {
    id: string;
    name: string;
    coverImage: string | null;
  } | null;
  spotName?: string | null;
  startTime?: string | null;
  endTime?: string | null;
  duration?: number | null;
  itemType: string;
  customTitle?: string | null;
  customContent?: string | null;
  estimatedCost?: number | null;
  notes?: string | null;
  aiNote?: string;
}

// AI Types

export interface AIRecommendationContext {
  location?: {
    lat: number;
    lng: number;
  };
  preferences?: UserPreferences;
  currentSeason?: string;
}

export interface ChatContext {
  sessionId?: string;
  location?: {
    lat: number;
    lng: number;
  };
  userPreferences?: UserPreferences;
  history?: ChatMessage[];
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}
