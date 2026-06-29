'use client';

import { useEffect, useState } from 'react';
import { productService, ProductData } from '@/services/product.service';
import { inventoryService } from '@/services/inventory.service';
import { orderService } from '@/services/order.service';
import { toast } from 'sonner';
import { Plus, Trash2, ShoppingBag, Loader2, AlertTriangle, CheckCircle } from 'lucide-react';

interface CartItem extends ProductData {
    quantity: number;
}

export default function CreateOrderPage() {
    const [products, setProducts] = useState<ProductData[]>([]);
    const [inventoryMap, setInventoryMap] = useState<Record<string, number>>({});
    const [cart, setCart] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    // Form inputs
    const [customerName, setCustomerName] = useState('');
    const [email, setEmail] = useState('');
    const [selectedBarcode, setSelectedBarcode] = useState('');

    const loadCatalogData = async () => {
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
        } catch (err) {
            toast.error('Failed to sync master catalog values');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadCatalogData();
    }, []);

    const handleAddItemToCart = () => {
        if (!selectedBarcode) return;

        const matchedProduct = products.find((p) => p.barcode === selectedBarcode);
        if (!matchedProduct) return;

        const stockAvailable = inventoryMap[selectedBarcode] ?? 0;

        // Warn if trying to add an item that has 0 available stock in the catalog lookup
        if (stockAvailable <= 0) {
            toast.warning('Note: This item currently registers as out of stock in warehouse.');
        }

        const existingIndex = cart.findIndex((item) => item.barcode === selectedBarcode);
        if (existingIndex > -1) {
            const updatedCart = [...cart];
            updatedCart[existingIndex].quantity += 1;
            setCart(updatedCart);
        } else {
            setCart([...cart, { ...matchedProduct, quantity: 1 }]);
        }
        setSelectedBarcode('');
    };

    const handleUpdateQuantity = (index: number, newQty: number) => {
        if (newQty < 1) return;
        const updatedCart = [...cart];
        updatedCart[index].quantity = newQty;
        setCart(updatedCart);
    };

    const handleRemoveFromCart = (index: number) => {
        setCart(cart.filter((_, i) => i !== index));
    };

    const grandTotal = cart.reduce((sum, item) => sum + item.mrp * item.quantity, 0);

    const handleOrderSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!customerName.trim() || !email.trim()) {
            toast.error('Please complete mandatory customer parameters.');
            return;
        }

        if (cart.length === 0) {
            toast.error('Your checkout cart remains empty.');
            return;
        }

        setSubmitting(true);
        try {
            const payload = {
                customerName,
                email,
                items: cart.map((item) => ({
                    barcode: item.barcode,
                    quantity: item.quantity,
                    sellingPrice: item.mrp
                }))
            };

            const result = await orderService.createOrder(payload);

            // ⚠️ Check backend errors payload array list first
            if (result.errors && result.errors.length > 0) {
                toast.error(`Order validation failed: ${result.errors.join(', ')}`);
                return;
            }

            // 🔄 Handle state returns machine cleanly:
            if (result.status === 'UNFULFILLED') {
                toast.warning(
                    `Order Created but UNFULFILLED (ID: ${result.orderId}). ` +
                    (result.message || 'Warehouse stock allocation failure. Go to History to retry.')
                );
            } else if (result.status === 'FULFILLED') {
                toast.success(result.message || `Order FULFILLED Successfully! ID: ${result.orderId}`);
            } else {
                toast.success(`Order registered successfully under state: ${result.status}`);
            }

            // Reset checkout cart board upon successful submission
            setCart([]);
            setCustomerName('');
            setEmail('');
            loadCatalogData(); // Re-sync live stock catalog levels across grid views instantly

        } catch (error: any) {
            const serverMsg = error.response?.data?.message || 'Transaction could not be completed on backend.';
            toast.error(serverMsg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight text-gray-900">Create Order Desk</h1>
                <p className="text-sm text-gray-500">Bill customers, compile real-time baskets, and verify pricing margins.</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div className="lg:col-span-2 space-y-6">

                    {/* Customer Profile Info */}
                    <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm space-y-4">
                        <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400">Customer Records</h3>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Customer Full Name *</label>
                                <input
                                    type="text"
                                    value={customerName}
                                    onChange={(e) => setCustomerName(e.target.value)}
                                    placeholder="e.g. Akshit"
                                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
                                />
                            </div>
                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Contact Email Address *</label>
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    placeholder="akshit@gmail.com"
                                    className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500"
                                />
                            </div>
                        </div>
                    </div>

                    {/* Selector Dropdown Frame */}
                    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm flex items-end gap-4">
                        <div className="flex-1">
                            <label className="block text-xs font-semibold text-gray-500 uppercase mb-1.5">Select Catalog Product</label>
                            <select
                                value={selectedBarcode}
                                onChange={(e) => setSelectedBarcode(e.target.value)}
                                disabled={loading}
                                className="block w-full rounded-md border border-gray-300 px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 bg-white text-gray-900 font-medium"
                            >
                                <option value="" className="text-gray-500">-- Choose target item SKU --</option>
                                {products.map((p, idx) => {
                                    // Ensure we fall back to standard field paths if keys are shifted
                                    const itemBarcode = p?.barcode || '';
                                    const itemName = p?.productName || 'Unnamed Product';
                                    const itemPrice = p?.mrp ?? 0;
                                    const stock = inventoryMap[itemBarcode] ?? 0;

                                    return (
                                        <option key={itemBarcode || idx} value={itemBarcode}>
                                            {itemName} [{itemBarcode}] — ₹{itemPrice} (Warehouse Stock: {stock})
                                        </option>
                                    );
                                })}
                            </select>
                        </div>
                        <button
                            type="button"
                            onClick={handleAddItemToCart}
                            className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 h-[38px] transition-colors shadow-sm w-full sm:w-auto"
                        >
                            Add Row Item
                        </button>
                    </div>

                    {/* Basket Checkout Table */}
                    <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
                        <table className="w-full border-collapse text-left text-sm text-gray-500">
                            <thead className="bg-gray-50 text-xs font-semibold uppercase text-gray-700 border-b border-gray-200">
                            <tr>
                                <th className="px-6 py-4">Item Catalog Details</th>
                                <th className="px-6 py-4 w-32">Units Ordered</th>
                                <th className="px-6 py-4">Stock Warning</th>
                                <th className="px-6 py-4">Row Subtotal</th>
                                <th className="px-6 py-4 text-right">Action</th>
                            </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-200">
                            {cart.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="px-6 py-12 text-center text-gray-400 font-medium">
                                        No active line items are loaded inside this checkout desk.
                                    </td>
                                </tr>
                            ) : (
                                cart.map((item, index) => {
                                    const currentStock = inventoryMap[item.barcode] ?? 0;
                                    const isInsufficient = item.quantity > currentStock;

                                    return (
                                        <tr key={item.barcode} className="hover:bg-gray-50/75">
                                            <td className="px-6 py-4">
                                                <div className="font-semibold text-gray-900">{item.productName}</div>
                                                <div className="text-xs font-mono text-gray-400">{item.barcode}</div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <input
                                                    type="number"
                                                    min="1"
                                                    value={item.quantity}
                                                    onChange={(e) => handleUpdateQuantity(index, parseInt(e.target.value) || 1)}
                                                    className="w-20 rounded border border-gray-300 px-2 py-1 text-sm focus:outline-none"
                                                />
                                            </td>
                                            <td className="px-6 py-4">
                                                {isInsufficient ? (
                                                    <span className="inline-flex items-center gap-1 text-xs text-amber-600 font-medium bg-amber-50 px-2 py-0.5 rounded">
                              <AlertTriangle className="h-3 w-3" /> Exceeds Stock ({currentStock})
                            </span>
                                                ) : (
                                                    <span className="inline-flex items-center gap-1 text-xs text-green-600 font-medium bg-green-50 px-2 py-0.5 rounded">
                              <CheckCircle className="h-3 w-3" /> Allocation Safe
                            </span>
                                                )}
                                            </td>
                                            <td className="px-6 py-4 font-bold text-gray-900">₹{(item.mrp * item.quantity).toFixed(2)}</td>
                                            <td className="px-6 py-4 text-right">
                                                <button
                                                    type="button"
                                                    onClick={() => handleRemoveFromCart(index)}
                                                    className="text-rose-500 hover:text-rose-700 p-1"
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </button>
                                            </td>
                                        </tr>
                                    );
                                })
                            )}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* SUMMARY CARD */}
                <div className="rounded-xl border border-gray-200 bg-white p-6 shadow-sm h-fit space-y-6">
                    <h3 className="text-xs font-bold uppercase tracking-wider text-gray-400">Invoice Ledger Summary</h3>
                    <div className="space-y-3 border-b border-gray-100 pb-4 text-sm text-gray-600">
                        <div className="flex justify-between">
                            <span>Unique Line Items</span>
                            <span className="font-semibold text-gray-900">{cart.length}</span>
                        </div>
                        <div className="flex justify-between">
                            <span>Gross Order Units</span>
                            <span className="font-semibold text-gray-900">{cart.reduce((s, i) => s + i.quantity, 0)} items</span>
                        </div>
                    </div>

                    <div className="flex justify-between items-baseline">
                        <span className="text-sm font-medium text-gray-900">Aggregate Total</span>
                        <span className="text-2xl font-extrabold text-indigo-600">₹{grandTotal.toFixed(2)}</span>
                    </div>

                    <button
                        type="button"
                        onClick={handleOrderSubmit}
                        disabled={submitting || cart.length === 0}
                        className="w-full flex items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-3 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400 shadow-md transition-colors"
                    >
                        {submitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <ShoppingBag className="h-4 w-4" />}
                        {submitting ? 'Processing Transaction...' : 'Complete Order Checkout'}
                    </button>
                </div>
            </div>
        </div>
    );
}