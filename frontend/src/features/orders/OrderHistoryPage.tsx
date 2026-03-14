import { useGetCustomerOrders } from '@/api/orderService/order-controller/order-controller';
import { useAuth } from '@/context/AuthContext';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';
import { Clock, Package, ChevronRight } from 'lucide-react';
import { format } from 'date-fns';

export const OrderHistoryPage = () => {
    const { user } = useAuth();
    // Assuming user ID is available in the user object
    const customerId = user?.id || '';

    const { data: ordersResponse, isLoading, error } = useGetCustomerOrders(customerId, {
        query: {
            enabled: !!customerId,
        }
    });

    const orders = ordersResponse?.data || [];

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[hsl(var(--primary-hue),90%,55%)]"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="text-center py-12">
                <p className="text-red-500">Failed to load orders. Please try again later.</p>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto space-y-8 py-8">
            <div className="flex flex-col gap-2">
                <h1 className="text-3xl font-bold tracking-tight">My Orders</h1>
                <p className="text-gray-400">View and track your previous orders</p>
            </div>

            {orders.length === 0 ? (
                <Card className="bg-[hsl(220,15%,12%)] border-gray-800">
                    <CardContent className="flex flex-col items-center justify-center py-16 gap-4">
                        <Package className="w-16 h-16 text-gray-600" />
                        <div className="text-center">
                            <h3 className="text-xl font-semibold">No orders yet</h3>
                            <p className="text-gray-400">When you place an order, it will appear here.</p>
                        </div>
                        <Button asChild className="mt-4 bg-[hsl(var(--primary-hue),90%,55%)] hover:bg-[hsl(var(--primary-hue),90%,45%)]">
                            <Link to="/">Order Now</Link>
                        </Button>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid gap-4">
                    {orders.sort((a, b) => new Date(b.createdAt || '').getTime() - new Date(a.createdAt || '').getTime()).map((order) => (
                        <Card key={order.id} className="bg-[hsl(220,15%,12%)] border-gray-800 hover:border-gray-700 transition-colors">
                            <CardContent className="p-0">
                                <Link to={`/orders/${order.id}`} className="flex items-center justify-between p-6">
                                    <div className="flex items-center gap-6">
                                        <div className="w-12 h-12 rounded-full bg-gray-800 flex items-center justify-center text-[hsl(var(--primary-hue),90%,55%)]">
                                            <Clock size={24} />
                                        </div>
                                        <div className="space-y-1">
                                            <div className="flex items-center gap-3">
                                                <span className="font-bold text-lg">Order #{order.id?.slice(0, 8)}</span>
                                                <Badge className={getStatusColor(order.status)}>
                                                    {order.status}
                                                </Badge>
                                            </div>
                                            <div className="text-sm text-gray-400 flex items-center gap-4">
                                                <span>{order.createdAt ? format(new Date(order.createdAt), 'MMM dd, yyyy HH:mm') : 'Unknown Date'}</span>
                                                <span className="w-1 h-1 bg-gray-600 rounded-full"></span>
                                                <span className="font-medium text-white">${order.totalAmount?.toFixed(2)}</span>
                                            </div>
                                        </div>
                                    </div>
                                    <ChevronRight className="text-gray-600" />
                                </Link>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}
        </div>
    );
};

const getStatusColor = (status: string | undefined) => {
    switch (status) {
        case 'PENDING': return 'bg-yellow-500/10 text-yellow-500 border-yellow-500/20';
        case 'CONFIRMED':
        case 'PAID': return 'bg-blue-500/10 text-blue-500 border-blue-500/20';
        case 'PREPARING': return 'bg-purple-500/10 text-purple-500 border-purple-500/20';
        case 'READY_FOR_DELIVERY':
        case 'OUT_FOR_DELIVERY': return 'bg-orange-500/10 text-orange-500 border-orange-500/20';
        case 'DELIVERED':
        case 'COMPLETED': return 'bg-green-500/10 text-green-500 border-green-500/20';
        case 'CANCELLED': return 'bg-red-500/10 text-red-500 border-red-500/20';
        default: return 'bg-gray-500/10 text-gray-500 border-gray-500/20';
    }
};
