import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Avatar, AvatarFallback } from './ui/avatar';
import { StatusBadge } from './StatusBadge';
import { MapPin, Package, LogOut, CheckCircle, Truck, User as UserIcon, Search } from 'lucide-react';
import type { Order, DeliveryHistory } from '../types';
import { updateOrderStatus, completeDelivery, fetchAssignments, fetchOrderById } from '../lib/api';

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
  latitude?: number;
  longitude?: number;
  affectationId?: string;
}


export function LivreurDashboard({ user, onLogout }: LivreurDashboardProps) {
  // Mock current location (in a real app, this would be from GPS)
  const [currentLocation] = useState({ lat: 48.8566, lon: 2.3522 });
  
  const [allOrders, setAllOrders] = useState<Order[]>([]);
  const [assignedDeliveries, setAssignedDeliveries] = useState<OrderWithDistance[]>([]);
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
        // Filter assigned deliveries - orders with status ASSIGNED or IN_TRANSIT
        // Note: We'll need to check if order is assigned to this deliverer via API
        try {
          const assignments = await fetchAssignments();
          // Map assignments to Order structure and include affectationId
          const myDeliveriesPromises = assignments.map(async (a) => {
            try {
              const order = await fetchOrderById(a.idPackage.toString());
              return {
                ...order,
                distance: 0, // Default distance for assigned orders
                affectationId: a.idAffectation.toString(), // Store the assignment ID for completion
              };
            } catch (err) {
              console.error(`Failed to fetch package ${a.idPackage}`, err);
              return null;
            }
          });
          
          const myDeliveries = (await Promise.all(myDeliveriesPromises)).filter((item): item is OrderWithDistance => item !== null);
          setAssignedDeliveries(myDeliveries);
          setAllOrders(myDeliveries);
        } catch (e) {
          console.error("Could not fetch assignments", e);
        }
        
        setAvailableOrders([]);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load data');
        setAllOrders([]);
        setHistory([]);
        setAssignedDeliveries([]);
        setAvailableOrders([]);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user.id, currentLocation.lat, currentLocation.lon]);

  const handlePickupOrder = async (orderId: string) => {
    const orderToPickup = availableOrders.find(o => o.idPackage.toString() === orderId);
    if (!orderToPickup) return;

    try {
      const updatedOrder = await updateOrderStatus(orderId, 'ASSIGNED');
      
      setAssignedDeliveries([{...updatedOrder, distance: 0}, ...assignedDeliveries]);
      setAvailableOrders(availableOrders.filter(o => o.idPackage.toString() !== orderId));
      setAllOrders(allOrders.map(o => o.idPackage.toString() === orderId ? updatedOrder : o));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to pickup order');
    }
  };

  const handleStartDelivery = async (orderId: string) => {
    try {
      const updatedOrder = await updateOrderStatus(orderId, 'IN_TRANSIT');
      setAssignedDeliveries(assignedDeliveries.map(order => 
        order.idPackage.toString() === orderId ? { ...order, ...updatedOrder } : order
      ));
      setAllOrders(allOrders.map(o => 
        o.idPackage.toString() === orderId ? updatedOrder : o
      ));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start delivery');
    }
  };

  const handleCompleteDelivery = async (orderId: string, affectationId?: string) => {
    if (!affectationId) {
      setError("Missing assignment ID for this order");
      return;
    }

    try {
      await completeDelivery(affectationId);
      
      // TODO: Create delivery history entry via API
      setAssignedDeliveries(assignedDeliveries.filter(o => o.idPackage.toString() !== orderId));
      setHistory([...history, {
        id: String(Date.now()),
        orderId,
        livreurId: user.id,
        completedAt: new Date(),
      }]);
      setAllOrders(allOrders.map(o => 
        o.idPackage.toString() === orderId ? { ...o, status: 'DELIVERED' } : o
      ));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to complete delivery');
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
              <UserIcon className="size-4 mr-2" />
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
                        key={order.idPackage}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div>
                            <p className="text-neutral-900">Order #{order.idPackage}</p>
                            <p className="text-sm text-neutral-500">{order.description || 'No description'}</p>
                          </div>
                          {order.distance > 0 && (
                            <div className="bg-green-100 text-green-900 px-3 py-1 rounded-full text-sm">
                              {order.distance.toFixed(1)} km away
                            </div>
                          )}
                        </div>
                        
                        <div className="space-y-3 mb-4">
                          <div className="bg-neutral-50 rounded-lg p-3">
                            <div className="flex items-start gap-2">
                              <div className="bg-green-100 rounded-full p-1.5 mt-0.5">
                                <MapPin className="size-3 text-green-700" />
                              </div>
                              <div className="flex-1">
                                <p className="text-sm text-neutral-500">Pickup from</p>
                                <p className="text-neutral-900">Client #{order.idClientSource}</p>
                                <p className="text-sm text-neutral-600">{order.addressSource}</p>
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
                                <p className="text-neutral-900">Client #{order.idClientDestination || 'N/A'}</p>
                                <p className="text-sm text-neutral-600">{order.addressDestination}</p>
                              </div>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <div>
                              <p className="text-neutral-500">Weight</p>
                              <p className="text-neutral-900">{order.weight} kg</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Price</p>
                              <p className="text-neutral-900">${order.price}</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Created</p>
                              <p className="text-neutral-900">{new Date(order.createdAt).toLocaleDateString()}</p>
                            </div>
                          </div>
                        </div>

                        <Button
                          onClick={() => handlePickupOrder(order.idPackage.toString())}
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
                        key={order.idPackage}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-4">
                          <div>
                            <p className="text-neutral-900">Order #{order.idPackage}</p>
                            <p className="text-sm text-neutral-500">{order.description || 'No description'}</p>
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
                                <p className="text-neutral-900">Client #{order.idClientSource}</p>
                                <p className="text-sm text-neutral-600">{order.addressSource}</p>
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
                                <p className="text-neutral-900">Client #{order.idClientDestination || 'N/A'}</p>
                                <p className="text-sm text-neutral-600">{order.addressDestination}</p>
                              </div>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <div>
                              <p className="text-neutral-500">Weight</p>
                              <p className="text-neutral-900">{order.weight} kg</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Price</p>
                              <p className="text-neutral-900">${order.price}</p>
                            </div>
                          </div>
                        </div>

                        <div className="flex gap-2">
                          {order.status === 'ASSIGNED' && (
                            <Button
                              onClick={() => handleStartDelivery(order.idPackage.toString())}
                              className="flex-1"
                            >
                              <Truck className="size-4 mr-2" />
                              Start Delivery
                            </Button>
                          )}
                          {order.status === 'IN_TRANSIT' && (
                            <Button
                              onClick={() => handleCompleteDelivery(order.idPackage.toString(), order.affectationId)}
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
                      const order = allOrders.find(o => o.idPackage.toString() === item.orderId);
                      return (
                        <div
                          key={item.id}
                          className="border border-neutral-200 rounded-lg p-4"
                        >
                          <div className="flex items-center justify-between mb-2">
                            <div>
                              <p className="text-neutral-900">Order #{item.orderId}</p>
                              {order && (
                                <p className="text-sm text-neutral-500">{order.description || 'No description'}</p>
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
                                <p className="text-neutral-900">Client #{order.idClientSource}</p>
                                <p className="text-xs text-neutral-600">{order.addressSource}</p>
                              </div>
                              <div>
                                <p className="text-neutral-500">To</p>
                                <p className="text-neutral-900">Client #{order.idClientDestination || 'N/A'}</p>
                                <p className="text-xs text-neutral-600">{order.addressDestination}</p>
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