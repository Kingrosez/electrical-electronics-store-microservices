import {BrowserRouter, Routes, Route} from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import Home from "../pages/Home";
import NotFound from "../pages/NotFound";

export default function AppRoutes(){
    return(
        <BrowserRouter>
        <Routes>
            <Route path="/" element={<Home/>}/>
            <Route element={<ProtectedRoute/>}></Route>

            <Route path="*" element={<NotFound/>}/>
        </Routes>
        </BrowserRouter>

    );
}