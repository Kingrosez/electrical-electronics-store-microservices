import { useQuery } from "@tanstack/react-query"
import { getProduct } from "../api/services/productService"

export const useProducts=()=>{
    return useQuery({
        queryKey:["products"],
        queryFn:getProduct,
    })
}