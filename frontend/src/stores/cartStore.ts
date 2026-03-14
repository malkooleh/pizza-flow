import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { Product } from '@/api/catalogService/model';

export interface CartItem extends Product {
    quantity: number;
}

interface CartState {
    items: CartItem[];
    isOpen: boolean;
    addItem: (product: Product) => void;
    removeItem: (productId: string) => void;
    updateQuantity: (productId: string, quantity: number) => void;
    clearCart: () => void;
    toggleCart: (isOpen?: boolean) => void;
    total: number;
}

export const useCartStore = create<CartState>()(
    persist(
        (set, get) => ({
            items: [],
            isOpen: false,
            total: 0,

            addItem: (product) => {
                set((state) => {
                    const existingItem = state.items.find((item) => item.id === product.id);
                    let updatedItems: CartItem[];

                    if (existingItem) {
                        updatedItems = state.items.map((item) =>
                            item.id === product.id
                                ? { ...item, quantity: item.quantity + 1 }
                                : item
                        );
                    } else {
                        updatedItems = [...state.items, { ...product, quantity: 1 }];
                    }
                    return {
                        items: updatedItems,
                        total: calculateTotal(updatedItems),
                    };
                });
            },

            removeItem: (productId) => {
                set((state) => {
                    const updatedItems = state.items.filter((item) => item.id !== productId);
                    return {
                        items: updatedItems,
                        total: calculateTotal(updatedItems),
                    };
                });
            },

            updateQuantity: (productId, quantity) => {
                if (quantity <= 0) {
                    get().removeItem(productId);
                    return;
                }
                set((state) => {
                    const updatedItems = state.items.map((item) =>
                        item.id === productId ? { ...item, quantity } : item
                    );
                    return {
                        items: updatedItems,
                        total: calculateTotal(updatedItems),
                    };
                });
            },

            clearCart: () => {
                set({ items: [], total: 0 });
            },

            toggleCart: (isOpen) => {
                set((state) => ({ isOpen: isOpen ?? !state.isOpen }));
            },
        }),
        {
            name: 'pizza-flow-cart',
        }
    )
);

const calculateTotal = (items: CartItem[]) => {
    return items.reduce((acc, item) => acc + (item.price || 0) * item.quantity, 0);
};
