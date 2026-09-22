import { api } from "@/lib/api";
import type {
  AddToLibraryInput,
  Book,
  BookSearchResult,
  CreateBookInput,
  PriceQuote,
  ReadingStatus,
  Recommendation,
  Stats,
  UpdateLibraryInput,
  UserBook,
} from "../types";

export const libraryApi = {
  searchBooks: (query: string, page = 0) =>
    api.get<BookSearchResult[]>(`/api/books/search?q=${encodeURIComponent(query)}&page=${page}`),

  browseByGenre: (genre: string, page = 0) =>
    api.get<BookSearchResult[]>(`/api/books/browse?genre=${encodeURIComponent(genre)}&page=${page}`),

  getPopular: () => api.get<BookSearchResult[]>("/api/books/popular"),

  getBook: (id: number) => api.get<Book>(`/api/books/${id}`),

  createBook: (input: CreateBookInput) => api.post<Book>("/api/books", input),

  listLibrary: (status?: ReadingStatus) =>
    api.get<UserBook[]>(`/api/library${status ? `?status=${status}` : ""}`),

  addToLibrary: (input: AddToLibraryInput) => api.post<UserBook>("/api/library", input),

  updateLibraryEntry: (userBookId: number, input: UpdateLibraryInput) =>
    api.patch<UserBook>(`/api/library/${userBookId}`, input),

  removeFromLibrary: (userBookId: number) => api.delete<void>(`/api/library/${userBookId}`),

  getRecommendations: () => api.get<Recommendation[]>("/api/recommendations"),

  getPrices: (bookId: number) => api.get<PriceQuote[]>(`/api/books/${bookId}/prices`),

  getStats: () => api.get<Stats>("/api/library/stats"),
};
