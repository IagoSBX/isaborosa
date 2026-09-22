import { Heart } from "lucide-react";
import { type ReactNode, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { cn } from "@/lib/utils";
import { STATUS_META, STATUS_ORDER } from "../statusMeta";
import type { ReadingStatus } from "../types";
import { RatingStars } from "./RatingStars";

interface AdicionarBibliotecaModalProps {
  trigger: ReactNode;
  bookTitle: string;
  initialStatus?: ReadingStatus;
  initialRating?: number | null;
  initialFavorite?: boolean;
  isPending: boolean;
  confirmLabel?: string;
  onConfirm: (status: ReadingStatus, rating: number | null, favorite: boolean) => void;
}

export function AdicionarBibliotecaModal({
  trigger,
  bookTitle,
  initialStatus,
  initialRating = null,
  initialFavorite = false,
  isPending,
  confirmLabel = "Confirmar e adicionar",
  onConfirm,
}: AdicionarBibliotecaModalProps) {
  const [open, setOpen] = useState(false);
  const [status, setStatus] = useState<ReadingStatus>(initialStatus ?? "QUERO_LER");
  const [rating, setRating] = useState<number | null>(initialRating);
  const [favorite, setFavorite] = useState(initialFavorite);

  const requiresRating = status === "LIDO";
  const canConfirm = !requiresRating || rating != null;

  function reset(next: boolean) {
    if (next) {
      setStatus(initialStatus ?? "QUERO_LER");
      setRating(initialRating);
      setFavorite(initialFavorite);
    }
    setOpen(next);
  }

  function handleConfirm() {
    if (!canConfirm) return;
    onConfirm(status, requiresRating ? rating : null, favorite);
    setOpen(false);
  }

  return (
    <Dialog open={open} onOpenChange={reset}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="font-display text-2xl">Adicionar à minha biblioteca</DialogTitle>
          <DialogDescription>
            Qual é o status deste livro? <span className="font-medium text-foreground">"{bookTitle}"</span>
          </DialogDescription>
        </DialogHeader>

        <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
          {STATUS_ORDER.map((option) => {
            const meta = STATUS_META[option];
            const Icon = meta.icon;
            const selected = status === option;
            return (
              <button
                key={option}
                type="button"
                onClick={() => setStatus(option)}
                className={cn(
                  "flex flex-col items-center gap-1.5 rounded-xl border-2 px-3 py-3 text-center text-sm transition-all",
                  selected
                    ? cn("shadow-sm", meta.ringClassName, meta.badgeClassName)
                    : "border-border bg-card text-foreground hover:border-muted-foreground/30 hover:bg-accent/40",
                )}
              >
                <Icon size={20} />
                <span className="font-medium">{meta.label}</span>
              </button>
            );
          })}
        </div>

        {requiresRating && (
          <div className="rounded-xl border-2 border-primary/30 bg-primary/5 p-4 text-center animate-in fade-in slide-in-from-top-1">
            <p className="mb-2 text-sm font-medium text-foreground">Como você avalia este livro?</p>
            <div className="flex justify-center">
              <RatingStars value={rating} onChange={setRating} size={28} />
            </div>
            {rating == null && (
              <p className="mt-2 text-xs text-muted-foreground">Escolha de 1 a 5 estrelas para continuar.</p>
            )}
          </div>
        )}

        <button
          type="button"
          onClick={() => setFavorite((value) => !value)}
          className={cn(
            "flex items-center gap-2 rounded-xl border-2 px-3 py-2.5 text-sm font-medium transition-all",
            favorite
              ? "border-rose-wash bg-rose-wash text-foreground shadow-sm"
              : "border-border bg-card text-muted-foreground hover:border-muted-foreground/30 hover:bg-accent/40",
          )}
        >
          <Heart size={18} className={cn("shrink-0", favorite && "fill-primary stroke-primary")} />
          Marcar como favorito
        </button>

        <DialogFooter>
          <Button size="lg" className="w-full sm:w-auto" onClick={handleConfirm} disabled={isPending || !canConfirm}>
            {confirmLabel}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
