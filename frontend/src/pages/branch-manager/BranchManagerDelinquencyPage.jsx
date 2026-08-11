import { useCallback, useEffect, useState } from "react";
import { AlertTriangle } from "lucide-react";
import { branchManagerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, Select, StatusBadge, toneForStatus, } from "@/components/ui";
const formatDate = (iso) => new Date(iso).toLocaleDateString();
const bucketTone = (bucket) => {
    if (bucket === "CURRENT")
        return "success";
    if (bucket === "PAR30" || bucket === "PAR60")
        return "warning";
    return "danger";
};
const isOpen = (c) => c.assignedCollectionsOfficerId == null;
/** Delinquency Case Tracker with branch-scoped officer assignment (Phase 8d). */
export function BranchManagerDelinquencyPage() {
    const [cases, setCases] = useState([]);
    const [officers, setOfficers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [choice, setChoice] = useState({});
    const [assigningId, setAssigningId] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        Promise.all([branchManagerApi.delinquencyCases(), branchManagerApi.collectionsOfficers()])
            .then(([c, o]) => {
            setCases(c);
            setOfficers(o);
        })
            .catch((e) => setError(errorMessage(e, "Could not load delinquency cases")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const assign = useCallback((caseId) => {
        const officerId = choice[caseId];
        if (!officerId)
            return;
        setAssigningId(caseId);
        setError(null);
        branchManagerApi
            .assignCase(caseId, officerId)
            .then(() => load())
            .catch((e) => setError(errorMessage(e, "Could not assign the case")))
            .finally(() => setAssigningId(null));
    }, [choice, load]);
    const columns = [
        { key: "caseId", header: "Case", render: (c) => `${c.caseId}` },
        { key: "borrowerName", header: "Borrower", render: (c) => c.borrowerName },
        { key: "dpd", header: "DPD", numeric: true, render: (c) => c.dpd },
        {
            key: "parBucket",
            header: "PAR Bucket",
            render: (c) => <StatusBadge tone={bucketTone(c.parBucket)}>{c.parBucket}</StatusBadge>,
        },
        {
            key: "status",
            header: "Status",
            render: (c) => <StatusBadge tone={toneForStatus(c.status)}>{c.status}</StatusBadge>,
        },
        {
            key: "assignedOfficer",
            header: "Assigned Officer",
            render: (c) => c.assignedCollectionsOfficerName ??
                (c.assignedCollectionsOfficerId != null
                    ? `Officer ${c.assignedCollectionsOfficerId}`
                    : "Unassigned"),
        },
        { key: "openedDate", header: "Opened", render: (c) => formatDate(c.openedDate) },
        {
            key: "assign",
            header: "Assign",
            render: (c) => isOpen(c) ? (<div className="flex flex-wrap items-center gap-2">
            <label className="mb-0 block">
              <span className="mb-2 block text-sm font-semibold" style={{ position: "absolute", left: -9999 }}>
                Collections Officer for case{c.caseId}
              </span>
              <Select className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px] min-w-[180px]" value={choice[c.caseId] ?? ""} onChange={(e) => setChoice((prev) => ({ ...prev, [c.caseId]: Number(e.target.value) }))}>
                <option value="">Select officer…</option>
                {officers.map((o) => (<option key={o.userId} value={o.userId}>
                    {o.name}
                  </option>))}
              </Select>
            </label>
            <Button size="sm" onClick={() => assign(c.caseId)} loading={assigningId === c.caseId} disabled={!choice[c.caseId]}>
              Assign
            </Button>
          </div>) : (<span className="text-sm" style={{ color: "#1C2826" }}>
            Assigned
          </span>),
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <AlertTriangle size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Delinquency Cases
        </h1>
        <p className="mt-2 text-ink-muted">Open cases in your branch. Assign a Collections Officer to unassigned cases.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <Card>
        <DataTable columns={columns} rows={cases} rowKey={(c) => c.caseId} loading={loading} emptyLabel="No open delinquency cases in your branch"/>
      </Card>
    </>);
}
