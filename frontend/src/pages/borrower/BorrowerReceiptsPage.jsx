import { useCallback, useEffect, useState } from "react";
import { Receipt, Check, AlertTriangle } from "lucide-react";
import { borrowerApi } from "@/api/borrower";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, EmptyState, LoadingState, StatusBadge, toneForStatus, } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const formatDate = (iso) => new Date(iso).toLocaleDateString();
/** Borrower receipt inbox (approve/dispute) + full receipt history (Phase 8a). */
export function BorrowerReceiptsPage() {
    const [pending, setPending] = useState([]);
    const [pendingLoading, setPendingLoading] = useState(true);
    const [history, setHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(true);
    const [error, setError] = useState(null);
    const [busyId, setBusyId] = useState(null);
    const [disputeFor, setDisputeFor] = useState(null);
    const [remarks, setRemarks] = useState("");
    const loadPending = useCallback(() => {
        setPendingLoading(true);
        borrowerApi
            .receipts(false)
            .then(setPending)
            .catch((e) => setError(errorMessage(e, "Could not load pending receipts")))
            .finally(() => setPendingLoading(false));
    }, []);
    const loadHistory = useCallback(() => {
        setHistoryLoading(true);
        borrowerApi
            .receipts(true)
            .then(setHistory)
            .catch((e) => setError(errorMessage(e, "Could not load receipt history")))
            .finally(() => setHistoryLoading(false));
    }, []);
    const refresh = useCallback(() => {
        loadPending();
        loadHistory();
    }, [loadPending, loadHistory]);
    useEffect(() => refresh(), [refresh]);
    const approve = async (id) => {
        setBusyId(id);
        setError(null);
        try {
            await borrowerApi.approveReceipt(id);
            refresh();
        }
        catch (e) {
            setError(errorMessage(e, "Could not approve the receipt"));
        }
        finally {
            setBusyId(null);
        }
    };
    const openDispute = (id) => {
        setDisputeFor(id);
        setRemarks("");
    };
    const submitDispute = async (id) => {
        if (!remarks.trim()) {
            setError("Please describe the reason for your dispute.");
            return;
        }
        setBusyId(id);
        setError(null);
        try {
            await borrowerApi.disputeReceipt(id, remarks.trim());
            setDisputeFor(null);
            setRemarks("");
            refresh();
        }
        catch (e) {
            setError(errorMessage(e, "Could not submit the dispute"));
        }
        finally {
            setBusyId(null);
        }
    };
    const historyColumns = [
        { key: "receiptId", header: "Receipt", render: (r) => `${r.receiptId}` },
        { key: "borrowerName", header: "Borrower", render: (r) => r.borrowerName },
        {
            key: "statedAmount",
            header: "Amount",
            numeric: true,
            render: (r) => money(r.statedAmount),
        },
        { key: "collectionDate", header: "Date", render: (r) => formatDate(r.collectionDate) },
        { key: "mode", header: "Mode", render: (r) => r.mode },
        {
            key: "borrowerApprovalStatus",
            header: "Approval",
            render: (r) => (<StatusBadge tone={toneForStatus(r.borrowerApprovalStatus)}>
          {r.borrowerApprovalStatus}
        </StatusBadge>),
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <Receipt size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          My Receipts
        </h1>
        <p className="mt-2 text-ink-muted">Confirm collections recorded against your loans, or raise a dispute.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      <h2 className="mb-4 text-lg font-semibold">Receipt Inbox</h2>
      {pendingLoading ? (<LoadingState label="Loading pending receipts"/>) : pending.length === 0 ? (<EmptyState title="No receipts awaiting your approval" description="When a Field Officer records a collection, the receipt appears here for you to confirm." icon={Receipt}/>) : (<div className="flex flex-col gap-4">
          {pending.map((r) => {
                const busy = busyId === r.receiptId;
                return (<Card key={r.receiptId}>
                <div className="flex items-center" style={{ justifyContent: "space-between", alignItems: "center", flexWrap: "wrap", gap: 12 }}>
                  <div>
                    <h3 style={{ margin: 0 }}>
                      Receipt{r.receiptId} · {r.borrowerName}
                    </h3>
                    <div className="flex items-center gap-6 mt-2" style={{ flexWrap: "wrap" }}>
                      <span className="text-sm" style={{ color: "#1C2826" }}>
                        Amount: <strong>{money(r.statedAmount)}</strong>
                      </span>
                      <span className="text-sm" style={{ color: "#1C2826" }}>
                        Date: <strong>{formatDate(r.collectionDate)}</strong>
                      </span>
                      <span className="text-sm" style={{ color: "#1C2826" }}>
                        Mode: <strong>{r.mode}</strong>
                      </span>
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    <Button loading={busy && disputeFor !== r.receiptId} onClick={() => approve(r.receiptId)}>
                      <Check size={16}/> Approve
                    </Button>
                    <Button variant="danger" disabled={busy} onClick={() => openDispute(r.receiptId)}>
                      <AlertTriangle size={16}/> Dispute
                    </Button>
                  </div>
                </div>

                {disputeFor === r.receiptId && (<div className="mt-4 flex flex-col gap-2">
                    <label className="mb-4 block">
                      <span className="mb-2 block text-sm font-semibold">Dispute reason<span className="ml-0.5 text-danger" title="Required">*</span></span>
                      <textarea rows={3} value={remarks} onChange={(e) => setRemarks(e.target.value)} placeholder="Explain why this receipt is incorrect" required style={{
                            width: "100%",
                            padding: "10px 12px",
                            background: "#fff",
                            border: "1px solid var(--color-border)",
                            borderRadius: "var(--radius-md)",
                            font: "inherit",
                            color: "var(--color-text)",
                            resize: "vertical",
                        }}/>
                    </label>
                    <div className="flex flex-wrap items-center gap-2">
                      <Button variant="danger" loading={busy} onClick={() => submitDispute(r.receiptId)}>
                        Submit dispute
                      </Button>
                      <Button variant="ghost" disabled={busy} onClick={() => setDisputeFor(null)}>
                        Cancel
                      </Button>
                    </div>
                  </div>)}
              </Card>);
            })}
        </div>)}

      <h2 className="mb-4 text-lg font-semibold mt-6">History</h2>
      <Card>
        <DataTable columns={historyColumns} rows={history} rowKey={(r) => r.receiptId} loading={historyLoading} emptyLabel="No receipts yet"/>
      </Card>
    </>);
}
