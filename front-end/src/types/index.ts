export type OrderStatus = 'CREATED' | 'ASSIGNED' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';

export interface Order {
  idPackage: number;
  idClientSource: number;
  idClientDestination?: number;
  vehicleTypeNeeded?: string;
  addressSource: string;
  addressDestination: string;
  weight: number;
  price: number;
  dimensions?: string;
  description?: string;
  deliveryInstructions?: string;
  status: OrderStatus;
  createdAt: string;
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

export interface Deliverer{
  id:number,
  email:string,
  username:string,
  firstName:string,
  lastName:string,
  phoneNumber:string,
  role:"DELIVERER",
  emailVerified:boolean,
  vehicleType:string,
  available:boolean,
  maxWeight:number,
  currentLoad:number,
  city:string,
  serialNumber:string,
  rate:number
}

// export interface Client{
//   id:number,
//   email:string,
//   username:string,
//   firstName:string,
//   lastName:string,
//   phoneNumber:string,
//   role:"DELIVERER",
//   emailVerified:boolean,
//   vehicleType:string,
//   available:boolean,
//   maxWeight:number,
//   currentLoad:number,
//   city:string,
//   serialNumber:string,
//   rate:number
// }