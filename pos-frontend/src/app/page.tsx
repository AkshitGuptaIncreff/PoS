import Link from 'next/link';
import { LogIn, UserPlus, Store } from 'lucide-react';

export default function LandingPage() {
    return (
        <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 px-4">
            <div className="w-full max-w-md space-y-8 text-center">
                <div className="space-y-3">
                    <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-indigo-600/20 ring-1 ring-indigo-500/30">
                        <Store className="h-8 w-8 text-indigo-400" />
                    </div>
                    <h1 className="text-3xl font-bold tracking-tight text-white">Point of Sale</h1>
                    <p className="text-sm text-slate-400 max-w-sm mx-auto">
                        Retail management terminal. Sign in to manage products, orders, clients, and reports.
                    </p>
                </div>

                <div className="space-y-3 pt-4">
                    <Link
                        href="/login"
                        className="flex items-center justify-center gap-2 w-full rounded-xl bg-indigo-600 px-4 py-3 text-sm font-semibold text-white hover:bg-indigo-500 transition-colors shadow-lg shadow-indigo-600/25"
                    >
                        <LogIn className="h-4 w-4" />
                        Sign In
                    </Link>
                    <Link
                        href="/signup"
                        className="flex items-center justify-center gap-2 w-full rounded-xl bg-white/10 px-4 py-3 text-sm font-semibold text-white hover:bg-white/15 transition-colors ring-1 ring-white/20"
                    >
                        <UserPlus className="h-4 w-4" />
                        Create Account
                    </Link>
                </div>
            </div>
        </div>
    );
}
