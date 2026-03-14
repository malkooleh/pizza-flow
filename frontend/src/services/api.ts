import axios from 'axios';

export const api = axios.create({
    baseURL: '/api/', // Proxied by Nginx/Vite to Gateway
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use((config) => {
    // Token injection will be handled here (integration with AuthContext)
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => {
    return Promise.reject(error);
});

api.interceptors.response.use((response) => {
    return response;
}, (error) => {
    if (error.response?.status === 401) {
        // Handle unauthorized access (redirect to login)
        window.location.href = '/login';
    }
    return Promise.reject(error);
});
