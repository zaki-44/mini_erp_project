import { useState } from 'react';
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Field,
  FieldDescription,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { login } from "@/lib/api"
import { Loader2, ShieldCheck, User, Truck } from "lucide-react"

// 1. UPDATE: Match the interface exactly with App.tsx
export interface AuthUser {
  id: number;          // Changed string -> number
  role: 'ADMIN' | 'CLIENT' | 'DELIVERER'; // Uppercase roles
  name?: string;       // Optional
  email?: string;      // Optional
}

interface LoginPageProps {
  onLogin: (user: AuthUser) => void;
  onShowRegister: () => void;
  className?: string;
}

export function LoginPage({ onLogin, onShowRegister, className }: LoginPageProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      // 2. UPDATE: Call the real API
      const response = await login(email, password);
      
      // 3. UPDATE: Map API response to AuthUser object
      // The API returns { userId, role }, so we map it here
      const user: AuthUser = {
        id: response.userId,
        role: response.role,
        email: email, // We keep the email the user typed for context
      };

      onLogin(user);
    } catch (err: any) {
      console.error('Login failed:', err);
      setError(err.message || 'Invalid email or password');
    } finally {
      setIsLoading(false);
    }
  };

  // 4. UPDATE: Quick Login Helpers for testing
  // These now simulate the correct API structure
  const handleQuickLogin = (role: 'ADMIN' | 'CLIENT' | 'DELIVERER') => {
    const mockUser: AuthUser = {
      id: 999,
      role: role,
      name: `Demo ${role}`,
      email: `demo.${role.toLowerCase()}@example.com`
    };
    onLogin(mockUser);
  };

  return (
    <div className={cn("flex min-h-screen items-center justify-center p-4 bg-muted/40", className)}>
      <div className="w-full max-w-md space-y-4">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-bold tracking-tighter">Welcome Back</h1>
          <p className="text-muted-foreground">Sign in to Livrili</p>
        </div>

        <Card>
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <FieldGroup>
                <Field>
                  <FieldLabel>Email</FieldLabel>
                  <Input 
                    type="email" 
                    placeholder="name@example.com" 
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </Field>
                <Field>
                  <FieldLabel>Password</FieldLabel>
                  <Input 
                    type="password" 
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </Field>
                
                {error && (
                  <div className="text-sm font-medium text-destructive">{error}</div>
                )}

                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Signing in...
                    </>
                  ) : (
                    "Sign In"
                  )}
                </Button>

                <div className="text-center text-sm text-muted-foreground pt-2">
                  Don't have an account?{' '}
                  <button 
                    type="button"
                    onClick={onShowRegister}
                    className="underline font-medium text-primary hover:text-primary/90"
                  >
                    Sign up
                  </button>
                </div>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>

        {/* Quick Login Buttons for Demo/Testing */}
        {/* <div className="grid grid-cols-3 gap-2">
          <Button variant="outline" size="sm" onClick={() => handleQuickLogin('ADMIN')} className="flex flex-col h-auto py-2 gap-1">
            <ShieldCheck className="h-4 w-4" />
            <span className="text-[10px]">Admin</span>
          </Button>
          <Button variant="outline" size="sm" onClick={() => handleQuickLogin('CLIENT')} className="flex flex-col h-auto py-2 gap-1">
            <User className="h-4 w-4" />
            <span className="text-[10px]">Client</span>
          </Button>
          <Button variant="outline" size="sm" onClick={() => handleQuickLogin('DELIVERER')} className="flex flex-col h-auto py-2 gap-1">
            <Truck className="h-4 w-4" />
            <span className="text-[10px]">Driver</span>
          </Button>
        </div> */}
      </div>
    </div>
  )
}