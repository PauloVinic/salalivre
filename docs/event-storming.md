# Event Storming — SalaLivre

## Contexto
O SalaLivre busca reduzir conflitos de agenda e aumentar a visibilidade sobre a ocupação de salas de reunião. O foco está em permitir reservas claras, com regras de disponibilidade, permissões e notificações automáticas.

## Eventos de Domínio (Eventos Críticos)
- **ReservaCriada**: uma reserva foi confirmada para uma sala em um período válido e sem conflito.
- **ReservaCancelada**: uma reserva foi cancelada pelo solicitante autorizado.
- **ReservaAlterada**: uma reserva teve seu período atualizado.
- **StatusReservaNotificado**: evento de notificação disparado após criação/cancelamento/alteração.

## Comandos
- **CadastrarSala**: administrador registra uma sala com capacidade, localização e recursos.
- **CriarReserva**: usuário solicita uma reserva informando sala, início e fim.
- **CancelarReserva**: usuário (ou admin) solicita cancelamento da reserva.
- **AlterarReserva**: usuário solicita alteração de período da reserva.
- **ListarDisponibilidade**: usuário consulta salas disponíveis para um período.

## Agregados e Entidades
- **Sala** (Agregado): id, nome, capacidade, localização, recursos, ativa.
- **Reserva** (Agregado): id, salaId, usuarioId, periodo, status, criadoEm, atualizadoEm.
- **Usuario** (Entidade de Suporte): id, nome, email, tipo (ADMIN/COMUM).

## Regras de Negócio
- Período válido: `fim > inicio`.
- Conflito de horário: `inicio < existente.fim` e `fim > existente.inicio` para a mesma sala.
- Reservas CANCELADAS não entram na verificação de conflito.
- Cancelamento permitido apenas para ADMIN ou para o próprio usuário da reserva.
- Alteração de período muda o status para ALTERADA.

## Fluxos de Negócio (Resumo)
1. **Criar reserva**
   - Recebe comando com salaId, usuarioId, inicio/fim.
   - Valida período e existência de sala/usuário.
   - Verifica conflitos.
   - Cria Reserva (status CONFIRMADA) e dispara notificação.

2. **Cancelar reserva**
   - Recebe reservaId e solicitanteUsuarioId.
   - Busca reserva e usuário.
   - Valida permissão no domínio.
   - Atualiza status para CANCELADA e dispara notificação.

3. **Alterar reserva**
   - Recebe reservaId e novo período.
   - Valida período e conflito.
   - Atualiza período, status ALTERADA e dispara notificação.

4. **Disponibilidade**
   - Recebe período.
   - Retorna salas ativas sem reservas conflitantes.
