import { api } from "./client";
export const branchManagerApi = {
    borrowers() {
        return api.get("/branch-manager/borrowers").then((r) => r.data);
    },
    officers() {
        return api.get("/branch-manager/officers").then((r) => r.data);
    },
    delinquencyCases() {
        return api.get("/branch-manager/delinquency-cases").then((r) => r.data);
    },
    collectionsOfficers() {
        return api.get("/branch-manager/collections-officers").then((r) => r.data);
    },
    assignCase(caseId, collectionsOfficerId) {
        return api
            .put(`/branch-manager/delinquency-cases/${caseId}/assign`, {
            collectionsOfficerId,
        })
            .then((r) => r.data);
    },
    disputes() {
        return api.get("/branch-manager/receipt-disputes").then((r) => r.data);
    },
    coSignReceipt(receiptId, justification) {
        return api
            .put(`/branch-manager/receipts/${receiptId}/co-sign`, { justification })
            .then((r) => r.data);
    },
};
export const collectionsOfficerApi = {
    cases() {
        return api.get("/collections-officer/cases").then((r) => r.data);
    },
};
