import { useCallback, useEffect, useState } from "react";
import { RefreshCw } from "lucide-react";
import { creditOfficerApi } from "@/api/loans";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, EmptyState, Input, LoadingState, Select, StatusBadge, toneForStatus, } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const formatDate = (iso) => new Date(iso).toLocaleDateString();
const controlStyle = {
    width: "100%",
    minHeight: 42,
    padding: "9px 12px",
    border: "1px solid var(--color-border)",
    borderRadius: "var(--radius-md)",
    background: "#fff",
    fontSize: "var(--fs-base)",
    color: "var(--color-text)",
    fontFamily: "inherit",
};
const ACTION_LABEL = {
    APPROVE: "Approve",
    WAITLIST: "Keep Waitlisted",
    REJECT: "Reject",
};
function asSanctionLetter(value) {
    if (value && typeof value === "object" && "sanctionId" in value) {
        return value;
    }
    return null;
}
/** Amber-bucket waiting list — re-open any application into a compact inline decision panel. */
export function CreditOfficerWaitlistPage() {
    const [apps, setApps] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedId, setSelectedId] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        creditOfficerApi
            .waitlisted()
            .then(setApps)
            .catch((e) => setError(errorMessage(e, "Could not load the waiting list")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const selected = apps.find((a) => a.applicationId === selectedId) ?? null;
    const columns = [
        { key: "applicationId", header: "App", render: (r) => `${r.applicationId}` },
        { key: "borrowerName", header: "Borrower", render: (r) => r.borrowerName },
        { key: "productName", header: "Product", render: (r) => r.productName },
        {
            key: "requestedAmount",
            header: "Requested",
            numeric: true,
            render: (r) => money(r.requestedAmount),
        },
        { key: "applicationDate", header: "Applied", render: (r) => formatDate(r.applicationDate) },
        {
            key: "reopen",
            header: "",
            render: (r) => (<Button size="sm" variant={selectedId === r.applicationId ? "primary" : "secondary"} onClick={() => setSelectedId(r.applicationId)}>
          Re-open
        </Button>),
        },
    ];
    if (loading)
        return <LoadingState label="Loading the waiting list"/>;
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Waiting List</h1>
        <p className="mt-2 text-ink-muted">Conditionally-approved (Amber) applications awaiting a final credit decision.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <div className="flex items-center gap-2 mb-4">
        <Button variant="ghost" size="sm" onClick={load}>
          <RefreshCw size={16}/> Refresh
        </Button>
      </div>

      <div className="grid grid-cols-1 items-start gap-5 lg:grid-cols-[1.15fr_1fr]">
        <Card pad={false}>
          <DataTable columns={columns} rows={apps} rowKey={(r) => r.applicationId} emptyLabel="No applications are currently waitlisted"/>
        </Card>

        <div>
          {selected ? (<DecisionPanel key={selected.applicationId} application={selected} onDecided={load}/>) : (<Card>
              <EmptyState title="Re-open an application" description="Pick a waitlisted application to review and decide."/>
            </Card>)}
        </div>
      </div>
    </>);
}
function DecisionPanel({ application, onDecided }) {
    const [assessment, setAssessment] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [action, setAction] = useState("APPROVE");
    const [override, setOverride] = useState(false);
    const [overrideRemarks, setOverrideRemarks] = useState("");
    const [sanctionedAmount, setSanctionedAmount] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [decisionError, setDecisionError] = useState(null);
    const [sanctionLetter, setSanctionLetter] = useState(null);
    const [done, setDone] = useState(null);
    useEffect(() => {
        let alive = true;
        setLoading(true);
        setError(null);
        creditOfficerApi
            .assessment(application.applicationId)
            .then((a) => {
            if (alive)
                setAssessment(a);
        })
            .catch((e) => {
            if (alive)
                setError(errorMessage(e, "Could not load the system assessment"));
        })
            .finally(() => {
            if (alive)
                setLoading(false);
        });
        return () => {
            alive = false;
        };
    }, [application.applicationId]);
    const submit = () => {
        if (override && overrideRemarks.trim().length === 0) {
            setDecisionError("An override remark is required.");
            return;
        }
        setSubmitting(true);
        setDecisionError(null);
        setSanctionLetter(null);
        setDone(null);
        const parsedAmount = sanctionedAmount.trim() ? Number(sanctionedAmount) : null;
        creditOfficerApi
            .decide(application.applicationId, {
            action,
            sanctionedAmount: action === "APPROVE" ? parsedAmount : null,
            override,
            overrideRemarks: override ? overrideRemarks.trim() : null,
        })
            .then((res) => {
            const letter = asSanctionLetter(res.sanctionLetter);
            if (letter)
                setSanctionLetter(letter);
            setDone(`Decision recorded: ${ACTION_LABEL[action]}.`);
            onDecided();
        })
            .catch((e) => setDecisionError(errorMessage(e, "Could not record the decision")))
            .finally(() => setSubmitting(false));
    };
    return (<div className="flex flex-col gap-4">
      <Card>
        <div className="flex items-center justify-between gap-2 mb-2">
          <h2 className="mb-4 text-lg font-semibold" style={{ margin: 0 }}>
            Application{application.applicationId}
          </h2>
          <StatusBadge tone={toneForStatus(application.status)}>{application.status}</StatusBadge>
        </div>
        <dl className="flex flex-col gap-2" style={{ margin: 0 }}>
          <Field label="Borrower" value={application.borrowerName}/>
          <Field label="Product" value={application.productName}/>
          <Field label="Requested" value={money(application.requestedAmount)}/>
          <Field label="Applied" value={formatDate(application.applicationDate)}/>
        </dl>

        <h3 className="text-sm mt-4 font-bold mb-2">
          System Assessment
        </h3>
        {loading ? (<LoadingState label="Loading assessment"/>) : error ? (<Alert tone="error">{error}</Alert>) : (assessment && (<dl className="flex flex-col gap-2" style={{ margin: 0 }}>
              <Field label="Credit Score" value={String(assessment.internalCreditScore)}/>
              <Field label="Debt Burden Ratio" value={`${(assessment.debtBurdenRatio * 100).toFixed(1)}%`}/>
              <div>
                <dt className="text-sm text-ink-muted">
                  Recommendation
                </dt>
                <dd style={{ margin: "4px 0 0" }}>
                  <StatusBadge tone={toneForStatus(assessment.recommendation)}>
                    {assessment.recommendation}
                  </StatusBadge>
                </dd>
              </div>
              <Field label="Remarks" value={assessment.remarks || "—"}/>
            </dl>))}
      </Card>

      <Card>
        <h3 className="text-sm font-bold mb-2">
          Decision
        </h3>
        {decisionError && <Alert tone="error">{decisionError}</Alert>}
        {done && !sanctionLetter && <Alert tone="info">{done}</Alert>}

        <label className="mb-4 block">
          <span className="mb-2 block text-sm font-semibold">Action<span className="ml-0.5 text-danger" title="Required">*</span></span>
          <Select value={action} onChange={(e) => setAction(e.target.value)} style={controlStyle}>
            <option value="APPROVE">Approve</option>
            <option value="REJECT">Reject</option>
            <option value="WAITLIST">Keep Waitlisted</option>
          </Select>
        </label>

        <label className="flex items-center gap-2 mb-4">
          <input type="checkbox" checked={override} onChange={(e) => setOverride(e.target.checked)}/>
          <span className="text-sm">Override the system recommendation</span>
        </label>

        {override && (<label className="mb-4 block">
            <span className="mb-2 block text-sm font-semibold">Override remarks<span className="ml-0.5 text-danger" title="Required">*</span></span>
            <textarea value={overrideRemarks} onChange={(e) => setOverrideRemarks(e.target.value)} rows={3} style={{ ...controlStyle, resize: "vertical" }} placeholder="Explain why you are overriding the automatic assessment"/>
          </label>)}

        {action === "APPROVE" && (<Input label="Sanctioned amount (optional — defaults to requested)" type="number" min={1} value={sanctionedAmount} onChange={(e) => setSanctionedAmount(e.target.value)} hint={`Requested: ${money(application.requestedAmount)}`}/>)}

        <Button variant={action === "REJECT" ? "danger" : action === "WAITLIST" ? "secondary" : "primary"} loading={submitting} onClick={submit}>
          {ACTION_LABEL[action]}
        </Button>
      </Card>

      {sanctionLetter && (<Card>
          <h3 className="mb-4 text-lg font-semibold" style={{ marginTop: 0 }}>
            Sanction Letter ·{sanctionLetter.sanctionId}
          </h3>
          <dl className="flex flex-col gap-2" style={{ margin: 0 }}>
            <Field label="Sanctioned Amount" value={money(sanctionLetter.sanctionedAmount)}/>
            <Field label="Interest Rate" value={`${sanctionLetter.interestRate}%`}/>
            <Field label="Tenure" value={`${sanctionLetter.tenure} months`}/>
            <Field label="EMI (this loan)" value={money(sanctionLetter.emiAmount)}/>
            <Field label="Disbursal Conditions" value={sanctionLetter.disbursalConditions || "—"}/>
          </dl>
        </Card>)}
    </div>);
}
function Field({ label, value }) {
    return (<div>
      <dt className="text-sm text-ink-muted">
        {label}
      </dt>
      <dd style={{ margin: "2px 0 0", color: "#1C2826", fontWeight: 500 }}>{value}</dd>
    </div>);
}
