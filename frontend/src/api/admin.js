import { api } from "./client";

// NBFC Admin console API
export const adminApi = {
    // Loan products
    products() {
        return api.get("/admin/loan-products").then((r) => r.data);
    },
    product(id) {
        return api.get(`/admin/loan-products/${id}`).then((r) => r.data);
    },
    createProduct(body) {
        return api.post("/admin/loan-products", body).then((r) => r.data);
    },
    updateProduct(id, body) {
        return api.put(`/admin/loan-products/${id}`, body).then((r) => r.data);
    },
    emiPreview(id) {
        return api.get(`/admin/loan-products/${id}/emi-preview`).then((r) => r.data);
    },
    // Users & audit
    users() {
        return api.get("/admin/users").then((r) => r.data);
    },
    createUser(body) {
        return api.post("/admin/users", body).then((r) => r.data);
    },
    updateUser(id, body) {
        return api.put(`/admin/users/${id}`, body).then((r) => r.data);
    },
    deleteUser(id) {
        return api.delete(`/admin/users/${id}`).then((r) => r.data);
    },
    /** Server-side filtered audit log. Undefined filters are omitted from the query. */
    auditLog(filters) {
        const params = {};
        if (filters?.userId != null)
            params.userId = filters.userId;
        if (filters?.module)
            params.module = filters.module;
        if (filters?.action)
            params.action = filters.action;
        if (filters?.from)
            params.from = filters.from;
        if (filters?.to)
            params.to = filters.to;
        if (filters?.limit != null)
            params.limit = filters.limit;
        return api.get("/admin/audit-log", { params }).then((r) => r.data);
    },
    // Delinquency scan
    runDelinquencyScan() {
        return api.post("/admin/delinquency/run").then((r) => r.data);
    },
    /** DEMO/dev tooling: seed a portfolio across PAR30/60/90/180 and run the scan in one click. */
    generateDemoDelinquency() {
        return api.post("/admin/delinquency/demo-portfolio").then((r) => r.data);
    },
};