import { createFileRoute, Link, Outlet } from "@tanstack/react-router";
import { BarChart3, ChevronLeft, LibraryBig, Search } from "lucide-react";

export const Route = createFileRoute("/biblioteca")({
  head: () => ({
    meta: [
      { title: "Biblioteca — Isaborosa" },
      { name: "description", content: "A biblioteca digital pessoal do Isaborosa." },
    ],
  }),
  component: BibliotecaLayout,
});

const TABS = [
  { to: "/biblioteca", label: "Minha biblioteca", icon: LibraryBig },
  { to: "/biblioteca/pesquisar", label: "Pesquisar", icon: Search },
  { to: "/biblioteca/estatisticas", label: "Estatísticas", icon: BarChart3 },
] as const;

function BibliotecaLayout() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-paper via-background to-background">
      <header className="border-b border-border bg-gradient-to-br from-paper via-rose-wash/30 to-nude/20 px-4 py-6 sm:px-8">
        <div className="mx-auto max-w-5xl">
          <Link
            to="/"
            className="inline-flex items-center gap-1 text-xs text-muted-foreground transition-colors hover:text-foreground"
          >
            <ChevronLeft size={14} /> Para Isaborosa
          </Link>
          <h1 className="mt-2 font-display text-3xl text-foreground sm:text-4xl">Biblioteca</h1>

          <nav aria-label="Seções da biblioteca" className="mt-5 flex gap-1.5 overflow-x-auto">
            {TABS.map((tab) => {
              const Icon = tab.icon;
              return (
                <Link
                  key={tab.to}
                  to={tab.to}
                  activeOptions={{ exact: true }}
                  className="flex shrink-0 items-center gap-1.5 whitespace-nowrap rounded-full border border-transparent px-3.5 py-1.5 text-sm font-medium text-muted-foreground transition-all hover:bg-card hover:text-foreground"
                  activeProps={{
                    className: "!border-primary !bg-primary !text-primary-foreground shadow-sm",
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
    </div>
  );
}
