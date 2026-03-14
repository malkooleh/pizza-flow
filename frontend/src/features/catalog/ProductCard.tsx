import type { Product } from '@/api/catalogService/model';
import { Plus } from 'lucide-react';

interface ProductCardProps {
    product: Product;
    onAdd: (product: Product) => void;
}

export const ProductCard = ({ product, onAdd }: ProductCardProps) => {
    return (
        <article className="bg-[#1a1a1a] rounded-xl overflow-hidden border border-white/5 flex flex-col h-full transition-all duration-300 hover:-translate-y-1 hover:shadow-xl hover:border-white/10 group">
            <div className="relative aspect-video bg-[#2a2a2a] overflow-hidden">
                {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-600 bg-gray-800">
                        No Image
                    </div>
                )}
            </div>
            <div className="p-4 flex flex-col flex-grow">
                <div className="flex justify-between items-start mb-2">
                    <h3 className="font-heading text-lg font-semibold text-gray-100">{product.name}</h3>
                    <span className="font-bold text-[hsl(var(--primary-hue),90%,55%)] text-lg">${(product.price || 0).toFixed(2)}</span>
                </div>
                <p className="text-gray-400 text-sm mb-4 flex-grow">{product.description}</p>

                <div className="mt-auto">
                    <button
                        className="w-full p-2 bg-[hsl(var(--primary-hue),90%,55%)] text-white border-none rounded-md font-semibold transition-colors duration-200 hover:bg-[hsl(var(--primary-hue),90%,45%)] disabled:bg-gray-500 disabled:cursor-not-allowed"
                        onClick={() => onAdd(product)}
                        disabled={!product.available}
                    >
                        {product.available ? (
                            <span className="flex items-center justify-center gap-2">
                                <Plus size={18} /> Add to Order
                            </span>
                        ) : (
                            'Sold Out'
                        )}
                    </button>
                </div>
            </div>
        </article>
    );
};
