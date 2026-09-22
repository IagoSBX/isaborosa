# Biblioteca — notas de auditoria visual (Fase 2, passo 2.1)

Levantamento feito antes de escrever qualquer componente novo, para garantir que a
Biblioteca herde a identidade visual do Isaborosa em vez de criar uma paralela.

## Stack real do projeto (difere do prompt original em um ponto)

- **Framework de rotas**: TanStack Start + **TanStack Router** com roteamento por
  arquivos em `src/routes/`, não "React Router" nem Next.js. Segmento dinâmico usa
  `$id` (ex.: `src/routes/biblioteca/livro.$id.tsx` → `/biblioteca/livro/:id`), nunca
  `:id` ou `[id]`. `routeTree.gen.ts` é gerado automaticamente — nunca editado à mão.
- **Estilo**: Tailwind CSS v4 (config só em CSS, sem `tailwind.config.js`), tokens
  definidos em `src/styles.css` via `@theme inline` + `:root`, cores em OKLCH.
- **Componentes**: shadcn/ui (`style: "new-york"`) já instalado em
  `src/components/ui/*` — `Button`, `Card`, `Badge`, `Dialog`, `Tabs`, `Skeleton`,
  etc. já existem e devem ser reaproveitados, não recriados.
- **Data fetching**: `@tanstack/react-query` já configurado globalmente em
  `src/routes/__root.tsx` (`QueryClientProvider`). Não existe `axios` nem wrapper de
  `fetch` ainda — criamos `src/lib/api.ts` como o prompt previu para esse caso.
- **Alias de import**: `@/*` → `src/*` (`tsconfig.json` + `components.json`).

## Paleta — já é praticamente a paleta pedida para a Biblioteca

O prompt pedia tons de creme/bege/marrom/vinho "como extensão" da paleta atual.
Na prática, a paleta atual **já é** essa paleta:

| Token CSS | Papel atual no site | Uso na Biblioteca |
| --- | --- | --- |
| `--background` / `--paper` | fundo creme claro | fundo das páginas da Biblioteca |
| `--card` | branco levemente creme | cartões de livro (`LivroCard`) |
| `--primary` / `--wine` | vinho escuro (títulos, ícones, CTAs) | destaque, botões primários, estrelas preenchidas |
| `--secondary` | bege rosado | badges de status (`QUERO_LER`) |
| `--accent` / `--rose-wash` | rosa/bege claro | hover, fundo de seções alternadas |
| `--nude` | bege médio | fundo de estados vazios / skeleton |
| `--muted` / `--muted-foreground` | texto secundário | metadados do livro (autor, ano) |
| `--border` | bege acinzentado | bordas de card e inputs |
| `--radius` (0.4rem) e `--radius-lg` | cantos do site | mesmos raios nos cards/inputs da Biblioteca |

**Decisão**: nenhum token novo foi criado. A Biblioteca usa exclusivamente as
classes utilitárias já existentes (`bg-background`, `text-foreground`, `bg-card`,
`text-primary`, `bg-secondary`, `border-border`, `rounded-lg`, etc.).

## Tipografia

- `--font-sans` (DM Sans): corpo de texto, metadados, botões.
- `--font-display` (DM Serif Display): títulos de página e de livro — mesmo peso
  visual usado em `h2` no restante do site.
- `--font-hand` (Caveat): reservado para o tom pessoal do site (cartinha, "Yo!");
  **não usado na Biblioteca**, que é uma área mais funcional/utilitária.

## Layout e navegação — divergência real do prompt, resolvida

O prompt assume um `Header`/`Container`/nav global já existentes. **Não existem.**
A home (`src/routes/index.tsx`) é uma única página de rolagem contínua (carta,
fotos, playlist), sem barra de navegação nenhuma — de propósito, para preservar o
tom de carta pessoal.

Decisão (a favor de manter fidelidade ao site atual, não de "parecer mais
corporativo"): em vez de inventar um header global inexistente, foi adicionado um
único elemento discreto:

- Um link fixo, pequeno, no canto superior direito de toda a aplicação (adicionado
  em `src/routes/__root.tsx`), com o mesmo tratamento visual dos links do site
  (`--card`, `--border`, `--font-sans`, tamanho pequeno) — não é uma barra, não
  compete com a seção "opening" em tela cheia.
- Dentro de `/biblioteca/*`, um layout dedicado (`src/routes/biblioteca.tsx`) traz
  um cabeçalho leve reaproveitando os tokens (fundo `--paper`, título em
  `--font-display`), um link "← Para Isaborosa" de volta à home, e uma sub-nav com
  as 3 seções (Minha Biblioteca | Pesquisar | Recomendações) usando o componente
  `Tabs` do shadcn já disponível em `src/components/ui/tabs.tsx`. Em mobile, os
  tabs rolam horizontalmente (comportamento nativo do componente Radix Tabs).

## Escopo confirmado como fora da Fase 2

- `PrecoLista` e a seção de preços em `DetalhesLivro`: adiados para a Fase 3, já
  que o backend não tem `GET /api/books/{id}/prices` ainda. A página de detalhes
  não simula preço nenhum.
- `Recomendacoes.tsx`: a rota existe (exigência do prompt), mas como
  `GET /api/recommendations` só chega na Fase 3, a página mostra um estado vazio
  honesto ("recomendações chegam em breve") em vez de dado fictício.
