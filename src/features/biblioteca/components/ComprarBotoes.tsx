import { ExternalLink, ShoppingBag } from "lucide-react";

import { cn } from "@/lib/utils";
import type { PriceQuote } from "../types";

function formatPrice(value: number) {
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function buildSearchQuery(title: string, author?: string | null) {
  return encodeURIComponent(author ? `${title} ${author}` : title);
}

interface StoreButtonProps {
  label: string;
  quote?: PriceQuote | undefined;
  fallbackUrl: string;
  accentClassName: string;
}

function StoreButton({ label, quote, fallbackUrl, accentClassName }: StoreButtonProps) {
  return (
    <a
      href={quote?.url ?? fallbackUrl}
      target="_blank"
      rel="noreferrer noopener"
      className={cn(
        "group relative flex flex-1 items-center justify-between gap-3 overflow-hidden rounded-xl border-2 px-4 py-3 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md",
        accentClassName,
      )}
    >
      <span className="flex items-center gap-2.5">
        <ShoppingBag size={18} className="shrink-0" />
        <span className="flex flex-col">
          <span className="text-sm font-semibold">Comprar na {label}</span>
          <span className="text-xs opacity-80">{quote ? formatPrice(quote.price) : "ver preço"}</span>
        </span>
      </span>
      <ExternalLink size={15} className="shrink-0 opacity-70 transition-transform group-hover:translate-x-0.5" />
    </a>
  );
}

interface ComprarBotoesProps {
  prices: PriceQuote[];
  isLoading: boolean;
  title: string;
  author?: string | null;
}

export function ComprarBotoes({ prices, isLoading, title, author }: ComprarBotoesProps) {
  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Consultando lojas...</p>;
  }

  const query = buildSearchQuery(title, author);
  const mercadoLivre = prices.find((quote) => quote.store === "MERCADO_LIVRE");
  const amazon = prices.find((quote) => quote.store === "AMAZON");

  return (
    <div className="flex flex-col gap-2 sm:flex-row">
      <StoreButton
        label="Amazon"
        quote={amazon}
        fallbackUrl={`https://www.amazon.com.br/s?k=${query}&i=stripbooks`}
        accentClassName="border-nude bg-nude/40 text-foreground hover:bg-nude/60"
      />
      <StoreButton
        label="Mercado Livre"
        quote={mercadoLivre}
        fallbackUrl={`https://lista.mercadolivre.com.br/${query}`}
        accentClassName="border-accent bg-accent/40 text-foreground hover:bg-accent/60"
      />
    </div>
  );
}
