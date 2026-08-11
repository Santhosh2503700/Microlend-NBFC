import { NavLink } from "react-router-dom";
import { navForRole } from "@/routes/nav";
import { EMERALD_GRADIENT_HORIZONTAL } from "./emeraldGradient";


export function BottomNav({ role }) {
    const items = navForRole(role).filter((item) => item.path !== "/notifications");

    return (
        <nav
            style={EMERALD_GRADIENT_HORIZONTAL}
            className="fixed inset-x-0 bottom-0 z-30 flex items-stretch overflow-x-auto border-t border-white/10 pb-[env(safe-area-inset-bottom)] text-white shadow-[0_-2px_12px_rgba(16,40,34,0.18)] backdrop-blur-glass md:hidden"
            aria-label="Primary"
        >
            {items.map((item) => {
                const Icon = item.icon;
                return (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        title={item.label}
                        className={({ isActive }) =>
                            `flex min-w-[64px] flex-1 shrink-0 flex-col items-center justify-center gap-1 px-2 py-2 text-[0.65rem] font-medium transition ${
                                isActive
                                    ? "bg-white/15 font-semibold text-white"
                                    : "text-white/70 hover:bg-white/10 hover:text-white"
                            }`
                        }
                    >
                        <Icon size={20} className="shrink-0" />
                        <span className="max-w-[72px] truncate leading-tight">{item.label}</span>
                    </NavLink>
                );
            })}
        </nav>
    );
}
