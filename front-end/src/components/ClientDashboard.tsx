import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Textarea } from './ui/textarea';
import { Label } from './ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { mockOrders, mockUsers } from '../lib/mockData';
import { StatusBadge } from './StatusBadge';
import { Plus, MapPin, Package, LogOut, Send, Inbox, CheckCircle, X, Search } from 'lucide-react';
import type { Order, User } from '../types';

interface AuthUser {
  id: string;
  name: string;
  email: string;
  type: string;
}

interface ClientDashboardProps {
  user: AuthUser;
  onLogout: () => void;
}

export function ClientDashboard({ user, onLogout }: ClientDashboardProps) {
  // Orders sent by this client
  const [sentOrders, setSentOrders] = useState<Order[]>(
    mockOrders.filter(o => o.emetteurId === user.id)
  );
  
  // Orders received by this client
  const [receivedOrders, setReceivedOrders] = useState<Order[]>(
    mockOrders.filter(o => o.recepteurId === user.id)
  );

  const [showNewOrderForm, setShowNewOrderForm] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [selectedReceiver, setSelectedReceiver] = useState<User | null>(null);
  const [newOrder, setNewOrder] = useState({
    description: '',
    weight: '',
    pickupAddress: mockUsers.find(u => u.id === user.id)?.address || '',
  });

  const handleSearchUsers = (query: string) => {
    setSearchQuery(query);
    if (query.trim().length < 2) {
      setSearchResults([]);
      return;
    }
    
    const results = mockUsers.filter(u => 
      u.role === 'client' && 
      u.id !== user.id &&
      (u.name.toLowerCase().includes(query.toLowerCase()) ||
       u.email.toLowerCase().includes(query.toLowerCase()))
    );
    setSearchResults(results);
  };

  const handleSelectReceiver = (receiver: User) => {
    setSelectedReceiver(receiver);
    setSearchQuery(receiver.name);
    setSearchResults([]);
  };

  const handleCreateOrder = () => {
    if (!selectedReceiver) return;

    const order: Order = {
      id: String(Date.now()),
      emetteurId: user.id,
      emetteurName: user.name,
      recepteurId: selectedReceiver.id,
      recepteurName: selectedReceiver.name,
      recepteurAddress: selectedReceiver.address || '',
      status: 'confirmed', // Orders are auto-confirmed, no admin approval needed
      description: newOrder.description,
      weight: parseFloat(newOrder.weight),
      pickupAddress: newOrder.pickupAddress,
      deliveryAddress: selectedReceiver.address || '',
      latitude: selectedReceiver.latitude || 48.8566,
      longitude: selectedReceiver.longitude || 2.3522,
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    setSentOrders([order, ...sentOrders]);
    setShowNewOrderForm(false);
    setSearchQuery('');
    setSelectedReceiver(null);
    setNewOrder({
      description: '',
      weight: '',
      pickupAddress: mockUsers.find(u => u.id === user.id)?.address || '',
    });
  };

  const handleCancelOrder = (orderId: string) => {
    // Can only cancel before it's picked up by a delivery person
    setSentOrders(sentOrders.map(order => 
      order.id === orderId && ['confirmed'].includes(order.status)
        ? { ...order, status: 'cancelled' as const, updatedAt: new Date() }
        : order
    ));
  };

  const handleAcceptDelivery = (orderId: string) => {
    setReceivedOrders(receivedOrders.map(order => 
      order.id === orderId && order.status === 'in_transit'
        ? { ...order, status: 'delivered' as const, updatedAt: new Date() }
        : order
    ));
  };

  const handleRejectDelivery = (orderId: string) => {
    setReceivedOrders(receivedOrders.map(order => 
      order.id === orderId && order.status === 'in_transit'
        ? { ...order, status: 'rejected_by_receiver' as const, updatedAt: new Date() }
        : order
    ));
  };

  return (
    <>
      {/* Header */}
      <header className="bg-white border-b border-neutral-200 sticky top-0 z-50">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-neutral-900">Delivery ERP - Client</h1>
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
        <Tabs defaultValue="send" className="space-y-6">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="send">
              <Send className="size-4 mr-2" />
              Send Packages
            </TabsTrigger>
            <TabsTrigger value="receive">
              <Inbox className="size-4 mr-2" />
              Receive Packages
            </TabsTrigger>
          </TabsList>

          {/* SEND TAB */}
          <TabsContent value="send" className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-neutral-900 mb-2">Send Packages</h2>
                <p className="text-neutral-500">Create and track your shipments</p>
              </div>
              <Button onClick={() => setShowNewOrderForm(!showNewOrderForm)}>
                <Plus className="size-4 mr-2" />
                New Shipment
              </Button>
            </div>

            {/* New Order Form */}
            {showNewOrderForm && (
              <Card>
                <CardHeader>
                  <CardTitle>Create New Shipment</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="recepteurSearch">Search Receiver by Name or Email</Label>
                    <div className="relative">
                      <Search className="absolute left-3 top-3 size-4 text-neutral-400" />
                      <Input
                        id="recepteurSearch"
                        placeholder="Type to search for a user..."
                        value={searchQuery}
                        onChange={(e) => handleSearchUsers(e.target.value)}
                        className="pl-9"
                      />
                    </div>
                    {searchResults.length > 0 && (
                      <div className="border border-neutral-200 rounded-lg mt-2 max-h-48 overflow-y-auto">
                        {searchResults.map(receiver => (
                          <button
                            key={receiver.id}
                            onClick={() => handleSelectReceiver(receiver)}
                            className="w-full text-left px-4 py-3 hover:bg-neutral-50 border-b border-neutral-100 last:border-b-0"
                          >
                            <p className="text-neutral-900">{receiver.name}</p>
                            <p className="text-sm text-neutral-500">{receiver.email}</p>
                            <p className="text-xs text-neutral-400">{receiver.address}</p>
                          </button>
                        ))}
                      </div>
                    )}
                    {selectedReceiver && (
                      <div className="bg-green-50 border border-green-200 rounded-lg p-3">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-sm text-green-900">Selected Receiver:</p>
                            <p className="text-green-900">{selectedReceiver.name}</p>
                            <p className="text-sm text-green-700">{selectedReceiver.address}</p>
                          </div>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => {
                              setSelectedReceiver(null);
                              setSearchQuery('');
                            }}
                          >
                            <X className="size-4" />
                          </Button>
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="description">Package Description</Label>
                    <Textarea
                      id="description"
                      placeholder="Describe the package contents"
                      value={newOrder.description}
                      onChange={(e) => setNewOrder({ ...newOrder, description: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="weight">Weight (kg)</Label>
                    <Input
                      id="weight"
                      type="number"
                      step="0.1"
                      placeholder="Enter weight"
                      value={newOrder.weight}
                      onChange={(e) => setNewOrder({ ...newOrder, weight: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="pickupAddress">Pickup Address</Label>
                    <Input
                      id="pickupAddress"
                      placeholder="Enter pickup address"
                      value={newOrder.pickupAddress}
                      onChange={(e) => setNewOrder({ ...newOrder, pickupAddress: e.target.value })}
                    />
                  </div>

                  <div className="flex gap-2">
                    <Button 
                      onClick={handleCreateOrder} 
                      className="flex-1"
                      disabled={!selectedReceiver || !newOrder.description || !newOrder.weight}
                    >
                      Create Shipment
                    </Button>
                    <Button variant="outline" onClick={() => {
                      setShowNewOrderForm(false);
                      setSelectedReceiver(null);
                      setSearchQuery('');
                    }}>
                      Cancel
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* My Shipments */}
            <Card>
              <CardHeader>
                <CardTitle>My Shipments</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {sentOrders.length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <Package className="size-12 mx-auto mb-3 opacity-30" />
                      <p>No shipments yet</p>
                      <p className="text-sm">Create your first shipment to get started</p>
                    </div>
                  ) : (
                    sentOrders.map(order => (
                      <div
                        key={order.id}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-3">
                          <div>
                            <p className="text-neutral-900">Order #{order.id}</p>
                            <p className="text-sm text-neutral-500">{order.description}</p>
                          </div>
                          <StatusBadge status={order.status} />
                        </div>
                        
                        <div className="space-y-2 text-sm mb-3">
                          <div className="flex items-start gap-2">
                            <MapPin className="size-4 mt-0.5 text-neutral-400 flex-shrink-0" />
                            <div>
                              <p className="text-neutral-500">To: {order.recepteurName}</p>
                              <p className="text-neutral-900">{order.deliveryAddress}</p>
                            </div>
                          </div>
                          {order.livreurName && (
                            <div>
                              <p className="text-neutral-500">Delivery Person</p>
                              <p className="text-neutral-900">{order.livreurName}</p>
                            </div>
                          )}
                          <div>
                            <p className="text-neutral-500">Created</p>
                            <p className="text-neutral-900">{order.createdAt.toLocaleString()}</p>
                          </div>
                        </div>

                        {order.status === 'confirmed' && (
                          <div className="space-y-3">
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm">
                              <p className="text-blue-900">Order confirmed - Waiting for delivery assignment</p>
                            </div>
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleCancelOrder(order.id)}
                            >
                              <X className="size-4 mr-2" />
                              Cancel Order
                            </Button>
                          </div>
                        )}

                        {['picked_up', 'in_transit'].includes(order.status) && (
                          <div className="bg-amber-50 border border-amber-200 rounded-lg p-3 text-sm">
                            <p className="text-amber-900">Delivery in progress - Cannot cancel</p>
                          </div>
                        )}
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* RECEIVE TAB */}
          <TabsContent value="receive" className="space-y-6">
            <div>
              <h2 className="text-neutral-900 mb-2">Receive Packages</h2>
              <p className="text-neutral-500">Manage your incoming deliveries</p>
            </div>

            {/* Incoming Packages */}
            <Card>
              <CardHeader>
                <CardTitle>Incoming Packages</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {receivedOrders.filter(o => !['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status)).length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <Package className="size-12 mx-auto mb-3 opacity-30" />
                      <p>No incoming packages</p>
                    </div>
                  ) : (
                    receivedOrders
                      .filter(o => !['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status))
                      .map(order => (
                        <div
                          key={order.id}
                          className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                        >
                          <div className="flex items-start justify-between mb-3">
                            <div>
                              <p className="text-neutral-900">Order #{order.id}</p>
                              <p className="text-sm text-neutral-500">{order.description}</p>
                            </div>
                            <StatusBadge status={order.status} />
                          </div>
                          
                          <div className="space-y-2 text-sm mb-3">
                            <div>
                              <p className="text-neutral-500">From</p>
                              <p className="text-neutral-900">{order.emetteurName}</p>
                              <p className="text-neutral-600">{order.pickupAddress}</p>
                            </div>
                            <div className="flex items-start gap-2">
                              <MapPin className="size-4 mt-0.5 text-neutral-400 flex-shrink-0" />
                              <div>
                                <p className="text-neutral-500">Delivery to</p>
                                <p className="text-neutral-900">{order.deliveryAddress}</p>
                              </div>
                            </div>
                            {order.livreurName && (
                              <div>
                                <p className="text-neutral-500">Delivery Person</p>
                                <p className="text-neutral-900">{order.livreurName}</p>
                              </div>
                            )}
                            <div>
                              <p className="text-neutral-500">Weight</p>
                              <p className="text-neutral-900">{order.weight} kg</p>
                            </div>
                          </div>

                          {order.status === 'in_transit' && (
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                onClick={() => handleAcceptDelivery(order.id)}
                                className="flex-1"
                              >
                                <CheckCircle className="size-4 mr-2" />
                                Accept Delivery
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleRejectDelivery(order.id)}
                              >
                                <X className="size-4 mr-2" />
                                Reject
                              </Button>
                            </div>
                          )}

                          {['confirmed', 'picked_up'].includes(order.status) && (
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm">
                              <p className="text-blue-900">Package is on its way</p>
                            </div>
                          )}
                        </div>
                      ))
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Delivery History */}
            <Card>
              <CardHeader>
                <CardTitle>Delivery History</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {receivedOrders.filter(o => ['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status)).length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No delivery history</p>
                    </div>
                  ) : (
                    receivedOrders
                      .filter(o => ['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status))
                      .map(order => (
                        <div
                          key={order.id}
                          className="border border-neutral-200 rounded-lg p-4"
                        >
                          <div className="flex items-start justify-between mb-3">
                            <div>
                              <p className="text-neutral-900">Order #{order.id}</p>
                              <p className="text-sm text-neutral-500">{order.description}</p>
                            </div>
                            <StatusBadge status={order.status} />
                          </div>
                          
                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <div>
                              <p className="text-neutral-500">From</p>
                              <p className="text-neutral-900">{order.emetteurName}</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Completed</p>
                              <p className="text-neutral-900">{order.updatedAt.toLocaleDateString()}</p>
                            </div>
                          </div>
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