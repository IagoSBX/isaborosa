import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";

export function usePopularBooks() {
  return useQuery({
    queryKey: ["popular-books"],
    queryFn: () => libraryApi.getPopular(),
    staleTime: 1000 * 60 * 30,
  });
}
