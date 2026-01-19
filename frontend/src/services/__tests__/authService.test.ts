import { describe, it, expect, vi, beforeEach } from 'vitest';
import { authService } from '../authService';
import api from '@/lib/api';

// Mock the api module
vi.mock('@/lib/api', () => {
  const mockPost = vi.fn();
  return {
    default: {
      post: mockPost,
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    },
    api: {
      post: mockPost,
      interceptors: {
        request: { use: vi.fn() },
        response: { use: vi.fn() },
      },
    },
  };
});

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('login', () => {
    it('should login successfully and return auth response', async () => {
      const mockResponse = {
        data: {
          token: 'test-jwt-token',
          username: 'testuser',
          email: 'test@example.com',
        },
      };

      vi.mocked(api.post).mockResolvedValue(mockResponse as any);

      const result = await authService.login({
        email: 'test@example.com',
        password: 'password123',
      });

      expect(api.post).toHaveBeenCalledWith('/auth/login', {
        email: 'test@example.com',
        password: 'password123',
      });

      expect(result).toEqual(mockResponse.data);
    });

    it('should handle login errors', async () => {
      const mockError = new Error('Invalid credentials');
      vi.mocked(api.post).mockRejectedValue(mockError);

      await expect(
        authService.login({
          email: 'test@example.com',
          password: 'wrongpassword',
        })
      ).rejects.toThrow();

      expect(api.post).toHaveBeenCalled();
    });
  });

  describe('register', () => {
    it('should register successfully and return auth response', async () => {
      const mockResponse = {
        data: {
          token: 'test-jwt-token',
          username: 'newuser',
          email: 'new@example.com',
        },
      };

      vi.mocked(api.post).mockResolvedValue(mockResponse as any);

      const result = await authService.register({
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123',
        fullName: 'New User',
      });

      expect(api.post).toHaveBeenCalledWith('/auth/register', {
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123',
        fullName: 'New User',
      });

      expect(result).toEqual(mockResponse.data);
    });

    it('should handle registration errors', async () => {
      const mockError = new Error('Username already exists');
      vi.mocked(api.post).mockRejectedValue(mockError);

      await expect(
        authService.register({
          username: 'existinguser',
          email: 'existing@example.com',
          password: 'password123',
        })
      ).rejects.toThrow();

      expect(api.post).toHaveBeenCalled();
    });
  });
});
