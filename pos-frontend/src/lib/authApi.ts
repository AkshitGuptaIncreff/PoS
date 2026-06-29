
import axios from 'axios';

export const authApi = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8040',
    headers: {
        'Content-Type': 'application/json'
    }
});