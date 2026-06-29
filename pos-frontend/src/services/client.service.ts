import { api } from '@/lib/api';

export interface ClientData {
    clientId: string;
    name: string;
    email: string;
}

export interface ClientForm {
    clientId?: string;
    name: string;
    email: string;
}

export interface PageData<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    page: number;
    size: number;
}

export const clientService = {
    async getAllClients(page = 0, size = 10): Promise<PageData<ClientData>> {
        const res = await api.get('/clients', { params: { page, size } });
        return res.data;
    },

    async filterClientsByName(name: string, page = 0, size = 10): Promise<PageData<ClientData>> {
        const res = await api.get('/clients/filter', { params: { name, page, size } });
        return res.data;
    },

    async createClient(data: ClientForm): Promise<ClientData> {
        const res = await api.post('/clients', data);
        return res.data;
    },

    async updateClient(clientId: string, data: ClientForm): Promise<ClientData> {
        const res = await api.put(`/clients/${clientId}`, data);
        return res.data;
    }
};
