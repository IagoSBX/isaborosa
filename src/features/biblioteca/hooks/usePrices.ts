import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";

export function usePrices(bookId: number) {
  return useQuery({
    queryKey: ["prices", bookId],
    queryFn: () => libraryApi.getPrices(bookId),
    enabled: Number.isFinite(bookId),
    retry: false,
  });
}
