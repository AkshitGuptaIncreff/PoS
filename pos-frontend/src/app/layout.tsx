'use client';

import { useEffect, useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import Link from 'next/link';
import { LayoutDashboard, Users, Box, ShoppingCart, ClipboardList, Loader2 } from 'lucide-react';
import { Toaster } from 'sonner';
import './globals.css';

export default function RootLayout({ children }: { children: React.ReactNode }) {
    const pathname = usePathname();
    const router = useRouter();
    const [authChecked, setAuthChecked] = useState(false);

    const isAuthPage = pathname === '/' || pathname === '/login' || pathname === '/signup';

    useEffect(() => {
        if (!isAuthPage) {
            const stored = sessionStorage.getItem('pos_session');
            if (!stored) {
                router.replace('/');
            } else {
                try {
                    const session = JSON.parse(stored);
                    if (!session?.sessionId) {
                        router.replace('/');
                    }
                } catch {
                    router.replace('/');
                }
            }
            setAuthChecked(true);
        }
    }, [pathname, isAuthPage, router]);

    // 2. The structural list of links to all your components
    const menuItems = [
        { name: 'View Products', href: '/products', icon: Box },
        { name: 'Client Management', href: '/clients', icon: Users },
        { name: 'Create Order', href: '/orders/create', icon: ShoppingCart },
        { name: 'Order Manager', href: '/orders/manage', icon: ClipboardList },
        { name: 'Sales Report', href: '/reports/sales', icon: LayoutDashboard },
        { name: 'Day Sales Report', href: '/reports/day-sales', icon: LayoutDashboard },
    ];

    return (
        <html lang="en">
        <body className="bg-gray-100 text-gray-900 antialiased m-0 p-0">
        <Toaster position="top-right" />

        {isAuthPage ? (
            <>{children}</>
        ) : !authChecked ? (
            <div className="flex h-screen items-center justify-center bg-gray-100">
                <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
        ) : (
            <div className="flex h-screen w-screen overflow-hidden">

                {/* LEFT COLUMN: FIXED SIDEBAR PANEL */}
                <aside className="w-64 bg-slate-900 text-white flex flex-col justify-between flex-shrink-0">
                    <div className="p-4">
                        <div className="mb-8 px-2 border-b border-slate-800 pb-4">
                            <h1 className="text-xl font-bold tracking-wider text-indigo-400">POS TERMINAL</h1>
                            <p className="text-xs text-gray-400 mt-1">Operational Control Desk</p>
                        </div>

                        <nav className="space-y-1">
                            {menuItems.map((item) => {
                                const Icon = item.icon;
                                const isActive = pathname === item.href;
                                return (
                                    <Link
                                        key={item.href}
                                        href={item.href}
                                        className={`flex items-center gap-3 px-4 py-2.5 text-sm font-medium rounded-lg transition-colors ${
                                            isActive
                                                ? 'bg-indigo-600 text-white'
                                                : 'text-gray-300 hover:bg-slate-800 hover:text-white'
                                        }`}
                                    >
                                        <Icon className="h-5 w-5 text-indigo-400" />
                                        {item.name}
                                    </Link>
                                );
                            })}
                        </nav>
                    </div>
                </aside>

                {/* RIGHT COLUMN: ACTIVE PAGE CONTENT WORKSPACE */}
                <main className="flex-1 overflow-y-auto bg-gray-50 p-8">
                    {children}
                </main>

            </div>
        )}
        </body>
        </html>
    );
}