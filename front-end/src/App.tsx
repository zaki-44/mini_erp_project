import { useState } from 'react';
import { LoginPage } from './components/LoginPage';
import { RegisterPage } from './components/RegisterPage';
import type { RegisterData } from './components/RegisterPage';
import { AdminDashboard } from './components/AdminDashboard';
import { ClientDashboard } from './components/ClientDashboard';
import { LivreurDashboard } from './components/LivreurDashboard';

type UserType = 'admin' | 'client' | 'livreur';
type AppView = 'login' | 'register' | 'dashboard';

interface AuthUser {
  id: string;
  name: string;
  email: string;
  type: UserType;
}

export default function App() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [view, setView] = useState<AppView>('login');

  const handleLogin = (userData: AuthUser) => {
    setUser(userData);
    setView('dashboard');
  };

  const handleLogout = () => {
    setUser(null);
    setView('login');
  };

  const handleRegister = (registerData: RegisterData) => {
    // In a real app, this would make an API call to create the user
    console.log('Registration data:', registerData);
    // After successful registration, user would need admin approval
  };

  const handleShowRegister = () => {
    setView('register');
  };

  const handleBackToLogin = () => {
    setView('login');
  };

  if (view === 'register') {
    return <RegisterPage onRegister={handleRegister} onBackToLogin={handleBackToLogin} />;
  }

  if (!user || view === 'login') {
    return <LoginPage onLogin={handleLogin} onShowRegister={handleShowRegister} />;
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      {user.type === 'admin' && <AdminDashboard user={user} onLogout={handleLogout} />}
      {user.type === 'client' && <ClientDashboard user={user} onLogout={handleLogout} />}
      {user.type === 'livreur' && <LivreurDashboard user={user} onLogout={handleLogout} />}
    </div>
  );
}