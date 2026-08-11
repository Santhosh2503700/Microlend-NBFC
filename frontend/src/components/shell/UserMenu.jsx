import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { LogOut, User } from "lucide-react";
import { useAuth } from "@/auth/useAuth";
import { ROLE_LABELS } from "@/routes/nav";

function initials(name) {
    return name
        .split(" ")
        .map((p) => p[0])
        .filter(Boolean)
        .slice(0, 2)
        .join("")
        .toUpperCase();
}


export function UserMenu() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);
    const ref = useRef(null);

    // Close on outside click.
    useEffect(() => {
        if (!open) return;
        const onClick = (e) => {
            if (ref.current && !ref.current.contains(e.target)) setOpen(false);
        };
        document.addEventListener("mousedown", onClick);
        return () => document.removeEventListener("mousedown", onClick);
    }, [open]);

    if (!user) return null;

    return (
        <div ref={ref} className="relative shrink-0">
            <button
                type="button"
                onClick={() => setOpen((v) => !v)}
                title="Account"
                aria-haspopup="menu"
                aria-expanded={open}
                className="flex shrink-0 items-center gap-2 rounded-full border border-white/25 bg-white/15 py-1 pl-1 pr-3 backdrop-blur transition hover:bg-white/25"
            >
                <span className="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-white text-sm font-semibold text-primary">
                    {initials(user.name)}
                </span>

                <span className="flex min-w-0 flex-col items-start leading-tight">
                    <span className="max-w-[30vw] truncate text-sm font-semibold text-white sm:max-w-[26vw] lg:max-w-[200px]">
                        {user.name}
                    </span>
                    <span className="max-w-[30vw] truncate text-xs text-white/70 sm:max-w-[26vw] lg:max-w-[200px]">
                        {ROLE_LABELS[user.role]}
                    </span>
                </span>
            </button>

            {open && (
                <div
                    role="menu"
                    className="absolute right-0 top-[calc(100%+8px)] z-40 w-[min(220px,calc(100vw-2rem))] overflow-hidden rounded-lg border border-border bg-white py-1 shadow-pop"
                >
                    <button
                        type="button"
                        role="menuitem"
                        onClick={() => {
                            setOpen(false);
                            navigate("/profile");
                        }}
                        className="flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm text-ink transition hover:bg-selection"
                    >
                        <User size={18} className="shrink-0 text-ink-muted" />
                        View profile
                    </button>
                    <button
                        type="button"
                        role="menuitem"
                        onClick={() => {
                            setOpen(false);
                            logout();
                        }}
                        className="flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm text-danger transition hover:bg-selection"
                    >
                        <LogOut size={18} className="shrink-0" />
                        Logout
                    </button>
                </div>
            )}
        </div>
    );
}
