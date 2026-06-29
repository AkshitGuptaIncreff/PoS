import { api } from './api';

export async function downloadInvoiceDirectly(orderId: string) {
    try {
        // Request a raw binary blob data return profile explicitly from your InvoiceController
        const response = await api.get(`/invoice/${orderId}`, {
            responseType: 'blob',
        });

        // Generate a temporary down-link URL pointing to the raw file data stream
        const blobUrl = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));

        const link = document.createElement('a');
        link.href = blobUrl;
        link.setAttribute('download', `Invoice_${orderId}.pdf`);

        document.body.appendChild(link);
        link.click();

        // Clean up memory
        link.parentNode?.removeChild(link);
        window.URL.revokeObjectURL(blobUrl);
    } catch (error) {
        console.error('Invoice download streaming extraction failed:', error);
        throw error;
    }
}