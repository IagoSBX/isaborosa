// Precisa bater exatamente com os rotulos em portugues que o backend produz
// em GenreTranslations (EN_TO_PT), pois "/api/books/browse" traduz de volta
// para o termo em ingles para pesquisar por assunto na Open Library.
export const GENRE_CATEGORIES = [
  "Fantasia",
  "Ficção científica",
  "Romance",
  "Mistério",
  "Terror",
  "Suspense",
  "Aventura",
  "Biografia",
  "História",
  "Infantojuvenil",
  "Jovem adulto",
  "Humor",
] as const;
