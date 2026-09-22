import { Bookmark, BookOpen, CheckCircle2, type LucideIcon } from "lucide-react";

import { READING_STATUS_LABELS, type ReadingStatus } from "./types";

export interface StatusMeta {
  label: string;
  icon: LucideIcon;
  badgeClassName: string;
  ringClassName: string;
  description: string;
}

export const STATUS_ORDER: ReadingStatus[] = ["QUERO_LER", "LENDO", "LIDO"];

export const STATUS_META: Record<ReadingStatus, StatusMeta> = {
  QUERO_LER: {
    label: READING_STATUS_LABELS.QUERO_LER,
    icon: Bookmark,
    badgeClassName: "bg-nude text-foreground",
    ringClassName: "border-nude ring-nude/40",
    description: "Guardado para uma próxima leitura",
  },
  LENDO: {
    label: READING_STATUS_LABELS.LENDO,
    icon: BookOpen,
    badgeClassName: "bg-accent text-accent-foreground",
    ringClassName: "border-accent ring-accent/40",
    description: "Em andamento agora",
  },
  LIDO: {
    label: READING_STATUS_LABELS.LIDO,
    icon: CheckCircle2,
    badgeClassName: "bg-primary text-primary-foreground",
    ringClassName: "border-primary ring-primary/40",
    description: "Concluído — avalie de 1 a 5 estrelas",
  },
};
