import { Children, isValidElement, useEffect, useLayoutEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { ChevronDown } from "lucide-react";

export function readOptions(children) {
    return Children.toArray(children)
        .filter((child) => isValidElement(child) && child.type === "option")
        .map((child) => ({
            value: String(child.props.value ?? ""),
            label:
                typeof child.props.children === "string"
                    ? child.props.children
                    : Children.toArray(child.props.children).join(""),
            disabled: Boolean(child.props.disabled),
        }));
}


export function Select({
    id,
    value,
    onChange,
    required,
    disabled,
    className = "",
    style,
    placeholder,
    children,
    "aria-label": ariaLabel,
    "aria-labelledby": ariaLabelledBy,
}) {
    const options = readOptions(children);
    const [open, setOpen] = useState(false);
    const [rect, setRect] = useState(null);
    const triggerRef = useRef(null);
    const panelRef = useRef(null);

    const current = String(value ?? "");
    const selected = options.find((o) => o.value === current);
    const emptyOpt = options.find((o) => o.value === "");
    const displayLabel = selected ? selected.label : placeholder ?? emptyOpt?.label ?? "";
    const isPlaceholder = !selected || selected.value === "";

    const measure = () => {
        const el = triggerRef.current;
        if (!el) return;
        const r = el.getBoundingClientRect();
        setRect({ top: r.top, bottom: r.bottom, left: r.left, width: r.width, vh: window.innerHeight });
    };

    useLayoutEffect(() => {
        if (open) measure();
    }, [open]);

    useEffect(() => {
        if (!open) return;
        const onDocMouse = (e) => {
            if (triggerRef.current?.contains(e.target)) return;
            if (panelRef.current?.contains(e.target)) return;
            setOpen(false);
        };
        const onKey = (e) => {
            if (e.key === "Escape") setOpen(false);
        };
        const reposition = () => measure();
        document.addEventListener("mousedown", onDocMouse);
        document.addEventListener("keydown", onKey);
        window.addEventListener("resize", reposition);
        window.addEventListener("scroll", reposition, true);
        return () => {
            document.removeEventListener("mousedown", onDocMouse);
            document.removeEventListener("keydown", onKey);
            window.removeEventListener("resize", reposition);
            window.removeEventListener("scroll", reposition, true);
        };
    }, [open]);

    const pick = (optValue) => {
        setOpen(false);
        onChange?.({ target: { value: optValue } });
    };

    // Prefer opening downward; flip up only when there's clearly more room above.
    const spaceBelow = rect ? rect.vh - rect.bottom : 0;
    const spaceAbove = rect ? rect.top : 0;
    const flipUp = rect ? spaceBelow < 220 && spaceAbove > spaceBelow : false;
    const maxHeight = rect
        ? Math.min(280, Math.max(140, (flipUp ? spaceAbove : spaceBelow) - 8))
        : 280;

    const panelStyle = rect
        ? {
              position: "fixed",
              left: rect.left,
              width: rect.width,
              maxHeight,
              ...(flipUp ? { bottom: rect.vh - rect.top + 4 } : { top: rect.bottom + 4 }),
          }
        : undefined;

    return (
        <>
            <button
                ref={triggerRef}
                type="button"
                id={id}
                disabled={disabled}
                aria-haspopup="listbox"
                aria-expanded={open}
                aria-required={required || undefined}
                aria-label={ariaLabel}
                aria-labelledby={ariaLabelledBy}
                onClick={() => !disabled && setOpen((v) => !v)}
                className={`flex items-center justify-between gap-2 text-left ${
                    disabled ? "cursor-not-allowed opacity-60" : "cursor-pointer"
                } ${className}`}
                style={style}
            >
                <span className={`min-w-0 flex-1 truncate ${isPlaceholder ? "text-ink-muted" : ""}`}>
                    {displayLabel}
                </span>
                <ChevronDown
                    size={16}
                    className={`shrink-0 text-ink-muted transition-transform ${open ? "rotate-180" : ""}`}
                />
            </button>

            {open &&
                rect &&
                createPortal(
                    <ul
                        ref={panelRef}
                        role="listbox"
                        style={panelStyle}
                        className="z-[60] overflow-y-auto rounded-md border border-border bg-white py-1 shadow-pop"
                    >
                        {options.map((opt) => {
                            const active = opt.value === current;
                            return (
                                <li
                                    key={opt.value || "__placeholder"}
                                    role="option"
                                    aria-selected={active}
                                    aria-disabled={opt.disabled || undefined}
                                    onClick={() => !opt.disabled && pick(opt.value)}
                                    className={`cursor-pointer truncate px-3 py-2 text-base transition ${
                                        opt.disabled
                                            ? "cursor-not-allowed text-ink-muted opacity-60"
                                            : active
                                              ? "bg-selection text-primary"
                                              : "text-ink hover:bg-canvas"
                                    } ${opt.value === "" ? "text-ink-muted" : ""}`}
                                >
                                    {opt.label}
                                </li>
                            );
                        })}
                    </ul>,
                    document.body,
                )}
        </>
    );
}
