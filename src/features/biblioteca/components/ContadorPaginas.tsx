import { Minus, Plus } from "lucide-react";
import { useEffect, useState } from "react";

interface ContadorPaginasProps {
  currentPage: number | null;
  pageCount: number | null;
  onCommit: (page: number) => void;
  isPending?: boolean;
}

/**
 * Contador de pagina atual para livros "Estou lendo". Quando o livro tem
 * numero total de paginas conhecido, mostra tambem uma barra de progresso
 * real (nunca uma porcentagem inventada quando pageCount e desconhecido).
 */
export function ContadorPaginas({ currentPage, pageCount, onCommit, isPending }: ContadorPaginasProps) {
  const [value, setValue] = useState(currentPage ?? 0);

  useEffect(() => {
    setValue(currentPage ?? 0);
  }, [currentPage]);

  function commit(next: number) {
    const clamped = pageCount != null ? Math.min(Math.max(next, 0), pageCount) : Math.max(next, 0);
    setValue(clamped);
    if (clamped !== (currentPage ?? 0)) {
      onCommit(clamped);
    }
  }

  const percent = pageCount != null && pageCount > 0 ? Math.min(100, Math.round((value / pageCount) * 100)) : null;

  return (
    <div className="space-y-1.5">
      <div className="flex items-center gap-1.5">
        <button
          type="button"
          onClick={() => commit(value - 1)}
          disabled={isPending || value <= 0}
          className="flex h-7 w-7 items-center justify-center rounded-full border border-border text-muted-foreground transition-colors hover:bg-muted disabled:opacity-40"
          aria-label="Página anterior"
        >
          <Minus size={13} />
        </button>
        <input
          type="number"
          inputMode="numeric"
          min={0}
          max={pageCount ?? undefined}
          value={value}
          onChange={(e) => setValue(Number(e.target.value) || 0)}
          onBlur={() => commit(value)}
          className="h-7 w-14 rounded-md border border-border bg-background px-1 text-center text-sm text-foreground"
          aria-label="Página atual"
        />
        <span className="text-xs text-muted-foreground">{pageCount != null ? `de ${pageCount}` : "página atual"}</span>
        <button
          type="button"
          onClick={() => commit(value + 1)}
          disabled={isPending || (pageCount != null && value >= pageCount)}
          className="flex h-7 w-7 items-center justify-center rounded-full border border-border text-muted-foreground transition-colors hover:bg-muted disabled:opacity-40"
          aria-label="Próxima página"
        >
          <Plus size={13} />
        </button>
      </div>
      {percent != null && (
        <div className="h-1.5 w-full max-w-48 overflow-hidden rounded-full bg-muted">
          <div className="h-full rounded-full bg-primary transition-all duration-300" style={{ width: `${percent}%` }} />
        </div>
      )}
    </div>
  );
}
