import { useCallback, useEffect, useMemo, useState } from "react";
import { Briefcase, FolderOpen, CheckCircle2, AlertTriangle } from "lucide-react";
import { collectionsOfficerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Card, DataTable, KpiCard, StatusBadge, } from "@/components/ui";
const PAR_BUCKETS = ["CURRENT", "PAR30", "PAR60", "PAR90", "PAR180"];
const bucketTone = (bucket) => {
    switch (bucket) {
        case "CURRENT":
            return "success";
        case "PAR30":
            return "info";
        case "PAR60":
            return "warning";
        case "PAR90":
        case "PAR180":
            return "danger";
        default:
            return "neutral";
    }
};
export function CollectionsOfficerDashboardPage() {
    const [cases, setCases] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        collectionsOfficerApi
            .cases()
            .then(setCases)
            .catch((e) => setError(errorMessage(e, "Could not load your assigned cases")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const stats = useMemo(() => {
        const active = cases.filter((c) => c.status === "OPEN" || c.status === "ASSIGNED" || c.status === "IN_PROGRESS").length;
        const resolved = cases.filter((c) => c.status === "RESOLVED").length;
        const highestDpd = cases.reduce((max, c) => Math.max(max, c.dpd), 0);
        const byBucket = PAR_BUCKETS.reduce((acc, bucket) => {
            acc[bucket] = cases.filter((c) => c.parBucket === bucket).length;
            return acc;
        }, { CURRENT: 0, PAR30: 0, PAR60: 0, PAR90: 0, PAR180: 0 });
        return { active, resolved, highestDpd, byBucket };
    }, [cases]);
    const recent = useMemo(() => [...cases].sort((a, b) => new Date(b.openedDate).getTime() - new Date(a.openedDate).getTime()).slice(0, 8), [cases]);
    const columns = [
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
            render: (c) => <StatusBadge tone={toneForCaseStatus(c.status)}>{c.status}</StatusBadge>,
        },
    ];
    if (error)
        return <Alert tone="error">{error}</Alert>;
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <Briefcase size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Collections Dashboard
        </h1>
        <p className="mt-2 text-ink-muted">A live view of the delinquency cases assigned to you.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard icon={Briefcase} label="Assigned Cases" value={cases.length}/>
        <KpiCard icon={FolderOpen} label="Open / Assigned / In-Progress" value={stats.active}/>
        <KpiCard icon={CheckCircle2} label="Resolved" value={stats.resolved}/>
        <KpiCard icon={AlertTriangle} label="Highest DPD" value={stats.highestDpd}/>
      </div>

      <h2 className="mb-4 text-lg font-semibold mt-6">Breakdown by PAR Bucket</h2>
      <Card>
        <div className="flex flex-wrap items-center gap-6">
          {PAR_BUCKETS.map((bucket) => (<div key={bucket} className="flex flex-col" style={{ minWidth: 96 }}>
              <StatusBadge tone={bucketTone(bucket)}>{bucket}</StatusBadge>
              <div style={{ fontSize: 22, fontWeight: 700, color: "#1C2826" }}>
                {stats.byBucket[bucket]}
              </div>
            </div>))}
        </div>
      </Card>

      <h2 className="mb-4 text-lg font-semibold mt-6">Recent Cases</h2>
      <Card>
        <DataTable columns={columns} rows={recent} rowKey={(c) => c.caseId} loading={loading} emptyLabel="No cases are currently assigned to you"/>
      </Card>
    </>);
}
function toneForCaseStatus(status) {
    switch (status) {
        case "RESOLVED":
            return "success";
        case "IN_PROGRESS":
            return "info";
        case "ASSIGNED":
            return "warning";
        case "OPEN":
            return "danger";
        default:
            return "neutral";
    }
}
