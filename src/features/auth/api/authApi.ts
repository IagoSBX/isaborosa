import { api } from "@/lib/api";
import type { ChangePasswordInput, LoginInput, UserSession } from "../types";

export const authApi = {
  login: (input: LoginInput) => api.post<UserSession>("/api/auth/login", input),
  logout: () => api.post<void>("/api/auth/logout"),
  me: () => api.get<UserSession>("/api/auth/me"),
  changePassword: (input: ChangePasswordInput) => api.post<void>("/api/auth/change-password", input),
};
