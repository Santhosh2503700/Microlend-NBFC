import { EmptyState } from "./EmptyState";
import { LoadingState } from "./LoadingState";

function cleanHeader(header) {
    if (typeof header === "string") {
        return header.replace(/#/g, "ID").replace(/ID\s*ID/g, "ID").trim();
    }
    return header;
}

function cleanValue(val) {
    if (typeof val === "string") {
        return val.replace(/#(\d+)/g, "$1").replace(/#/g, "").trim();
    }
    return val;
}

const WRAP =
    "max-w-full overflow-x-auto rounded-lg border border-border bg-card shadow-card max-md:overflow-visible max-md:border-0 max-md:bg-transparent max-md:shadow-none";
const TH_BASE =
    "border-b border-border bg-[#fbfcfc] px-4 py-3 text-left text-xs font-semibold uppercase tracking-[0.05em] text-ink-muted whitespace-nowrap";
const TR =
    "hover:bg-canvas max-md:mb-3 max-md:block max-md:rounded-lg max-md:border max-md:border-border max-md:bg-card max-md:px-4 max-md:py-2 max-md:shadow-card max-md:hover:bg-card max-md:[&>td:last-child]:border-b-0";
const TD_BASE =
    "border-b border-border px-4 py-3 text-ink max-md:flex max-md:items-center max-md:justify-between max-md:gap-4 max-md:px-0 max-md:py-2 max-md:text-right max-md:before:flex-none max-md:before:text-left max-md:before:text-xs max-md:before:font-semibold max-md:before:uppercase max-md:before:tracking-[0.05em] max-md:before:text-ink-muted max-md:before:content-[attr(data-label)]";


export function DataTable({ columns, rows, rowKey, loading = false, emptyLabel = "No records to display" }) {
    if (loading) {
        return (
            <div className={WRAP}>
                <LoadingState label="Loading…" />
            </div>
        );
    }
    if (rows.length === 0) {
        return (
            <div className={WRAP}>
                <EmptyState title={emptyLabel} />
            </div>
        );
    }
    return (
        <div className={WRAP}>
            <table className="w-full border-collapse text-base max-md:block">
                <thead className="max-md:hidden">
                    <tr>
                        {columns.map((c) => (
                            <th key={c.key} className={`${TH_BASE} ${c.numeric ? "text-center" : ""}`}>
                                {cleanHeader(c.header)}
                            </th>
                        ))}
                    </tr>
                </thead>
                <tbody className="max-md:block">
                    {rows.map((row) => (
                        <tr key={rowKey(row)} className={TR}>
                            {columns.map((c) => {
                                const content = c.render ? c.render(row) : String(row[c.key] ?? "");
                                const cleanedContent = cleanValue(content);
                                const cleanedHeader = cleanHeader(c.header);
                                return (
                                    <td
                                        key={c.key}
                                        className={`${TD_BASE} ${c.numeric ? "md:text-center tabular-nums" : ""}`}
                                        data-label={cleanedHeader}
                                    >
                                        {cleanedContent}
                                    </td>
                                );
                            })}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
