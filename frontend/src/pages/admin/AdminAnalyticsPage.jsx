import { BarChart3 } from "lucide-react";
import { AnalyticsCharts } from "@/components/charts/AnalyticsCharts";
export function AdminAnalyticsPage() {
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <BarChart3 size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Portfolio Analytics
        </h1>
        <p className="mt-2 text-ink-muted">System-wide portfolio, PAR and collections analytics — sourced live from the database.</p>
      </div>

      <AnalyticsCharts scope="system" title="System-wide analytics"/>
    </>);
}
