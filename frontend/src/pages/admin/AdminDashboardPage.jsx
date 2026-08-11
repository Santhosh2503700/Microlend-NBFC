import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Boxes, Users, KeyRound, ScrollText, Package, ShieldAlert, BarChart3, } from "lucide-react";
import { adminApi } from "@/api/admin";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, KpiCard, LoadingState } from "@/components/ui";
export function AdminDashboardPage() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        Promise.all([adminApi.products(), adminApi.users(), adminApi.auditLog()])
            .then(([products, users, audit]) => {
            setStats({
                productCount: products.length,
                userCount: users.length,
                pendingResetCount: users.filter((u) => u.mustResetPassword).length,
                auditCount: audit.length,
            });
        })
            .catch((e) => setError(errorMessage(e, "Could not load admin dashboard")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">NBFC Admin Console</h1>
        <p className="mt-2 text-ink-muted">System-wide configuration, access control, audit and portfolio operations.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {loading ? (<LoadingState label="Loading system metrics"/>) : stats ? (<div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <KpiCard icon={Boxes} label="Loan products" value={stats.productCount}/>
          <KpiCard icon={Users} label="Total users" value={stats.userCount}/>
          <KpiCard icon={KeyRound} label="Users pending password reset" value={stats.pendingResetCount}/>
          <KpiCard icon={ScrollText} label="Audit events" value={stats.auditCount}/>
        </div>) : null}

      <h2 className="mb-4 text-lg font-semibold mt-6">Console</h2>
      <Card>
        <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
            gap: 12,
        }}>
          <Link to="/admin/products" style={{ textDecoration: "none" }}>
            <Button variant="secondary" block>
              <Package size={16}/> Loan Product Builder
            </Button>
          </Link>
          <Link to="/admin/users" style={{ textDecoration: "none" }}>
            <Button variant="secondary" block>
              <Users size={16}/> User Access Management
            </Button>
          </Link>
          <Link to="/admin/audit" style={{ textDecoration: "none" }}>
            <Button variant="secondary" block>
              <ScrollText size={16}/> Audit Log
            </Button>
          </Link>
          <Link to="/admin/delinquency" style={{ textDecoration: "none" }}>
            <Button variant="secondary" block>
              <ShieldAlert size={16}/> Delinquency Scan
            </Button>
          </Link>
          <Link to="/admin/analytics" style={{ textDecoration: "none" }}>
            <Button variant="secondary" block>
              <BarChart3 size={16}/> Portfolio Analytics
            </Button>
          </Link>
        </div>
      </Card>
    </>);
}
