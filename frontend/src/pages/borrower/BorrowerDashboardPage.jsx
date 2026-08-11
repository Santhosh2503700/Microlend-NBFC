import { useCallback, useEffect, useState } from "react";
import { Wallet, HandCoins, CalendarClock, Layers, ClipboardCheck, CheckCircle2, FileSignature, Banknote, Clock, XCircle, } from "lucide-react";
import { borrowerApi } from "@/api/borrower";
import { errorMessage } from "@/api/client";
import { Alert, Card, KpiCard, LoadingState } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const formatDate = (iso) => new Date(iso).toLocaleDateString();
/** Borrower self-service home — live KPIs and the application pipeline tracker (Phase 8a). */
export function BorrowerDashboardPage() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        borrowerApi
            .dashboard()
            .then(setData)
            .catch((e) => setError(errorMessage(e, "Could not load your dashboard")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    if (loading)
        return <LoadingState label="Loading your dashboard"/>;
    if (error)
        return <Alert tone="error">{error}</Alert>;
    if (!data)
        return null;
    const pipeline = [
        { label: "Under assessment", count: data.applicationsUnderAssessment, icon: ClipboardCheck },
        { label: "Approved", count: data.applicationsApproved, icon: CheckCircle2 },
        { label: "Sanctioned", count: data.applicationsSanctioned, icon: FileSignature },
        { label: "Disbursed", count: data.applicationsDisbursed, icon: Banknote },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">My Dashboard</h1>
        <p className="mt-2 text-ink-muted">A live view of your loans, dues and application progress.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard icon={Wallet} label="Active Loan Balance" value={money(data.totalOutstandingPrincipal)}/>
        <KpiCard icon={HandCoins} label="Current Amount Due" value={money(data.amountCurrentlyDue)}/>
        <KpiCard icon={CalendarClock} label="Next Payment Date" value={data.nextDueDate
            ? `${formatDate(data.nextDueDate)}${data.nextDueAmount != null ? ` · ${money(data.nextDueAmount)}` : ""}`
            : "—"}/>
        <KpiCard icon={Layers} label="Active Loans" value={data.activeLoanCount}/>
      </div>

      <h2 className="mb-4 text-lg font-semibold mt-6">Application Tracker</h2>
      <Card>
        <div className="flex items-center gap-2" style={{ alignItems: "stretch", flexWrap: "wrap", justifyContent: "space-between" }}>
          {pipeline.map((stage, idx) => (<div key={stage.label} className="flex items-center gap-2" style={{ alignItems: "center", flex: "1 1 auto" }}>
              <div style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                gap: 6,
                padding: "12px 16px",
                minWidth: 120,
            }}>
                <span style={{
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                width: 44,
                height: 44,
                borderRadius: 12,
                background: stage.count > 0 ? "#004D40" : "#E0F2F1",
                color: stage.count > 0 ? "#FFFFFF" : "#004D40",
            }}>
                  <stage.icon size={22}/>
                </span>
                <div style={{ fontSize: 22, fontWeight: 700, color: "#1C2826" }}>
                  {stage.count}
                </div>
                <div className="text-sm" style={{ color: "#1C2826", textAlign: "center" }}>
                  {stage.label}
                </div>
              </div>
              {idx < pipeline.length - 1 && (<span aria-hidden style={{ color: "#0B8A75", fontSize: 20, fontWeight: 600 }}>
                  →
                </span>)}
            </div>))}
        </div>

        <div className="flex items-center gap-6 mt-4" style={{ flexWrap: "wrap" }}>
          <div className="flex items-center gap-2" style={{ alignItems: "center" }}>
            <Clock size={18} style={{ color: "#EF6C00" }}/>
            <span className="text-sm" style={{ color: "#1C2826" }}>
              Waitlisted: <strong>{data.applicationsWaitlisted}</strong>
            </span>
          </div>
          <div className="flex items-center gap-2" style={{ alignItems: "center" }}>
            <XCircle size={18} style={{ color: "#D32F2F" }}/>
            <span className="text-sm" style={{ color: "#1C2826" }}>
              Rejected: <strong>{data.applicationsRejected}</strong>
            </span>
          </div>
        </div>
      </Card>
    </>);
}
