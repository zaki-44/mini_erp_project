import './index.css'
import { useState } from 'react'



type UserType = 'admin' | 'client' | 'livreur'
type AppView = 'login' | 'register' | 'dashboard'

interface AuthUser{
    id: string ;
    name : string;
    email:string;
    type:UserType;
}

export default function App(){
    const [user,setUser] = useState<AuthUser | null>(null)
    const [view,setView] = useState<AppView>('login')

    const handleLogin = (userData : AuthUser) =>{
        setUser(userData)
        setView('dashboard')
    }

    const handleLogout = () => {
        setUser(null)
        setView('login')
    }

    const handleRegister = (registerData : RegisterData) =>{
        //api call to create a user
        // if ok , wait for admin approval
    }

    const handleShowRegister = () =>{
        setView('register')
    }
    const handleBackToLogin = () =>{
        setView('login')
    }

    if(view === 'register'){
        return <RegisterPage onRegister={handleRegister} onBackToLogin={handleBackToLogin} />
    }
    if(!user || view === 'login'){
        return <LoginPage onLogin={handleLogin} onShowRegister={handleShowRegister}/>
    }

    return (
        <div className='min-h-screen bg-neutral-50'>
            {user.type === 'admin' &&<AdminDashboard user={user} onLogout={handleLogout}/>}
            {user.type === 'client' &&<ClienDashboard user={user} onLogout={handleLogout}/>}
            {user.type === 'livreur' &&<LivreurDashboard user={user} onLogout={handleLogout}/>}
        </div>
    )
}