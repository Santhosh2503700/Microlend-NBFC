import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, CheckCheck } from "lucide-react";
import { notificationsApi } from "@/api/notifications";
import { LoadingState } from "@/components/ui/LoadingState";
import { EmptyState } from "@/components/ui/EmptyState";

function timeAgo(iso) {
    const then = new Date(iso).getTime();
    if (Number.isNaN(then))
        return "";
    const diff = Math.max(0, Date.now() - then);
    const mins = Math.floor(diff / 60000);
    if (mins < 1)
        return "just now";
    if (mins < 60)
        return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24)
        return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
}

export function NotificationsBell() {
    const [open, setOpen] = useState(false);
    const [count, setCount] = useState(0);
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(false);
    const ref = useRef(null);
    const navigate = useNavigate();

    const refreshCount = useCallback(() => {
        notificationsApi.unreadCount().then(setCount).catch(() => undefined);
    }, []);

    // Poll the unread count.
    useEffect(() => {
        refreshCount();
        const t = window.setInterval(refreshCount, 30000);
        return () => window.clearInterval(t);
    }, [refreshCount]);

    // Close on outside click.
    useEffect(() => {
        if (!open)
            return;
        const onClick = (e) => {
            if (ref.current && !ref.current.contains(e.target))
                setOpen(false);
        };
        document.addEventListener("mousedown", onClick);
        return () => document.removeEventListener("mousedown", onClick);
    }, [open]);

    const toggle = () => {
        // On mobile the bell mirrors the old bottom-nav tab: it opens the full notifications page
        // (the compact dropdown is a tablet/desktop affordance where there's room for it).
        if (typeof window !== "undefined" && window.matchMedia("(max-width: 767px)").matches) {
            setOpen(false);
            navigate("/notifications");
            return;
        }
        const next = !open;
        setOpen(next);
        if (next) {
            setLoading(true);
            notificationsApi
                .list()
                .then(setItems)
                .catch(() => setItems([]))
                .finally(() => setLoading(false));
        }
    };

    const markRead = (n) => {
        if (n.status !== "UNREAD")
            return;
        notificationsApi.markRead(n.notificationId).then(() => {
            setItems((prev) => prev.map((x) => (x.notificationId === n.notificationId ? { ...x, status: "READ" } : x)));
            setCount((c) => Math.max(0, c - 1));
        });
    };

    return (
        <div ref={ref} className="relative">
            <button
                type="button"
                className="relative inline-flex h-11 w-11 items-center justify-center rounded-md border border-transparent text-white transition hover:bg-white/15"
                aria-label="Notifications"
                onClick={toggle}
            >
                <Bell size={20} />
                {count > 0 && (
                    <span className="absolute right-1 top-1 inline-flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold leading-none text-white">
                        {count > 99 ? "99+" : count}
                    </span>
                )}
            </button>

            {open && (
                <div className="absolute right-0 top-[calc(100%+8px)] z-40 max-h-[min(460px,70vh)] w-[min(360px,calc(100vw-2rem))] max-w-[95vw] overflow-y-auto rounded-lg border border-emerald-800 bg-[#004d40] text-emerald-50 shadow-pop">

                    <div className="flex items-center justify-between border-b border-emerald-700/50 p-4 font-semibold text-white">
                        <span>Notifications</span>
                        {count > 0 && (
                            <span className="flex items-center gap-2 text-sm text-emerald-200/80">
                                <CheckCheck size={14} /> {count} unread
                            </span>
                        )}
                    </div>

                    {loading ? (
                        <div className="p-4">
                            <LoadingState />
                        </div>
                    ) : items.length === 0 ? (
                        <div className="p-4">
                            <EmptyState title="You're all caught up" description="No notifications yet." icon={Bell} />
                        </div>
                    ) : (
                        items.map((n) => (
                            <div
                                key={n.notificationId}
                                className={`flex cursor-pointer gap-3 border-b border-emerald-700/30 p-3 sm:px-4 transition-colors hover:bg-[#00695c] ${
                                    n.status === "UNREAD" ? "bg-[#005a4f]" : ""
                                }`}
                                onClick={() => markRead(n)}
                            >
                                {n.status === "UNREAD" && (
                                    <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-emerald-400" aria-hidden />
                                )}
                                <div className="min-w-0">
                                    <div className="text-sm leading-snug text-white">{n.message}</div>
                                    <div className="mt-1 text-xs text-emerald-200/70">
                                        {n.category ?? "SYSTEM"} · {timeAgo(n.createdDate)}
                                    </div>
                                </div>
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
}