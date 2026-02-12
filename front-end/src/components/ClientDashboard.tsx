import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Textarea } from './ui/textarea';
import { Label } from './ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { StatusBadge } from './StatusBadge';
import { 
  Plus, MapPin, Package, LogOut, Search, Truck, Bell, 
  User as UserIcon, X, Pencil, Save, Trash2, UserPlus, Star // Added Star icon
} from 'lucide-react';
import type { Order } from '../types';
import { 
  fetchOrders, 
  fetchOrderById, 
  createOrder, 
  updatePackage,
  deleteOrder,
  requestDriver,
  submitRating, // 1. IMPORT ADDED
  searchClients, 
  fetchNotifications, 
  markNotificationAsRead, 
  type Notification,
  type CreatePackagePayload 
} from '../lib/api';

interface AuthUser {
  id: number;
  name?: string;
  email?: string;
  role: 'CLIENT' | 'ADMIN' | 'DELIVERER';
}

interface ClientDashboardProps {
  user: AuthUser;
  onLogout: () => void;
}

interface NewOrderState {
  recipientId: number | null;
  recipientName: string;
  pickupAddress: string;
  deliveryAddress: string;
  description: string;
  weight: string;
  price: string;
  vehicleType: 'CAR' | 'BIKE' | 'TRUCK' ;
}

export function ClientDashboard({ user, onLogout }: ClientDashboardProps) {
  const [myOrders, setMyOrders] = useState<Order[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Search State
  const [orderIdSearch, setOrderIdSearch] = useState('');
  
  // Edit Mode State
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editForm, setEditForm] = useState({
    description: '',
    weight: '',
    price: ''
  });

  // 2. RATING STATE ADDED
  const [ratingId, setRatingId] = useState<number | null>(null);
  const [ratingData, setRatingData] = useState({
    rating: 5,
    comment: ''
  });

  // Create Order Form State
  const [newOrder, setNewOrder] = useState<NewOrderState>({
    recipientId: null,
    recipientName: '',
    pickupAddress: '',
    deliveryAddress: '',
    description: '',
    weight: '',
    price: '',
    vehicleType: 'BIKE'
  });

  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, [user.id]);

  async function loadDashboardData() {
    try {
      setLoading(true);
      const orders = await fetchOrders();
      setMyOrders(orders);
      
      const notifs = await fetchNotifications();
      setNotifications(notifs);
    } catch (error) {
      console.error("Failed to load dashboard:", error);
    } finally {
      setLoading(false);
    }
  }

  // --- RATING FUNCTION ---
  const openRating = (orderId: number) => {
    setRatingId(orderId);
    setRatingData({ rating: 5, comment: '' });
  };

  const cancelRating = () => {
    setRatingId(null);
  };

  const handleRateDriver = async (delivererId: number) => {
    if (!ratingId) return;

    try {
      await submitRating({
        idDeliverer: delivererId,
        rating: ratingData.rating,
        comment: ratingData.comment
      });
      
      alert("Rating submitted successfully!");
      setRatingId(null);
    } catch (error) {
      console.error("Failed to submit rating", error);
      alert("Failed to submit rating. Please try again.");
    }
  };

  // --- REQUEST DRIVER FUNCTION ---
  const handleRequestDriver = async (orderId: number) => {
    try {
      setLoading(true);
      await requestDriver(orderId.toString());
      alert("Driver requested successfully! The system is finding a match.");
      await loadDashboardData(); 
    } catch (error) {
      console.error("Failed to request driver", error);
      alert("Failed to request driver. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  // --- DELETE FUNCTION ---
  const handleDeleteOrder = async (orderId: number) => {
    if (!confirm("Are you sure you want to delete this package? This action cannot be undone.")) return;

    try {
      await deleteOrder(orderId.toString());
      setMyOrders(prev => prev.filter(o => o.idPackage !== orderId));
      alert("Package deleted successfully.");
    } catch (error) {
      console.error("Delete failed", error);
      alert("Failed to delete package.");
    }
  };

  // --- EDIT FUNCTIONS ---
  const startEditing = (order: Order) => {
    setEditingId(order.idPackage);
    setEditForm({
      description: order.description || '',
      weight: order.weight.toString(),
      price: order.price.toString()
    });
  };

  const cancelEditing = () => {
    setEditingId(null);
    setEditForm({ description: '', weight: '', price: '' });
  };

  const saveEdit = async () => {
    if (!editingId) return;

    try {
      await updatePackage(editingId.toString(), {
        description: editForm.description,
        weight: parseFloat(editForm.weight),
        price: parseFloat(editForm.price)
      });

      setEditingId(null);
      await loadDashboardData();
      alert("Order updated successfully!");
    } catch (error) {
      console.error("Update failed", error);
      alert("Failed to update order.");
    }
  };

  // --- SEARCH FUNCTIONS ---
  const handleSearchOrder = async () => {
    if (!orderIdSearch.trim()) {
      loadDashboardData();
      return;
    }
    setLoading(true);
    try {
      const order = await fetchOrderById(orderIdSearch);
      setMyOrders(order ? [order] : []);
    } catch (error) {
      console.error("Search failed:", error);
      setMyOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const clearSearch = () => {
    setOrderIdSearch('');
    loadDashboardData();
  };

  const handleSearchRecipient = async () => {
    if (!searchQuery.trim()) return;
    setIsSearching(true);
    try {
      const results = await searchClients(searchQuery);
      setSearchResults(results.filter((c: any) => c.id !== user.id));
    } catch (err) {
      console.error("Search failed", err);
    } finally {
      setIsSearching(false);
    }
  };

  const selectRecipient = (client: any) => {
    setNewOrder(prev => ({
      ...prev,
      recipientId: client.id,
      recipientName: `${client.firstName} ${client.lastName}`
    }));
    setSearchResults([]);
    setSearchQuery('');
  };

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newOrder.recipientId) {
      alert("Please search and select a recipient first.");
      return;
    }

    try {
      const payload: CreatePackagePayload = {
        vehicleTypeNeeded: newOrder.vehicleType,
        addressSource: newOrder.pickupAddress,
        addressDestination: newOrder.deliveryAddress,
        description: newOrder.description,
        weight: parseFloat(newOrder.weight),
        price: parseFloat(newOrder.price),
        idClientDestination: newOrder.recipientId
      };

      await createOrder(payload);
      
      setNewOrder({
        recipientId: null,
        recipientName: '',
        pickupAddress: '',
        deliveryAddress: '',
        description: '',
        weight: '',
        price: '',
        vehicleType: 'BIKE'
      });
      
      loadDashboardData();
      alert("Package created successfully!");
    } catch (error) {
      console.error("Failed to create order:", error);
      alert("Failed to create order. Check console for details.");
    }
  };

  const handleMarkAsRead = async (id: number) => {
    try {
      await markNotificationAsRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="min-h-screen bg-neutral-50 pb-20">
      <header className="bg-white border-b sticky top-0 z-30 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-2">
          <div className="bg-primary/10 p-2 rounded-lg">
            {/* <Package className="h-6 w-6 text-primary" /> */}
            <div className='w-[70px]'><img src="\src\logo-mdiwasi.png" alt="" /></div>
          </div>
          <h1 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-primary/60">
            Client Dashboard
          </h1>
        </div>
        <div className="flex items-center gap-4">
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium">{user.name || 'Client'}</p>
            <p className="text-xs text-muted-foreground">{user.email}</p>
          </div>
          <Button variant="ghost" size="icon" onClick={onLogout} className="text-muted-foreground hover:text-destructive">
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </header>

      <main className="max-w-5xl mx-auto p-6 space-y-6">
        <Tabs defaultValue="new-package" className="space-y-6">
          <TabsList className="grid w-full grid-cols-2 max-w-[400px]">
            <TabsTrigger value="new-package">New Package</TabsTrigger>
            <TabsTrigger value="my-packages">My Packages</TabsTrigger>
          </TabsList>

          <TabsContent value="new-package">
            <Card>
              <CardHeader>
                <CardTitle>Send a New Package</CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleCreateOrder} className="space-y-6">
                  {/* ... (Create Package Form - Unchanged) ... */}
                  <div className="space-y-2">
                    <Label>Recipient</Label>
                    <div className="flex gap-2">
                      <Input 
                        placeholder="Search by name..." 
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                      />
                      <Button type="button" onClick={handleSearchRecipient} disabled={isSearching}>
                        <Search className="h-4 w-4" />
                      </Button>
                    </div>
                    
                    {searchResults.length > 0 && (
                      <div className="border rounded-md mt-2 bg-white shadow-sm divide-y">
                        {searchResults.map(client => (
                          <div 
                            key={client.id} 
                            className="p-3 hover:bg-neutral-50 cursor-pointer flex justify-between items-center"
                            onClick={() => selectRecipient(client)}
                          >
                            <div className="flex items-center gap-3">
                              <div className="bg-neutral-100 p-2 rounded-full">
                                <UserIcon className="h-4 w-4 text-neutral-500" />
                              </div>
                              <div>
                                <p className="text-sm font-medium">{client.firstName} {client.lastName}</p>
                                <p className="text-xs text-neutral-500">{client.email}</p>
                              </div>
                            </div>
                            <Button size="sm" variant="ghost">Select</Button>
                          </div>
                        ))}
                      </div>
                    )}

                    {newOrder.recipientName && (
                      <div className="flex items-center gap-2 text-sm text-green-600 bg-green-50 p-3 rounded-md border border-green-100">
                        <Truck className="h-4 w-4" />
                        Selected: <span className="font-medium">{newOrder.recipientName}</span>
                      </div>
                    )}
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <Label>Pickup Address (Source)</Label>
                      <div className="relative">
                        <MapPin className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                        <Input 
                          className="pl-9" 
                          placeholder="Where to pick up?" 
                          value={newOrder.pickupAddress}
                          onChange={e => setNewOrder({...newOrder, pickupAddress: e.target.value})}
                          required
                        />
                      </div>
                    </div>
                    <div className="space-y-2">
                      <Label>Delivery Address (Destination)</Label>
                      <div className="relative">
                        <MapPin className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                        <Input 
                          className="pl-9" 
                          placeholder="Where to deliver?" 
                          value={newOrder.deliveryAddress}
                          onChange={e => setNewOrder({...newOrder, deliveryAddress: e.target.value})}
                          required
                        />
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>Package Description</Label>
                    <Textarea 
                      placeholder="What's in the package?" 
                      value={newOrder.description}
                      onChange={e => setNewOrder({...newOrder, description: e.target.value})}
                      required
                    />
                  </div>

                  <div className="grid grid-cols-3 gap-6">
                    <div className="space-y-2">
                      <Label>Weight (kg)</Label>
                      <Input 
                        type="number" 
                        step="0.1" 
                        value={newOrder.weight}
                        onChange={e => setNewOrder({...newOrder, weight: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Price (DA)</Label>
                      <Input 
                        type="number" 
                        value={newOrder.price}
                        onChange={e => setNewOrder({...newOrder, price: e.target.value})}
                        required
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>Vehicle Needed</Label>
                      <Select 
                        value={newOrder.vehicleType} 
                        onValueChange={(val: any) => setNewOrder({...newOrder, vehicleType: val})}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="BIKE">Bike</SelectItem>
                          <SelectItem value="CAR">Car</SelectItem>
                          <SelectItem value="TRUCK">Truck</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <Button type="submit" className="w-full h-11 text-base">
                    <Plus className="mr-2 h-5 w-5" /> Create Package Request
                  </Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="my-packages">
            <Card>
              <CardHeader>
                <CardTitle>My Packages</CardTitle>
              </CardHeader>
              <CardContent>
                {/* Search Bar */}
                <div className="flex gap-2 mb-6">
                  <div className="relative flex-1">
                    <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search by Order ID..."
                      value={orderIdSearch}
                      onChange={(e) => setOrderIdSearch(e.target.value)}
                      className="pl-8"
                      onKeyDown={(e) => e.key === 'Enter' && handleSearchOrder()}
                    />
                  </div>
                  <Button onClick={handleSearchOrder}>Search</Button>
                  {orderIdSearch && (
                    <Button variant="outline" size="icon" onClick={clearSearch} title="Clear Search">
                      <X className="h-4 w-4" />
                    </Button>
                  )}
                </div>

                {loading ? (
                  <p className="text-center py-8">Loading...</p>
                ) : myOrders.length === 0 ? (
                  <div className="text-center py-12 text-muted-foreground">
                    <Package className="h-12 w-12 mx-auto mb-4 opacity-20" />
                    <p>No packages found.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {myOrders.map((order) => {
                      const delivererId = (order as any).idDeliverer || (order as any).idLivreur || (order as any).delivererId;
                      const isRateable = ['DELIVERED', 'COMPLETED'].includes(order.status?.toUpperCase() || '');

                      return (
                        /* KEY 1: Main Order Card */
                        <div key={order.idPackage} className={`p-4 border rounded-lg bg-white transition-colors ${editingId === order.idPackage || ratingId === order.idPackage ? 'border-primary shadow-md ring-1 ring-primary' : 'hover:border-primary/50'}`}>
                          
                          {/* --- RATING MODE UI --- */}
                          {ratingId === order.idPackage ? (
                            <div className="space-y-4">
                              <div className="flex justify-between items-center mb-2">
                                <span className="font-semibold text-lg">Rate Driver for #{order.idPackage}</span>
                                <StatusBadge status={order.status} />
                              </div>
                              
                              <div className="flex gap-1 justify-center py-2">
                                {[1, 2, 3, 4, 5].map((star) => (
                                  /* KEY 2: Star Rating Buttons */
                                  <button
                                    key={`star-${order.idPackage}-${star}`}
                                    type="button"
                                    onClick={() => setRatingData(prev => ({ ...prev, rating: star }))}
                                    className={`p-1 transition-colors ${ratingData.rating >= star ? 'text-yellow-500' : 'text-neutral-300'}`}
                                  >
                                    <Star className="h-8 w-8 fill-current" />
                                  </button>
                                ))}
                              </div>

                              <div className="grid gap-2">
                                <Label>Comment (Optional)</Label>
                                <Textarea 
                                  placeholder="How was the delivery?"
                                  value={ratingData.comment}
                                  onChange={e => setRatingData({...ratingData, comment: e.target.value})}
                                />
                              </div>

                              <div className="flex justify-end gap-2 pt-2">
                                <Button variant="outline" size="sm" onClick={cancelRating}>
                                  <X className="h-4 w-4 mr-1" /> Cancel
                                </Button>
                                <Button size="sm" onClick={() => handleRateDriver(delivererId)}>
                                  Submit Rating
                                </Button>
                              </div>
                            </div>
                          ) : editingId === order.idPackage ? (
                            /* --- EDIT MODE UI --- */
                            <div className="space-y-4">
                              <div className="flex justify-between items-center mb-2">
                                <span className="font-semibold text-lg">Editing #{order.idPackage}</span>
                                <StatusBadge status={order.status} />
                              </div>
                              
                              <div className="grid gap-4">
                                <div className="grid gap-2">
                                  <Label>Description</Label>
                                  <Input 
                                    value={editForm.description} 
                                    onChange={e => setEditForm({...editForm, description: e.target.value})}
                                  />
                                </div>
                                <div className="grid grid-cols-2 gap-4">
                                  <div className="grid gap-2">
                                    <Label>Price (DA)</Label>
                                    <Input 
                                      type="number"
                                      value={editForm.price} 
                                      onChange={e => setEditForm({...editForm, price: e.target.value})}
                                    />
                                  </div>
                                  <div className="grid gap-2">
                                    <Label>Weight (kg)</Label>
                                    <Input 
                                      type="number"
                                      value={editForm.weight} 
                                      onChange={e => setEditForm({...editForm, weight: e.target.value})}
                                    />
                                  </div>
                                </div>
                              </div>

                              <div className="flex justify-end gap-2 pt-2">
                                <Button variant="outline" size="sm" onClick={cancelEditing}>
                                  <X className="h-4 w-4 mr-1" /> Cancel
                                </Button>
                                <Button size="sm" onClick={saveEdit}>
                                  <Save className="h-4 w-4 mr-1" /> Save Changes
                                </Button>
                              </div>
                            </div>
                          ) : (
                            /* --- NORMAL VIEW UI --- */
                            <div className="flex items-center justify-between">
                              <div className="space-y-1">
                                <div className="flex items-center gap-2">
                                  <span className="font-semibold text-lg">#{order.idPackage}</span>
                                  <StatusBadge status={order.status} />
                                </div>
                                <p className="text-sm text-muted-foreground">{order.description}</p>
                                <div className="flex items-center gap-4 text-xs text-muted-foreground mt-2">
                                  <span className="flex items-center gap-1"><MapPin className="h-3 w-3" /> {order.addressDestination}</span>
                                  <span className="flex items-center gap-1"><Truck className="h-3 w-3" /> {order.vehicleTypeNeeded}</span>
                                </div>
                              </div>
                              <div className="text-right flex flex-col items-end gap-2">
                                <div>
                                  <p className="font-bold text-lg">{order.price} DA</p>
                                  <p className="text-xs text-muted-foreground">{new Date(order.createdAt).toLocaleDateString()}</p>
                                </div>
                                
                                <div className="flex gap-2 flex-wrap justify-end">
                                  {order.status === 'CREATED' && (
                                    <Button 
                                      size="sm" 
                                      onClick={() => handleRequestDriver(order.idPackage)}
                                      className="bg-blue-600 hover:bg-blue-700 text-white"
                                    >
                                      <UserPlus className="h-4 w-4 mr-1" /> Request Driver
                                    </Button>
                                  )}

                                  {isRateable && delivererId && (
                                    <Button 
                                      size="sm" 
                                      variant="outline"
                                      onClick={() => openRating(order.idPackage)}
                                      className="text-yellow-600 border-yellow-600 hover:bg-yellow-50"
                                    >
                                      <Star className="h-4 w-4 mr-1" /> Rate Driver
                                    </Button>
                                  )}

                                  {order.status !== 'DELIVERED' && order.status !== 'CANCELLED' && (
                                    <>
                                      <Button variant="ghost" size="sm" onClick={() => startEditing(order)}>
                                        <Pencil className="h-4 w-4 mr-1" /> Edit
                                      </Button>
                                      <Button variant="ghost" size="sm" onClick={() => handleDeleteOrder(order.idPackage)} className="text-red-500 hover:text-red-700 hover:bg-red-50">
                                        <Trash2 className="h-4 w-4 mr-1" /> Delete
                                      </Button>
                                    </>
                                  )}
                                </div>
                              </div>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
            
            <Card className="mt-6">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Bell className="h-5 w-5" /> Notifications
                </CardTitle>
              </CardHeader>
              <CardContent>
                {notifications.length === 0 ? (
                  <p className="text-muted-foreground text-sm">No new notifications.</p>
                ) : (
                  <div className="space-y-2">
                    {notifications.map(n => (
                      /* KEY 3: Notifications List */
                      <div key={n.id} className={`p-3 rounded border text-sm flex justify-between ${n.isRead ? 'bg-white' : 'bg-blue-50 border-blue-100'}`}>
                        <span>{n.message}</span>
                        {!n.isRead && (
                          <Button variant="ghost" size="sm" className="h-auto p-0 text-blue-600" onClick={() => handleMarkAsRead(n.id)}>
                            Mark read
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
    </div>
  );
}