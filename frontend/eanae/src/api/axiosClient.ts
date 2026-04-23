import axios from "axios";

const axiosClient = axios.create({
    baseURL:import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    // withCredentials:true
});

axiosClient.interceptors.request.use(
    (config)=>{
        return config;
    },
    (error)=> Promise.reject(error)
);

axiosClient.interceptors.response.use(
    (response)=>response.data,
    (error)=>{
        if(error.response?.status === 401){

        }
        return Promise.reject(error)
    }
);

export default axiosClient;