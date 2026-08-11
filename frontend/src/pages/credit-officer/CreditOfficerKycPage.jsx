import { useCallback, useEffect, useState } from "react";
import { ExternalLink, FileText, RefreshCw, User } from "lucide-react";
import { creditOfficerApi } from "@/api/loans";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, EmptyState, LoadingState, Select, StatusBadge, toneForStatus } from "@/components/ui";

const formatDate = (iso) => new Date(iso).toLocaleDateString();

const controlStyle = {
    width: "100%",
    minHeight: 42,
    padding: "9px 12px",
    border: "1px solid var(--color-border)",
    borderRadius: "var(--radius-md)",
    background: "#fff",
    fontSize: "var(--fs-base)",
    color: "var(--color-text)",
    fontFamily: "inherit",
};

export function CreditOfficerKycPage() {
    const [borrowers, setBorrowers] = useState([]);
    const [loadingBorrowers, setLoadingBorrowers] = useState(true);
    const [error, setError] = useState(null);
    const [selectedId, setSelectedId] = useState(null);

    const loadBorrowers = useCallback(() => {
        setLoadingBorrowers(true);
        setError(null);
        creditOfficerApi
            .pendingKycBorrowers()
            .then((list) => {
                const sorted = [...list].sort((x, y) => x.name.localeCompare(y.name));
                setBorrowers(sorted);
                setSelectedId((cur) => cur ?? sorted[0]?.borrowerId ?? null);
            })
            .catch((e) => setError(errorMessage(e, "Could not load borrowers")))
            .finally(() => setLoadingBorrowers(false));
    }, []);

    useEffect(() => loadBorrowers(), [loadBorrowers]);

    if (loadingBorrowers)
        return <LoadingState label="Loading borrowers" />;

    return (
        <>
            <div className="mb-6">
                <h1 className="text-2xl font-bold">KYC Review</h1>
                <p className="mt-2 text-ink-muted">Verify or reject the identity documents uploaded for each borrower.</p>
            </div>

            {error && <Alert tone="error">{error}</Alert>}

            <div className="flex flex-wrap items-end gap-2 mb-4">
                <label className="mb-0 block min-w-[260px]">
                    <span className="mb-2 block text-sm font-semibold">Borrower</span>
                    <Select value={selectedId ?? ""} onChange={(e) => setSelectedId(e.target.value ? Number(e.target.value) : null)} style={controlStyle}>
                        {borrowers.map((b) => (
                            <option key={b.borrowerId} value={b.borrowerId}>
                                {b.name}
                            </option>
                        ))}
                    </Select>
                </label>
                <Button variant="ghost" size="sm" onClick={loadBorrowers}>
                    <RefreshCw size={16} /> Refresh
                </Button>
            </div>

            {borrowers.length === 0 ? (
                <Card>
                    <EmptyState icon={User} title="No borrowers to review" description="Borrowers appear here once KYC documents have been uploaded." />
                </Card>
            ) : selectedId != null ? (
                <KycDocsPanel key={selectedId} borrowerId={selectedId} />
            ) : null}
        </>
    );
}

function KycDocsPanel({ borrowerId }) {
    const [docs, setDocs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        creditOfficerApi
            .borrowerKyc(borrowerId)
            .then(setDocs)
            .catch((e) => setError(errorMessage(e, "Could not load KYC documents")))
            .finally(() => setLoading(false));
    }, [borrowerId]);

    useEffect(() => load(), [load]);

    if (loading)
        return <LoadingState label="Loading KYC documents" />;
    if (error)
        return <Alert tone="error">{error}</Alert>;
    if (docs.length === 0) {
        return (
            <Card>
                <EmptyState icon={FileText} title="No KYC documents" description="This borrower has not uploaded any identity documents yet." />
            </Card>
        );
    }

    return (
        <div className="flex flex-col gap-4">
            {docs.map((doc) => (
                <KycDocCard key={doc.kycId} doc={doc} onChanged={load} />
            ))}
        </div>
    );
}

function KycDocCard({ doc, onChanged }) {
    const [rejecting, setRejecting] = useState(false);
    const [remarks, setRemarks] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    const act = (status) => {
        if (status === "REJECTED" && remarks.trim().length === 0) {
            setError("Rejection remarks are required.");
            return;
        }
        setSubmitting(true);
        setError(null);
        creditOfficerApi
            .verifyKyc(doc.kycId, status, status === "REJECTED" ? remarks.trim() : undefined)
            .then(() => {
                setRejecting(false);
                setRemarks("");
                onChanged();
            })
            .catch((e) => setError(errorMessage(e, "Could not update the document")))
            .finally(() => setSubmitting(false));
    };

    const isPending = doc.status === "PENDING";

    return (
        <Card>
            <div className="flex flex-wrap items-center justify-between gap-2 mb-2">
                <div className="flex items-center gap-2">
                    <FileText size={18} style={{ color: "#004D40" }} />
                    <strong>{doc.documentType}</strong>
                    <StatusBadge tone={toneForStatus(doc.status)}>{doc.status}</StatusBadge>
                </div>
                <span className="text-sm text-ink-muted">
                    Uploaded {formatDate(doc.uploadedDate)}
                </span>
            </div>

            <dl className="flex flex-col gap-2" style={{ margin: "0 0 12px" }}>
                <div>
                    <dt className="text-sm text-ink-muted">
                        Reference
                    </dt>
                    <dd style={{ margin: "2px 0 0", color: "#1C2826", fontWeight: 500 }}>
                        {doc.documentRef ?? "—"}
                    </dd>
                </div>
            </dl>

            {error && <Alert tone="error">{error}</Alert>}

            <div className="flex flex-wrap items-center gap-2">
                {/* OPENS DIRECTLY IN A NEW TAB */}
                <a
                    href={creditOfficerApi.kycFileUrl(doc.documentFileUrl)}
                    target="_blank"
                    rel="noreferrer"
                    className="inline-flex min-h-[32px] items-center justify-center gap-2 whitespace-nowrap rounded-md border border-transparent px-3 text-sm font-semibold no-underline transition-colors disabled:cursor-not-allowed disabled:opacity-60 max-md:min-h-[38px] text-primary hover:enabled:bg-selection"
                >
                    <ExternalLink size={14} /> View
                </a>

                {/* ONLY SHOW ACTION BUTTONS WHILE STATUS IS PENDING */}
                {isPending && (
                    <>
                        <Button size="sm" variant="primary" loading={submitting} onClick={() => act("VERIFIED")}>
                            Verify
                        </Button>
                        <Button size="sm" variant="danger" onClick={() => setRejecting((r) => !r)}>
                            Reject
                        </Button>
                    </>
                )}
            </div>

            {/* REJECTION REMARKS FIELD */}
            {rejecting && isPending && (
                <div className="mt-4">
                    <label className="mb-4 block">
                        <span className="mb-2 block text-sm font-semibold">Rejection remarks<span className="ml-0.5 text-danger" title="Required">*</span></span>
                        <textarea
                            value={remarks}
                            onChange={(e) => setRemarks(e.target.value)}
                            rows={3}
                            style={{ ...controlStyle, resize: "vertical" }}
                            placeholder="Explain why this document is being rejected"
                        />
                    </label>
                    <div className="flex items-center gap-2 mt-2">
                        <Button size="sm" variant="secondary" onClick={() => setRejecting(false)}>
                            Cancel
                        </Button>
                        <Button size="sm" variant="danger" loading={submitting} onClick={() => act("REJECTED")}>
                            Confirm Rejection
                        </Button>
                    </div>
                </div>
            )}
        </Card>
    );
}