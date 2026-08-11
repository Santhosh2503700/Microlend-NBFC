import { useCallback, useEffect, useState } from "react";
import { FileWarning, ShieldCheck } from "lucide-react";
import { branchManagerApi } from "@/api/branchManager";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, EmptyState, LoadingState } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const formatDate = (iso) => new Date(iso).toLocaleDateString();
/** Receipt Disputes Queue — Branch Manager co-signs on the borrower's behalf (Phase 8d). */
export function BranchManagerDisputesPage() {
    const [disputes, setDisputes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [openId, setOpenId] = useState(null);
    const [justification, setJustification] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        branchManagerApi
            .disputes()
            .then(setDisputes)
            .catch((e) => setError(errorMessage(e, "Could not load receipt disputes")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const startCoSign = useCallback((receiptId) => {
        setOpenId(receiptId);
        setJustification("");
        setFormError(null);
    }, []);
    const cancel = useCallback(() => {
        setOpenId(null);
        setJustification("");
        setFormError(null);
    }, []);
    const submit = useCallback((receiptId) => {
        const text = justification.trim();
        if (!text) {
            setFormError("A justification is required to co-sign this receipt.");
            return;
        }
        setSubmitting(true);
        setFormError(null);
        branchManagerApi
            .coSignReceipt(receiptId, text)
            .then(() => {
            cancel();
            load();
        })
            .catch((e) => setFormError(errorMessage(e, "Could not co-sign the receipt")))
            .finally(() => setSubmitting(false));
    }, [justification, cancel, load]);
    if (loading)
        return <LoadingState label="Loading receipt disputes"/>;
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <FileWarning size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Receipt Disputes
        </h1>
        <p className="mt-2 text-ink-muted">
          Borrowers who disputed a collection receipt. Co-signing resolves the dispute and approves
          the receipt on the borrower's behalf, applying the payment to their loan.
        </p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {disputes.length === 0 ? (<EmptyState title="No receipt disputes awaiting your review"/>) : (<div className="flex flex-col gap-4">
          {disputes.map((r) => (<Card key={r.receiptId}>
              <div className="flex flex-wrap items-center justify-between">
                <div>
                  <div style={{ fontWeight: 700, color: "#1C2826", fontSize: 16 }}>
                    {r.borrowerName}
                  </div>
                  <div className="text-sm mt-1" style={{ color: "#1C2826" }}>
                    Receipt{r.receiptId} · {money(r.statedAmount)} · {formatDate(r.collectionDate)}{" "}
                    · {r.mode}
                  </div>
                </div>
                {openId !== r.receiptId && (<Button size="sm" onClick={() => startCoSign(r.receiptId)}>
                    <ShieldCheck size={16}/> Co-sign (resolve)
                  </Button>)}
              </div>

              <div className="mt-4">
                <div className="mb-2 block text-sm font-semibold">Dispute remarks</div>
                <p className="text-sm" style={{ margin: "4px 0 0", color: "#1C2826" }}>
                  {r.disputeRemarks ?? "—"}
                </p>
              </div>

              {openId === r.receiptId && (<div className="mt-4">
                  {formError && <Alert tone="error">{formError}</Alert>}
                  <label className="mb-4 block">
                    <span className="mb-2 block text-sm font-semibold">Co-sign justification<span className="ml-0.5 text-danger" title="Required">*</span></span>
                    <textarea value={justification} onChange={(e) => setJustification(e.target.value)} rows={3} placeholder="Explain why you are approving this receipt on the borrower's behalf…" style={{
                        width: "100%",
                        borderRadius: 10,
                        border: "1px solid rgba(0,77,64,0.2)",
                        padding: "10px 12px",
                        fontSize: 14,
                        resize: "vertical",
                    }}/>
                  </label>
                  <div className="flex flex-wrap items-center gap-2">
                    <Button size="sm" onClick={() => submit(r.receiptId)} loading={submitting} disabled={!justification.trim()}>
                      Confirm co-sign
                    </Button>
                    <Button size="sm" variant="ghost" onClick={cancel} disabled={submitting}>
                      Cancel
                    </Button>
                  </div>
                </div>)}
            </Card>))}
        </div>)}
    </>);
}
