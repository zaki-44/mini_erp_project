import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Textarea } from './ui/textarea';
import { Label } from './ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { StatusBadge } from './StatusBadge';
import { Plus, MapPin, Package, LogOut, Send, Inbox, CheckCircle, X, Search, Truck, Star, Bell } from 'lucide-react';
import type { Order, User } from '../types';
import { fetchOrdersById, fetchOrderById,fetchOrders, createOrder, updateOrderStatus, deleteOrder, requestDriver, submitRating, searchClients, fetchNotifications, markNotificationAsRead, type Notification } from '../lib/api';

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
  const [allOrders, setAllOrders] = useState<Order[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [allUsers, setAllUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Orders sent by this client
  const [sentOrders, setSentOrders] = useState<Order[]>([]);
  
  // Orders received by this client
  const [receivedOrders, setReceivedOrders] = useState<Order[]>([]);

  const [showNewOrderForm, setShowNewOrderForm] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [selectedReceiver, setSelectedReceiver] = useState<User | null>(null);
  const [newOrder, setNewOrder] = useState({
    description: '',
    weight: '',
    pickupAddress: '',
    vehicleTypeNeeded: 'CAR',
    deliveryInstructions: '',
    price: '',
    addressDestination: '',
  });
  
  const [ratingOrderId, setRatingOrderId] = useState<string | null>(null);
  const [ratingValue, setRatingValue] = useState(5);
  const [ratingComment, setRatingComment] = useState('');
  const [orderSearchId, setOrderSearchId] = useState('');

  // Fetch data on component mount
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);
      try {
        const [ordersData, notificationsData] = await Promise.all([
          fetchOrders(),
          fetchNotifications()
        ]);
        setAllOrders(ordersData);
        setNotifications(notificationsData);
        setAllUsers([]); // TODO: Implement fetchUsers API if needed
        
        // Filter orders for this user - using idClientSource for sent, idClientDestination for received
        // Note: user.id is string, but idClientSource is number, so we need to convert
        const userIdNum = parseInt(user.id);
        setSentOrders(ordersData.filter(o => o.idClientSource === userIdNum));
        setReceivedOrders(ordersData.filter(o => o.idClientDestination === userIdNum));
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load data');
        setAllOrders([]);
        setNotifications([]);
        setAllUsers([]);
        setSentOrders([]);
        setReceivedOrders([]);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user.id]);

  const handleSearchUsers = async (query: string) => {
    setSearchQuery(query);
    if (query.trim().length < 2) {
      setSearchResults([]);
      return;
    }
    
    try {
      const results = await searchClients(query);
      setSearchResults(results);
    } catch (err) {
      console.error("Search failed", err);
    }
  };

  const handleSelectReceiver = (receiver: any) => {
    setSelectedReceiver(receiver);
    setSearchQuery(receiver.name || `${receiver.firstName} ${receiver.lastName}`);
    setSearchResults([]);
  };

  const handleSearchOrder = async () => {
    if (!orderSearchId.trim()) {
      const userIdNum = parseInt(user.id);
      setSentOrders(allOrders.filter(o => o.idClientSource === userIdNum));
      return;
    }

    try {
      const order = await fetchOrderById(orderSearchId);
      if (order && order.idClientSource === parseInt(user.id)) {
        setSentOrders([order]);
      } else {
        setSentOrders([]);
      }
    } catch (err) {
      setSentOrders([]);
    }
  };

  const handleCreateOrder = async () => {
    try {
      const orderData = {
        vehicleTypeNeeded: newOrder.vehicleTypeNeeded,
        addressSource: newOrder.pickupAddress,
        addressDestination: selectedReceiver?.address || newOrder.addressDestination,
        weight: parseFloat(newOrder.weight),
        price: parseFloat(newOrder.price) || 0,
        description: newOrder.description,
        deliveryInstructions: newOrder.deliveryInstructions,
      };

      const createdOrder = await createOrder(orderData);
      setSentOrders([createdOrder, ...sentOrders]);
      setAllOrders([createdOrder, ...allOrders]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create order');
    }

    setShowNewOrderForm(false);
    setSearchQuery('');
    setSelectedReceiver(null);
    setNewOrder({
      description: '',
      weight: '',
      pickupAddress: '',
      vehicleTypeNeeded: 'CAR',
      deliveryInstructions: '',
      price: '',
      addressDestination: '',
    });
  };

  const handleCancelOrder = async (orderId: string) => {
    try {
      await deleteOrder(orderId);
      setSentOrders(sentOrders.filter(o => o.idPackage.toString() !== orderId));
      setAllOrders(allOrders.filter(o => o.idPackage.toString() !== orderId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to cancel order');
    }
  };

  const handleRequestDriver = async (orderId: string) => {
    try {
      const response = await requestDriver(orderId);
      alert(`${response.message}\nDriver: ${response.delivererName}`);
      // Refresh orders to show updated status
      const ordersData = await fetchOrders();
      setAllOrders(ordersData);
      setSentOrders(ordersData.filter(o => o.idClientSource === parseInt(user.id)));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to request driver');
    }
  };

  const handleRateDeliverer = async (delivererId: number, rating: number, comment: string) => {
    try {
      await submitRating({ idDeliverer: delivererId, rating, comment });
      setRatingOrderId(null);
      // Could add a success toast here
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit rating');
    }
  };

  const handleAcceptDelivery = async (orderId: string) => {
    try {
      const updatedOrder = await updateOrderStatus(orderId, 'DELIVERED');
      setReceivedOrders(receivedOrders.map(o => o.idPackage.toString() === orderId ? updatedOrder : o));
      setAllOrders(allOrders.map(o => o.idPackage.toString() === orderId ? updatedOrder : o));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to accept delivery');
    }
  };

  const handleRejectDelivery = async (orderId: string) => {
    try {
      // Note: CANCELLED is used for rejected deliveries in the new status system
      const updatedOrder = await updateOrderStatus(orderId, 'CANCELLED');
      setReceivedOrders(receivedOrders.map(o => o.idPackage.toString() === orderId ? updatedOrder : o));
      setAllOrders(allOrders.map(o => o.idPackage.toString() === orderId ? updatedOrder : o));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reject delivery');
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await markNotificationAsRead(id);
      setNotifications(notifications.map(n => n.id === id ? { ...n, isRead: true } : n));
    } catch (err) {
      console.error("Failed to mark notification as read", err);
    }
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
        {error && (
          <div className="mb-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg text-yellow-900">
            <p className="text-sm">⚠️ {error} (Using fallback data)</p>
          </div>
        )}
        <Tabs defaultValue="send" className="space-y-6">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="send">
              <Send className="size-4 mr-2" />
              Send Packages
            </TabsTrigger>
            <TabsTrigger value="receive">
              <Inbox className="size-4 mr-2" />
              Receive Packages
            </TabsTrigger>
            <TabsTrigger value="notifications" className="relative">
              <Bell className="size-4 mr-2" />
              Notifications
              {notifications.filter(n => !n.isRead).length > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full size-4 flex items-center justify-center">
                  {notifications.filter(n => !n.isRead).length}
                </span>
              )}
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
                        {searchResults.map((receiver: any) => (
                          <button
                            key={receiver.id}
                            onClick={() => handleSelectReceiver(receiver)}
                            className="w-full text-left px-4 py-3 hover:bg-neutral-50 border-b border-neutral-100 last:border-b-0"
                          >
                            <p className="text-neutral-900">{receiver.name || `${receiver.firstName} ${receiver.lastName}`}</p>
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
                            <p className="text-green-900">{selectedReceiver.name || `${(selectedReceiver as any).firstName} ${(selectedReceiver as any).lastName}`}</p>
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
                    <Label htmlFor="price">Price</Label>
                    <Input
                      id="price"
                      type="number"
                      placeholder="Enter price"
                      value={newOrder.price}
                      onChange={(e) => setNewOrder({ ...newOrder, price: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="deliveryInstructions">Delivery Instructions</Label>
                    <Textarea
                      id="deliveryInstructions"
                      placeholder="Enter delivery instructions"
                      value={newOrder.deliveryInstructions}
                      onChange={(e) => setNewOrder({ ...newOrder, deliveryInstructions: e.target.value })}
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

                  <div className="space-y-2">
                    <Label htmlFor="addressDestination">Destination Address</Label>
                    <Input
                      id="addressDestination"
                      placeholder="Enter destination address (if no receiver selected)"
                      value={newOrder.addressDestination}
                      onChange={(e) => setNewOrder({ ...newOrder, addressDestination: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="vehicleTypeNeeded">Vehicle Type Needed</Label>
                    <select
                      id="vehicleTypeNeeded"
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                      value={newOrder.vehicleTypeNeeded}
                      onChange={(e) => setNewOrder({ ...newOrder, vehicleTypeNeeded: e.target.value })}
                    >
                      <option value="BIKE">BIKE</option>
                      <option value="CAR">CAR</option>
                      <option value="TRUCK">TRUCK</option>
                      <option value="VAN">VAN</option>
                    </select>
                  </div>

                  <div className="flex gap-2">
                    <Button 
                      onClick={handleCreateOrder} 
                      className="flex-1"
                      disabled={!newOrder.description || !newOrder.weight}
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
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle>My Shipments</CardTitle>
                <div className="flex items-center space-x-2">
                  <Input
                    placeholder="Order ID"
                    className="h-8 w-[150px]"
                    value={orderSearchId}
                    onChange={(e) => setOrderSearchId(e.target.value)}
                  />
                  <Button size="sm" variant="ghost" onClick={handleSearchOrder}>
                    <Search className="size-4" />
                  </Button>
                </div>
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
                        key={order.idPackage}
                        className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                      >
                        <div className="flex items-start justify-between mb-3">
                          <div>
                            <p className="text-neutral-900">Order #{order.idPackage}</p>
                            <p className="text-sm text-neutral-500">{order.description || 'No description'}</p>
                          </div>
                          <StatusBadge status={order.status} />
                        </div>
                        
                        <div className="space-y-2 text-sm mb-3">
                          <div className="flex items-start gap-2">
                            <MapPin className="size-4 mt-0.5 text-neutral-400 flex-shrink-0" />
                            <div>
                              <p className="text-neutral-500">To: Client #{order.idClientDestination || 'N/A'}</p>
                              <p className="text-neutral-900">{order.addressDestination}</p>
                            </div>
                          </div>
                          <div>
                            <p className="text-neutral-500">Weight / Price</p>
                            <p className="text-neutral-900">{order.weight} kg / ${order.price}</p>
                          </div>
                          <div>
                            <p className="text-neutral-500">Created</p>
                            <p className="text-neutral-900">{new Date(order.createdAt).toLocaleString()}</p>
                          </div>
                        </div>

                        {order.status === 'CREATED' && (
                          <div className="space-y-3">
                            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm">
                              <p className="text-blue-900">Order created</p>
                            </div>
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                onClick={() => handleRequestDriver(order.idPackage.toString())}
                                className="flex-1"
                              >
                                <Truck className="size-4 mr-2" />
                                Request Driver
                              </Button>
                              <Button
                                variant="destructive"
                                size="sm"
                                onClick={() => handleCancelOrder(order.idPackage.toString())}
                              >
                                <X className="size-4 mr-2" />
                                Cancel
                              </Button>
                            </div>
                          </div>
                        )}

                        {order.status === 'DELIVERED' && (
                          <div className="mt-3 pt-3 border-t border-neutral-100">
                            {ratingOrderId === order.idPackage.toString() ? (
                              <div className="bg-neutral-50 p-3 rounded-lg space-y-3 border border-neutral-200">
                                <p className="font-medium text-sm">Rate Delivery</p>
                                <div className="flex items-center gap-1">
                                  {[1, 2, 3, 4, 5].map((star) => (
                                    <button
                                      key={star}
                                      onClick={() => setRatingValue(star)}
                                      className={`p-1 hover:scale-110 transition-transform ${star <= ratingValue ? 'text-yellow-400' : 'text-gray-300'}`}
                                      type="button"
                                    >
                                      <Star className="size-5 fill-current" />
                                    </button>
                                  ))}
                                </div>
                                <Textarea 
                                  placeholder="Add a comment..." 
                                  value={ratingComment}
                                  onChange={(e) => setRatingComment(e.target.value)}
                                  className="text-sm"
                                />
                                <div className="flex gap-2">
                                  <Button size="sm" onClick={() => {
                                    // Cast to any to access idDeliverer if it's not in the Order type definition yet
                                    const delivererId = (order as any).idDeliverer;
                                    if (delivererId) {
                                      handleRateDeliverer(delivererId, ratingValue, ratingComment);
                                    } else {
                                      setError("Cannot rate: Deliverer information missing");
                                    }
                                  }}>Submit</Button>
                                  <Button size="sm" variant="ghost" onClick={() => setRatingOrderId(null)}>Cancel</Button>
                                </div>
                              </div>
                            ) : (
                              <Button variant="outline" size="sm" onClick={() => {
                                setRatingOrderId(order.idPackage.toString());
                                setRatingValue(5);
                                setRatingComment('');
                              }}>
                                <Star className="size-4 mr-2" />
                                Rate Deliverer
                              </Button>
                            )}
                          </div>
                        )}

                        {['ASSIGNED', 'IN_TRANSIT'].includes(order.status) && (
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
                  {receivedOrders.filter(o => !['DELIVERED', 'CANCELLED'].includes(o.status)).length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <Package className="size-12 mx-auto mb-3 opacity-30" />
                      <p>No incoming packages</p>
                    </div>
                  ) : (
                    receivedOrders
                      .filter(o => !['DELIVERED', 'CANCELLED'].includes(o.status))
                      .map(order => (
                        <div
                          key={order.idPackage}
                          className="border border-neutral-200 rounded-lg p-4 hover:border-neutral-300 transition-colors"
                        >
                          <div className="flex items-start justify-between mb-3">
                            <div>
                              <p className="text-neutral-900">Order #{order.idPackage}</p>
                              <p className="text-sm text-neutral-500">{order.description || 'No description'}</p>
                            </div>
                            <StatusBadge status={order.status} />
                          </div>
                          
                          <div className="space-y-2 text-sm mb-3">
                            <div>
                              <p className="text-neutral-500">From</p>
                              <p className="text-neutral-900">Client #{order.idClientSource}</p>
                              <p className="text-neutral-600">{order.addressSource}</p>
                            </div>
                            <div className="flex items-start gap-2">
                              <MapPin className="size-4 mt-0.5 text-neutral-400 flex-shrink-0" />
                              <div>
                                <p className="text-neutral-500">Delivery to</p>
                                <p className="text-neutral-900">{order.addressDestination}</p>
                              </div>
                            </div>
                            <div>
                              <p className="text-neutral-500">Weight / Price</p>
                              <p className="text-neutral-900">{order.weight} kg / ${order.price}</p>
                            </div>
                          </div>

                          {order.status === 'IN_TRANSIT' && (
                            <div className="flex gap-2">
                              <Button
                                size="sm"
                                onClick={() => handleAcceptDelivery(order.idPackage.toString())}
                                className="flex-1"
                              >
                                <CheckCircle className="size-4 mr-2" />
                                Accept Delivery
                              </Button>
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => handleRejectDelivery(order.idPackage.toString())}
                              >
                                <X className="size-4 mr-2" />
                                Reject
                              </Button>
                            </div>
                          )}

                          {['CREATED', 'ASSIGNED'].includes(order.status) && (
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
                  {receivedOrders.filter(o => ['DELIVERED', 'CANCELLED'].includes(o.status)).length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No delivery history</p>
                    </div>
                  ) : (
                    receivedOrders
                      .filter(o => ['DELIVERED', 'CANCELLED'].includes(o.status))
                      .map(order => (
                        <div
                          key={order.idPackage}
                          className="border border-neutral-200 rounded-lg p-4"
                        >
                          <div className="flex items-start justify-between mb-3">
                            <div>
                              <p className="text-neutral-900">Order #{order.idPackage}</p>
                              <p className="text-sm text-neutral-500">{order.description || 'No description'}</p>
                            </div>
                            <StatusBadge status={order.status} />
                          </div>
                          
                          <div className="grid grid-cols-2 gap-3 text-sm">
                            <div>
                              <p className="text-neutral-500">From</p>
                              <p className="text-neutral-900">Client #{order.idClientSource}</p>
                            </div>
                            <div>
                              <p className="text-neutral-500">Completed</p>
                              <p className="text-neutral-900">{new Date(order.createdAt).toLocaleDateString()}</p>
                            </div>
                          </div>
                        </div>
                      ))
                  )}
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* NOTIFICATIONS TAB */}
          <TabsContent value="notifications" className="space-y-6">
            <div>
              <h2 className="text-neutral-900 mb-2">Notifications</h2>
              <p className="text-neutral-500">Updates about your shipments</p>
            </div>

            <Card>
              <CardContent className="p-0">
                {notifications.length === 0 ? (
                  <div className="text-center py-12 text-neutral-500">
                    <Bell className="size-12 mx-auto mb-3 opacity-30" />
                    <p>No notifications</p>
                  </div>
                ) : (
                  <div className="divide-y divide-neutral-100">
                    {notifications.map(notification => (
                      <div 
                        key={notification.id} 
                        className={`p-4 flex items-start justify-between hover:bg-neutral-50 transition-colors ${!notification.isRead ? 'bg-blue-50/50' : ''}`}
                      >
                        <div className="space-y-1">
                          <p className={`text-sm ${!notification.isRead ? 'font-semibold text-neutral-900' : 'text-neutral-700'}`}>
                            {notification.message}
                          </p>
                          <p className="text-xs text-neutral-500">{notification.dateNotif}</p>
                        </div>
                        {!notification.isRead && (
                          <Button size="sm" variant="ghost" onClick={() => handleMarkAsRead(notification.id)}>
                            Mark as read
                          </Button>
                        )}
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