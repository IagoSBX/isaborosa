import { createFileRoute } from "@tanstack/react-router";

import { ChangePassword } from "@/features/auth/pages/ChangePassword";

export const Route = createFileRoute("/biblioteca/trocar-senha")({
  component: ChangePassword,
});
