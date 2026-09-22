import { createFileRoute } from "@tanstack/react-router";

import { Estatisticas } from "@/features/biblioteca/pages/Estatisticas";

export const Route = createFileRoute("/biblioteca/estatisticas")({
  component: Estatisticas,
});
