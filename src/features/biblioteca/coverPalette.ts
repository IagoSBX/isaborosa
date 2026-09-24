/**
 * Gradientes para a capa tipografica (quando o livro nao tem capa real).
 * Um por genero conhecido (taxonomia do backend, GenreTranslations); generos
 * desconhecidos ou ausentes caem num dos gradientes de reserva, escolhido de
 * forma estavel por hash do titulo (mesmo livro sempre cai na mesma cor).
 */
const GENRE_GRADIENTS: Record<string, string> = {
  "Ficção": "from-rose-800 to-red-950",
  Fantasia: "from-violet-700 to-indigo-950",
  "Ficção científica": "from-sky-700 to-blue-950",
  "Mistério": "from-slate-700 to-slate-950",
  Policial: "from-zinc-700 to-neutral-950",
  Romance: "from-pink-700 to-rose-950",
  Terror: "from-neutral-800 to-black",
  Suspense: "from-amber-700 to-orange-950",
  Aventura: "from-emerald-700 to-teal-950",
  Biografia: "from-indigo-700 to-blue-950",
  "Autobiografia": "from-indigo-600 to-indigo-950",
  "História": "from-amber-800 to-yellow-950",
  Poesia: "from-fuchsia-700 to-purple-950",
  Drama: "from-red-800 to-rose-950",
  Infantojuvenil: "from-lime-600 to-green-800",
  "Jovem adulto": "from-purple-700 to-violet-950",
  "Literatura clássica": "from-stone-700 to-stone-950",
  "Ficção histórica": "from-yellow-800 to-amber-950",
  "Ficção de guerra": "from-gray-700 to-gray-950",
  Humor: "from-yellow-500 to-amber-700",
  "Autoajuda": "from-teal-600 to-cyan-900",
  Filosofia: "from-slate-600 to-gray-900",
  Psicologia: "from-cyan-700 to-teal-950",
  "Graphic novel": "from-orange-700 to-red-950",
  Quadrinhos: "from-orange-600 to-red-900",
  Contos: "from-teal-700 to-emerald-950",
};

const FALLBACK_GRADIENTS = [
  "from-rose-800 to-red-950",
  "from-violet-700 to-indigo-950",
  "from-slate-700 to-slate-950",
  "from-emerald-700 to-teal-950",
  "from-amber-700 to-orange-950",
  "from-indigo-700 to-blue-950",
  "from-stone-700 to-stone-950",
  "from-pink-700 to-rose-950",
];

function hashString(text: string): number {
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    hash = (hash << 5) - hash + text.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash);
}

export function coverGradientFor(title: string, genre?: string | null): string {
  if (genre && GENRE_GRADIENTS[genre]) {
    return GENRE_GRADIENTS[genre];
  }
  return FALLBACK_GRADIENTS[hashString(title) % FALLBACK_GRADIENTS.length]!;
}
