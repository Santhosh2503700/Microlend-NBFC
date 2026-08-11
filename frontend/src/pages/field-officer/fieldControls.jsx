import { Select } from "@/components/ui/Select";
import { RequiredMark } from "@/components/ui/RequiredMark";

/** Shared INR money formatter (rule: Intl.NumberFormat en-IN). */
export const inr = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
});
/** Localised date, tolerant of null. */
export function fmtDate(iso) {
    return iso ? new Date(iso).toLocaleDateString() : "—";
}

const CONTROL_SHELL =
    "min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]";


export function SelectField({ label, hint, children, id, value, onChange, required, disabled }) {
    return (
        <div className="mb-4 block">
            {label && (
                <span id={`${id}-label`} className="mb-2 block text-sm font-semibold">
                    {label}
                    {required && <RequiredMark />}
                </span>
            )}
            <Select
                id={id}
                value={value}
                onChange={onChange}
                required={required}
                disabled={disabled}
                aria-labelledby={label ? `${id}-label` : undefined}
                className={CONTROL_SHELL}
            >
                {children}
            </Select>
            {hint && <span className="mt-2 block text-xs text-ink-muted">{hint}</span>}
        </div>
    );
}

export function TextareaField({ label, hint, id, rows = 3, required, ...rest }) {
    return (
        <label className="mb-4 block" htmlFor={id}>
            {label && (
                <span className="mb-2 block text-sm font-semibold">
                    {label}
                    {required && <RequiredMark />}
                </span>
            )}
            <textarea
                required={required}
                id={id}
                rows={rows}
                className="w-full resize-y rounded-md border border-border bg-white p-3 text-base text-ink outline-none transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20"
                {...rest}
            />
            {hint && <span className="mt-2 block text-xs text-ink-muted">{hint}</span>}
        </label>
    );
}

export function MultiSelectField({ label, hint, children, id, ...rest }) {
    return (
        <label className="mb-4 block" htmlFor={id}>
            {label && <span className="mb-2 block text-sm font-semibold">{label}</span>}
            <select
                id={id}
                multiple
                className="min-h-[120px] w-full rounded-md border border-border bg-white p-2 text-base text-ink outline-none transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20"
                {...rest}
            >
                {children}
            </select>
            {hint && <span className="mt-2 block text-xs text-ink-muted">{hint}</span>}
        </label>
    );
}
