import { Link } from "@tanstack/react-router";
import { BookOpen } from "lucide-react";
import type { ReactNode } from "react";

import { cn } from "@/lib/utils";
import { STATUS_META } from "../statusMeta";
import type { ReadingStatus } from "../types";
import { RatingStars } from "./RatingStars";

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

function CoverImage({
  title,
  coverUrl,
  genre,
  status,
  favorite,
}: {
  title: string;
  coverUrl?: string | null | undefined;
  genre?: string | null | undefined;
  status?: ReadingStatus | undefined;
  favorite?: boolean | undefined;
}) {
  const statusMeta = status ? STATUS_META[status] : undefined;
  const StatusIcon = statusMeta?.icon;

  return (
    <div className="relative aspect-[2/3] w-full overflow-hidden rounded-xl border border-border bg-gradient-to-br from-muted to-nude/50 shadow-sm transition-shadow duration-300 group-hover:shadow-lg">
      {coverUrl ? (
        <img
          src={coverUrl}
          alt={`Capa de ${title}`}
          loading="lazy"
          width={200}
          height={300}
          className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-[1.03]"
        />
      ) : (
        <div className="flex h-full w-full items-center justify-center text-muted-foreground">
          <BookOpen size={28} strokeWidth={1.5} />
        </div>
      )}

      {genre && (
        <span className="absolute left-1.5 top-1.5 rounded-full bg-background/90 px-2 py-0.5 text-[10px] font-medium text-foreground shadow-sm backdrop-blur-sm">
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
  const content = (
    <>
      <CoverImage title={title} coverUrl={coverUrl} genre={genre} status={status} favorite={favorite} />
      <div className="mt-2 space-y-0.5">
        <p className="line-clamp-2 font-display text-sm leading-snug text-foreground transition-colors group-hover:text-primary">
          {title}
        </p>
        {author && <p className="line-clamp-1 text-xs text-muted-foreground">{author}</p>}
        {rating != null && (
          <div className="pt-0.5">
            <RatingStars value={rating} readOnly />
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
        className="group block rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
      >
        {content}
      </Link>
    );
  }

  return <div className="group">{content}</div>;
}
