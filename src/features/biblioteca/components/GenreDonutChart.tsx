import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

import type { GenreCount } from "../types";

const MAX_SLICES = 6;

// Sequencia fixa de cores dentro da paleta do site (tons de vinho/terracota/
// bege), da mais escura pra mais clara, na ordem de atribuicao das fatias.
const SLICE_COLORS = [
  "oklch(0.42 0.085 15)",
  "oklch(0.56 0.08 18)",
  "oklch(0.897 0.04 24)",
  "oklch(0.72 0.06 35)",
  "oklch(0.88 0.034 51)",
  "oklch(0.945 0.011 58)",
];

function capToTopGenres(genres: GenreCount[]): GenreCount[] {
  if (genres.length <= MAX_SLICES) {
    return genres;
  }
  const top = genres.slice(0, MAX_SLICES - 1);
  const othersCount = genres.slice(MAX_SLICES - 1).reduce((sum, g) => sum + g.count, 0);
  return [...top, { genre: "Outros", count: othersCount }];
}

interface GenreDonutChartProps {
  genreDistribution: GenreCount[];
}

export function GenreDonutChart({ genreDistribution }: GenreDonutChartProps) {
  if (genreDistribution.length === 0) {
    return (
      <p className="text-sm text-muted-foreground">
        Sem dados de gênero ainda — adicione mais livros para ver essa distribuição.
      </p>
    );
  }

  const data = capToTopGenres(genreDistribution);
  const total = data.reduce((sum, g) => sum + g.count, 0);

  return (
    <ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie
          data={data}
          dataKey="count"
          nameKey="genre"
          innerRadius="55%"
          outerRadius="85%"
          paddingAngle={2}
          cornerRadius={4}
          stroke="oklch(0.99 0.004 65)"
          strokeWidth={2}
        >
          {data.map((entry, index) => (
            <Cell key={entry.genre} fill={SLICE_COLORS[index % SLICE_COLORS.length]} />
          ))}
        </Pie>
        <Tooltip
          contentStyle={{
            borderRadius: 8,
            borderColor: "oklch(0.86 0.019 45)",
            fontSize: 12,
            fontFamily: "var(--font-sans)",
          }}
          formatter={(value: number, name: string) => [
            `${value} livro${value === 1 ? "" : "s"} (${Math.round((value / total) * 100)}%)`,
            name,
          ]}
        />
        <Legend
          layout="vertical"
          verticalAlign="middle"
          align="right"
          iconType="circle"
          iconSize={8}
          formatter={(value) => <span className="text-xs text-foreground">{value}</span>}
        />
      </PieChart>
    </ResponsiveContainer>
  );
}
