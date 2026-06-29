import { api } from '@/lib/api';
import { authApi } from '@/lib/authApi';

// ⚡ Matches your Java AuthForm properties exactly
export interface AuthForm {
    email: string;
    password?: string;
}

// ⚡ Strong data type contract mirroring your backend response data
export interface AuthData {
    email: string;
    role: string;
    sessionId: string;
}

// ⚡ Explicit session validation contract response structure
export interface ValidateResponse {
    email: string;
    role: string;
    sessionId: string;
}

export const authService = {

    async validateSession() {
        const res = await authApi.post('/auth/validate', {}, {
            headers: {
                sessionId: JSON.parse(
                    sessionStorage.getItem('pos_session')!
                ).sessionId
            }
        });

        return res.data;
    },
    // 1. User Workspace Login Sequence Route
    async login(credentials: AuthForm): Promise<AuthData> {
        // Hits: POST http://localhost:8040/auth/login
        const res = await api.post('/auth/login', credentials);
        return res.data;
    },

    // 2. User Account Creation Sequence Route (Automatically assigns roles on backend service)
    async signup(form: AuthForm): Promise<AuthData> {
        // Hits: POST http://localhost:8040/auth/signup
        const res = await api.post('/auth/signup', form);
        return res.data;
    },
};