import type { AxiosInstance } from 'axios';
import axios from 'axios';

const api: AxiosInstance = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
});

// Inject Authorization on every request — works for JSON, FormData, and any content type
api.interceptors.request.use(config => {
    const token = localStorage.getItem('auth_token');
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
});

export function setAuthToken(token: string): void {
    localStorage.setItem('auth_token', token);
}

export function loadStoredToken(): void {
    // No-op: interceptor reads from localStorage on every request
}

export async function login(username: string, password: string): Promise<string> {
    const { data } = await api.post<{ token: string }>('/auth/login', {
        username,
        password,
    });
    setAuthToken(data.token);
    return data.token;
}

export async function register(username: string, password: string): Promise<string> {
    const { data } = await api.post<{ token: string }>('/auth/register', {
        username,
        password,
    });
    setAuthToken(data.token);
    return data.token;
}

export function logout(): void {
    localStorage.removeItem('jwt');
}

export default api;
