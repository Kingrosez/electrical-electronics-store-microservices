import axiosClient from "../axiosClient"

export const getProduct = async ()=>{
    return axiosClient.get("/products");
};