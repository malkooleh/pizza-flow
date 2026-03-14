import { useGetAllOrders, useUpdateStatus } from '@/api/orderService/order-controller/order-controller';
import { OrderResponseStatus } from '@/api/orderService/model/orderResponseStatus';
import type { OrderResponse } from '@/api/orderService/model/orderResponse';
import { useQueryClient } from '@tanstack/react-query';
import { useWebSocket } from '@/hooks/useWebSocket';
import type { AxiosResponse } from 'axios';
import { Skeleton } from '@/components/ui/skeleton';
import { useToast } from '@/hooks/use-toast';

const ORDER_STATUSES = Object.values(OrderResponseStatus);

const OrderSkeleton = () => (
    <div className="bg-white/5 border border-white/5 rounded-xl overflow-hidden">
        <div className="p-4 border-b border-white/5 space-y-4">
            {[...Array(5)].map((_, i) => (
                <div key={i} className="flex gap-4">
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-4 w-32" />
                    <Skeleton className="h-6 w-20 rounded-full" />
                    <Skeleton className="h-4 w-24" />
                    <Skeleton className="h-4 w-16" />
                    <Skeleton className="h-4 w-32 ml-auto" />
                </div>
            ))}
        </div>
    </div>
);

const getStatusColor = (status: string | undefined) => {
    if (!status) return 'bg-gray-500/20 text-gray-500';
    switch (status.toUpperCase()) {
        case 'PENDING': return 'bg-amber-500/20 text-amber-500';
        case 'CONFIRMED': return 'bg-blue-500/20 text-blue-500';
        case 'PAID': return 'bg-green-500/20 text-green-500';
        case 'PREPARING': return 'bg-purple-500/20 text-purple-500';
        case 'READY_FOR_DELIVERY': return 'bg-cyan-500/20 text-cyan-500';
        case 'OUT_FOR_DELIVERY': return 'bg-orange-500/20 text-orange-500';
        case 'DELIVERED':
        case 'COMPLETED': return 'bg-green-500/20 text-green-500';
        case 'CANCELLED': return 'bg-red-500/20 text-red-500';
        default: return 'bg-gray-500/20 text-gray-500';
    }
};

export const AdminOrderPage = () => {
    const queryClient = useQueryClient();
    const { toast } = useToast();
    
    // Real-time updates: Invalidate query when any order update is received
    useWebSocket('/topic/orders', (data) => {
        console.log('Real-time order update received:', data);
        queryClient.invalidateQueries({ queryKey: [`/api/v1/orders`] });
        toast({
            title: "Real-time Update",
            description: `Order ${data.orderId?.split('-')[0]} status changed to ${data.status}`,
        });
    });

    const { data: ordersResponse, isLoading, error } = useGetAllOrders();
    const orders = ordersResponse?.data;

    const { mutate: updateStatus } = useUpdateStatus({
        mutation: {
            onMutate: async ({ id, data }) => {
                await queryClient.cancelQueries({ queryKey: [`/api/v1/orders`] });
                const previousOrders = queryClient.getQueryData<AxiosResponse<OrderResponse[]>>([`/api/v1/orders`]);

                if (previousOrders) {
                    queryClient.setQueryData([`/api/v1/orders`], {
                        ...previousOrders,
                        data: previousOrders.data.map(order => 
                            order.id === id ? { ...order, status: data.status } : order
                        )
                    });
                }

                return { previousOrders };
            },
            onSuccess: (_, { id, data }) => {
                toast({
                    title: "Success",
                    description: `Order ${id.split('-')[0]} updated to ${data.status}`,
                });
            },
            onError: (err: Error, _variables: unknown, context: { previousOrders: AxiosResponse<OrderResponse[]> | undefined } | undefined) => {
                if (context?.previousOrders) {
                    queryClient.setQueryData([`/api/v1/orders`], context.previousOrders);
                }
                toast({
                    title: "Update Failed",
                    description: "Failed to update order status. Please try again.",
                    variant: "destructive",
                });
                console.error('Failed to update status:', err);
            },
            onSettled: () => {
                queryClient.invalidateQueries({ queryKey: [`/api/v1/orders`] });
            }
        }
    });

    if (isLoading) return (
        <div className="p-8 text-white">
            <header className="flex justify-between items-center mb-8">
                <Skeleton className="h-10 w-64" />
                <Skeleton className="h-8 w-32" />
            </header>
            <OrderSkeleton />
        </div>
    );
    
    if (error) return (
        <div className="flex flex-col gap-4 justify-center items-center h-[300px] text-red-500">
            <span className="text-xl font-bold">Error loading orders</span>
            <p className="text-white/60">Failed to communicate with the service.</p>
        </div>
    );

    const handleStatusUpdate = (orderId: string, status: string) => {
        updateStatus({ id: orderId, data: { status: status as OrderResponseStatus } });
    };


    return (
        <div className="p-8 text-white">
            <header className="flex justify-between items-center mb-8">
                <h1 className="text-3xl font-bold">Order Management</h1>
                <div className="bg-white/5 py-2 px-4 rounded-lg text-sm">
                    Total Orders: {orders?.length || 0}
                </div>
            </header>

            <div className="bg-white/5 border border-white/5 rounded-xl overflow-hidden">
                <table className="w-full border-collapse text-left">
                    <thead>
                        <tr>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Order ID</th>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Customer</th>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Status</th>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Items</th>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Total</th>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Created At</th>
                            <th className="bg-white/5 p-4 font-semibold text-sm uppercase tracking-wider text-white/60">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {orders?.map((order: OrderResponse) => (
                            <tr key={order.id} className="hover:bg-white/5 transition-colors">
                                <td className="p-4 border-b border-white/5 text-[0.95rem] font-mono text-white/60">{order.id?.split('-')[0]}...</td>
                                <td className="p-4 border-b border-white/5 text-[0.95rem]">{order.customerId?.split('-')[0]}...</td>
                                <td className="p-4 border-b border-white/5 text-[0.95rem]">
                                    <span className={`px-3 py-1 rounded-full text-xs font-semibold uppercase ${getStatusColor(order.status)}`}>
                                        {order.status}
                                    </span>
                                </td>
                                <td className="p-4 border-b border-white/5 text-[0.95rem]">{order.items?.length || 0} items</td>
                                <td className="p-4 border-b border-white/5 text-[0.95rem]">${(order.totalAmount || 0).toFixed(2)}</td>
                                <td className="p-4 border-b border-white/5 text-[0.95rem]">{order.createdAt ? new Date(order.createdAt).toLocaleString() : 'N/A'}</td>
                                <td className="p-4 border-b border-white/5 text-[0.95rem]">
                                    <select
                                        className="bg-white/5 border border-white/10 text-white py-1.5 px-2.5 rounded-md text-sm cursor-pointer outline-none focus:border-orange-500 transition-colors"
                                        value={order.status}
                                        onChange={(e) => order.id && handleStatusUpdate(order.id, e.target.value)}
                                    >
                                        {ORDER_STATUSES.map(status => (
                                            <option key={status} value={status} className="bg-gray-800">{status}</option>
                                        ))}
                                    </select>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
