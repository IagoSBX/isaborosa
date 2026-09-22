import { useInfiniteQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";
import type { BookSearchResult } from "../types";
import { useDebouncedValue } from "./useDebouncedValue";

function getNextPageParam(lastPage: BookSearchResult[], allPages: BookSearchResult[][]) {
  // Enquanto a fonte devolver resultados, ha mais pra carregar - a API ja
  // combina Open Library + Google Books e pagina por offset (nunca "acaba"
  // de verdade para termos comuns, dado o tamanho dos acervos).
  return lastPage.length > 0 ? allPages.length : undefined;
}

export function useInfiniteBookSearch(query: string) {
  const debouncedQuery = useDebouncedValue(query);

  return useInfiniteQuery({
    queryKey: ["book-search-infinite", debouncedQuery],
    queryFn: ({ pageParam }) => libraryApi.searchBooks(debouncedQuery, pageParam),
    initialPageParam: 0,
    getNextPageParam,
    enabled: debouncedQuery.trim().length > 0,
  });
}

export function useInfiniteBrowse(genre: string | null) {
  return useInfiniteQuery({
    queryKey: ["book-browse-infinite", genre],
    queryFn: ({ pageParam }) => libraryApi.browseByGenre(genre as string, pageParam),
    initialPageParam: 0,
    getNextPageParam,
    enabled: Boolean(genre),
  });
}
