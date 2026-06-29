import axios from 'axios';
import { authService } from '@/services/auth.service';

const FIVE_MINUTES_MS = 5 * 60 * 1000;

export const api = axios.create({
    // Updated port to 8040 to match your running backend
    baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8040',
    headers: {
        'Content-Type': 'application/json',
    },
});

let validationPromise: Promise<void> | null = null;
api.interceptors.request.use(async (config) => {
    if (typeof window === 'undefined') {
        return config;
    }

    const sessionRaw = sessionStorage.getItem('pos_session');
    if (!sessionRaw) {
        return config;
    }

    const session = JSON.parse(sessionRaw);
    const now = Date.now();

    // Refresh session every 5 minutes
    if (now - session.lastCheckTime >= FIVE_MINUTES_MS) {
        // Prevent multiple parallel validation calls
        if (!validationPromise) {
            validationPromise = (async () => {
                const verified = await authService.validateSession();
                const updated = {
                    ...session,
                    email: verified.email,
                    role: verified.role,
                    lastCheckTime: Date.now()
                };
                sessionStorage.setItem(
                    'pos_session',
                    JSON.stringify(updated)
                );
            })().finally(() => {
                validationPromise = null;
            });
        }

        await validationPromise;
    }

    const latestSession = JSON.parse(
        sessionStorage.getItem('pos_session')!
    );

    config.headers['sessionId'] = latestSession.sessionId;

    return config;
});

const SESSION_ERRORS = ['Session expired', 'Invalid session', 'Missing Session'];

api.interceptors.response.use(
    (response) => {
        if (typeof window !== 'undefined') {
            const newSessionId = response.headers['sessionid'];
            if (newSessionId) {
                const sessionRaw = sessionStorage.getItem('pos_session');
                if (sessionRaw) {
                    try {
                        const session = JSON.parse(sessionRaw);
                        session.sessionId = newSessionId;
                        sessionStorage.setItem('pos_session', JSON.stringify(session));
                    } catch { /* ignore parse errors */ }
                }
            }
        }
        return response;
    },
    (error) => {
        if (typeof window !== 'undefined') {
            const message = error?.response?.data?.message;
            if (message && SESSION_ERRORS.some((e) => message.includes(e))) {
                sessionStorage.removeItem('pos_session');
                window.location.href = '/';
            }
        }
        return Promise.reject(error);
    }
);