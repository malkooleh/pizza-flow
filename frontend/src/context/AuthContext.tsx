import { createContext, useContext, useEffect, useState, useRef } from 'react';
import Keycloak from 'keycloak-js';
import axios from 'axios';

interface UserProfile {
    id?: string;
    username?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    roles?: string[];
}

interface AuthContextType {
    isAuthenticated: boolean;
    token: string | null;
    user: UserProfile | null;
    login: () => void;
    logout: () => void;
    loading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const isRun = useRef(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [token, setToken] = useState<string | null>(null);
    const [user, setUser] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);

    const keycloakRef = useRef<Keycloak | null>(null);

    useEffect(() => {
        if (isRun.current) return;
        isRun.current = true;

        const keycloak = new Keycloak({
            url: 'http://localhost:8081',
            realm: 'pizza-flow',
            clientId: 'pizza-flow-web', // Assuming this client ID
        });

        keycloakRef.current = keycloak;

        keycloak
            .init({
                onLoad: 'check-sso',
                checkLoginIframe: false,
                pkceMethod: 'S256',
            })
            .then((authenticated) => {
                setIsAuthenticated(authenticated);
                if (authenticated) {
                    setToken(keycloak.token || null);
                    localStorage.setItem('token', keycloak.token || '');

                    keycloak.loadUserProfile().then((profile) => {
                        setUser({
                            ...profile,
                            roles: keycloak.realmAccess?.roles || []
                        });
                    });
                }
                setLoading(false);
            })
            .catch((err) => {
                console.error('Keycloak init failed', err);
                setLoading(false);
            });

    }, []);

    // Global Axios interceptor for token injection
    useEffect(() => {
        if (!token) return;

        const interceptor = axios.interceptors.request.use((config) => {
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
            return config;
        });

        return () => {
            axios.interceptors.request.eject(interceptor);
        };
    }, [token]);

    const login = () => keycloakRef.current?.login();
    const logout = () => {
        localStorage.removeItem('token');
        keycloakRef.current?.logout();
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, token, user, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
};
