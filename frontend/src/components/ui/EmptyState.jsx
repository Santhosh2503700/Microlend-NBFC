import { Inbox } from "lucide-react";
export function EmptyState({ title, description, icon: Icon = Inbox, action }) {
    return (
        <div className="flex flex-col items-center justify-center gap-3 px-6 py-10 text-center text-ink-muted">
            <span className="text-primary opacity-80">
                <Icon size={40} strokeWidth={1.5} />
            </span>
            <span className="font-semibold text-ink">{title}</span>
            {description && <span className="text-sm">{description}</span>}
            {action}
        </div>
    );
}
