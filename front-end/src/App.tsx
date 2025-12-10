import './index.css'
import Res from './components/Reg'
import { BrowserRouter } from 'react-router-dom'



export default function App(){
    return (
        <BrowserRouter>
            <div className='h-screen w-screen'>
                <Res />
            </div>
        </BrowserRouter>
        
    )
}