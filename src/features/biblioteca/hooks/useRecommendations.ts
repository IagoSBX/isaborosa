import { useQuery } from "@tanstack/react-query";

import { libraryApi } from "../api/libraryApi";

export function useRecommendations() {
  return useQuery({
    queryKey: ["recommendations"],
    queryFn: () => libraryApi.getRecommendations(),
  });
}
