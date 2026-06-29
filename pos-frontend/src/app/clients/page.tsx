'use client';

import { useEffect, useState, useCallback, useRef } from 'react';
import { clientService, ClientData } from '@/services/client.service';
import { toast } from 'sonner';
import { Plus, Edit2, Search, X, Loader2, Users } from 'lucide-react';

const PAGE_SIZE = 10;
const DEBOUNCE_MS = 300;

function useDebounce<T>(value: T, delay: number): T {
    const [debounced, setDebounced] = useState(value);
    useEffect(() => {
        const id = setTimeout(() => setDebounced(value), delay);
        return () => clearTimeout(id);
    }, [value, delay]);
    return debounced;
}

export default function ClientManagementPage() {
    const [clients, setClients] = useState<ClientData[]>([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [searchQuery, setSearchQuery] = useState('');
    const debouncedSearch = useDebounce(searchQuery, DEBOUNCE_MS);
    const searchInputRef = useRef<HTMLInputElement>(null);

    const [modalOpen, setModalOpen] = useState(false);
    const [isEditMode, setIsEditMode] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [editingClient, setEditingClient] = useState<ClientData | null>(null);

    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [nameError, setNameError] = useState('');
    const [emailError, setEmailError] = useState('');

    const loadClients = useCallback(async (currentPage: number, search?: string) => {
        setLoading(true);
        try {
            const data = search && search.trim()
                ? await clientService.filterClientsByName(search.trim(), currentPage, PAGE_SIZE)
                : await clientService.getAllClients(currentPage, PAGE_SIZE);
            setClients(data.content);
            setTotalPages(data.totalPages);
            setTotalElements(data.totalElements);
            setPage(data.page);
        } catch {
            toast.error('Failed to load clients.');
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        setPage(0);
        loadClients(0, debouncedSearch);
    }, [debouncedSearch, loadClients]);

    const handlePageChange = (newPage: number) => {
        if (newPage < 0 || newPage >= totalPages) return;
        loadClients(newPage, debouncedSearch);
    };

    const clearSearch = () => {
        setSearchQuery('');
        searchInputRef.current?.focus();
    };

    const validateForm = (): boolean => {
        let valid = true;
        setNameError('');
        setEmailError('');

        if (!name.trim()) {
            setNameError('Client name is required.');
            valid = false;
        }
        if (!email.trim()) {
            setEmailError('Email is required.');
            valid = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
            setEmailError('Invalid email format.');
            valid = false;
        }
        return valid;
    };

    const resetForm = () => {
        setName('');
        setEmail('');
        setNameError('');
        setEmailError('');
        setEditingClient(null);
        setIsEditMode(false);
    };

    const handleOpenAdd = () => {
        resetForm();
        setModalOpen(true);
    };

    const handleOpenEdit = (client: ClientData) => {
        resetForm();
        setIsEditMode(true);
        setEditingClient(client);
        setName(client.name);
        setEmail(client.email);
        setModalOpen(true);
    };

    const handleCloseModal = () => {
        setModalOpen(false);
        resetForm();
    };

    const handleFormSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validateForm()) return;

        setSubmitting(true);
        try {
            if (isEditMode && editingClient) {
                await clientService.updateClient(editingClient.clientId, { name: name.trim(), email: email.trim() });
                toast.success('Client updated successfully.');
            } else {
                await clientService.createClient({ name: name.trim(), email: email.trim() });
                toast.success('Client created successfully.');
            }
            handleCloseModal();
            loadClients(debouncedSearch ? 0 : page, debouncedSearch);
        } catch (err: any) {
            const msg = err?.response?.data?.message || 'An unexpected error occurred.';
            toast.error(msg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="space-y-6">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-gray-900">Clients</h1>
                    <p className="text-sm text-gray-500">Manage wholesale client accounts.</p>
                </div>
                <button
                    onClick={handleOpenAdd}
                    className="flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 transition-colors shadow-sm"
                >
                    <Plus className="h-4 w-4" /> Add Client
                </button>
            </div>

            <div className="relative max-w-md">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-400" />
                <input
                    ref={searchInputRef}
                    type="text"
                    placeholder="Search by client name..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full rounded-lg border border-gray-300 pl-10 pr-10 py-2 text-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                />
                {searchQuery && (
                    <button onClick={clearSearch} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                        <X className="h-4 w-4" />
                    </button>
                )}
            </div>

            {totalElements > 0 && !loading && (
                <p className="text-sm text-gray-500">
                    {totalElements} client{totalElements !== 1 ? 's' : ''}
                    {debouncedSearch && ` matching &ldquo;${debouncedSearch}&rdquo;`}
                </p>
            )}

            <div className="overflow-hidden rounded-xl border border-gray-200 bg-white shadow-sm">
                {loading ? (
                    <div className="divide-y divide-gray-100">
                        {Array.from({ length: 5 }).map((_, i) => (
                            <div key={i} className="flex items-center gap-4 px-6 py-4 animate-pulse">
                                <div className="h-4 bg-gray-200 rounded w-24" />
                                <div className="h-4 bg-gray-200 rounded w-40 flex-1" />
                                <div className="h-4 bg-gray-200 rounded w-48 flex-1" />
                                <div className="h-8 bg-gray-200 rounded w-16" />
                            </div>
                        ))}
                    </div>
                ) : clients.length === 0 ? (
                    <div className="text-center py-16 text-gray-400">
                        <Users className="h-12 w-12 mx-auto mb-3 text-gray-300" />
                        <p className="font-medium">
                            {debouncedSearch ? 'No clients match your search.' : 'No clients found.'}
                        </p>
                        {!debouncedSearch && (
                            <button onClick={handleOpenAdd} className="mt-3 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
                                Add your first client
                            </button>
                        )}
                    </div>
                ) : (
                    <table className="w-full border-collapse text-left text-sm text-gray-500">
                        <thead className="bg-gray-50 text-xs font-semibold uppercase text-gray-700 border-b border-gray-200">
                        <tr>
                            <th className="px-6 py-4">Client ID</th>
                            <th className="px-6 py-4">Client Name</th>
                            <th className="px-6 py-4">Email</th>
                            <th className="px-6 py-4 text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                        {clients.map((client) => (
                            <tr key={client.clientId} className="hover:bg-gray-50/75 transition-colors">
                                <td className="px-6 py-4 font-mono text-xs font-medium text-gray-700">{client.clientId}</td>
                                <td className="px-6 py-4 font-medium text-gray-900">{client.name}</td>
                                <td className="px-6 py-4 text-slate-600">{client.email}</td>
                                <td className="px-6 py-4 text-right">
                                    <button
                                        onClick={() => handleOpenEdit(client)}
                                        className="inline-flex items-center gap-1 rounded-md px-2.5 py-1 text-xs font-semibold text-indigo-600 hover:bg-indigo-50 transition-colors"
                                    >
                                        <Edit2 className="h-3 w-3" /> Edit
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                )}
            </div>

            {totalPages > 1 && !loading && (
                <div className="flex items-center justify-between">
                    <p className="text-sm text-gray-500">
                        Page {page + 1} of {totalPages}
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

            {modalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4" onClick={handleCloseModal}>
                    <div className="w-full max-w-md rounded-xl bg-white p-6 shadow-xl relative" onClick={(e) => e.stopPropagation()}>
                        <button onClick={handleCloseModal} className="absolute right-4 top-4 text-gray-400 hover:text-gray-600">
                            <X className="h-5 w-5" />
                        </button>
                        <h3 className="text-lg font-bold text-gray-900 mb-4">
                            {isEditMode ? 'Edit Client' : 'Add Client'}
                        </h3>

                        <form onSubmit={handleFormSubmit} className="space-y-4">
                            {isEditMode && editingClient && (
                                <div>
                                    <label className="block text-xs font-semibold text-gray-500 uppercase">Client ID</label>
                                    <input
                                        type="text"
                                        value={editingClient.clientId}
                                        disabled
                                        className="mt-1 block w-full rounded-md border border-gray-200 bg-gray-50 px-3 py-2 text-sm text-gray-400 cursor-not-allowed"
                                    />
                                </div>
                            )}

                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Client Name *</label>
                                <input
                                    type="text"
                                    value={name}
                                    onChange={(e) => { setName(e.target.value); setNameError(''); }}
                                    placeholder="e.g. ABC Traders"
                                    className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 ${
                                        nameError ? 'border-red-400 bg-red-50' : 'border-gray-300'
                                    }`}
                                />
                                {nameError && <p className="mt-1 text-xs text-red-500">{nameError}</p>}
                            </div>

                            <div>
                                <label className="block text-xs font-semibold text-gray-500 uppercase">Email *</label>
                                <input
                                    type="email"
                                    value={email}
                                    onChange={(e) => { setEmail(e.target.value); setEmailError(''); }}
                                    placeholder="e.g. abc@example.com"
                                    className={`mt-1 block w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-indigo-500 ${
                                        emailError ? 'border-red-400 bg-red-50' : 'border-gray-300'
                                    }`}
                                />
                                {emailError && <p className="mt-1 text-xs text-red-500">{emailError}</p>}
                            </div>

                            <div className="flex justify-end gap-3 pt-2">
                                <button
                                    type="button"
                                    onClick={handleCloseModal}
                                    className="rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={submitting}
                                    className="flex items-center gap-1.5 rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-700 disabled:bg-indigo-400"
                                >
                                    {submitting && <Loader2 className="h-4 w-4 animate-spin" />}
                                    {isEditMode ? 'Save Changes' : 'Create Client'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
