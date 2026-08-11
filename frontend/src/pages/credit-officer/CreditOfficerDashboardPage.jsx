import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ClipboardList, FileSearch, CheckCircle2, Clock, ArrowRight } from "lucide-react";
import { creditOfficerApi } from "@/api/loans";
import { errorMessage } from "@/api/client";
import { Alert, Card, KpiCard, LoadingState, StatusBadge, toneForStatus } from "@/components/ui";
const STATUS_LABEL = {
    UNDER_ASSESSMENT: "Under Assessment",
    APPROVED: "Approved",
    WAITLISTED: "Waitlisted",
    REJECTED: "Rejected",
    SANCTIONED: "Sanctioned",
    DISBURSED: "Disbursed",
};
export function CreditOfficerDashboardPage() {
    const [apps, setApps] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        creditOfficerApi
            .queue()
            .then(setApps)
            .catch((e) => setError(errorMessage(e, "Could not load the application queue")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const counts = useMemo(() => {
        const byStatus = new Map();
        for (const a of apps)
            byStatus.set(a.status, (byStatus.get(a.status) ?? 0) + 1);
        return {
            total: apps.length,
            underAssessment: byStatus.get("UNDER_ASSESSMENT") ?? 0,
            sanctioned: (byStatus.get("APPROVED") ?? 0) +
                (byStatus.get("SANCTIONED") ?? 0) +
                (byStatus.get("DISBURSED") ?? 0),
            waitlisted: byStatus.get("WAITLISTED") ?? 0,
            byStatus,
        };
    }, [apps]);
    if (loading)
        return <LoadingState label="Loading your workbench"/>;
    if (error)
        return <Alert tone="error">{error}</Alert>;
    const distribution = Object.keys(STATUS_LABEL)
        .map((s) => ({ status: s, count: counts.byStatus.get(s) ?? 0 }))
        .filter((d) => d.count > 0);
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Credit Workbench</h1>
        <p className="mt-2 text-ink-muted">A live view of every application awaiting a credit decision.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard icon={ClipboardList} label="Total Applications" value={counts.total}/>
        <KpiCard icon={FileSearch} label="Under Assessment" value={counts.underAssessment}/>
        <KpiCard icon={CheckCircle2} label="Approved / Sanctioned" value={counts.sanctioned}/>
        <KpiCard icon={Clock} label="Waitlisted" value={counts.waitlisted}/>
      </div>

      <h2 className="mb-4 text-lg font-semibold mt-6">Status Distribution</h2>
      <Card>
        {distribution.length === 0 ? (<p className="text-sm">No applications in the pipeline yet.</p>) : (<div className="flex flex-wrap items-center gap-6">
            {distribution.map((d) => (<div key={d.status} className="flex items-center gap-2 min-w-[160px]">
                <StatusBadge tone={toneForStatus(d.status)}>{STATUS_LABEL[d.status]}</StatusBadge>
                <strong style={{ fontSize: 18, color: "#1C2826" }}>{d.count}</strong>
              </div>))}
          </div>)}

        <div className="mt-4">
          <Link to="/co/applications" className="flex items-center gap-2 font-semibold" style={{ color: "#004D40" }}>
            Go to the processing queue
            <ArrowRight size={16}/>
          </Link>
        </div>
      </Card>
    </>);
}
