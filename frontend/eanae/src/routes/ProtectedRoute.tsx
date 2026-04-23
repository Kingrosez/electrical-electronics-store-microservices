import { Navigate, Outlet } from "react-router-dom";

const ProtectedRoute = ()=>{
    const isAunthenticate = false;
return(
isAunthenticate ? <Outlet/> : <Navigate to={"/"}/>
);
}

export default ProtectedRoute;