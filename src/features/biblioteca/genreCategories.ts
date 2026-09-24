import {
  Baby,
  BookUser,
  Compass,
  Ghost,
  Heart,
  Landmark,
  type LucideIcon,
  Rocket,
  Search,
  Smile,
  Users,
  Wand2,
  Zap,
} from "lucide-react";

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

export interface GenreMeta {
  icon: LucideIcon;
  gradient: string;
}

export const GENRE_META: Record<(typeof GENRE_CATEGORIES)[number], GenreMeta> = {
  Fantasia: { icon: Wand2, gradient: "from-violet-500/15 to-fuchsia-500/10" },
  "Ficção científica": { icon: Rocket, gradient: "from-sky-500/15 to-cyan-500/10" },
  Romance: { icon: Heart, gradient: "from-rose-500/15 to-pink-500/10" },
  Mistério: { icon: Search, gradient: "from-slate-500/15 to-zinc-500/10" },
  Terror: { icon: Ghost, gradient: "from-neutral-700/15 to-neutral-500/10" },
  Suspense: { icon: Zap, gradient: "from-amber-500/15 to-orange-500/10" },
  Aventura: { icon: Compass, gradient: "from-emerald-500/15 to-teal-500/10" },
  Biografia: { icon: BookUser, gradient: "from-indigo-500/15 to-blue-500/10" },
  História: { icon: Landmark, gradient: "from-yellow-600/15 to-amber-600/10" },
  Infantojuvenil: { icon: Baby, gradient: "from-lime-500/15 to-green-500/10" },
  "Jovem adulto": { icon: Users, gradient: "from-purple-500/15 to-violet-500/10" },
  Humor: { icon: Smile, gradient: "from-yellow-400/15 to-amber-400/10" },
};
