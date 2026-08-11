/** Small persistence helper for the JWT + a cached principal snapshot (survives page reload). */
const TOKEN_KEY = "microlend.token";
const USER_KEY = "microlend.user";
export const tokenStore = {
    getToken() {
        return localStorage.getItem(TOKEN_KEY);
    },
    setToken(token) {
        localStorage.setItem(TOKEN_KEY, token);
    },
    getUserRaw() {
        return localStorage.getItem(USER_KEY);
    },
    setUserRaw(json) {
        localStorage.setItem(USER_KEY, json);
    },
    clear() {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    },
};
/** Event fired by the axios interceptor when the backend rejects the token (401). */
export const UNAUTHORIZED_EVENT = "microlend:unauthorized";
