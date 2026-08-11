import { useCallback, useEffect, useMemo, useState } from "react";
import { MapPin, Users2, ClipboardList, Pencil, Trash2, BarChart3, X } from "lucide-react";
import { fieldOfficerApi } from "@/api/fieldOfficer";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, ConfirmDialog, DataTable, Input, KpiCard, Select, StatusBadge, toneForStatus } from "@/components/ui";
import { SelectField, MultiSelectField, fmtDate } from "./fieldControls";

const MEETING_DAYS = [
    "MONDAY",
    "TUESDAY",
    "WEDNESDAY",
    "THURSDAY",
    "FRIDAY",
    "SATURDAY",
    "SUNDAY",
];

const money = (v) => (v == null ? "—" : `₹${Number(v).toLocaleString("en-IN")}`);

export function FieldOfficerCentresPage() {
    const [centres, setCentres] = useState([]);
    const [groups, setGroups] = useState([]);
    const [borrowers, setBorrowers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState(null);

    // Centre form state
    const [editingCentreId, setEditingCentreId] = useState(null);
    const [centreName, setCentreName] = useState("");
    const [centreVillage, setCentreVillage] = useState("");
    const [meetingDay, setMeetingDay] = useState("");
    const [meetingTime, setMeetingTime] = useState(""); // Stores "HH:mm" or "10:30"
    const [meetingAmPm, setMeetingAmPm] = useState("AM");
    const [centreSubmitting, setCentreSubmitting] = useState(false);
    const [centreError, setCentreError] = useState(null);
    const [centreListError, setCentreListError] = useState(null);
    const [centreBusyId, setCentreBusyId] = useState(null);

    // Group form state
    const [editingGroupId, setEditingGroupId] = useState(null);
    const [groupName, setGroupName] = useState("");
    const [groupCentreId, setGroupCentreId] = useState("");
    const [jointLiability, setJointLiability] = useState(true);
    const [memberIds, setMemberIds] = useState([]);
    const [groupSubmitting, setGroupSubmitting] = useState(false);
    const [groupError, setGroupError] = useState(null);
    const [groupListError, setGroupListError] = useState(null);
    const [groupBusyId, setGroupBusyId] = useState(null);

    // Roster
    const [rosterGroupId, setRosterGroupId] = useState("");

    // Group summary
    const [summary, setSummary] = useState(null);
    const [summaryLoading, setSummaryLoading] = useState(false);
    const [summaryError, setSummaryError] = useState(null);

    // In-app confirmation modal (replaces window.confirm) — holds the pending destructive action.
    const [confirmState, setConfirmState] = useState(null);
    const [confirmBusy, setConfirmBusy] = useState(false);

    const load = useCallback(() => {
        setLoading(true);
        setLoadError(null);
        Promise.all([
            fieldOfficerApi.listCentres(),
            fieldOfficerApi.listGroups(),
            fieldOfficerApi.myBorrowers(),
        ])
            .then(([c, g, b]) => {
                setCentres(c);
                setGroups(g);
                setBorrowers(b);
            })
            .catch((e) => setLoadError(errorMessage(e, "Could not load centres/groups")))
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => load(), [load]);

    // Helper: Convert 12-hour input + AM/PM to 24-hour HH:mm format for backend
    const formatTimeWithAmPm = (timeStr, amPm) => {
        if (!timeStr) return undefined;
        let [hours, minutes] = timeStr.split(":").map(Number);
        if (isNaN(hours) || isNaN(minutes)) return undefined;

        if (amPm === "PM" && hours < 12) hours += 12;
        if (amPm === "AM" && hours === 12) hours = 0;

        const formattedHours = String(hours).padStart(2, "0");
        const formattedMinutes = String(minutes).padStart(2, "0");
        return `${formattedHours}:${formattedMinutes}`;
    };

    // Helper: split a stored 24h "HH:mm[:ss]" back into the 12h input value + AM/PM (edit prefill).
    const splitTime24 = (stored) => {
        if (!stored) return { time: "", ampm: "AM" };
        const [h, m] = stored.split(":").map(Number);
        if (isNaN(h) || isNaN(m)) return { time: "", ampm: "AM" };
        const ampm = h >= 12 ? "PM" : "AM";
        let h12 = h % 12;
        if (h12 === 0) h12 = 12;
        return { time: `${String(h12).padStart(2, "0")}:${String(m).padStart(2, "0")}`, ampm };
    };

    const resetCentreForm = () => {
        setEditingCentreId(null);
        setCentreName("");
        setCentreVillage("");
        setMeetingDay("");
        setMeetingTime("");
        setMeetingAmPm("AM");
        setCentreError(null);
    };

    const startEditCentre = (c) => {
        setCentreListError(null);
        setEditingCentreId(c.centreId);
        setCentreName(c.centreName ?? "");
        setCentreVillage(c.village ?? "");
        setMeetingDay(c.meetingDay ?? "");
        const { time, ampm } = splitTime24(c.meetingTime);
        setMeetingTime(time);
        setMeetingAmPm(ampm);
        setCentreError(null);
        if (typeof window !== "undefined") window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // Create OR update a centre with strict validations
    const submitCentre = async (e) => {
        e.preventDefault();
        setCentreError(null);

        const trimmedName = centreName.trim();
        const trimmedVillage = centreVillage.trim();

        if (!trimmedName) {
            setCentreError("Centre name is required.");
            return;
        }
        if (trimmedName.length > 20) {
            setCentreError("Centre name cannot exceed 20 characters.");
            return;
        }
        if (!trimmedVillage) {
            setCentreError("Village is required.");
            return;
        }
        if (trimmedVillage.length > 20) {
            setCentreError("Village cannot exceed 20 characters.");
            return;
        }

        const formattedTime = formatTimeWithAmPm(meetingTime, meetingAmPm);

        const payload = {
            centreName: trimmedName,
            village: trimmedVillage,
            meetingDay: meetingDay || undefined,
            meetingTime: formattedTime,
        };

        setCentreSubmitting(true);
        try {
            if (editingCentreId != null) {
                await fieldOfficerApi.updateCentre(editingCentreId, payload);
            } else {
                await fieldOfficerApi.createCentre(payload);
            }
            resetCentreForm();
            load();
        } catch (err) {
            setCentreError(errorMessage(err, editingCentreId != null ? "Could not update centre" : "Could not create centre"));
        } finally {
            setCentreSubmitting(false);
        }
    };

    const deleteCentre = (c) => {
        setCentreListError(null);
        setConfirmState({
            title: "Delete centre",
            message: `Delete centre "${c.centreName}"? This cannot be undone.`,
            confirmLabel: "Delete",
            onConfirm: () => performDeleteCentre(c),
        });
    };

    const performDeleteCentre = async (c) => {
        setConfirmBusy(true);
        setCentreBusyId(c.centreId);
        try {
            await fieldOfficerApi.deleteCentre(c.centreId);
            if (editingCentreId === c.centreId) resetCentreForm();
            load();
            setConfirmState(null);
        } catch (err) {
            setCentreListError(errorMessage(err, "Could not delete centre"));
            setConfirmState(null);
        } finally {
            setConfirmBusy(false);
            setCentreBusyId(null);
        }
    };

    const resetGroupForm = () => {
        setEditingGroupId(null);
        setGroupName("");
        setGroupCentreId("");
        setJointLiability(true);
        setMemberIds([]);
        setGroupError(null);
    };

    const startEditGroup = (g) => {
        setGroupListError(null);
        setEditingGroupId(g.groupId);
        setGroupName(g.groupName ?? "");
        setGroupCentreId(String(g.centreId));
        setJointLiability(g.jointLiabilityEnabled);
        setMemberIds([]); // membership is managed at creation; edit only renames + toggles JLG
        setGroupError(null);
        if (typeof window !== "undefined") window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // Create OR update a group with strict validations
    const submitGroup = async (e) => {
        e.preventDefault();
        setGroupError(null);

        const trimmedGroupName = groupName.trim();

        if (!trimmedGroupName) {
            setGroupError("Group name is required.");
            return;
        }
        if (trimmedGroupName.length > 20) {
            setGroupError("Group name cannot exceed 20 characters.");
            return;
        }
        if (!groupCentreId) {
            setGroupError("Select the centre this group belongs to.");
            return;
        }

        setGroupSubmitting(true);
        try {
            if (editingGroupId != null) {
                // Membership is managed at registration — update only renames + toggles JLG.
                await fieldOfficerApi.updateGroup(editingGroupId, {
                    groupName: trimmedGroupName,
                    centreId: Number(groupCentreId),
                    jointLiabilityEnabled: jointLiability,
                });
            } else {
                await fieldOfficerApi.createGroup({
                    groupName: trimmedGroupName,
                    centreId: Number(groupCentreId),
                    jointLiabilityEnabled: jointLiability,
                    memberBorrowerIds: memberIds.length ? memberIds : undefined,
                });
            }
            resetGroupForm();
            load();
        } catch (err) {
            setGroupError(errorMessage(err, editingGroupId != null ? "Could not update group" : "Could not create group"));
        } finally {
            setGroupSubmitting(false);
        }
    };

    const deleteGroup = (g) => {
        setGroupListError(null);
        setConfirmState({
            title: "Delete group",
            message: `Delete group "${g.groupName}"? This cannot be undone.`,
            confirmLabel: "Delete",
            onConfirm: () => performDeleteGroup(g),
        });
    };

    const performDeleteGroup = async (g) => {
        setConfirmBusy(true);
        setGroupBusyId(g.groupId);
        try {
            await fieldOfficerApi.deleteGroup(g.groupId);
            if (editingGroupId === g.groupId) resetGroupForm();
            if (summary?.groupId === g.groupId) setSummary(null);
            load();
            setConfirmState(null);
        } catch (err) {
            setGroupListError(errorMessage(err, "Could not delete group"));
            setConfirmState(null);
        } finally {
            setConfirmBusy(false);
            setGroupBusyId(null);
        }
    };

    const viewSummary = async (g) => {
        setSummaryError(null);
        setSummaryLoading(true);
        setSummary(null);
        try {
            const s = await fieldOfficerApi.groupSummary(g.groupId);
            setSummary(s);
            if (typeof window !== "undefined") {
                window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });
            }
        } catch (err) {
            setSummaryError(errorMessage(err, "Could not load group summary"));
        } finally {
            setSummaryLoading(false);
        }
    };

    const centreName_ = useCallback((id) => centres.find((c) => c.centreId === id)?.centreName ?? `Centre ${id}`, [centres]);

    const rosterMembers = useMemo(() => rosterGroupId
        ? borrowers.filter((b) => String(b.groupId) === rosterGroupId)
        : [], [borrowers, rosterGroupId]);

    const centreColumns = [
        { key: "centreName", header: "Centre", render: (c) => c.centreName },
        { key: "village", header: "Village", render: (c) => c.village },
        { key: "meetingDay", header: "Meeting day", render: (c) => c.meetingDay ?? "—" },
        { key: "meetingTime", header: "Meeting time", render: (c) => c.meetingTime ?? "—" },
        {
            key: "status",
            header: "Status",
            render: (c) => <StatusBadge tone={toneForStatus(c.status)}>{c.status}</StatusBadge>,
        },
        {
            key: "actions",
            header: "Actions",
            render: (c) => (
                <div className="flex flex-wrap items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={() => startEditCentre(c)} title="Edit centre">
                        <Pencil size={14} /> Edit
                    </Button>
                    <Button variant="danger" size="sm" loading={centreBusyId === c.centreId}
                        onClick={() => deleteCentre(c)} title="Delete centre">
                        <Trash2 size={14} /> Delete
                    </Button>
                </div>
            ),
        },
    ];

    const groupColumns = [
        { key: "groupName", header: "Group", render: (g) => g.groupName },
        { key: "centre", header: "Centre", render: (g) => centreName_(g.centreId) },
        { key: "memberCount", header: "Members", numeric: true, render: (g) => g.memberCount },
        {
            key: "jointLiabilityEnabled",
            header: "Joint liability",
            render: (g) => (
                <StatusBadge tone={g.jointLiabilityEnabled ? "success" : "neutral"}>
                    {g.jointLiabilityEnabled ? "Enabled" : "Off"}
                </StatusBadge>
            ),
        },
        { key: "formationDate", header: "Formed", render: (g) => fmtDate(g.formationDate) },
        {
            key: "status",
            header: "Status",
            render: (g) => <StatusBadge tone={toneForStatus(g.status)}>{g.status}</StatusBadge>,
        },
        {
            key: "actions",
            header: "Actions",
            render: (g) => (
                <div className="flex flex-wrap items-center gap-2">
                    <Button variant="ghost" size="sm" onClick={() => startEditGroup(g)} title="Edit group">
                        <Pencil size={14} /> Edit
                    </Button>
                    <Button variant="ghost" size="sm" onClick={() => viewSummary(g)} title="View summary">
                        <BarChart3 size={14} /> Summary
                    </Button>
                    <Button variant="danger" size="sm" loading={groupBusyId === g.groupId}
                        onClick={() => deleteGroup(g)} title="Delete group">
                        <Trash2 size={14} /> Delete
                    </Button>
                </div>
            ),
        },
    ];

    const rosterColumns = [
        { key: "name", header: "Member", render: (b) => b.name },
        { key: "phone", header: "Phone", render: (b) => b.phone },
        { key: "village", header: "Village", render: (b) => b.village },
        { key: "nationalId", header: "National ID", render: (b) => b.nationalIdNumberMasked },
        {
            key: "status",
            header: "Status",
            render: (b) => <StatusBadge tone={toneForStatus(b.status)}>{b.status}</StatusBadge>,
        },
    ];

    const summaryMemberColumns = [
        { key: "name", header: "Member", render: (m) => m.name },
        { key: "activeLoans", header: "Active loans", numeric: true, render: (m) => m.activeLoans },
        { key: "outstandingPrincipal", header: "Outstanding", numeric: true, render: (m) => money(m.outstandingPrincipal) },
        {
            key: "hasOverdue",
            header: "Overdue?",
            render: (m) => (
                <StatusBadge tone={m.hasOverdue ? "danger" : "success"}>
                    {m.hasOverdue ? "Overdue" : "Clear"}
                </StatusBadge>
            ),
        },
    ];

    return (
        <>
            <div className="mb-6">
                <h1 className="text-2xl font-bold">Centres &amp; Groups</h1>
                <p className="mt-2 text-ink-muted">Create meeting centres and joint-liability groups for your borrowers.</p>
            </div>

            {loadError && <Alert tone="error">{loadError}</Alert>}

            {/* CREATE / EDIT CENTRE CARD */}
            <Card className="mb-6">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-6">
                    <div className="flex items-center gap-2">
                        <MapPin size={18} />
                        <span className="mb-0 text-lg font-semibold">
                            {editingCentreId != null ? "Edit centre" : "Create a centre"}
                        </span>
                    </div>
                    {editingCentreId != null && (
                        <Button variant="ghost" size="sm" onClick={resetCentreForm}>
                            <X size={14} /> Cancel edit
                        </Button>
                    )}
                </div>
                {centreError && <Alert tone="error">{centreError}</Alert>}
                <form onSubmit={submitCentre}>
                    <div style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                        gap: "var(--space-4)",
                    }}>
                        <div>
                            <Input
                                id="c-name"
                                label="Centre name"
                                value={centreName}
                                maxLength={20}
                                onChange={(e) => setCentreName(e.target.value)}
                                placeholder="Max 20 chars"
                                required
                            />
                            <span className="text-xs text-ink-muted block mt-1">
                                {centreName.length}/20 chars
                            </span>
                        </div>

                        <div>
                            <Input
                                id="c-village"
                                label="Village"
                                value={centreVillage}
                                maxLength={20}
                                onChange={(e) => setCentreVillage(e.target.value)}
                                placeholder="Max 20 chars"
                                required
                            />
                            <span className="text-xs text-ink-muted block mt-1">
                                {centreVillage.length}/20 chars
                            </span>
                        </div>

                        <SelectField id="c-day" label="Meeting day (optional)" value={meetingDay} onChange={(e) => setMeetingDay(e.target.value)}>
                            <option value="">Not set</option>
                            {MEETING_DAYS.map((d) => (
                                <option key={d} value={d}>
                                    {d.charAt(0) + d.slice(1).toLowerCase()}
                                </option>
                            ))}
                        </SelectField>

                        {/* Meeting Time + AM/PM Selector */}
                        <div className="mb-4 block">
                            <span className="mb-2 block text-sm font-semibold">Meeting time (optional)</span>
                            <div className="flex items-center gap-2">
                                <input
                                    id="c-time"
                                    type="time"
                                    className="flex min-h-[42px] w-full items-center gap-2 rounded-md border border-border bg-white px-3 transition focus-within:border-primary-hover focus-within:ring-2 focus-within:ring-primary-hover/20 max-md:min-h-[46px]"
                                    style={{ flex: 1 }}
                                    value={meetingTime}
                                    onChange={(e) => setMeetingTime(e.target.value)}
                                />
                                <Select
                                    className="min-h-[42px] rounded-md border border-border bg-white px-3 text-base text-ink transition focus:border-primary-hover focus:ring-2 focus:ring-primary-hover/20 max-md:min-h-[46px]"
                                    style={{ width: "80px" }}
                                    value={meetingAmPm}
                                    onChange={(e) => setMeetingAmPm(e.target.value)}
                                >
                                    <option value="AM">AM</option>
                                    <option value="PM">PM</option>
                                </Select>
                            </div>
                        </div>
                    </div>
                    <div className="mt-4 flex flex-wrap items-center gap-2">
                        <Button type="submit" loading={centreSubmitting}>
                            {editingCentreId != null ? "Save changes" : "Create centre"}
                        </Button>
                        {editingCentreId != null && (
                            <Button type="button" variant="secondary" onClick={resetCentreForm}>
                                Cancel
                            </Button>
                        )}
                    </div>
                </form>
            </Card>

            <div className="mb-4 text-lg font-semibold">Centres</div>
            {centreListError && <Alert tone="error">{centreListError}</Alert>}
            <Card className="mb-6">
                <DataTable columns={centreColumns} rows={centres} rowKey={(c) => c.centreId} loading={loading} emptyLabel="No centres created yet" />
            </Card>

            {/* CREATE / EDIT GROUP CARD */}
            <Card className="mb-6">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between mb-6">
                    <div className="flex items-center gap-2">
                        <Users2 size={18} />
                        <span className="mb-0 text-lg font-semibold">
                            {editingGroupId != null ? "Edit group" : "Create a joint-liability group"}
                        </span>
                    </div>
                    {editingGroupId != null && (
                        <Button variant="ghost" size="sm" onClick={resetGroupForm}>
                            <X size={14} /> Cancel edit
                        </Button>
                    )}
                </div>
                {groupError && <Alert tone="error">{groupError}</Alert>}
                <form onSubmit={submitGroup}>
                    <div style={{
                        display: "grid",
                        gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                        gap: "var(--space-4)",
                    }}>
                        <div>
                            <Input
                                id="g-name"
                                label="Group name"
                                value={groupName}
                                maxLength={20}
                                onChange={(e) => setGroupName(e.target.value)}
                                placeholder="Max 20 chars"
                                required
                            />
                            <span className="text-xs text-ink-muted block mt-1">
                                {groupName.length}/20 chars
                            </span>
                        </div>

                        <SelectField id="g-centre" label="Centre" value={groupCentreId} onChange={(e) => setGroupCentreId(e.target.value)} required>
                            <option value="">Select a centre…</option>
                            {centres.map((c) => (
                                <option key={c.centreId} value={c.centreId}>
                                    {c.centreName} — {c.village}
                                </option>
                            ))}
                        </SelectField>

                        {editingGroupId == null && (
                            <MultiSelectField id="g-members" label="Members (optional, hold Ctrl/Cmd to multi-select)" value={memberIds.map(String)} onChange={(e) => setMemberIds(Array.from(e.target.selectedOptions).map((o) => Number(o.value)))}>
                                {borrowers.map((b) => (
                                    <option key={b.borrowerId} value={b.borrowerId}>
                                        {b.name}
                                    </option>
                                ))}
                            </MultiSelectField>
                        )}
                    </div>

                    {editingGroupId != null && (
                        <p className="text-xs text-ink-muted mt-2">
                            Membership is managed at borrower registration — editing a group only renames it and toggles joint liability.
                        </p>
                    )}

                    <label className="mb-4 flex items-center gap-2 mt-4 cursor-pointer">
                        <input type="checkbox" checked={jointLiability} onChange={(e) => setJointLiability(e.target.checked)} />
                        <span className="mb-0 block text-sm font-semibold">
                            Joint liability enabled
                        </span>
                    </label>

                    <div className="mt-4 flex flex-wrap items-center gap-2">
                        <Button type="submit" loading={groupSubmitting}>
                            {editingGroupId != null ? "Save changes" : "Create group"}
                        </Button>
                        {editingGroupId != null && (
                            <Button type="button" variant="secondary" onClick={resetGroupForm}>
                                Cancel
                            </Button>
                        )}
                    </div>
                </form>
            </Card>

            <div className="mb-4 text-lg font-semibold">Groups</div>
            {groupListError && <Alert tone="error">{groupListError}</Alert>}
            <Card className="mb-6">
                <DataTable columns={groupColumns} rows={groups} rowKey={(g) => g.groupId} loading={loading} emptyLabel="No groups created yet" />
            </Card>

            {/* GROUP SUMMARY CARD */}
            {summaryError && <Alert tone="error">{summaryError}</Alert>}
            {summary && (
                <>
                    <div className="mb-4 text-lg font-semibold">
                        <span className="flex items-center gap-2 justify-between">
                            <span className="flex items-center gap-2">
                                <BarChart3 size={18} /> Group summary — {summary.groupName}
                            </span>
                            <Button variant="ghost" size="sm" onClick={() => setSummary(null)}>
                                <X size={14} /> Close
                            </Button>
                        </span>
                    </div>
                    <Card className="mb-6">
                        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
                            <KpiCard icon={Users2} label="Members" value={summary.memberCount} />
                            <KpiCard icon={BarChart3} label="Total disbursed" value={money(summary.totalDisbursed)} />
                            <KpiCard icon={BarChart3} label="Total outstanding" value={money(summary.totalOutstanding)} />
                            <KpiCard icon={ClipboardList} label="Members overdue" value={summary.overdueMemberCount} />
                        </div>
                        <div className="mt-4">
                            <DataTable columns={summaryMemberColumns} rows={summary.members ?? []}
                                rowKey={(m) => m.borrowerId} loading={summaryLoading}
                                emptyLabel="No members in this group yet" />
                        </div>
                    </Card>
                </>
            )}

            {/* ROSTER CARD */}
            <div className="mb-4 text-lg font-semibold">
                <span className="flex items-center gap-2">
                    <ClipboardList size={18} /> Joint-liability group roster
                </span>
            </div>
            <Card>
                <SelectField id="roster-group" label="Select a group to view its members" value={rosterGroupId} onChange={(e) => setRosterGroupId(e.target.value)}>
                    <option value="">Select a group…</option>
                    {groups.map((g) => (
                        <option key={g.groupId} value={g.groupId}>
                            {g.groupName} ({g.memberCount} members)
                        </option>
                    ))}
                </SelectField>
                {rosterGroupId && (
                    <DataTable columns={rosterColumns} rows={rosterMembers} rowKey={(b) => b.borrowerId} loading={loading} emptyLabel="No members assigned to this group yet" />
                )}
            </Card>

            <ConfirmDialog
                open={confirmState != null}
                title={confirmState?.title}
                message={confirmState?.message}
                confirmLabel={confirmState?.confirmLabel ?? "Delete"}
                loading={confirmBusy}
                onConfirm={() => confirmState?.onConfirm?.()}
                onCancel={() => !confirmBusy && setConfirmState(null)}
            />
        </>
    );
}
