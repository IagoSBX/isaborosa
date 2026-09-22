# Biblioteca — Backend

API REST em Spring Boot para o serviço de Biblioteca Digital do Isaborosa.

## Stack

Java 21, Spring Boot 3.3, Maven, MySQL 8, Flyway, springdoc-openapi (Swagger UI).

## Como rodar localmente

1. Copie `.env.example` para `.env` e ajuste se necessário (a porta padrão de dev é `3309` para o MySQL local, para não colidir com outros serviços na máquina).
2. Suba o MySQL:
   ```bash
   docker compose up -d mysql
   ```
3. Exporte as variáveis de ambiente (ou use um plugin de `.env` da sua IDE) e rode a aplicação:
   ```bash
   export $(grep -v '^#' .env | xargs)
   mvn spring-boot:run
   ```
4. A API sobe em `http://localhost:8082` (porta configurável via `SERVER_PORT`).

URLs úteis:
- Swagger UI: `http://localhost:8082/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8082/v3/api-docs`
- Health check: `http://localhost:8082/actuator/health`

## Como testar o fluxo completo

Com a aplicação e o MySQL no ar:

```bash
# 1. Buscar na Open Library
curl "http://localhost:8082/api/books/search?q=harry+potter"

# 2. Salvar um resultado localmente
curl -X POST http://localhost:8082/api/books -H 'Content-Type: application/json' -d '{
  "title": "Harry Potter and the Philosopher'"'"'s Stone",
  "author": "J. K. Rowling",
  "coverUrl": "https://covers.openlibrary.org/b/id/15155833-L.jpg",
  "publishYear": 1997,
  "openLibraryKey": "/works/OL82563W"
}'

# 3. Adicionar à biblioteca pessoal (use o "id" retornado acima)
curl -X POST http://localhost:8082/api/library -H 'Content-Type: application/json' -d '{"bookId": 1, "status": "QUERO_LER"}'

# 4. Listar a biblioteca (com filtro opcional ?status=)
curl http://localhost:8082/api/library

# 5. Atualizar status e nota (use o "id" do item da biblioteca)
curl -X PATCH http://localhost:8082/api/library/1 -H 'Content-Type: application/json' -d '{"status": "LIDO", "rating": 5}'

# 6. Remover
curl -X DELETE http://localhost:8082/api/library/1

# 7. Recomendacoes (baseadas em livros avaliados com nota >= 4)
curl http://localhost:8082/api/recommendations

# 8. Precos reais (hoje, apenas Mercado Livre; ver nota abaixo)
curl http://localhost:8082/api/books/1/prices
```

Ou use o Swagger UI para o mesmo fluxo de forma interativa.

Testes automatizados (unitarios + integracao com H2 em memoria, sem precisar de Docker):
```bash
mvn test
```

## Notas da Fase 1

- Sem autenticação ainda: existe um único usuário fixo (seed na migration `V1__init_schema.sql`), usado implicitamente por todos os endpoints de biblioteca.
- Busca na Open Library nunca retorna 500 por falha do terceiro: em timeout/erro, loga um warning e devolve lista vazia (ver `OpenLibraryClient`).
- `POST /api/books` faz upsert: se já existe um livro com o mesmo ISBN ou `openLibraryKey`, atualiza os dados em vez de duplicar.

## Notas da Fase 3

- **Recomendações**: `TopRatedAuthorRecommendationStrategy` agrega os autores dos livros avaliados com nota >= 4, busca candidatos na Open Library por autor e remove o que já está na biblioteca. Resultado cacheado em memória por 1h (`RecommendationService`). A porta `RecommendationStrategy` permite trocar por uma estratégia baseada em modelo depois sem mudar o endpoint.
- **Preços**: `PriceService` consulta cada `PriceSource` em paralelo (`CompletableFuture`, com timeout por fonte) e persiste a última consulta em `price_checks`, servindo de cache de 20 min (`PRICE_CACHE_MINUTES`). Fonte que falha é omitida da resposta — nunca é inventado um valor.
- **Limitação real da API do Mercado Livre**: o endpoint público `/sites/MLB/search` hoje retorna `403 Forbidden` para requisições anônimas (isso mudou desde que a API era totalmente aberta). Configure `MERCADOLIVRE_ACCESS_TOKEN` com um token de uma aplicação cadastrada em https://developers.mercadolivre.com.br/ para essa fonte voltar a funcionar; sem o token, `GET /api/books/{id}/prices` responde `[]` honestamente em vez de simular um preço.

