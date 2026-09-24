import { Link } from "@tanstack/react-router";
import { BookOpen, ChevronLeft, Heart, Trash2 } from "lucide-react";
import { useMemo } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import { AdicionarBibliotecaModal } from "../components/AdicionarBibliotecaModal";
import { ComprarBotoes } from "../components/ComprarBotoes";
import { ContadorPaginas } from "../components/ContadorPaginas";
import { EncontrarGratuitamente } from "../components/EncontrarGratuitamente";
import { RatingStars } from "../components/RatingStars";
import { ResultadosGrid, ResultadosGridSkeleton } from "../components/ResultadosGrid";
import { useBook } from "../hooks/useBook";
import { useAddToLibrary, useLibrary, useRemoveFromLibrary, useUpdateLibraryEntry } from "../hooks/useLibrary";
import { useFreeSources } from "../hooks/useFreeSources";
import { useOpenBook } from "../hooks/useOpenBook";
import { usePrices } from "../hooks/usePrices";
import { useRecommendations } from "../hooks/useRecommendations";
import { useRelatedBooks } from "../hooks/useRelatedBooks";
import { STATUS_META } from "../statusMeta";
import type { ReadingStatus } from "../types";

interface DetalhesLivroProps {
  bookId: number;
}

export function DetalhesLivro({ bookId }: DetalhesLivroProps) {
  const { data: book, isLoading, isError } = useBook(bookId);
  const { data: libraryEntries } = useLibrary();
  const { data: prices, isLoading: isLoadingPrices } = usePrices(bookId);
  const { data: freeSources, isLoading: isLoadingFreeSources } = useFreeSources(bookId);
  const addToLibrary = useAddToLibrary();
  const updateEntry = useUpdateLibraryEntry();
  const removeEntry = useRemoveFromLibrary();
  const { openBook, isPending: isOpeningRelated } = useOpenBook();

  const existingEntry = useMemo(
    () => libraryEntries?.find((entry) => entry.book.id === bookId),
    [libraryEntries, bookId],
  );

  const { data: related, isLoading: isLoadingRelated } = useRelatedBooks(book?.author);
  const relatedFiltered = useMemo(
    () => (related ?? []).filter((item) => item.title.toLowerCase() !== book?.title.toLowerCase()).slice(0, 12),
    [related, book?.title],
  );

  const { data: recommendations, isLoading: isLoadingRecommendations } = useRecommendations();

  function handleAdd(status: ReadingStatus, rating: number | null, favorite: boolean) {
    addToLibrary.mutate(
      { bookId, status, rating, favorite },
      {
        onSuccess: () => toast.success("Livro adicionado à biblioteca!"),
        onError: () => toast.error("Não foi possível adicionar o livro. Tente novamente."),
      },
    );
  }

  function handleUpdateStatus(status: ReadingStatus, rating: number | null, favorite: boolean) {
    if (!existingEntry) return;
    updateEntry.mutate(
      { userBookId: existingEntry.id, input: { status, rating, favorite } },
      {
        onSuccess: () => toast.success("Status atualizado!"),
        onError: () => toast.error("Não foi possível atualizar o status."),
      },
    );
  }

  function handleRemove() {
    if (!existingEntry) return;
    removeEntry.mutate(existingEntry.id, {
      onSuccess: () => toast.success("Removido da biblioteca."),
      onError: () => toast.error("Não foi possível remover o livro."),
    });
  }

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 gap-8 sm:grid-cols-[14rem_1fr]">
        <Skeleton className="aspect-[2/3] w-full max-w-56 rounded-xl" />
        <div className="space-y-3">
          <Skeleton className="h-8 w-2/3" />
          <Skeleton className="h-4 w-1/3" />
          <Skeleton className="h-24 w-full" />
        </div>
      </div>
    );
  }

  if (isError || !book) {
    return <p className="text-sm text-muted-foreground">Não foi possível carregar este livro.</p>;
  }

  const statusMeta = existingEntry ? STATUS_META[existingEntry.status] : undefined;
  const StatusIcon = statusMeta?.icon;

  return (
    <div className="space-y-10">
      <Link
        to="/biblioteca"
        className="inline-flex items-center gap-1 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <ChevronLeft size={16} /> Minha Estante
      </Link>

      <div className="grid grid-cols-1 gap-8 sm:grid-cols-[15rem_1fr]">
        <div className="relative aspect-[2/3] w-full max-w-60 overflow-hidden rounded-2xl border border-border bg-gradient-to-br from-muted to-nude/50 shadow-md">
          {book.coverUrl ? (
            <img
              src={book.coverUrl}
              alt={`Capa de ${book.title}`}
              className="h-full w-full object-cover"
              width={224}
              height={336}
            />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-muted-foreground">
              <BookOpen size={40} strokeWidth={1.5} />
            </div>
          )}
          {existingEntry?.favorite && (
            <span className="absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full bg-background/90 text-primary shadow-sm backdrop-blur-sm">
              <Heart size={16} className="fill-primary stroke-primary" />
            </span>
          )}
        </div>

        <div className="space-y-5">
          <div>
            {book.genre && (
              <span className="mb-2 inline-block rounded-full bg-secondary px-2.5 py-0.5 text-xs font-medium text-secondary-foreground">
                {book.genre}
              </span>
            )}
            <h1 className="font-display text-3xl text-foreground sm:text-4xl">{book.title}</h1>
            {book.author && <p className="mt-1 text-muted-foreground">{book.author}</p>}
            {book.publishYear && <p className="text-sm text-muted-foreground">{book.publishYear}</p>}
          </div>

          {book.description && (
            <p className="max-w-prose whitespace-pre-line text-sm leading-relaxed text-foreground">
              {book.description}
            </p>
          )}

          {(isLoadingPrices ||
            isLoadingFreeSources ||
            (prices && prices.length > 0) ||
            (freeSources && freeSources.length > 0)) && (
            <div className="space-y-3">
              <h2 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">Onde encontrar</h2>
              <div className="flex flex-col gap-2 sm:flex-row sm:flex-wrap">
                <EncontrarGratuitamente sources={freeSources ?? []} isLoading={isLoadingFreeSources} />
                <ComprarBotoes prices={prices ?? []} isLoading={isLoadingPrices} />
              </div>
            </div>
          )}

          <div className="rounded-2xl border border-border bg-gradient-to-br from-card to-rose-wash/10 p-5 shadow-sm">
            {existingEntry ? (
              <div className="space-y-4">
                <div className="flex flex-wrap items-center gap-3">
                  {statusMeta && StatusIcon && (
                    <span
                      className={cn(
                        "inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-sm font-medium",
                        statusMeta.badgeClassName,
                      )}
                    >
                      <StatusIcon size={14} />
                      {statusMeta.label}
                    </span>
                  )}
                  {existingEntry.status === "LIDO" && (
                    <RatingStars
                      value={existingEntry.rating}
                      onChange={(rating) => updateEntry.mutate({ userBookId: existingEntry.id, input: { rating } })}
                    />
                  )}
                </div>

                {existingEntry.status === "LENDO" && (
                  <ContadorPaginas
                    currentPage={existingEntry.currentPage}
                    pageCount={book.pageCount}
                    isPending={updateEntry.isPending}
                    onCommit={(currentPage) =>
                      updateEntry.mutate({ userBookId: existingEntry.id, input: { currentPage } })
                    }
                  />
                )}

                <div className="flex flex-wrap gap-2">
                  <AdicionarBibliotecaModal
                    trigger={
                      <Button variant="outline" size="sm">
                        Alterar status
                      </Button>
                    }
                    bookTitle={book.title}
                    initialStatus={existingEntry.status}
                    initialRating={existingEntry.rating}
                    initialFavorite={existingEntry.favorite}
                    isPending={updateEntry.isPending}
                    confirmLabel="Salvar alterações"
                    onConfirm={handleUpdateStatus}
                  />
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-muted-foreground hover:text-destructive"
                    onClick={handleRemove}
                    disabled={removeEntry.isPending}
                  >
                    <Trash2 size={14} /> Remover da biblioteca
                  </Button>
                </div>
              </div>
            ) : (
              <AdicionarBibliotecaModal
                trigger={
                  <Button size="lg" className="shadow-sm">
                    Adicionar à biblioteca
                  </Button>
                }
                bookTitle={book.title}
                isPending={addToLibrary.isPending}
                onConfirm={handleAdd}
              />
            )}
          </div>
        </div>
      </div>

      {(isLoadingRelated || relatedFiltered.length > 0) && (
        <section>
          <h2 className="mb-3 font-display text-xl text-foreground">Livros semelhantes</h2>
          {isLoadingRelated ? (
            <ResultadosGridSkeleton count={6} />
          ) : (
            <ResultadosGrid items={relatedFiltered} onSelect={openBook} disabled={isOpeningRelated} />
          )}
        </section>
      )}

      {(isLoadingRecommendations || (recommendations && recommendations.length > 0)) && (
        <section>
          <h2 className="mb-3 font-display text-xl text-foreground">Recomendações para você</h2>
          {isLoadingRecommendations ? (
            <ResultadosGridSkeleton count={6} />
          ) : (
            <ResultadosGrid
              items={(recommendations ?? []).map((r) => r.book)}
              getReason={(item) => recommendations?.find((r) => r.book === item)?.reason}
              onSelect={openBook}
              disabled={isOpeningRelated}
            />
          )}
        </section>
      )}
    </div>
  );
}
