import { api } from "./client";
export const borrowerApi = {
    dashboard() {
        return api.get("/borrower/dashboard").then((r) => r.data);
    },
    loans() {
        return api.get("/borrower/loans").then((r) => r.data);
    },
    schedule(loanAccountId) {
        return api
            .get(`/borrower/loans/${loanAccountId}/schedule`)
            .then((r) => r.data);
    },
    sanctionLetters() {
        return api.get("/borrower/sanction-letters").then((r) => r.data);
    },
    acceptSanction(id) {
        return api.put(`/borrower/sanction-letters/${id}/accept`).then((r) => r.data);
    },
    rejectSanction(id) {
        return api.put(`/borrower/sanction-letters/${id}/reject`).then((r) => r.data);
    },
    receipts(all = false) {
        return api
            .get("/borrower/receipts", { params: { all } })
            .then((r) => r.data);
    },
    approveReceipt(id) {
        return api.put(`/borrower/receipts/${id}/approve`).then((r) => r.data);
    },
    disputeReceipt(id, disputeRemarks) {
        return api
            .put(`/borrower/receipts/${id}/dispute`, { disputeRemarks })
            .then((r) => r.data);
    },
};
