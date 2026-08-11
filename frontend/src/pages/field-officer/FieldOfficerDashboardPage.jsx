import { useCallback, useEffect, useState } from "react";
import { Users, MapPin, Users2, Wallet } from "lucide-react";
import { fieldOfficerApi } from "@/api/fieldOfficer";
import { errorMessage } from "@/api/client";
import { useAuth } from "@/auth/useAuth";
import { Alert, Card, DataTable, KpiCard, StatusBadge, toneForStatus, } from "@/components/ui";
import { inr } from "./fieldControls";
export function FieldOfficerDashboardPage() {
    const { user } = useAuth();
    const [borrowers, setBorrowers] = useState([]);
    const [centres, setCentres] = useState([]);
    const [groups, setGroups] = useState([]);
    const [collections, setCollections] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        Promise.all([
            fieldOfficerApi.myBorrowers(),
            fieldOfficerApi.listCentres(),
            fieldOfficerApi.listGroups(),
            fieldOfficerApi.myCollections(),
        ])
            .then(([b, c, g, col]) => {
            setBorrowers(b);
            setCentres(c);
            setGroups(g);
            setCollections(col);
        })
            .catch((e) => setError(errorMessage(e, "Could not load dashboard")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const recent = [...collections]
        .sort((a, b) => new Date(b.collectionDate).getTime() - new Date(a.collectionDate).getTime())
        .slice(0, 8);
    const columns = [
        { key: "borrowerName", header: "Borrower", render: (r) => r.borrowerName },
        {
            key: "collectedAmount",
            header: "Amount",
            numeric: true,
            render: (r) => inr.format(r.collectedAmount),
        },
        {
            key: "collectionDate",
            header: "Date",
            render: (r) => new Date(r.collectionDate).toLocaleDateString(),
        },
        { key: "mode", header: "Mode", render: (r) => r.mode.replace(/_/g, " ") },
        {
            key: "status",
            header: "Status",
            render: (r) => <StatusBadge tone={toneForStatus(r.status)}>{r.status}</StatusBadge>,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Field Officer Dashboard</h1>
        <p className="mt-2 text-ink-muted">Welcome back{user ? `, ${user.name}` : ""}. Your book of work, live from the database.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4 mb-6">
        <KpiCard icon={Users} label="My Borrowers" value={borrowers.length}/>
        <KpiCard icon={MapPin} label="Centres" value={centres.length}/>
        <KpiCard icon={Users2} label="Groups" value={groups.length}/>
        <KpiCard icon={Wallet} label="Collections Recorded" value={collections.length}/>
      </div>

      <div className="mb-4 text-lg font-semibold">Recent collections</div>
      <Card>
        <DataTable columns={columns} rows={recent} rowKey={(r) => r.collectionId} loading={loading} emptyLabel="No collections recorded yet"/>
      </Card>
    </>);
}
