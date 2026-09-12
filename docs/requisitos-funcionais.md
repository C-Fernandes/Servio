# Requisitos Funcionais — Servio

Levantamento dos requisitos funcionais (RF) do sistema, para a apresentação da
disciplina **Desenvolvimento de Software com IA** (PPGTI / UFRN). A especificação
exige no mínimo **10 RF claramente identificáveis e demonstráveis** (seção III).

O sistema atende **21 RF**. Os onze requisitos construídos via
Spec-Driven Development — **RF-05 (Busca Avançada)**, **RF-12 (Favoritos)**,
**RF-13 (Jornada do Pedido)**, **RF-14 (Notificações de Status)**,
**RF-15 (Relatório de Desempenho do Prestador)**, **RF-16 (Chat)**,
**RF-17 (Bloqueio de Horários)**, **RF-18 (Cupons de Desconto)**,
**RF-19 (Histórico de Interações)**, **RF-20 (Denúncia)** e
**RF-21 (Recomendações de Serviços)** — têm especificação própria
(ver [SPEC-001](specs/SPEC-001-sistema-de-favoritos.md) a
[SPEC-011](specs/SPEC-011-recomendacoes-servicos.md)) e, na maioria dos
casos, um ADR de modelagem em `docs/adr/`. RF-05 e RF-21 não têm ADR próprio
por decisão consciente — reaproveitam padrões e entidades já existentes no
projeto, sem decisão de arquitetura nova o bastante para justificar um
registro dedicado (ver seção 7 de cada SPEC). Cada seção abaixo traz a
especificação, o ADR (quando houver) e a evidência de código e de teste do
respectivo RF.

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09

---

## Papéis

| Papel | Descrição |
| :--- | :--- |
| `CLIENT` | Cliente que busca e contrata serviços. |
| `PROVIDER` | Prestador que oferece serviços. |
| `ADMIN` | Administrador da plataforma (taxonomia). |

---

## Lista de Requisitos Funcionais

### RF-01 — Autenticação e cadastro
Registro de usuário como cliente ou prestador e login com emissão de token JWT.
- **Evidência:** `POST /auth/register`, `POST /auth/login`; telas de login e cadastro.

### RF-02 — Gestão do perfil do usuário
Visualização e edição de dados pessoais, contato e endereço; exclusão lógica da conta.
- **Evidência:** `GET/PUT/DELETE /users/{id}`; página "Minha conta".

### RF-03 — Cadastro e gestão de serviços (prestador)
CRUD de serviços com upload de imagem, categoria e tags; ativação/desativação e
exclusão lógica.
- **Evidência:** `POST /services`, `PUT /services/{id}`, `PATCH /services/{id}/status`, `DELETE /services/{id}`, `GET /services/my-services`; página "Meus serviços".

### RF-04 — Catálogo de serviços (marketplace)
Listagem de serviços ativos e visualização de detalhes com prestador, avaliação
média e horários disponíveis.
- **Evidência:** `GET /services`, `GET /services/{id}`; página "Explorar serviços" e página de detalhes.

### RF-05 — Busca avançada de serviços
Busca reativa (com debounce) por categoria, faixa de preço, avaliação mínima,
localização (cidade/estado) e texto livre, com ordenação. Backend com filtros
dinâmicos via JPA Specifications (join com a localidade do prestador, subquery
de avaliação média).
- **Evidência:** `GET /services/search`, `GET /services/locations`; página "Explorar serviços".
- **Especificação:** [SPEC-007](specs/SPEC-007-busca-avancada-servicos.md) (justifica a ausência de ADR: reuso do padrão `Specification` já usado no projeto).
- **Evidência (teste):** `ServiceServiceSearchTest` (3).

### RF-06 — Solicitação e acompanhamento de pedidos (cliente)
Cliente cria um pedido a partir de um serviço, lista seus pedidos e acompanha o status.
- **Evidência:** `POST /orders`, `GET /orders/my-orders`; painel do cliente.

### RF-07 — Gestão de pedidos e mudança de status (prestador)
Prestador visualiza os pedidos recebidos e atualiza o status ao longo da jornada
(solicitado → aceito → em andamento → concluído / cancelado).
- **Evidência:** `GET /orders/provider`, `PATCH /orders/{id}/status`; página "Pedidos".

### RF-08 — Avaliações
Cliente avalia um pedido concluído com nota e comentário, podendo editar ou excluir;
cálculo de média por serviço; prestador consulta as avaliações recebidas.
- **Evidência:** `POST/PUT/DELETE /reviews`, `GET /reviews/my-reviews`, `GET /reviews/provider`, `GET /reviews/service/{id}`; modal de avaliação e exibição na página do serviço.

### RF-09 — Agenda e disponibilidade do prestador
Definição de janelas de disponibilidade, criação de horários extras e remoção;
geração de horários disponíveis por serviço a partir da agenda.
- **Evidência:** `POST /api/calendar/sync`, `GET /api/calendar/calendar`; página "Agenda".

### RF-10 — Dashboards
Painel financeiro do prestador (ganhos, desempenho) e painel do cliente (gastos e
histórico).
- **Evidência:** `GET /financial-dashboard/provider`, `GET /financial-dashboard/client`; painéis de prestador e cliente.

### RF-11 — Gestão de categorias e tags (admin)
CRUD de categorias e tags restrito ao administrador, com validação que impede a
exclusão de categoria em uso.
- **Evidência:** `GET/POST/PUT/DELETE /category`, `GET/POST/PUT/DELETE /tags`; página "Categorias e Tags".

### RF-12 — Sistema de favoritos (NOVO — via SDD)
Cliente marca e desmarca serviços como favoritos, consulta a lista "Meus Favoritos"
e vê o indicador de favorito nos cards do marketplace. Operação idempotente;
serviços inativos/excluídos não podem ser favoritados; restrito ao perfil `CLIENT`.
- **Especificação:** [SPEC-001](specs/SPEC-001-sistema-de-favoritos.md) · [ADR-002](adr/ADR-002-modelagem-sistema-favoritos.md) · [diagrama](diagrams/diagrama-favoritos.md)
- **Evidência:** `POST/DELETE /favorites/{serviceId}`, `GET /favorites`, `GET /favorites/check/{serviceId}`; botão de favoritar no card, página "Meus favoritos", atalho na sidebar; `FavoriteServiceTest` (8 testes).

### RF-13 — Acompanhamento da jornada do pedido (NOVO — via SDD)
Cliente e prestador acompanham o pedido em quatro etapas canônicas
(Solicitado → Aceito → Em andamento → Concluído), com data/hora de cada etapa,
etapa atual destacada e tratamento do estado terminal "Cancelado". Cada transição
de status grava um evento de histórico; pedidos anteriores à funcionalidade têm a
jornada reconstruída a partir do status atual. Inclui a etapa real "Aceito"
(`PENDING → CONFIRMED`) no fluxo do prestador.
- **Especificação:** [SPEC-002](specs/SPEC-002-jornada-do-pedido.md) · [ADR-003](adr/ADR-003-historico-de-status-e-jornada-do-pedido.md)
- **Evidência (backend):** entidade `OrderStatusHistory`; `GET /orders/{id}/journey` (`OrderJourneyResponseDTO`) com verificação de participante (403); transições `PENDING → CONFIRMED → IN_PROGRESS → COMPLETED`; `OrderJourneyServiceTest` (8) + `OrderServiceTest` (3).
- **Evidência (frontend):** `OrderJourneyComponent` (linha do tempo) no detalhe do pedido do prestador e no painel do cliente ("Ver jornada"); botão "Aceitar pedido" no Kanban do prestador.

### RF-14 — Notificações de mudança de status do pedido (NOVO — via SDD)
A cada mudança de status de um pedido, cada participante que não seja o autor da
mudança recebe uma notificação in-app não lida (PROVIDER → cliente; CLIENT →
prestador; ADMIN → ambos). A geração é em melhor esforço: uma falha ao notificar
não reverte a mudança de status.
- **Especificação:** [SPEC-003](specs/SPEC-003-notificacoes-de-status.md) · [ADR-004](adr/ADR-004-notificacoes-in-app-melhor-esforco.md) · [diagrama](diagrams/notificacoes-de-status.md)
- **Evidência (backend):** entidade `Notification`; `GET /notifications`, `GET /notifications/unread-count`, `PATCH /notifications/{id}/read` (dono → 403), `PATCH /notifications/read-all`; gatilho em `OrderService.updateStatus` (try/catch, `REQUIRES_NEW`); `NotificationServiceTest` (10).
- **Evidência (frontend):** `NotificationBellComponent` (sino com badge, dropdown, "marcar todas") no topo da sidebar; recarga do contador a cada navegação.

### RF-15 — Relatório de desempenho do prestador (NOVO — via SDD)
Ranking dos próprios serviços por pedidos concluídos, com avaliação média e
quantidade de avaliações de cada um. Reaproveita dados de `Order` e `Review`
sem nova tabela.
- **Especificação:** [SPEC-004](specs/SPEC-004-relatorio-desempenho-prestador.md) · [ADR-009](adr/ADR-009-modelagem-relatorio-desempenho-prestador.md)
- **Evidência (backend):** `GET /financial-dashboard/provider/top-services`; `FinancialDashboardServiceTest` (3).
- **Evidência (frontend):** tabela "Serviços mais contratados" na aba Finanças do painel do prestador.

### RF-16 — Chat entre cliente e prestador
Canal de mensagens entre cliente e prestador antes da contratação
(`Conversation`/`Message`), com página dedicada e link na sidebar.
- **Evidência:** `ChatController`/`ChatService`; página "Mensagens".
- **Especificação:** [SPEC-008](specs/SPEC-008-chat-cliente-prestador.md) · [ADR-006](adr/ADR-006-modelagem-chat-cliente-prestador.md)
- **Evidência (teste):** `ChatServiceTest` (5).

### RF-17 — Bloqueio de horários específicos na disponibilidade (NOVO — via SDD)
Prestador bloqueia uma data/horário específico (ex.: feriado, compromisso),
mesmo quando coberto por uma regra semanal recorrente. O horário some das
opções de agendamento (`generateAvailableSlots`) sem apagar a regra semanal.
- **Especificação:** [SPEC-005](specs/SPEC-005-bloqueio-de-horarios.md) · [ADR-010](adr/ADR-010-modelagem-bloqueio-de-horarios.md)
- **Evidência (backend):** `POST/GET /api/calendar/blocks`, `DELETE /api/calendar/blocks/{id}` (dono → 403); `AvailabilityServiceTest` (7).
- **Evidência (frontend):** seção "Bloqueios de horário" na página Agenda (criar/listar/remover).

### RF-18 — Cupons de desconto por serviço (NOVO — via SDD)
Prestador cria cupons de desconto percentual restritos a um dos próprios serviços.
Cliente valida o código antes de reservar (vê preço original e preço com desconto) e o
aplica na criação do pedido; cada cliente pode usar um mesmo cupom no máximo uma vez. O
pedido grava preço original, percentual de desconto e preço final, preservados mesmo se o
cupom for desativado depois.
- **Especificação:** [SPEC-006](specs/SPEC-006-sistema-de-cupons-de-desconto.md) · [ADR-005](adr/ADR-005-modelagem-sistema-cupons.md)
- **Evidência (backend):** entidades `Coupon`/`CouponUsage`; `POST/GET /coupons`, `GET /coupons/my-coupons`, `DELETE /coupons/{id}` (dono → 403), `GET /coupons/validate`; `couponCode` opcional em `POST /orders`; `CouponServiceTest` (7).
- **Evidência (frontend):** página "Meus cupons" (criar/listar/desativar) na sidebar do prestador; campo de cupom com validação e preço com desconto na reserva do serviço.

### RF-19 — Histórico detalhado de interações entre cliente e prestador
Linha do tempo única, ordenada da mais recente para a mais antiga, reunindo
mensagens de chat (todas as conversas entre o par, não só uma), mudanças de
status de pedidos e avaliações trocadas entre um cliente e um prestador
específicos. Pedidos legados sem histórico de status são reconstruídos a
partir do status atual.
- **Evidência:** `GET /interactions/{otherUserId}`; botão "Ver histórico" no cabeçalho da conversa, na página "Mensagens".
- **Especificação:** [SPEC-009](specs/SPEC-009-historico-interacoes.md) · [ADR-007](adr/ADR-007-modelagem-historico-interacoes.md)
- **Evidência (teste):** `InteractionServiceTest` (5).

### RF-20 — Denúncia de serviço ou usuário
Cliente denuncia um serviço ou um usuário por conteúdo inapropriado, spam,
fraude, assédio, perfil falso ou outro motivo, com comentário livre. Admin
lista as denúncias (com filtro por status), analisa e marca como
"Revisada"/"Descartada"; ao marcar como revisada, a parte denunciada (dono do
serviço ou usuário) recebe uma notificação in-app informando que sua
conta/serviço foi analisado pela moderação.
- **Evidência (backend):** entidade `Report` (`ReportTargetType`: SERVICE/USER;
  `ReportReason`: INAPPROPRIATE_CONTENT, SPAM, FRAUD, HARASSMENT, FAKE_PROFILE,
  OTHER; `ReportStatus`: PENDING/REVIEWED/DISMISSED); `POST /reports` (Client),
  `GET /reports/my` (Client), `GET /reports` com filtro de status (Admin),
  `PATCH /reports/{id}/status` (Admin) — notifica a parte denunciada quando
  o novo status é REVIEWED.
- **Evidência (frontend):** `report-modal` (denunciar serviço/usuário), página
  "Denúncias" (`/reports`, admin) para listar e analisar.
- **Especificação:** [SPEC-010](specs/SPEC-010-denuncia-servico-usuario.md) · [ADR-008](adr/ADR-008-modelagem-sistema-denuncias.md)
- **Evidência (teste):** `ReportServiceTest` (5).

### RF-21 — Recomendações de serviços por perfil/interesse do cliente
Serviços ativos recomendados ao cliente com base na categoria e nas tags dos
serviços que ele já favoritou ou contratou, excluindo o que ele já conhece.
Sem nenhum sinal de interesse (cliente novo), o sistema recomenda os serviços
mais bem avaliados do catálogo.
- **Evidência:** `GET /services/recommendations`; seção "Recomendados para você" no topo da página "Explorar serviços" (só sem filtros ativos).
- **Especificação:** [SPEC-011](specs/SPEC-011-recomendacoes-servicos.md) (justifica a ausência de ADR: reuso de entidades e repositórios já existentes, sem decisão de arquitetura nova).
- **Evidência (teste):** teste automatizado ainda não implementado.

---

## Cobertura de testes automatizados

| RF | Teste |
| :--- | :--- |
| RF-05 | `ServiceServiceSearchTest` — ordenação por avaliação + casos de borda (preço inválido, sem filtro) da SPEC-007 |
| RF-12 | `FavoriteServiceTest` — 3 cenários Gherkin + 3 casos de borda da SPEC-001 |
| RF-13 | `OrderJourneyServiceTest` — 3 cenários + casos de borda (cancelado, legado, acesso indevido, 404) da SPEC-002 |
| RF-13 | `OrderServiceTest` — registro de histórico e validação das novas transições de status |
| RF-14 | `NotificationServiceTest` — 4 cenários + casos de borda (ADMIN notifica ambos, 403, idempotência, marcar todas sem nenhuma, 404) da SPEC-003 |
| RF-15 | `FinancialDashboardServiceTest` — ranking ordenado + casos de borda (sem concluídos, sem perfil de prestador) da SPEC-004 |
| RF-16 | `ChatServiceTest` — 3 cenários + casos de borda (não participante, prestador tentando iniciar) da SPEC-008 |
| RF-17 | `AvailabilityServiceTest` — exclusão de horário bloqueado + casos de borda (intervalo inválido, dono, 404) da SPEC-005 |
| RF-18 | `CouponServiceTest` — criação/validação de cupom + casos de borda (reuso, dono, expirado, código duplicado, serviço errado) da SPEC-006 |
| RF-19 | `InteractionServiceTest` — 3 cenários + casos de borda (pedido legado, role incompatível) da SPEC-009 |
| RF-20 | `ReportServiceTest` — 3 cenários + casos de borda (denúncia duplicada pendente reaproveitada, auto-denúncia bloqueada) da SPEC-010 |
| (infra) | `ServioApplicationTests` — carga do contexto Spring |

Total: **65 testes automatizados** cobrindo 10 dos 11 requisitos funcionais
novos além da base (RF-21 ainda sem teste automatizado).
