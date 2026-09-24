import { createFileRoute, Link, Outlet, useNavigate, useRouterState } from "@tanstack/react-router";
import { BarChart3, ChevronLeft, Lamp, LibraryBig, LogOut, Search } from "lucide-react";
import { useEffect } from "react";

import { cn } from "@/lib/utils";
import { useLogout } from "@/features/auth/hooks/useAuthMutations";
import { useSession } from "@/features/auth/hooks/useSession";
import { Cartinha } from "@/features/biblioteca/components/Cartinha";
import { useCozyMode } from "@/features/biblioteca/hooks/useCozyMode";

export const Route = createFileRoute("/biblioteca")({
  head: () => ({
    meta: [
      { title: "Biblioteca da Baldinho — Isaborosa" },
      { name: "description", content: "A biblioteca pessoal da Isaborosa: seu cantinho de histórias." },
    ],
  }),
  component: BibliotecaLayout,
});

const TABS = [
  { to: "/biblioteca", label: "Minha Estante", icon: LibraryBig },
  { to: "/biblioteca/pesquisar", label: "Pesquisar", icon: Search },
  { to: "/biblioteca/estatisticas", label: "Estatísticas", icon: BarChart3 },
] as const;

const PUBLIC_PATHS = ["/biblioteca/login", "/biblioteca/trocar-senha"];

function BibliotecaLayout() {
  const pathname = useRouterState({ select: (state) => state.location.pathname });
  const isPublicPath = PUBLIC_PATHS.includes(pathname);
  const { data: session, isLoading, isError } = useSession();
  const navigate = useNavigate();
  const logout = useLogout();
  const cozy = useCozyMode();

  useEffect(() => {
    if (isPublicPath || isLoading) return;
    if (isError || !session) {
      navigate({ to: "/biblioteca/login" });
    } else if (session.mustChangePassword && pathname !== "/biblioteca/trocar-senha") {
      navigate({ to: "/biblioteca/trocar-senha" });
    }
  }, [isPublicPath, isLoading, isError, session, pathname, navigate]);

  // Tela de login/troca de senha: sem o cabecalho/sub-nav da biblioteca.
  if (isPublicPath) {
    return <Outlet />;
  }

  if (isLoading || isError || !session || session.mustChangePassword) {
    return <div className="min-h-screen bg-background" />;
  }

  return (
    <div
      className={cn(
        "paper-grain min-h-screen bg-gradient-to-b from-paper via-background to-background",
        cozy.enabled && "cozy",
      )}
    >
      <header className="border-b border-border bg-gradient-to-br from-paper via-rose-wash/30 to-nude/20 px-4 py-6 sm:px-8">
        <div className="mx-auto max-w-5xl">
          <div className="flex flex-wrap items-center justify-between gap-x-3 gap-y-2">
            <Link
              to="/"
              className="inline-flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
            >
              <ChevronLeft size={14} /> <span className="hidden sm:inline">Para Isaborosa</span>
            </Link>
            <div className="flex items-center gap-3 sm:gap-4">
              <Cartinha />
              <button
                type="button"
                onClick={cozy.toggle}
                title="Modo Aconchego"
                className={cn(
                  "inline-flex items-center gap-1.5 text-xs transition-colors",
                  cozy.enabled ? "text-primary" : "text-muted-foreground hover:text-foreground",
                )}
              >
                <Lamp size={14} /> <span className="hidden sm:inline">Modo Aconchego</span>
              </button>
              <button
                type="button"
                onClick={() => logout.mutate(undefined, { onSuccess: () => navigate({ to: "/biblioteca/login" }) })}
                className="inline-flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
              >
                <LogOut size={14} /> <span className="hidden sm:inline">Sair</span>
              </button>
            </div>
          </div>
          <h1 className="mt-2 font-display text-3xl text-foreground sm:text-4xl">✦ Biblioteca da Baldinho</h1>
          <p className="mt-0.5 text-sm text-muted-foreground">Seu cantinho de histórias.</p>

          <nav aria-label="Seções da biblioteca" className="mt-5 flex gap-5 overflow-x-auto border-b border-border/70">
            {TABS.map((tab) => {
              const Icon = tab.icon;
              return (
                <Link
                  key={tab.to}
                  to={tab.to}
                  activeOptions={{ exact: true }}
                  className="flex shrink-0 items-center gap-1.5 whitespace-nowrap border-b-2 border-transparent pb-2.5 text-sm font-medium text-muted-foreground transition-all hover:text-foreground"
                  activeProps={{
                    className: "!border-primary !text-primary",
                  }}
                >
                  <Icon size={15} />
                  {tab.label}
                </Link>
              );
            })}
          </nav>
        </div>
      </header>

      <main className="mx-auto max-w-5xl px-4 py-8 sm:px-8">
        <Outlet />
      </main>

      <p className="pb-6 text-center text-[10px] tracking-wide text-muted-foreground/50">
        ✦ Feito especialmente para você
      </p>
    </div>
  );
}
