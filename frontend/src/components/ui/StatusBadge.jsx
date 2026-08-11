const TONES = {
    success: "text-success bg-success-tint",
    warning: "text-warning bg-warning-tint",
    danger: "text-danger bg-danger-tint",
    info: "text-primary bg-info-tint",
    neutral: "text-ink-muted bg-[#eef1f0]",
};

export function StatusBadge({ tone = "neutral", children }) {
    return (
        <span
            className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-semibold leading-relaxed before:h-1.5 before:w-1.5 before:rounded-full before:bg-current ${
                TONES[tone] ?? TONES.neutral
            }`}
        >
            {children}
        </span>
    );
}

export function toneForStatus(status) {
    const s = status.toUpperCase();
    if (["PAID", "APPROVED", "ACCEPTED", "VERIFIED", "ACTIVE", "RESOLVED", "CONFIRMED", "GREEN"].includes(s)) {
        return "success";
    }
    if (["PENDING", "WAITLISTED", "UNDER_ASSESSMENT", "ISSUED", "OPEN", "AMBER", "PARTIALLY_PAID"].includes(s)) {
        return "warning";
    }
    if (["OVERDUE", "REJECTED", "DISPUTED", "LAPSED", "WRITTEN_OFF", "RED", "PAR90", "PAR180", "NPA"].includes(s)) {
        return "danger";
    }
    if (["ASSIGNED", "IN_PROGRESS", "SANCTIONED", "DISBURSED", "CO_SIGNED", "PAR30", "PAR60"].includes(s)) {
        return "info";
    }
    return "neutral";
}
