import { createContext, useCallback, useEffect, useMemo, useState } from "react";
import { authApi } from "@/api/auth";
import { tokenStore, UNAUTHORIZED_EVENT } from "@/api/tokenStore";
export const AuthContext = createContext(undefined);
function toUser(r) {
    return { userId: r.userId, name: r.name, role: r.role, branchId: r.branchId };
}
export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [mustReset, setMustReset] = useState(false);
    const [loading, setLoading] = useState(true);
    const logout = useCallback(() => {
        tokenStore.clear();
        setUser(null);
        setMustReset(false);
    }, []);
    // Hydrate from a persisted token on first load.
    useEffect(() => {
        const token = tokenStore.getToken();
        if (!token) {
            setLoading(false);
            return;
        }
        authApi
            .me()
            .then((me) => {
            setUser({ userId: me.userId, name: me.name, email: me.email, role: me.role, branchId: me.branchId });
            setMustReset(me.mustResetPassword);
        })
            .catch(() => tokenStore.clear())
            .finally(() => setLoading(false));
    }, []);
    // React to a 401 from anywhere in the app.
    useEffect(() => {
        const handler = () => logout();
        window.addEventListener(UNAUTHORIZED_EVENT, handler);
        return () => window.removeEventListener(UNAUTHORIZED_EVENT, handler);
    }, [logout]);
    const login = useCallback(async (email, password) => {
        const res = await authApi.login({ email, password });
        tokenStore.setToken(res.token);
        tokenStore.setUserRaw(JSON.stringify(toUser(res)));
        setUser(toUser(res));
        setMustReset(res.forcePasswordReset);
        return res;
    }, []);
    const submitReset = useCallback(async (newPassword) => {
        const res = await authApi.resetPassword({ newPassword });
        tokenStore.setToken(res.token);
        tokenStore.setUserRaw(JSON.stringify(toUser(res)));
        setUser(toUser(res));
        setMustReset(false);
        return res;
    }, []);
    const value = useMemo(() => ({ user, mustReset, loading, login, submitReset, logout }), [user, mustReset, loading, login, submitReset, logout]);
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
