import { Sparkles } from "lucide-react";
import { useMemo, useState } from "react";

import { BuscaBarra } from "../components/BuscaBarra";
import { GenreCard } from "../components/GenreCard";
import { InfiniteScrollSentinel } from "../components/InfiniteScrollSentinel";
import { LivroCard } from "../components/LivroCard";
import { ResultadosGrid, ResultadosGridSkeleton } from "../components/ResultadosGrid";
import { GENRE_CATEGORIES } from "../genreCategories";
import { useInfiniteBookSearch, useInfiniteBrowse } from "../hooks/useInfiniteBooks";
import { useLibrary } from "../hooks/useLibrary";
import { useOpenBook } from "../hooks/useOpenBook";
import { usePopularBooks } from "../hooks/usePopularBooks";
import { useRecommendations } from "../hooks/useRecommendations";
import { useRelatedBooks } from "../hooks/useRelatedBooks";

const DEFAULT_EXPLORE_GENRE = "Ficção";

function RecommendationsEmptyState() {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-border bg-gradient-to-br from-card to-rose-wash/20 px-6 py-14 text-center">
      <Sparkles size={26} className="text-primary" strokeWidth={1.5} />
      <p className="font-display text-lg text-foreground">Recomendações chegam depois de algumas avaliações.</p>
      <p className="max-w-sm text-sm text-muted-foreground">
        Avalie com estrelas os livros que você marcar como "já li" para receber sugestões baseadas nos gêneros e
        autores que você mais lê.
      </p>
    </div>
  );
}

export function Pesquisar() {
  const [query, setQuery] = useState("");
  const [activeCategory, setActiveCategory] = useState<string | null>(null);
  const hasQuery = query.trim().length > 0;
  const mode: "search" | "category" | "idle" = hasQuery ? "search" : activeCategory ? "category" : "idle";

  const { openBook, isPending: isOpening } = useOpenBook();

  // --- modo pesquisa ---
  const searchQuery = useInfiniteBookSearch(query);
  const searchResults = useMemo(() => searchQuery.data?.pages.flat() ?? [], [searchQuery.data]);
  const firstResultAuthor = searchResults[0]?.author;
  const { data: related, isLoading: isLoadingRelated } = useRelatedBooks(mode === "search" ? firstResultAuthor : null);
  const relatedFiltered = useMemo(() => {
    if (!related) return [];
    const seenTitles = new Set(searchResults.map((r) => r.title.toLowerCase()));
    return related.filter((item) => !seenTitles.has(item.title.toLowerCase())).slice(0, 12);
  }, [related, searchResults]);

  // --- modo categoria ---
  const categoryQuery = useInfiniteBrowse(mode === "category" ? activeCategory : null);
  const categoryResults = useMemo(() => categoryQuery.data?.pages.flat() ?? [], [categoryQuery.data]);

  // --- modo ocioso ---
  const { data: popular, isLoading: isLoadingPopular } = usePopularBooks();
  const { data: libraryEntries } = useLibrary();
  const recentBooks = useMemo(
    () =>
      [...(libraryEntries ?? [])]
        .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        .slice(0, 12)
        .map((entry) => entry.book),
    [libraryEntries],
  );
  const exploreQuery = useInfiniteBrowse(mode === "idle" ? DEFAULT_EXPLORE_GENRE : null);
  const exploreResults = useMemo(() => exploreQuery.data?.pages.flat() ?? [], [exploreQuery.data]);

  // --- recomendações (usadas em pesquisa e ociosa) ---
  const { data: recommendations, isLoading: isLoadingRecommendations } = useRecommendations();

  return (
    <div className="space-y-10">
      <div className="rounded-2xl border border-border bg-gradient-to-br from-card to-nude/20 p-5 shadow-sm">
        <h1 className="font-display text-2xl text-foreground">Pesquisar livros</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Pesquise por título, autor ou gênero — resultados, relacionados e recomendações aparecem logo abaixo.
        </p>
        <div className="mt-4">
          <BuscaBarra value={query} onChange={(value) => { setQuery(value); if (value.trim()) setActiveCategory(null); }} />
        </div>

        {mode !== "search" && (
          <div className="mt-4 flex gap-2.5 overflow-x-auto pb-1">
            {GENRE_CATEGORIES.map((genre) => (
              <GenreCard
                key={genre}
                genre={genre}
                active={activeCategory === genre}
                onClick={() => setActiveCategory((current) => (current === genre ? null : genre))}
              />
            ))}
          </div>
        )}
      </div>

      {mode === "search" && (
        <>
          <section>
            <h2 className="mb-3 font-display text-xl text-foreground">Resultados para "{query}"</h2>
            {searchQuery.isError && (
              <p className="text-sm text-muted-foreground">Não foi possível buscar agora. Tente novamente.</p>
            )}
            {searchQuery.isLoading && <ResultadosGridSkeleton />}
            {!searchQuery.isLoading && searchResults.length === 0 && (
              <p className="text-sm text-muted-foreground">Nenhum resultado encontrado para "{query}".</p>
            )}
            {searchResults.length > 0 && (
              <>
                <ResultadosGrid items={searchResults} onSelect={openBook} disabled={isOpening} />
                <InfiniteScrollSentinel
                  onVisible={() => searchQuery.fetchNextPage()}
                  isFetching={searchQuery.isFetchingNextPage}
                  hasMore={Boolean(searchQuery.hasNextPage)}
                />
              </>
            )}
          </section>

          {relatedFiltered.length > 0 && (
            <section>
              <h2 className="mb-3 font-display text-xl text-foreground">Livros relacionados</h2>
              {isLoadingRelated ? (
                <ResultadosGridSkeleton count={6} />
              ) : (
                <ResultadosGrid items={relatedFiltered} onSelect={openBook} disabled={isOpening} />
              )}
            </section>
          )}
        </>
      )}

      {mode === "category" && activeCategory && (
        <section>
          <h2 className="mb-3 font-display text-xl text-foreground">Explorando "{activeCategory}"</h2>
          {categoryQuery.isLoading && <ResultadosGridSkeleton />}
          {categoryResults.length > 0 && (
            <>
              <ResultadosGrid items={categoryResults} onSelect={openBook} disabled={isOpening} />
              <InfiniteScrollSentinel
                onVisible={() => categoryQuery.fetchNextPage()}
                isFetching={categoryQuery.isFetchingNextPage}
                hasMore={Boolean(categoryQuery.hasNextPage)}
              />
            </>
          )}
        </section>
      )}

      {mode === "idle" && (
        <>
          {recentBooks.length > 0 && (
            <section>
              <h2 className="mb-3 font-display text-xl text-foreground">Adicionados recentemente</h2>
              <div className="grid grid-cols-[repeat(auto-fill,minmax(9rem,1fr))] gap-x-4 gap-y-6">
                {recentBooks.map((book) => (
                  <LivroCard
                    key={book.id}
                    title={book.title}
                    author={book.author}
                    coverUrl={book.coverUrl}
                    genre={book.genre}
                    href={`/biblioteca/livro/${book.id}`}
                  />
                ))}
              </div>
            </section>
          )}

          <section>
            <h2 className="mb-3 font-display text-xl text-foreground">Livros populares</h2>
            {isLoadingPopular ? (
              <ResultadosGridSkeleton />
            ) : (
              <ResultadosGrid items={popular ?? []} onSelect={openBook} disabled={isOpening} />
            )}
          </section>
        </>
      )}

      <section>
        <h2 className="font-display text-xl text-foreground">Escolhidos para a Baldinho</h2>
        <p className="mb-3 text-sm text-muted-foreground">Livros que combinam com as histórias que você já gostou.</p>
        {isLoadingRecommendations ? (
          <ResultadosGridSkeleton />
        ) : recommendations && recommendations.length > 0 ? (
          <ResultadosGrid
            items={recommendations.map((r) => r.book)}
            getReason={(item) => recommendations.find((r) => r.book === item)?.reason}
            onSelect={openBook}
            disabled={isOpening}
          />
        ) : (
          <RecommendationsEmptyState />
        )}
      </section>

      {mode === "idle" && (
        <section>
          <h2 className="mb-3 font-display text-xl text-foreground">Mais livros para explorar</h2>
          {exploreQuery.isLoading && <ResultadosGridSkeleton />}
          {exploreResults.length > 0 && (
            <>
              <ResultadosGrid items={exploreResults} onSelect={openBook} disabled={isOpening} />
              <InfiniteScrollSentinel
                onVisible={() => exploreQuery.fetchNextPage()}
                isFetching={exploreQuery.isFetchingNextPage}
                hasMore={Boolean(exploreQuery.hasNextPage)}
              />
            </>
          )}
        </section>
      )}
    </div>
  );
}
