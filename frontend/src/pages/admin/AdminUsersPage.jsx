import { useCallback, useEffect, useMemo, useState } from "react";
import { UserCog, UserPlus, ChevronUp, ChevronDown, Edit2, Trash2, X, AlertTriangle } from "lucide-react";
import { adminApi } from "@/api/admin";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, Input, Select, StatusBadge, toneForStatus } from "@/components/ui";

const ROLES = [
    "FIELD_OFFICER",
    "CREDIT_OFFICER",
    "BRANCH_MANAGER",
    "COLLECTIONS_OFFICER",
    "NBFC_ADMIN",
    "BORROWER",
];

const roleLabel = (r) => r
    .toLowerCase()
    .split("_")
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");

const EMPTY_CREATE_FORM = {
    name: "",
    email: "",
    phone: "",
    role: "FIELD_OFFICER",
    branchId: "1",
};

const EMPTY_EDIT_FORM = {
    name: "",
    email: "",
    role: "FIELD_OFFICER",
    branchId: "1",
};

export function AdminUsersPage() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [roleFilter, setRoleFilter] = useState("ALL");

    // Inline Card Creation Form State (Create User)
    const [showFormCard, setShowFormCard] = useState(false);
    const [form, setForm] = useState(EMPTY_CREATE_FORM);
    const [submitting, setSubmitting] = useState(false);
    const [formError, setFormError] = useState(null);
    const [successNotice, setSuccessNotice] = useState(null);

    // Edit Popup Modal State
    const [editingUser, setEditingUser] = useState(null);
    const [editForm, setEditForm] = useState(EMPTY_EDIT_FORM);
    const [editSubmitting, setEditSubmitting] = useState(false);
    const [editError, setEditError] = useState(null);

    // Delete Confirmation Popup Modal State
    const [deletingUser, setDeletingUser] = useState(null);
    const [deleteSubmitting, setDeleteSubmitting] = useState(false);

    const load = useCallback(() => {
        setLoading(true);
        setError(null);
        adminApi
            .users()
            .then(setUsers)
            .catch((e) => setError(errorMessage(e, "Could not load users")))
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => load(), [load]);

    const filtered = useMemo(() => (roleFilter === "ALL" ? users : users.filter((u) => u.role === roleFilter)), [users, roleFilter]);

    const setField = (key, val) => setForm((prev) => ({ ...prev, [key]: val }));
    const setEditField = (key, val) => setEditForm((prev) => ({ ...prev, [key]: val }));

    // Create User Handler (Requires 10-digit Phone Validation)
    const handleCreateUser = async (e) => {
        e.preventDefault();
        setFormError(null);
        setSuccessNotice(null);
        setSubmitting(true);

        const payload = {
            name: form.name.trim(),
            email: form.email.trim(),
            phone: form.phone.trim(),
            role: form.role,
            branchId: form.branchId ? Number(form.branchId) : null,
            password: "Password@123",
        };

        try {
            await adminApi.createUser(payload);
            setSuccessNotice(`Account created for ${form.name}. Temporary password set to 'Password@123'.`);
            setForm(EMPTY_CREATE_FORM);
            setShowFormCard(false);
            load();
        } catch (err) {
            setFormError(errorMessage(err, "Failed to create user account"));
        } finally {
            setSubmitting(false);
        }
    };

    // Open Edit Popup Modal
    const startEdit = (user) => {
        setEditError(null);
        setEditingUser(user);
        setEditForm({
            name: user.name || "",
            email: user.email || "",
            role: user.role || "FIELD_OFFICER",
            branchId: user.branchId ? String(user.branchId) : "1",
        });
    };

    // Update User Handler (No Phone field sent)
    const handleUpdateUser = async (e) => {
        e.preventDefault();
        setEditError(null);
        setSuccessNotice(null);
        setEditSubmitting(true);

        const payload = {
            name: editForm.name.trim(),
            email: editForm.email.trim(),
            role: editForm.role,
            branchId: editForm.branchId ? Number(editForm.branchId) : null,
        };

        try {
            await adminApi.updateUser(editingUser.userId, payload);
            setSuccessNotice(`User details updated successfully for ${editForm.name}.`);
            setEditingUser(null);
            load();
        } catch (err) {
            setEditError(errorMessage(err, "Failed to update user"));
        } finally {
            setEditSubmitting(false);
        }
    };

    // Delete User Handler
    const confirmDeleteUser = async () => {
        if (!deletingUser) return;
        setDeleteSubmitting(true);
        setSuccessNotice(null);

        try {
            await adminApi.deleteUser(deletingUser.userId);
            setSuccessNotice(`User account "${deletingUser.name}" has been deleted.`);
            setDeletingUser(null);
            load();
        } catch (err) {
            setError(errorMessage(err, "Could not delete user account"));
            setDeletingUser(null);
        } finally {
            setDeleteSubmitting(false);
        }
    };

    const columns = [
        { key: "name", header: "Name", render: (u) => u.name },
        { key: "email", header: "Email", render: (u) => u.email },
        {
            key: "role",
            header: "Role",
            render: (u) => <StatusBadge tone="info">{roleLabel(u.role)}</StatusBadge>,
        },
        { key: "branchId", header: "Branch", render: (u) => (u.branchId == null ? "—" : `${u.branchId}`) },
        {
            key: "status",
            header: "Status",
            render: (u) => <StatusBadge tone={toneForStatus(u.status)}>{u.status}</StatusBadge>,
        },
        {
            key: "mustResetPassword",
            header: "Password",
            render: (u) => u.mustResetPassword ? (<StatusBadge tone="warning">Reset pending</StatusBadge>) : (<StatusBadge tone="success">Set</StatusBadge>),
        },
        {
            key: "actions",
            header: "Actions",
            render: (u) => (
                <div className="flex items-center gap-2">
                    <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => startEdit(u)}
                        title="Edit User"
                    >
                        <Edit2 size={14} /> Edit
                    </Button>
                    <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setDeletingUser(u)}
                        style={{ color: "var(--color-error, #dc2626)" }}
                        title="Delete User"
                    >
                        <Trash2 size={14} />
                    </Button>
                </div>
            ),
        },
    ];

    return (
        <>
            <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold">
                        <UserCog size={22} style={{ verticalAlign: "-4px", marginRight: 8 }} />
                        User Access Management
                    </h1>
                    <p className="mt-2 text-ink-muted">All portal accounts and their access roles. Passwords are never exposed.</p>
                </div>

                <Button onClick={() => setShowFormCard((prev) => !prev)}>
                    <UserPlus size={18} />
                    {showFormCard ? "Hide Form" : "Create User"}
                    {showFormCard ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
                </Button>
            </div>

            {successNotice && <Alert tone="info">{successNotice}</Alert>}
            {error && <Alert tone="error">{error}</Alert>}

            {/* INLINE CREATE USER CARD */}
            {showFormCard && (
                <Card className="mb-6">
                    <div className="flex items-center gap-2 mb-4 border-b border-border pb-3">
                        <UserPlus size={18} />
                        <h2 style={{ fontSize: "var(--fs-lg)", margin: 0 }}>Provision New Portal Account</h2>
                    </div>

                    {formError && <Alert tone="error">{formError}</Alert>}

                    <form onSubmit={handleCreateUser}>
                        <div style={{
                            display: "grid",
                            gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                            gap: "var(--space-4)",
                        }}>
                            <Input
                                id="u-name"
                                label="Full name"
                                placeholder="e.g. Rahul Sharma"
                                value={form.name}
                                onChange={(e) => setField("name", e.target.value)}
                                required
                            />

                            <Input
                                id="u-email"
                                label="Email address"
                                type="email"
                                placeholder="e.g. rahul@microlend.com"
                                value={form.email}
                                onChange={(e) => setField("email", e.target.value)}
                                required
                            />

                            <Input
                                id="u-phone"
                                label="Phone (10 digits)"
                                type="tel"
                                maxLength={10}
                                placeholder="9876543210"
                                value={form.phone}
                                onChange={(e) => setField("phone", e.target.value.replace(/\D/g, ""))}
                                required
                            />

                            <label className="mb-4 block">
                                <span className="mb-2 block text-sm font-semibold">Role<span className="ml-0.5 text-danger" title="Required">*</span></span>
                                <Select
                                    className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]"
                                    value={form.role}
                                    onChange={(e) => setField("role", e.target.value)}
                                    required
                                >
                                    {ROLES.map((r) => (
                                        <option key={r} value={r}>
                                            {roleLabel(r)}
                                        </option>
                                    ))}
                                </Select>
                            </label>

                            <Input
                                id="u-branch"
                                label="Branch ID"
                                type="number"
                                value={form.branchId}
                                onChange={(e) => setField("branchId", e.target.value)}
                                required
                            />
                        </div>

                        <div className="flex flex-wrap items-center justify-end gap-3 mt-4">
                            <Button type="button" variant="secondary" onClick={() => setShowFormCard(false)}>
                                Cancel
                            </Button>
                            <Button type="submit" loading={submitting}>
                                Provision Account
                            </Button>
                        </div>
                    </form>
                </Card>
            )}

            {/* EDIT USER POPUP MODAL (NO PHONE FIELD) */}
            {editingUser && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-[2px]">
                    <div className="max-h-[90vh] w-full max-w-[500px] overflow-y-auto rounded-lg bg-white p-6 shadow-pop">
                        <div className="flex items-center justify-between" style={{ marginBottom: "var(--space-4)" }}>
                            <h2 style={{ fontSize: "var(--fs-xl)" }} className="flex items-center gap-2">
                                <Edit2 size={20} /> Edit User Details
                            </h2>
                            <button
                                type="button"
                                className="relative inline-flex h-11 w-11 items-center justify-center rounded-md border border-transparent text-ink transition hover:bg-selection"
                                onClick={() => setEditingUser(null)}
                            >
                                <X size={20} />
                            </button>
                        </div>

                        {editError && <Alert tone="error">{editError}</Alert>}

                        <form onSubmit={handleUpdateUser} className="flex flex-col gap-4">
                            <Input
                                id="edit-name"
                                label="Full name"
                                value={editForm.name}
                                onChange={(e) => setEditField("name", e.target.value)}
                                required
                            />

                            <Input
                                id="edit-email"
                                label="Email address"
                                type="email"
                                value={editForm.email}
                                onChange={(e) => setEditField("email", e.target.value)}
                                required
                            />

                            <label className="mb-4 block">
                                <span className="mb-2 block text-sm font-semibold">Role<span className="ml-0.5 text-danger" title="Required">*</span></span>
                                <Select
                                    className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]"
                                    value={editForm.role}
                                    onChange={(e) => setEditField("role", e.target.value)}
                                    required
                                >
                                    {ROLES.map((r) => (
                                        <option key={r} value={r}>
                                            {roleLabel(r)}
                                        </option>
                                    ))}
                                </Select>
                            </label>

                            <Input
                                id="edit-branch"
                                label="Branch ID"
                                type="number"
                                value={editForm.branchId}
                                onChange={(e) => setEditField("branchId", e.target.value)}
                                required
                            />

                            <div className="flex flex-wrap items-center justify-end gap-3 mt-4">
                                <Button type="button" variant="secondary" onClick={() => setEditingUser(null)}>
                                    Cancel
                                </Button>
                                <Button type="submit" loading={editSubmitting}>
                                    Save Changes
                                </Button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* DELETE CONFIRMATION POPUP MODAL */}
            {deletingUser && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 backdrop-blur-[2px]">
                    <div className="max-h-[90vh] w-full max-w-[420px] overflow-y-auto rounded-lg bg-white p-6 shadow-pop">
                        <div className="flex items-center gap-2 mb-3" style={{ color: "var(--color-error, #dc2626)" }}>
                            <AlertTriangle size={24} />
                            <h2 style={{ fontSize: "var(--fs-xl)", margin: 0 }}>Delete User</h2>
                        </div>

                        <p className="mb-4">
                            Are you sure you want to delete user account <strong>{deletingUser.name}</strong> ({deletingUser.email})? This action cannot be undone.
                        </p>

                        <div className="flex flex-wrap items-center justify-end gap-3">
                            <Button
                                type="button"
                                variant="secondary"
                                onClick={() => setDeletingUser(null)}
                                disabled={deleteSubmitting}
                            >
                                Cancel
                            </Button>
                            <Button
                                type="button"
                                onClick={confirmDeleteUser}
                                loading={deleteSubmitting}
                                style={{ backgroundColor: "var(--color-error, #dc2626)", color: "#fff" }}
                            >
                                Delete
                            </Button>
                        </div>
                    </div>
                </div>
            )}

            {/* USERS LIST TABLE */}
            <Card>
                <div className="flex items-end gap-2 mb-4">
                    <label className="mb-4 block max-w-[260px]">
                        <span className="mb-2 block text-sm font-semibold">Filter by role</span>
                        <Select className="min-h-[42px] w-full rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value)}>
                            <option value="ALL">All roles</option>
                            {ROLES.map((r) => (
                                <option key={r} value={r}>
                                    {roleLabel(r)}
                                </option>
                            ))}
                        </Select>
                    </label>
                    <span className="text-sm">
                        {filtered.length} of {users.length} users
                    </span>
                </div>

                <DataTable columns={columns} rows={filtered} rowKey={(u) => u.userId} loading={loading} emptyLabel="No users match this filter" />
            </Card>
        </>
    );
}