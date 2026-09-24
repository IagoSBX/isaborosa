import { Heart } from "lucide-react";
import { useMemo, useState } from "react";

import { cn } from "@/lib/utils";
import { CarrosselSecao } from "../components/CarrosselSecao";
import { Estante } from "../components/Estante";
import { LivroCard } from "../components/LivroCard";
import { useLibrary } from "../hooks/useLibrary";
import { STATUS_META, STATUS_ORDER } from "../statusMeta";
import type { ReadingStatus, UserBook } from "../types";

function librarySummary(userBooks: ReturnType<typeof useLibrary>["data"]) {
  const books = userBooks ?? [];
  const currentYear = new Date().getFullYear();
  const lidosEsteAno = books.filter(
    (userBook) => userBook.status === "LIDO" && new Date(userBook.updatedAt).getFullYear() === currentYear,
  ).length;
  const emAndamento = books.filter((userBook) => userBook.status === "LENDO").length;
  return { total: books.length, lidosEsteAno, emAndamento, currentYear };
}

function BarraProgresso({ userBook }: { userBook: UserBook }) {
  const { currentPage, book } = userBook;
  const pageCount = book.pageCount;

  if (currentPage != null && pageCount != null && pageCount > 0) {
    const percent = Math.min(100, Math.round((currentPage / pageCount) * 100));
    return (
      <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-muted">
        <div className="h-full rounded-full bg-primary transition-all duration-300" style={{ width: `${percent}%` }} />
      </div>
    );
  }

  // Sem pagina atual ou total de paginas conhecido: indicador so de "ativo",
  // nunca uma porcentagem inventada.
  return (
    <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-muted">
      <div className="h-full w-full animate-pulse rounded-full bg-primary/70" />
    </div>
  );
}

function DestaqueCard({ userBook, showProgress }: { userBook: UserBook; showProgress?: boolean }) {
  return (
    <div className="w-40 shrink-0 snap-start sm:w-44">
      <LivroCard
        title={userBook.book.title}
        author={userBook.book.author}
        coverUrl={userBook.book.coverUrl}
        genre={userBook.book.genre}
        rating={userBook.rating}
        status={userBook.status}
        favorite={userBook.favorite}
        href={`/biblioteca/livro/${userBook.book.id}`}
        footer={showProgress ? <BarraProgresso userBook={userBook} /> : undefined}
      />
    </div>
  );
}

export function MinhaBiblioteca() {
  const [filter, setFilter] = useState<ReadingStatus | undefined>(undefined);
  const [favoritesOnly, setFavoritesOnly] = useState(false);
  const { data, isLoading, isError } = useLibrary();

  const visibleBooks = useMemo(() => {
    let books = data ?? [];
    if (filter) books = books.filter((userBook) => userBook.status === filter);
    if (favoritesOnly) books = books.filter((userBook) => userBook.favorite);
    return books;
  }, [data, filter, favoritesOnly]);

  const summary = useMemo(() => librarySummary(data), [data]);

  const showSections = filter === undefined && !favoritesOnly;
  const lendoAgora = useMemo(() => (data ?? []).filter((userBook) => userBook.status === "LENDO"), [data]);
  const favoritos = useMemo(() => (data ?? []).filter((userBook) => userBook.favorite), [data]);
  const lidosRecentemente = useMemo(
    () =>
      (data ?? [])
        .filter((userBook) => userBook.status === "LIDO")
        .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
        .slice(0, 12),
    [data],
  );

  return (
    <div className="estante-glow space-y-6">
      <div>
        <h2 className="font-display text-xl text-foreground">Olá, Isaborosa.</h2>
        <p className="text-sm text-muted-foreground">Sua estante está esperando por você.</p>
      </div>

      {summary.total > 0 && (
        <p className="text-sm text-muted-foreground">
          <span className="font-medium text-foreground">{summary.total}</span> livro{summary.total === 1 ? "" : "s"}
          {summary.lidosEsteAno > 0 && (
            <>
              {" "}
              · <span className="font-medium text-foreground">{summary.lidosEsteAno}</span> lido
              {summary.lidosEsteAno === 1 ? "" : "s"} em {summary.currentYear}
            </>
          )}
          {summary.emAndamento > 0 && (
            <>
              {" "}
              · <span className="font-medium text-foreground">{summary.emAndamento}</span> em andamento
            </>
          )}
        </p>
      )}

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
        <>
          {showSections && !isLoading && (
            <div className="space-y-8">
              {lendoAgora.length > 0 && (
                <CarrosselSecao title="Lendo agora">
                  {lendoAgora.map((userBook) => (
                    <DestaqueCard key={userBook.id} userBook={userBook} showProgress />
                  ))}
                </CarrosselSecao>
              )}
              {favoritos.length > 0 && (
                <CarrosselSecao title="Favoritos">
                  {favoritos.map((userBook) => (
                    <DestaqueCard key={userBook.id} userBook={userBook} />
                  ))}
                </CarrosselSecao>
              )}
              {lidosRecentemente.length > 0 && (
                <CarrosselSecao title="Lidos recentemente">
                  {lidosRecentemente.map((userBook) => (
                    <DestaqueCard key={userBook.id} userBook={userBook} />
                  ))}
                </CarrosselSecao>
              )}
            </div>
          )}

          {showSections && (lendoAgora.length > 0 || favoritos.length > 0 || lidosRecentemente.length > 0) && (
            <h2 className="font-display text-lg text-foreground">Estante da Baldinho</h2>
          )}

          <Estante userBooks={visibleBooks} isLoading={isLoading} />
        </>
      )}
    </div>
  );
}
