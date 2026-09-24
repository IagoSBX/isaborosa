import { BookOpen } from "lucide-react";

import { cn } from "@/lib/utils";
import { GENRE_META } from "../genreCategories";

interface GenreCardProps {
  genre: string;
  active: boolean;
  onClick: () => void;
}

export function GenreCard({ genre, active, onClick }: GenreCardProps) {
  const meta = GENRE_META[genre as keyof typeof GENRE_META];
  const Icon = meta?.icon ?? BookOpen;

  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "group flex min-w-[7.5rem] shrink-0 flex-col items-start gap-2.5 rounded-2xl border bg-gradient-to-br px-4 py-3.5 text-left shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md",
        meta?.gradient ?? "from-muted to-muted/50",
        active ? "border-primary ring-2 ring-primary/30" : "border-border",
      )}
    >
      <span
        className={cn(
          "flex h-9 w-9 items-center justify-center rounded-full bg-background/80 shadow-sm transition-colors",
          active && "bg-primary text-primary-foreground",
        )}
      >
        <Icon size={17} />
      </span>
      <span className="text-sm font-semibold text-foreground">{genre}</span>
    </button>
  );
}
