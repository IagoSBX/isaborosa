import { Skeleton } from "@/components/ui/skeleton";
import { LivroCard } from "./LivroCard";
import type { BookSearchResult } from "../types";

interface ResultadosGridProps {
  items: BookSearchResult[];
  onSelect: (item: BookSearchResult) => void;
  disabled?: boolean;
  getReason?: (item: BookSearchResult) => string | undefined;
}

export function ResultadosGrid({ items, onSelect, disabled, getReason }: ResultadosGridProps) {
  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(9rem,1fr))] gap-x-4 gap-y-6">
      {items.map((item, index) => {
        const reason = getReason?.(item);
        return (
          <button
            key={item.openLibraryKey ?? `${item.title}-${index}`}
            type="button"
            onClick={() => onSelect(item)}
            disabled={disabled}
            className="cursor-pointer text-left disabled:cursor-not-allowed disabled:opacity-60"
          >
            <LivroCard
              title={item.title}
              author={item.author}
              coverUrl={item.coverUrl}
              genre={item.genre}
              footer={
                reason ? <p className="mt-1 line-clamp-2 text-xs text-muted-foreground">{reason}</p> : undefined
              }
            />
          </button>
        );
      })}
    </div>
  );
}

export function ResultadosGridSkeleton({ count = 6 }: { count?: number }) {
  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(9rem,1fr))] gap-x-4 gap-y-6">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index}>
          <Skeleton className="aspect-[2/3] w-full rounded-xl" />
          <Skeleton className="mt-2 h-3 w-3/4" />
        </div>
      ))}
    </div>
  );
}
