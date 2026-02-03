import { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { StatusBadge } from './StatusBadge';
import { MapPin, Package, LogOut, CheckCircle, Truck, RefreshCw, AlertTriangle, Scale, Banknote } from 'lucide-react';
import type { Order } from '../types';
import { 
  completeDelivery, 
  fetchAssignments, 
  fetchOrderById, 
  type Assignment 
} from '../lib/api';

interface AuthUser {
  id: number;
  name?: string;
  email?: string;
  role: 'CLIENT' | 'ADMIN' | 'DELIVERER';
}

interface LivreurDashboardProps {
  user: AuthUser;
  onLogout: () => void;
}

interface EnrichedAssignment extends Assignment {
  packageDetails?: Order;
}

export function LivreurDashboard({ user, onLogout }: LivreurDashboardProps) {
  const [assignments, setAssignments] = useState<EnrichedAssignment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadAssignments();
  }, []);

  const loadAssignments = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAssignments();
      
      if (!Array.isArray(data)) {
        console.warn("API did not return an array!", data);
        setAssignments([]);
        return;
      }
      
      const enrichedData = await Promise.all(
        data.map(async (asg) => {
          try {
            if (!asg.idPackage) return asg;
            const details = await fetchOrderById(asg.idPackage.toString());
            return { ...asg, packageDetails: details };
          } catch (err) {
            console.error(`Failed to load details for package ${asg.idPackage}`, err);
            return asg;
          }
        })
      );
      
      setAssignments(enrichedData);
    } catch (err: any) {
      console.error("Failed to load assignments:", err);
      setError("Failed to load assignments.");
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteDelivery = async (assignmentId: number) => {
    if (!confirm("Confirm delivery completion?")) return;
    try {
      await completeDelivery(assignmentId.toString());
      loadAssignments();
    } catch (error) {
      console.error("Failed to complete delivery", error);
      alert("Error completing delivery.");
    }
  };

  if (error) {
    return (
      <div className="p-8 text-center">
        <AlertTriangle className="h-12 w-12 text-red-500 mx-auto mb-4" />
        <h2 className="text-xl font-bold text-red-600">Something went wrong</h2>
        <p className="text-muted-foreground mb-4">{error}</p>
        <Button onClick={onLogout}>Logout</Button>
      </div>
    );
  }

  const activeDeliveries = assignments.filter(a => a.status !== 'DELIVERED' && a.status !== 'COMPLETED');
  const pastDeliveries = assignments.filter(a => a.status === 'DELIVERED' || a.status === 'COMPLETED');

  return (
    <>
      <header className="bg-white border-b sticky top-0 z-30 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-2">
          <div className="bg-primary/10 p-2 rounded-lg">
            <Truck className="h-6 w-6 text-primary" />
          </div>
          <h1 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-primary/60">
            Driver Dashboard
          </h1>
        </div>
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={loadAssignments} title="Refresh">
             <RefreshCw className="h-5 w-5 text-muted-foreground" />
          </Button>
          <div className="text-right hidden sm:block">
            <p className="text-sm font-medium">{user.name || 'Driver'}</p>
            <div className="flex items-center gap-1 justify-end text-xs text-muted-foreground">
              <span className="w-2 h-2 rounded-full bg-green-500 animate-pulse" />
              Online
            </div>
          </div>
          <Button variant="ghost" size="icon" onClick={onLogout} className="text-muted-foreground hover:text-destructive">
            <LogOut className="h-5 w-5" />
          </Button>
        </div>
      </header>

      <main className="max-w-5xl mx-auto p-6 space-y-6">
        <div className="grid grid-cols-2 gap-4">
          <Card>
            <CardContent className="p-6">
              <div className="text-sm font-medium text-muted-foreground">Active Jobs</div>
              <div className="text-2xl font-bold">{activeDeliveries.length}</div>
            </CardContent>
          </Card>
          <Card>
            <CardContent className="p-6">
              <div className="text-sm font-medium text-muted-foreground">Completed</div>
              <div className="text-2xl font-bold">{pastDeliveries.length}</div>
            </CardContent>
          </Card>
        </div>

        <Tabs defaultValue="current" className="space-y-6">
          <TabsList>
            <TabsTrigger value="current">Current Deliveries</TabsTrigger>
            <TabsTrigger value="history">History</TabsTrigger>
          </TabsList>

          <TabsContent value="current">
            <Card>
              <CardHeader>
                <CardTitle>Assigned Packages</CardTitle>
              </CardHeader>
              <CardContent>
                {loading ? (
                  <div className="p-8 text-center text-muted-foreground">Loading assignments...</div>
                ) : activeDeliveries.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-muted-foreground">
                    <Package className="h-12 w-12 mb-4 opacity-20" />
                    <p>No active deliveries assigned.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {activeDeliveries.map((item) => (
                      <div key={item.idAffectation} className="border rounded-lg p-4 bg-white shadow-sm">
                        <div className="flex justify-between items-start mb-4">
                          <div>
                            <div className="flex items-center gap-2 mb-1">
                              <span className="font-bold text-lg">Package #{item.idPackage}</span>
                              <StatusBadge status={item.status as any} />
                            </div>
                            <p className="text-xs text-muted-foreground">
                              Assigned: {new Date(item.assignedAt).toLocaleString()}
                            </p>
                          </div>
                          <Button 
                            onClick={() => handleCompleteDelivery(item.idAffectation)}
                            className="bg-green-600 hover:bg-green-700"
                          >
                            <CheckCircle className="mr-2 h-4 w-4" />
                            Complete Delivery
                          </Button>
                        </div>

                        {item.packageDetails ? (
                          <div className="grid md:grid-cols-2 gap-4 bg-neutral-50 p-4 rounded-md text-sm border">
                            {/* Destination Block */}
                            <div className="space-y-3">
                              <div className="space-y-1">
                                <span className="text-xs font-semibold uppercase text-muted-foreground flex items-center gap-1">
                                  <MapPin className="h-3 w-3" /> Source
                                </span>
                                <p className="font-medium text-neutral-900">{item.packageDetails.addressSource || 'N/A'}</p>
                              </div>
                              
                              <div className="space-y-1">
                                <span className="text-xs font-semibold uppercase text-green-600 flex items-center gap-1">
                                  <MapPin className="h-3 w-3" /> Destination
                                </span>
                                <p className="font-medium text-lg text-green-700">{item.packageDetails.addressDestination || 'N/A'}</p>
                              </div>
                            </div>

                            {/* Details Block */}
                            <div className="space-y-2 border-l pl-4 flex flex-col justify-center">
                              <div className="flex items-center justify-between">
                                <span className="text-muted-foreground flex items-center gap-2">
                                  <Scale className="h-4 w-4" /> Weight:
                                </span>
                                <span className="font-bold">{item.packageDetails.weight} kg</span>
                              </div>
                              
                              <div className="flex items-center justify-between">
                                <span className="text-muted-foreground flex items-center gap-2">
                                  <Banknote className="h-4 w-4" /> Price:
                                </span>
                                <span className="font-bold text-primary">{item.packageDetails.price} DA</span>
                              </div>

                              <div className="pt-2 mt-2 border-t text-xs text-muted-foreground">
                                <span className="font-semibold text-neutral-700">Content:</span> {item.packageDetails.description}
                              </div>
                            </div>
                          </div>
                        ) : (
                          <div className="text-sm text-yellow-600 p-2 bg-yellow-50 rounded">
                            <AlertTriangle className="h-4 w-4 inline mr-2"/>
                            Loading details...
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="history">
            <Card>
              <CardHeader>
                <CardTitle>Delivery History</CardTitle>
              </CardHeader>
              <CardContent>
                {pastDeliveries.length === 0 ? (
                  <p className="text-muted-foreground">No completed deliveries yet.</p>
                ) : (
                  <div className="space-y-4">
                     {pastDeliveries.map((item) => (
                      <div key={item.idAffectation} className="flex justify-between items-center p-4 border rounded-lg bg-neutral-50/50">
                        <div className="space-y-1">
                          <div className="flex items-center gap-2">
                            <span className="font-medium">Package #{item.idPackage}</span>
                            <StatusBadge status="DELIVERED" />
                          </div>
                          
                          <div className="grid grid-cols-2 gap-x-8 text-sm mt-2">
                            <div className="flex items-center gap-2 text-muted-foreground">
                              <MapPin className="h-3 w-3" /> 
                              <span className="truncate max-w-[200px]" title={item.packageDetails?.addressDestination}>
                                {item.packageDetails?.addressDestination || 'N/A'}
                              </span>
                            </div>
                            <div className="flex gap-4">
                              <span className="flex items-center gap-1 text-muted-foreground">
                                <Scale className="h-3 w-3" /> {item.packageDetails?.weight || '-'} kg
                              </span>
                              <span className="flex items-center gap-1 font-semibold text-neutral-900">
                                <Banknote className="h-3 w-3" /> {item.packageDetails?.price || '-'} DA
                              </span>
                            </div>
                          </div>
                          
                          <p className="text-xs text-muted-foreground mt-1">
                            Assigned: {new Date(item.assignedAt).toLocaleDateString()}
                          </p>
                        </div>
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