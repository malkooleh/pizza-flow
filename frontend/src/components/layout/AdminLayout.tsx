import { Outlet, Link, useLocation } from 'react-router-dom';
import { LayoutDashboard, ShoppingBag, UtensilsCrossed, Package, Settings, LogOut } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';

const NAV_ITEMS = [
    { icon: LayoutDashboard, label: 'Dashboard', path: '/admin' },
    { icon: ShoppingBag, label: 'Orders', path: '/admin/orders' },
    { icon: UtensilsCrossed, label: 'Menu', path: '/admin/menu' },
    { icon: Package, label: 'Inventory', path: '/admin/inventory' },
    { icon: Settings, label: 'Settings', path: '/admin/settings' },
];

export const AdminLayout = () => {
    const { pathname } = useLocation();
    const { logout, user } = useAuth();

    return (
        <div className="min-h-screen bg-gray-900 text-gray-100 flex font-body">
            {/* Sidebar */}
            <aside className="w-64 border-r border-gray-800 bg-gray-900/50 flex flex-col fixed h-full z-10">
                <div className="h-16 flex items-center px-6 border-b border-gray-800">
                    <span className="text-xl font-bold font-heading text-[hsl(var(--primary-hue),90%,55%)]">PizzaFlow Admin</span>
                </div>

                <nav className="flex-1 p-4 space-y-2">
                    {NAV_ITEMS.map(({ icon: Icon, label, path }) => {
                        const isActive = pathname === path;
                        return (
                            <Link
                                key={path}
                                to={path}
                                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${isActive
                                        ? 'bg-[hsl(var(--primary-hue),90%,55%)]/10 text-[hsl(var(--primary-hue),90%,55%)] border border-[hsl(var(--primary-hue),90%,55%)]/20'
                                        : 'text-gray-400 hover:bg-white/5 hover:text-white'
                                    }`}
                            >
                                <Icon size={20} />
                                <span className="font-medium">{label}</span>
                            </Link>
                        );
                    })}
                </nav>

                <div className="p-4 border-t border-gray-800">
                    <div className="flex items-center gap-3 px-4 py-3 mb-2">
                        <div className="w-8 h-8 rounded-full bg-gray-700 flex items-center justify-center font-bold text-xs">
                            {user?.username?.substring(0, 2).toUpperCase() || 'AD'}
                        </div>
                        <div className="overflow-hidden">
                            <p className="text-sm font-medium truncate">{user?.firstName || 'Admin'}</p>
                            <p className="text-xs text-gray-500 truncate">{user?.email}</p>
                        </div>
                    </div>
                    <button
                        onClick={logout}
                        className="flex items-center gap-3 px-4 py-2 w-full text-left text-sm text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
                    >
                        <LogOut size={18} />
                        Logout
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 ml-64 p-8 overflow-y-auto">
                <Outlet />
            </main>
        </div>
    );
};
