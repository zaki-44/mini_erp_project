import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { StatusBadge } from './StatusBadge';
import { Package, Users, CheckCircle, Search, LogOut, RefreshCw, Truck } from 'lucide-react';
import type { Order } from '../types';
import { 
  fetchOrders, 
  fetchOrdersById, 
  fetchOrderById, 
  fetchPendingDeliverers, 
  approveDeliverer 
} from '../lib/api';

// 1. UPDATE: Interface matches your strict lowercase requirements
interface PendingDeliverer {
  id: number;
  firstname: string;
  lastname: string;
  username: string;
  email: string;
  phonenumber: string;
  vehicletype: 'BIKE' | 'CAR' | 'TRUCK';
  serialnumber: string;
  maxweight: number;
  city: string;
}

interface AuthUser {
  id: number;
  name?: string;
  email?: string;
  role: 'CLIENT' | 'ADMIN' | 'DELIVERER';
}

interface AdminDashboardProps {
  user: AuthUser;
  onLogout: () => void;
}

export function AdminDashboard({ user, onLogout }: AdminDashboardProps) {
  const [orders, setOrders] = useState<Order[]>([]);
  // 2. UPDATE: State uses the new strict interface
  const [pendingDeliverers, setPendingDeliverers] = useState<PendingDeliverer[]>([]);
  
  // Search Filters
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
      // We cast the result of fetchPendingDeliverers because the API might return "any" 
      // but we want to enforce our PendingDeliverer shape
      const [allOrders, pending] = await Promise.all([
        fetchOrders(),
        fetchPendingDeliverers()
      ]);
      setOrders(allOrders);
      setPendingDeliverers(pending as unknown as PendingDeliverer[]);
    } catch (err: any) {
      console.error('Failed to load admin data:', err);
      setError('Failed to load data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearchByClientId = async () => {
    if (!clientIdFilter.trim()) return;
    setLoading(true);
    try {
      const clientOrders = await fetchOrdersById(parseInt(clientIdFilter));
      setOrders(clientOrders);
    } catch (err) {
      console.error(err);
      setError(`Could not find orders for Client ID ${clientIdFilter}`);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchByOrderId = async () => {
    if (!orderIdFilter.trim()) return;
    setLoading(true);
    try {
      const order = await fetchOrderById(orderIdFilter);
      setOrders(order ? [order] : []);
    } catch (err) {
      console.error(err);
      setError(`Could not find Order #${orderIdFilter}`);
    } finally {
      setLoading(false);
    }
  };

  const handleApproveDeliverer = async (delivererId: string) => {
    try {
      await approveDeliverer(delivererId);
      // Remove from list immediately to update UI
      setPendingDeliverers(prev => prev.filter(d => String(d.id) !== delivererId));
      alert("Deliverer approved successfully.");
    } catch (err) {
      console.error('Failed to approve:', err);
      alert('Failed to approve deliverer.');
    }
  };

  const handleReset = () => {
    setClientIdFilter('');
    setOrderIdFilter('');
    loadInitialData();
  };

  return (
    <>
      <header className="bg-white border-b sticky top-0 z-30 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-2">
          <div className="bg-primary/10 p-2 rounded-lg">
            <Package className="h-6 w-6 text-primary" />
          </div>
          <h1 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-primary/60">
            Admin Dashboard
          </h1>
        </div>
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={loadInitialData} title="Refresh Data">
            <RefreshCw className="h-5 w-5 text-muted-foreground" />
          </Button>
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium">{user.name || 'Administrator'}</p>
            <p className="text-xs text-muted-foreground">{user.email}</p>
          </div>
          <Button variant="ghost" size="icon" onClick={onLogout} className="text-muted-foreground hover:text-destructive">
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto p-6 space-y-6">
        {error && (
          <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md border border-destructive/20">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Card>
            <CardContent className="p-6 flex items-center gap-4">
              <div className="p-3 bg-blue-100 text-blue-600 rounded-full">
                <Package className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Total Packages</p>
                <p className="text-2xl font-bold">{orders.length}</p>
              </div>
            </CardContent>
          </Card>
          
          <Card>
            <CardContent className="p-6 flex items-center gap-4">
              <div className="p-3 bg-orange-100 text-orange-600 rounded-full">
                <Users className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Pending Approvals</p>
                <p className="text-2xl font-bold">{pendingDeliverers.length}</p>
              </div>
            </CardContent>
          </Card>

           <Card>
            <CardContent className="p-6 flex items-center gap-4">
              <div className="p-3 bg-green-100 text-green-600 rounded-full">
                <CheckCircle className="h-6 w-6" />
              </div>
              <div>
                <p className="text-sm text-muted-foreground">System Status</p>
                <p className="text-2xl font-bold text-green-600">Active</p>
              </div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="packages" className="space-y-6">
          <TabsList>
            <TabsTrigger value="packages">Package Management</TabsTrigger>
            <TabsTrigger value="approvals">
              Driver Approvals 
              {pendingDeliverers.length > 0 && (
                <span className="ml-2 bg-red-500 text-white text-[10px] px-1.5 py-0.5 rounded-full">
                  {pendingDeliverers.length}
                </span>
              )}
            </TabsTrigger>
          </TabsList>

          {/* --- TAB: PACKAGES --- */}
          <TabsContent value="packages">
            <Card>
              <CardHeader>
                <CardTitle className="flex justify-between items-center">
                  <span>All Packages</span>
                  <div className="flex gap-2">
                    <div className="flex gap-2">
                      <Input 
                        placeholder="Client ID..." 
                        className="w-32 h-9 text-sm"
                        value={clientIdFilter}
                        onChange={(e) => setClientIdFilter(e.target.value)}
                      />
                      <Button variant="outline" size="sm" onClick={handleSearchByClientId}>
                        <Search className="h-4 w-4" />
                      </Button>
                    </div>
                    <div className="flex gap-2">
                      <Input 
                        placeholder="Order ID..." 
                        className="w-32 h-9 text-sm"
                        value={orderIdFilter}
                        onChange={(e) => setOrderIdFilter(e.target.value)}
                      />
                      <Button variant="outline" size="sm" onClick={handleSearchByOrderId}>
                        <Search className="h-4 w-4" />
                      </Button>
                    </div>
                    <Button variant="ghost" size="sm" onClick={handleReset}>Reset</Button>
                  </div>
                </CardTitle>
              </CardHeader>
              <CardContent>
                {loading && orders.length === 0 ? (
                  <div className="text-center py-8">Loading packages...</div>
                ) : orders.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">No packages found.</div>
                ) : (
                  <div className="space-y-4">
                    {orders.map((order) => (
                      <div key={order.idPackage} className="flex items-center justify-between p-4 border rounded-lg hover:bg-neutral-50">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="font-mono text-sm bg-neutral-100 px-2 py-0.5 rounded">#{order.idPackage}</span>
                            <StatusBadge status={order.status} />
                          </div>
                          <p className="text-sm font-medium">{order.description}</p>
                          <div className="text-xs text-muted-foreground flex gap-4">
                            <span>From: Client #{order.idClientSource}</span>
                            <span>To: Client #{order.idClientDestination || '?'}</span>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="font-bold">{order.price} DA</p>
                          <p className="text-xs text-muted-foreground">{new Date(order.createdAt).toLocaleDateString()}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* --- TAB: APPROVALS --- */}
          <TabsContent value="approvals">
            <Card>
              <CardHeader>
                <CardTitle>Pending Deliverer Approvals</CardTitle>
              </CardHeader>
              <CardContent>
                {pendingDeliverers.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground">
                    <CheckCircle className="h-12 w-12 mx-auto mb-4 opacity-20" />
                    <p>No pending approvals.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {pendingDeliverers.map(deliverer => (
                      <div
                        key={deliverer.id}
                        className="flex items-center justify-between border border-neutral-200 rounded-lg p-4 bg-white shadow-sm"
                      >
                        <div className="flex items-start gap-4">
                           <div className="bg-orange-100 p-2 rounded-full mt-1">
                             <Truck className="h-5 w-5 text-orange-600" />
                           </div>
                           
                           <div>
                            {/* 3. UPDATE: Access fields using strict lowercase names */}
                            <p className="font-medium text-neutral-900 text-lg">
                              {deliverer.firstname} {deliverer.lastname}
                            </p>
                            <p className="text-sm text-neutral-500">{deliverer.email}</p>
                            
                            <div className="flex flex-wrap gap-2 mt-2 text-xs font-mono">
                              {/* Vehicle Type */}
                              <div className="bg-neutral-100 px-2 py-1 rounded font-bold uppercase text-neutral-700">
                                {deliverer.vehicletype}
                              </div>

                              {/* Max Weight */}
                              <div className="bg-neutral-100 px-2 py-1 rounded text-neutral-600">
                                Max: {deliverer.maxweight}kg
                              </div>

                              {/* Serial Number */}
                              <div className="bg-neutral-100 px-2 py-1 rounded text-neutral-600">
                                SN: {deliverer.serialnumber}
                              </div>

                              {/* City */}
                              <div className="bg-neutral-100 px-2 py-1 rounded text-neutral-600">
                                {deliverer.city}
                              </div>
                            </div>
                          </div>
                        </div>

                        <Button
                          size="sm"
                          onClick={() => handleApproveDeliverer(String(deliverer.id))}
                          className="bg-green-600 hover:bg-green-700 h-10 px-6"
                        >
                          <CheckCircle className="size-4 mr-2" />
                          Approve
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </main>
    </>
  );
}