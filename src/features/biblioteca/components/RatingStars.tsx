import { Star } from "lucide-react";
import { useState } from "react";

import { cn } from "@/lib/utils";

interface RatingStarsProps {
  value: number | null;
  onChange?: (rating: number) => void;
  readOnly?: boolean;
  size?: number;
}

export function RatingStars({ value, onChange, readOnly = false, size }: RatingStarsProps) {
  const stars = [1, 2, 3, 4, 5];
  const [hovered, setHovered] = useState<number | null>(null);

  if (readOnly) {
    return (
      <div className="flex items-center gap-0.5" aria-label={value ? `Nota ${value} de 5` : "Sem nota"}>
        {stars.map((star) => (
          <Star
            key={star}
            size={size ?? 16}
            className={cn(
              "fill-none stroke-muted-foreground",
              value != null && star <= value && "fill-primary stroke-primary",
            )}
          />
        ))}
      </div>
    );
  }

  const display = hovered ?? value;

  return (
    <div
      role="radiogroup"
      aria-label="Sua nota para este livro"
      className="flex items-center gap-0.5"
      onMouseLeave={() => setHovered(null)}
    >
      {stars.map((star) => (
        <button
          key={star}
          type="button"
          role="radio"
          aria-checked={value === star}
          aria-label={`${star} ${star === 1 ? "estrela" : "estrelas"}`}
          onClick={() => onChange?.(star)}
          onMouseEnter={() => setHovered(star)}
          className="cursor-pointer rounded-sm p-0.5 transition-transform hover:scale-125 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
        >
          <Star
            size={size ?? 20}
            className={cn(
              "fill-none stroke-muted-foreground transition-colors",
              display != null && star <= display && "fill-primary stroke-primary",
            )}
          />
        </button>
      ))}
    </div>
  );
}
