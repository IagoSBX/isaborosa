import { Link } from "@tanstack/react-router";

import { Skeleton } from "@/components/ui/skeleton";
import { Button } from "@/components/ui/button";
import { LivroCard } from "./LivroCard";
import type { UserBook } from "../types";

interface EstanteProps {
  userBooks: UserBook[];
  isLoading: boolean;
}

function EstanteSkeleton() {
  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(9rem,1fr))] gap-4">
      {Array.from({ length: 8 }).map((_, index) => (
        <div key={index}>
          <Skeleton className="aspect-[2/3] w-full rounded-xl" />
          <Skeleton className="mt-2 h-3 w-3/4" />
          <Skeleton className="mt-1 h-3 w-1/2" />
        </div>
      ))}
    </div>
  );
}

export function Estante({ userBooks, isLoading }: EstanteProps) {
  if (isLoading) {
    return <EstanteSkeleton />;
  }

  if (userBooks.length === 0) {
    return (
      <div className="flex flex-col items-center gap-4 rounded-2xl border border-dashed border-border bg-gradient-to-br from-card to-rose-wash/20 px-6 py-16 text-center">
        <p className="font-display text-xl text-foreground">Sua estante está vazia, por enquanto.</p>
        <p className="max-w-sm text-sm text-muted-foreground">
          Procure um livro e adicione à sua biblioteca pessoal para começar a organizar sua leitura.
        </p>
        <Button asChild size="lg">
          <Link to="/biblioteca/pesquisar">Pesquisar livros</Link>
        </Button>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(9rem,1fr))] gap-x-4 gap-y-6">
      {userBooks.map((userBook) => (
        <LivroCard
          key={userBook.id}
          title={userBook.book.title}
          author={userBook.book.author}
          coverUrl={userBook.book.coverUrl}
          genre={userBook.book.genre}
          rating={userBook.rating}
          status={userBook.status}
          favorite={userBook.favorite}
          href={`/biblioteca/livro/${userBook.book.id}`}
        />
      ))}
    </div>
  );
}
