import { createBrowserRouter } from 'react-router-dom';
import App from '@/App';
import { HomePage } from '@/pages/HomePage';
import { CheckoutPage } from '@/features/checkout/CheckoutPage';
import { OrderTracker } from '@/features/tracking/OrderTracker';
import { AdminLayout } from '@/components/layout/AdminLayout';
import { DashboardPage } from '@/features/admin/DashboardPage';
import { AdminOrderPage } from '@/features/admin/AdminOrderPage';
import { MenuManagementPage } from '@/features/admin/MenuManagementPage';
import { InventoryManagementPage } from '@/features/admin/InventoryManagementPage';
import { OrderHistoryPage } from '@/features/orders/OrderHistoryPage';
import { BookingPage } from '@/features/bookings/BookingPage';

export const router = createBrowserRouter([
    {
        path: '/',
        element: <App />,
        children: [
            {
                index: true,
                element: <HomePage />
            },
            {
                path: 'checkout',
                element: <CheckoutPage />
            },
            {
                path: 'orders',
                element: <OrderHistoryPage />
            },
            {
                path: 'orders/:orderId',
                element: <OrderTracker />
            },
            {
                path: 'bookings',
                element: <BookingPage />
            },
        ]
    },
    {
        path: '/admin',
        element: <AdminLayout />,
        children: [
            {
                index: true,
                element: <DashboardPage />
            },
            {
                path: 'orders',
                element: <AdminOrderPage />
            },
            {
                path: 'menu',
                element: <MenuManagementPage />
            },
            {
                path: 'inventory',
                element: <InventoryManagementPage />
            },
            {
                path: 'settings',
                element: <div>Settings (Coming Soon)</div>
            }
        ]
    }
]);
