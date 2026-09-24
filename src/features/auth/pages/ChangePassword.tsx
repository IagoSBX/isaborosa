import { useNavigate } from "@tanstack/react-router";
import { KeyRound, ShieldCheck } from "lucide-react";
import { type FormEvent, useState } from "react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api";
import { useChangePassword } from "../hooks/useAuthMutations";

export function ChangePassword() {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const changePassword = useChangePassword();
  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);

    if (newPassword.length < 8) {
      setError("A nova senha precisa ter pelo menos 8 caracteres.");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("As senhas não coincidem.");
      return;
    }

    try {
      await changePassword.mutateAsync({ currentPassword, newPassword });
      navigate({ to: "/biblioteca" });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Não foi possível trocar a senha. Tente novamente.");
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-paper via-rose-wash/30 to-nude/20 px-4">
      <div className="w-full max-w-sm rounded-2xl border border-border bg-card p-8 shadow-lg">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
            <ShieldCheck size={22} />
          </span>
          <h1 className="font-display text-2xl text-foreground">Crie sua nova senha</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Por segurança, defina uma nova senha antes de continuar.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="relative">
            <KeyRound size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              autoFocus
              type="password"
              placeholder="Senha atual"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              className="pl-9"
              autoComplete="current-password"
            />
          </div>
          <div className="relative">
            <KeyRound size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="password"
              placeholder="Nova senha (mín. 8 caracteres)"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="pl-9"
              autoComplete="new-password"
            />
          </div>
          <div className="relative">
            <KeyRound size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="password"
              placeholder="Confirme a nova senha"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="pl-9"
              autoComplete="new-password"
            />
          </div>

          {error && (
            <p className="rounded-lg bg-destructive/10 px-3 py-2 text-sm text-destructive">{error}</p>
          )}

          <Button
            type="submit"
            size="lg"
            className="w-full shadow-sm"
            disabled={changePassword.isPending || !currentPassword || !newPassword || !confirmPassword}
          >
            {changePassword.isPending ? "Salvando..." : "Salvar nova senha"}
          </Button>
        </form>
      </div>
    </div>
  );
}
