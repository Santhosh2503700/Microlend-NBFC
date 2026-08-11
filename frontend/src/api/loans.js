import { api } from "./client";
export const loanApi = {
    submitApplication(body) {
        return api.post("/loan-applications", body).then((r) => r.data);
    },
    listApplications() {
        return api.get("/loan-applications").then((r) => r.data);
    },
    productCatalogue() {
        return api.get("/loan-products").then((r) => r.data);
    },
};
export const creditOfficerApi = {
    queue() {
        return api.get("/credit-officer/applications").then((r) => r.data);
    },
    waitlisted() {
        return api
            .get("/credit-officer/applications/waitlisted")
            .then((r) => r.data);
    },
    // Borrowers having PENDING KYC verification.

    pendingKycBorrowers() {
        return api
            .get("/credit-officer/borrowers/pending-kyc")
            .then((r) => r.data);
    },
    assessment(applicationId) {
        return api
            .get(`/credit-officer/applications/${applicationId}/assessment`)
            .then((r) => r.data);
    },
    decide(applicationId, body) {
        return api
            .put(`/credit-officer/applications/${applicationId}/decision`, body)
            .then((r) => r.data);
    },
    borrower(borrowerId) {
        return api
            .get(`/credit-officer/borrowers/${borrowerId}`)
            .then((r) => r.data);
    },
    borrowerKyc(borrowerId) {
        return api
            .get(`/credit-officer/borrowers/${borrowerId}/kyc`)
            .then((r) => r.data);
    },
    verifyKyc(kycId, status, remarks) {
        return api
            .put(`/credit-officer/kyc/${kycId}/verify`, { status, remarks })
            .then((r) => r.data);
    },
    // URL for inline KYC file viewing.

    kycFileUrl(documentFileUrl) {
        return documentFileUrl ?? "";
    },
};
