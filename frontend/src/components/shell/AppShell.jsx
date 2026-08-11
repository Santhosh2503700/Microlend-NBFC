import { useEffect, useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import { Sidebar } from "./Sidebar";
import { BottomNav } from "./BottomNav";
import { Header } from "./Header";
import { useAuth } from "@/auth/useAuth";


export function AppShell() {
    const { user } = useAuth();
    const location = useLocation();
    const [collapsed, setCollapsed] = useState(() =>
        typeof window !== "undefined" ? window.innerWidth < 1024 : false);

    useEffect(() => {
        const onResize = () => {
            if (window.innerWidth >= 1024) setCollapsed(false);
        };
        window.addEventListener("resize", onResize);
        return () => window.removeEventListener("resize", onResize);
    }, []);

    if (!user) return null;

    return (
        <div className="flex h-full w-full overflow-hidden bg-canvas">
            <Sidebar role={user.role} collapsed={collapsed} onToggle={() => setCollapsed((v) => !v)} />

            <div className="flex min-w-0 flex-1 flex-col">
                <Header />

                <main
                    key={location.pathname}
                    className="min-w-0 flex-1 overflow-y-auto p-4 pb-[calc(theme(spacing.bottomnav)+1.25rem)] sm:p-5 md:pb-5 lg:p-6"
                >
                    <Outlet />
                </main>
            </div>

            <BottomNav role={user.role} />
        </div>
    );
}
