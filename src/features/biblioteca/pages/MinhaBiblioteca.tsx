import { Heart } from "lucide-react";
import { useMemo, useState } from "react";

import { cn } from "@/lib/utils";
import { Estante } from "../components/Estante";
import { useLibrary } from "../hooks/useLibrary";
import { STATUS_META, STATUS_ORDER } from "../statusMeta";
import type { ReadingStatus } from "../types";

export function MinhaBiblioteca() {
  const [filter, setFilter] = useState<ReadingStatus | undefined>(undefined);
  const [favoritesOnly, setFavoritesOnly] = useState(false);
  const { data, isLoading, isError } = useLibrary(filter);

  const visibleBooks = useMemo(
    () => (favoritesOnly ? (data ?? []).filter((userBook) => userBook.favorite) : (data ?? [])),
    [data, favoritesOnly],
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-2">
        <button
          type="button"
          onClick={() => setFilter(undefined)}
          className={cn(
            "cursor-pointer rounded-full border px-3 py-1.5 text-sm font-medium transition-all",
            filter === undefined
              ? "border-primary bg-primary text-primary-foreground shadow-sm"
              : "border-border bg-card text-muted-foreground hover:border-muted-foreground/30 hover:text-foreground",
          )}
        >
          Todos
        </button>
        {STATUS_ORDER.map((status) => {
          const meta = STATUS_META[status];
          const Icon = meta.icon;
          const selected = filter === status;
          return (
            <button
              key={status}
              type="button"
              onClick={() => setFilter(status)}
              className={cn(
                "flex cursor-pointer items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm font-medium transition-all",
                selected ? cn("shadow-sm", meta.badgeClassName, "border-transparent") : "border-border bg-card text-muted-foreground hover:border-muted-foreground/30 hover:text-foreground",
              )}
            >
              <Icon size={14} />
              {meta.label}
            </button>
          );
        })}

        <button
          type="button"
          onClick={() => setFavoritesOnly((value) => !value)}
          className={cn(
            "ml-auto flex cursor-pointer items-center gap-1.5 rounded-full border px-3 py-1.5 text-sm font-medium transition-all",
            favoritesOnly
              ? "border-transparent bg-rose-wash text-foreground shadow-sm"
              : "border-border bg-card text-muted-foreground hover:border-muted-foreground/30 hover:text-foreground",
          )}
        >
          <Heart size={14} className={cn(favoritesOnly && "fill-primary stroke-primary")} />
          Favoritos
        </button>
      </div>

      {isError ? (
        <p className="text-sm text-muted-foreground">
          Não foi possível carregar sua biblioteca agora. Tente novamente em instantes.
        </p>
      ) : (
        <Estante userBooks={visibleBooks} isLoading={isLoading} />
      )}
    </div>
  );
}
