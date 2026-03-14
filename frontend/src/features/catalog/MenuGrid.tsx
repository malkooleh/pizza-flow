import { useGetAllProducts } from '@/api/catalogService/catalog-controller/catalog-controller';
import type { Product } from '@/api/catalogService/model';
import { ProductCard } from './ProductCard';

export const MenuGrid = () => {
    // We fetch all products for the customer app MVP
    const { data: productsResponse, isLoading, error } = useGetAllProducts();
    const products = productsResponse?.data;

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
        console.log('Added to cart:', product);
        // TODO: Connect to Cart Store
    };

    return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {products?.map((product) => (
                <ProductCard key={product.id} product={product} onAdd={handleAddToCart} />
            ))}
        </div>
    );
};
