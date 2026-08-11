import { api } from "./client";
/** Live analytics aggregation API. `scope` optional; server defaults by role. */
export const analyticsApi = {
    parDistribution(scope) {
        return api
            .get("/analytics/par-distribution", { params: scope ? { scope } : {} })
            .then((r) => r.data);
    },
    portfolioTrend(scope) {
        return api
            .get("/analytics/portfolio-trend", { params: scope ? { scope } : {} })
            .then((r) => r.data);
    },
    collectionEfficiency(scope) {
        return api
            .get("/analytics/collection-efficiency", {
            params: scope ? { scope } : {},
        })
            .then((r) => r.data);
    },
    officerPerformance(scope) {
        return api
            .get("/analytics/officer-performance", {
            params: scope ? { scope } : {},
        })
            .then((r) => r.data);
    },
    loanFunnel(scope) {
        return api
            .get("/analytics/loan-funnel", { params: scope ? { scope } : {} })
            .then((r) => r.data);
    },
    npaTrend(scope) {
        return api
            .get("/analytics/npa-trend", { params: scope ? { scope } : {} })
            .then((r) => r.data);
    },
};
