import { useCallback, useEffect, useMemo, useState } from "react";
import { Users, Search } from "lucide-react";
import { branchManagerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Card, DataTable, Input, StatusBadge, toneForStatus, } from "@/components/ui";
/**
 * All-Borrower Master View for the Branch Manager (Phase 8d).
 * Rule 5: every row shows the registering Field Officer's name.
 */
export function BranchManagerBorrowersPage() {
    const [borrowers, setBorrowers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filter, setFilter] = useState("");
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        branchManagerApi
            .borrowers()
            .then(setBorrowers)
            .catch((e) => setError(errorMessage(e, "Could not load branch borrowers")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const filtered = useMemo(() => {
        const q = filter.trim().toLowerCase();
        if (!q)
            return borrowers;
        return borrowers.filter((b) => [
            b.name,
            b.phone,
            b.village,
            b.district,
            b.borrowerType,
            b.registeredByFieldOfficerName ?? "",
            b.status,
        ]
            .join(" ")
            .toLowerCase()
            .includes(q));
    }, [borrowers, filter]);
    const columns = [
        { key: "name", header: "Borrower", render: (b) => b.name },
        { key: "phone", header: "Phone", render: (b) => b.phone },
        {
            key: "location",
            header: "Village / District",
            render: (b) => `${b.village}, ${b.district}`,
        },
        { key: "borrowerType", header: "Type", render: (b) => b.borrowerType },
        {
            key: "registeredBy",
            header: "Registered by",
            render: (b) => b.registeredByFieldOfficerName ?? `Officer ${b.registeredByFieldOfficerId}`,
        },
        {
            key: "nationalId",
            header: "National ID",
            render: (b) => b.nationalIdNumberMasked,
        },
        {
            key: "status",
            header: "Status",
            render: (b) => <StatusBadge tone={toneForStatus(b.status)}>{b.status}</StatusBadge>,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <Users size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Branch Borrowers
        </h1>
        <p className="mt-2 text-ink-muted">Every borrower in your branch, with the Field Officer who registered them.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <Card>
        <div className="mb-4 w-full max-w-[360px]">
          <Input label="Filter" icon={<Search size={16}/>} placeholder="Search by name, phone, village, officer…" value={filter} onChange={(e) => setFilter(e.target.value)}/>
        </div>
        <DataTable columns={columns} rows={filtered} rowKey={(b) => b.borrowerId} loading={loading} emptyLabel="No borrowers match your filter"/>
      </Card>
    </>);
}
