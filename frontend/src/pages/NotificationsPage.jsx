import { useCallback, useEffect, useState } from "react";
import { Bell, Check } from "lucide-react";
import { notificationsApi } from "@/api/notifications";
import { errorMessage } from "@/api/client";
import { Alert, Button, Card, DataTable, StatusBadge, toneForStatus, } from "@/components/ui";
export function NotificationsPage() {
    const [rows, setRows] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const load = useCallback(() => {
        setLoading(true);
        notificationsApi
            .list()
            .then(setRows)
            .catch((e) => setError(errorMessage(e, "Could not load notifications")))
            .finally(() => setLoading(false));
    }, []);
    useEffect(() => load(), [load]);
    const markRead = async (id) => {
        await notificationsApi.markRead(id);
        setRows((prev) => prev.map((n) => (n.notificationId === id ? { ...n, status: "READ" } : n)));
    };
    const columns = [
        {
            key: "category",
            header: "Category",
            render: (n) => <StatusBadge tone="info">{n.category ?? "SYSTEM"}</StatusBadge>,
        },
        { key: "message", header: "Message" },
        {
            key: "createdDate",
            header: "Received",
            render: (n) => new Date(n.createdDate).toLocaleString(),
        },
        {
            key: "status",
            header: "Status",
            render: (n) => (<StatusBadge tone={toneForStatus(n.status ?? "READ")}>{n.status ?? "READ"}</StatusBadge>),
        },
        {
            key: "actions",
            header: "",
            render: (n) => n.status === "UNREAD" ? (<Button size="sm" variant="ghost" onClick={() => markRead(n.notificationId)}>
            <Check size={16}/> Mark read
          </Button>) : null,
        },
    ];
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <Bell size={22} style={{ verticalAlign: "-4px", marginRight: 8 }}/>
          Notifications
        </h1>
        <p className="mt-2 text-ink-muted">Every notification is delivered to you specifically — never broadcast.</p>
      </div>
      {error && <Alert tone="error">{error}</Alert>}
      <Card>
        <DataTable columns={columns} rows={rows} rowKey={(n) => n.notificationId} loading={loading} emptyLabel="No notifications yet"/>
      </Card>
    </>);
}
