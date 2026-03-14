# PizzaFlow Frontend Setup & Running Guide

## 📋 Overview

This is the PizzaFlow frontend application built with:
- **React 19** - UI framework
- **TypeScript** - Type safety
- **Vite** - Build tool and dev server
- **TailwindCSS** - Styling
- **React Router** - Navigation
- **TanStack Query** - Server state management
- **Zustand** - Client state management
- **Keycloak** - Authentication
- **WebSocket (STOMP)** - Real-time updates

---

## 🚀 Quick Start

### Prerequisites

1. **Node.js 22+** (as specified in Dockerfile)
   - Download from: https://nodejs.org/
   - Verify installation: `node --version`

2. **npm** (comes with Node.js)
   - Verify installation: `npm --version`

3. **Backend Services Running**
   - API Gateway: http://localhost:8080
   - Config Server: http://localhost:8888
   - Eureka Discovery: http://localhost:8761
   - All microservices up and running

---

## 📦 Installation

### Step 1: Navigate to Frontend Directory

```powershell
cd C:\Users\oleh.malko\work\projects\pizzaFlow\frontend
```

### Step 2: Install Dependencies

```powershell
npm install
```

This will install all required packages from `package.json`:
- React and React DOM
- TypeScript
- Vite
- TailwindCSS
- Keycloak integration
- Axios for HTTP requests
- And all other dependencies

**Expected Duration:** 2-5 minutes (depending on internet speed)

---

## 🏃 Running the Frontend

### Development Mode (Recommended for Development)

```powershell
npm run dev
```

**What happens:**
- ✅ Starts Vite development server
- ✅ Opens at: http://localhost:5173
- ✅ Hot Module Replacement (HMR) enabled
- ✅ TypeScript type checking
- ✅ Proxies API requests to API Gateway (localhost:8080)

**Console Output:**
```
VITE v7.3.1  ready in XXX ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
➜  press h + enter to show help
```

**Access the application:**
- Open browser: http://localhost:5173

---

## 📋 Architecture

```
Browser (localhost:5173)
    ↓
Vite Dev Server (proxies /api requests)
    ↓
API Gateway (localhost:8080)
    ↓
Microservices (8082-8089)
```

**Proxy Configuration:**
- All `/api/*` requests → forwarded to `http://localhost:8080`
- WebSocket `/ws/*` → forwarded to `http://localhost:8080`

---

### Production Build

```powershell
npm run build
```

**What happens:**
- ✅ TypeScript compilation
- ✅ Vite optimized production build
- ✅ Output to `dist/` directory
- ✅ Minified and optimized assets

---

### Preview Production Build Locally

```powershell
npm run preview
```

**What happens:**
- ✅ Serves the production build from `dist/`
- ✅ Opens at: http://localhost:4173

---

## 🔧 Configuration

### API Gateway Proxy

The frontend is configured to proxy API requests to your backend:

**File:** `vite.config.ts`

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // API Gateway
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
    '/ws': {
      target: 'http://localhost:8080',
      ws: true, // WebSocket support
    }
  }
}
```

**Usage in Code:**
```typescript
// Instead of: http://localhost:8080/catalog-service/products
// You can use: /api/catalog-service/products
axios.get('/api/catalog-service/products')
```

---

### Authentication (Keycloak)

The application uses Keycloak for authentication.

**Default Keycloak Configuration:**
- URL: http://localhost:8081
- Realm: pizzaflow
- Client ID: pizzaflow-frontend

**Configuration Location:**
- Check: `src/context/AuthContext.tsx`
- Or: `src/services/` directory

---

## 🐳 Docker Deployment

### Build Docker Image

```powershell
cd C:\Users\oleh.malko\work\projects\pizzaFlow\frontend
docker build -t pizzaflow/frontend:latest .
```

### Run Docker Container

```powershell
docker run -d -p 80:80 --name pizzaflow-frontend pizzaflow/frontend:latest
```

**Access:**
- http://localhost (port 80)

---

## 📁 Project Structure

```
frontend/
├── src/
│   ├── assets/          # Static assets (images, fonts)
│   ├── components/      # Reusable UI components
│   ├── context/         # React context (Auth, Theme, etc.)
│   ├── features/        # Feature-specific components
│   ├── hooks/           # Custom React hooks
│   ├── pages/           # Page components
│   ├── routes/          # React Router configuration
│   ├── services/        # API clients, query client
│   ├── stores/          # Zustand state stores
│   ├── types/           # TypeScript type definitions
│   ├── App.tsx          # Main App component
│   ├── main.tsx         # Application entry point
│   └── index.css        # Global styles
├── public/              # Static public assets
├── dist/                # Production build output
├── Dockerfile           # Docker build configuration
├── nginx.conf           # Nginx configuration for production
├── package.json         # Dependencies and scripts
├── tsconfig.json        # TypeScript configuration
├── vite.config.ts       # Vite configuration
└── tailwind.config.js   # TailwindCSS configuration
```

---

## 🛠️ Available Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start development server with HMR |
| `npm run build` | Create production build |
| `npm run preview` | Preview production build locally |
| `npm run lint` | Run ESLint for code quality |

---

## 🔍 Troubleshooting

### Issue: Port 5173 Already in Use

**Solution:**
```powershell
# Find process using port 5173
netstat -ano | findstr :5173

# Kill the process (replace XXXX with PID)
taskkill /PID XXXX /F

# Or use a different port
npm run dev -- --port 3000
```

---

### Issue: API Requests Failing

**Check:**
1. ✅ Is API Gateway running on port 8080?
   ```powershell
   curl http://localhost:8080/actuator/health
   ```

2. ✅ Are backend services registered with Eureka?
   - Open: http://localhost:8761

3. ✅ Check browser console for CORS errors

---

### Issue: Keycloak Authentication Not Working

**Check:**
1. ✅ Is Keycloak running on port 8081?
   ```powershell
   curl http://localhost:8081
   ```

2. ✅ Is the realm configured correctly?
   - Admin console: http://localhost:8081/admin
   - Username: admin
   - Password: admin

3. ✅ Is the client ID correct in your code?

---

### Issue: WebSocket Connection Failed

**Check:**
1. ✅ Kitchen Service WebSocket endpoint available
2. ✅ Proxy configuration in `vite.config.ts` correct
3. ✅ Browser console for connection errors

---

## 🌐 Accessing the Application

### Development

1. **Start Backend Services** (in order):
   ```powershell
   # Terminal 1 - Infrastructure
   cd infrastructure/docker
   docker-compose up -d
   
   # Terminal 2 - Discovery
   ./mvnw spring-boot:run -pl services/discovery-service
   
   # Terminal 3 - Config Server
   ./mvnw spring-boot:run -pl services/config-service
   
   # Terminal 4 - API Gateway
   ./mvnw spring-boot:run -pl services/api-gateway
   
   # Terminal 5+ - Business Services
   ./mvnw spring-boot:run -pl services/catalog-service
   ./mvnw spring-boot:run -pl services/order-service
   # ... etc
   ```

2. **Start Frontend**:
   ```powershell
   cd frontend
   npm run dev
   ```

3. **Access Application**:
   - Frontend: http://localhost:5173
   - API Gateway: http://localhost:8080
   - Eureka Dashboard: http://localhost:8761
   - Keycloak Admin: http://localhost:8081/admin

---

## 🎯 Key Features

The PizzaFlow frontend provides:

- 🍕 **Menu Browsing** - View available pizzas and items
- 🛒 **Order Placement** - Add items to cart and checkout
- 💳 **Payment Processing** - Secure payment flow
- 📍 **Delivery Tracking** - Real-time delivery status
- 👨‍🍳 **Kitchen Display** - Real-time kitchen order updates (WebSocket)
- 📅 **Table Booking** - Restaurant table reservations
- 📊 **Order History** - View past orders
- 🔐 **Authentication** - Secure login via Keycloak
- 📱 **Responsive Design** - Mobile-friendly UI

---

## 📚 Additional Resources

- **React Documentation:** https://react.dev/
- **Vite Documentation:** https://vitejs.dev/
- **TailwindCSS:** https://tailwindcss.com/
- **React Router:** https://reactrouter.com/
- **TanStack Query:** https://tanstack.com/query/latest
- **Keycloak:** https://www.keycloak.org/documentation

---

## 🎉 You're Ready!

Your PizzaFlow frontend is ready to run. Execute:

```powershell
cd C:\Users\oleh.malko\work\projects\pizzaFlow\frontend
npm install
npm run dev
```

Then open http://localhost:5173 in your browser!

---

**Happy Coding! 🚀🍕**

