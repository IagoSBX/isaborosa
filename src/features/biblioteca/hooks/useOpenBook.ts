import { useNavigate } from "@tanstack/react-router";

import { useCreateBook } from "./useBook";
import type { BookSearchResult } from "../types";

/**
 * Fluxo comum a pesquisa/recomendacoes/relacionados: ao clicar num resultado
 * que ainda nao esta salvo localmente, cria (upsert) o livro e navega para a
 * pagina de detalhes, onde o usuario decide o status pelo modal.
 */
export function useOpenBook() {
  const createBook = useCreateBook();
  const navigate = useNavigate();

  async function openBook(result: BookSearchResult) {
    const book = await createBook.mutateAsync({
      title: result.title,
      author: result.author,
      isbn: result.isbn,
      coverUrl: result.coverUrl,
      publishYear: result.publishYear,
      publisher: result.publisher,
      pageCount: result.pageCount,
      genre: result.genre,
      openLibraryKey: result.openLibraryKey,
    });
    navigate({ to: `/biblioteca/livro/${book.id}` });
  }

  return { openBook, isPending: createBook.isPending };
}
