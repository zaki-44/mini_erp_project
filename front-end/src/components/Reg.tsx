import { BrowserRouter, Route, Routes } from "react-router-dom";
import { LoginForm } from "./login-register/login-form";
import { SignupForm } from "./login-register/signup-form";
import {motion} from 'motion/react' 

export default function Res(){


    return(
        <motion.div
            // initial={{y:-10,opacity:0,scale:0.8}}
            // animate={{y:0,opacity:1,scale:1}}
            className="grid place-content-center h-screen"
        >
            
        <Routes>
            <Route path="" element={<LoginForm className="w-[40vw]"/>} />
            <Route path="/login" element={<LoginForm className="w-[40vw]"/>} />
            <Route path="/signup" element={<SignupForm className="w-[40vw]"/>} />
        </Routes>
            

        </motion.div>
    )
}