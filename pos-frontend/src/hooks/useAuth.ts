import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { authService } from '@/services/auth.service';
import { SessionData, UserRole } from '@/types/auth';

const FIVE_MINUTES_MS = 5 * 60 * 1000;

export function useAuth() {
    const [user, setUser] = useState<SessionData | null>(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();
    const pathname = usePathname();

    useEffect(() => {
        async function checkAuth() {
            const stored = sessionStorage.getItem('pos_session');

            if (!stored) {
                handleLogout();
                return;
            }

            try {
                const session: SessionData = JSON.parse(stored);
                setUser(session);
                setLoading(false);
            } catch (error) {
                console.error("Session validation failed:", error);
                handleLogout();
            } finally {
                setLoading(false);
            }
        }

        checkAuth();
    }, [pathname]);

    const handleLogout = () => {
        sessionStorage.removeItem('pos_session');
        setUser(null);
        setLoading(false);

        // Adjusted to check the flat path layout strings
        if (pathname !== '/' && pathname !== '/login' && pathname !== '/signup') {
            router.push('/');
        }
    };

    return { user, loading, logout: handleLogout };
}