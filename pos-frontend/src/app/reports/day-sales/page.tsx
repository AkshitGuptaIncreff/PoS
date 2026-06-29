'use client';

import { useEffect, useState } from 'react';
import { reportService, DaySalesReportData } from '@/services/report.service';
import { toast } from 'sonner';
import { Calendar, TrendingUp, ShieldAlert, Loader2 } from 'lucide-react';

export default function DaySalesReportPage() {
    const [reports, setReports] = useState<DaySalesReportData[]>([]);
    const [loading, setLoading] = useState(true);
    const [userRole, setUserRole] = useState<string | null>(null);

    useEffect(() => {
        if (typeof window !== 'undefined') {
            const stored = sessionStorage.getItem('pos_session');
            if (stored) {
                try {
                    setUserRole(JSON.parse(stored)?.role || null);
                } catch (e) {
                    console.error('Failed parsing role configurations metadata', e);
                }
            }
        }
    }, []);

    useEffect(() => {
        if (userRole && userRole !== 'SUPERVISOR') {
            setLoading(false);
            return;
        }
        if (!userRole) return;

        async function loadReports() {
            setLoading(true);
            try {
                const data = await reportService.getDaySalesReport();
                setReports(data);
            } catch (err) {
                toast.error('Failed loading day sales report.');
            } finally {
                setLoading(false);
            }
        }

        loadReports();
    }, [userRole]);

    if (userRole && userRole !== 'SUPERVISOR') {
        return (
            <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 bg-white rounded-xl border border-dashed border-gray-300 max-w-2xl mx-auto mt-12 shadow-sm">
                <ShieldAlert className="h-12 w-12 text-rose-500 mb-4 animate-pulse" />
                <h2 className="text-xl font-bold text-gray-900">Access Level Restriction</h2>
                <p className="text-sm text-gray-500 mt-2 max-w-md">
                    Chronological timeline logs are strictly restricted to the <span className="font-semibold text-slate-800">SUPERVISOR</span> operational authority.
                </p>
            </div>
        );
    }

    const totalRevenue = reports.reduce((sum, day) => sum + (day.totalRevenue || 0), 0);

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-gray-900">Daily Order Sales Timeline</h1>
                <p className="text-sm text-gray-500">Monitor calendar turn counts and track macroeconomic metrics.</p>
            </div>

            {loading ? (
                <div className="flex items-center justify-center py-20">
                    <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
                </div>
            ) : (
                <>
                    <div className="flex items-center gap-2 text-sm text-gray-500">
                        <TrendingUp className="h-4 w-4" />
                        <span>{reports.length} day{reports.length !== 1 ? 's' : ''} recorded</span>
                        {totalRevenue > 0 && (
                            <span className="ml-auto font-semibold text-gray-900">
                                Total: ₹{totalRevenue.toFixed(2)}
                            </span>
                        )}
                    </div>

                    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
                        <table className="w-full border-collapse text-left text-sm text-gray-500">
                            <thead className="bg-gray-50 text-xs font-semibold uppercase text-gray-700 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-4">Date</th>
                                <th className="px-6 py-4">Orders</th>
                                <th className="px-6 py-4">Items</th>
                                <th className="px-6 py-4 text-right">Revenue</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {reports.length === 0 ? (
                                <tr>
                                    <td colSpan={4} className="px-6 py-12 text-center text-gray-400 font-medium">
                                        No day sales records found. The scheduler generates reports nightly.
                                    </td>
                                </tr>
                            ) : (
                                reports.map((day) => (
                                    <tr key={day.date} className="hover:bg-slate-50/60">
                                        <td className="px-6 py-4 font-semibold text-gray-900">
                                            <span className="flex items-center gap-2">
                                                <Calendar className="h-4 w-4 text-indigo-500" />
                                                {day.date}
                                            </span>
                                        </td>
                                        <td className="px-6 py-4">{day.invoicedOrdersCount}</td>
                                        <td className="px-6 py-4">{day.invoicedItemsCount}</td>
                                        <td className="px-6 py-4 text-right font-bold text-indigo-600">₹{day.totalRevenue?.toFixed(2)}</td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </div>
                </>
            )}
        </div>
    );
}
