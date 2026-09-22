import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";

export function useStats() {
  return useQuery({
    queryKey: ["stats"],
    queryFn: () => libraryApi.getStats(),
  });
}
