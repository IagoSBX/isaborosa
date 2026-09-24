import { cn } from "@/lib/utils";
import { coverGradientFor } from "../coverPalette";

interface TypographicCoverProps {
  title: string;
  author?: string | null | undefined;
  genre?: string | null | undefined;
}

/**
 * Capa gerada quando o livro nao tem imagem real: titulo + autor sobre um
 * gradiente que varia por genero (ou por hash do titulo, quando o genero e
 * desconhecido) - assim cada capa fica visualmente distinta em vez de repetir
 * o mesmo placeholder generico.
 */
export function TypographicCover({ title, author, genre }: TypographicCoverProps) {
  const gradient = coverGradientFor(title, genre);

  return (
    <div className={cn("flex h-full w-full flex-col justify-between bg-gradient-to-br p-3.5", gradient)}>
      <div className="h-0.5 w-7 rounded-full bg-white/40" />
      <p className="line-clamp-5 font-display text-base leading-tight text-white/95 [text-wrap:balance]">{title}</p>
      <div className="space-y-1.5">
        <div className="h-px w-full bg-white/25" />
        {author && (
          <p className="line-clamp-1 text-[10px] font-medium uppercase tracking-[0.15em] text-white/70">{author}</p>
        )}
      </div>
    </div>
  );
}
