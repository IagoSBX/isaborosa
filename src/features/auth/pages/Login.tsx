import { useNavigate } from "@tanstack/react-router";
import { LibraryBig, Lock, User } from "lucide-react";
import { type FormEvent, useState } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api";
import { useLogin } from "../hooks/useAuthMutations";

export function Login() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const login = useLogin();
  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const session = await login.mutateAsync({ username, password });
      navigate({ to: session.mustChangePassword ? "/biblioteca/trocar-senha" : "/biblioteca" });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Não foi possível entrar. Tente novamente.");
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-paper via-rose-wash/30 to-nude/20 px-4">
      <div className="w-full max-w-sm rounded-2xl border border-border bg-card p-8 shadow-lg">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
            <LibraryBig size={22} />
          </span>
          <h1 className="font-display text-2xl text-foreground">Biblioteca da Baldinho</h1>
          <p className="mt-1 text-sm text-muted-foreground">Bem-vinda ao seu cantinho de histórias.</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="relative">
            <User size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              autoFocus
              placeholder="Usuário"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="pl-9"
              autoComplete="username"
            />
          </div>
          <div className="relative">
            <Lock size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="password"
              placeholder="Senha"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="pl-9"
              autoComplete="current-password"
            />
          </div>

          {error && (
            <p className="rounded-lg bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>
          )}

          <Button type="submit" size="lg" className="w-full shadow-sm" disabled={login.isPending || !username || !password}>
            {login.isPending ? "Entrando..." : "Entrar"}
          </Button>
        </form>
      </div>
    </div>
  );
}
