import { useCallback, useEffect, useState } from "react";
import { Wallet, Users, AlertTriangle } from "lucide-react";
import { fieldOfficerApi } from "@/api/fieldOfficer";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, Select, StatusBadge, toneForStatus, } from "@/components/ui";
import { SelectField, inr } from "./fieldControls";
const MODES = ["CASH", "BANK_TRANSFER", "CENTRE_COLLECTION"];
const cellSelectStyle = {
    height: 34,
    border: "1px solid var(--color-border)",
    borderRadius: "var(--radius-md)",
    padding: "0 var(--space-2)",
    background: "#fff",
    fontSize: "var(--fs-sm)",
    color: "var(--color-text)",
};
export function FieldOfficerCollectionsPage() {
    const [centres, setCentres] = useState([]);
    const [borrowers, setBorrowers] = useState([]);
    const [refError, setRefError] = useState(null);
    const [centreId, setCentreId] = useState("");
    const [rows, setRows] = useState([]);
    const [skipped, setSkipped] = useState([]);
    const [preparing, setPreparing] = useState(false);
    const [prepError, setPrepError] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [summary, setSummary] = useState(null);
    const [submitError, setSubmitError] = useState(null);
    const [history, setHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(true);
    const loadHistory = useCallback(() => {
        setHistoryLoading(true);
        fieldOfficerApi
            .myCollections()
            .then(setHistory)
            .catch((e) => setRefError(errorMessage(e, "Could not load collection history")))
            .finally(() => setHistoryLoading(false));
    }, []);
    useEffect(() => {
        Promise.all([fieldOfficerApi.listCentres(), fieldOfficerApi.myBorrowers()])
            .then(([c, b]) => {
            setCentres(c);
            setBorrowers(b);
        })
            .catch((e) => setRefError(errorMessage(e, "Could not load centres/borrowers")));
        loadHistory();
    }, [loadHistory]);
    const prepare = useCallback(async (selectedCentreId) => {
        if (!selectedCentreId) {
            setRows([]);
            setSkipped([]);
            return;
        }
        setPreparing(true);
        setPrepError(null);
        setSummary(null);
        setSubmitError(null);
        const atCentre = borrowers.filter((b) => String(b.centreId) === selectedCentreId);
        try {
            const built = [];
            const skippedNames = [];
            for (const b of atCentre) {
                const loans = await fieldOfficerApi.borrowerLoans(b.borrowerId);
                const active = loans.find((l) => l.status === "ACTIVE");
                if (!active) {
                    skippedNames.push(`${b.name} (no active loan)`);
                    continue;
                }
                const schedule = await fieldOfficerApi.loanSchedule(active.loanAccountId);
                const next = [...schedule]
                    .sort((x, y) => x.installmentNumber - y.installmentNumber)
                    .find((s) => s.status === "PENDING" || s.status === "OVERDUE");
                if (!next) {
                    skippedNames.push(`${b.name} (no unpaid installment)`);
                    continue;
                }
                built.push({
                    borrowerId: b.borrowerId,
                    borrowerName: b.name,
                    loanAccountId: active.loanAccountId,
                    scheduleId: next.scheduleId,
                    installmentNumber: next.installmentNumber,
                    totalDue: next.totalDue,
                    amount: String(next.totalDue),
                    mode: "CENTRE_COLLECTION",
                });
            }
            setRows(built);
            setSkipped(skippedNames);
        }
        catch (err) {
            setPrepError(errorMessage(err, "Could not resolve borrower loans for this centre"));
            setRows([]);
            setSkipped([]);
        }
        finally {
            setPreparing(false);
        }
    }, [borrowers]);
    useEffect(() => {
        prepare(centreId);
    }, [centreId, prepare]);
    const patchRow = (borrowerId, patch) => setRows((prev) => prev.map((r) => (r.borrowerId === borrowerId ? { ...r, ...patch } : r)));
    const submitAll = async () => {
        setSubmitError(null);
        setSummary(null);
        const payable = rows.filter((r) => Number(r.amount) > 0);
        if (payable.length === 0) {
            setSubmitError("Enter at least one collected amount greater than zero.");
            return;
        }
        setSubmitting(true);
        let ok = 0;
        const failures = [];
        for (const r of payable) {
            try {
                await fieldOfficerApi.recordCollection({
                    loanAccountId: r.loanAccountId,
                    scheduleId: r.scheduleId,
                    collectedAmount: Number(r.amount),
                    mode: r.mode,
                    centreMeetingId: null,
                });
                ok += 1;
            }
            catch (err) {
                failures.push(`${r.borrowerName}: ${errorMessage(err, "failed")}`);
            }
        }
        setSubmitting(false);
        if (failures.length) {
            setSubmitError(`Some entries failed — ${failures.join("; ")}`);
        }
        if (ok > 0) {
            setSummary(`Recorded ${ok} collection${ok === 1 ? "" : "s"}. Each borrower receives their own digital receipt and a notification, and must approve it before the installment is marked paid.`);
        }
        loadHistory();
        prepare(centreId);
    };
    const entryColumns = [
        { key: "borrowerName", header: "Borrower", render: (r) => r.borrowerName },
        {
            key: "installmentNumber",
            header: "Loan / Installment",
            render: (r) => `Loan ${r.loanAccountId} · Inst. ${r.installmentNumber}`,
        },
        {
            key: "totalDue",
            header: "Due",
            numeric: true,
            render: (r) => inr.format(r.totalDue),
        },
        {
            key: "amount",
            header: "Collected amount",
            render: (r) => (<input type="number" min={0} value={r.amount} onChange={(e) => patchRow(r.borrowerId, { amount: e.target.value })} style={{ ...cellSelectStyle, width: 130 }} aria-label={`Collected amount for ${r.borrowerName}`}/>),
        },
        {
            key: "mode",
            header: "Mode",
            render: (r) => (<Select value={r.mode} onChange={(e) => patchRow(r.borrowerId, { mode: e.target.value })} style={cellSelectStyle} aria-label={`Mode for ${r.borrowerName}`}>
          {MODES.map((m) => (<option key={m} value={m}>
              {m.replace(/_/g, " ")}
            </option>))}
        </Select>),
        },
    ];
    const historyColumns = [
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
        <h1 className="text-2xl font-bold">Centre Collections</h1>
        <p className="mt-2 text-ink-muted">Record repayments for a whole centre meeting in one pass — one receipt per borrower.</p>
      </div>

      {refError && <Alert tone="error">{refError}</Alert>}

      <Card className="mb-6">
        <div className="flex items-center gap-2 mb-6">
          <Wallet size={18}/>
          <span className="mb-0 text-lg font-semibold">
            Meeting collection entry
          </span>
        </div>

        <SelectField id="col-centre" label="Centre" value={centreId} onChange={(e) => setCentreId(e.target.value)}>
          <option value="">Select a centre…</option>
          {centres.map((c) => (<option key={c.centreId} value={c.centreId}>
              {c.centreName} — {c.village}
            </option>))}
        </SelectField>

        {prepError && <Alert tone="error">{prepError}</Alert>}
        {submitError && <Alert tone="error">{submitError}</Alert>}
        {summary && <Alert tone="info">{summary}</Alert>}

        {skipped.length > 0 && (<div className="flex items-center gap-2 text-sm mb-6" style={{ color: "var(--color-warning)" }}>
            <AlertTriangle size={16}/>
            <span>Skipped: {skipped.join(", ")}</span>
          </div>)}

        {centreId && (<>
            <DataTable columns={entryColumns} rows={rows} rowKey={(r) => r.borrowerId} loading={preparing} emptyLabel="No borrowers with an active loan and an unpaid installment at this centre"/>
            {rows.length > 0 && (<div className="mt-4">
                <Button onClick={submitAll} loading={submitting}>
                  <Users size={16}/> Submit all ({rows.filter((r) => Number(r.amount) > 0).length})
                </Button>
              </div>)}
          </>)}
      </Card>

      <div className="mb-4 text-lg font-semibold">My recorded collections</div>
      <Card>
        <DataTable columns={historyColumns} rows={history} rowKey={(r) => r.collectionId} loading={historyLoading} emptyLabel="No collections recorded yet"/>
      </Card>
    </>);
}
