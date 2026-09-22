import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";
import { useDebouncedValue } from "./useDebouncedValue";

export function useBookSearch(query: string) {
  const debouncedQuery = useDebouncedValue(query);

  return useQuery({
    queryKey: ["book-search", debouncedQuery],
    queryFn: () => libraryApi.searchBooks(debouncedQuery),
    enabled: debouncedQuery.trim().length > 0,
  });
}
