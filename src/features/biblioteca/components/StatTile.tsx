import type { LucideIcon } from "lucide-react";

import { cn } from "@/lib/utils";

interface StatTileProps {
  label: string;
  value: string;
  hint?: string | undefined;
  icon: LucideIcon;
  accentClassName?: string;
}

export function StatTile({ label, value, hint, icon: Icon, accentClassName = "bg-primary/10 text-primary" }: StatTileProps) {
  return (
    <div className="group rounded-2xl border border-border bg-card px-4 py-4 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md">
      <div className={`mb-2 inline-flex h-8 w-8 items-center justify-center rounded-full ${accentClassName}`}>
        <Icon size={16} />
      </div>
      <p className="text-xs text-muted-foreground">{label}</p>
      <p
        className={cn(
          "mt-0.5 font-display text-foreground",
          value.length > 10 ? "text-lg leading-tight sm:text-xl" : "text-2xl sm:text-3xl",
        )}
      >
        {value}
      </p>
      {hint && <p className="mt-0.5 text-xs text-muted-foreground">{hint}</p>}
    </div>
  );
}
