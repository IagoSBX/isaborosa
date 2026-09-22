import { createFileRoute } from "@tanstack/react-router";

import { MinhaBiblioteca } from "@/features/biblioteca/pages/MinhaBiblioteca";

export const Route = createFileRoute("/biblioteca/")({
  component: MinhaBiblioteca,
});
