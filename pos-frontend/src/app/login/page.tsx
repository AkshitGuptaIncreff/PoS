'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import { authService } from '@/services/auth.service';
import { SessionData, UserRole } from '@/types/auth';

const loginSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
    const router = useRouter();
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (data: LoginFormValues) => {
        try {
            const response = await authService.login(data);

            const sessionData: SessionData = {
                sessionId: response.sessionId,
                email: response.email,
                role: response.role as UserRole,
                lastCheckTime: Date.now(),
            };

            sessionStorage.setItem('pos_session', JSON.stringify(sessionData));
            toast.success('Welcome back!');

            // Redirect to the core dashboard
            router.push('/products');
        } catch (error: any) {
            toast.error(error.response?.data?.message || 'Authentication failed');
        }
    };

    return (
        <div className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
            <div className="w-full max-w-md space-y-6 rounded-xl bg-white p-8 shadow-md">
                <h2 className="text-center text-3xl font-bold tracking-tight text-gray-900">PoS Terminal Sign In</h2>
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">Email</label>
                        <input
                            type="email"
                            {...register('email')}
                            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-indigo-500"
                        />
                        {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700">Password</label>
                        <input
                            type="password"
                            {...register('password')}
                            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-indigo-500"
                        />
                        {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full rounded-md bg-indigo-600 px-4 py-2 font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400"
                    >
                        {isSubmitting ? 'Verifying...' : 'Sign In'}
                    </button>
                </form>
                <div className="text-center text-xs text-gray-500 border-t border-gray-100 pt-4 space-y-1">
                    <p>Don&apos;t have an account?{' '}
                        <Link href="/signup" className="font-semibold text-indigo-600 hover:text-indigo-500 hover:underline">
                            Create one
                        </Link>
                    </p>
                    <p>
                        <Link href="/" className="text-gray-400 hover:text-gray-600 hover:underline">
                            Back to home
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}