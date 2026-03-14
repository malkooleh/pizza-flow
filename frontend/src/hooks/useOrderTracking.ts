import { useEffect, useState } from 'react';

type OrderStatus = 'PENDING' | 'CONFIRMED' | 'PAID' | 'PREPARING' | 'READY_FOR_DELIVERY' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'COMPLETED' | 'CANCELLED';

interface OrderUpdate {
    orderId: string;
    status: OrderStatus;
    message?: string;
}

export const useOrderTracking = (orderId: string | undefined) => {
    const [status, setStatus] = useState<OrderStatus>('PENDING');
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!orderId) return;

        // In a real app, this would be an environment variable
        // We assume the API Gateway is on port 8080
        const eventSource = new EventSource(`/api/v1/orders/${orderId}/track`);

        eventSource.onmessage = (event) => {
            console.log('Received generic SSE event:', event.data);
        };

        eventSource.addEventListener('INIT', (event: MessageEvent) => {
            const data = JSON.parse(event.data);
            console.log('Tracking initialized:', data);
        });

        eventSource.addEventListener('ORDER_UPDATE', (event: MessageEvent) => {
            const data: OrderUpdate = JSON.parse(event.data);
            console.log('Order status updated:', data);
            setStatus(data.status);
        });

        eventSource.onerror = (err) => {
            console.error('EventSource failed:', err);
            setError('Connection lost. Attempting to reconnect...');
            eventSource.close();
        };

        return () => {
            eventSource.close();
        };
    }, [orderId]);

    return { status, error };
};
