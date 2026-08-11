import axios from "axios";
import { tokenStore, UNAUTHORIZED_EVENT } from "./tokenStore";

export const api = axios.create({
    baseURL: "/api",
    headers: { "Content-Type": "application/json" },
});
api.interceptors.request.use((config) => {
    const token = tokenStore.getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});
api.interceptors.response.use((res) => res, (error) => {
    if (error.response?.status === 401) {
        tokenStore.clear();
        window.dispatchEvent(new CustomEvent(UNAUTHORIZED_EVENT));
    }
    return Promise.reject(error);
});
/** Normalises a backend/axios error into a human-readable message for the UI. */
export function errorMessage(err, fallback = "Something went wrong") {
    if (axios.isAxiosError(err)) {
        const data = err.response?.data;
        return data?.message || data?.error || err.message || fallback;
    }
    if (err instanceof Error)
        return err.message;
    return fallback;
}
