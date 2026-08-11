import { useCallback, useEffect, useMemo, useState } from "react";
import { FileText, CheckCircle2 } from "lucide-react";
import { fieldOfficerApi } from "@/api/fieldOfficer";
import { loanApi } from "@/api/loans";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, Input, StatusBadge, toneForStatus, } from "@/components/ui";
import { SelectField, TextareaField, inr, fmtDate } from "./fieldControls";
export function FieldOfficerApplicationsPage() {
    const [borrowers, setBorrowers] = useState([]);
    const [products, setProducts] = useState([]);
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState(null);
    const [borrowerId, setBorrowerId] = useState("");
    const [productId, setProductId] = useState("");
    const [amount, setAmount] = useState("");
    const [purpose, setPurpose] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState(null);
    const [success, setSuccess] = useState(null);
    const loadRefData = useCallback(() => {
        Promise.all([fieldOfficerApi.myBorrowers(), loanApi.productCatalogue()])
            .then(([b, p]) => {
            setBorrowers(b);
            setProducts(p);
        })
            .catch((e) => setLoadError(errorMessage(e, "Could not load reference data")));
    }, []);
    const loadApplications = useCallback(() => {
        setLoading(true);
        loanApi
            .listApplications()
            .then(setApplications)
            .catch((e) => setLoadError(errorMessage(e, "Could not load applications")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => {
        loadRefData();
        loadApplications();
    }, [loadRefData, loadApplications]);
    const selectedBorrower = useMemo(() => borrowers.find((b) => String(b.borrowerId) === borrowerId) ?? null, [borrowers, borrowerId]);
    const selectedProduct = useMemo(() => products.find((p) => String(p.productId) === productId) ?? null, [products, productId]);
    const amountNum = Number(amount);
    const amountError = selectedProduct && amount !== "" && (amountNum < selectedProduct.minAmount || amountNum > selectedProduct.maxAmount)
        ? `Amount must be between ${inr.format(selectedProduct.minAmount)} and ${inr.format(selectedProduct.maxAmount)}`
        : undefined;
    const submit = async (e) => {
        e.preventDefault();
        setFormError(null);
        setSuccess(null);
        if (!selectedBorrower) {
            setFormError("Select a borrower.");
            return;
        }
        if (!selectedProduct) {
            setFormError("Select a loan product.");
            return;
        }
        if (amountError) {
            setFormError(amountError);
            return;
        }
        setSubmitting(true);
        try {
            const app = await loanApi.submitApplication({
                borrowerId: selectedBorrower.borrowerId,
                loanProductId: selectedProduct.productId,
                requestedAmount: amountNum,
                purpose: purpose.trim() || undefined,
                ...(selectedBorrower.groupId ? { groupId: selectedBorrower.groupId } : {}),
            });
            setSuccess(`Application ${app.applicationId} for ${app.borrowerName} submitted — current status: ${app.status}. Automatic credit assessment runs immediately.`);
            setBorrowerId("");
            setProductId("");
            setAmount("");
            setPurpose("");
            loadApplications();
        }
        catch (err) {
            setFormError(errorMessage(err, "Could not submit application"));
        }
        finally {
            setSubmitting(false);
        }
    };
    const columns = [
        { key: "applicationId", header: "App", numeric: true, render: (a) => a.applicationId },
        { key: "borrowerName", header: "Borrower", render: (a) => a.borrowerName },
        { key: "productName", header: "Product", render: (a) => a.productName },
        {
            key: "requestedAmount",
            header: "Requested",
            numeric: true,
            render: (a) => inr.format(a.requestedAmount),
        },
        { key: "applicationDate", header: "Applied", render: (a) => fmtDate(a.applicationDate) },
        {
            key: "status",
            header: "Status",
            render: (a) => <StatusBadge tone={toneForStatus(a.status)}>{a.status}</StatusBadge>,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Loan Applications</h1>
        <p className="mt-2 text-ink-muted">Submit applications for your borrowers; the credit assessment runs automatically.</p>
      </div>

      {loadError && <Alert tone="error">{loadError}</Alert>}

      <Card className="mb-6">
        <div className="flex items-center gap-2 mb-6">
          <FileText size={18}/>
          <span className="mb-0 text-lg font-semibold">
            New application
          </span>
        </div>
        {formError && <Alert tone="error">{formError}</Alert>}
        {success && (<div className="flex items-center gap-2 mb-6" style={{ color: "var(--color-success)" }}>
            <CheckCircle2 size={18}/>
            <span className="text-sm">{success}</span>
          </div>)}
        <form onSubmit={submit}>
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
            gap: "var(--space-4)",
        }}>
            <SelectField id="a-borrower" label="Borrower" value={borrowerId} onChange={(e) => setBorrowerId(e.target.value)} required>
              <option value="">Select a borrower…</option>
              {borrowers.map((b) => (<option key={b.borrowerId} value={b.borrowerId}>
                  {b.name}
                </option>))}
            </SelectField>
            <SelectField id="a-product" label="Loan product" value={productId} onChange={(e) => setProductId(e.target.value)} required>
              <option value="">Select a product…</option>
              {products.map((p) => (<option key={p.productId} value={p.productId}>
                  {p.productName}
                </option>))}
            </SelectField>
            <Input id="a-amount" label="Requested amount (INR)" type="number" min={selectedProduct?.minAmount ?? 1} max={selectedProduct?.maxAmount} value={amount} onChange={(e) => setAmount(e.target.value)} error={amountError} hint={selectedProduct
            ? `Allowed: ${inr.format(selectedProduct.minAmount)} – ${inr.format(selectedProduct.maxAmount)}`
            : undefined} required/>
          </div>

          {selectedBorrower && (<div className="mb-6 text-sm" style={{
                background: "#E0F2F1",
                borderRadius: "var(--radius-md)",
                padding: "var(--space-4)",
            }}>
              <strong>{selectedBorrower.name}</strong> · {selectedBorrower.phone} ·{" "}
              {selectedBorrower.village}, {selectedBorrower.district}
              {selectedBorrower.groupId ? " · Group borrower (joint liability)" : " · Individual"}
            </div>)}

          {selectedProduct && (<div className="text-sm text-ink-muted mb-6">
              {selectedProduct.productName}: {inr.format(selectedProduct.minAmount)}–
              {inr.format(selectedProduct.maxAmount)}, {selectedProduct.tenureMonths} months at{" "}
              {selectedProduct.interestRatePercent}% ({selectedProduct.interestType.replace(/_/g, " ")})
            </div>)}

          <TextareaField id="a-purpose" label="Purpose (optional)" value={purpose} onChange={(e) => setPurpose(e.target.value)}/>

          <div className="mt-4">
            <Button type="submit" loading={submitting} disabled={!!amountError}>
              Submit application
            </Button>
          </div>
        </form>
      </Card>

      <div className="mb-4 text-lg font-semibold">Applications for my borrowers</div>
      <Card>
        <DataTable columns={columns} rows={applications} rowKey={(a) => a.applicationId} loading={loading} emptyLabel="No applications submitted yet"/>
      </Card>
    </>);
}
