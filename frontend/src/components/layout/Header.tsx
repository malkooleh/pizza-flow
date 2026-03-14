import { useAuth } from '@/context/AuthContext';
import { Link } from 'react-router-dom';
import { ShoppingBag } from 'lucide-react';
import { useCartStore } from '@/stores/cartStore';

export const Header = () => {
    const { isAuthenticated, user, login, logout, loading } = useAuth();
    const { toggleCart, items } = useCartStore();
    const cartItemCount = items.reduce((acc, item) => acc + item.quantity, 0);

    return (
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-6 shadow-sm sticky top-0 z-40">
            <div className="flex items-center gap-4">
                <Link to="/" className="text-2xl font-bold text-[hsl(var(--primary-hue),90%,55%)] font-heading">
                    PizzaFlow
                </Link>
                <nav className="hidden md:flex gap-4 ml-8 text-sm font-medium text-gray-600">
                    <button 
                        onClick={() => {
                            if (window.location.pathname === '/') {
                                document.getElementById('menu')?.scrollIntoView({ behavior: 'smooth' });
                            } else {
                                window.location.href = '/#menu';
                            }
                        }}
                        className="hover:text-black"
                    >
                        Menu
                    </button>
                    {isAuthenticated && (
                        <>
                            <Link to="/orders" className="hover:text-black">My Orders</Link>
                            <Link to="/bookings" className="hover:text-black">Book a Table</Link>
                        </>
                    )}
                    {user?.roles?.includes('admin') && (
                        <Link to="/admin" className="text-red-500 hover:text-red-700">Admin</Link>
                    )}
                </nav>
            </div>

            <div className="flex items-center gap-4">
                <button
                    onClick={() => toggleCart()}
                    className="relative p-2 text-gray-600 hover:bg-gray-100 rounded-full transition-colors mr-2"
                >
                    <ShoppingBag size={20} />
                    {cartItemCount > 0 && (
                        <span className="absolute -top-1 -right-1 bg-[hsl(var(--primary-hue),90%,55%)] text-white text-[10px] font-bold w-4 h-4 flex items-center justify-center rounded-full">
                            {cartItemCount}
                        </span>
                    )}
                </button>

                {loading ? (
                    <span className="text-sm text-gray-400">Loading...</span>
                ) : isAuthenticated ? (
                    <div className="flex items-center gap-4">
                        <span className="text-sm">Hi, {user?.firstName || user?.username}</span>
                        <button
                            onClick={logout}
                            className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                        >
                            Logout
                        </button>
                    </div>
                ) : (
                    <button
                        onClick={login}
                        className="px-4 py-2 text-sm font-medium text-white bg-[hsl(var(--primary-hue),90%,55%)] rounded-md hover:bg-[hsl(var(--primary-hue),90%,45%)] transition-colors shadow-sm"
                    >
                        Login
                    </button>
                )}
            </div>
        </header>
    );
};
