'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/auth.service';
import { toast } from 'sonner';
import { UserPlus, Loader2, ShieldCheck, Mail, Lock } from 'lucide-react';
import Link from 'next/link';

export default function SignUpPage() {
    const router = useRouter();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [submitting, setSubmitting] = useState(false);

    const handleSignUpSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!email.trim() || !password.trim()) {
            toast.error('Please input a valid email address and password.');
            return;
        }

        setSubmitting(true);
        try {
            // ⚡ Only sending email and password to match your custom AuthForm backend object
            const result = await authService.signup({
                email: email.trim(),
                password: password
            });

            // Show the exact authorization layer assigned natively by your backend rulesets
            toast.success(`Account successfully provisioned as ${result.role || 'OPERATOR'}!`);

            setTimeout(() => {
                router.push('/login');
            }, 2000);

        } catch (error: any) {
            const serverMsg = error.response?.data?.message || 'Failed to create network security credentials profile.';
            toast.error(serverMsg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50/50 px-4">
            <div className="w-full max-w-md space-y-6 bg-white p-8 rounded-2xl border border-gray-200 shadow-xl/5">

                {/* Upper Header Block Branding */}
                <div className="text-center space-y-2">
                    <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600">
                        <UserPlus className="h-6 w-6" />
                    </div>
                    <h2 className="text-2xl font-bold tracking-tight text-gray-900">Register Profile</h2>
                    <p className="text-sm text-gray-500">Create a terminal identity node within the regional network registry.</p>
                </div>

                {/* Input Form */}
                <form onSubmit={handleSignUpSubmit} className="space-y-4">
                    <div>
                        <label className="block text-xs font-semibold uppercase tracking-wider text-gray-500">Email Address *</label>
                        <div className="relative mt-1">
                            <input
                                type="email"
                                required
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="operator@pos.local"
                                className="block w-full rounded-lg border border-gray-300 pl-10 pr-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 bg-white text-gray-900"
                            />
                            <Mail className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                        </div>
                    </div>

                    <div>
                        <label className="block text-xs font-semibold uppercase tracking-wider text-gray-500">Secure Password *</label>
                        <div className="relative mt-1">
                            <input
                                type="password"
                                required
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                placeholder="••••••••"
                                className="block w-full rounded-lg border border-gray-300 pl-10 pr-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 bg-white text-gray-900"
                            />
                            <Lock className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
                        </div>
                    </div>

                    {/* Submitting Trigger Button */}
                    <button
                        type="submit"
                        disabled={submitting}
                        className="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400 transition-colors shadow-md mt-4"
                    >
                        {submitting ? (
                            <>
                                <Loader2 className="h-4 w-4 animate-spin" /> Provisioning Credentials...
                            </>
                        ) : (
                            <span className="flex items-center gap-1.5">
                <ShieldCheck className="h-4 w-4" /> Register Profile
              </span>
                        )}
                    </button>
                </form>

                <div className="text-center text-xs text-gray-500 border-t border-gray-100 pt-4 space-y-1">
                    <p>
                        Already have an account?{' '}
                        <Link href="/login" className="font-semibold text-indigo-600 hover:text-indigo-500 hover:underline">
                            Sign in
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