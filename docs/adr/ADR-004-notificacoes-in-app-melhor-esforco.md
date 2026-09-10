# [ADR-004] Notificações In-App com Geração em Melhor Esforço

* **Status**: Aceito
* **Data**: 2026-09-09
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)
* **SPEC Relacionada**: [SPEC-003 — Notificações de Mudança de Status do Pedido](../specs/SPEC-003-notificacoes-de-status.md)

---

## 1. Contexto e Declaração do Problema

O RF-14 pede que os participantes de um pedido sejam avisados quando o status
muda. Era preciso decidir **o canal de entrega**, **como persistir** e **como a
geração da notificação se relaciona com a transação que muda o status** do pedido
(que já grava `OrderStatusHistory` — ver [ADR-003](ADR-003-historico-de-status-e-jornada-do-pedido.md)).

## 2. Direcionadores de Decisão (Drivers)

* **Prazo curto** (apresentação em 12/09): sem infraestrutura nova (broker de
  e-mail, servidor de push, WebSocket).
* **Não regredir o fluxo de pedidos**: uma falha ao notificar não pode impedir a
  mudança de status nem estourar erro para quem agiu.
* **Demonstrabilidade**: o professor precisa ver a notificação chegando na tela.
* **Reuso**: aproveitar o ponto único de mudança de status
  (`OrderService.updateStatus`).

## 3. Decisão Considerada e Aprovada

### 3.1 Canal: notificação **in-app** persistida
* Tabela dedicada `notifications` (`recipient_id`, `message`, `order_id`
  nullable, `is_read`, `read_at`, `created_at`).
* Exibição via sino no topo da sidebar, com contador de não lidas e lista.

### 3.2 Geração em **melhor esforço**, em transação separada
* `NotificationService.notifyOrderStatusChange` roda com
  `@Transactional(propagation = REQUIRES_NEW)`.
* `OrderService.updateStatus` chama esse método dentro de um `try/catch`: se a
  geração falhar, registra `log.warn` e **segue**; a mudança de status e o
  histórico já commitados permanecem.

### 3.3 Regra de destinatário
* Notifica cada participante do pedido **exceto** o autor da mudança
  (PROVIDER → cliente; CLIENT → prestador; ADMIN → ambos).

### 3.4 Atualização no cliente
* O contador é recarregado a cada `NavigationEnd` do Router (sem polling nem
  tempo real).

## 4. Consequências

### Positivas
* Zero dependência de infraestrutura externa.
* Fluxo de pedidos resiliente a falhas de notificação.
* Base reaproveitável para futuros eventos (nova avaliação, etc.).

### Negativas / Trade-offs
* Não há entrega em tempo real: o usuário só vê a notificação ao navegar/atualizar.
* `REQUIRES_NEW` abre uma segunda conexão/transação por mudança de status.
* A mensagem é montada como texto fixo em português no serviço (sem i18n).

## 5. Alternativas Consideradas

* **Coluna/flag no `Order`**: *descartada*. Não guarda histórico de avisos nem
  estado de leitura por usuário.
* **E-mail (SMTP)**: *descartada* para o prazo — exige provedor, template,
  tratamento de bounce, e não é demonstrável offline.
* **WebSocket / SSE para tempo real**: *descartada* por complexidade
  desproporcional ao escopo.
* **Evento de domínio + listener assíncrono (`@TransactionalEventListener`)**:
  *descartada* por ora — boa evolução futura, mas adiciona configuração de
  publicação de eventos e assíncrono que não cabe no prazo. O ponto de chamada
  único em `updateStatus` já isola a responsabilidade e facilita essa migração
  depois.
