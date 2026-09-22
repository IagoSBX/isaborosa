import { Search } from "lucide-react";

import { Input } from "@/components/ui/input";

interface BuscaBarraProps {
  value: string;
  onChange: (value: string) => void;
}

export function BuscaBarra({ value, onChange }: BuscaBarraProps) {
  return (
    <div className="relative">
      <Search
        size={16}
        className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground"
      />
      <Input
        type="search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder="Buscar por título ou autor..."
        aria-label="Buscar livros"
        className="pl-9"
      />
    </div>
  );
}
