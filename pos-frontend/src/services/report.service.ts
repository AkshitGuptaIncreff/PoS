import { api } from '@/lib/api';

export interface SalesReportData {
    clientName: string;
    clientId:string,
    productName: string;
    quantity: number;
    revenue: number;
}

export interface DaySalesReportData {
    date: string;
    invoicedOrdersCount: number;
    invoicedItemsCount: number;
    totalRevenue: number;
}

export const reportService = {
    // 1. Wholesale Sales Report (POST JSON payload with Auth Session Header)
    async getSalesReport(filters: { startDate: string; endDate: string; clientId?: string; })
        : Promise<SalesReportData[]> {

        const payload = {
            startDate: filters.startDate ? `${filters.startDate}T00:00:00Z` : null,
            endDate: filters.endDate ? `${filters.endDate}T23:59:59Z` : null,
            clientId: filters.clientId && filters.clientId.trim() !== '' ? filters.clientId.trim() : null
        };

        let sessionId = '';
        if (typeof window !== 'undefined') {
            const stored = sessionStorage.getItem('pos_session');
            if (stored) {
                try {
                    sessionId = JSON.parse(stored)?.token || '';
                } catch (e) {
                    console.error('Failed parsing session token', e);
                }
            }
        }

        const res = await api.post('/reports/sales', payload, {
            headers: {
                sessionId, 'Content-Type': 'application/json'
            }
        });

        return Array.isArray(res.data) ? res.data : [];
    },

    // 2. Day Sales Report (GET — auto-loads all records, most recent first)
    async getDaySalesReport(): Promise<DaySalesReportData[]> {
        const res = await api.get('/reports/day-sales');
        return Array.isArray(res.data) ? res.data : [];
    }
};