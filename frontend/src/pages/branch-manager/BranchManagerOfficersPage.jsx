import { useCallback, useEffect, useState } from "react";
import { UserCog } from "lucide-react";
import { branchManagerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Card, DataTable, StatusBadge, toneForStatus, } from "@/components/ui";
const ROLE_LABEL = {
    FIELD_OFFICER: "Field Officer",
    COLLECTIONS_OFFICER: "Collections Officer",
    CREDIT_OFFICER: "Credit Officer",
    BRANCH_MANAGER: "Branch Manager",
    NBFC_ADMIN: "NBFC Admin",
    BORROWER: "Borrower",
};
const columns = [
    { key: "name", header: "Name", render: (o) => o.name },
    { key: "email", header: "Email", render: (o) => o.email },
    {
        key: "role",
        header: "Role",
        render: (o) => <StatusBadge tone="info">{ROLE_LABEL[o.role] ?? o.role}</StatusBadge>,
    },
    {
        key: "workload",
        header: "Workload",
        render: (o) => `${o.workloadCount} ${o.workloadLabel}`,
    },
    {
        key: "status",
        header: "Status",
        render: (o) => <StatusBadge tone={toneForStatus(o.status)}>{o.status}</StatusBadge>,
    },
];
function RosterSection({ title, rows }) {
    return (<>
      <h2 className="mb-4 text-lg font-semibold mt-4">{title}</h2>
      <Card>
        <DataTable columns={columns} rows={rows} rowKey={(o) => o.userId} emptyLabel={`No ${title.toLowerCase()} in your branch`}/>
      </Card>
    </>);
}
export function BranchManagerOfficersPage() {
    const [officers, setOfficers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        branchManagerApi
            .officers()
            .then(setOfficers)
            .catch((e) => setError(errorMessage(e, "Could not load the officer roster")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const fieldOfficers = officers.filter((o) => o.role === "FIELD_OFFICER");
    const collectionsOfficers = officers.filter((o) => o.role === "COLLECTIONS_OFFICER");
    const others = officers.filter((o) => o.role !== "FIELD_OFFICER" && o.role !== "COLLECTIONS_OFFICER");
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <UserCog size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Officer Roster
        </h1>
        <p className="mt-2 text-ink-muted">Staff in your branch with their live workload.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {loading ? (<Card>
          <DataTable columns={columns} rows={[]} rowKey={(o) => o.userId} loading/>
        </Card>) : (<>
          <RosterSection title="Field Officers" rows={fieldOfficers}/>
          <RosterSection title="Collections Officers" rows={collectionsOfficers}/>
          {others.length > 0 && <RosterSection title="Other Staff" rows={others}/>}
        </>)}
    </>);
}
