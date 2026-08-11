import { AlertCircle, Info } from "lucide-react";

const TONES = {
    error: "bg-danger-tint text-danger",
    info: "bg-info-tint text-primary",
    success: "bg-success-tint text-success",
};

export function Alert({ tone = "info", children }) {
    return (
        <div
            className={`mb-4 flex items-start gap-2 rounded-md px-4 py-3 text-sm ${TONES[tone] ?? TONES.info}`}
            role={tone === "error" ? "alert" : "status"}
        >
            {tone === "error" ? <AlertCircle size={16} className="mt-0.5 shrink-0" /> : <Info size={16} className="mt-0.5 shrink-0" />}
            <span className="min-w-0">{children}</span>
        </div>
    );
}
