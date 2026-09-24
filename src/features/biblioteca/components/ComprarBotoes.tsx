import { ExternalLink, ShoppingBag } from "lucide-react";

import { cn } from "@/lib/utils";
import type { PriceQuote } from "../types";

function formatPrice(value: number) {
  return value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

interface StoreButtonProps {
  label: string;
  quote: PriceQuote;
  accentClassName: string;
}

function StoreButton({ label, quote, accentClassName }: StoreButtonProps) {
  return (
    <a
      href={quote.url}
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
          <span className="text-xs opacity-80">{formatPrice(quote.price)}</span>
        </span>
      </span>
      <ExternalLink size={15} className="shrink-0 opacity-70 transition-transform group-hover:translate-x-0.5" />
    </a>
  );
}

interface ComprarBotoesProps {
  prices: PriceQuote[];
  isLoading: boolean;
}

/**
 * So mostra um botao de loja quando ha um preco real encontrado - nunca um
 * link generico de busca sem informacao confirmada.
 */
export function ComprarBotoes({ prices, isLoading }: ComprarBotoesProps) {
  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Consultando lojas...</p>;
  }

  const mercadoLivre = prices.find((quote) => quote.store === "MERCADO_LIVRE");
  const amazon = prices.find((quote) => quote.store === "AMAZON");

  if (!mercadoLivre && !amazon) {
    return null;
  }

  return (
    <>
      {amazon && (
        <StoreButton label="Amazon" quote={amazon} accentClassName="border-nude bg-nude/40 text-foreground hover:bg-nude/60" />
      )}
      {mercadoLivre && (
        <StoreButton
          label="Mercado Livre"
          quote={mercadoLivre}
          accentClassName="border-accent bg-accent/40 text-foreground hover:bg-accent/60"
        />
      )}
    </>
  );
}
