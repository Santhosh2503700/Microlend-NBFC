import { useState } from "react";
import { ShieldAlert, Play, Clock, AlertTriangle, Search, FolderPlus, RefreshCw, CheckCircle2, Sparkles, } from "lucide-react";
import { adminApi } from "@/api/admin";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, KpiCard } from "@/components/ui";
/** On-demand delinquency scan trigger (Phase 8f). */
export function AdminDelinquencyPage() {
    const [result, setResult] = useState(null);
    const [ranAt, setRanAt] = useState(null);
    const [running, setRunning] = useState(false);
    const [seeding, setSeeding] = useState(false);
    const [error, setError] = useState(null);
    const [notice, setNotice] = useState(null);
    const run = async () => {
        setRunning(true);
        setError(null);
        setNotice(null);
        try {
            const res = await adminApi.runDelinquencyScan();
            setResult(res);
            setRanAt(new Date().toLocaleString());
        }
        catch (e) {
            setError(errorMessage(e, "Could not run the delinquency scan"));
        }
        finally {
            setRunning(false);
        }
    };
    const generateDemo = async () => {
        setSeeding(true);
        setError(null);
        setNotice(null);
        try {
            const res = await adminApi.generateDemoDelinquency();
            setResult(res.scan);
            setRanAt(new Date().toLocaleString());
            setNotice("Seeded 4 demo loans across PAR30/60/90/180 and ran the scan. " +
                "Log in as Branch Manager → Delinquency to assign a Collections Officer.");
        }
        catch (e) {
            setError(errorMessage(e, "Could not generate the demo portfolio"));
        }
        finally {
            setSeeding(false);
        }
    };
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <ShieldAlert size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Delinquency Scan
        </h1>
        <p className="mt-2 text-ink-muted">Run the portfolio delinquency scan on demand.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}
      {notice && <Alert tone="info">{notice}</Alert>}

      <Card>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-2">
            <Clock size={18}/>
            <span className="text-sm">
              The scheduled scan runs automatically every day at 01:00. Use this to trigger an
              extra scan now — it re-ages installments, recomputes DPD / PAR buckets, and opens,
              updates or resolves delinquency cases.
            </span>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button loading={running} disabled={seeding} onClick={run}>
              <Play size={16}/> Run scan now
            </Button>
            <Button variant="secondary" loading={seeding} disabled={running} onClick={generateDemo}>
              <Sparkles size={16}/> Generate demo portfolio &amp; scan
            </Button>
          </div>
        </div>
      </Card>

      {result && (<>
          <h2 className="mb-4 text-lg font-semibold mt-6">
            Scan summary{ranAt ? ` · ${ranAt}` : ""}
          </h2>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <KpiCard icon={AlertTriangle} label="Installments marked overdue" value={result.installmentsMarkedOverdue}/>
            <KpiCard icon={Search} label="Accounts scanned" value={result.accountsScanned}/>
            <KpiCard icon={FolderPlus} label="Cases opened" value={result.casesOpened}/>
            <KpiCard icon={RefreshCw} label="Cases updated" value={result.casesUpdated}/>
            <KpiCard icon={CheckCircle2} label="Cases resolved" value={result.casesResolved}/>
          </div>
        </>)}
    </>);
}
