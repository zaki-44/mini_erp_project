import type { Order, User, DeliveryHistory } from '../types';
import type { RegisterData } from '../components/RegisterPage';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:3000/api';

// Helper function to get auth token
const getAuthToken = (): string | null => {
  return localStorage.getItem('authToken');
};

// Helper function to set auth token
const setAuthToken = (token: string): void => {
  localStorage.setItem('authToken', token);
};

// Helper function to remove auth token
const removeAuthToken = (): void => {
  localStorage.removeItem('authToken');
};

// Generic fetch wrapper with auth
async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'An error occurred' }));
    throw new Error(error.message || `HTTP error! status: ${response.status}`);
  }

  return response.json();
}

// Auth API
export interface LoginResponse {
  user: {
    id: string;
    name: string;
    email: string;
    type: 'admin' | 'client' | 'livreur';
  };
  token: string;
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const response = await apiFetch<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
  
  if (response.token) {
    setAuthToken(response.token);
  }
  
  return response;
}

export async function register(userData: RegisterData): Promise<{ message: string }> {
  return apiFetch<{ message: string }>('/auth/register', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

export function logout(): void {
  removeAuthToken();
}

// Orders API
export async function fetchOrders(): Promise<Order[]> {
  return apiFetch<Order[]>('/orders');
}

export async function fetchOrderById(orderId: string): Promise<Order> {
  return apiFetch<Order>(`/orders/${orderId}`);
}

export async function createOrder(orderData: Partial<Order>): Promise<Order> {
  return apiFetch<Order>('/orders', {
    method: 'POST',
    body: JSON.stringify(orderData),
  });
}

export async function updateOrderStatus(
  orderId: string,
  status: Order['status']
): Promise<Order> {
  return apiFetch<Order>(`/orders/${orderId}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  });
}

export async function deleteOrder(orderId: string): Promise<void> {
  return apiFetch<void>(`/orders/${orderId}`, {
    method: 'DELETE',
  });
}

// Users API
export async function fetchUsers(): Promise<User[]> {
  return apiFetch<User[]>('/users');
}

export async function fetchUserById(userId: string): Promise<User> {
  return apiFetch<User>(`/users/${userId}`);
}

export async function createUser(userData: Partial<User>): Promise<User> {
  return apiFetch<User>('/users', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

export async function updateUser(userId: string, userData: Partial<User>): Promise<User> {
  return apiFetch<User>(`/users/${userId}`, {
    method: 'PATCH',
    body: JSON.stringify(userData),
  });
}

export async function deleteUser(userId: string): Promise<void> {
  return apiFetch<void>(`/users/${userId}`, {
    method: 'DELETE',
  });
}

// Delivery History API
export async function fetchDeliveryHistory(): Promise<DeliveryHistory[]> {
  return apiFetch<DeliveryHistory[]>('/delivery-history');
}

export async function createDeliveryHistory(
  historyData: Partial<DeliveryHistory>
): Promise<DeliveryHistory> {
  return apiFetch<DeliveryHistory>('/delivery-history', {
    method: 'POST',
    body: JSON.stringify(historyData),
  });
}

export async function deleteDeliveryHistory(historyId: string): Promise<void> {
  return apiFetch<void>(`/delivery-history/${historyId}`, {
    method: 'DELETE',
  });
}
