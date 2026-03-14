import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useCartStore } from '@/stores/cartStore';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';

import { useCreateOrder } from '@/api/orderService/order-controller/order-controller';

const checkoutSchema = z.object({
    fullName: z.string().min(3, 'Name is required'),
    email: z.string().email('Invalid email'),
    address: z.string().min(5, 'Address is too short'),
    city: z.string().min(2, 'City is required'),
    state: z.string().min(2, 'State is required'),
    zipCode: z.string().min(3, 'Zip Code is required'),
    country: z.string().min(2, 'Country is required'),
    cardNumber: z.string().regex(/^\d{16}$/, 'Invalid card number (16 digits)'),
    expiry: z.string().regex(/^(0[1-9]|1[0-2])\/\d{2}$/, 'MM/YY'),
    cvc: z.string().regex(/^\d{3}$/, '3 digits'),
});

type CheckoutForm = z.infer<typeof checkoutSchema>;

export const CheckoutPage = () => {
    const { items, total, clearCart } = useCartStore();
    const { user } = useAuth();
    const navigate = useNavigate();
    const { mutateAsync: createOrder } = useCreateOrder();

    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<CheckoutForm>({
        resolver: zodResolver(checkoutSchema),
        defaultValues: {
            fullName: user ? `${user.firstName || ''} ${user.lastName || ''}`.trim() : '',
            email: user?.email || '',
            state: 'NY', // Defaults for demo
            country: 'USA'
        }
    });

    const onSubmit = async (data: CheckoutForm) => {
        if (!user?.id) {
            alert('Please login to place an order.');
            return;
        }

        try {
            const orderPayload = {
                customerId: user.id,
                items: items.map(item => ({
                    productId: item.id || '',
                    quantity: item.quantity,
                    unitPrice: item.price || 0
                })),
                deliveryAddress: {
                    street: data.address,
                    city: data.city,
                    state: data.state,
                    zipCode: data.zipCode,
                    country: data.country
                },
                longitude: 0, // Placeholder
                latitude: 0   // Placeholder
            };

            const response = await createOrder({ data: orderPayload });
            const orderId = response.data.id;

            clearCart();
            navigate(`/orders/${orderId}`);
        } catch (error) {
            console.error('Order failed', error);
            alert('Failed to place order. Check if all services are running.');
        }
    };

    if (items.length === 0) {
        return <div className="text-center py-20">Your cart is empty. <a href="/" className="text-orange-500">Go back to menu</a></div>;
    }

    return (
        <div className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-12">
            <div className="space-y-8">
                <h2 className="text-3xl font-heading font-bold">Checkout</h2>
                <form id="checkout-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                    <div className="space-y-2">
                        <label className="text-sm font-medium">Full Name</label>
                        <input {...register('fullName')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                        {errors.fullName && <p className="text-red-400 text-sm">{errors.fullName.message}</p>}
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium">Email</label>
                        <input {...register('email')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                        {errors.email && <p className="text-red-400 text-sm">{errors.email.message}</p>}
                    </div>

                    <h3 className="text-xl font-bold pt-4">Delivery Address</h3>
                    <div className="space-y-2">
                        <label className="text-sm font-medium">Street Address</label>
                        <input {...register('address')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                        {errors.address && <p className="text-red-400 text-sm">{errors.address.message}</p>}
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <label className="text-sm font-medium">City</label>
                            <input {...register('city')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                            {errors.city && <p className="text-red-400 text-sm">{errors.city.message}</p>}
                        </div>
                        <div className="space-y-2">
                            <label className="text-sm font-medium">State</label>
                            <input {...register('state')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                            {errors.state && <p className="text-red-400 text-sm">{errors.state.message}</p>}
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <label className="text-sm font-medium">Zip Code</label>
                            <input {...register('zipCode')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                            {errors.zipCode && <p className="text-red-400 text-sm">{errors.zipCode.message}</p>}
                        </div>
                        <div className="space-y-2">
                            <label className="text-sm font-medium">Country</label>
                            <input {...register('country')} className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                            {errors.country && <p className="text-red-400 text-sm">{errors.country.message}</p>}
                        </div>
                    </div>

                    <h3 className="text-xl font-bold pt-4">Payment Details</h3>
                    <div className="space-y-2">
                        <label className="text-sm font-medium">Card Number</label>
                        <input {...register('cardNumber')} placeholder="0000 0000 0000 0000" className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                        {errors.cardNumber && <p className="text-red-400 text-sm">{errors.cardNumber.message}</p>}
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                        <input {...register('expiry')} placeholder="MM/YY" className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                        <input {...register('cvc')} placeholder="CVC" className="w-full p-3 bg-white/5 border border-white/10 rounded-lg focus:ring-2 focus:ring-[hsl(var(--primary-hue),90%,55%)] outline-none transition-all" />
                    </div>
                </form>
            </div>

            <div className="bg-[hsl(220,15%,15%)] p-8 rounded-xl h-fit sticky top-24 border border-white/5">
                <h3 className="text-xl font-bold mb-6">Order Summary</h3>
                <div className="space-y-4 mb-6">
                    {items.map(item => (
                        <div key={item.id} className="flex justify-between text-sm">
                            <span>{item.quantity}x {item.name}</span>
                            <span>${((item.price || 0) * item.quantity).toFixed(2)}</span>
                        </div>
                    ))}
                </div>
                <div className="border-t border-white/10 pt-4 flex justify-between text-xl font-bold mb-8">
                    <span>Total</span>
                    <span>${total.toFixed(2)}</span>
                </div>
                <button
                    type="submit"
                    form="checkout-form"
                    disabled={isSubmitting}
                    className="w-full py-4 bg-[hsl(var(--primary-hue),90%,55%)] text-white font-bold rounded-lg hover:bg-[hsl(var(--primary-hue),90%,45%)] transition-colors shadow-lg disabled:opacity-50"
                >
                    {isSubmitting ? 'Processing...' : 'Place Order'}
                </button>
            </div>
        </div>
    );
};
