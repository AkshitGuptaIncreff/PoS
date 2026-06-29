import { api } from '@/lib/api';

export interface ProductData {
    barcode: string;
    productName: string;
    clientName: string;
    clientId: string;
    mrp: number;
    imageUrl?: string;
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

export interface ProductForm {
    barcode: string;
    clientId: string;
    name: string;
    mrp: number;
    imageUrl?: string;
}

export const productService = {
    async getProducts(): Promise<ProductData[]> {
        const res = await api.get('/products');
        return Array.isArray(res.data) ? res.data : [];
    },

    async createSingleProduct(data: ProductForm): Promise<ProductData> {
        const res = await api.post('/products', data);
        return res.data;
    },

    async updateProduct(productId: string, data: ProductForm): Promise<ProductData> {
        const res = await api.put(`/products/update`, data);
        return res.data;
    },

    async uploadProductTSV(file: File): Promise<UploadResult<ProductData>> {
        const formData = new FormData();
        formData.append('file', file);
        const res = await api.post('/products/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return res.data;
    },

    downloadTemplate(): void {
        const header = ['barcode', 'clientId', 'name', 'mrp', 'imageUrl'];
        const rows = [
            header.join('\t'),
            'SKU001\tCL000001\tproduct-name\t99.00\thttp://example.com/image.png',
        ];
        const blob = new Blob([rows.join('\n')], { type: 'text/tab-separated-values' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'product-template.tsv';
        a.click();
        URL.revokeObjectURL(url);
    }
};
