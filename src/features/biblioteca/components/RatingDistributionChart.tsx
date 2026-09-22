import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Star } from "lucide-react";

import type { RatingCount } from "../types";

const BAR_COLOR = "oklch(0.42 0.085 15)";

function StarsTick({ x, y, payload }: { x?: number; y?: number; payload?: { value: number } }) {
  const rating = payload?.value ?? 0;
  return (
    <g transform={`translate(${(x ?? 0) - 10},${(y ?? 0) - 6})`}>
      <text x={0} y={12} fontSize={11} fill="oklch(0.52 0.022 38)">
        {rating}
      </text>
    </g>
  );
}

interface RatingDistributionChartProps {
  ratingDistribution: RatingCount[];
}

export function RatingDistributionChart({ ratingDistribution }: RatingDistributionChartProps) {
  const hasAny = ratingDistribution.some((item) => item.count > 0);
  if (!hasAny) {
    return <p className="text-sm text-muted-foreground">Avalie livros marcados como "já li" para ver esse gráfico.</p>;
  }

  return (
    <ResponsiveContainer width="100%" height={200}>
      <BarChart data={ratingDistribution} layout="vertical" margin={{ top: 0, right: 24, left: 8, bottom: 0 }}>
        <XAxis type="number" hide allowDecimals={false} />
        <YAxis type="category" dataKey="rating" width={24} tickLine={false} axisLine={false} tick={<StarsTick />} />
        <Tooltip
          cursor={{ fill: "oklch(0.945 0.011 58)" }}
          contentStyle={{
            borderRadius: 8,
            borderColor: "oklch(0.86 0.019 45)",
            fontSize: 12,
            fontFamily: "var(--font-sans)",
          }}
          formatter={(value: number, _name, item) => [
            `${value} livro${value === 1 ? "" : "s"}`,
            `${item.payload.rating} estrela${item.payload.rating === 1 ? "" : "s"}`,
          ]}
        />
        <Bar dataKey="count" radius={[0, 6, 6, 0]} maxBarSize={18} label={{ position: "right", fontSize: 12, fill: "oklch(0.52 0.022 38)" }}>
          {ratingDistribution.map((entry) => (
            <Cell key={entry.rating} fillOpacity={0.4 + entry.rating * 0.12} fill={BAR_COLOR} />
          ))}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

export function RatingDistributionLegend() {
  return (
    <div className="flex items-center gap-1 text-muted-foreground">
      <Star size={12} />
      <span className="text-xs">= nota em estrelas</span>
    </div>
  );
}
