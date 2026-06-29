'use client';

import { useEffect, useState, useRef, type ChangeEvent } from 'react';
import { productService, type ProductData, type ProductForm, type UploadResult, type RowError } from '@/services/product.service';
import { inventoryService, type InventoryData } from '@/services/inventory.service';
import { toast } from 'sonner';
import { Plus, Upload, Download, Search, X, Loader2, CheckCircle2, XCircle, Package, Edit3, ImageOff } from 'lucide-react';

type TsvMode = 'product' | 'inventory';

export default function ProductPage() {
    const [products, setProducts] = useState<ProductData[]>([]);
    const [inventoryMap, setInventoryMap] = useState<Record<string, number>>({});
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState('');

    const [selectedProduct, setSelectedProduct] = useState<ProductData | null>(null);
    const [detailsOpen, setDetailsOpen] = useState(false);

    const [createOpen, setCreateOpen] = useState(false);
    const [createBarcode, setCreateBarcode] = useState('');
    const [createName, setCreateName] = useState('');
    const [createClientId, setCreateClientId] = useState('');
    const [createMrp, setCreateMrp] = useState('');
    const [createImageUrl, setCreateImageUrl] = useState('');
    const [creating, setCreating] = useState(false);

    const [editOpen, setEditOpen] = useState(false);
    const [editName, setEditName] = useState('');
    const [editMrp, setEditMrp] = useState('');
    const [editImageUrl, setEditImageUrl] = useState('');
    const [updating, setUpdating] = useState(false);

    const [inventoryBarcode, setInventoryBarcode] = useState('');
    const [inventoryQuantity, setInventoryQuantity] = useState<number>(0);
    const [inventoryUpdating, setInventoryUpdating] = useState(false);

    const [tsvMode, setTsvMode] = useState<TsvMode>('product');
    const [tsvOpen, setTsvOpen] = useState(false);
    const [tsvUploading, setTsvUploading] = useState(false);
    const [tsvResult, setTsvResult] = useState<UploadResult<ProductData | InventoryData> | null>(null);
    const tsvInputRef = useRef<HTMLInputElement>(null);

    const loadPageData = async () => {
        setLoading(true);
        try {
            const [productsData, inventoryData] = await Promise.all([
                productService.getProducts(),
                inventoryService.getAllInventory()
            ]);
            const invMap: Record<string, number> = {};
            inventoryData.forEach((item) => {
                if (item.productBarcode) {
                    invMap[item.productBarcode] = item.quantity;
                }
            });
            setProducts(productsData);
            setInventoryMap(invMap);
        } catch {
            toast.error('Failed to load catalog data.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { loadPageData(); }, []);

    const filteredProducts = products.filter((p) => {
        const q = searchQuery.toLowerCase();
        return (p.productName || '').toLowerCase().includes(q)
            || (p.barcode || '').toLowerCase().includes(q)
            || (p.clientName || '').toLowerCase().includes(q)
            || (p.clientId || '').toLowerCase().includes(q);
    });

    const openDetails = (product: ProductData) => {
        setSelectedProduct(product);
        setDetailsOpen(true);
    };

    const openEdit = () => {
        if (!selectedProduct) return;
        setEditName(selectedProduct.productName);
        setEditMrp(String(selectedProduct.mrp));
        setEditImageUrl(selectedProduct.imageUrl || '');
        setEditOpen(true);
    };

    const openInventoryUpdate = () => {
        if (!selectedProduct) return;
        setInventoryBarcode(selectedProduct.barcode);
        setInventoryQuantity(inventoryMap[selectedProduct.barcode] ?? 0);
    };

    const handleUpdateProduct = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!selectedProduct) return;
        setUpdating(true);
        try {
            const form: ProductForm = {
                barcode: selectedProduct.barcode,
                clientId: selectedProduct.clientId,
                name: editName,
                mrp: parseFloat(editMrp),
                imageUrl: editImageUrl || undefined,
            };
            const updated = await productService.updateProduct(selectedProduct.barcode, form);
            setProducts((prev) => prev.map((p) => p.barcode === updated.barcode ? updated : p));
            setSelectedProduct(updated);
            setEditOpen(false);
            toast.success('Product updated.');
        } catch (err: any) {
            toast.error(err?.response?.data?.message || 'Failed to update product.');
        } finally {
            setUpdating(false);
        }
    };

    const handleUpdateInventory = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!inventoryBarcode.trim()) return;
        setInventoryUpdating(true);
        try {
            await inventoryService.updateSingleInventory({ barcode: inventoryBarcode, quantity: inventoryQuantity });
            toast.success('Inventory updated.');
            setInventoryBarcode('');
            setInventoryQuantity(0);
            loadPageData();
        } catch (err: any) {
            toast.error(err?.response?.data?.message || 'Failed to update inventory.');
        } finally {
            setInventoryUpdating(false);
        }
    };

    const handleCreateProduct = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!createBarcode || !createName || !createClientId || !createMrp) {
            toast.error('Please fill all required fields.');
            return;
        }
        setCreating(true);
        try {
            await productService.createSingleProduct({
                barcode: createBarcode,
                name: createName,
                clientId: createClientId,
                mrp: parseFloat(createMrp),
                imageUrl: createImageUrl || undefined,
            });
            toast.success('Product created.');
            setCreateOpen(false);
            setCreateBarcode(''); setCreateName(''); setCreateClientId(''); setCreateMrp(''); setCreateImageUrl('');
            loadPageData();
        } catch (err: any) {
            toast.error(err?.response?.data?.message || 'Failed to create product.');
        } finally {
            setCreating(false);
        }
    };

    const handleTsvUpload = async (e: ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        setTsvUploading(true);
        setTsvResult(null);
        try {
            const result = tsvMode === 'product'
                ? await productService.uploadProductTSV(file)
                : await inventoryService.uploadInventoryTsv(file);
            setTsvResult(result);
            if (result.importedCount > 0) toast.success(`${result.importedCount} rows imported.`);
            if (result.errorCount > 0) toast.error(`${result.errorCount} rows failed.`);
            loadPageData();
        } catch {
            toast.error('TSV upload failed.');
        } finally {
            setTsvUploading(false);
            if (tsvInputRef.current) tsvInputRef.current.value = '';
        }
    };

    const handleDownloadTemplate = () => {
        if (tsvMode === 'product') productService.downloadTemplate();
        else inventoryService.downloadTemplate();
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-gray-900">Products</h1>
                    <p className="text-sm text-gray-500">Manage products, inventory, and bulk uploads.</p>
                </div>
                <div className="flex items-center gap-3">
                    <button onClick={() => setCreateOpen(true)} className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors shadow-sm">
                        <Plus className="h-4 w-4" /> Add Product
                    </button>
                    <button onClick={() => { setTsvOpen(true); setTsvResult(null); }} className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors shadow-sm">
                        <Upload className="h-4 w-4" /> Upload TSV
                    </button>
                </div>
            </div>

            <div className="relative max-w-md">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                <input
                    type="text"
                    placeholder="Search by name, barcode, or client..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full rounded-lg border border-gray-300 pl-10 pr-4 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
            </div>

            {loading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {Array.from({ length: 8 }).map((_, i) => (
                        <div key={i} className="rounded-xl border border-gray-200 bg-white p-4 shadow-sm animate-pulse space-y-3">
                            <div className="aspect-square bg-gray-200 rounded-lg" />
                            <div className="h-4 bg-gray-200 rounded w-3/4" />
                            <div className="h-3 bg-gray-200 rounded w-1/2" />
                            <div className="h-3 bg-gray-200 rounded w-1/3" />
                        </div>
                    ))}
                </div>
            ) : filteredProducts.length === 0 ? (
                <div className="text-center py-20 text-gray-400">
                    <Package className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                    <p className="font-medium">{searchQuery ? 'No products match your search.' : 'No products available.'}</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {filteredProducts.map((product) => {
                        const stock = inventoryMap[product.barcode] ?? 0;
                        return (
                            <div key={product.barcode} className="rounded-xl border border-gray-200 bg-white shadow-sm hover:shadow-md transition-shadow overflow-hidden group cursor-pointer" onClick={() => openDetails(product)}>
                                <div className="aspect-square bg-gray-100 flex items-center justify-center overflow-hidden">
                                    {product.imageUrl ? (
                                        <img src={product.imageUrl} alt={product.productName} className="w-full h-full object-cover group-hover:scale-105 transition-transform" />
                                    ) : (
                                        <ImageOff className="h-10 w-10 text-gray-300" />
                                    )}
                                </div>
                                <div className="p-4 space-y-2">
                                    <h3 className="font-semibold text-gray-900 truncate">{product.productName}</h3>
                                    <p className="text-sm font-bold text-indigo-600">₹{product.mrp}</p>
                                    <div className="flex items-center justify-between text-xs">
                                        <span className={`inline-flex items-center rounded-md px-2 py-0.5 font-medium ${stock > 0 ? 'bg-green-50 text-green-700' : 'bg-rose-50 text-rose-700'}`}>
                                            Inventory: {stock}
                                        </span>
                                        <span className="text-indigo-600 font-medium opacity-0 group-hover:opacity-100 transition-opacity">View Details →</span>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}

            {/* Product Details Dialog */}
            {detailsOpen && selectedProduct && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => setDetailsOpen(false)}>
                    <div className="w-full max-w-lg rounded-xl bg-white shadow-xl relative overflow-hidden" onClick={(e) => e.stopPropagation()}>
                        <button onClick={() => setDetailsOpen(false)} className="absolute right-4 top-4 z-10 text-gray-400 hover:text-gray-600">
                            <X className="h-5 w-5" />
                        </button>
                        <div className="h-48 bg-gray-100 flex items-center justify-center">
                            {selectedProduct.imageUrl ? (
                                <img src={selectedProduct.imageUrl} alt={selectedProduct.productName} className="w-full h-full object-contain" />
                            ) : (
                                <ImageOff className="h-12 w-12 text-gray-300" />
                            )}
                        </div>
                        <div className="p-6 space-y-4">
                            <div>
                                <h2 className="text-xl font-bold text-gray-900">{selectedProduct.productName}</h2>
                                <p className="text-sm text-gray-500 font-mono">{selectedProduct.barcode}</p>
                            </div>
                            <div className="grid grid-cols-2 gap-4 text-sm">
                                <div><span className="text-gray-500">MRP</span><p className="font-semibold text-gray-900">₹{selectedProduct.mrp}</p></div>
                                <div><span className="text-gray-500">Client</span><p className="font-semibold text-gray-900">{selectedProduct.clientName} <span className="text-xs text-gray-400">({selectedProduct.clientId})</span></p></div>
                                <div><span className="text-gray-500">Inventory</span><p className="font-semibold text-gray-900">{inventoryMap[selectedProduct.barcode] ?? 0}</p></div>
                            </div>
                            <div className="flex gap-3 pt-2">
                                <button onClick={openEdit} className="flex-1 flex items-center justify-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-semibold text-gray-700 hover:bg-gray-50 transition-colors">
                                    <Edit3 className="h-4 w-4" /> Edit Product
                                </button>
                                <button onClick={openInventoryUpdate} className="flex-1 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors">
                                    Update Inventory
                                </button>
                            </div>
                            {inventoryBarcode && inventoryBarcode === selectedProduct.barcode && (
                                <form onSubmit={handleUpdateInventory} className="border-t border-gray-100 pt-4 space-y-3">
                                    <h4 className="text-sm font-semibold text-gray-700">Update Inventory</h4>
                                    <div className="flex gap-3 items-end">
                                        <div className="flex-1">
                                            <label className="block text-xs font-semibold text-gray-500 uppercase">Quantity</label>
                                            <input type="number" min="0" value={inventoryQuantity} onChange={(e) => setInventoryQuantity(parseInt(e.target.value) || 0)} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                                        </div>
                                        <button type="submit" disabled={inventoryUpdating} className="flex items-center gap-1 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400">
                                            {inventoryUpdating && <Loader2 className="h-4 w-4 animate-spin" />}
                                            Save
                                        </button>
                                    </div>
                                </form>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* Edit Product Modal */}
            {editOpen && selectedProduct && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => setEditOpen(false)}>
                    <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl relative" onClick={(e) => e.stopPropagation()}>
                        <button onClick={() => setEditOpen(false)} className="absolute right-4 top-4 text-gray-400 hover:text-gray-600">
                            <X className="h-5 w-5" />
                        </button>
                        <h3 className="text-lg font-bold text-gray-900 mb-4">Edit Product</h3>
                        <form onSubmit={handleUpdateProduct} className="space-y-3">
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Barcode</label>
                                <input type="text" value={selectedProduct.barcode} disabled className="mt-1 block w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-400 cursor-not-allowed" />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Client</label>
                                <input type="text" value={`${selectedProduct.clientName} (${selectedProduct.clientId})`} disabled className="mt-1 block w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-400 cursor-not-allowed" />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Product Name *</label>
                                <input type="text" value={editName} onChange={(e) => setEditName(e.target.value)} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">MRP (₹) *</label>
                                <input type="number" step="0.01" value={editMrp} onChange={(e) => setEditMrp(e.target.value)} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Image URL</label>
                                <input type="text" value={editImageUrl} onChange={(e) => setEditImageUrl(e.target.value)} className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" />
                            </div>
                            <div className="flex justify-end gap-3 pt-2">
                                <button type="button" onClick={() => setEditOpen(false)} className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">Cancel</button>
                                <button type="submit" disabled={updating} className="flex items-center gap-1 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400">
                                    {updating && <Loader2 className="h-4 w-4 animate-spin" />}
                                    Save Changes
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Create Product Modal */}
            {createOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => setCreateOpen(false)}>
                    <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl relative" onClick={(e) => e.stopPropagation()}>
                        <button onClick={() => setCreateOpen(false)} className="absolute right-4 top-4 text-gray-400 hover:text-gray-600">
                            <X className="h-5 w-5" />
                        </button>
                        <h3 className="text-lg font-bold text-gray-900 mb-4">Add Product</h3>
                        <form onSubmit={handleCreateProduct} className="space-y-3">
                            <div><label className="block text-xs font-semibold text-gray-500 uppercase">Barcode *</label><input type="text" value={createBarcode} onChange={(e) => setCreateBarcode(e.target.value)} placeholder="e.g. SKU001" className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" /></div>
                            <div><label className="block text-xs font-semibold text-gray-500 uppercase">Product Name *</label><input type="text" value={createName} onChange={(e) => setCreateName(e.target.value)} placeholder="e.g. socks" className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" /></div>
                            <div><label className="block text-xs font-semibold text-gray-500 uppercase">Client ID *</label><input type="text" value={createClientId} onChange={(e) => setCreateClientId(e.target.value)} placeholder="e.g. CL000001" className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" /></div>
                            <div><label className="block text-xs font-semibold text-gray-500 uppercase">MRP (₹) *</label><input type="number" step="0.01" value={createMrp} onChange={(e) => setCreateMrp(e.target.value)} placeholder="99.00" className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" /></div>
                            <div><label className="block text-xs font-semibold text-gray-500 uppercase">Image URL</label><input type="text" value={createImageUrl} onChange={(e) => setCreateImageUrl(e.target.value)} placeholder="http://..." className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500" /></div>
                            <div className="flex justify-end gap-3 pt-2">
                                <button type="button" onClick={() => setCreateOpen(false)} className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50">Cancel</button>
                                <button type="submit" disabled={creating} className="flex items-center gap-1 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400">
                                    {creating && <Loader2 className="h-4 w-4 animate-spin" />}
                                    Save Product
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* TSV Upload Modal */}
            {tsvOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={() => setTsvOpen(false)}>
                    <div className="w-full max-w-2xl rounded-xl bg-white p-6 shadow-xl relative max-h-[85vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
                        <button onClick={() => setTsvOpen(false)} className="absolute right-4 top-4 text-gray-400 hover:text-gray-600">
                            <X className="h-5 w-5" />
                        </button>
                        <h3 className="text-lg font-bold text-gray-900 mb-4">
                            Upload {tsvMode === 'product' ? 'Product' : 'Inventory'} TSV
                        </h3>

                        <div className="flex gap-2 mb-4">
                            <button type="button" onClick={() => { setTsvMode('product'); setTsvResult(null); }} className={`px-3 py-1.5 text-sm rounded-md font-medium transition-colors ${tsvMode === 'product' ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>Products</button>
                            <button type="button" onClick={() => { setTsvMode('inventory'); setTsvResult(null); }} className={`px-3 py-1.5 text-sm rounded-md font-medium transition-colors ${tsvMode === 'inventory' ? 'bg-indigo-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>Inventory</button>
                        </div>

                        <div className="flex items-center gap-3 mb-4">
                            <button onClick={handleDownloadTemplate} className="flex items-center gap-2 rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors">
                                <Download className="h-4 w-4" /> Template
                            </button>
                            <label className="flex cursor-pointer items-center gap-2 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors">
                                {tsvUploading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Upload className="h-4 w-4" />}
                                {tsvUploading ? 'Uploading...' : 'Choose File'}
                                <input ref={tsvInputRef} type="file" accept=".tsv, .txt" onChange={handleTsvUpload} disabled={tsvUploading} className="hidden" />
                            </label>
                        </div>

                        {tsvResult && (
                            <div className="space-y-4 border-t border-gray-200 pt-4">
                                <div className="flex gap-3 text-sm">
                                    <span className="flex items-center gap-1 text-green-700 bg-green-50 rounded-lg px-3 py-1.5 font-medium"><CheckCircle2 className="h-4 w-4" /> {tsvResult.importedCount} imported</span>
                                    <span className="flex items-center gap-1 text-rose-700 bg-rose-50 rounded-lg px-3 py-1.5 font-medium"><XCircle className="h-4 w-4" /> {tsvResult.errorCount} failed</span>
                                    <span className="text-gray-500 bg-gray-50 rounded-lg px-3 py-1.5">{tsvResult.totalRows} total</span>
                                </div>

                                {tsvResult.errors.length > 0 && (
                                    <div>
                                        <h4 className="text-sm font-semibold text-gray-700 mb-2">Failed Rows</h4>
                                        <div className="space-y-2 max-h-48 overflow-y-auto">
                                            {tsvResult.errors.map((err, idx) => (
                                                <div key={idx} className="bg-rose-50 border border-rose-200 rounded-lg px-4 py-3 text-sm">
                                                    <div className="flex justify-between items-start">
                                                        <span className="font-semibold text-rose-800">Row {err.row}</span>
                                                        <span className="text-rose-600">{err.error}</span>
                                                    </div>
                                                    <div className="text-xs text-rose-500 mt-1 space-x-2">
                                                        <span>barcode: {err.barcode || '-'}</span>
                                                        {'clientId' in err && <span>clientId: {(err as RowError).clientId || '-'}</span>}
                                                        {'name' in err && <span>name: {(err as RowError).name || '-'}</span>}
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {tsvResult.importedCount > 0 && (
                                    <div>
                                        <h4 className="text-sm font-semibold text-gray-700 mb-2">Imported Rows</h4>
                                        <div className="text-xs text-gray-500 space-y-1 max-h-48 overflow-y-auto">
                                            {tsvResult.imported.map((item: any, idx) => (
                                                <div key={idx} className="flex gap-2">
                                                    <span className="font-mono">{item.barcode || item.productBarcode}</span>
                                                    <span>{item.productName}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {!tsvResult && (
                            <p className="text-sm text-gray-400 text-center py-8">Select a TSV file to upload. Download the template for the correct format.</p>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
