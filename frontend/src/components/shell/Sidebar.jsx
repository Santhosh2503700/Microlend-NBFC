import { NavLink } from "react-router-dom";
import { LogOut, Menu } from "lucide-react";
import { Wordmark } from "@/components/brand/Wordmark";
import { navForRole, ROLE_LABELS } from "@/routes/nav";
import { useAuth } from "@/auth/useAuth";
import { EMERALD_GRADIENT_VERTICAL } from "./emeraldGradient";


export function Sidebar({ role, collapsed = false, onToggle }) {
    const items = navForRole(role);
    const { logout } = useAuth();

    return (
        <aside
            style={EMERALD_GRADIENT_VERTICAL}
            className={`hidden shrink-0 flex-col overflow-y-auto overflow-x-hidden border-r border-white/10 px-4 py-5 text-white transition-[width] duration-200 ease-out md:flex ${
                collapsed ? "w-20" : "w-64"
            }`}
        >
            <div className={`flex items-center pb-5 ${collapsed ? "justify-center" : "gap-2"}`}>
                <button
                    type="button"
                    onClick={onToggle}
                    aria-label="Toggle sidebar"
                    className="flex h-10 w-10 shrink-0 items-center justify-center rounded-md text-white transition hover:bg-white/15"
                >
                    <Menu size={22} />
                </button>
                {!collapsed && <Wordmark height={30} variant="light" />}
            </div>

            {!collapsed && (
                <div className="px-3 pb-2 pt-4 text-xs uppercase tracking-[0.08em] text-white/65">
                    {ROLE_LABELS[role]} workspace
                </div>
            )}

            <nav className="flex flex-1 flex-col gap-2">
                {items.map((item) => {
                    const Icon = item.icon;
                    return (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            title={item.label}
                            className={({ isActive }) =>
                                `flex items-center gap-3 rounded-md px-3 py-3 font-medium transition ${
                                    collapsed ? "justify-center" : ""
                                } ${
                                    isActive
                                        ? "bg-primary text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.14)]"
                                        : "text-white/80 hover:bg-white/10 hover:text-white"
                                }`
                            }
                        >
                            <Icon size={18} className="shrink-0" />
                            {!collapsed && <span className="truncate text-base">{item.label}</span>}
                        </NavLink>
                    );
                })}
            </nav>

            <div className="mt-auto border-t border-white/15 pt-3">
                <button
                    type="button"
                    onClick={logout}
                    title="Logout"
                    className={`flex w-full items-center gap-3 rounded-md border border-white/25 bg-white/15 px-3 py-3 font-semibold text-white backdrop-blur transition hover:bg-white/25 ${
                        collapsed ? "justify-center" : ""
                    }`}
                >
                    <LogOut size={18} className="shrink-0" />
                    {!collapsed && <span className="truncate text-base">Logout</span>}
                </button>
            </div>
        </aside>
    );
}
