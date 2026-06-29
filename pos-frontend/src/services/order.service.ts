import { api } from '@/lib/api';

export interface OrderItemForm {
    barcode: string;
    quantity: number;
    sellingPrice: number;
}

export interface CreateOrderForm {
    customerName: string;
    email: string;
    items: OrderItemForm[];
}

export interface OrderData {
    orderId: string;
    customerName: string;
    customerEmail: string;
    status: string;
    message?: string;
    errors?: string[];
    cancellable: boolean;
    retryable: boolean;
}

export interface PageData<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    page: number;
    size: number;
}

export interface OrderFilters {
    orderId?: string;
    status?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
}

export const orderService = {
    async createOrder(data: any): Promise<any> {
        const res = await api.post('/order', data);
        return res.data;
    },

    async getOrders(filters: OrderFilters = {}): Promise<PageData<OrderData>> {
        const params: Record<string, any> = {};
        if (filters.orderId) params.orderId = filters.orderId;
        if (filters.status) params.status = filters.status;
        if (filters.startDate) params.startDate = new Date(filters.startDate).toISOString();
        if (filters.endDate) params.endDate = new Date(filters.endDate).toISOString();
        params.page = filters.page ?? 0;
        params.size = filters.size ?? 10;

        const res = await api.get('/order', { params });
        return res.data;
    },

    async retryOrder(orderId: string): Promise<any> {
        const res = await api.post(`/order/${orderId}/retry`);
        return res.data;
    },

    async cancelOrder(orderId: string): Promise<any> {
        const res = await api.post(`/order/${orderId}/cancel`);
        return res.data;
    }
};