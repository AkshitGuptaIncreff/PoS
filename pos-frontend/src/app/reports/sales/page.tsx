'use client';

import {useEffect, useState, useCallback, useMemo} from 'react';
import { clientService, type ClientData } from '@/services/client.service';
import { reportService, type SalesReportData } from '@/services/report.service';
import { toast } from 'sonner';
import { startOfMonth, format } from 'date-fns';
import { BarChart3, ShieldAlert, Loader2, Search, AlertCircle } from 'lucide-react';

type ValidationErrors = {
    clientName?: string;
    startDate?: string;
    endDate?: string;
    dateRange?: string;
};

export default function SalesReportPage() {
    const [reports, setReports] = useState<SalesReportData[]>([]);
    const [loading, setLoading] = useState(false);
    const [userRole, setUserRole] = useState<string | null>(null);
    const [clients, setClients] = useState<ClientData[]>([]);
    const [clientsLoading, setClientsLoading] = useState(true);

    const [clientId, setClientId] = useState('');
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});
    const [hasSearched, setHasSearched] = useState(false);

    useEffect(() => {
        if (typeof window !== 'undefined') {
            const stored = sessionStorage.getItem('pos_session');
            if (stored) {
                try {
                    const session = JSON.parse(stored);
                    setUserRole(session?.role || null);
                } catch {
                    console.error('Failed reading session role data');
                }
            }
        }
    }, []);

    useEffect(() => {
        if (userRole !== 'SUPERVISOR') {
            setClientsLoading(false);
            return;
        }

        const now = new Date();
        setStartDate(format(startOfMonth(now), 'yyyy-MM-dd'));
        setEndDate(format(now, 'yyyy-MM-dd'));

        async function loadClients() {
            try {
                const pageData = await clientService.getAllClients(0, 100);
                setClients(pageData.content);
            } catch {
                toast.error('Failed to load client list.');
            } finally {
                setClientsLoading(false);
            }
        }
        loadClients();
    }, [userRole]);

    const validate = useCallback((): boolean => {
        const errors: ValidationErrors = {};
        if (!clientId) {
            errors.clientName = 'Please select a client.';
        }
        if (!startDate) {
            errors.startDate = 'Start date is required.';
        }
        if (!endDate) {
            errors.endDate = 'End date is required.';
        }
        if (startDate && endDate && startDate > endDate) {
            errors.dateRange = 'Start date must be on or before end date.';
        }
        setValidationErrors(errors);
        return Object.keys(errors).length === 0;
    }, [clientId, startDate, endDate]);

    const handleFetchReport = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        setLoading(true);
        setHasSearched(true);
        try {
            const data = await reportService.getSalesReport({ startDate, endDate, clientId });
            console.log("Sales Report Response:", data);
            setReports(data);
            if (data.length === 0) {
                toast.info('No sales found for the selected client and date range.');
            } else {
                toast.success(`Sales report for "${clientId}" compiled successfully.`);
            }
            setValidationErrors({});
        } catch (err: any) {
            const serverMsg = err?.response?.data?.message || 'Failed to generate sales report.';
            toast.error(serverMsg);
            setReports([]);
        } finally {
            setLoading(false);
        }
    };

    const clientMap = useMemo(() =>
            Object.fromEntries(clients.map(c => [c.clientId, c.name])), [clients]
    );

    if (userRole && userRole !== 'SUPERVISOR') {
        return (
            <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 bg-white rounded-xl border border-dashed border-gray-300 max-w-2xl mx-auto mt-12 shadow-sm">
                <ShieldAlert className="h-12 w-12 text-rose-500 mb-4 animate-pulse" />
                <h2 className="text-xl font-bold text-gray-900">Access Level Restriction</h2>
                <p className="text-sm text-gray-500 mt-2 max-w-md">
                    Sales reports are restricted to the <span className="font-semibold text-slate-800">SUPERVISOR</span> role.
                </p>
            </div>
        );
    }

    const cumulativeRevenue = reports.reduce((sum, item) => sum + (item.revenue || 0), 0);

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-gray-900">Sales Report</h1>
                <p className="text-sm text-gray-500">View aggregated sales by client and product within a date range.</p>
            </div>

            <form onSubmit={handleFetchReport} className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm space-y-4" noValidate>
                <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 items-end">
                    <div>
                        <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">Client</label>
                        <select
                            value={clientId}
                            onChange={(e) => {
                                setClientId(e.target.value);
                                setValidationErrors(prev => ({
                                    ...prev,
                                    clientName: undefined
                                }));
                            }}
                            className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 ${
                                validationErrors.clientName
                                    ? 'border-red-400'
                                    : 'border-gray-300'
                            }`}
                        >
                            <option value="">
                                {clientsLoading
                                    ? 'Loading clients...'
                                    : '-- Select a Client --'}
                            </option>

                            {clients.map((c) => (
                                <option key={c.clientId} value={c.clientId}>
                                    {c.name} ({c.clientId})
                                </option>
                            ))}
                        </select>
                        {validationErrors.clientName && (
                            <p className="mt-1 text-xs text-red-500 flex items-center gap-1"><AlertCircle className="h-3 w-3" />{validationErrors.clientName}</p>
                        )}
                    </div>
                    <div>
                        <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">Start Date</label>
                        <input
                            type="date"
                            value={startDate}
                            onChange={(e) => { setStartDate(e.target.value); setValidationErrors((prev) => ({ ...prev, startDate: undefined, dateRange: undefined })); }}
                            className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 ${validationErrors.startDate || validationErrors.dateRange ? 'border-red-400' : 'border-gray-300'}`}
                        />
                        {validationErrors.startDate && (
                            <p className="mt-1 text-xs text-red-500 flex items-center gap-1"><AlertCircle className="h-3 w-3" />{validationErrors.startDate}</p>
                        )}
                    </div>
                    <div>
                        <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">End Date</label>
                        <input
                            type="date"
                            value={endDate}
                            onChange={(e) => { setEndDate(e.target.value); setValidationErrors((prev) => ({ ...prev, endDate: undefined, dateRange: undefined })); }}
                            className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 ${validationErrors.endDate || validationErrors.dateRange ? 'border-red-400' : 'border-gray-300'}`}
                        />
                        {validationErrors.endDate && (
                            <p className="mt-1 text-xs text-red-500 flex items-center gap-1"><AlertCircle className="h-3 w-3" />{validationErrors.endDate}</p>
                        )}
                    </div>
                    <button
                        type="submit"
                        disabled={loading}
                        className="flex w-full items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400 disabled:cursor-not-allowed transition-colors h-[38px] shadow-sm"
                    >
                        {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
                        {loading ? 'Generating...' : 'Generate Report'}
                    </button>
                </div>
                {validationErrors.dateRange && (
                    <p className="text-xs text-red-500 flex items-center gap-1"><AlertCircle className="h-3 w-3" />{validationErrors.dateRange}</p>
                )}
            </form>

            <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
                <table className="w-full border-collapse text-left text-sm text-gray-500">
                    <thead className="bg-gray-50 text-xs font-semibold uppercase text-gray-700 border-b border-gray-200">
                        <tr>
                            <th className="px-6 py-4">Client</th>
                            <th className="px-6 py-4">Product</th>
                            <th className="px-6 py-4">Quantity</th>
                            <th className="px-6 py-4 text-right">Revenue</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200">
                        {!hasSearched ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-16 text-center text-gray-400">
                                    <div className="flex flex-col items-center gap-2">
                                        <BarChart3 className="h-8 w-8 text-gray-300" />
                                        <span className="font-medium">Select a client and click &quot;Generate Report&quot; to view sales data.</span>
                                    </div>
                                </td>
                            </tr>
                        ) : loading ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-16 text-center text-gray-400">
                                    <Loader2 className="h-8 w-8 animate-spin mx-auto text-indigo-500" />
                                </td>
                            </tr>
                        ) : reports.length === 0 ? (
                            <tr>
                                <td colSpan={4} className="px-6 py-16 text-center text-gray-400">
                                    <div className="flex flex-col items-center gap-2">
                                        <Search className="h-8 w-8 text-gray-300" />
                                        <span className="font-medium">No sales found for the selected client and date range.</span>
                                    </div>
                                </td>
                            </tr>
                        ) : (
                            <>
                                {reports.map((row, idx) => (
                                    <tr key={idx} className="hover:bg-gray-50/50">
                                        <td className="px-6 py-4">
                                            <div className="flex flex-col gap-1">
                                                <span className="font-semibold text-gray-900">
                                                    {clientMap[row.clientId] ?? "Unknown Client"}
                                                </span>
                                                <span className="inline-flex w-fit rounded bg-gray-100 px-2 py-0.5 text-[11px] text-gray-600">
                                                    {row.clientId}
                                                </span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 text-gray-600">{row.productName}</td>
                                        <td className="px-6 py-4 font-medium text-slate-700">{row.quantity}</td>
                                        <td className="px-6 py-4 text-right font-bold text-gray-900">₹{row.revenue?.toFixed(2)}</td>
                                    </tr>
                                ))}
                                <tr className="bg-indigo-50/40 font-bold border-t-2 border-indigo-100">
                                    <td colSpan={3} className="px-6 py-4 text-indigo-900 text-sm uppercase tracking-wider">Total</td>
                                    <td className="px-6 py-4 text-right text-indigo-600 text-lg">₹{cumulativeRevenue.toFixed(2)}</td>
                                </tr>
                            </>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}