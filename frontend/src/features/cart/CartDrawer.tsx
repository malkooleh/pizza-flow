import { useCartStore } from '@/stores/cartStore';
import { X, Minus, Plus, ShoppingBag } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const CartDrawer = () => {
    const { items, isOpen, toggleCart, removeItem, updateQuantity, total } = useCartStore();
    const navigate = useNavigate();

    if (!isOpen) return null;

    const handleCheckout = () => {
        toggleCart(false);
        navigate('/checkout');
    };

    return (
        <div className="fixed inset-0 z-50 flex justify-end">
            {/* Backdrop */}
            <div
                className="absolute inset-0 bg-black/50 backdrop-blur-sm transition-opacity"
                onClick={() => toggleCart(false)}
            />

            {/* Drawer Panel */}
            <div className="relative w-full max-w-md bg-[hsl(220,15%,15%)] h-full shadow-2xl p-6 flex flex-col transform transition-transform animate-slide-in-right">
                <div className="flex items-center justify-between mb-8">
                    <h2 className="text-2xl font-heading font-bold flex items-center gap-2">
                        <ShoppingBag /> Your Cart
                    </h2>
                    <button
                        onClick={() => toggleCart(false)}
                        className="p-2 hover:bg-white/10 rounded-full transition-colors"
                    >
                        <X size={24} />
                    </button>
                </div>

                <div className="flex-grow overflow-y-auto space-y-6">
                    {items.length === 0 ? (
                        <div className="text-center text-gray-500 mt-20">
                            <p>Your cart is empty.</p>
                            <button
                                onClick={() => toggleCart(false)}
                                className="mt-4 text-[hsl(var(--primary-hue),90%,55%)] hover:underline"
                            >
                                Go back to menu
                            </button>
                        </div>
                    ) : (
                        items.map((item) => (
                            <div key={item.id} className="flex gap-4 p-4 bg-[hsl(220,15%,10%)] rounded-lg border border-white/5">
                                <div className="w-20 h-20 bg-gray-800 rounded-md overflow-hidden flex-shrink-0">
                                    {item.imageUrl && <img src={item.imageUrl} alt={item.name} className="w-full h-full object-cover" />}
                                </div>
                                <div className="flex-grow">
                                    <h3 className="font-bold">{item.name}</h3>
                                    <p className="text-sm text-gray-400 mb-2">${(item.price || 0).toFixed(2)}</p>

                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-3 bg-[hsl(220,15%,20%)] rounded-full px-2 py-1">
                                            <button
                                                onClick={() => item.id && updateQuantity(item.id, item.quantity - 1)}
                                                className="p-1 hover:text-[hsl(var(--primary-hue),90%,55%)]"
                                            >
                                                <Minus size={14} />
                                            </button>
                                            <span className="text-sm font-medium w-4 text-center">{item.quantity}</span>
                                            <button
                                                onClick={() => item.id && updateQuantity(item.id, item.quantity + 1)}
                                                className="p-1 hover:text-[hsl(var(--primary-hue),90%,55%)]"
                                            >
                                                <Plus size={14} />
                                            </button>
                                        </div>
                                        <button
                                            onClick={() => item.id && removeItem(item.id)}
                                            className="text-xs text-red-400 hover:text-red-300 transition-colors"
                                        >
                                            Remove
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>

                {items.length > 0 && (
                    <div className="mt-6 border-t border-white/10 pt-6 space-y-4">
                        <div className="flex justify-between text-xl font-bold">
                            <span>Total</span>
                            <span>${total.toFixed(2)}</span>
                        </div>
                        <button
                            onClick={handleCheckout}
                            className="w-full py-4 bg-[hsl(var(--primary-hue),90%,55%)] text-white font-bold rounded-lg hover:bg-[hsl(var(--primary-hue),90%,45%)] transition-colors shadow-lg"
                        >
                            Proceed to Checkout
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};
