import { useEffect, useRef } from "react";

import { ResultadosGridSkeleton } from "./ResultadosGrid";

interface InfiniteScrollSentinelProps {
  onVisible: () => void;
  isFetching: boolean;
  hasMore: boolean;
}

export function InfiniteScrollSentinel({ onVisible, isFetching, hasMore }: InfiniteScrollSentinelProps) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const node = ref.current;
    if (!node || !hasMore) return;

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry?.isIntersecting) {
          onVisible();
        }
      },
      { rootMargin: "600px" },
    );
    observer.observe(node);
    return () => observer.disconnect();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hasMore, onVisible]);

  if (!hasMore) {
    return null;
  }

  return (
    <div ref={ref} className="pt-2">
      {isFetching && <ResultadosGridSkeleton count={6} />}
    </div>
  );
}
