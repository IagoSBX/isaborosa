import { useQuery } from "@tanstack/react-query";

import { authApi } from "../api/authApi";

/**
 * 401 aqui significa "sem sessao" (usuario deslogado), nao um erro de rede -
 * por isso retry:false. `isError` no resultado e o sinal de "va pro login".
 */
export function useSession() {
  return useQuery({
    queryKey: ["session"],
    queryFn: () => authApi.me(),
    retry: false,
    staleTime: 1000 * 60,
  });
}
