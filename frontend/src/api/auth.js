import { api } from "./client";
export const authApi = {
    login(body) {
        return api.post("/auth/login", body).then((r) => r.data);
    },
    resetPassword(body) {
        return api.post("/auth/reset-password", body).then((r) => r.data);
    },
    me() {
        return api.get("/auth/me").then((r) => r.data);
    },
};
