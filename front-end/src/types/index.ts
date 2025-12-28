export type OrderStatus = 'pending' | 'confirmed' | 'picked_up' | 'in_transit' | 'delivered' | 'cancelled' | 'rejected_by_receiver';

export interface Order {
  id: string;
  emetteurId: string;
  emetteurName: string;
  recepteurId: string;
  recepteurName: string;
  recepteurAddress: string;
  livreurId?: string;
  livreurName?: string;
  status: OrderStatus;
  description: string;
  weight: number;
  pickupAddress: string;
  deliveryAddress: string;
  latitude: number;
  longitude: number;
  createdAt: Date;
  updatedAt: Date;
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'client' | 'livreur';
  phone: string;
  address?: string;
  latitude?: number;
  longitude?: number;
}

export interface DeliveryHistory {
  id: string;
  orderId: string;
  livreurId: string;
  completedAt: Date;
}