import { TrendingUp, TrendingDown } from "lucide-react";
export function KpiCard({ icon: Icon, label, value, delta }) {
    return (
        <div className="flex min-w-0 items-start gap-4 rounded-lg border border-border bg-card p-5 shadow-card">
            <span className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-md bg-selection text-primary">
                <Icon size={22} />
            </span>
            <div className="min-w-0">
                <div className="text-sm text-ink-muted">{label}</div>
                <div className="mt-1 break-words text-2xl font-bold">{value}</div>
                {delta && (
                    <div
                        className={`mt-1 flex items-center gap-2 text-xs font-semibold ${
                            delta.direction === "up" ? "text-success" : "text-danger"
                        }`}
                    >
                        {delta.direction === "up" ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                        <span>{delta.value}</span>
                    </div>
                )}
            </div>
        </div>
    );
}
