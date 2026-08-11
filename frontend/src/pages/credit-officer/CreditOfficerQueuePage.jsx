import { useCallback, useEffect, useMemo, useState } from "react";
import { ExternalLink, FileText, RefreshCw } from "lucide-react";
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
const BUCKET = {
    GREEN: { action: "APPROVE", label: "Approve", tone: "success" },
    AMBER: { action: "WAITLIST", label: "Move to Waiting List", tone: "warning" },
    RED: { action: "REJECT", label: "Reject", tone: "danger" },
};
const ACTION_LABEL = {
    APPROVE: "Approve",
    WAITLIST: "Move to Waiting List",
    REJECT: "Reject",
};
function asSanctionLetter(value) {
    if (value && typeof value === "object" && "sanctionId" in value) {
        return value;
    }
    return null;
}
export function CreditOfficerQueuePage() {
    const [apps, setApps] = useState([]);
    const [assessments, setAssessments] = useState({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filter, setFilter] = useState("ALL");
    const [selectedId, setSelectedId] = useState(null);
    const loadAssessments = useCallback((rows) => {
        rows.forEach((row) => {
            creditOfficerApi
                .assessment(row.applicationId)
                .then((a) => setAssessments((prev) => ({ ...prev, [row.applicationId]: a })))
                .catch(() => {
                /* individual assessment failures shouldn't break the queue */
            });
        });
    }, []);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        creditOfficerApi
            .queue()
            .then((rows) => {
            setApps(rows);
            loadAssessments(rows);
        })
            .catch((e) => setError(errorMessage(e, "Could not load the application queue")))
            .finally(() => setLoading(false));
    }, [loadAssessments]);
    useEffect(() => load(), [load]);
    const visible = useMemo(() => {
        if (filter === "ALL")
            return apps;
        return apps.filter((a) => assessments[a.applicationId]?.recommendation === filter);
    }, [apps, assessments, filter]);
    const selectedApp = apps.find((a) => a.applicationId === selectedId) ?? null;
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
            key: "status",
            header: "Status",
            render: (r) => <StatusBadge tone={toneForStatus(r.status)}>{r.status}</StatusBadge>,
        },
        {
            key: "bucket",
            header: "Bucket",
            render: (r) => {
                const a = assessments[r.applicationId];
                if (!a)
                    return <span className="text-sm">…</span>;
                return (<StatusBadge tone={BUCKET[a.recommendation].tone}>{a.recommendation}</StatusBadge>);
            },
        },
        {
            key: "open",
            header: "",
            render: (r) => (<Button size="sm" variant={selectedId === r.applicationId ? "primary" : "secondary"} onClick={() => setSelectedId(r.applicationId)}>
          Review
        </Button>),
        },
    ];
    if (loading)
        return <LoadingState label="Loading the application queue"/>;
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Application Processing Queue</h1>
        <p className="mt-2 text-ink-muted">Review each borrower, read the system assessment, and record your decision.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <div className="flex flex-wrap items-end gap-2 mb-4">
        <label className="mb-0 block min-w-[200px]">
          <span className="mb-2 block text-sm font-semibold">Filter by bucket</span>
          <Select value={filter} onChange={(e) => setFilter(e.target.value)} style={controlStyle}>
            <option value="ALL">All buckets</option>
            <option value="GREEN">Green — Eligible</option>
            <option value="AMBER">Amber — Conditional</option>
            <option value="RED">Red — Not Eligible</option>
          </Select>
        </label>
        <Button variant="ghost" size="sm" onClick={load}>
          <RefreshCw size={16}/> Refresh
        </Button>
      </div>

      <div className="grid grid-cols-1 items-start gap-5 lg:grid-cols-[1.15fr_1fr]">
        <Card pad={false}>
          <DataTable columns={columns} rows={visible} rowKey={(r) => r.applicationId} emptyLabel="No applications match this filter"/>
        </Card>

        <div>
          {selectedApp ? (<ReviewPane key={selectedApp.applicationId} application={selectedApp} assessment={assessments[selectedApp.applicationId]} onDecided={load}/>) : (<Card>
              <EmptyState icon={FileText} title="Select an application" description="Pick a row from the queue to review the borrower and decide."/>
            </Card>)}
        </div>
      </div>
    </>);
}
function ReviewPane({ application, assessment: cachedAssessment, onDecided }) {
    const [borrower, setBorrower] = useState(null);
    const [kyc, setKyc] = useState([]);
    const [assessment, setAssessment] = useState(cachedAssessment ?? null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [override, setOverride] = useState(false);
    const [action, setAction] = useState("APPROVE");
    const [overrideRemarks, setOverrideRemarks] = useState("");
    const [sanctionedAmount, setSanctionedAmount] = useState("");
    const [viewingKycId, setViewingKycId] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [decisionError, setDecisionError] = useState(null);
    const [sanctionLetter, setSanctionLetter] = useState(null);
    const [done, setDone] = useState(null);
    useEffect(() => {
        let alive = true;
        setLoading(true);
        setError(null);
        Promise.all([
            creditOfficerApi.borrower(application.borrowerId),
            creditOfficerApi.borrowerKyc(application.borrowerId),
            cachedAssessment
                ? Promise.resolve(cachedAssessment)
                : creditOfficerApi.assessment(application.applicationId),
        ])
            .then(([b, k, a]) => {
            if (!alive)
                return;
            setBorrower(b);
            setKyc(k);
            setAssessment(a);
            setAction(BUCKET[a.recommendation].action);
        })
            .catch((e) => {
            if (alive)
                setError(errorMessage(e, "Could not load the borrower review"));
        })
            .finally(() => {
            if (alive)
                setLoading(false);
        });
        return () => {
            alive = false;
        };
    }, [application.applicationId, application.borrowerId, cachedAssessment]);
    const effectiveAction = override
        ? action
        : assessment
            ? BUCKET[assessment.recommendation].action
            : "APPROVE";
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
            action: effectiveAction,
            sanctionedAmount: effectiveAction === "APPROVE" ? parsedAmount : null,
            override,
            overrideRemarks: override ? overrideRemarks.trim() : null,
        })
            .then((res) => {
            const letter = asSanctionLetter(res.sanctionLetter);
            if (letter)
                setSanctionLetter(letter);
            setDone(`Decision recorded: ${ACTION_LABEL[effectiveAction]}.`);
            onDecided();
        })
            .catch((e) => setDecisionError(errorMessage(e, "Could not record the decision")))
            .finally(() => setSubmitting(false));
    };
    if (loading) {
        return (<Card>
        <LoadingState label="Loading borrower review"/>
      </Card>);
    }
    if (error)
        return <Alert tone="error">{error}</Alert>;
    return (<div className="flex flex-col gap-4">
      <Card>
        <div className="flex items-center justify-between gap-2 mb-2">
          <h2 className="mb-4 text-lg font-semibold" style={{ margin: 0 }}>
            Application{application.applicationId}
          </h2>
          <StatusBadge tone={toneForStatus(application.status)}>{application.status}</StatusBadge>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {/* LEFT — borrower profile */}
          <div>
            <h3 className="text-sm font-bold mb-2">
              Borrower Profile
            </h3>
            {borrower && (<dl className="flex flex-col gap-2" style={{ margin: 0 }}>
                <Field label="Name" value={borrower.name}/>
                <Field label="National ID" value={borrower.nationalIdNumberMasked}/>
                <Field label="Phone" value={borrower.phone}/>
                <Field label="Location" value={`${borrower.village}, ${borrower.district}`}/>
                <Field label="Occupation" value={borrower.occupation ?? "—"}/>
                <Field label="Monthly Income" value={money(borrower.monthlyIncome)}/>
              </dl>)}
          </div>

          {/* RIGHT — system assessment */}
          <div>
            <h3 className="text-sm font-bold mb-2">
              System Assessment
            </h3>
            {assessment && (<dl className="flex flex-col gap-2" style={{ margin: 0 }}>
                <Field label="Credit Score" value={String(assessment.internalCreditScore)}/>
                <Field label="Debt Burden Ratio" value={`${(assessment.debtBurdenRatio * 100).toFixed(1)}%`}/>
                <div>
                  <dt className="text-sm text-ink-muted">
                    Recommendation
                  </dt>
                  <dd style={{ margin: "4px 0 0" }}>
                    <StatusBadge tone={BUCKET[assessment.recommendation].tone}>
                      {assessment.recommendation}
                    </StatusBadge>
                  </dd>
                </div>
                <Field label="Remarks" value={assessment.remarks || "—"}/>
                {assessment.assessmentType === "MANUAL_OVERRIDE" &&
                assessment.originalRecommendation && (<Field label="Original (pre-override)" value={assessment.originalRecommendation}/>)}
              </dl>)}
          </div>
        </div>
      </Card>

      {/* KYC documents */}
      <Card>
        <h3 className="text-sm font-bold mb-2">
          KYC Documents
        </h3>
        {kyc.length === 0 ? (<p className="text-sm">No KYC documents uploaded for this borrower.</p>) : (<div className="flex flex-col gap-2">
            {kyc.map((doc) => (<div key={doc.kycId} className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <FileText size={16} style={{ color: "#004D40" }}/>
                  <span className="text-sm" style={{ fontWeight: 600 }}>
                    {doc.documentType}
                  </span>
                  <StatusBadge tone={toneForStatus(doc.status)}>{doc.status}</StatusBadge>
                </div>
                <div className="flex items-center gap-2">
                  <Button size="sm" variant="ghost" onClick={() => setViewingKycId((cur) => (cur === doc.kycId ? null : doc.kycId))}>
                    {viewingKycId === doc.kycId ? "Hide" : "View"}
                  </Button>
                  <a href={creditOfficerApi.kycFileUrl(doc.documentFileUrl)} target="_blank" rel="noreferrer" className="inline-flex min-h-[32px] items-center justify-center gap-2 whitespace-nowrap rounded-md border border-transparent px-3 text-sm font-semibold no-underline transition-colors disabled:cursor-not-allowed disabled:opacity-60 max-md:min-h-[38px] text-primary hover:enabled:bg-selection">
                    <ExternalLink size={14}/> New tab
                  </a>
                </div>
                {viewingKycId === doc.kycId && (<iframe title={`KYC ${doc.kycId}`} src={creditOfficerApi.kycFileUrl(doc.documentFileUrl)} style={{
                        width: "100%",
                        height: 320,
                        border: "1px solid var(--color-border)",
                        borderRadius: "var(--radius-md)",
                    }}/>)}
              </div>))}
          </div>)}
      </Card>

      {/* Decision */}
      <Card>
        <h3 className="text-sm font-bold mb-2">
          Credit Decision
        </h3>
        {decisionError && <Alert tone="error">{decisionError}</Alert>}
        {done && !sanctionLetter && <Alert tone="info">{done}</Alert>}

        <label className="flex items-center gap-2 mb-4">
          <input type="checkbox" checked={override} onChange={(e) => setOverride(e.target.checked)}/>
          <span className="text-sm">Override the system recommendation</span>
        </label>

        {override && (<>
            <label className="mb-4 block">
              <span className="mb-2 block text-sm font-semibold">Decision<span className="ml-0.5 text-danger" title="Required">*</span></span>
              <Select value={action} onChange={(e) => setAction(e.target.value)} style={controlStyle}>
                <option value="APPROVE">Approve</option>
                <option value="WAITLIST">Move to Waiting List</option>
                <option value="REJECT">Reject</option>
              </Select>
            </label>
            <label className="mb-4 block">
              <span className="mb-2 block text-sm font-semibold">Override remarks<span className="ml-0.5 text-danger" title="Required">*</span></span>
              <textarea value={overrideRemarks} onChange={(e) => setOverrideRemarks(e.target.value)} rows={3} style={{ ...controlStyle, resize: "vertical" }} placeholder="Explain why you are overriding the automatic assessment"/>
            </label>
          </>)}

        {effectiveAction === "APPROVE" && (<Input label="Sanctioned amount (optional — defaults to requested)" type="number" min={1} value={sanctionedAmount} onChange={(e) => setSanctionedAmount(e.target.value)} hint={`Requested: ${money(application.requestedAmount)}`}/>)}

        <Button variant={effectiveAction === "REJECT"
            ? "danger"
            : effectiveAction === "WAITLIST"
                ? "secondary"
                : "primary"} loading={submitting} onClick={submit}>
          {ACTION_LABEL[effectiveAction]}
        </Button>
      </Card>

      {sanctionLetter && (<Card>
          <h3 className="mb-4 text-lg font-semibold" style={{ marginTop: 0 }}>
            Sanction Letter ·{sanctionLetter.sanctionId}
          </h3>
          <p className="text-sm mb-4">
            Auto-generated for {sanctionLetter.borrowerName}. Disbursement and repayment schedule
            follow automatically once the borrower accepts — no manual step.
          </p>
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
