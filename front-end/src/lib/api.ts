import type { Order, User, Deliverer } from '../types'; // Keep your types if they match, or update them
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
    body: JSON.stringify({ email, password }),
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

export async function resendCode(email: string) {
    return apiFetch('/resend-code', {
        method: 'POST',
        body: JSON.stringify({ email })
    });
}

export interface LogoutResponse {
  status: 'success' | 'error';
  message: string;
}

export async function logout(): Promise<LogoutResponse> {
  // Must tell server to clear cookie
  return apiFetch<LogoutResponse>('/logout', { method: 'POST' }); 
}

// --- PACKAGES (Formerly Orders) ---

// Renamed fetchOrders to match backend concept, but you can keep name 'fetchOrders'
export async function fetchOrders(): Promise<Order[]> {
  return apiFetch<Order[]>('/api/packages');
}

export async function fetchOrdersById(id: number): Promise<Order[]> {
  return apiFetch<Order[]>(`/api/packages?idClient=${id}`);
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

export async function fetchPendingDeliverers(): Promise<Deliverer[]> {
    return apiFetch<Deliverer[]>('/api/admin/pending-deliverers');
}

export async function approveDeliverer(delivererId: string) {
    return apiFetch(`/api/admin/approve?id=${delivererId}`, {
        method: 'POST'
    });
}

export interface Assignment {
  idAffectation: number;
  idDeliverer: number;
  idPackage: number;
  status: string;
  assignedAt: string;
}

export async function fetchAssignments(): Promise<Assignment[]> {
  return apiFetch<Assignment[]>('/api/assignments');
}

// ... existing imports and code ...

export interface RequestDriverResponse {
  status: string;
  message: string;
  delivererId: number;
  delivererName: string;
}

export async function requestDriver(packageId: string): Promise<RequestDriverResponse> {
  return apiFetch<RequestDriverResponse>(`/api/assignments/request/${packageId}`, {
    method: 'POST'
  });
}

export async function submitRating(data: { idDeliverer: number; rating: number; comment: string }) {
  return apiFetch('/api/rates', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export async function completeDelivery(affectationId: string) {
  // Note: The API expects the Assignment (Affectation) ID here.
  // If the frontend is passing the Package ID, ensure they match or are mapped correctly.
  return apiFetch(`/api/assignments/complete/${affectationId}`, {
    method: 'POST'
  });
}

// --- NOTIFICATIONS ---

export interface Notification {
  id: number;
  idPackage: number;
  message: string;
  type: string;
  isRead: boolean;
  dateNotif: string;
}

export async function fetchNotifications(): Promise<Notification[]> {
  return apiFetch<Notification[]>('/api/notifications');
}

export async function markNotificationAsRead(notificationId: number): Promise<void> {
  // Uses query parameter: POST /api/notifications?notificationId=X
  return apiFetch<void>(`/api/notifications?notificationId=${notificationId}`, {
    method: 'POST'
  });
}

// --- SEARCH ---

export async function searchClients(query: string): Promise<any[]> {
  // Returns: [{ id, firstName, lastName, email }]
  return apiFetch<any[]>(`/api/search?q=${encodeURIComponent(query)}`);
}
