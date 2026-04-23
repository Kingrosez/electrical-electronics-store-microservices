import type { ReactNode } from "react";

const MainLayout = ({children}:{children:ReactNode})=>{
return(
    <>
    <div>
        <header className="p-4 bg-gray-800 text-white">Header</header>
        <main>{children}</main>
    </div>
    </>
);
}

export default MainLayout;