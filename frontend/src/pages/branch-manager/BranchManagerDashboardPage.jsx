import { useCallback, useEffect, useState } from "react";
import { Users, AlertTriangle, FileWarning, UserCog } from "lucide-react";
import { branchManagerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, KpiCard, LoadingState } from "@/components/ui";
import { AnalyticsCharts } from "@/components/charts/AnalyticsCharts";
export function BranchManagerDashboardPage() {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        Promise.all([
            branchManagerApi.borrowers(),
            branchManagerApi.delinquencyCases(),
            branchManagerApi.disputes(),
            branchManagerApi.officers(),
        ])
            .then(([borrowers, cases, disputes, officers]) => setData({
            borrowerCount: borrowers.length,
            cases,
            disputeCount: disputes.length,
            officerCount: officers.length,
        }))
            .catch((e) => setError(errorMessage(e, "Could not load the branch dashboard")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    if (loading)
        return <LoadingState label="Loading branch dashboard"/>;
    if (error)
        return <Alert tone="error">{error}</Alert>;
    if (!data)
        return null;
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Branch Dashboard</h1>
        <p className="mt-2 text-ink-muted">A live view of your branch portfolio, delinquency and staff.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <KpiCard icon={Users} label="Borrowers in Branch" value={data.borrowerCount}/>
        <KpiCard icon={AlertTriangle} label="Open Delinquency Cases" value={data.cases.length}/>
        <KpiCard icon={FileWarning} label="Pending Receipt Disputes" value={data.disputeCount}/>
        <KpiCard icon={UserCog} label="Officers in Branch" value={data.officerCount}/>
      </div>

      <div className="mt-4">
        <AnalyticsCharts scope="branch" title="Branch analytics"/>
      </div>
    </>);
}
