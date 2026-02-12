import { useState, useEffect } from 'react';
import { LoginPage } from './components/LoginPage';
import { RegisterPage } from './components/RegisterPage';
import { EmailVerificationPage } from './components/EmailVerificationPage';
import type { RegisterData } from './components/RegisterPage';
import { AdminDashboard } from './components/AdminDashboard';
import { ClientDashboard } from './components/ClientDashboard';
import { LivreurDashboard } from './components/LivreurDashboard';
import { register, logout as apiLogout } from './lib/api';

// 1. UPDATE: Match API Role names (Uppercase)
type UserType = 'ADMIN' | 'CLIENT' | 'DELIVERER'; 
type AppView = 'login' | 'register' | 'verify' | 'dashboard';

// 2. UPDATE: Match API response structure
interface AuthUser {
  id: number;          // Changed from string to number
  role: UserType;      // Renamed from 'type' to 'role' to match API
  name?: string;       // Made optional
  email?: string;      // Made optional
}

export default function App() {
  // 3. FIX: Initialize user state from Local Storage
  // This function runs only once when the page refreshes.
  const [user, setUser] = useState<AuthUser | null>(() => {
    try {
      const savedUser = localStorage.getItem('app_user');
      return savedUser ? JSON.parse(savedUser) : null;
    } catch (error) {
      console.error("Failed to parse user from storage", error);
      return null;
    }
  });

  // 4. FIX: Initialize view based on whether user exists
  const [view, setView] = useState<AppView>(() => {
    return localStorage.getItem('app_user') ? 'dashboard' : 'login';
  });

  const [verificationEmail, setVerificationEmail] = useState<string>('');

  const handleLogin = (userData: AuthUser) => {
    // 5. FIX: Save user to Local Storage on login
    localStorage.setItem('app_user', JSON.stringify(userData));
    setUser(userData);
    setView('dashboard');
  };

  const handleLogout = async () => {
    try {
      await apiLogout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // 6. FIX: Remove user from Local Storage on logout
      localStorage.removeItem('app_user');
      setUser(null);
      setView('login');
    }
  };

  const handleRegister = async (registerData: RegisterData) => {
    const response = await register(registerData);
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
      {/* 7. UPDATE: Render Dashboard based on Role */}
      {user.role === 'ADMIN' && <AdminDashboard user={user} onLogout={handleLogout} />}
      {user.role === 'CLIENT' && <ClientDashboard user={user} onLogout={handleLogout} />}
      {user.role === 'DELIVERER' && <LivreurDashboard user={user} onLogout={handleLogout} />}
    </div>
  );
}