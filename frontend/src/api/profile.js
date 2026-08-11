import { api } from "./client";

export const profileApi = {
  getProfile() {
    return api.get("/profile").then((r) => r.data);
  },

  updateProfile(body) {
    return api.put("/profile", body).then((r) => r.data);
  },
};