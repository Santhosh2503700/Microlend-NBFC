import { useCallback, useEffect, useRef, useState } from "react";
import { Percent, RefreshCw, TrendingUp, Wallet } from "lucide-react";
import { analyticsApi } from "@/api/analytics";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, EmptyState, KpiCard, LoadingState } from "@/components/ui";
import { inr } from "./chartTheme";
import { ParDistributionChart } from "./ParDistributionChart";
import { PortfolioTrendChart } from "./PortfolioTrendChart";
import { CollectionEfficiencyChart } from "./CollectionEfficiencyChart";
import { OfficerPerformanceChart } from "./OfficerPerformanceChart";
import { LoanFunnelChart } from "./LoanFunnelChart";
import { NpaTrendChart } from "./NpaTrendChart";
const POLL_MS = 45000;
/** One chart card with its own title and per-dataset empty state. */
function ChartSection({ title, isEmpty, emptyLabel, children }) {
    return (<div className="flex flex-col gap-2">
      <h3 className="mb-4 text-lg font-semibold">{title}</h3>
      <Card>
        {isEmpty ? (<EmptyState title="No data yet" description={emptyLabel}/>) : (children)}
      </Card>
    </div>);
}

export function AnalyticsCharts({ scope, title }) {
    const [bundle, setBundle] = useState(null);
    const [error, setError] = useState(null);
    const [refreshing, setRefreshing] = useState(false);
    const [updatedAt, setUpdatedAt] = useState(null);
    const mounted = useRef(true);
    const load = useCallback(() => {
        setRefreshing(true);
        Promise.all([
            analyticsApi.parDistribution(scope),
            analyticsApi.portfolioTrend(scope),
            analyticsApi.collectionEfficiency(scope),
            analyticsApi.officerPerformance(scope),
            analyticsApi.loanFunnel(scope),
            analyticsApi.npaTrend(scope),
        ])
            .then(([par, portfolio, efficiency, officers, funnel, npa]) => {
            if (!mounted.current)
                return;
            setBundle({ par, portfolio, efficiency, officers, funnel, npa });
            setError(null);
            setUpdatedAt(new Date());
        })
            .catch((e) => {
            if (mounted.current)
                setError(errorMessage(e, "Could not load analytics"));
        })
            .finally(() => {
            if (mounted.current)
                setRefreshing(false);
        });
    }, [scope]);
    useEffect(() => {
        mounted.current = true;
        load();
        const id = window.setInterval(load, POLL_MS);
        return () => {
            mounted.current = false;
            window.clearInterval(id);
        };
    }, [load]);
    if (!bundle && !error)
        return <LoadingState label="Loading analytics"/>;
    const totalOutstanding = bundle ? bundle.par.reduce((sum, r) => sum + r.outstanding, 0) : 0;
    const latestPortfolio = bundle && bundle.portfolio.length
        ? bundle.portfolio[bundle.portfolio.length - 1].cumulativePortfolio
        : 0;
    const latestNpa = bundle && bundle.npa.length ? bundle.npa[bundle.npa.length - 1].npaPercent : 0;
    const latestEfficiency = bundle && bundle.efficiency.length
        ? bundle.efficiency[bundle.efficiency.length - 1].efficiencyPercent
        : 0;
    return (<div className="flex flex-col gap-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          {title && <h2 className="mb-4 text-lg font-semibold">{title}</h2>}
          {updatedAt && (<p className="text-sm text-ink">
              Live from the portfolio database · updated {updatedAt.toLocaleTimeString("en-IN")}
            </p>)}
        </div>
        <Button variant="secondary" size="sm" onClick={load} loading={refreshing}>
          <RefreshCw size={16} style={{ marginRight: 6, verticalAlign: "-3px" }}/>
          Refresh
        </Button>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {bundle && (<>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <KpiCard icon={Wallet} label="Total Outstanding" value={inr(totalOutstanding)}/>
            <KpiCard icon={TrendingUp} label="Portfolio (Cumulative)" value={inr(latestPortfolio)}/>
            <KpiCard icon={Percent} label="Current NPA %" value={`${latestNpa.toFixed(2)}%`}/>
            <KpiCard icon={Percent} label="Latest Collection Efficiency" value={`${latestEfficiency.toFixed(1)}%`}/>
          </div>

          <ChartSection title="Portfolio at Risk (PAR) distribution" isEmpty={bundle.par.length === 0} emptyLabel="No delinquency exposure recorded yet.">
            <ParDistributionChart data={bundle.par}/>
          </ChartSection>

          <ChartSection title="Portfolio & disbursement trend" isEmpty={bundle.portfolio.length === 0} emptyLabel="No disbursements have been recorded yet.">
            <PortfolioTrendChart data={bundle.portfolio}/>
          </ChartSection>

          <ChartSection title="Collection efficiency (due vs collected)" isEmpty={bundle.efficiency.length === 0} emptyLabel="No collections have been recorded yet.">
            <CollectionEfficiencyChart data={bundle.efficiency}/>
          </ChartSection>

          <ChartSection title="Officer performance (amount collected)" isEmpty={bundle.officers.length === 0} emptyLabel="No officer collection activity yet.">
            <OfficerPerformanceChart data={bundle.officers}/>
          </ChartSection>

          <ChartSection title="Loan application funnel" isEmpty={bundle.funnel.length === 0} emptyLabel="No loan applications recorded yet.">
            <LoanFunnelChart data={bundle.funnel}/>
          </ChartSection>

          <ChartSection title="NPA trend" isEmpty={bundle.npa.length === 0} emptyLabel="No NPA history available yet.">
            <NpaTrendChart data={bundle.npa}/>
          </ChartSection>
        </>)}
    </div>);
}
