# SalaLivre

Sistema para reserva de salas de reuniao com foco em disponibilidade, conflitos de agenda e gestao de reservas.

Repositorio do projeto: https://github.com/PauloVinic/salalivre

## Stack
- Spring Boot 4.0.2
- Java 21
- Maven
- H2 (memoria)
- JPA + Validation
- Swagger/OpenAPI (springdoc)

## Como rodar
```bash
mvn spring-boot:run
```

A aplicacao sobe em `http://localhost:8080`.

## Swagger
- UI: `http://localhost:8080/swagger-ui/index.html`

## H2 Console
- `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:salalivre`
- Usuario: `sa`
- Senha: (vazia)

## Endpoints principais
Base: `/api/v1`

### Salas
- POST `/salas` (ADMIN-only, exige `X-User-Id` e perfil admin)
- GET `/salas`
- GET `/salas/{id}`
- PATCH `/salas/{id}/ativar` (ADMIN-only, exige `X-User-Id` e perfil admin)
- PATCH `/salas/{id}/desativar` (ADMIN-only, exige `X-User-Id` e perfil admin)

### Reservas
- POST `/reservas`
- GET `/reservas` (ADMIN-only)
- GET `/reservas/{id}` (ADMIN-only)
- GET `/reservas/sala/{salaId}` (ADMIN-only)
- GET `/reservas/usuario/{usuarioId}` (ADMIN-only)
- PATCH `/reservas/{id}/cancelar` (exige `X-User-Id`)
- PATCH `/reservas/{id}/alterar` (exige `X-User-Id`)

### Disponibilidade
- GET `/disponibilidade?inicio=...&fim=...`

## Exemplos (curl)
Cadastrar sala:
```bash
curl -X POST http://localhost:8080/api/v1/salas \
  -H "X-User-Id: USUARIO_ID" \
  -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Sala Azul","capacidade":10,"localizacao":"Andar 1","recursos":["Projetor"]}'
```

Criar reserva:
```bash
curl -X POST http://localhost:8080/api/v1/reservas \
  -H "Content-Type: application/json" \
  -d '{"salaId":"SALA_ID","usuarioId":"USUARIO_ID","inicio":"2026-01-26T09:00:00","fim":"2026-01-26T10:00:00"}'
```

Consultar disponibilidade:
```bash
curl "http://localhost:8080/api/v1/disponibilidade?inicio=2026-01-26T09:00:00&fim=2026-01-26T10:00:00"
```

Cancelar reserva (headers obrigatorios):
```bash
curl -X PATCH http://localhost:8080/api/v1/reservas/RESERVA_ID/cancelar \
  -H "X-User-Id: USUARIO_ID" \
  -H "X-User-Role: ADMIN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

## Autorizacao simulada (escopo academico)
- O projeto nao usa Spring Security nesta fase.
- A identidade/perfil do solicitante e simulada por headers:
  - `X-User-Id` (obrigatorio nas operacoes protegidas)
  - `X-User-Role` (opcional; `ADMIN` autoriza operacoes administrativas)
  - `X-Admin` (compatibilidade com testes/clients legados)
- Operacoes ADMIN-only aceitam `X-User-Role: ADMIN` (preferencial) ou `X-Admin: true`.
- `PATCH /reservas/{id}/cancelar` e `PATCH /reservas/{id}/alterar` exigem `X-User-Id`.

## Documentacao
- `docs/event-storming.md`
- `docs/arquitetura-ddd.md`

## Testes
```bash
mvn test
```

Os testes usam Mockito inline. O Maven Surefire executa o Byte Buddy agent via `-javaagent`
para evitar self-attach no JDK 21 e manter o output limpo.

> Observacao: evite `mvn clean` no Windows se houver falhas com `target`. Se necessario, exclua `target` manualmente.
