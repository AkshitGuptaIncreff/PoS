import { api } from '@/lib/api';

export interface InventoryData {
    clientName: string;
    productName: string;
    productBarcode: string;
    clientId: string;
    mrp: number | null;
    quantity: number;
    imageUrl: string | null;
}

export interface RowError {
    row: number;
    error: string;
    barcode?: string;
    clientId?: string;
    name?: string;
    mrp?: string;
    imageUrl?: string;
}

export interface UploadResult<T> {
    imported: T[];
    errors: RowError[];
    totalRows: number;
    importedCount: number;
    errorCount: number;
}

export interface InventoryForm {
    barcode: string;
    quantity: number;
}

export const inventoryService = {
    async getAllInventory(): Promise<InventoryData[]> {
        const res = await api.get('/inventory', { params: { page: 0, size: 10000 } });
        return res.data.content || [];
    },

    async uploadInventoryTsv(file: File): Promise<UploadResult<InventoryData>> {
        const formData = new FormData();
        formData.append('file', file);
        const res = await api.post('/inventory/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return res.data;
    },

    async updateSingleInventory(data: InventoryForm): Promise<InventoryData> {
        const res = await api.post('/inventory', data);
        return res.data;
    },

    downloadTemplate(): void {
        const header = ['barcode', 'quantity'];
        const rows = [
            header.join('\t'),
            'SKU001\t50',
        ];
        const blob = new Blob([rows.join('\n')], { type: 'text/tab-separated-values' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'inventory-template.tsv';
        a.click();
        URL.revokeObjectURL(url);
    }
};
