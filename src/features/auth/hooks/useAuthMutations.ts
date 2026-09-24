import { useMutation, useQueryClient } from "@tanstack/react-query";

import { authApi } from "../api/authApi";
import type { ChangePasswordInput, LoginInput, UserSession } from "../types";

export function useLogin() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: LoginInput) => authApi.login(input),
    onSuccess: (session) => {
      queryClient.setQueryData(["session"], session);
    },
  });
}

export function useLogout() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => authApi.logout(),
    onSuccess: () => {
      queryClient.setQueryData(["session"], null);
      queryClient.clear();
    },
  });
}

export function useChangePassword() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: ChangePasswordInput) => authApi.changePassword(input),
    onSuccess: () => {
      queryClient.setQueryData(["session"], (current: UserSession | undefined) =>
        current ? { ...current, mustChangePassword: false } : current,
      );
    },
  });
}
