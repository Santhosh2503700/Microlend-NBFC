import { useCallback, useEffect, useState } from "react";
import { FileText, FileSignature, CheckCircle2 } from "lucide-react";
import { borrowerApi } from "@/api/borrower";
import { loanApi } from "@/api/loans";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, EmptyState, LoadingState, StatusBadge, toneForStatus, } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const formatDate = (iso) => new Date(iso).toLocaleDateString();
/** Borrower application tracker + sanction-letter accept/reject (Phase 8a). */
export function BorrowerSanctionPage() {
    const [applications, setApplications] = useState([]);
    const [appsLoading, setAppsLoading] = useState(true);
    const [letters, setLetters] = useState([]);
    const [lettersLoading, setLettersLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [busyId, setBusyId] = useState(null);
    const loadApplications = useCallback(() => {
        setAppsLoading(true);
        loanApi
            .listApplications()
            .then(setApplications)
            .catch((e) => setError(errorMessage(e, "Could not load your applications")))
            .finally(() => setAppsLoading(false));
    }, []);
    const loadLetters = useCallback(() => {
        setLettersLoading(true);
        borrowerApi
            .sanctionLetters()
            .then(setLetters)
            .catch((e) => setError(errorMessage(e, "Could not load your sanction letters")))
            .finally(() => setLettersLoading(false));
    }, []);
    useEffect(() => {
        loadApplications();
        loadLetters();
    }, [loadApplications, loadLetters]);
    const accept = async (id) => {
        setBusyId(id);
        setError(null);
        setSuccess(null);
        try {
            await borrowerApi.acceptSanction(id);
            setSuccess("Loan accepted — disbursement complete and repayment schedule generated. Check My Loans for your new account.");
            loadLetters();
            loadApplications();
        }
        catch (e) {
            setError(errorMessage(e, "Could not accept the sanction letter"));
        }
        finally {
            setBusyId(null);
        }
    };
    const reject = async (id) => {
        if (!window.confirm("Reject this sanction letter? This cannot be undone."))
            return;
        setBusyId(id);
        setError(null);
        setSuccess(null);
        try {
            await borrowerApi.rejectSanction(id);
            loadLetters();
            loadApplications();
        }
        catch (e) {
            setError(errorMessage(e, "Could not reject the sanction letter"));
        }
        finally {
            setBusyId(null);
        }
    };
    const appColumns = [
        { key: "applicationId", header: "Application", render: (a) => `${a.applicationId}` },
        { key: "productName", header: "Product", render: (a) => a.productName },
        {
            key: "requestedAmount",
            header: "Requested",
            numeric: true,
            render: (a) => money(a.requestedAmount),
        },
        { key: "applicationDate", header: "Applied", render: (a) => formatDate(a.applicationDate) },
        {
            key: "status",
            header: "Status",
            render: (a) => <StatusBadge tone={toneForStatus(a.status)}>{a.status}</StatusBadge>,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Applications & Sanction Letters</h1>
        <p className="mt-2 text-ink-muted">Track your applications and act on issued sanction letters.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}
      {success && (<Alert tone="info">
          <CheckCircle2 size={16} style={{ verticalAlign: "-3px", marginRight: 6 }}/>
          {success}
        </Alert>)}

      <h2 className="mb-4 text-lg font-semibold">
        <FileText size={18} style={{ verticalAlign: "-3px", marginRight: 6 }}/>
        Application Tracker
      </h2>
      <Card>
        <DataTable columns={appColumns} rows={applications} rowKey={(a) => a.applicationId} loading={appsLoading} emptyLabel="You have not submitted any applications yet"/>
      </Card>

      <h2 className="mb-4 text-lg font-semibold mt-6">
        <FileSignature size={18} style={{ verticalAlign: "-3px", marginRight: 6 }}/>
        Sanction Letters
      </h2>

      {lettersLoading ? (<LoadingState label="Loading sanction letters"/>) : letters.length === 0 ? (<EmptyState title="No sanction letters" description="Once a Credit Officer approves an application, your sanction letter appears here." icon={FileSignature}/>) : (<div className="flex flex-col gap-4">
          {letters.map((l) => {
                const canAct = l.status === "ISSUED" && !l.acceptedByBorrower;
                const busy = busyId === l.sanctionId;
                return (<Card key={l.sanctionId}>
                <div className="flex items-center" style={{ justifyContent: "space-between", alignItems: "center" }}>
                  <h3 style={{ margin: 0 }}>
                    Sanction{l.sanctionId} · {l.borrowerName}
                  </h3>
                  <StatusBadge tone={toneForStatus(l.status)}>{l.status}</StatusBadge>
                </div>

                <div className="mt-4" style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))",
                        gap: 16,
                    }}>
                  <DetailItem label="Sanctioned Amount" value={money(l.sanctionedAmount)}/>
                  <DetailItem label="Interest Rate" value={`${l.interestRate}%`}/>
                  <DetailItem label="Tenure" value={`${l.tenure} months`}/>
                  <DetailItem label="EMI Amount" value={money(l.emiAmount)}/>
                  <DetailItem label="Issued Date" value={formatDate(l.issuedDate)}/>
                </div>

                <div className="mt-4">
                  <div className="mb-2 block text-sm font-semibold">Disbursal Conditions</div>
                  <p className="text-sm" style={{ margin: "4px 0 0", color: "#1C2826" }}>
                    {l.disbursalConditions}
                  </p>
                </div>

                {canAct && (<div className="flex flex-wrap items-center gap-2 mt-4">
                    <Button loading={busy} onClick={() => accept(l.sanctionId)}>
                      Accept
                    </Button>
                    <Button variant="danger" disabled={busy} onClick={() => reject(l.sanctionId)}>
                      Reject
                    </Button>
                  </div>)}
              </Card>);
            })}
        </div>)}
    </>);
}
function DetailItem({ label, value }) {
    return (<div>
      <div className="mb-2 block text-sm font-semibold">{label}</div>
      <div style={{ fontWeight: 600, color: "#1C2826", marginTop: 2 }}>{value}</div>
    </div>);
}
