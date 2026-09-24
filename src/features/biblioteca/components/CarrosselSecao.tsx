import type { ReactNode } from "react";

interface CarrosselSecaoProps {
  title: string;
  children: ReactNode;
}

export function CarrosselSecao({ title, children }: CarrosselSecaoProps) {
  return (
    <section>
      <h2 className="mb-3 font-display text-lg text-foreground">{title}</h2>
      <div className="flex snap-x gap-4 overflow-x-auto pb-2">{children}</div>
    </section>
  );
}
