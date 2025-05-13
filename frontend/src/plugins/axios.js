// Axios
import axios from 'axios'
import router from "@/router"
import {useUserStore} from '@/plugins/store'

// Configuration
axios.defaults.baseURL = process.env.NODE_ENV === 'development' ? 'http://localhost:8081' : '/api'
axios.defaults.withCredentials = process.env.NODE_ENV === 'development'
console.log(`Using API URL: ${axios.defaults.baseURL}`)

// Update login secret when user is set

// Logout if login secret is revoked
axios.interceptors.response.use(undefined, (error) => {
    if (error) {
        const originalRequest = error.config;
        if (error?.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            let store = useUserStore()
            store.logout()
            return router.go()
        }
        throw error // rethrow
    }
})

export default axios
