import { useCallback, useEffect, useMemo, useState } from "react";
import { Package, Calculator, Save, Plus } from "lucide-react";
import { adminApi } from "@/api/admin";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, Input, Select, StatusBadge, toneForStatus, } from "@/components/ui";
const money = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
const CATEGORIES = [
    "INDIVIDUAL",
    "GROUP_LENDING",
    "MICRO_ENTERPRISE",
    "AGRICULTURE",
    "EDUCATION",
    "HOUSING",
];
const INTEREST_TYPES = ["REDUCING_BALANCE", "FLAT"];
const EMPTY_FORM = {
    productName: "",
    category: "INDIVIDUAL",
    minAmount: "",
    maxAmount: "",
    tenureMonths: "",
    interestRatePercent: "",
    interestType: "REDUCING_BALANCE",
    processingFeePercent: "",
    status: "ACTIVE",
};
/** Interactive Loan Product Builder with live EMI preview (Phase 8f). */
export function AdminProductsPage() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [editingId, setEditingId] = useState(null);
    const [saving, setSaving] = useState(false);
    const [previewId, setPreviewId] = useState(null);
    const [preview, setPreview] = useState(null);
    const [previewLoading, setPreviewLoading] = useState(false);
    const [previewError, setPreviewError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        adminApi
            .products()
            .then(setProducts)
            .catch((e) => setError(errorMessage(e, "Could not load loan products")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const loadPreview = useCallback((id) => {
        setPreviewId(id);
        setPreviewLoading(true);
        setPreviewError(null);
        setPreview(null);
        adminApi
            .emiPreview(id)
            .then(setPreview)
            .catch((e) => setPreviewError(errorMessage(e, "Could not load EMI preview")))
            .finally(() => setPreviewLoading(false));
    }, []);
    const selectForEdit = useCallback((p) => {
        setEditingId(p.productId);
        setForm({
            productName: p.productName,
            category: p.category,
            minAmount: String(p.minAmount),
            maxAmount: String(p.maxAmount),
            tenureMonths: String(p.tenureMonths),
            interestRatePercent: String(p.interestRatePercent),
            interestType: p.interestType,
            processingFeePercent: p.processingFeePercent == null ? "" : String(p.processingFeePercent),
            status: p.status,
        });
        setSuccess(null);
        setError(null);
        loadPreview(p.productId);
    }, [loadPreview]);
    const resetForm = () => {
        setEditingId(null);
        setForm(EMPTY_FORM);
        setSuccess(null);
        setError(null);
    };
    const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
    const submit = async (e) => {
        e.preventDefault();
        setSaving(true);
        setError(null);
        setSuccess(null);
        const payload = {
            productName: form.productName.trim(),
            category: form.category,
            minAmount: Number(form.minAmount),
            maxAmount: Number(form.maxAmount),
            tenureMonths: Number(form.tenureMonths),
            interestRatePercent: Number(form.interestRatePercent),
            interestType: form.interestType,
            processingFeePercent: form.processingFeePercent.trim() === "" ? null : Number(form.processingFeePercent),
            status: form.status,
        };
        try {
            const saved = editingId
                ? await adminApi.updateProduct(editingId, payload)
                : await adminApi.createProduct(payload);
            setSuccess(editingId
                ? `Product "${saved.productName}" updated.`
                : `Product "${saved.productName}" created.`);
            setEditingId(saved.productId);
            load();
            loadPreview(saved.productId);
        }
        catch (err) {
            setError(errorMessage(err, "Could not save the loan product"));
        }
        finally {
            setSaving(false);
        }
    };
    const columns = useMemo(() => [
        { key: "productName", header: "Name", render: (p) => p.productName },
        { key: "category", header: "Category", render: (p) => p.category.replace(/_/g, " ") },
        {
            key: "range",
            header: "Min – Max",
            numeric: true,
            render: (p) => `${money(p.minAmount)} – ${money(p.maxAmount)}`,
        },
        { key: "tenureMonths", header: "Tenure", numeric: true, render: (p) => `${p.tenureMonths} mo` },
        {
            key: "interestRatePercent",
            header: "Rate %",
            numeric: true,
            render: (p) => `${p.interestRatePercent}%`,
        },
        { key: "interestType", header: "Type", render: (p) => p.interestType.replace(/_/g, " ") },
        {
            key: "status",
            header: "Status",
            render: (p) => <StatusBadge tone={toneForStatus(p.status)}>{p.status}</StatusBadge>,
        },
        {
            key: "actions",
            header: "",
            render: (p) => (<Button size="sm" variant={editingId === p.productId ? "primary" : "secondary"} onClick={() => selectForEdit(p)}>
            Edit / preview
          </Button>),
        },
    ], [editingId, selectForEdit]);
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <Package size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Loan Product Builder
        </h1>
        <p className="mt-2 text-ink-muted">Define lending products. EMI preview below is an illustrative sample only.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}
      {success && <Alert tone="info">{success}</Alert>}

      <div className="grid grid-cols-1 items-start gap-5 lg:grid-cols-[1.15fr_1fr]">
        <Card>
          <h2 className="mb-4 text-lg font-semibold mt-0">
            {editingId ? `Edit product ${editingId}` : "New product"}
          </h2>
          <form onSubmit={submit} className="flex flex-col gap-4">
            <Input id="productName" label="Product name" value={form.productName} onChange={(e) => set("productName", e.target.value)} required/>

            <label className="mb-4 block">
              <span className="mb-2 block text-sm font-semibold">Category<span className="ml-0.5 text-danger" title="Required">*</span></span>
              <Select className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]" value={form.category} onChange={(e) => set("category", e.target.value)}>
                {CATEGORIES.map((c) => (<option key={c} value={c}>
                    {c.replace(/_/g, " ")}
                  </option>))}
              </Select>
            </label>

            <div className="flex flex-wrap items-center gap-2">
              <Input id="minAmount" label="Min amount" type="number" min={1} value={form.minAmount} onChange={(e) => set("minAmount", e.target.value)} required/>
              <Input id="maxAmount" label="Max amount" type="number" min={1} value={form.maxAmount} onChange={(e) => set("maxAmount", e.target.value)} required/>
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <Input id="tenureMonths" label="Tenure (months)" type="number" min={1} value={form.tenureMonths} onChange={(e) => set("tenureMonths", e.target.value)} required/>
              <Input id="interestRatePercent" label="Interest rate %" type="number" min={0} value={form.interestRatePercent} onChange={(e) => set("interestRatePercent", e.target.value)} required/>
            </div>

            <label className="mb-4 block">
              <span className="mb-2 block text-sm font-semibold">Interest type<span className="ml-0.5 text-danger" title="Required">*</span></span>
              <Select className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]" value={form.interestType} onChange={(e) => set("interestType", e.target.value)}>
                {INTEREST_TYPES.map((t) => (<option key={t} value={t}>
                    {t.replace(/_/g, " ")}
                  </option>))}
              </Select>
            </label>

            <div className="flex flex-wrap items-center gap-2">
              <Input id="processingFeePercent" label="Processing fee % (optional)" type="number" min={0} value={form.processingFeePercent} onChange={(e) => set("processingFeePercent", e.target.value)}/>
              <label className="mb-4 block">
                <span className="mb-2 block text-sm font-semibold">Status<span className="ml-0.5 text-danger" title="Required">*</span></span>
                <Select className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]" value={form.status} onChange={(e) => set("status", e.target.value)}>
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="INACTIVE">INACTIVE</option>
                </Select>
              </label>
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <Button type="submit" loading={saving}>
                {editingId ? <Save size={16}/> : <Plus size={16}/>}
                {editingId ? "Save changes" : "Create product"}
              </Button>
              {editingId && (<Button type="button" variant="ghost" onClick={resetForm}>
                  New product
                </Button>)}
            </div>
          </form>
        </Card>

        <Card>
          <h2 className="mb-4 text-lg font-semibold mt-0">
            <Calculator size={18} style={{ verticalAlign: "-3px", marginRight: 6 }}/>
            EMI preview
          </h2>
          {previewId == null ? (<p className="text-sm">
              Save this product or select one from the list to see an illustrative EMI preview.
            </p>) : previewLoading ? (<p className="text-sm">Calculating EMI preview…</p>) : previewError ? (<Alert tone="error">{previewError}</Alert>) : preview ? (<div className="flex flex-col gap-4">
              <div className="grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
                <PreviewCell label={`EMI at ${money(preview.minAmount)}`} value={money(preview.emiAtMin)}/>
                <PreviewCell label={`EMI at ${money(preview.midAmount)}`} value={money(preview.emiAtMid)}/>
                <PreviewCell label={`EMI at ${money(preview.maxAmount)}`} value={money(preview.emiAtMax)}/>
              </div>
              <div className="flex flex-wrap items-center gap-2 text-sm">
                <span>Tenure: {preview.tenureMonths} mo</span>
                <span>Rate: {preview.interestRatePercent}%</span>
                <span>Type: {preview.interestType.replace(/_/g, " ")}</span>
              </div>
              <Alert tone="info">{preview.note}</Alert>
            </div>) : null}
        </Card>
      </div>

      <h2 className="mb-4 text-lg font-semibold mt-6">Products</h2>
      <Card>
        <DataTable columns={columns} rows={products} rowKey={(p) => p.productId} loading={loading} emptyLabel="No loan products defined yet"/>
      </Card>
    </>);
}
function PreviewCell({ label, value }) {
    return (<div>
      <div className="mb-2 block text-sm font-semibold">{label}</div>
      <div style={{ fontWeight: 700, color: "#1C2826", marginTop: 2 }}>{value}</div>
    </div>);
}
