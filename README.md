# SalaLivre

Sistema para reserva de salas de reuniao com foco em disponibilidade, conflitos de agenda e gestao de reservas.

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
- POST `/salas`
- GET `/salas`
- GET `/salas/{id}`
- PATCH `/salas/{id}/ativar`
- PATCH `/salas/{id}/desativar`

### Reservas
- POST `/reservas`
- PATCH `/reservas/{id}/cancelar`
- PATCH `/reservas/{id}/alterar`

### Disponibilidade
- GET `/disponibilidade?inicio=...&fim=...`

## Exemplos (curl)
Cadastrar sala:
```bash
curl -X POST http://localhost:8080/api/v1/salas \
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
