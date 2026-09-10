# [ADR-003] Histórico de Status e Jornada do Pedido

* **Status**: Aceito
* **Data**: 2026-09-09
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)
* **SPEC Relacionada**: [SPEC-002 — Acompanhamento da Jornada do Pedido](../specs/SPEC-002-jornada-do-pedido.md)

---

## 1. Contexto e Declaração do Problema

O requisito RF-13 (item 8 da proposta de checkpoint) pede uma área de
acompanhamento da jornada do pedido, mostrando as etapas "solicitado", "aceito",
"em andamento" e "concluído".

A entidade `Order` guardava apenas o **status atual** (`PENDING`, `CONFIRMED`,
`IN_PROGRESS`, `COMPLETED`, `CANCELLED`) e a data de criação. Faltava:

1. **Quando** cada etapa foi atingida (não há como montar uma linha do tempo).
2. **Quem** promoveu cada transição.
3. Uma etapa real de **aceite**: o front-end já modelava a coluna `CONFIRMED`
   ("Aceito") no Kanban, mas `OrderService.validateProviderStatusTransition` só
   permitia `PENDING → IN_PROGRESS`, tornando o status `CONFIRMED` inalcançável.

Era preciso decidir **como registrar o histórico**, **como expor a jornada** e
**como tratar pedidos que já existem sem histórico**.

## 2. Direcionadores de Decisão (Drivers)

* **Observabilidade e auditoria**: registrar cada transição com data/hora e autor.
* **Compatibilidade com dados existentes**: pedidos antigos, sem histórico,
  precisam exibir uma jornada coerente sem migração de dados.
* **Não regredir contratos**: as listagens de pedidos (`/orders/my-orders`,
  `/orders/provider`) não devem mudar de formato.
* **Baixo acoplamento**: a lógica da jornada não deve inchar `OrderService` nem o
  `OrderMapper`.
* **Prazo curto** (apresentação em 12/09): preferir a solução mais simples que
  atenda aos critérios de aceite.

## 3. Decisão Considerada e Aprovada

### 3.1 Entidade dedicada `OrderStatusHistory` (append-only)
* Tabela `order_status_history`: `id`, `order_id` (FK), `status`, `changed_at`,
  `changed_by_role` (nullable).
* Um registro é gravado em `OrderService.create` (status inicial `PENDING`) e em
  cada `OrderService.updateStatus` bem-sucedido.
* Consulta principal: `findByOrderIdOrderByChangedAtAsc`.

### 3.2 Etapa "Aceito" real
* O fluxo do prestador passa a ser
  `PENDING → CONFIRMED → IN_PROGRESS → COMPLETED`, com cancelamento possível em
  `PENDING` e `CONFIRMED`.
* O cliente continua podendo apenas `PENDING → CANCELLED`.

### 3.3 Endpoint separado para a jornada
* `GET /orders/{id}/journey` retorna `OrderJourneyResponseDTO` (quatro etapas
  canônicas com estado `DONE | CURRENT | PENDING | SKIPPED` e `reachedAt`).
* Acesso restrito ao cliente dono, ao prestador do pedido ou a um `ADMIN`
  (senão `403`). O `OrderResponseDTO` **não** é alterado.
* A montagem fica em um serviço próprio, `OrderJourneyService`.

### 3.4 Reconstrução para pedidos legados
* Quando não há histórico, a jornada é derivada do **status atual** e da
  **data de criação** do pedido: todas as etapas até o status atual ficam `DONE`
  (carimbadas com `createdAt`), a próxima fica `CURRENT`.

## 4. Consequências

### Positivas
* Trilha temporal completa e rastreável de cada pedido.
* Pedidos antigos exibem jornada sem necessidade de script de migração.
* Contrato das listagens de pedidos preservado.
* Base pronta para o RF de Notificações (a SPEC futura pode reagir aos eventos de
  histórico).

### Negativas / Trade-offs
* Uma nova tabela, entidade e repositório.
* Mudança nas transições de status exigiu ajuste no front-end (botão "Aceitar" e
  selects) — feito na mesma entrega.
* O histórico é gravado na mesma transação da mudança de status; uma falha ao
  gravar o histórico aborta a transição (aceitável: consistência > disponibilidade
  neste caso).

## 5. Alternativas Consideradas

* **Campos de timestamp no `Order`** (`confirmedAt`, `startedAt`, `completedAt`,
  `cancelledAt`): *descartada*. Não registra o autor, não comporta reversões nem
  novos estados sem alterar o schema, e polui a entidade principal.
* **Log de auditoria genérico** (tabela de eventos para todas as entidades):
  *descartada* por over-engineering para o escopo e o prazo.
* **Embutir o array `journey` no `OrderResponseDTO`**: *descartada*. Incharia
  todas as listagens de pedidos e obrigaria o `OrderMapper` a carregar o
  histórico em respostas onde ele não é usado.
* **Manter as transições atuais e derivar "Aceito"** quando `status >= IN_PROGRESS`:
  *descartada*. Não teria carimbo de data próprio para a etapa e contrariava o
  Kanban já existente.
