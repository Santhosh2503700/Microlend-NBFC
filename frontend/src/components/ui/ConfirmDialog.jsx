import { useEffect } from "react";
import { createPortal } from "react-dom";
import { Button } from "./Button";


export function ConfirmDialog({
    open,
    title,
    message,
    confirmLabel = "Delete",
    cancelLabel = "Cancel",
    tone = "danger",
    loading = false,
    onConfirm,
    onCancel,
}) {
    useEffect(() => {
        if (!open) return;
        const onKey = (e) => {
            if (e.key === "Escape" && !loading) onCancel?.();
        };
        document.addEventListener("keydown", onKey);
        return () => document.removeEventListener("keydown", onKey);
    }, [open, loading, onCancel]);

    if (!open) return null;

    return createPortal(
        <div
            className="fixed inset-0 z-[70] flex items-center justify-center p-4"
            role="dialog"
            aria-modal="true"
            aria-label={title || "Confirm"}
        >
            <div
                className="absolute inset-0 bg-black/40"
                onClick={() => !loading && onCancel?.()}
                aria-hidden
            />
            <div className="relative w-full max-w-sm rounded-lg border border-border bg-white p-5 shadow-pop">
                {title && <h3 className="mb-2 mt-0 text-lg font-semibold text-ink">{title}</h3>}
                {message && <p className="mb-5 text-sm text-ink-muted">{message}</p>}
                <div className="flex justify-end gap-2">
                    <Button variant="secondary" onClick={onCancel} disabled={loading}>
                        {cancelLabel}
                    </Button>
                    <Button
                        variant={tone === "danger" ? "danger" : "primary"}
                        onClick={onConfirm}
                        loading={loading}
                    >
                        {confirmLabel}
                    </Button>
                </div>
            </div>
        </div>,
        document.body,
    );
}
