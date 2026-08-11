import { api } from "./client";
export const notificationsApi = {
    list() {
        return api.get("/notifications").then((r) => r.data);
    },
    unreadCount() {
        return api.get("/notifications/unread-count").then((r) => r.data.unread);
    },
    markRead(id) {
        return api.put(`/notifications/${id}/read`).then(() => undefined);
    },
};
