import { Outlet } from 'react-router-dom';
import { Header } from '@/components/layout/Header';
import { CartDrawer } from '@/features/cart/CartDrawer';

import { Toaster } from '@/components/ui/toaster';

function App() {
  return (
    <div className="min-h-screen bg-[hsl(220,15%,10%)] text-[hsl(0,0%,95%)] font-body relative overflow-x-hidden">
      <Header />
      <CartDrawer />
      <main className="container mx-auto px-4 py-8">
        <Outlet />
      </main>
      <Toaster />
    </div>
  )
}

export default App
