import { useMutation, useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";
import type { CreateBookInput } from "../types";

export function useBook(id: number) {
  return useQuery({
    queryKey: ["book", id],
    queryFn: () => libraryApi.getBook(id),
    enabled: Number.isFinite(id),
  });
}

export function useCreateBook() {
  return useMutation({
    mutationFn: (input: CreateBookInput) => libraryApi.createBook(input),
  });
}
