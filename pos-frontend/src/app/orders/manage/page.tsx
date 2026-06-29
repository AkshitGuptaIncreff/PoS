'use client';

import { Fragment, useCallback, useEffect, useState } from 'react';
import { orderService } from '@/services/order.service';
import { downloadInvoiceDirectly } from '@/lib/downloadPdf';
import { toast } from 'sonner';
import { Search, FileDown, ChevronDown, ChevronUp, Loader2, RefreshCw, XCircle, FileText } from 'lucide-react';

const PAGE_SIZE = 10;

export default function OrderManagePage() {
    const [orders, setOrders] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [expandedOrders, setExpandedOrders] = useState<Record<string, boolean>>({});
    const [actionLoading, setActionLoading] = useState<Record<string, boolean>>({});

    // Query parameters filter states
    const [orderIdSearch, setOrderIdSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    // Pagination states
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const loadOrderHistoryList = useCallback(async (currentPage: number) => {
        setLoading(true);
        try {
            const data = await orderService.getOrders({
                orderId: orderIdSearch.trim() || undefined,
                status: statusFilter || undefined,
                page: currentPage,
                size: PAGE_SIZE,
            });
            setOrders(data.content);
            setPage(data.page);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
        } catch (err) {
            toast.error('Failed to sync historical order logs from database');
        } finally {
            setLoading(false);
        }
    }, [orderIdSearch, statusFilter]);

    useEffect(() => {
        loadOrderHistoryList(0);
    }, [statusFilter, loadOrderHistoryList]);

    const handlePageChange = (newPage: number) => {
        if (newPage < 0 || newPage >= totalPages) return;
        loadOrderHistoryList(newPage);
    };

    const handleFilter = () => {
        loadOrderHistoryList(0);
    };

    const toggleRowExpansion = (id: string) => {
        setExpandedOrders((prev) => ({ ...prev, [id]: !prev[id] }));
    };


    const handleInvoiceAction = async (orderId: string, currentStatus: string) => {
        setActionLoading((prev) => ({ ...prev, [orderId]: true }));
        try {
            if (currentStatus === 'FULFILLED') {
                toast.info('Compiling and generating fresh commercial invoice document...');
            } else {
                toast.info('Streaming existing invoice asset download...');
            }

            // 1. Await the complete binary file download loop to finish
            await downloadInvoiceDirectly(orderId);
            toast.success('Invoice document downloaded successfully.');

            // 2. Clear out any client-side layout cache and pull fresh logs from the database
            // This forces the UI to look at what Spring Boot + MongoDB says the state is now!
            await loadOrderHistoryList(page);

        } catch (err) {
            console.error("Invoice action loop failed:", err);
            toast.error('Failed downloading invoice file stream resource.');
        } finally {
            setActionLoading((prev) => ({ ...prev, [orderId]: false }));
        }
    };

    const handleCancelOrder = async (orderId: string) => {
        setActionLoading((prev) => ({ ...prev, [orderId]: true }));
        try {
            await orderService.cancelOrder(orderId);
            toast.success('Order successfully CANCELLED.');
            loadOrderHistoryList(page);
        } catch (err) {
            toast.error('Cancellation request rejected.');
        } finally {
            setActionLoading((prev) => ({ ...prev, [orderId]: false }));
        }
    };

    const handleRetryOrder = async (orderId: string) => {
        setActionLoading((prev) => ({ ...prev, [orderId]: true }));
        try {
            await orderService.retryOrder(orderId);
            toast.success('Inventory re-allocation pass processed successfully!');
            loadOrderHistoryList(page);
        } catch (err) {
            toast.error('Retry execution failed. Stock might still be unavailable.');
        } finally {
            setActionLoading((prev) => ({ ...prev, [orderId]: false }));
        }
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-gray-900">Order Management Center</h1>
                <p className="text-sm text-gray-500">Monitor running transaction fulfillment states, process unfulfilled backlogs, and compile invoice receipts.</p>
            </div>

            {/* Query Filter Section */}
            <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm flex flex-col sm:flex-row items-end gap-4">
                <div className="flex-1 w-full">
                    <label className="block text-xs font-semibold text-gray-500 uppercase mb-1.5">Lookup Order ID</label>
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                        <input
                            type="text"
                            placeholder="Search by backend Order reference identity string..."
                            value={orderIdSearch}
                            onChange={(e) => setOrderIdSearch(e.target.value)}
                            className="w-full rounded-md border border-gray-300 pl-10 pr-4 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        />
                    </div>
                </div>

                <div className="w-full sm:w-56">
                    <label className="block text-xs font-semibold text-gray-500 uppercase mb-1.5">Fulfillment State Filter</label>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 bg-white"
                    >
                        <option value="">-- All Statuses --</option>
                        <option value="FULFILLED">FULFILLED</option>
                        <option value="UNFULFILLED">UNFULFILLED</option>
                        <option value="INVOICED">INVOICED</option>
                        <option value="CANCELLED">CANCELLED</option>
                    </select>
                </div>

                <button
                    onClick={handleFilter}
                    className="rounded-lg bg-indigo-600 px-5 py-2 text-sm font-semibold text-white hover:bg-indigo-700 h-[38px] transition-colors shadow-sm w-full sm:w-auto"
                >
                    Filter Logs
                </button>
            </div>

            {/* Main Ledger Grid */}
            <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
                {loading ? (
                    <div className="p-8 text-center text-gray-500">Parsing transaction journals database...</div>
                ) : (
                    <table className="w-full border-collapse text-left text-sm text-gray-500">
                        <thead className="bg-gray-50 text-xs font-semibold uppercase text-gray-700 border-b border-gray-200">
                        <tr>
                            <th className="w-10 px-6 py-4"></th>
                            <th className="px-6 py-4">Fulfillment Reference ID</th>
                            <th className="px-6 py-4">Customer Details</th>
                            <th className="px-6 py-4">Current Condition</th>
                            <th className="px-6 py-4 text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {orders.length === 0 ? (
                            <tr>
                                <td colSpan={5} className="px-6 py-12 text-center text-gray-400 font-medium">
                                    No matching transaction journals located.
                                </td>
                            </tr>
                        ) : (
                            orders.map((order) => {
                                const isExpanded = !!expandedOrders[order.orderId];
                                const isDoingAction = !!actionLoading[order.orderId];

                                return (
                                    <Fragment key={order.orderId}>
                                        <tr className="hover:bg-gray-50/50">
                                            <td className="px-6 py-4">
                                                <button onClick={() => toggleRowExpansion(order.orderId)} className="text-gray-400 hover:text-gray-600">
                                                    {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
                                                </button>
                                            </td>
                                            <td className="px-6 py-4 font-mono text-xs font-bold text-gray-600">{order.orderId}</td>
                                            <td className="px-6 py-4">
                                                <div className="font-semibold text-gray-900">{order.customerName}</div>
                                                <div className="text-xs text-gray-400">{order.customerEmail}</div>
                                            </td>
                                            <td className="px-6 py-4">
                          <span className={`inline-flex items-center rounded-md px-2.5 py-1 text-xs font-bold uppercase tracking-wide ${
                              order.status === 'FULFILLED' ? 'bg-emerald-50 text-emerald-700' :
                                  order.status === 'INVOICED' ? 'bg-indigo-50 text-indigo-700' :
                                      order.status === 'UNFULFILLED' ? 'bg-amber-50 text-amber-700' :
                                          'bg-slate-100 text-slate-600'
                          }`}>
                            {order.status}
                          </span>
                                            </td>
                                            <td className="px-6 py-4 text-right space-x-2">

                                                {order.cancellable && (
                                                    <button
                                                        onClick={() => handleCancelOrder(order.orderId)}
                                                        disabled={isDoingAction}
                                                        className="inline-flex items-center gap-1 text-xs font-bold text-rose-700 bg-rose-50 hover:bg-rose-100 px-2.5 py-1.5 rounded transition-colors"
                                                    >
                                                        <XCircle className="h-3 w-3" /> Cancel Order
                                                    </button>
                                                )}

                                                {order.retryable && (
                                                    <button
                                                        onClick={() => handleRetryOrder(order.orderId)}
                                                        disabled={isDoingAction}
                                                        className="inline-flex items-center gap-1 text-xs font-bold text-amber-700 bg-amber-50 hover:bg-amber-100 px-2.5 py-1.5 rounded transition-colors"
                                                    >
                                                        <RefreshCw className={`h-3 w-3 ${isDoingAction ? 'animate-spin' : ''}`} /> Retry Allocation
                                                    </button>
                                                )}

                                                {order.status === 'FULFILLED' && (
                                                    <button
                                                        onClick={() => handleInvoiceAction(order.orderId, 'FULFILLED')}
                                                        disabled={isDoingAction}
                                                        className="inline-flex items-center gap-1 text-xs font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 px-3 py-1.5 rounded border border-emerald-200 shadow-sm transition-colors"
                                                    >
                                                        <FileText className="h-3.5 w-3.5" /> Generate Invoice
                                                    </button>
                                                )}

                                                {order.status === 'INVOICED' && (
                                                    <button
                                                        onClick={() => handleInvoiceAction(order.orderId, 'INVOICED')}
                                                        disabled={isDoingAction}
                                                        className="inline-flex items-center gap-1 text-xs font-bold text-indigo-700 bg-indigo-50 hover:bg-indigo-100 px-3 py-1.5 rounded transition-colors"
                                                    >
                                                        <FileDown className="h-3.5 w-3.5" /> Download PDF
                                                    </button>
                                                )}

                                            </td>
                                        </tr>

                                        {/* Collapsible Line-item section */}
                                        {isExpanded && (
                                            <tr className="bg-gray-50/50">
                                                <td colSpan={5} className="px-16 py-4">
                                                    <div className="border border-gray-200 rounded-lg bg-white overflow-hidden shadow-inner">
                                                        <table className="w-full text-left text-xs text-gray-500">
                                                            <thead className="bg-slate-100 text-[10px] font-bold uppercase tracking-wider text-slate-600 border-b border-gray-200">
                                                            <tr>
                                                                <th className="px-4 py-2.5">Item Catalog Name</th>
                                                                <th className="px-4 py-2.5">SKU Barcode</th>
                                                                <th className="px-4 py-2.5">Purchased Quantity</th>
                                                                <th className="px-4 py-2.5 text-right">Selling Price</th>
                                                            </tr>
                                                            </thead>
                                                            <tbody className="divide-y divide-gray-100">
                                                            {order.items?.map((item: any, idx: number) => (
                                                                <tr key={idx} className="hover:bg-slate-50/50">
                                                                    <td className="px-4 py-2.5 font-semibold text-gray-900">{item.productName}</td>
                                                                    <td className="px-4 py-2.5 font-mono text-gray-400">{item.barcode}</td>
                                                                    <td className="px-4 py-2.5 font-bold text-slate-700">{item.quantity} units</td>
                                                                    <td className="px-4 py-2.5 text-right font-semibold text-gray-900">₹{item.sellingPrice?.toFixed(2)}</td>
                                                                </tr>
                                                            ))}
                                                            </tbody>
                                                        </table>
                                                    </div>
                                                </td>
                                            </tr>
                                        )}
                                    </Fragment>
                                );
                            })
                        )}
                        </tbody>
                    </table>
                )}
            </div>

            {totalPages > 1 && !loading && (
                <div className="flex items-center justify-between">
                    <p className="text-sm text-gray-500">
                        Page {page + 1} of {totalPages} ({totalElements} orders)
                    </p>
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => handlePageChange(page - 1)}
                            disabled={page === 0}
                            className="rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                        >
                            Previous
                        </button>
                        <div className="flex items-center gap-1">
                            {Array.from({ length: totalPages }).map((_, i) => (
                                <button
                                    key={i}
                                    onClick={() => handlePageChange(i)}
                                    className={`w-8 h-8 rounded-md text-sm font-medium transition-colors ${
                                        i === page
                                            ? 'bg-indigo-600 text-white'
                                            : 'text-gray-600 hover:bg-gray-100'
                                    }`}
                                >
                                    {i + 1}
                                </button>
                            ))}
                        </div>
                        <button
                            onClick={() => handlePageChange(page + 1)}
                            disabled={page === totalPages - 1}
                            className="rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                        >
                            Next
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}