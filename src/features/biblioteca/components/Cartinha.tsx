import { Mail, X } from "lucide-react";
import { useState } from "react";
import { createPortal } from "react-dom";

import { cn } from "@/lib/utils";

export function Cartinha() {
  const [open, setOpen] = useState(false);
  const [opened, setOpened] = useState(false);

  function handleOpen() {
    setOpen(true);
    // pequeno atraso para a animacao do envelope abrindo antes do papel aparecer
    window.setTimeout(() => setOpened(true), 250);
  }

  function handleClose() {
    setOpen(false);
    setOpened(false);
  }

  return (
    <>
      <button
        type="button"
        onClick={handleOpen}
        title="Feito especialmente para você"
        className="inline-flex items-center gap-1.5 text-xs text-muted-foreground transition-colors hover:text-primary"
      >
        <Mail size={14} />
        <span className="hidden sm:inline">Uma cartinha para você</span>
      </button>

      {open &&
        createPortal(
          <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-wine/40 p-4 backdrop-blur-sm"
            onClick={handleClose}
          >
            <div
              onClick={(e) => e.stopPropagation()}
              className={cn(
                "relative w-full max-w-md rounded-lg border border-border bg-gradient-to-br from-paper to-nude/30 p-8 shadow-2xl transition-all duration-500",
                opened ? "scale-100 opacity-100" : "scale-90 opacity-0",
              )}
            >
              <button
                type="button"
                onClick={handleClose}
                className="absolute right-3 top-3 flex h-7 w-7 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                aria-label="Fechar carta"
              >
                <X size={15} />
              </button>

              <div className="space-y-4 font-hand text-lg leading-relaxed text-foreground sm:text-xl">
                <p>Para Isaborosa</p>
                <p>
                  Fiz a biblioteca para você, porque sei o quanto ler é bom pra você minha gordinha, então mantenha
                  organizado!
                </p>
                <p>Te amo muito!!</p>
                <p className="pt-2">Iago</p>
              </div>
            </div>
          </div>,
          document.body,
        )}
    </>
  );
}
