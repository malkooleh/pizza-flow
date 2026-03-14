import { useGetAllProducts } from '@/api/catalogService/catalog-controller/catalog-controller';
import type { Product } from '@/api/catalogService/model';
import { ProductCard } from './ProductCard';
import { useCartStore } from '@/stores/cartStore';

interface MenuGridProps {
    category: string;
}

export const MenuGrid = ({ category }: MenuGridProps) => {
    // We fetch all products for the customer app MVP
    const { data: productsResponse, isLoading, error } = useGetAllProducts();
    const addItem = useCartStore((state) => state.addItem);
    const toggleCart = useCartStore((state) => state.toggleCart);

    const products = productsResponse?.data;

    // Filter products by category if selected
    const filteredProducts = products?.filter((product) => {
        if (category === 'All') return true;
        // Basic matching logic: category names in UI (Pizzas, Drinks, Sides) 
        // usually match the backend category strings (PIZZA, DRINK, etc.)
        const normalizedProductCat = product.category?.toUpperCase();
        const normalizedUISelection = category.toUpperCase();
        
        if (normalizedUISelection.startsWith('PIZZA')) return normalizedProductCat === 'PIZZA';
        if (normalizedUISelection.startsWith('DRINK')) return normalizedProductCat === 'DRINK';
        if (normalizedUISelection.startsWith('SIDE')) return normalizedProductCat === 'SIDE';
        
        return true;
    });

    if (isLoading) {
        return (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {[...Array(4)].map((_, i) => (
                    <div key={i} className="h-96 bg-gray-800 rounded-lg animate-pulse" />
                ))}
            </div>
        );
    }

    if (error) {
        return <div className="text-red-500 text-center py-8">Failed to load menu. Please try again.</div>;
    }

    const handleAddToCart = (product: Product) => {
        addItem(product);
        toggleCart(true);
    };

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {filteredProducts?.map((product) => (
                <ProductCard key={product.id} product={product} onAdd={handleAddToCart} />
            ))}
            {filteredProducts?.length === 0 && (
                <div className="col-span-full text-center py-12 text-gray-500">
                    No items found in this category.
                </div>
            )}
        </div>
    );
};
