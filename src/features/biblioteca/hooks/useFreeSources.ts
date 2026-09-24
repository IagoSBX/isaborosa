import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";

export function useFreeSources(bookId: number) {
  return useQuery({
    queryKey: ["free-sources", bookId],
    queryFn: () => libraryApi.getFreeSources(bookId),
    enabled: Number.isFinite(bookId),
    retry: false,
  });
}
