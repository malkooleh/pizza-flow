import { useState } from 'react';
import {
    useGetAllItems,
    useCreateInventoryItem,
    useUpdateInventoryItem,
    useDeleteInventoryItem,
    getGetAllItemsQueryKey
} from '@/api/inventoryService/inventory-controller/inventory-controller';
import type { 
    InventoryItemResponse, 
    CreateInventoryItemRequest, 
    UpdateInventoryItemRequest 
} from '@/api/inventoryService/model';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Plus, Edit, Trash2, Box, AlertTriangle, CheckCircle } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { Skeleton } from '@/components/ui/skeleton';
import { useToast } from '@/hooks/use-toast';

const InventorySkeleton = () => (
    <div className="rounded-xl border border-gray-800 bg-gray-900/50 backdrop-blur-sm overflow-hidden shadow-xl">
        <div className="bg-gray-800/30 p-4 border-b border-gray-800 flex gap-4">
            {[...Array(5)].map((_, i) => <Skeleton key={i} className="h-4 w-24" />)}
        </div>
        <div className="p-4 space-y-4">
            {[...Array(5)].map((_, i) => (
                <div key={i} className="flex gap-4 items-center">
                    <div className="space-y-2 flex-1">
                        <Skeleton className="h-4 w-48" />
                    </div>
                    <Skeleton className="h-4 w-16" />
                    <Skeleton className="h-6 w-12 rounded" />
                    <Skeleton className="h-6 w-24 rounded-full" />
                    <div className="flex gap-2">
                        <Skeleton className="h-8 w-8 rounded" />
                        <Skeleton className="h-8 w-8 rounded" />
                    </div>
                </div>
            ))}
        </div>
    </div>
);

export const InventoryManagementPage = () => {
    const { toast } = useToast();
    const { data: response, isLoading } = useGetAllItems();
    const items = response?.data || [];
    
    const queryClient = useQueryClient();
    const createMutation = useCreateInventoryItem();
    const updateMutation = useUpdateInventoryItem();
    const deleteMutation = useDeleteInventoryItem();

    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [editingItem, setEditingItem] = useState<InventoryItemResponse | null>(null);

    // Form for create/update. We use CreateInventoryItemRequest as a base.
    const { register, handleSubmit, reset, setValue } = useForm<CreateInventoryItemRequest>();

    const openEditDialog = (item: InventoryItemResponse) => {
        setEditingItem(item);
        setValue('productName', item.productName || '');
        setValue('quantity', item.quantity || 0);
        setValue('unit', item.unit || 'g');
        setIsDialogOpen(true);
    };

    const openCreateDialog = () => {
        setEditingItem(null);
        reset({
            productName: '',
            quantity: 0,
            unit: 'g'
        });
        setIsDialogOpen(true);
    };

    const onSubmit = (data: CreateInventoryItemRequest) => {
        const payload = {
            ...data,
            quantity: Number(data.quantity)
        };

        if (editingItem && editingItem.id) {
            const updatePayload: UpdateInventoryItemRequest = {
                productName: payload.productName,
                quantity: payload.quantity,
                unit: payload.unit
            };
            
            updateMutation.mutate({ id: editingItem.id, data: updatePayload }, {
                onSuccess: () => {
                    queryClient.invalidateQueries({ queryKey: getGetAllItemsQueryKey() });
                    setIsDialogOpen(false);
                    toast({
                        title: "Inventory Updated",
                        description: `${payload.productName} stock level has been adjusted.`,
                    });
                },
                onError: () => {
                    toast({
                        title: "Update Failed",
                        description: "Could not update inventory item. Please try again.",
                        variant: "destructive",
                    });
                }
            });
        } else {
            createMutation.mutate({ data: payload }, {
                onSuccess: () => {
                    queryClient.invalidateQueries({ queryKey: getGetAllItemsQueryKey() });
                    setIsDialogOpen(false);
                    toast({
                        title: "Item Added",
                        description: `${payload.productName} has been added to inventory.`,
                    });
                },
                onError: () => {
                    toast({
                        title: "Creation Failed",
                        description: "Could not add item to inventory. Please try again.",
                        variant: "destructive",
                    });
                }
            });
        }
    };

    const handleDelete = (id: number) => {
        if (window.confirm('Are you sure you want to delete this inventory item?')) {
            deleteMutation.mutate({ id }, {
                onSuccess: () => {
                    queryClient.invalidateQueries({ queryKey: getGetAllItemsQueryKey() });
                    toast({
                        title: "Item Deleted",
                        description: "The item has been removed from inventory.",
                    });
                },
                onError: () => {
                    toast({
                        title: "Deletion Failed",
                        description: "Could not delete the item.",
                        variant: "destructive",
                    });
                }
            });
        }
    };

    const getStockStatus = (quantity: number) => {
        if (quantity === 0) return { label: 'Out of Stock', color: 'text-red-500', icon: AlertTriangle };
        if (quantity < 100) return { label: 'Low Stock', color: 'text-amber-500', icon: AlertTriangle };
        return { label: 'In Stock', color: 'text-green-500', icon: CheckCircle };
    };

    if (isLoading) return (
        <div className="space-y-8">
            <div className="flex justify-between items-center">
                <div className="space-y-2">
                    <Skeleton className="h-10 w-64" />
                    <Skeleton className="h-4 w-96" />
                </div>
                <Skeleton className="h-10 w-32" />
            </div>
            <InventorySkeleton />
        </div>
    );

    return (
        <div className="space-y-8">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold font-heading text-gray-100">Inventory Management</h1>
                    <p className="text-gray-400">Track and manage ingredient levels and supply units.</p>
                </div>
                
                <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                    <DialogTrigger asChild>
                        <Button onClick={openCreateDialog} className="bg-primary hover:bg-primary/90 text-primary-foreground font-bold shadow-lg shadow-primary/20">
                            <Plus className="mr-2 h-4 w-4" /> Add Item
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px] bg-gray-950 border-gray-800 text-gray-100 shadow-2xl">
                        <DialogHeader>
                            <DialogTitle className="text-xl font-heading flex items-center gap-2">
                                <Box className="h-5 w-5 text-primary" />
                                {editingItem ? 'Edit Inventory Item' : 'Add New Inventory Item'}
                            </DialogTitle>
                        </DialogHeader>
                        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
                            <div className="grid gap-2">
                                <Label htmlFor="productName">Product/Ingredient Name</Label>
                                <Input 
                                    id="productName" 
                                    placeholder="e.g. Pepperoni, Mozzarella" 
                                    {...register('productName', { required: true })} 
                                    className="bg-gray-900 border-gray-800 focus:ring-primary focus:border-primary" 
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="grid gap-2">
                                    <Label htmlFor="quantity">Quantity</Label>
                                    <Input 
                                        id="quantity" 
                                        type="number" 
                                        {...register('quantity', { required: true, min: 0 })} 
                                        className="bg-gray-900 border-gray-800 focus:ring-primary focus:border-primary" 
                                    />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="unit">Unit</Label>
                                    <select 
                                        {...register('unit', { required: true })} 
                                        className="flex h-10 w-full rounded-md border border-gray-800 bg-gray-900 px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 text-gray-100"
                                    >
                                        <option value="g">Grams (g)</option>
                                        <option value="kg">Kilograms (kg)</option>
                                        <option value="ml">Milliliters (ml)</option>
                                        <option value="l">Liters (l)</option>
                                        <option value="pcs">Pieces (pcs)</option>
                                    </select>
                                </div>
                            </div>
                            <div className="pt-6 flex justify-end space-x-3">
                                <Button type="button" variant="ghost" onClick={() => setIsDialogOpen(false)} className="text-gray-400 hover:text-white hover:bg-gray-800">
                                    Cancel
                                </Button>
                                <Button type="submit" className="bg-primary hover:bg-primary/90 text-primary-foreground font-bold min-w-[100px]" disabled={createMutation.isPending || updateMutation.isPending}>
                                    {editingItem ? 'Save Changes' : 'Add to Inventory'}
                                </Button>
                            </div>
                        </form>
                    </DialogContent>
                </Dialog>
            </div>

            <div className="rounded-xl border border-gray-800 bg-gray-900/50 backdrop-blur-sm overflow-hidden shadow-xl">
                <Table>
                    <TableHeader className="bg-gray-800/30">
                        <TableRow className="border-gray-800 hover:bg-transparent">
                            <TableHead className="text-gray-400 font-semibold py-4">Item Name</TableHead>
                            <TableHead className="text-gray-400 font-semibold py-4">Current Stock</TableHead>
                            <TableHead className="text-gray-400 font-semibold py-4">Unit</TableHead>
                            <TableHead className="text-gray-400 font-semibold py-4">Status</TableHead>
                            <TableHead className="text-gray-400 font-semibold py-4 text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {items.length === 0 ? (
                            <TableRow className="border-gray-800">
                                <TableCell colSpan={5} className="text-center text-gray-500 py-12">
                                    <div className="flex flex-col items-center gap-2">
                                        <Box className="h-12 w-12 text-gray-700 mb-2" />
                                        <p>No inventory items found.</p>
                                        <Button variant="link" onClick={openCreateDialog} className="text-primary p-0">Add your first item</Button>
                                    </div>
                                </TableCell>
                            </TableRow>
                        ) : (
                            items.map((item) => {
                                const status = getStockStatus(item.quantity || 0);
                                const StatusIcon = status.icon;
                                
                                return (
                                    <TableRow key={item.id} className="border-gray-800 hover:bg-gray-800/40 transition-all duration-200">
                                        <TableCell className="font-medium text-gray-100 py-4">
                                            {item.productName}
                                        </TableCell>
                                        <TableCell className="text-gray-200 font-mono">
                                            {item.quantity?.toLocaleString()}
                                        </TableCell>
                                        <TableCell>
                                            <span className="text-gray-400 text-xs px-2 py-0.5 rounded bg-gray-800 border border-gray-700 uppercase tracking-tighter">
                                                {item.unit}
                                            </span>
                                        </TableCell>
                                        <TableCell>
                                            <div className={`flex items-center gap-1.5 ${status.color} font-medium text-sm`}>
                                                <StatusIcon className="h-4 w-4" />
                                                {status.label}
                                            </div>
                                        </TableCell>
                                        <TableCell className="text-right py-4">
                                            <div className="flex justify-end space-x-2">
                                                <Button 
                                                    variant="ghost" 
                                                    size="icon" 
                                                    onClick={() => openEditDialog(item)} 
                                                    className="h-8 w-8 text-gray-400 hover:text-primary hover:bg-primary/10 transition-colors"
                                                    title="Edit Item"
                                                >
                                                    <Edit className="h-4 w-4" />
                                                </Button>
                                                <Button 
                                                    variant="ghost" 
                                                    size="icon" 
                                                    onClick={() => item.id && handleDelete(item.id)} 
                                                    className="h-8 w-8 text-gray-500 hover:text-red-500 hover:bg-red-500/10 transition-colors"
                                                    title="Delete Item"
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                );
                            })
                        )}
                    </TableBody>
                </Table>
            </div>
            
            {/* Quick Stats Overlay (Optional future enhancement) */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 pt-4">
                <div className="p-4 rounded-lg bg-gray-900/40 border border-gray-800">
                    <div className="text-xs text-gray-500 uppercase font-bold mb-1">Total Items</div>
                    <div className="text-2xl font-bold text-gray-100">{items.length}</div>
                </div>
                <div className="p-4 rounded-lg bg-gray-900/40 border border-gray-800">
                    <div className="text-xs text-gray-500 uppercase font-bold mb-1">Low Stock Alerts</div>
                    <div className="text-2xl font-bold text-amber-500">
                        {items.filter(i => (i.quantity || 0) > 0 && (i.quantity || 0) < 100).length}
                    </div>
                </div>
                <div className="p-4 rounded-lg bg-gray-900/40 border border-gray-800">
                    <div className="text-xs text-gray-500 uppercase font-bold mb-1">Out of Stock</div>
                    <div className="text-2xl font-bold text-red-500">
                        {items.filter(i => (i.quantity || 0) === 0).length}
                    </div>
                </div>
            </div>
        </div>
    );
};
