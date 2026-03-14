import { useOrderTracking } from '@/hooks/useOrderTracking';
import { useParams } from 'react-router-dom';
import { CheckCircle, Clock, Truck, ChefHat } from 'lucide-react';

const STEPS = [
    { id: 'PENDING', label: 'Order Received', icon: Clock },
    { id: 'PREPARING', label: 'Kitchen Preparing', icon: ChefHat },
    { id: 'OUT_FOR_DELIVERY', label: 'Out for Delivery', icon: Truck },
    { id: 'COMPLETED', label: 'Delivered', icon: CheckCircle },
];

export const OrderTracker = () => {
    const { orderId } = useParams();
    const { status, error } = useOrderTracking(orderId);

    const currentStepIndex = STEPS.findIndex(s => s.id === status);
    // Fallback if status is not in STEPS (e.g. PAID, READY_FOR_DELIVERY)
    const effectiveStepIndex = currentStepIndex === -1 ? 0 : currentStepIndex;

    return (
        <div className="max-w-3xl mx-auto py-12">
            <h1 className="text-3xl font-bold mb-8 text-center">Order Status</h1>
            <div className="text-center text-gray-400 mb-12">Order ID: {orderId}</div>

            <div className="relative flex justify-between">
                {/* Progress Bar Background */}
                <div className="absolute top-6 left-0 w-full h-1 bg-gray-700 -z-10"></div>

                {/* Active Progress Bar */}
                <div
                    className="absolute top-6 left-0 h-1 bg-[hsl(var(--primary-hue),90%,55%)] -z-10 transition-all duration-500"
                    style={{ width: `${(effectiveStepIndex / (STEPS.length - 1)) * 100}%` }}
                ></div>

                {STEPS.map((step, index) => {
                    const Icon = step.icon;
                    const isActive = index <= effectiveStepIndex;

                    return (
                        <div key={step.id} className="flex flex-col items-center gap-4 bg-[hsl(220,15%,10%)] px-2">
                            <div className={`
                                w-12 h-12 rounded-full flex items-center justify-center border-2 transition-colors duration-300
                                ${isActive ? 'border-[hsl(var(--primary-hue),90%,55%)] bg-[hsl(var(--primary-hue),90%,55%)] text-white' : 'border-gray-600 bg-gray-800 text-gray-400'}
                            `}>
                                <Icon size={24} />
                            </div>
                            <span className={`text-sm font-medium ${isActive ? 'text-white' : 'text-gray-500'}`}>
                                {step.label}
                            </span>
                        </div>
                    );
                })}
            </div>

            {error && (
                <div className="mt-8 text-center text-yellow-500 text-sm">
                    {error}
                </div>
            )}

            {status === 'CANCELLED' && (
                <div className="mt-12 p-4 bg-red-500/10 border border-red-500/20 text-red-500 text-center rounded-lg">
                    This order has been cancelled.
                </div>
            )}
        </div>
    );
};
