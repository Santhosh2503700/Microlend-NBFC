export function Card({ pad = true, className, children, ...rest }) {
    const classes = [
        "min-w-0 rounded-lg border border-border bg-card shadow-card",
        pad ? "p-5" : "",
        className ?? "",
    ]
        .filter(Boolean)
        .join(" ");
    return (
        <div className={classes} {...rest}>
            {children}
        </div>
    );
}
