# Para Isaborosa 🤍

Um site romântico de uma página — surpresa do Iagostoso para a Isaborosa.

## Como rodar

```sh
npm install
npm run dev
```

Depois abra o endereço que aparecer no terminal (geralmente http://localhost:3000).

> Também funciona com `bun install` e `bun run dev`.

## O que tem aqui

- Abertura com "Para Isaborosa"
- Letra da música na primeira seção
- "Você é o meu lugar quando tudo por um fio está"
- Mural de fotos com legendas
- Encerramento: "essa é só o começo da nossa história, amor da minha vida"
- Player da música "Young and Beautiful" — Lana Del Rey

## Biblioteca

Um novo serviço em `/biblioteca` — estante pessoal, busca, recomendações e preços.
Roda sobre o mesmo shell e design system do site. O backend fica em `backend/`
(ver `backend/README.md` para subir localmente). Para o frontend consumir a API,
defina `VITE_API_BASE_URL` (copie `.env.example` para `.env`).

## Feito com

- TanStack Start
- React + TypeScript
- Tailwind CSS
- Backend: Java 21 + Spring Boot (em `backend/`)
# isaborosa
