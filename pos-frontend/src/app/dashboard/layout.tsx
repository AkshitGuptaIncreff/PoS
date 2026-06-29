'use client';

import Link from 'next/link'; //  Import the component from 'next/link'
import { usePathname } from 'next/navigation'; // Keep this here for active links
import { useAuth } from '@/hooks/useAuth';
import { LayoutDashboard, Users, Box, ShoppingCart, FileText, ClipboardList, LogOut } from 'lucide-react';

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
    const { user, loading, logout } = useAuth();
    const pathname = usePathname();

    if (loading) {
        return <div className="flex h-screen items-center justify-center">Authenticating Session...</div>;
    }

    // Entire navigation configuration structural registry
    const menuItems = [
        { name: 'View Products', href: '/products', icon: Box, roles: ['SUPERVISOR', 'OPERATOR'] },
        { name: 'Create Order', href: '/orders/create', icon: ShoppingCart, roles: ['SUPERVISOR', 'OPERATOR'] },
        { name: 'Order Manager', href: '/orders/manage', icon: ClipboardList, roles: ['SUPERVISOR', 'OPERATOR'] },
        { name: 'Client Management', href: '/clients', icon: Users, roles: ['SUPERVISOR'] },
        { name: 'Sales Report', href: '/reports/sales', icon: FileText, roles: ['SUPERVISOR'] },
        { name: 'Day Sales Report', href: '/reports/day-sales', icon: LayoutDashboard, roles: ['SUPERVISOR'] },
    ];

    return (
        <div className="flex h-screen bg-gray-100">
            {/* Sidebar Navigation */}
            <aside className="w-64 bg-slate-900 text-white flex flex-col justify-between">
                <div className="p-4">
                    <div className="mb-8 px-2">
                        <h1 className="text-xl font-bold tracking-wider text-indigo-400">POS RETAIL</h1>
                        <p className="text-xs text-gray-400 mt-1">Role: <span className="font-semibold text-white">{user?.role}</span></p>
                    </div>

                    <nav className="space-y-1">
                        {menuItems
                            .filter((item) => item.roles.includes(user?.role || ''))
                            .map((item) => {
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
                                        <Icon className="h-5 w-5" />
                                        {item.name}
                                    </Link>
                                );
                            })}
                    </nav>
                </div>

                {/* User Identity & Logout Action Footer */}
                <div className="p-4 border-t border-slate-800 bg-slate-950/50">
                    <div className="flex items-center justify-between mb-2 px-2">
                        <span className="text-xs text-gray-400 truncate max-w-[140px]">{user?.email}</span>
                    </div>
                    <button
                        onClick={logout}
                        className="flex w-full items-center gap-3 px-4 py-2 text-sm font-medium text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
                    >
                        <LogOut className="h-5 w-5" />
                        Sign Out
                    </button>
                </div>
            </aside>

            {/* Primary Workspace Interface View */}
            <main className="flex-1 overflow-y-auto p-8">
                {children}
            </main>
        </div>
    );
}