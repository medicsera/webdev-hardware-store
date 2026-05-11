import axios from 'axios';

const api = axios.create({
    baseURL: process.env.VUE_APP_API_URL || 'http://localhost:8080',
});

// Store token in localStorage after login
export function setAuthToken(token) {
    localStorage.setItem('jwt', token);
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
}

// Load token on app start
const saved = localStorage.getItem('jwt');
if (saved) {
    api.defaults.headers.common['Authorization'] = `Bearer ${saved}`;
}

// Simple login helper
export async function login(username, password) {
    const { data } = await api.post('/auth/login', { username, password });
    setAuthToken(data.token);
    return data;
}

export default api;