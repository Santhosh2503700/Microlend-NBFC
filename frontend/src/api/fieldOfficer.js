import { api } from "./client";
// Field Officer API.
export const fieldOfficerApi = {
    // Borrowers
    registerBorrower(body) {
        return api
            .post("/field-officer/borrowers", body)
            .then((r) => r.data);
    },
    myBorrowers() {
        return api.get("/field-officer/borrowers").then((r) => r.data);
    },
    getBorrower(id) {
        return api.get(`/field-officer/borrowers/${id}`).then((r) => r.data);
    },
    // KYC
    listKyc(borrowerId) {
        return api.get(`/field-officer/borrowers/${borrowerId}/kyc`).then((r) => r.data);
    },
    uploadKyc(borrowerId, documentType, file, documentRef) {
        const form = new FormData();
        form.append("documentType", documentType);
        if (documentRef)
            form.append("documentRef", documentRef);
        form.append("file", file);
        return api
            .post(`/field-officer/borrowers/${borrowerId}/kyc`, form, {
            headers: { "Content-Type": "multipart/form-data" },
        })
            .then((r) => r.data);
    },
    // Centres & groups
    listCentres() {
        return api.get("/field-officer/centres").then((r) => r.data);
    },
    createCentre(body) {
        return api.post("/field-officer/centres", body).then((r) => r.data);
    },
    updateCentre(id, body) {
        return api.put(`/field-officer/centres/${id}`, body).then((r) => r.data);
    },
    deleteCentre(id) {
        return api.delete(`/field-officer/centres/${id}`).then((r) => r.data);
    },
    listGroups() {
        return api.get("/field-officer/groups").then((r) => r.data);
    },
    createGroup(body) {
        return api.post("/field-officer/groups", body).then((r) => r.data);
    },
    updateGroup(id, body) {
        return api.put(`/field-officer/groups/${id}`, body).then((r) => r.data);
    },
    deleteGroup(id) {
        return api.delete(`/field-officer/groups/${id}`).then((r) => r.data);
    },
    groupSummary(id) {
        return api.get(`/field-officer/groups/${id}/summary`).then((r) => r.data);
    },
    // Loan look-ups (for collection entry)
    borrowerLoans(borrowerId) {
        return api
            .get(`/field-officer/borrowers/${borrowerId}/loans`)
            .then((r) => r.data);
    },
    loanSchedule(loanAccountId) {
        return api
            .get(`/field-officer/loans/${loanAccountId}/schedule`)
            .then((r) => r.data);
    },
    // Collections
    recordCollection(body) {
        return api.post("/field-officer/collections", body).then((r) => r.data);
    },
    myCollections() {
        return api.get("/field-officer/collections").then((r) => r.data);
    },
};
