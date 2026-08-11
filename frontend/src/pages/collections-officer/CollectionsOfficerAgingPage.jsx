import { useCallback, useEffect, useMemo, useState } from "react";
import { TrendingUp, Gavel, MapPin, RefreshCw, Info } from "lucide-react";
import { collectionsOfficerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, StatusBadge, } from "@/components/ui";
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
const statusTone = (status) => {
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
};

const ESCALATION_ACTIONS = [
    { key: "legal", label: "Issue Legal Notice", icon: Gavel },
    { key: "visit", label: "Log Field Visit", icon: MapPin },
    { key: "restructure", label: "Initiate Restructuring", icon: RefreshCw },
];
export function CollectionsOfficerAgingPage() {
    const [cases, setCases] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [actionNote, setActionNote] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        collectionsOfficerApi
            .cases()
            .then(setCases)
            .catch((e) => setError(errorMessage(e, "Could not load the aging report")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const sorted = useMemo(() => [...cases].sort((a, b) => b.dpd - a.dpd), [cases]);
    const byBucket = useMemo(() => PAR_BUCKETS.reduce((acc, bucket) => {
        acc[bucket] = cases.filter((c) => c.parBucket === bucket).length;
        return acc;
    }, { CURRENT: 0, PAR30: 0, PAR60: 0, PAR90: 0, PAR180: 0 }), [cases]);
    const triggerAction = useCallback((action) => {
        const label = ESCALATION_ACTIONS.find((a) => a.key === action)?.label ?? action;
        setActionNote(`"${label}" would be recorded via a future endpoint (not yet implemented). Nothing has been saved.`);
    }, []);
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
            render: (c) => <StatusBadge tone={statusTone(c.status)}>{c.status}</StatusBadge>,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <TrendingUp size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          DPD Aging &amp; Escalation
        </h1>
        <p className="mt-2 text-ink-muted">Your assigned cases ranked by days-past-due, segmented into PAR aging buckets.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <h2 className="mb-4 text-lg font-semibold">Cases by PAR Bucket</h2>
      <Card>
        <div className="flex flex-wrap items-center gap-6">
          {PAR_BUCKETS.map((bucket) => (<div key={bucket} className="flex flex-col" style={{ minWidth: 96 }}>
              <StatusBadge tone={bucketTone(bucket)}>{bucket}</StatusBadge>
              <div style={{ fontSize: 22, fontWeight: 700, color: "#1C2826" }}>{byBucket[bucket]}</div>
            </div>))}
        </div>
      </Card>

      <h2 className="mb-4 text-lg font-semibold mt-6">Aging Report (highest DPD first)</h2>
      <Card>
        <DataTable columns={columns} rows={sorted} rowKey={(c) => c.caseId} loading={loading} emptyLabel="No cases are currently assigned to you"/>
      </Card>

      <h2 className="mb-4 text-lg font-semibold mt-6">Escalation Actions</h2>
      <Card>
        <Alert tone="info">
          <span className="flex items-center gap-2">
            <Info size={16}/>
            Escalation actions are not yet wired to the backend. Selecting one shows what would be
            recorded once the endpoint exists — no data is created or faked.
          </span>
        </Alert>

        <div className="flex flex-wrap items-center gap-2 mt-4">
          {ESCALATION_ACTIONS.map((a) => (<Button key={a.key} variant="secondary" onClick={() => triggerAction(a.key)}>
              <a.icon size={16}/> {a.label}
            </Button>))}
        </div>

        {actionNote && (<div className="mt-4">
            <Alert tone="info">{actionNote}</Alert>
          </div>)}
      </Card>
    </>);
}
