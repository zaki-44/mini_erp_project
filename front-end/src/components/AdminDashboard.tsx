import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { StatusBadge } from './StatusBadge';
import { Package, Users, CheckCircle, Search, LogOut, RefreshCw } from 'lucide-react';
import type { Order, Deliverer } from '../types';
import { fetchOrders, fetchOrdersById, fetchOrderById, fetchPendingDeliverers, approveDeliverer } from '../lib/api';

interface AuthUser {
  id: string;
  name: string;
  email: string;
  type: string;
}

interface AdminDashboardProps {
  user: AuthUser;
  onLogout: () => void;
}

export function AdminDashboard({ user, onLogout }: AdminDashboardProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  const [pendingDeliverers, setPendingDeliverers] = useState<Deliverer[]>([]);
  const [clientIdFilter, setClientIdFilter] = useState('');
  const [orderIdFilter, setOrderIdFilter] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadInitialData();
    
  }, []);

  const loadInitialData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [allOrders, deliverers] = await Promise.all([
        fetchOrders(),
        fetchPendingDeliverers()
        
      ]);
      // console.log(deliverers)
      setOrders(allOrders);
      setPendingDeliverers(deliverers);
    } catch (err) {
      console.error(err);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchByClient = async () => {
    setLoading(true);
    setError(null);
    try {
      if (clientIdFilter.trim()) {
        const id = parseInt(clientIdFilter);
        if (isNaN(id)) {
          setError('Client ID must be a number');
          setLoading(false);
          return;
        }
        const clientOrders = await fetchOrdersById(id);
        setOrders(clientOrders);
      } else {
        const allOrders = await fetchOrders();
        setOrders(allOrders);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch orders');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchByOrder = async () => {
    setLoading(true);
    setError(null);
    try {
      if (orderIdFilter.trim()) {
        const order = await fetchOrderById(orderIdFilter);
        setOrders([order]);
      } else {
        const allOrders = await fetchOrders();
        setOrders(allOrders);
      }
    } catch (err) {
      console.error(err);
      setError('Failed to fetch order');
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveDeliverer = async (id: string) => {
    try {
      await approveDeliverer(id);
      setPendingDeliverers(pendingDeliverers.filter(d => String(d.id) !== id));
    } catch (err) {
      console.error(err);
      setError('Failed to approve deliverer');
    }
  };

  return (
    <>
      <header className="bg-white border-b border-neutral-200 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-neutral-900">Delivery ERP - Admin</h1>
              <p className="text-neutral-500 text-sm">Welcome, {user.name}</p>
            </div>
            <Button variant="outline" onClick={onLogout}>
              <LogOut className="size-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        {error && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg text-red-900">
            <p className="text-sm">⚠️ {error}</p>
          </div>
        )}

        <Tabs defaultValue="packages" className="space-y-6">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="packages">
              <Package className="size-4 mr-2" />
              Manage Packages
            </TabsTrigger>
            <TabsTrigger value="deliverers">
              <Users className="size-4 mr-2" />
              Pending Deliverers
              {pendingDeliverers.length > 0 && (
                <span className="ml-2 bg-red-500 text-white text-xs rounded-full px-2 py-0.5">
                  {pendingDeliverers.length}
                </span>
              )}
            </TabsTrigger>
          </TabsList>

          <TabsContent value="packages" className="space-y-6">
            <Card>
              <CardHeader className="flex flex-col space-y-4 pb-2">
                <div className="flex items-center justify-between">
                  <CardTitle>All Packages</CardTitle>
                  <Button size="sm" variant="ghost" onClick={() => { setClientIdFilter(''); setOrderIdFilter(''); loadInitialData(); }}>
                    <RefreshCw className="size-4" />
                  </Button>
                </div>
                <div className="flex flex-wrap gap-4">
                  <div className="flex items-center space-x-2">
                    <Input
                      placeholder="Filter by Client ID"
                      className="h-9 w-[200px]"
                      value={clientIdFilter}
                      onChange={(e) => setClientIdFilter(e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleSearchByClient()}
                    />
                    <Button size="sm" variant="secondary" onClick={handleSearchByClient}>
                      <Search className="size-4 mr-2" />
                      Client
                    </Button>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Input
                      placeholder="Search by Order ID"
                      className="h-9 w-[200px]"
                      value={orderIdFilter}
                      onChange={(e) => setOrderIdFilter(e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleSearchByOrder()}
                    />
                    <Button size="sm" variant="secondary" onClick={handleSearchByOrder}>
                      <Search className="size-4 mr-2" />
                      Order
                    </Button>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {orders.length === 0 ? (
                    <div className="text-center py-12 text-neutral-500">
                      <Package className="size-12 mx-auto mb-3 opacity-30" />
                      <p>No packages found</p>
                    </div>
                  ) : (
                    orders.map(order => (
                      <div
                        key={order.idPackage}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-2">
                          <div>
                            <p className="font-medium text-neutral-900">Order #{order.idPackage}</p>
                            <p className="text-sm text-neutral-500">{order.description}</p>
                          </div>
                          <StatusBadge status={order.status} />
                        </div>
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm text-neutral-600">
                          <div>
                            <span className="block text-xs text-neutral-400">From Client</span>
                            #{order.idClientSource}
                          </div>
                          <div>
                            <span className="block text-xs text-neutral-400">To Client</span>
                            #{order.idClientDestination || 'N/A'}
                          </div>
                          <div>
                            <span className="block text-xs text-neutral-400">Weight</span>
                            {order.weight} kg
                          </div>
                          <div>
                            <span className="block text-xs text-neutral-400">Price</span>
                            ${order.price}
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="deliverers" className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Pending Approvals</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {pendingDeliverers.length === 0 ? (
                    <div className="text-center py-12 text-neutral-500">
                      <Users className="size-12 mx-auto mb-3 opacity-30" />
                      <p>No pending deliverers</p>
                    </div>
                  ) : (
                    pendingDeliverers.map(deliverer => (
                      <div
                        key={deliverer.id}
                        className="flex items-center justify-between border border-neutral-200 rounded-lg p-4"
                      >
                        <div>
                          <p className="font-medium text-neutral-900">
                            {deliverer.firstName} {deliverer.lastName}
                          </p>
                          <p className="text-sm text-neutral-500">{deliverer.email}</p>
                          <div className="flex gap-2 mt-1 text-xs text-neutral-400">
                            <span>{deliverer.city}</span>
                            <span>•</span>
                            <span>{deliverer.vehicleType}</span>
                          </div>
                        </div>
                        <Button
                          size="sm"
                          onClick={() => handleApproveDeliverer(String(deliverer.id))}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <CheckCircle className="size-4 mr-2" />
                          Approve
                        </Button>
                      </div>
                    ))
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