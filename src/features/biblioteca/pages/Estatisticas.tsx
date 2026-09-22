import { BookMarked, BookOpenCheck, Library, Star, Tag } from "lucide-react";
import type { ReactNode } from "react";

import { Skeleton } from "@/components/ui/skeleton";
import { GenreDonutChart } from "../components/GenreDonutChart";
import { RatingDistributionChart } from "../components/RatingDistributionChart";
import { ReadingEvolutionChart } from "../components/ReadingEvolutionChart";
import { StatTile } from "../components/StatTile";
import { StatusChart } from "../components/StatusChart";
import { useStats } from "../hooks/useStats";

function EstatisticasSkeleton() {
  return (
    <div className="space-y-8">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-5">
        {Array.from({ length: 5 }).map((_, index) => (
          <Skeleton key={index} className="h-24 rounded-2xl" />
        ))}
      </div>
      <Skeleton className="h-64 rounded-2xl" />
    </div>
  );
}

function ChartCard({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
      <h2 className="mb-3 font-display text-lg text-foreground">{title}</h2>
      {children}
    </div>
  );
}

export function Estatisticas() {
  const { data: stats, isLoading, isError } = useStats();

  if (isLoading) {
    return <EstatisticasSkeleton />;
  }

  if (isError || !stats) {
    return (
      <p className="text-sm text-muted-foreground">
        Não foi possível carregar as estatísticas agora. Tente novamente em instantes.
      </p>
    );
  }

  if (stats.totalBooks === 0) {
    return (
      <div className="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-border bg-gradient-to-br from-card to-rose-wash/20 px-6 py-16 text-center">
        <p className="font-display text-xl text-foreground">Ainda não há dados para mostrar.</p>
        <p className="max-w-sm text-sm text-muted-foreground">
          Adicione e avalie livros na sua biblioteca para ver estatísticas de leitura aqui.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-5">
        <StatTile icon={Library} label="Total de livros" value={String(stats.totalBooks)} accentClassName="bg-secondary text-secondary-foreground" />
        <StatTile
          icon={BookOpenCheck}
          label="Já lidos"
          value={String(stats.booksByStatus.LIDO ?? 0)}
          accentClassName="bg-primary/10 text-primary"
        />
        <StatTile
          icon={BookMarked}
          label="Páginas lidas"
          value={stats.totalPagesRead != null ? stats.totalPagesRead.toLocaleString("pt-BR") : "—"}
          hint={stats.totalPagesRead == null ? "sem dados ainda" : undefined}
          accentClassName="bg-accent text-accent-foreground"
        />
        <StatTile
          icon={Star}
          label="Nota média"
          value={stats.averageRating != null ? stats.averageRating.toFixed(1) : "—"}
          hint={stats.averageRating == null ? "sem avaliações" : "de 5 estrelas"}
          accentClassName="bg-nude text-foreground"
        />
        <StatTile
          icon={Tag}
          label="Gênero favorito"
          value={stats.topGenre ?? "—"}
          accentClassName="bg-rose-wash text-foreground"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <ChartCard title="Status de leitura">
          <StatusChart booksByStatus={stats.booksByStatus} />
        </ChartCard>

        <ChartCard title="Gêneros mais lidos">
          <GenreDonutChart genreDistribution={stats.genreDistribution} />
        </ChartCard>

        <ChartCard title="Evolução da leitura">
          <ReadingEvolutionChart readingEvolution={stats.readingEvolution} />
        </ChartCard>

        <ChartCard title="Distribuição de avaliações">
          <RatingDistributionChart ratingDistribution={stats.ratingDistribution} />
        </ChartCard>
      </div>
    </div>
  );
}
