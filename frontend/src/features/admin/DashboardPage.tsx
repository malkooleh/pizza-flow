import { XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import { DollarSign, ShoppingBag, Clock, TrendingUp } from 'lucide-react';

const EVENTS_DATA = [
    { time: '10:00', orders: 4 },
    { time: '11:00', orders: 12 },
    { time: '12:00', orders: 28 },
    { time: '13:00', orders: 22 },
    { time: '14:00', orders: 15 },
    { time: '15:00', orders: 8 },
    { time: '16:00', orders: 18 },
    { time: '17:00', orders: 35 },
];

const STATS = [
    { label: 'Total Revenue', value: '$12,450', icon: DollarSign, change: '+12%', positive: true },
    { label: 'Active Orders', value: '24', icon: ShoppingBag, change: '+4', positive: true },
    { label: 'Avg Prep Time', value: '14m', icon: Clock, change: '-2m', positive: true },
    { label: 'Growth', value: '18%', icon: TrendingUp, change: '+2%', positive: true },
];

export const DashboardPage = () => {
    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-bold font-heading">Dashboard</h1>
                <p className="text-gray-400">Overview of your restaurant's performance today.</p>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {STATS.map((stat) => (
                    <div key={stat.label} className="bg-gray-800 border border-gray-700 p-6 rounded-xl">
                        <div className="flex justify-between items-start mb-4">
                            <div className="p-2 bg-gray-700/50 rounded-lg text-[hsl(var(--primary-hue),90%,55%)]">
                                <stat.icon size={24} />
                            </div>
                            <span className={`text-sm font-medium ${stat.positive ? 'text-green-500' : 'text-red-500'}`}>
                                {stat.change}
                            </span>
                        </div>
                        <h3 className="text-gray-400 text-sm font-medium">{stat.label}</h3>
                        <p className="text-2xl font-bold mt-1">{stat.value}</p>
                    </div>
                ))}
            </div>

            {/* Charts Section */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="bg-gray-800 border border-gray-700 p-6 rounded-xl h-[400px]">
                    <h3 className="text-lg font-bold mb-6">Orders Activity</h3>
                    <ResponsiveContainer width="100%" height="100%">
                        <AreaChart data={EVENTS_DATA}>
                            <defs>
                                <linearGradient id="colorOrders" x1="0" y1="0" x2="0" y2="1">
                                    <stop offset="5%" stopColor="hsl(var(--primary-hue),90%,55%)" stopOpacity={0.3} />
                                    <stop offset="95%" stopColor="hsl(var(--primary-hue),90%,55%)" stopOpacity={0} />
                                </linearGradient>
                            </defs>
                            <CartesianGrid strokeDasharray="3 3" stroke="#374151" vertical={false} />
                            <XAxis dataKey="time" stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} />
                            <YAxis stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} />
                            <Tooltip
                                contentStyle={{ backgroundColor: '#1F2937', borderColor: '#374151', color: '#fff' }}
                                itemStyle={{ color: '#fff' }}
                            />
                            <Area
                                type="monotone"
                                dataKey="orders"
                                stroke="hsl(var(--primary-hue),90%,55%)"
                                strokeWidth={2}
                                fillOpacity={1}
                                fill="url(#colorOrders)"
                            />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>

                <div className="bg-gray-800 border border-gray-700 p-6 rounded-xl h-[400px]">
                    <h3 className="text-lg font-bold mb-6">Popular Items</h3>
                    {/* Placeholder for now */}
                    <div className="flex items-center justify-center h-full text-gray-500">
                        Top Selling Items Chart (Coming Soon)
                    </div>
                </div>
            </div>
        </div>
    );
};
