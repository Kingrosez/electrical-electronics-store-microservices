
import axiosClient from "../axiosClient";

export const registerUser = async (data:{
    name:string
    email:string
    password:string
    phone:string
})=>{
    return axiosClient.post("/v1/users/register",data);
};