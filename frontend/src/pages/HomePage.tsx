import { useState } from 'react';
import { MenuGrid } from '@/features/catalog/MenuGrid';

export const HomePage = () => {
    const [selectedCategory, setSelectedCategory] = useState('All');

    const scrollToMenu = () => {
        const menuElement = document.getElementById('menu');
        if (menuElement) {
            menuElement.scrollIntoView({ behavior: 'smooth' });
        }
    };

    const categories = ['All', 'Pizzas', 'Drinks', 'Sides'];

    return (
        <div className="space-y-16">
            {/* Hero Section */}
            <section className="text-center space-y-6 py-20 relative">
                <div className="absolute inset-0 bg-gradient-to-r from-[hsl(var(--primary-hue),90%,55%)]/10 to-transparent blur-3xl -z-10" />
                <h1 className="text-5xl md:text-7xl font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-gray-400">
                    Premium Pizza, <br />
                    <span className="text-[hsl(var(--primary-hue),90%,55%)]">Delivered Fast.</span>
                </h1>
                <p className="text-xl text-gray-400 max-w-2xl mx-auto">
                    Experience the future of pizza delivery. Real-time tracking, fresh ingredients, and a seamless ordering experience.
                </p>
                <button 
                    onClick={scrollToMenu}
                    className="px-8 py-4 bg-[hsl(var(--primary-hue),90%,55%)] text-white font-bold rounded-full text-lg hover:shadow-premium hover:scale-105 transition-all"
                >
                    Order Now
                </button>
            </section>

            {/* Menu Section */}
            <section id="menu" className="space-y-8 scroll-mt-20">
                <div className="flex flex-col md:flex-row items-center justify-between gap-4">
                    <h2 className="text-3xl font-bold">Our Menu</h2>
                    <div className="flex flex-wrap gap-2 justify-center">
                        {categories.map((cat) => (
                            <button 
                                key={cat} 
                                onClick={() => setSelectedCategory(cat)}
                                className={`px-4 py-2 rounded-full border transition-all text-sm font-medium ${
                                    selectedCategory === cat 
                                        ? 'bg-[hsl(var(--primary-hue),90%,55%)] border-[hsl(var(--primary-hue),90%,55%)] text-white' 
                                        : 'border-gray-700 text-gray-400 hover:border-gray-500 hover:bg-white/5'
                                }`}
                            >
                                {cat}
                            </button>
                        ))}
                    </div>
                </div>

                <MenuGrid category={selectedCategory} />
            </section>
        </div>
    );
};
