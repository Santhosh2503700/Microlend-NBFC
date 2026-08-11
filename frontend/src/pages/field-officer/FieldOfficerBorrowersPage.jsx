import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { UserPlus, Users, KeyRound, Upload, ShieldCheck, FileText } from "lucide-react";
import { fieldOfficerApi } from "@/api/fieldOfficer";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, Input, StatusBadge, toneForStatus } from "@/components/ui";
import { SelectField, fmtDate } from "./fieldControls";

const GENDERS = ["MALE", "FEMALE", "OTHER"];
const DOC_TYPES = [
    "NATIONAL_ID",
    "VOTER_ID",
    "PASSPORT",
    "UTILITY_BILL",
    "INCOME_PROOF",
    "BANK_STATEMENT",
    "OTHER",
];

const EMPTY_FORM = {
    name: "",
    dateOfBirth: "",
    gender: "MALE",
    nationalIdNumber: "",
    village: "",
    district: "",
    phone: "",
    occupation: "",
    monthlyIncome: "",
    bankAccountNumber: "",
    ifscCode: "",
    portalEmail: "",
    borrowerType: "INDIVIDUAL",
    centreId: "",
    groupId: "",
};

// Helper function to dynamically derive max date (18 years ago from today)
const getEighteenYearsAgo = () => {
    const today = new Date();
    today.setFullYear(today.getFullYear() - 18);
    return today.toISOString().split("T")[0];
};

export function FieldOfficerBorrowersPage() {
    const [borrowers, setBorrowers] = useState([]);
    const [centres, setCentres] = useState([]);
    const [groups, setGroups] = useState([]);
    const [listLoading, setListLoading] = useState(true);
    const [listError, setListError] = useState(null);
    const [form, setForm] = useState(EMPTY_FORM);
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState(null);
    const [result, setResult] = useState(null);

    // KYC upload state
    const [kycBorrowerId, setKycBorrowerId] = useState("");
    const [kycDocType, setKycDocType] = useState("NATIONAL_ID");
    const [kycFile, setKycFile] = useState(null);
    const [kycDocs, setKycDocs] = useState([]);
    const [kycLoading, setKycLoading] = useState(false);
    const [kycError, setKycError] = useState(null);
    const [kycUploading, setKycUploading] = useState(false);
    const [kycNotice, setKycNotice] = useState(null);

    const fileInputRef = useRef(null);

    const loadLists = useCallback(() => {
        setListLoading(true);
        setListError(null);
        Promise.all([
            fieldOfficerApi.myBorrowers(),
            fieldOfficerApi.listCentres(),
            fieldOfficerApi.listGroups(),
        ])
            .then(([b, c, g]) => {
                setBorrowers(b);
                setCentres(c);
                setGroups(g);
            })
            .catch((e) => setListError(errorMessage(e, "Could not load borrowers")))
            .finally(() => setListLoading(false));
    }, []);

    useEffect(() => loadLists(), [loadLists]);

    const set = (key, value) => setForm((prev) => ({ ...prev, [key]: value }));

    const selectedGroup = useMemo(() => groups.find((g) => String(g.groupId) === form.groupId) ?? null, [groups, form.groupId]);

    const derivedCentreName = useMemo(() => {
        if (!selectedGroup)
            return null;
        return centres.find((c) => c.centreId === selectedGroup.centreId)?.centreName ?? null;
    }, [selectedGroup, centres]);

    const submit = async (e) => {
        e.preventDefault();
        setFormError(null);
        setResult(null);

        // Explicit 18+ age validation check on submission
        if (form.dateOfBirth > getEighteenYearsAgo()) {
            setFormError("Borrower must be at least 18 years old.");
            return;
        }

        if (form.borrowerType === "INDIVIDUAL" && !form.centreId) {
            setFormError("Select a centre for an individual borrower.");
            return;
        }

        if (form.borrowerType === "GROUP" && !form.groupId) {
            setFormError("Select a group for a group borrower.");
            return;
        }

        const payload = {
            name: form.name.trim(),
            dateOfBirth: form.dateOfBirth,
            gender: form.gender,
            nationalIdNumber: form.nationalIdNumber.trim(),
            village: form.village.trim(),
            district: form.district.trim(),
            phone: form.phone.trim(),
            occupation: form.occupation.trim() || undefined,
            monthlyIncome: Number(form.monthlyIncome),
            bankAccountNumber: form.bankAccountNumber.trim(),
            ifscCode: form.ifscCode.trim().toUpperCase(),
            portalEmail: form.portalEmail.trim(),
            borrowerType: form.borrowerType,
            ...(form.borrowerType === "INDIVIDUAL"
                ? { centreId: Number(form.centreId) }
                : { groupId: Number(form.groupId) }),
        };

        setSubmitting(true);
        try {
            const res = await fieldOfficerApi.registerBorrower(payload);
            setResult(res);
            setForm(EMPTY_FORM);
            loadLists();
        }
        catch (err) {
            setFormError(errorMessage(err, "Registration failed"));
        }
        finally {
            setSubmitting(false);
        }
    };

    const loadKyc = useCallback((borrowerId) => {
        setKycLoading(true);
        setKycError(null);
        fieldOfficerApi
            .listKyc(borrowerId)
            .then(setKycDocs)
            .catch((e) => setKycError(errorMessage(e, "Could not load KYC documents")))
            .finally(() => setKycLoading(false));
    }, []);

    useEffect(() => {
        if (kycBorrowerId)
            loadKyc(Number(kycBorrowerId));
        else
            setKycDocs([]);
    }, [kycBorrowerId, loadKyc]);

    const uploadKyc = async (e) => {
        e.preventDefault();
        setKycError(null);
        setKycNotice(null);

        if (!kycBorrowerId) {
            setKycError("Select a borrower first.");
            return;
        }

        if (!kycFile) {
            setKycError("Choose a document file to upload.");
            return;
        }

        setKycUploading(true);
        try {
            await fieldOfficerApi.uploadKyc(Number(kycBorrowerId), kycDocType, kycFile);
            setKycNotice("Document uploaded. It is now pending Credit Officer verification.");
            setKycFile(null);
            if (fileInputRef.current) {
                fileInputRef.current.value = "";
            }
            loadKyc(Number(kycBorrowerId));
        }
        catch (err) {
            setKycError(errorMessage(err, "Upload failed"));
        }
        finally {
            setKycUploading(false);
        }
    };

    const borrowerColumns = [
        { key: "name", header: "Name", render: (b) => b.name },
        { key: "phone", header: "Phone", render: (b) => b.phone },
        { key: "location", header: "Village / District", render: (b) => `${b.village}, ${b.district}` },
        {
            key: "borrowerType",
            header: "Type",
            render: (b) => <StatusBadge tone="info">{b.borrowerType}</StatusBadge>,
        },
        { key: "nationalId", header: "National ID", render: (b) => b.nationalIdNumberMasked },
        {
            key: "status",
            header: "Status",
            render: (b) => <StatusBadge tone={toneForStatus(b.status)}>{b.status}</StatusBadge>,
        },
    ];

    const kycColumns = [
        { key: "documentType", header: "Type", render: (k) => k.documentType.replace(/_/g, " ") },
        { key: "uploadedDate", header: "Uploaded", render: (k) => fmtDate(k.uploadedDate) },
        {
            key: "status",
            header: "Status",
            render: (k) => <StatusBadge tone={toneForStatus(k.status)}>{k.status}</StatusBadge>,
        },
        { key: "verifiedDate", header: "Verified", render: (k) => fmtDate(k.verificationDate) },
    ];

    return (
        <>
            <div className="mb-6">
                <h1 className="text-2xl font-bold">Borrowers</h1>
                <p className="mt-2 text-ink-muted">Register borrowers, view your own book, and upload KYC documents.</p>
            </div>

            {result && (
                <Card className="mb-6">
                    <div className="flex items-center gap-2 mb-6" style={{ color: "var(--color-primary)" }}>
                        <ShieldCheck size={20} />
                        <span className="mb-0 text-lg font-semibold">
                            {result.borrower.name} registered
                        </span>
                    </div>
                    <p className="text-sm mb-6">{result.message}</p>
                    <div style={{
                        background: "#E0F2F1",
                        border: "1px solid var(--color-border)",
                        borderRadius: "var(--radius-md)",
                        padding: "var(--space-4)",
                    }}>
                        <div className="flex items-center gap-2 mb-6">
                            <KeyRound size={16} />
                            <strong>Auto-provisioned portal login</strong>
                        </div>
                        <div className="flex flex-col gap-2 text-sm">
                            <div>
                                <strong>Email:</strong> {result.portalEmail}
                            </div>
                            <div>
                                <strong>Temporary password:</strong> <code>{result.portalDefaultPassword}</code>
                            </div>
                            <div className="text-ink-muted">
                                The borrower is forced to reset this password on first login before any workspace is reachable.
                            </div>
                        </div>
                    </div>
                    <div className="mt-4">
                        <Button variant="ghost" size="sm" onClick={() => setResult(null)}>
                            Dismiss
                        </Button>
                    </div>
                </Card>
            )}

            <Card className="mb-6">
                <div className="flex items-center gap-2 mb-6">
                    <UserPlus size={18} />
                    <span className="mb-0 text-lg font-semibold">
                        Register a borrower
                    </span>
                </div>

                {formError && <Alert tone="error">{formError}</Alert>}

                <form onSubmit={submit}>
                    <div className="mb-4 block">
                        <span className="mb-2 block text-sm font-semibold">Borrower type<span className="ml-0.5 text-danger" title="Required">*</span></span>
                        <div className="flex items-center gap-2">
                            {["INDIVIDUAL", "GROUP"].map((t) => (
                                <Button key={t} type="button" variant={form.borrowerType === t ? "primary" : "secondary"} size="sm" onClick={() => set("borrowerType", t)}>
                                    {t === "INDIVIDUAL" ? "Individual" : "Group"}
                                </Button>
                            ))}
                        </div>
                    </div>

                    <div style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
                        gap: "var(--space-4)",
                    }}>
                        <Input
                            id="b-name"
                            label="Full name"
                            maxLength={50}
                            hint={`${form.name.length}/50 characters`}
                            value={form.name}
                            onChange={(e) => set("name", e.target.value)}
                            required
                        />

                        <Input
                            id="b-dob"
                            label="Date of birth"
                            type="date"
                            max={getEighteenYearsAgo()}
                            value={form.dateOfBirth}
                            onChange={(e) => set("dateOfBirth", e.target.value)}
                            required
                        />

                        <SelectField
                            id="b-gender"
                            label="Gender"
                            value={form.gender}
                            onChange={(e) => set("gender", e.target.value)}
                            required
                        >
                            {GENDERS.map((g) => (
                                <option key={g} value={g}>
                                    {g.charAt(0) + g.slice(1).toLowerCase()}
                                </option>
                            ))}
                        </SelectField>
                        <Input id="b-nid" label="National ID (Aadhaar, 12 digits)" type="text" inputMode="numeric" maxLength={12} value={form.nationalIdNumber} onChange={(e) => set("nationalIdNumber", e.target.value.replace(/\D/g, ""))} required />
                      <Input
                          id="b-village"
                          label="Village"
                          maxLength={15}
                          hint={`${form.village.length}/15 characters`}
                          value={form.village}
                          onChange={(e) => set("village", e.target.value)}
                          required
                      />

                      <Input
                          id="b-district"
                          label="District"
                          maxLength={15}
                          hint={`${form.district.length}/15 characters`}
                          value={form.district}
                          onChange={(e) => set("district", e.target.value)}
                          required
                      />
                        <Input id="b-phone" label="Phone (10 digits)" type="tel" maxLength={10} value={form.phone} onChange={(e) => set("phone", e.target.value.replace(/\D/g, ""))} required />
                        <Input id="b-occupation" label="Occupation (optional)" value={form.occupation} onChange={(e) => set("occupation", e.target.value)} />
                        <Input id="b-income" label="Monthly income (INR)" type="number" min={1} value={form.monthlyIncome} onChange={(e) => set("monthlyIncome", e.target.value)} required />
                        <Input id="b-bank" label="Bank account number (9-18 digits)" type="text" inputMode="numeric" maxLength={18} value={form.bankAccountNumber} onChange={(e) => set("bankAccountNumber", e.target.value.replace(/\D/g, ""))} required />
                        <Input id="b-ifsc" label="IFSC code" placeholder="ABCD0XXXXXX" value={form.ifscCode} onChange={(e) => set("ifscCode", e.target.value.toUpperCase())} required />
                        <Input id="b-email" label="Portal email" type="email" autoComplete="off" placeholder="borrower@example.com" value={form.portalEmail} onChange={(e) => set("portalEmail", e.target.value)} required />
                        {form.borrowerType === "INDIVIDUAL" ? (
                            <SelectField id="b-centre" label="Centre" value={form.centreId} onChange={(e) => set("centreId", e.target.value)} required>
                                <option value="">Select a centre…</option>
                                {centres.map((c) => (
                                    <option key={c.centreId} value={c.centreId}>
                                        {c.centreName} — {c.village}
                                    </option>
                                ))}
                            </SelectField>
                        ) : (
                            <SelectField id="b-group" label="Group" value={form.groupId} onChange={(e) => set("groupId", e.target.value)} hint={derivedCentreName
                                ? `Centre auto-derives from the group: ${derivedCentreName}`
                                : "The centre is derived server-side from the group"} required>
                                <option value="">Select a group…</option>
                                {groups.map((g) => (
                                    <option key={g.groupId} value={g.groupId}>
                                        {g.groupName}
                                    </option>
                                ))}
                            </SelectField>
                        )}
                    </div>

                    <div className="mt-4">
                        <Button type="submit" loading={submitting}>
                            Register borrower
                        </Button>
                    </div>
                </form>
            </Card>

            <div className="mb-4 text-lg font-semibold">
                <span className="flex items-center gap-2">
                    <Users size={18} /> My borrowers
                </span>
            </div>
            {listError && <Alert tone="error">{listError}</Alert>}
            <Card className="mb-6">
                <DataTable columns={borrowerColumns} rows={borrowers} rowKey={(b) => b.borrowerId} loading={listLoading} emptyLabel="You have not registered any borrowers yet" />
            </Card>

            <div className="mb-4 text-lg font-semibold">
                <span className="flex items-center gap-2">
                    <Upload size={18} /> KYC document upload
                </span>
            </div>
            <Card>
                {kycError && <Alert tone="error">{kycError}</Alert>}
                {kycNotice && <Alert tone="info">{kycNotice}</Alert>}
                <form onSubmit={uploadKyc}>
                    <div style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                        gap: "var(--space-4)",
                    }}>
                        <SelectField id="kyc-borrower" label="Borrower" value={kycBorrowerId} onChange={(e) => setKycBorrowerId(e.target.value)} required>
                            <option value="">Select a borrower…</option>
                            {borrowers.map((b) => (
                                <option key={b.borrowerId} value={b.borrowerId}>
                                    {b.name}
                                </option>
                            ))}
                        </SelectField>
                        <SelectField id="kyc-type" label="Document type" value={kycDocType} onChange={(e) => setKycDocType(e.target.value)} required>
                            {DOC_TYPES.map((d) => (
                                <option key={d} value={d}>
                                    {d.replace(/_/g, " ")}
                                </option>
                            ))}
                        </SelectField>

                        <div className="mb-4 block">
                            <span className="mb-2 block text-sm font-semibold">Document file<span className="ml-0.5 text-danger" title="Required">*</span></span>
                            <input
                                id="kyc-file"
                                type="file"
                                ref={fileInputRef}
                                onChange={(e) => setKycFile(e.target.files?.[0] ?? null)}
                                style={{ display: "none" }}
                            />
                            <div className="flex items-center gap-3">
                                <Button
                                    type="button"
                                    variant="secondary"
                                    onClick={() => fileInputRef.current?.click()}
                                >
                                    <Upload size={16} /> Choose File
                                </Button>
                                <span className="text-sm text-ink-muted">
                                    {kycFile ? (
                                        <span className="flex items-center gap-1" style={{ color: "var(--color-primary)", fontWeight: 500 }}>
                                            <FileText size={16} /> {kycFile.name}
                                        </span>
                                    ) : (
                                        "No file chosen"
                                    )}
                                </span>
                            </div>
                        </div>
                    </div>
                    <div className="mt-4">
                        <Button type="submit" loading={kycUploading} disabled={!kycBorrowerId}>
                            Upload document
                        </Button>
                    </div>
                </form>

                {kycBorrowerId && (
                    <div className="mt-4">
                        <div className="mb-4 text-lg font-semibold">Documents on file</div>
                        <DataTable columns={kycColumns} rows={kycDocs} rowKey={(k) => k.kycId} loading={kycLoading} emptyLabel="No documents uploaded for this borrower yet" />
                    </div>
                )}
            </Card>
        </>
    );
}