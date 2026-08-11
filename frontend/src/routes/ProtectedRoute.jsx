import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "@/auth/useAuth";
import { LoadingState } from "@/components/ui";

export function ProtectedRoute() {
    const { user, mustReset, loading } = useAuth();
    if (loading) {
        return (<div style={{ display: "grid", placeItems: "center", height: "100%" }}>
        <LoadingState label="Loading your workspace…"/>
      </div>);
    }
    if (!user)
        return <Navigate to="/login" replace/>;
    if (mustReset)
        return <Navigate to="/reset-password" replace/>;
    return <Outlet />;
}
