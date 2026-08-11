import { useCallback, useEffect, useState } from "react";
import { Landmark, CalendarDays } from "lucide-react";
import { borrowerApi } from "@/api/borrower";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, StatusBadge, toneForStatus, } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const formatDate = (iso) => new Date(iso).toLocaleDateString();
/** Borrower loan book + interactive repayment schedule (Phase 8a). */
export function BorrowerLoansPage() {
    const [loans, setLoans] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selected, setSelected] = useState(null);
    const [schedule, setSchedule] = useState([]);
    const [scheduleLoading, setScheduleLoading] = useState(false);
    const [scheduleError, setScheduleError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        borrowerApi
            .loans()
            .then(setLoans)
            .catch((e) => setError(errorMessage(e, "Could not load your loans")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const viewSchedule = useCallback((loan) => {
        setSelected(loan);
        setScheduleLoading(true);
        setScheduleError(null);
        setSchedule([]);
        borrowerApi
            .schedule(loan.loanAccountId)
            .then(setSchedule)
            .catch((e) => setScheduleError(errorMessage(e, "Could not load the repayment schedule")))
            .finally(() => setScheduleLoading(false));
    }, []);
    const loanColumns = [
        { key: "loanAccountId", header: "Loan", render: (l) => `${l.loanAccountId}` },
        { key: "productId", header: "Product", render: (l) => `Product ${l.productId}` },
        {
            key: "disbursedAmount",
            header: "Disbursed",
            numeric: true,
            render: (l) => money(l.disbursedAmount),
        },
        {
            key: "outstandingPrincipal",
            header: "Outstanding",
            numeric: true,
            render: (l) => money(l.outstandingPrincipal),
        },
        { key: "dpd", header: "DPD", numeric: true, render: (l) => l.dpd },
        {
            key: "status",
            header: "Status",
            render: (l) => <StatusBadge tone={toneForStatus(l.status)}>{l.status}</StatusBadge>,
        },
        {
            key: "actions",
            header: "",
            render: (l) => (<Button size="sm" variant={selected?.loanAccountId === l.loanAccountId ? "primary" : "secondary"} onClick={() => viewSchedule(l)}>
          <CalendarDays size={16}/> View schedule
        </Button>),
        },
    ];
    const scheduleColumns = [
        { key: "installmentNumber", header: "Installment", render: (s) => s.installmentNumber },
        { key: "dueDate", header: "Due Date", render: (s) => formatDate(s.dueDate) },
        {
            key: "principalDue",
            header: "Principal Due",
            numeric: true,
            render: (s) => money(s.principalDue),
        },
        {
            key: "interestDue",
            header: "Interest Due",
            numeric: true,
            render: (s) => money(s.interestDue),
        },
        { key: "totalDue", header: "Total Due", numeric: true, render: (s) => money(s.totalDue) },
        {
            key: "status",
            header: "Status",
            render: (s) => <StatusBadge tone={toneForStatus(s.status)}>{s.status}</StatusBadge>,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <Landmark size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          My Loans
        </h1>
        <p className="mt-2 text-ink-muted">Your disbursed loan accounts. Select a loan to view its repayment schedule.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <Card>
        <DataTable columns={loanColumns} rows={loans} rowKey={(l) => l.loanAccountId} loading={loading} emptyLabel="You have no active loans yet"/>
      </Card>

      {selected && (<>
          <h2 className="mb-4 text-lg font-semibold mt-6">Repayment Schedule — Loan{selected.loanAccountId}</h2>
          {scheduleError && <Alert tone="error">{scheduleError}</Alert>}
          <Card>
            <DataTable columns={scheduleColumns} rows={schedule} rowKey={(s) => s.scheduleId} loading={scheduleLoading} emptyLabel="No schedule available for this loan"/>
          </Card>
        </>)}
    </>);
}
