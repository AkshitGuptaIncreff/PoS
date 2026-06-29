import { useAuth } from '@/hooks/useAuth';
import { UserRole } from '@/types/auth';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

interface RoleGuardProps {
    allowedRoles: UserRole[];
    children: React.ReactNode;
}

export function RoleGuard({ allowedRoles, children }: RoleGuardProps) {
    const { user, loading } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!loading && (!user || !allowedRoles.includes(user.role))) {
            router.push('/login'); // Or a dedicated 403 Forbidden page
        }
    }, [user, loading, allowedRoles, router]);

    if (loading) return <div className="flex h-screen items-center justify-center">Loading PoS...</div>;

    return user && allowedRoles.includes(user.role) ? <>{children}</> : null;
}