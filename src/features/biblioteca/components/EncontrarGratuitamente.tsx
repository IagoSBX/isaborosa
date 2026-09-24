import { BookOpenCheck, ExternalLink } from "lucide-react";

import type { FreeSource } from "../types";

const STORE_LABELS: Record<string, string> = {
  PROJECT_GUTENBERG: "Ler gratuitamente",
  INTERNET_ARCHIVE: "Ler no Internet Archive",
};

interface EncontrarGratuitamenteProps {
  sources: FreeSource[];
  isLoading: boolean;
}

export function EncontrarGratuitamente({ sources, isLoading }: EncontrarGratuitamenteProps) {
  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Verificando fontes gratuitas...</p>;
  }

  if (sources.length === 0) {
    return null;
  }

  return (
    <>
      {sources.map((source) => (
        <a
          key={source.store}
          href={source.url}
          target="_blank"
          rel="noreferrer noopener"
          className="group relative flex flex-1 items-center justify-between gap-3 overflow-hidden rounded-xl border-2 border-emerald-600/30 bg-emerald-600/10 px-4 py-3 text-emerald-900 shadow-sm transition-all hover:-translate-y-0.5 hover:bg-emerald-600/15 hover:shadow-md"
        >
          <span className="flex items-center gap-2.5">
            <BookOpenCheck size={18} className="shrink-0" />
            <span className="flex flex-col">
              <span className="text-sm font-semibold">{STORE_LABELS[source.store] ?? "Ler gratuitamente"}</span>
              <span className="text-xs opacity-80">via {source.label}</span>
            </span>
          </span>
          <ExternalLink size={15} className="shrink-0 opacity-70 transition-transform group-hover:translate-x-0.5" />
        </a>
      ))}
    </>
  );
}
