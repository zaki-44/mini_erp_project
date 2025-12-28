import { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { mockOrders, mockUsers } from '../lib/mockData';
import { StatusBadge } from './StatusBadge';
import { Package, Truck, CheckCircle, LogOut, Users, Plus, Trash2, UserCheck } from 'lucide-react';
import type { Order, User } from '../types';

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
  const [orders] = useState<Order[]>(mockOrders);
  const [users, setUsers] = useState<User[]>(mockUsers);
  const [showAddUserForm, setShowAddUserForm] = useState(false);
  const [newUser, setNewUser] = useState({
    name: '',
    email: '',
    password: '',
    phone: '',
    address: '',
    role: 'client' as 'client' | 'livreur',
  });

  const handleAddUser = () => {
    const user: User = {
      id: `user_${Date.now()}`,
      name: newUser.name,
      email: newUser.email,
      role: newUser.role,
      phone: newUser.phone,
      address: newUser.address || undefined,
      latitude: 48.8566 + (Math.random() - 0.5) * 0.1,
      longitude: 2.3522 + (Math.random() - 0.5) * 0.1,
    };

    setUsers([...users, user]);
    setShowAddUserForm(false);
    setNewUser({
      name: '',
      email: '',
      password: '',
      phone: '',
      address: '',
      role: 'client',
    });
  };

  const handleRemoveUser = (userId: string) => {
    setUsers(users.filter(u => u.id !== userId));
  };

  const stats = {
    totalOrders: orders.length,
    activeDeliveries: orders.filter(o => ['confirmed', 'in_transit', 'picked_up'].includes(o.status)).length,
    delivered: orders.filter(o => o.status === 'delivered').length,
    totalClients: users.filter(u => u.role === 'client').length,
    totalLivreurs: users.filter(u => u.role === 'livreur').length,
  };

  const activeOrders = orders.filter(o => ['confirmed', 'picked_up', 'in_transit'].includes(o.status));
  const completedOrders = orders.filter(o => ['delivered', 'cancelled', 'rejected_by_receiver'].includes(o.status));
  const clients = users.filter(u => u.role === 'client');
  const livreurs = users.filter(u => u.role === 'livreur');

  return (
    <>
      {/* Header */}
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

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <Tabs defaultValue="overview" className="space-y-8">
          <TabsList className="grid w-full grid-cols-2 max-w-md">
            <TabsTrigger value="overview">
              <Package className="size-4 mr-2" />
              Overview
            </TabsTrigger>
            <TabsTrigger value="users">
              <Users className="size-4 mr-2" />
              Users
            </TabsTrigger>
          </TabsList>

          {/* OVERVIEW TAB */}
          <TabsContent value="overview" className="space-y-8">
            <div>
              <h2 className="text-neutral-900 mb-2">System Overview</h2>
              <p className="text-neutral-500">Monitor all deliveries and system activity</p>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm text-neutral-500 flex items-center gap-2">
                    <Package className="size-4" />
                    Total Orders
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl text-neutral-900">{stats.totalOrders}</div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm text-neutral-500 flex items-center gap-2">
                    <Truck className="size-4" />
                    Active Deliveries
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl text-neutral-900">{stats.activeDeliveries}</div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm text-neutral-500 flex items-center gap-2">
                    <CheckCircle className="size-4" />
                    Delivered
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl text-neutral-900">{stats.delivered}</div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm text-neutral-500 flex items-center gap-2">
                    <Users className="size-4" />
                    Total Users
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl text-neutral-900">{stats.totalClients + stats.totalLivreurs}</div>
                </CardContent>
              </Card>
            </div>

            {/* Active Deliveries */}
            <Card>
              <CardHeader>
                <CardTitle>Active Deliveries</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {activeOrders.length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No active deliveries</p>
                    </div>
                  ) : (
                    activeOrders.map(order => (
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
                        
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 text-sm">
                          <div>
                            <p className="text-neutral-500">Sender</p>
                            <p className="text-neutral-900">{order.emetteurName}</p>
                          </div>
                          <div>
                            <p className="text-neutral-500">Receiver</p>
                            <p className="text-neutral-900">{order.recepteurName}</p>
                          </div>
                          <div>
                            <p className="text-neutral-500">Delivery Person</p>
                            <p className="text-neutral-900">{order.livreurName || 'Not assigned'}</p>
                          </div>
                          <div>
                            <p className="text-neutral-500">Updated</p>
                            <p className="text-neutral-900">{order.updatedAt.toLocaleTimeString()}</p>
                          </div>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Completed Orders */}
            <Card>
              <CardHeader>
                <CardTitle>Recent Completed Orders</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {completedOrders.length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No completed orders yet</p>
                    </div>
                  ) : (
                    completedOrders.slice(0, 5).map(order => (
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
                        
                        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 text-sm">
                          <div>
                            <p className="text-neutral-500">Sender</p>
                            <p className="text-neutral-900">{order.emetteurName}</p>
                          </div>
                          <div>
                            <p className="text-neutral-500">Receiver</p>
                            <p className="text-neutral-900">{order.recepteurName}</p>
                          </div>
                          <div>
                            <p className="text-neutral-500">Delivery Person</p>
                            <p className="text-neutral-900">{order.livreurName || '-'}</p>
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

          {/* USERS TAB */}
          <TabsContent value="users" className="space-y-8">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-neutral-900 mb-2">User Management</h2>
                <p className="text-neutral-500">Approve and manage clients and delivery persons</p>
              </div>
              <Button onClick={() => setShowAddUserForm(!showAddUserForm)}>
                <Plus className="size-4 mr-2" />
                Add User
              </Button>
            </div>

            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm text-neutral-500 flex items-center gap-2">
                    <Users className="size-4" />
                    Total Clients
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl text-neutral-900">{stats.totalClients}</div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="pb-3">
                  <CardTitle className="text-sm text-neutral-500 flex items-center gap-2">
                    <Truck className="size-4" />
                    Total Delivery Persons
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-3xl text-neutral-900">{stats.totalLivreurs}</div>
                </CardContent>
              </Card>
            </div>

            {/* Add User Form */}
            {showAddUserForm && (
              <Card>
                <CardHeader>
                  <CardTitle>Add New User</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="role">User Type</Label>
                    <select
                      id="role"
                      className="flex h-10 w-full rounded-md border border-neutral-200 bg-white px-3 py-2 text-sm"
                      value={newUser.role}
                      onChange={(e) => setNewUser({ ...newUser, role: e.target.value as 'client' | 'livreur' })}
                    >
                      <option value="client">Client</option>
                      <option value="livreur">Delivery Person</option>
                    </select>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="name">Full Name</Label>
                    <Input
                      id="name"
                      placeholder="Enter full name"
                      value={newUser.name}
                      onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="Enter email address"
                      value={newUser.email}
                      onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="password">Password</Label>
                    <Input
                      id="password"
                      type="password"
                      placeholder="Enter password"
                      value={newUser.password}
                      onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="phone">Phone Number</Label>
                    <Input
                      id="phone"
                      placeholder="Enter phone number"
                      value={newUser.phone}
                      onChange={(e) => setNewUser({ ...newUser, phone: e.target.value })}
                    />
                  </div>

                  {newUser.role === 'client' && (
                    <div className="space-y-2">
                      <Label htmlFor="address">Address (Optional)</Label>
                      <Input
                        id="address"
                        placeholder="Enter address"
                        value={newUser.address}
                        onChange={(e) => setNewUser({ ...newUser, address: e.target.value })}
                      />
                    </div>
                  )}

                  <div className="flex gap-2">
                    <Button onClick={handleAddUser} className="flex-1">
                      <UserCheck className="size-4 mr-2" />
                      Approve & Add User
                    </Button>
                    <Button variant="outline" onClick={() => setShowAddUserForm(false)}>
                      Cancel
                    </Button>
                  </div>
                </CardContent>
              </Card>
            )}

            {/* Clients List */}
            <Card>
              <CardHeader>
                <CardTitle>Clients</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {clients.length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No clients yet</p>
                    </div>
                  ) : (
                    clients.map(client => (
                      <div
                        key={client.id}
                        className="border border-neutral-200 rounded-lg p-4 flex items-start justify-between"
                      >
                        <div>
                          <p className="text-neutral-900">{client.name}</p>
                          <p className="text-sm text-neutral-500">{client.email}</p>
                          <p className="text-sm text-neutral-500">{client.phone}</p>
                          {client.address && (
                            <p className="text-sm text-neutral-400">{client.address}</p>
                          )}
                        </div>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleRemoveUser(client.id)}
                        >
                          <Trash2 className="size-4 mr-2" />
                          Remove
                        </Button>
                      </div>
                    ))
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Delivery Persons List */}
            <Card>
              <CardHeader>
                <CardTitle>Delivery Persons</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {livreurs.length === 0 ? (
                    <div className="text-center py-8 text-neutral-500">
                      <p>No delivery persons yet</p>
                    </div>
                  ) : (
                    livreurs.map(livreur => (
                      <div
                        key={livreur.id}
                        className="border border-neutral-200 rounded-lg p-4 flex items-start justify-between"
                      >
                        <div>
                          <p className="text-neutral-900">{livreur.name}</p>
                          <p className="text-sm text-neutral-500">{livreur.email}</p>
                          <p className="text-sm text-neutral-500">{livreur.phone}</p>
                        </div>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleRemoveUser(livreur.id)}
                        >
                          <Trash2 className="size-4 mr-2" />
                          Remove
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