# Arquitetura DDD — SalaLivre

## Visão Geral
O projeto organiza o sistema em camadas inspiradas em DDD, mantendo o domínio puro e isolando preocupações de infraestrutura e interface.

O diagrama de Event Storming em `docs/event-storming.md` foi usado para separar os contextos centrais em Sala, Reserva e Notificação. Os comandos de API entram pela camada de Interfaces, os casos de uso ficam na Application e os eventos de domínio representam mudanças de estado relevantes (criação, alteração, cancelamento e lembrete). Para o escopo acadêmico, a autorização é simulada por headers (`X-User-Id`/`X-User-Role`) e as notificações são publicadas em log via `NotificacaoService`, sem integração externa de email.

## Camadas
- **Domain** (`br.com.fiap.salalivre.domain`)
  - Entidades, value objects, eventos e exceções de domínio.
  - Sem dependências de Spring/JPA.

- **Application** (`br.com.fiap.salalivre.application`)
  - Casos de uso e serviços de aplicação.
  - Coordena regras de negócio, validações e disparo de eventos.
  - Contém serviços em memória para testes unitários e serviços JPA para produção.

- **Infrastructure** (`br.com.fiap.salalivre.infrastructure`)
  - Persistência JPA (entities, repositories, mappers).
  - Seed de dados para facilitar testes manuais.
  - Isolamento completo do domínio, sem anotações JPA no domain.

- **Interfaces** (`br.com.fiap.salalivre.interfaces.api`)
  - Controllers REST e DTOs (request/response).
  - Validação de entrada, exposição de endpoints e serialização.

## Responsabilidades e Interações
- **Controllers** chamam os serviços de aplicação.
- **Serviços de aplicação** utilizam o domínio para aplicar regras e os repositórios JPA para persistir.
- **Mappers** convertem entre entidades JPA e objetos de domínio.
- **Eventos de domínio** são disparados na aplicação e registrados via `NotificacaoService`.

## Persistência (JPA)
- Entidades JPA ficam em `infrastructure/persistence/entity`.
- Repositórios Spring Data em `infrastructure/persistence/repository`.
- Mapeamento de listas (`recursos`) com `@ElementCollection`.
- Conflitos de reserva são consultados por JPQL no repositório de reservas.

## API REST
- Prefixo `/api/v1`.
- DTOs de request/response evitam exposição direta do domínio.
- Validações com Jakarta Bean Validation.
- Datas em ISO-8601.

## Tratamento de Erros
- `ApiExceptionHandler` centraliza respostas padronizadas.
- Códigos HTTP alinhados às regras: 400, 403, 404, 409.

## Decisões Técnicas
- **H2** em memória para facilitar desenvolvimento.
- **Swagger/OpenAPI** para documentação interativa.
- **Seed** para criar dados iniciais se o banco estiver vazio.

## Pontos de Evolução
- Introduzir autenticação/autorizações reais.
- Adicionar filtros mais performáticos de disponibilidade.
- Implementar notificações reais (email/integrações).
- Migrar repositórios em memória para mocks quando necessário.
