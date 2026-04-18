import { createContext, useContext, useState, type ReactNode } from "react";

type AuthContextType={
    user: any;
    setUser:(user:any) => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({children}:{children:ReactNode}){
    const [user, setUser]=useState<any>(null);

    return(
        <AuthContext.Provider value={{user,setUser}}>{children}</AuthContext.Provider>
    )
}
export function useAuth(){
    const context = useContext(AuthContext);
    if(!context)throw new Error ("useAuth must be used within AuthProvider");
    return context;
}