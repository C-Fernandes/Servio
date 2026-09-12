# Requisitos Funcionais — Servio

Levantamento dos requisitos funcionais (RF) do sistema, para a apresentação da
disciplina **Desenvolvimento de Software com IA** (PPGTI / UFRN). A especificação
exige no mínimo **10 RF claramente identificáveis e demonstráveis** (seção III).

O sistema atende **16 RF**. Os requisitos **RF-12 (Favoritos)**,
**RF-13 (Jornada do Pedido)**, **RF-14 (Notificações de Status)** e
**RF-15 (Relatório de Desempenho do Prestador)** foram implementados seguindo o
ciclo de Spec-Driven Development (ver [SPEC-001](specs/SPEC-001-sistema-de-favoritos.md),
[SPEC-002](specs/SPEC-002-jornada-do-pedido.md), [SPEC-003](specs/SPEC-003-notificacoes-de-status.md)
e [SPEC-004](specs/SPEC-004-relatorio-desempenho-prestador.md)).

O **RF-05 (Busca Avançada)** foi ampliado e o **RF-16 (Chat)** foi adicionado por
Bianca Antonelly, em paralelo, **fora do fluxo estrito de SDD** desta
documentação — sem SPEC/ADR/teste dedicados. Gap reconhecido explicitamente
como aprendizado do processo (ver seção V.7 da apresentação).

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
- **Nota SDD:** implementado por Bianca fora do fluxo estrito de SPEC/ADR/teste desta documentação (gap reconhecido).

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
- **Especificação:** [SPEC-002](specs/SPEC-002-jornada-do-pedido.md)
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
- **Especificação:** [SPEC-004](specs/SPEC-004-relatorio-desempenho-prestador.md)
- **Evidência (backend):** `GET /financial-dashboard/provider/top-services`; `FinancialDashboardServiceTest` (3).
- **Evidência (frontend):** tabela "Serviços mais contratados" na aba Finanças do painel do prestador.

### RF-16 — Chat entre cliente e prestador
Canal de mensagens entre cliente e prestador antes da contratação
(`Conversation`/`Message`), com página dedicada e link na sidebar.
- **Evidência:** `ChatController`/`ChatService`; página "Mensagens".
- **Nota SDD:** implementado por Bianca fora do fluxo estrito de SPEC/ADR/teste desta documentação (gap reconhecido, ver introdução).

---

## Cobertura de testes automatizados

| RF | Teste |
| :--- | :--- |
| RF-12 | `FavoriteServiceTest` — 3 cenários Gherkin + 3 casos de borda da SPEC-001 |
| RF-13 | `OrderJourneyServiceTest` — 3 cenários + casos de borda (cancelado, legado, acesso indevido, 404) da SPEC-002 |
| RF-13 | `OrderServiceTest` — registro de histórico e validação das novas transições de status |
| RF-14 | `NotificationServiceTest` — 4 cenários + casos de borda (ADMIN notifica ambos, 403, idempotência, marcar todas sem nenhuma, 404) da SPEC-003 |
| RF-15 | `FinancialDashboardServiceTest` — ranking ordenado + casos de borda (sem concluídos, sem perfil de prestador) da SPEC-004 |
| (infra) | `ServioApplicationTests` — carga do contexto Spring |

Total: **33 testes automatizados** cobrindo 4 requisitos funcionais novos (RF-05 e RF-16 ainda sem cobertura — gap reconhecido).
