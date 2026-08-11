import { forwardRef } from "react";
import { RequiredMark } from "./RequiredMark";

export const Input = forwardRef(function Input({ label, hint, error, icon, id, className, required, ...rest }, ref) {
    const control = [
        "flex min-h-[42px] w-full items-center gap-2 rounded-md border bg-white px-3 transition focus-within:ring-2 focus-within:ring-primary-hover/20 max-md:min-h-[46px]",
        error ? "border-danger" : "border-border focus-within:border-primary-hover",
    ].join(" ");
    return (
        <label className={["mb-4 block", className ?? ""].filter(Boolean).join(" ")} htmlFor={id}>
            {label && (
                <span className="mb-2 block text-sm font-semibold">
                    {label}
                    {required && <RequiredMark />}
                </span>
            )}
            <span className={control}>
                {icon && <span className="inline-flex text-ink-muted">{icon}</span>}
                <input
                    id={id}
                    ref={ref}
                    required={required}
                    className="min-w-0 flex-1 border-0 bg-transparent text-base text-ink outline-none"
                    {...rest}
                />
            </span>
            {error ? (
                <span className="mt-2 block text-xs text-danger">{error}</span>
            ) : (
                hint && <span className="mt-2 block text-xs text-ink-muted">{hint}</span>
            )}
        </label>
    );
});
