import type { ReactNode } from "react"
import clsx from "clsx"
type CardProps={
    children:ReactNode;
    className?:string;
    padding?:"sm"|"md"|"lg"
    shadow?:"none"|"sm"|"md"
};

export default function Card({
    children,
    className,
    padding="md",
    shadow="sm",
}:CardProps){
    const base="rounded-2xl bg-white border transition";

    const paddingStyles={
        sm:"p-3",
        md:"p-4",
        lg:"p-6"
    };

     const shadowStyles={
        none:"",
        sm:"shadow-sm",
        md:"shadow-mg",
    };

    return(
        <>
        <div className={clsx(
        base,
        paddingStyles[padding],
        shadowStyles[shadow],
        className)}
        style={{borderColor:"var(--color-border)",}}
        >{children}</div>
        </>
    )
}