# Requisitos Funcionais — Servio

Levantamento dos requisitos funcionais (RF) do sistema, para a apresentação da
disciplina **Desenvolvimento de Software com IA** (PPGTI / UFRN). A especificação
exige no mínimo **10 RF claramente identificáveis e demonstráveis** (seção III).

O sistema atende **13 RF**. Os requisitos **RF-12 (Favoritos)** e
**RF-13 (Jornada do Pedido)** são novos, implementados neste projeto seguindo o
ciclo de Spec-Driven Development (ver [SPEC-001](specs/SPEC-001-sistema-de-favoritos.md)
e [SPEC-002](specs/SPEC-002-jornada-do-pedido.md)).

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

### RF-05 — Busca e filtro de serviços
Filtro por categoria, preço máximo, avaliação mínima e busca textual por título/descrição.
- **Evidência:** filtros da página "Explorar serviços" (marketplace).

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

---

## Requisitos novos planejados (se houver tempo até 12/09)

| ID | Requisito | Status |
| :--- | :--- | :--- |
| RF-14 | Notificações de mudança de status do pedido | não iniciado |
| RF-15 | Página pública de perfil do prestador (serviços + avaliações) | não iniciado |

---

## Cobertura de testes automatizados

| RF | Teste |
| :--- | :--- |
| RF-12 | `FavoriteServiceTest` — 3 cenários Gherkin + 3 casos de borda da SPEC-001 |
| RF-13 | `OrderJourneyServiceTest` — 3 cenários + casos de borda (cancelado, legado, acesso indevido, 404) da SPEC-002 |
| RF-13 | `OrderServiceTest` — registro de histórico e validação das novas transições de status |
| (infra) | `ServioApplicationTests` — carga do contexto Spring |

Total: **20 testes automatizados** cobrindo 2 requisitos funcionais novos.
