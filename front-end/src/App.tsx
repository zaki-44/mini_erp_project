import { useState } from 'react';
import { LoginPage } from './components/LoginPage';
import { RegisterPage } from './components/RegisterPage';
import { EmailVerificationPage } from './components/EmailVerificationPage';
import type { RegisterData } from './components/RegisterPage';
import { AdminDashboard } from './components/AdminDashboard';
import { ClientDashboard } from './components/ClientDashboard';
import { LivreurDashboard } from './components/LivreurDashboard';
import { register, logout as apiLogout } from './lib/api';

type UserType = 'admin' | 'client' | 'livreur';
type AppView = 'login' | 'register' | 'verify' | 'dashboard';

interface AuthUser {
  id: string;
  name: string;
  email: string;
  type: UserType;
}

export default function App() {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [view, setView] = useState<AppView>('login');
  const [verificationEmail, setVerificationEmail] = useState<string>('');

  const handleLogin = (userData: AuthUser) => {
    setUser(userData);
    setView('dashboard');
  };

  const handleLogout = async () => {
    try {
      await apiLogout();
      setUser(null);
      setView('login');
    } catch (error) {
      // Even if logout fails on server, clear local state
      setUser(null);
      setView('login');
      console.error('Logout error:', error);
    }
  };

  const handleRegister = async (registerData: RegisterData) => {
    const response = await register(registerData);
    // Registration successful, will redirect to verification via RegisterPage
    return response;
  };

  const handleShowRegister = () => {
    setView('register');
  };

  const handleBackToLogin = () => {
    setView('login');
  };

  const handleVerification = (email: string) => {
    setVerificationEmail(email);
    setView('verify');
  };

  const handleVerificationSuccess = () => {
    setView('login');
    setVerificationEmail('');
  };

  if (view === 'register') {
    return <RegisterPage onRegister={handleRegister} onBackToLogin={handleBackToLogin} onVerification={handleVerification} />;
  }

  if (view === 'verify') {
    return <EmailVerificationPage email={verificationEmail} onVerificationSuccess={handleVerificationSuccess} onBackToLogin={handleBackToLogin} />;
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