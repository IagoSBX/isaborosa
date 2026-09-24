import { Link } from "@tanstack/react-router";
import { useState } from "react";
import type { ReactNode } from "react";

import { cn } from "@/lib/utils";
import { STATUS_META } from "../statusMeta";
import type { ReadingStatus } from "../types";
import { RatingStars } from "./RatingStars";
import { TypographicCover } from "./TypographicCover";

interface LivroCardProps {
  title: string;
  author?: string | null;
  coverUrl?: string | null;
  genre?: string | null;
  rating?: number | null;
  status?: ReadingStatus;
  favorite?: boolean;
  href?: string;
  footer?: ReactNode;
}

/**
 * Titulos vindos de fontes externas as vezes trazem a serie entre parenteses
 * no final (ex.: "After (After Series, Book 1)") - separamos isso do titulo
 * principal para exibir como um dado secundario, nao misturado ao titulo.
 */
/**
 * Pequena inclinacao deterministica por titulo, simulando livros apoiados
 * numa estante fisica (nunca aleatoria de verdade, senao mudaria a cada
 * re-render). Endireita no hover, como se a pessoa estivesse pegando o livro.
 */
function tiltFor(title: string): number {
  let hash = 0;
  for (let i = 0; i < title.length; i++) {
    hash = (hash << 5) - hash + title.charCodeAt(i);
    hash |= 0;
  }
  return ((Math.abs(hash) % 30) - 15) / 10;
}

function splitSeriesFromTitle(title: string): { main: string; series: string | null } {
  const match = title.match(/^(.*)\s\(([^)]+)\)\s*$/);
  if (!match || !match[1] || !match[2]) {
    return { main: title, series: null };
  }
  return { main: match[1].trim(), series: match[2].trim() };
}

function CoverImage({
  title,
  author,
  coverUrl,
  genre,
  status,
  favorite,
}: {
  title: string;
  author?: string | null | undefined;
  coverUrl?: string | null | undefined;
  genre?: string | null | undefined;
  status?: ReadingStatus | undefined;
  favorite?: boolean | undefined;
}) {
  const statusMeta = status ? STATUS_META[status] : undefined;
  const StatusIcon = statusMeta?.icon;
  const [imageFailed, setImageFailed] = useState(false);

  return (
    <div
      className={cn(
        "relative aspect-[2/3] w-full overflow-hidden rounded-l-md rounded-r-xl border border-border shadow-[inset_3px_0_6px_-2px_rgba(0,0,0,0.35)]",
        "shadow-md transition-all duration-300 group-hover:-translate-y-1 group-hover:rotate-[0.6deg] group-hover:shadow-xl",
      )}
    >
      {coverUrl && !imageFailed ? (
        <img
          src={coverUrl}
          alt={`Capa de ${title}`}
          loading="lazy"
          onError={() => setImageFailed(true)}
          width={200}
          height={300}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.03]"
        />
      ) : (
        <TypographicCover title={title} author={author} genre={genre} />
      )}

      {genre && (
        <span className="absolute left-1.5 top-1.5 rounded-full bg-white/70 px-2 py-0.5 text-[9px] font-medium text-foreground shadow-sm backdrop-blur-md">
          {genre}
        </span>
      )}

      {statusMeta && StatusIcon && (
        <span
          className={cn(
            "absolute right-1.5 top-1.5 flex h-6 w-6 items-center justify-center rounded-full shadow-sm",
            statusMeta.badgeClassName,
          )}
          title={statusMeta.label}
        >
          <StatusIcon size={13} />
        </span>
      )}

      {favorite && (
        <span className="absolute bottom-1.5 right-1.5 flex h-6 w-6 items-center justify-center rounded-full bg-background/90 text-primary shadow-sm backdrop-blur-sm">
          ♥
        </span>
      )}

      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/25 via-transparent to-transparent opacity-0 transition-opacity duration-300 group-hover:opacity-100" />
    </div>
  );
}

export function LivroCard({ title, author, coverUrl, genre, rating, status, favorite, href, footer }: LivroCardProps) {
  const { main, series } = splitSeriesFromTitle(title);
  const tilt = tiltFor(title);

  const content = (
    <>
      <CoverImage title={title} author={author} coverUrl={coverUrl} genre={genre} status={status} favorite={favorite} />
      <div className="mt-2 space-y-0.5">
        <div className="min-h-[2.6rem]">
          <p className="line-clamp-2 font-display text-sm leading-snug text-foreground transition-colors group-hover:text-primary">
            {main}
          </p>
          {series && <p className="mt-0.5 line-clamp-1 text-[10px] text-muted-foreground">{series}</p>}
        </div>
        {author && <p className="line-clamp-1 text-xs text-muted-foreground">{author}</p>}
        {rating != null && (
          <div className="pt-0.5">
            <RatingStars value={rating} readOnly size={14} emptyVariant="muted" />
          </div>
        )}
      </div>
      {footer}
    </>
  );

  if (href) {
    return (
      <Link
        to={href}
        style={{ transform: `rotate(${tilt}deg)` }}
        className="group block rounded-xl transition-transform duration-300 hover:rotate-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      >
        {content}
      </Link>
    );
  }

  return (
    <div style={{ transform: `rotate(${tilt}deg)` }} className="group transition-transform duration-300 hover:rotate-0">
      {content}
    </div>
  );
}
