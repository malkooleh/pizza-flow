# PizzaFlow Frontend Documentation

## 1. Overview
The PizzaFlow Frontend is a modern Single Page Application (SPA) built to interface with the distributed microservices backend. It serves two primary user personas:
1.  **Customers**: Ordering food, tracking deliveries.
2.  **Staff/Admins**: Managing orders, inventory, and viewing analytics.

## 2. Technology Stack (Modern Best Practices)

| Category | Technology | Reasoning |
| :--- | :--- | :--- |
| **Framework** | **React 18** | Industry standard, component-based UI. |
| **Build Tool** | **Vite** | Extremely fast HMR, replacing Webpack/CRA. |
| **Language** | **TypeScript** | Strict type safety for enterprise reliability. |
| **Styling** | **Tailwind CSS + CSS Modules** | Modern utility-first styling with scoped modules for complex components. |
| **State (Server)**| **TanStack Query (v5)** | The standard for async state, caching, and background updates. |
| **State (Client)**| **Zustand** | Minimalist, high-performance state management. |
| **Forms** | **React Hook Form + Zod** | Performance-focused form handling with schema validation. |
| **Accessibility**| **Radix UI (Primitives)**| Unstyled, accessible components ensuring WCAG compliance. |
| **Charts** | **Recharts** | Composable chart library for analytics. |
| **Real-time** | **SSE (Server-Sent Events)** | Primary protocol for customer order tracking (efficient, unidirectional). |
| **WebSockets** | **STOMP / SockJS** | Used for Admin Dashboard real-time broadcasting. |

---

## 3. Project Structure

```text
frontend/
├── public/                 # Static assets
├── src/
│   ├── assets/             # Global styles (variables.css, reset.css)
│   ├── components/
│   │   ├── ui/             # Radix Primitives + styles (Button, Dialog, Input)
│   │   └── layout/         # LayoutShell, Sidebar (Admin), Navbar (Customer)
│   ├── features/           # Domain-driven feature modules
│   │   ├── auth/           # Login logic
│   │   ├── catalog/        # Menu Grid, Product Cards
│   │   ├── cart/           # Cart Store (Zustand), Slide-over
│   │   ├── checkout/       # Multi-step Form (React Hook Form)
│   │   ├── admin/          # Dashboard Widgets, Recharts
│   │   ├── orders/         # Order History & Details
│   │   ├── bookings/       # Table Reservations
│   │   └── tracking/       # SSE / WebSocket listeners
│   ├── stores/             # Global Zustand Stores
│   ├── api/                # Generated (Orval) + Manual API Clients
│   ├── routes/             # Router Configuration (Loaders/Actions)
│   ├── types/              # TS Interfaces
│   └── App.tsx             # Root
├── Dockerfile              # Nginx-based production image
└── vite.config.ts          # Vite configuration
```

---

## 4. Key Architectural Patterns

### 4.1 State Management Strategy
We avoid "Context Hell" by splitting state concerns:

1.  **Server State (TanStack Query)**:
    - "Is the menu loading?"
    - "What is the list of orders?"
    - *Pattern*: `useQuery(['menu', restaurantId], fetchMenu)`
2.  **Client Global State (Zustand)**:
    - "What is in the cart?"
    - "Is the sidebar open?"
    - *Pattern*: `const cart = useCartStore((state) => state.items)`
3.  **Form State (React Hook Form)**:
    - "What did the user type in the address field?"
    - *Pattern*: `const { register, handleSubmit } = useForm({ resolver: zodResolver(schema) })`

### 4.2 Styling with CSS Modules
We use **CSS Modules** to ensure styles are locally scoped to components, preventing global side effects—a best practice in modern component architecture.

**`Button.module.css`**
```css
.button {
  background: var(--color-primary);
  border-radius: var(--radius-md);
  /* ... */
}
.button--outline {
  background: transparent;
  border: 1px solid var(--color-primary);
}
```

**`Button.tsx`**
```tsx
import styles from './Button.module.css';

export const Button = ({ variant = 'primary', ...props }) => (
  <button className={`${styles.button} ${styles[`button--${variant}`]}`} {...props} />
);
```

### 4.3 Accessibility (Headless UI)
We use **Radix UI** primitives. These provide the *functionality* (keyboard navigation, focus management, ARIA attributes) but *zero styles*. We apply our own "Premium" CSS to them.

**Example: Modal (Dialog)**
```tsx
import * as Dialog from '@radix-ui/react-dialog';
import styles from './Dialog.module.css';

export const Modal = ({ children }) => (
  <Dialog.Root>
    <Dialog.Portal>
      <Dialog.Overlay className={styles.overlay} />
      <Dialog.Content className={styles.content}>
        {children}
      </Dialog.Content>
    </Dialog.Portal>
  </Dialog.Root>
);
```

---

## 5. Admin Dashboard Features

The Admin Panel (`/admin/*`) is a data-heavy interface.

### 5.1 Technology Choice
- **Recharts**: Chosen for its React-native feel and flexibility with SVGs.
- **TanStack Table**: (Optional) For complex data grids (sorting/filtering orders), if simple tables suffice we will stick to native semantic HTML tables styled with CSS Grid/Flex.

### 5.2 Real-Time Operations
- **Order Tracking (SSE)**: `useOrderTracking(orderId)` uses Server-Sent Events for efficient customer tracking.
- **Admin Kitchen (WebSockets)**: `useWebSocket('/topic/orders')` listens for broadcasting events.
- **Optimistic Updates**: Immediate UI feedback (via TanStack Query `setQueryData`) while background requests process.

---

## 6. Development Workflow

1.  **Install**: `npm install`
2.  **Run**: `npm run dev`
3.  **Lint**: `npm run lint` (ESLint + Prettier)
4.  **Test**: `npm run test` (Vitest)
