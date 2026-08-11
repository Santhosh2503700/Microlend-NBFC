import { useCallback, useEffect, useState } from "react";
import { Gavel, MapPin, RefreshCw, ListChecks, Info } from "lucide-react";
import { collectionsOfficerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, StatusBadge, } from "@/components/ui";
const formatDate = (iso) => (iso ? new Date(iso).toLocaleDateString() : "—");
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

const CASE_ACTIONS = [
    { key: "legal", label: "Issue Legal Notice", icon: Gavel },
    { key: "visit", label: "Log Field Visit", icon: MapPin },
    { key: "restructure", label: "Initiate Restructuring", icon: RefreshCw },
];
export function CollectionsOfficerCasesPage() {
    const [cases, setCases] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [actionNote, setActionNote] = useState(null);
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
    const selectCase = useCallback((c) => {
        setSelected(c);
        setActionNote(null);
    }, []);
    const triggerAction = useCallback((action) => {
        if (!selected)
            return;
        const label = CASE_ACTIONS.find((a) => a.key === action)?.label ?? action;
        // UI-only stub — no server call is made; a real endpoint will back this later.
        setActionNote(`"${label}" for ${selected.borrowerName} (Case ${selected.caseId}) would be recorded via a future endpoint (not yet implemented). Nothing has been saved.`);
    }, [selected]);
    const columns = [
        { key: "caseId", header: "Case", render: (c) => `${c.caseId}` },
        { key: "borrowerName", header: "Borrower", render: (c) => c.borrowerName },
        { key: "loanAccountId", header: "Loan", render: (c) => `${c.loanAccountId}` },
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
        { key: "openedDate", header: "Opened", render: (c) => formatDate(c.openedDate) },
        { key: "assignedDate", header: "Assigned", render: (c) => formatDate(c.assignedDate) },
        {
            key: "actions",
            header: "",
            render: (c) => (<Button size="sm" variant={selected?.caseId === c.caseId ? "primary" : "secondary"} onClick={() => selectCase(c)}>
          <ListChecks size={16}/> Actions
        </Button>),
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <ListChecks size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          My Assigned Cases
        </h1>
        <p className="mt-2 text-ink-muted">Delinquency cases assigned to you. Select a case to record a collections action.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <Card>
        <DataTable columns={columns} rows={cases} rowKey={(c) => c.caseId} loading={loading} emptyLabel="No cases are currently assigned to you"/>
      </Card>

      {selected && (<>
          <h2 className="mb-4 text-lg font-semibold mt-6">
            Case Actions — {selected.borrowerName} (Case{selected.caseId})
          </h2>
          <Card>
            <Alert tone="info">
              <span className="flex items-center gap-2">
                <Info size={16}/>
                These actions are not yet wired to the backend. Selecting one shows what would be
                recorded once the endpoint exists — no data is created or faked.
              </span>
            </Alert>

            <div className="flex flex-wrap items-center gap-2 mt-4">
              {CASE_ACTIONS.map((a) => (<Button key={a.key} variant="secondary" onClick={() => triggerAction(a.key)}>
                  <a.icon size={16}/> {a.label}
                </Button>))}
            </div>

            {actionNote && (<div className="mt-4">
                <Alert tone="info">{actionNote}</Alert>
              </div>)}
          </Card>
        </>)}
    </>);
}
