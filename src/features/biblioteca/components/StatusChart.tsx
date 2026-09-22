import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

import { STATUS_ORDER } from "../statusMeta";
import { READING_STATUS_LABELS, type ReadingStatus, type Stats } from "../types";

// Mesmas cores do design system do site (src/styles.css), para bater com os
// badges de status usados no restante da Biblioteca (ver statusMeta.ts).
const STATUS_COLORS: Record<ReadingStatus, string> = {
  QUERO_LER: "oklch(0.88 0.034 51)", // --nude
  LENDO: "oklch(0.897 0.04 24)", // --accent
  LIDO: "oklch(0.42 0.085 15)", // --primary
};

interface StatusChartProps {
  booksByStatus: Stats["booksByStatus"];
}

export function StatusChart({ booksByStatus }: StatusChartProps) {
  const data = STATUS_ORDER.map((status) => ({
    status,
    label: READING_STATUS_LABELS[status],
    count: booksByStatus[status] ?? 0,
  }));

  return (
    <ResponsiveContainer width="100%" height={220}>
      <BarChart data={data} margin={{ top: 16, right: 8, left: 0, bottom: 0 }}>
        <XAxis
          dataKey="label"
          tickLine={false}
          axisLine={{ stroke: "oklch(0.86 0.019 45)" }}
          tick={{ fill: "oklch(0.52 0.022 38)", fontSize: 12 }}
        />
        <YAxis hide allowDecimals={false} />
        <Tooltip
          cursor={{ fill: "oklch(0.945 0.011 58)" }}
          contentStyle={{
            borderRadius: 8,
            borderColor: "oklch(0.86 0.019 45)",
            fontSize: 12,
            fontFamily: "var(--font-sans)",
          }}
          formatter={(value: number) => [`${value} livro${value === 1 ? "" : "s"}`, ""]}
          labelFormatter={() => ""}
        />
        <Bar dataKey="count" radius={[6, 6, 0, 0]} maxBarSize={64} label={{ position: "top", fontSize: 12, fill: "oklch(0.29 0.018 35)" }}>
          {data.map((entry) => (
            <Cell key={entry.status} fill={STATUS_COLORS[entry.status]} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}
