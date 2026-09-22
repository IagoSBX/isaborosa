import { createFileRoute } from "@tanstack/react-router";

import { DetalhesLivro } from "@/features/biblioteca/pages/DetalhesLivro";

export const Route = createFileRoute("/biblioteca/livro/$id")({
  component: () => {
    const { id } = Route.useParams();
    return <DetalhesLivro bookId={Number(id)} />;
  },
});
