import { useState } from 'react';
import { Card , CardContent , CardHeader ,CardTitle } from "@/components/ui/card";
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Tabs , TabsContent , TabsList , TabsTrigger } from './ui/tabs';
import { StatusBadge } from './StatusBadge'
import { Package , Truck , CheckCircle , Logout,Users,Plus,Trash2,UserCheck} from 'lucide-react'
import {Orders , User} from '../types'

interface AuthUser{
    id:string;
    name:string;
    email:string;
    type:string;
}

interface AdminDashProps{
    user:AuthUser;
    onLogout:()=>void;
}

export function AdminDash({user , onLgout}:AdminDashProps){
    const [orders] = useState<Order[]>(mockOrders);
    const [users , setUsers] = useState<User[]>(mockUsers);
    const [showAddUserFrom , setShowAddFrom] = useState(false);
    const [newUser , setNewUser] = useState({
        name:'',
        email:'',
        password:'',
        phone:'',
        address:'',
        role:'client' as 'client' | 'livreur',
    });


    
}