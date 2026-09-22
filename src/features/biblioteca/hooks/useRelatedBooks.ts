import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";

/**
 * "Você também pode gostar": busca mais livros do mesmo autor do primeiro
 * resultado da pesquisa atual. Usa o mesmo endpoint de busca (sem endpoint
 * dedicado no backend), so com o autor como termo.
 */
export function useRelatedBooks(seedAuthor: string | null | undefined) {
  const author = seedAuthor?.split(",")[0]?.trim();

  return useQuery({
    queryKey: ["related-books", author],
    queryFn: () => libraryApi.searchBooks(author as string),
    enabled: Boolean(author),
  });
}
