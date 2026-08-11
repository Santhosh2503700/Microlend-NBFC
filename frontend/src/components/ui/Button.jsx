import { Loader2 } from "lucide-react";

const VARIANTS = {
    primary: "border-transparent bg-primary text-white hover:enabled:bg-primary-hover",
    secondary:
        "border-border bg-white text-ink hover:enabled:border-primary-hover hover:enabled:text-primary-hover",
    ghost: "border-transparent bg-transparent text-primary hover:enabled:bg-selection",
    danger: "border-transparent bg-danger text-white",
};

export function Button({
    variant = "primary",
    size = "md",
    block = false,
    loading = false,
    disabled,
    className,
    children,
    ...rest
}) {
    const base =
        "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md border font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60";
    const sizing =
        size === "sm"
            ? "min-h-[32px] px-3 text-sm max-md:min-h-[38px]"
            : "min-h-[40px] px-4 text-base max-md:min-h-[44px]";
    const classes = [base, sizing, VARIANTS[variant] ?? VARIANTS.primary, block ? "w-full" : "", className ?? ""]
        .filter(Boolean)
        .join(" ");
    return (
        <button className={classes} disabled={disabled || loading} {...rest}>
            {loading && <Loader2 size={16} className="animate-spin" />}
            {children}
        </button>
    );
}
