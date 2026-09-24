import { createFileRoute } from "@tanstack/react-router";

import { Login } from "@/features/auth/pages/Login";

export const Route = createFileRoute("/biblioteca/login")({
  component: Login,
});
