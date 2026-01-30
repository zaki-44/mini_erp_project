import type { Order, User } from '../types'; // Keep your types if they match, or update them
import type { RegisterData } from '../components/RegisterPage';

// 1. Point to the correct backend URL
const API_BASE_URL = 'http://localhost:8080';

// 2. Generic fetch wrapper (rewritten for Cookies)
async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
    credentials: 'include', // <--- CRITICAL: Sends the HttpOnly cookie
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'An error occurred' }));
    throw new Error(error.message || `HTTP error! status: ${response.status}`);
  }

  // Handle empty responses (like from DELETE)
  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}

// --- AUTHENTICATION ---

export interface LoginResponse {
  // Adjust this based on exactly what your backend returns
  userId: number;
  role: 'ADMIN' | 'CLIENT' | 'DELIVERER';
  name?: string; // Optional if backend sends it
  // No token string here!
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  // Backend expects 'username', so we map email -> username
  return apiFetch<LoginResponse>('/login', {
    method: 'POST',
    body: JSON.stringify({ username: email, password }),
  });
}

export async function register(userData: any): Promise<any> {
  // Endpoint is /signup, not /auth/register
  return apiFetch('/signup', {
    method: 'POST',
    body: JSON.stringify(userData),
  });
}

export async function verifyCode(email: string, code: string) {
    return apiFetch('/verifycode', {
        method: 'POST',
        body: JSON.stringify({ email, code })
    });
}

export async function logout(): Promise<void> {
  // Must tell server to clear cookie
  return apiFetch<void>('/logout', { method: 'POST' }); 
}

// --- PACKAGES (Formerly Orders) ---

// Renamed fetchOrders to match backend concept, but you can keep name 'fetchOrders'
export async function fetchOrders(): Promise<Order[]> {
  return apiFetch<Order[]>('/api/packages');
}

export async function fetchOrderById(orderId: string): Promise<Order> {
  return apiFetch<Order>(`/api/packages/${orderId}`);
}

export async function createOrder(orderData: any): Promise<Order> {
  // Ensure orderData maps correctly to: 
  // { idClientSource, addressSource, addressDestination, weight, price }
  return apiFetch<Order>('/api/packages', {
    method: 'POST',
    body: JSON.stringify(orderData),
  });
}

export async function updateOrderStatus(
  orderId: string,
  status: string // e.g., "DELIVERED"
): Promise<Order> {
  // Backend uses PUT for updates
  return apiFetch<Order>(`/api/packages/${orderId}`, {
    method: 'PUT',
    body: JSON.stringify({ status }), 
  });
}

export async function deleteOrder(orderId: string): Promise<void> {
  return apiFetch<void>(`/api/packages/${orderId}`, {
    method: 'DELETE',
  });
}

// --- ADMIN / USERS ---

export async function fetchPendingDeliverers() {
    return apiFetch('/api/admin/pending-deliverers');
}

export async function approveDeliverer(delivererId: string) {
    return apiFetch(`/api/admin/approve?id=${delivererId}`, {
        method: 'POST'
    });
}