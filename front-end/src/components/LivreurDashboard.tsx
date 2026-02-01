import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Avatar, AvatarFallback } from './ui/avatar';
import { StatusBadge } from './StatusBadge';
import { MapPin, Package, LogOut, CheckCircle, Truck, User as UserIcon, Search, Loader2 } from 'lucide-react';
import type { Order, DeliveryHistory } from '../types';
import { fetchOrders, updateOrderStatus } from '../lib/api';
import { mockOrders, mockDeliveryHistory } from '../lib/mockData';

interface AuthUser {
  id: string;
  name: string;
  email: string;
  type: string;
}

interface LivreurDashboardProps {
  user: AuthUser;
  onLogout: () => void;
}

// Extended Order type with distance property
interface OrderWithDistance extends Order {
  distance: number;
}

// Helper function to calculate distance between two coordinates (in km)
function calculateDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371; // Radius of the Earth in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

export function LivreurDashboard({ user, onLogout }: LivreurDashboardProps) {
  // Mock current location (in a real app, this would be from GPS)
  const [currentLocation] = useState({ lat: 48.8566, lon: 2.3522 });
  
  const [allOrders, setAllOrders] = useState<Order[]>([]);
  const [assignedDeliveries, setAssignedDeliveries] = useState<Order[]>([]);
  const [availableOrders, setAvailableOrders] = useState<OrderWithDistance[]>([]);
  const [history, setHistory] = useState<DeliveryHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch data on component mount
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [ordersData, historyData] = await Promise.all([
          fetchOrders(),
          fetchDeliveryHistory(),
        ]);
        setAllOrders(ordersData);
        setHistory(historyData.filter(h => h.livreurId === user.id));
        
        // Filter assigned deliveries
        const assigned = ordersData.filter(
          o => o.livreurId === user.id && !['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status)
        );
        setAssignedDeliveries(assigned);
        
        // Calculate available orders with distance
        const available = ordersData
          .filter(o => o.status === 'confirmed' && !o.livreurId)
          .map(o => ({
            ...o,
            distance: calculateDistance(currentLocation.lat, currentLocation.lon, o.latitude, o.longitude)
          }))
          .sort((a, b) => a.distance - b.distance);
        setAvailableOrders(available);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load data');
        // Fallback to mock data
        setAllOrders(mockOrders);
        setHistory(mockDeliveryHistory.filter(h => h.livreurId === user.id));
        const assigned = mockOrders.filter(
          o => o.livreurId === user.id && !['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status)
        );
        setAssignedDeliveries(assigned);
        const available = mockOrders
          .filter(o => o.status === 'confirmed' && !o.livreurId)
          .map(o => ({
            ...o,
            distance: calculateDistance(currentLocation.lat, currentLocation.lon, o.latitude, o.longitude)
          }))
          .sort((a, b) => a.distance - b.distance);
        setAvailableOrders(available);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user.id, currentLocation.lat, currentLocation.lon]);

  const handlePickupOrder = async (orderId: string) => {
    const orderToPickup = availableOrders.find(o => o.id === orderId);
    if (!orderToPickup) return;

    try {
      const updatedOrder = await updateOrderStatus(orderId, 'picked_up');
      // Update order with livreur info
      const orderWithLivreur = {
        ...updatedOrder,
        livreurId: user.id,
        livreurName: user.name,
      };
      
      setAssignedDeliveries([orderWithLivreur, ...assignedDeliveries]);
      setAvailableOrders(availableOrders.filter(o => o.id !== orderId));
      setAllOrders(allOrders.map(o => o.id === orderId ? orderWithLivreur : o));
    } catch (err) {
      // Fallback: update locally
      const updatedOrder = {
        ...orderToPickup,
        livreurId: user.id,
        livreurName: user.name,
        status: 'picked_up' as const,
        updatedAt: new Date()
      };
      setAssignedDeliveries([updatedOrder, ...assignedDeliveries]);
      setAvailableOrders(availableOrders.filter(o => o.id !== orderId));
    }
  };

  const handleStartDelivery = async (orderId: string) => {
    try {
      await updateOrderStatus(orderId, 'in_transit');
      const updated = assignedDeliveries.map(order => 
        order.id === orderId && order.status === 'picked_up'
          ? { ...order, status: 'in_transit' as const, updatedAt: new Date() }
          : order
      );
      setAssignedDeliveries(updated);
      setAllOrders(allOrders.map(o => 
        o.id === orderId ? { ...o, status: 'in_transit' as const, updatedAt: new Date() } : o
      ));
    } catch (err) {
      // Fallback: update locally
      setAssignedDeliveries(assignedDeliveries.map(order => 
        order.id === orderId && order.status === 'picked_up'
          ? { ...order, status: 'in_transit' as const, updatedAt: new Date() }
          : order
      ));
    }
  };

  const handleCompleteDelivery = async (orderId: string) => {
    try {
      await updateOrderStatus(orderId, 'delivered');
      
      // Create delivery history entry
      await createDeliveryHistory({
        orderId,
        livreurId: user.id,
        completedAt: new Date(),
      });
      
      setAssignedDeliveries(assignedDeliveries.filter(o => o.id !== orderId));
      setHistory([...history, {
        id: String(Date.now()),
        orderId,
        livreurId: user.id,
        completedAt: new Date(),
      }]);
      setAllOrders(allOrders.map(o => 
        o.id === orderId ? { ...o, status: 'delivered' as const, updatedAt: new Date() } : o
      ));
    } catch (err) {
      // Fallback: update locally
      setAssignedDeliveries(assignedDeliveries.filter(o => o.id !== orderId));
      setHistory([...history, {
        id: String(Date.now()),
        orderId,
        livreurId: user.id,
        completedAt: new Date(),
      }]);
    }
  };

  return (
    <>
      {/* Header */}
      <header className="bg-white border-b border-neutral-200 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-neutral-900">Delivery ERP - Delivery Person</h1>
              <p className="text-neutral-500 text-sm">Welcome, {user.name}</p>
            </div>
            <Button variant="outline" onClick={onLogout}>
              <LogOut className="size-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        {error && (
          <div className="mb-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-900">
            <p className="text-sm">⚠️ {error} (Using fallback data)</p>
          </div>
        )}
        <Tabs defaultValue="search" className="space-y-6">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="search">
              <Search className="size-4 mr-2" />
              Find Nearby
            </TabsTrigger>
            <TabsTrigger value="deliveries">
              <Truck className="size-4 mr-2" />
              My Deliveries
            </TabsTrigger>
            <TabsTrigger value="profile">
              <User className="size-4 mr-2" />
              Profile
            </TabsTrigger>
          </TabsList>

          {/* SEARCH NEARBY TAB */}
          <TabsContent value="search" className="space-y-6">
            <div>
              <h2 className="text-neutral-900 mb-2">Nearby Available Deliveries</h2>
              <p className="text-neutral-500">Find and pickup packages near your location</p>
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Available Orders</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {availableOrders.length === 0 ? (
                    <div className="text-center py-12 text-neutral-500">
                      <Package className="size-16 mx-auto mb-4 opacity-30" />
                      <p className="text-lg">No available deliveries nearby</p>
                      <p className="text-sm">Check back later for new delivery opportunities</p>
                    </div>
                  ) : (
                    availableOrders.map(order => (
                      <div
                        key={order.id}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div>
                            <p className="text-neutral-900">Order #{order.id}</p>
                            <p className="text-sm text-neutral-500">{order.description}</p>
                          </div>
                          <div className="bg-green-100 text-green-900 px-3 py-1 rounded-full text-sm">
                            {order.distance.toFixed(1)} km away
                          </div>
                        </div>
                        
                        <div className="space-y-3 mb-4">
                          <div className="bg-neutral-50 rounded-lg p-3">
                            <div className="flex items-start gap-2">
                              <div className="bg-green-100 rounded-full p-1.5 mt-0.5">
                                <MapPin className="size-3 text-green-700" />
                              </div>
                              <div className="flex-1">
                                <p className="text-sm text-neutral-500">Pickup from</p>
                                <p className="text-neutral-900">{order.emetteurName}</p>
                                <p className="text-sm text-neutral-600">{order.pickupAddress}</p>
                              </div>
                            </div>
                          </div>

                          <div className="bg-neutral-50 rounded-lg p-3">
                            <div className="flex items-start gap-2">
                              <div className="bg-blue-100 rounded-full p-1.5 mt-0.5">
                                <MapPin className="size-3 text-blue-700" />
                              </div>
                              <div className="flex-1">
                                <p className="text-sm text-neutral-500">Deliver to</p>
                                <p className="text-neutral-900">{order.recepteurName}</p>
                                <p className="text-sm text-neutral-600">{order.deliveryAddress}</p>
                              </div>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <div>
                              <p className="text-neutral-500">Weight</p>
                              <p className="text-neutral-900">{order.weight} kg</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Created</p>
                              <p className="text-neutral-900">{order.createdAt.toLocaleDateString()}</p>
                            </div>
                          </div>
                        </div>

                        <Button
                          onClick={() => handlePickupOrder(order.id)}
                          className="w-full"
                        >
                          <CheckCircle className="size-4 mr-2" />
                          Accept & Pickup
                        </Button>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* DELIVERIES TAB */}
          <TabsContent value="deliveries" className="space-y-6">
            <div>
              <h2 className="text-neutral-900 mb-2">My Active Deliveries</h2>
              <p className="text-neutral-500">Manage your current delivery tasks</p>
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Active Deliveries</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {assignedDeliveries.length === 0 ? (
                    <div className="text-center py-12 text-neutral-500">
                      <Package className="size-16 mx-auto mb-4 opacity-30" />
                      <p className="text-lg">No active deliveries</p>
                      <p className="text-sm">Search for nearby deliveries to get started</p>
                    </div>
                  ) : (
                    assignedDeliveries.map(order => (
                      <div
                        key={order.id}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div>
                            <p className="text-neutral-900">Order #{order.id}</p>
                            <p className="text-sm text-neutral-500">{order.description}</p>
                          </div>
                          <StatusBadge status={order.status} />
                        </div>
                        
                        <div className="space-y-3 mb-4">
                          <div className="bg-neutral-50 rounded-lg p-3">
                            <div className="flex items-start gap-2">
                              <div className="bg-green-100 rounded-full p-1.5 mt-0.5">
                                <MapPin className="size-3 text-green-700" />
                              </div>
                              <div className="flex-1">
                                <p className="text-sm text-neutral-500">Pickup from</p>
                                <p className="text-neutral-900">{order.emetteurName}</p>
                                <p className="text-sm text-neutral-600">{order.pickupAddress}</p>
                              </div>
                            </div>
                          </div>

                          <div className="bg-neutral-50 rounded-lg p-3">
                            <div className="flex items-start gap-2">
                              <div className="bg-blue-100 rounded-full p-1.5 mt-0.5">
                                <MapPin className="size-3 text-blue-700" />
                              </div>
                              <div className="flex-1">
                                <p className="text-sm text-neutral-500">Deliver to</p>
                                <p className="text-neutral-900">{order.recepteurName}</p>
                                <p className="text-sm text-neutral-600">{order.deliveryAddress}</p>
                              </div>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <div>
                              <p className="text-neutral-500">Weight</p>
                              <p className="text-neutral-900">{order.weight} kg</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Status</p>
                              <p className="text-neutral-900 capitalize">{order.status.replace('_', ' ')}</p>
                            </div>
                          </div>
                        </div>

                        <div className="flex gap-2">
                          {order.status === 'picked_up' && (
                            <Button
                              onClick={() => handleStartDelivery(order.id)}
                              className="flex-1"
                            >
                              <Truck className="size-4 mr-2" />
                              Start Delivery
                            </Button>
                          )}
                          {order.status === 'in_transit' && (
                            <Button
                              onClick={() => handleCompleteDelivery(order.id)}
                              className="flex-1"
                            >
                              <CheckCircle className="size-4 mr-2" />
                              Complete Delivery
                            </Button>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* PROFILE TAB */}
          <TabsContent value="profile" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Profile</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-col items-center space-y-4 pb-6">
                  <Avatar className="size-24">
                    <AvatarFallback className="text-2xl">
                      {user.name.split(' ').map(n => n[0]).join('')}
                    </AvatarFallback>
                  </Avatar>
                  <div className="text-center">
                    <p className="text-xl text-neutral-900">{user.name}</p>
                    <p className="text-neutral-500">{user.email}</p>
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-6 border-t border-neutral-200">
                  <div className="text-center">
                    <div className="flex items-center justify-center gap-2 text-neutral-500 mb-2">
                      <Package className="size-4" />
                      <span className="text-sm">Completed Deliveries</span>
                    </div>
                    <p className="text-3xl text-neutral-900">{history.length}</p>
                  </div>
                  <div className="text-center">
                    <div className="flex items-center justify-center gap-2 text-neutral-500 mb-2">
                      <Truck className="size-4" />
                      <span className="text-sm">Active Deliveries</span>
                    </div>
                    <p className="text-3xl text-neutral-900">{assignedDeliveries.length}</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Delivery History</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {history.length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No delivery history yet</p>
                    </div>
                  ) : (
                    history.map(item => {
                      const order = allOrders.find(o => o.id === item.orderId);
                      return (
                        <div
                          key={item.id}
                          className="border border-neutral-200 rounded-lg p-4"
                        >
                          <div className="flex items-center justify-between mb-2">
                            <div>
                              <p className="text-neutral-900">Order #{item.orderId}</p>
                              {order && (
                                <p className="text-sm text-neutral-500">{order.description}</p>
                              )}
                            </div>
                            <div className="bg-green-100 text-green-900 px-3 py-1 rounded-full text-sm">
                              Delivered
                            </div>
                          </div>
                          {order && (
                            <div className="grid grid-cols-2 gap-3 text-sm pt-2 border-t border-neutral-200">
                              <div>
                                <p className="text-neutral-500">From</p>
                                <p className="text-neutral-900">{order.emetteurName}</p>
                              </div>
                              <div>
                                <p className="text-neutral-500">To</p>
                                <p className="text-neutral-900">{order.recepteurName}</p>
                              </div>
                            </div>
                          )}
                          <div className="mt-2 text-sm text-neutral-500">
                            Completed on {item.completedAt.toLocaleDateString()} at {item.completedAt.toLocaleTimeString()}
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>
    </>
  );
}