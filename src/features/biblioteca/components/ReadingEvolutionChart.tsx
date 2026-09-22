import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import type { MonthCount } from "../types";

const MONTH_LABELS = [
  "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez",
];

function formatMonth(month: string) {
  const [, monthNumber] = month.split("-");
  const index = Number(monthNumber) - 1;
  return MONTH_LABELS[index] ?? month;
}

interface ReadingEvolutionChartProps {
  readingEvolution: MonthCount[];
}

export function ReadingEvolutionChart({ readingEvolution }: ReadingEvolutionChartProps) {
  if (readingEvolution.length < 2) {
    return (
      <p className="text-sm text-muted-foreground">
        Continue marcando livros como "já li" para ver sua evolução mês a mês aqui.
      </p>
    );
  }

  const data = readingEvolution.map((item) => ({ ...item, label: formatMonth(item.month) }));

  return (
    <ResponsiveContainer width="100%" height={220}>
      <AreaChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="readingEvolutionFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="oklch(0.42 0.085 15)" stopOpacity={0.35} />
            <stop offset="100%" stopColor="oklch(0.42 0.085 15)" stopOpacity={0.02} />
          </linearGradient>
        </defs>
        <CartesianGrid vertical={false} stroke="oklch(0.92 0.012 55)" />
        <XAxis
          dataKey="label"
          tickLine={false}
          axisLine={{ stroke: "oklch(0.86 0.019 45)" }}
          tick={{ fill: "oklch(0.52 0.022 38)", fontSize: 12 }}
        />
        <YAxis hide allowDecimals={false} />
        <Tooltip
          cursor={{ stroke: "oklch(0.86 0.019 45)" }}
          contentStyle={{
            borderRadius: 8,
            borderColor: "oklch(0.86 0.019 45)",
            fontSize: 12,
            fontFamily: "var(--font-sans)",
          }}
          formatter={(value: number) => [`${value} livro${value === 1 ? "" : "s"}`, "Lidos"]}
        />
        <Area
          type="monotone"
          dataKey="count"
          stroke="oklch(0.42 0.085 15)"
          strokeWidth={2}
          fill="url(#readingEvolutionFill)"
          dot={{ r: 3, fill: "oklch(0.42 0.085 15)" }}
          activeDot={{ r: 5 }}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
}
