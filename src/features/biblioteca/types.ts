export type ReadingStatus = "QUERO_LER" | "LENDO" | "LIDO";

export interface BookSearchResult {
  title: string;
  author: string | null;
  isbn: string | null;
  coverUrl: string | null;
  publishYear: number | null;
  publisher: string | null;
  pageCount: number | null;
  genre: string | null;
  openLibraryKey: string | null;
}

export interface Book {
  id: number;
  title: string;
  author: string | null;
  isbn: string | null;
  coverUrl: string | null;
  publishYear: number | null;
  publisher: string | null;
  pageCount: number | null;
  genre: string | null;
  description: string | null;
  openLibraryKey: string | null;
}

export interface UserBook {
  id: number;
  book: Book;
  status: ReadingStatus;
  rating: number | null;
  favorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBookInput {
  title: string;
  author?: string | null;
  isbn?: string | null;
  coverUrl?: string | null;
  publishYear?: number | null;
  publisher?: string | null;
  pageCount?: number | null;
  genre?: string | null;
  description?: string | null;
  openLibraryKey?: string | null;
}

export interface AddToLibraryInput {
  bookId: number;
  status: ReadingStatus;
  rating?: number | null;
  favorite?: boolean;
}

export interface UpdateLibraryInput {
  status?: ReadingStatus;
  rating?: number | null;
  favorite?: boolean;
}

export interface Recommendation {
  book: BookSearchResult;
  reason: string;
}

export interface PriceQuote {
  store: string;
  price: number;
  url: string;
  checkedAt: string;
}

export interface GenreCount {
  genre: string;
  count: number;
}

export interface RatingCount {
  rating: number;
  count: number;
}

export interface MonthCount {
  month: string;
  count: number;
}

export interface Stats {
  totalBooks: number;
  booksByStatus: Record<ReadingStatus, number>;
  totalPagesRead: number | null;
  averageRating: number | null;
  topGenre: string | null;
  genreDistribution: GenreCount[];
  ratingDistribution: RatingCount[];
  readingEvolution: MonthCount[];
}

export const READING_STATUS_LABELS: Record<ReadingStatus, string> = {
  QUERO_LER: "Quero ler",
  LENDO: "Estou lendo",
  LIDO: "Já li",
};
