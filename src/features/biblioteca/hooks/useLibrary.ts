import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";
import type { AddToLibraryInput, ReadingStatus, UpdateLibraryInput } from "../types";

const libraryKey = (status?: ReadingStatus) => ["library", status ?? "all"] as const;

export function useLibrary(status?: ReadingStatus) {
  return useQuery({
    queryKey: libraryKey(status),
    queryFn: () => libraryApi.listLibrary(status),
  });
}

export function useAddToLibrary() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (input: AddToLibraryInput) => libraryApi.addToLibrary(input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["library"] });
    },
  });
}

export function useUpdateLibraryEntry() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ userBookId, input }: { userBookId: number; input: UpdateLibraryInput }) =>
      libraryApi.updateLibraryEntry(userBookId, input),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["library"] });
    },
  });
}

export function useRemoveFromLibrary() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (userBookId: number) => libraryApi.removeFromLibrary(userBookId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["library"] });
    },
  });
}
