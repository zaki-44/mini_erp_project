import { useState } from 'react';
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import {
  Field,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Loader2 } from "lucide-react"

// Updated to match your EXACT lowercase requirements
export interface RegisterData {
  firstname: string;
  lastname: string;
  username: string; // Added
  email: string;
  password: string;
  phonenumber: string;
  role: 'CLIENT' | 'DELIVERER';
  city: string;     // Added (Required for both)
  
  // Client specific
  address?: string; 
  postalcode?: string; // Added
  
  // Deliverer specific
  vehicletype?: 'BIKE' | 'CAR' | 'TRUCK'; // Updated options
  maxweight?: number; // Added
  serialnumber?: string;
}

interface RegisterPageProps {
  onRegister: (userData: RegisterData) => Promise<any>;
  onBackToLogin: () => void;
  onVerification: (email: string) => void;
  className?: string;
}

export function RegisterPage({ onRegister, onBackToLogin, onVerification, className }: RegisterPageProps) {
  const [formData, setFormData] = useState<RegisterData>({
    firstname: '',
    lastname: '',
    username: '',
    email: '',
    password: '',
    phonenumber: '',
    role: 'CLIENT',
    city: '',
    address: '',
    postalcode: '',
    vehicletype: 'BIKE',
    maxweight: 0,
    serialnumber: ''
  });

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSelectChange = (name: string, value: string) => {
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      // Create payload based on Role requirements
      const payload: RegisterData = {
        firstname: formData.firstname,
        lastname: formData.lastname,
        username: formData.username,
        email: formData.email,
        password: formData.password,
        phonenumber: formData.phonenumber,
        role: formData.role,
        city: formData.city,
      };

      if (formData.role === 'CLIENT') {
        payload.address = formData.address;
        payload.postalcode = formData.postalcode;
      } else {
        payload.vehicletype = formData.vehicletype;
        payload.maxweight = Number(formData.maxweight); // Ensure number
        payload.serialnumber = formData.serialnumber;
      }

      await onRegister(payload);
      onVerification(formData.email);
    } catch (err: any) {
      console.error('Registration failed:', err);
      setError(err.message || 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={cn("flex min-h-screen items-center justify-center p-4 bg-muted/40", className)}>
      <div className="w-full max-w-xl space-y-4">
        <div className="text-center space-y-2">
          <h1 className="text-3xl font-bold tracking-tighter">Create Account</h1>
          <p className="text-muted-foreground">Join our delivery network</p>
        </div>

        <Card>
          <CardContent className="pt-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <FieldGroup>
                <div className="grid grid-cols-2 gap-4">
                  <Field>
                    <FieldLabel>First Name</FieldLabel>
                    <Input name="firstname" value={formData.firstname} onChange={handleChange} required />
                  </Field>
                  <Field>
                    <FieldLabel>Last Name</FieldLabel>
                    <Input name="lastname" value={formData.lastname} onChange={handleChange} required />
                  </Field>
                </div>

                <Field>
                  <FieldLabel>Username</FieldLabel>
                  <Input name="username" value={formData.username} onChange={handleChange} required placeholder="johndoe123" />
                </Field>

                <div className="grid grid-cols-2 gap-4">
                    <Field>
                        <FieldLabel>Email</FieldLabel>
                        <Input type="email" name="email" value={formData.email} onChange={handleChange} required />
                    </Field>
                    <Field>
                        <FieldLabel>Phone Number</FieldLabel>
                        <Input type="tel" name="phonenumber" value={formData.phonenumber} onChange={handleChange} required />
                    </Field>
                </div>

                <Field>
                  <FieldLabel>Password</FieldLabel>
                  <Input type="password" name="password" value={formData.password} onChange={handleChange} required />
                </Field>

                <div className="grid grid-cols-2 gap-4">
                    <Field>
                    <FieldLabel>I am a...</FieldLabel>
                    <Select value={formData.role} onValueChange={(val) => handleSelectChange('role', val)}>
                        <SelectTrigger>
                        <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                        <SelectItem value="CLIENT">Client (Sender)</SelectItem>
                        <SelectItem value="DELIVERER">Deliverer (Driver)</SelectItem>
                        </SelectContent>
                    </Select>
                    </Field>
                    <Field>
                        <FieldLabel>City</FieldLabel>
                        <Input name="city" value={formData.city} onChange={handleChange} required />
                    </Field>
                </div>

                {/* CLIENT FIELDS */}
                {formData.role === 'CLIENT' ? (
                  <div className="grid grid-cols-2 gap-4">
                    <Field>
                        <FieldLabel>Address</FieldLabel>
                        <Input name="address" value={formData.address} onChange={handleChange} required placeholder="123 Main St" />
                    </Field>
                    <Field>
                        <FieldLabel>Postal Code</FieldLabel>
                        <Input name="postalcode" value={formData.postalcode} onChange={handleChange} required type="number" />
                    </Field>
                  </div>
                ) : (
                  // DELIVERER FIELDS
                  <div className="space-y-4 border-t pt-4 mt-2">
                    <p className="text-sm font-medium text-muted-foreground">Vehicle Details</p>
                    <div className="grid grid-cols-3 gap-4">
                        <Field>
                        <FieldLabel>Vehicle Type</FieldLabel>
                        <Select value={formData.vehicletype} onValueChange={(val) => handleSelectChange('vehicletype', val)}>
                            <SelectTrigger>
                            <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                            <SelectItem value="BIKE">Bike</SelectItem>
                            <SelectItem value="CAR">Car</SelectItem>
                            <SelectItem value="TRUCK">Truck</SelectItem>
                            </SelectContent>
                        </Select>
                        </Field>
                        <Field>
                        <FieldLabel>Max Weight (kg)</FieldLabel>
                        <Input name="maxweight" type="number" value={formData.maxweight} onChange={handleChange} required />
                        </Field>
                        <Field>
                        <FieldLabel>Serial Number</FieldLabel>
                        <Input name="serialnumber" value={formData.serialnumber} onChange={handleChange} required />
                        </Field>
                    </div>
                  </div>
                )}

                {error && <div className="text-sm font-medium text-destructive">{error}</div>}

                <Button type="submit" className="w-full" disabled={isLoading}>
                  {isLoading ? (
                    <> <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Creating account... </>
                  ) : ( "Create Account" )}
                </Button>

                <div className="text-center text-sm text-muted-foreground pt-2">
                  Already have an account?{' '}
                  <button type="button" onClick={onBackToLogin} className="underline font-medium text-primary hover:text-primary/90">
                    Sign in
                  </button>
                </div>
              </FieldGroup>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}