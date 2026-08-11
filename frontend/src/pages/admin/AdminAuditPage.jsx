import { useEffect, useState } from "react";
import { ScrollText, Search } from "lucide-react";
import { adminApi } from "@/api/admin";
import { errorMessage } from "@/api/client";
import { Alert, Card, DataTable, Input, Select } from "@/components/ui";

const KNOWN_MODULES = [
    "AUTH",
    "BORROWER",
    "KYC",
    "CENTRE",
    "GROUP",
    "LOAN_PRODUCT",
    "LOAN_APPLICATION",
    "SANCTION_LETTER",
    "LOAN_ACCOUNT",
    "COLLECTION",
    "DELINQUENCY",
];
export function AdminAuditPage() {
    const [entries, setEntries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [moduleFilter, setModuleFilter] = useState("ALL");
    const [actionFilter, setActionFilter] = useState("");
    const [userIdFilter, setUserIdFilter] = useState("");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");
    useEffect(() => {
        let cancelled = false;
        const handle = setTimeout(() => {
            setLoading(true);
            setError(null);
            const userIdNum = Number(userIdFilter.trim());
            const filters = {
                module: moduleFilter === "ALL" ? undefined : moduleFilter,
                action: actionFilter.trim() || undefined,
                userId: userIdFilter.trim() && !Number.isNaN(userIdNum) ? userIdNum : undefined,
                from: fromDate || undefined,
                to: toDate || undefined,
            };
            adminApi
                .auditLog(filters)
                .then((rows) => {
                if (!cancelled)
                    setEntries(rows);
            })
                .catch((e) => {
                if (!cancelled)
                    setError(errorMessage(e, "Could not load the audit log"));
            })
                .finally(() => {
                if (!cancelled)
                    setLoading(false);
            });
        }, 300);
        return () => {
            cancelled = true;
            clearTimeout(handle);
        };
    }, [moduleFilter, actionFilter, userIdFilter, fromDate, toDate]);
    const columns = [
        { key: "timestamp", header: "Timestamp", render: (e) => new Date(e.timestamp).toLocaleString() },
        { key: "userName", header: "User", render: (e) => e.userName },
        { key: "action", header: "Action", render: (e) => e.action },
        { key: "module", header: "Module", render: (e) => e.module },
        { key: "details", header: "Details", render: (e) => e.details ?? "—" },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <ScrollText size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Audit Log
        </h1>
        <p className="mt-2 text-ink-muted">System-wide, tamper-evident activity trail. Newest events first.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <Card>
        <div className="mb-4" style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
            gap: 12,
            alignItems: "flex-end",
        }}>
          <label className="mb-4 block">
            <span className="mb-2 block text-sm font-semibold">Module</span>
            <Select className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]" value={moduleFilter} onChange={(e) => setModuleFilter(e.target.value)}>
              <option value="ALL">All modules</option>
              {KNOWN_MODULES.map((m) => (<option key={m} value={m}>
                  {m}
                </option>))}
            </Select>
          </label>
          <Input id="actionFilter" label="Action contains" value={actionFilter} onChange={(e) => setActionFilter(e.target.value)} placeholder="e.g. LOGIN" icon={<Search size={16}/>}/>
          <Input id="userIdFilter" label="User ID" type="number" value={userIdFilter} onChange={(e) => setUserIdFilter(e.target.value)} placeholder="e.g. 3"/>
          <Input id="fromDate" label="From" type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)}/>
          <Input id="toDate" label="To" type="date" value={toDate} onChange={(e) => setToDate(e.target.value)}/>
        </div>

        <div className="text-sm mb-4">{entries.length} events</div>

        <DataTable columns={columns} rows={entries} rowKey={(e) => e.auditId} loading={loading} emptyLabel="No audit events match these filters"/>
      </Card>
    </>);
}
