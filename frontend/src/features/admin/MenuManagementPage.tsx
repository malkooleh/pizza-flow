import { useState } from 'react';
import {
    useGetAllProducts,
    useCreateProduct,
    useUpdateProduct,
    useDeleteProduct,
    getGetAllProductsQueryKey
} from '@/api/catalogService/catalog-controller/catalog-controller';
import { type Product, ProductCategory, type ProductRequest } from '@/api/catalogService/model';
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Plus, Edit, Trash2 } from 'lucide-react';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useForm } from 'react-hook-form';
import { useQueryClient } from '@tanstack/react-query';
import { Skeleton } from '@/components/ui/skeleton';
import { useToast } from '@/hooks/use-toast';

const MenuSkeleton = () => (
    <div className="rounded-md border border-gray-800 bg-gray-900 overflow-hidden">
        <div className="bg-gray-800/50 p-4 border-b border-gray-800 flex gap-4">
            {[...Array(6)].map((_, i) => <Skeleton key={i} className="h-4 w-24" />)}
        </div>
        <div className="p-4 space-y-4">
            {[...Array(5)].map((_, i) => (
                <div key={i} className="flex gap-4 items-center">
                    <Skeleton className="h-12 w-12 rounded" />
                    <div className="space-y-2 flex-1">
                        <Skeleton className="h-4 w-32" />
                        <Skeleton className="h-3 w-48" />
                    </div>
                    <Skeleton className="h-6 w-20 rounded-full" />
                    <Skeleton className="h-4 w-16" />
                    <Skeleton className="h-6 w-16 rounded-full" />
                    <div className="flex gap-2">
                        <Skeleton className="h-8 w-8 rounded" />
                        <Skeleton className="h-8 w-8 rounded" />
                    </div>
                </div>
            ))}
        </div>
    </div>
);

export const MenuManagementPage = () => {
    const { toast } = useToast();
    const { data: response, isLoading } = useGetAllProducts();
    const products = response?.data || [];
    
    const queryClient = useQueryClient();
    const createMutation = useCreateProduct();
    const updateMutation = useUpdateProduct();
    const deleteMutation = useDeleteProduct();

    const [isDialogOpen, setIsDialogOpen] = useState(false);
    const [editingProduct, setEditingProduct] = useState<Product | null>(null);

    const { register, handleSubmit, reset, setValue } = useForm<ProductRequest>();

    const openEditDialog = (product: Product) => {
        setEditingProduct(product);
        setValue('name', product.name || '');
        setValue('description', product.description || '');
        setValue('price', product.price || 0);
        setValue('category', product.category as ProductCategory);
        setValue('imageUrl', product.imageUrl || '');
        setValue('available', product.available ?? true);
        setIsDialogOpen(true);
    };

    const openCreateDialog = () => {
        setEditingProduct(null);
        reset({
            name: '',
            description: '',
            price: 0,
            category: 'PIZZA',
            imageUrl: '',
            available: true,
            ingredients: []
        });
        setIsDialogOpen(true);
    };

    const onSubmit = (data: ProductRequest) => {
        const payload: ProductRequest = {
            ...data,
            price: Number(data.price),
            available: String(data.available) === 'true',
            ingredients: data.ingredients || []
        };

        if (editingProduct && editingProduct.id) {
            updateMutation.mutate({ id: editingProduct.id, data: payload }, {
                onSuccess: () => {
                    queryClient.invalidateQueries({ queryKey: getGetAllProductsQueryKey() });
                    setIsDialogOpen(false);
                    toast({
                        title: "Product Updated",
                        description: `${payload.name} has been updated successfully.`,
                    });
                },
                onError: () => {
                    toast({
                        title: "Update Failed",
                        description: "Could not update the product. Please try again.",
                        variant: "destructive",
                    });
                }
            });
        } else {
            createMutation.mutate({ data: payload }, {
                onSuccess: () => {
                    queryClient.invalidateQueries({ queryKey: getGetAllProductsQueryKey() });
                    setIsDialogOpen(false);
                    toast({
                        title: "Product Created",
                        description: `${payload.name} has been added to the catalog.`,
                    });
                },
                onError: () => {
                    toast({
                        title: "Creation Failed",
                        description: "Could not create the product. Please try again.",
                        variant: "destructive",
                    });
                }
            });
        }
    };

    const handleDelete = (id: string) => {
        if (window.confirm('Are you sure you want to delete this product?')) {
            deleteMutation.mutate({ id }, {
                onSuccess: () => {
                    queryClient.invalidateQueries({ queryKey: getGetAllProductsQueryKey() });
                    toast({
                        title: "Product Deleted",
                        description: "The product has been removed from the catalog.",
                    });
                },
                onError: () => {
                    toast({
                        title: "Deletion Failed",
                        description: "Could not delete the product.",
                        variant: "destructive",
                    });
                }
            });
        }
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
            <MenuSkeleton />
        </div>
    );

    return (
        <div className="space-y-8">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold font-heading">Menu Management</h1>
                    <p className="text-gray-400">View, add, edit, and remove products from the catalog.</p>
                </div>
                
                <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                    <DialogTrigger asChild>
                        <Button onClick={openCreateDialog} className="bg-primary hover:bg-primary/90 text-primary-foreground font-bold">
                            <Plus className="mr-2 h-4 w-4" /> Add Product
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px] bg-gray-900 border-gray-800 text-gray-100">
                        <DialogHeader>
                            <DialogTitle>{editingProduct ? 'Edit Product' : 'Add New Product'}</DialogTitle>
                        </DialogHeader>
                        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 pt-4">
                            <div className="grid gap-2">
                                <Label htmlFor="name">Name</Label>
                                <Input id="name" {...register('name', { required: true })} className="bg-gray-800 border-gray-700" />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="description">Description</Label>
                                <Input id="description" {...register('description')} className="bg-gray-800 border-gray-700" />
                            </div>
                            <div className="grid grid-cols-2 gap-4">
                                <div className="grid gap-2">
                                    <Label htmlFor="price">Price ($)</Label>
                                    <Input id="price" type="number" step="0.01" {...register('price', { required: true })} className="bg-gray-800 border-gray-700" />
                                </div>
                                <div className="grid gap-2">
                                    <Label htmlFor="category">Category</Label>
                                    <select 
                                        {...register('category', { required: true })} 
                                        className="flex h-10 w-full rounded-md border border-gray-700 bg-gray-800 px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 text-gray-100"
                                    >
                                        <option value="PIZZA">Pizza</option>
                                        <option value="SIDES">Sides</option>
                                        <option value="DRINK">Drink</option>
                                        <option value="DESSERT">Dessert</option>
                                    </select>
                                </div>
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="imageUrl">Image URL</Label>
                                <Input id="imageUrl" {...register('imageUrl')} className="bg-gray-800 border-gray-700" />
                            </div>
                            <div className="grid gap-2">
                                <Label htmlFor="available">Availability</Label>
                                <select 
                                    {...register('available')}
                                    className="flex h-10 w-full rounded-md border border-gray-700 bg-gray-800 px-3 py-2 text-sm text-gray-100"
                                >
                                    <option value="true">Available</option>
                                    <option value="false">Out of Stock</option>
                                </select>
                            </div>
                            <div className="pt-4 flex justify-end space-x-2">
                                <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)} className="border-gray-700 text-gray-300 hover:text-white">
                                    Cancel
                                </Button>
                                <Button type="submit" className="bg-primary hover:bg-primary/90 text-primary-foreground font-bold">
                                    {editingProduct ? 'Save Changes' : 'Create'}
                                </Button>
                            </div>
                        </form>
                    </DialogContent>
                </Dialog>
            </div>

            <div className="rounded-md border border-gray-800 bg-gray-900 overflow-hidden">
                <Table>
                    <TableHeader className="bg-gray-800/50">
                        <TableRow className="border-gray-800 hover:bg-transparent">
                            <TableHead className="text-gray-400">Image</TableHead>
                            <TableHead className="text-gray-400">Name</TableHead>
                            <TableHead className="text-gray-400">Category</TableHead>
                            <TableHead className="text-gray-400">Price</TableHead>
                            <TableHead className="text-gray-400">Status</TableHead>
                            <TableHead className="text-gray-400 text-right">Actions</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {products.length === 0 ? (
                            <TableRow className="border-gray-800">
                                <TableCell colSpan={6} className="text-center text-gray-500 py-8">
                                    No products found in the catalog.
                                </TableCell>
                            </TableRow>
                        ) : (
                            products.map((product) => (
                                <TableRow key={product.id} className="border-gray-800 hover:bg-gray-800/50 transition-colors">
                                    <TableCell>
                                        <div className="w-12 h-12 rounded overflow-hidden bg-gray-800">
                                            {product.imageUrl ? (
                                                <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
                                            ) : (
                                                <div className="w-full h-full flex items-center justify-center text-xs text-gray-600">No Img</div>
                                            )}
                                        </div>
                                    </TableCell>
                                    <TableCell className="font-medium text-gray-200">
                                        {product.name}
                                        <div className="text-xs text-gray-500 font-normal truncate max-w-xs">{product.description}</div>
                                    </TableCell>
                                    <TableCell>
                                        <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-800 text-gray-300">
                                            {product.category}
                                        </span>
                                    </TableCell>
                                    <TableCell className="text-gray-300">${product.price?.toFixed(2)}</TableCell>
                                    <TableCell>
                                        {product.available ? (
                                            <span className="inline-flex items-center space-x-1 text-green-500">
                                                <span className="w-2 h-2 rounded-full bg-green-500"></span>
                                                <span className="text-sm">Available</span>
                                            </span>
                                        ) : (
                                            <span className="inline-flex items-center space-x-1 text-red-500">
                                                <span className="w-2 h-2 rounded-full bg-red-500"></span>
                                                <span className="text-sm">Unavailable</span>
                                            </span>
                                        )}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <div className="flex justify-end space-x-2">
                                            <Button variant="outline" size="icon" onClick={() => openEditDialog(product)} className="h-8 w-8 border-gray-700 text-gray-400 hover:text-white hover:bg-gray-700">
                                                <Edit className="h-4 w-4" />
                                            </Button>
                                            <Button variant="outline" size="icon" onClick={() => product.id && handleDelete(product.id)} className="h-8 w-8 border-red-900/50 text-red-500 hover:text-red-400 hover:bg-red-900/20">
                                                <Trash2 className="h-4 w-4" />
                                            </Button>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    );
};
