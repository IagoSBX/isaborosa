import { createFileRoute } from "@tanstack/react-router";

import { Pesquisar } from "@/features/biblioteca/pages/Pesquisar";

export const Route = createFileRoute("/biblioteca/pesquisar")({
  component: Pesquisar,
});
