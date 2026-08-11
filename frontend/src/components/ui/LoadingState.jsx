export function LoadingState({ label = "Loading…" }) {
    return (
        <div className="flex flex-col items-center justify-center gap-3 px-6 py-10 text-center text-ink-muted">
            <span
                className="h-7 w-7 animate-spin rounded-full border-[3px] border-selection border-t-primary"
                aria-hidden
            />
            <span className="text-sm">{label}</span>
        </div>
    );
}
