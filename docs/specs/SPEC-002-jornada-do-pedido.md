# [SPEC-002] Acompanhamento da Jornada do Pedido

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09
* **Status**: Aprovado
* **Requisito do Projeto**: RF-13 (item 8 da proposta de checkpoint — "Área de acompanhamento da jornada do pedido")
* **ADR Relacionada**: [ADR-003 — Histórico de Status e Jornada do Pedido](../adr/ADR-003-historico-de-status-e-jornada-do-pedido.md)
* **Diagramas**: [jornada-do-pedido.md](../diagrams/jornada-do-pedido.md)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é especificar e implementar o Acompanhamento da Jornada do Pedido no marketplace Servio.
Restrições de Comportamento:
- A jornada tem quatro etapas canônicas nesta ordem: Solicitado, Aceito, Em andamento, Concluído.
- "Cancelado" é um estado terminal alternativo que pode ocorrer a partir de Solicitado ou Aceito.
- Cada transição de status registra um evento com data/hora; a etapa "Solicitado" é registrada na criação do pedido.
- A jornada só pode ser consultada pelo cliente dono do pedido, pelo prestador do pedido ou por um ADMIN.
- Pedidos criados antes desta funcionalidade (sem histórico) devem ter a jornada reconstruída a partir do status atual e da data de criação, sem erro.
- Não alterar valores monetários, agendamento ou regras de disponibilidade existentes.
- Cubra explicitamente os casos de borda com testes automatizados.
```

---

## 2. Contexto e Motivação

Hoje o `Order` guarda apenas o **status atual** (`PENDING`, `CONFIRMED`,
`IN_PROGRESS`, `COMPLETED`, `CANCELLED`) e a data de criação. O cliente não
consegue ver **quando** cada etapa aconteceu nem qual é o próximo passo; o
prestador não tem uma linha do tempo do atendimento.

Além disso há uma inconsistência: o front-end já modela a etapa **`CONFIRMED`
("Aceito")** — coluna no Kanban, mapeamento de status — mas o back-end
(`OrderService.validateProviderStatusTransition`) **não permite** chegar a esse
status (só aceita `PENDING → IN_PROGRESS`). Esta SPEC também corrige isso.

A **Jornada do Pedido** dá ao cliente e ao prestador uma visão clara do
andamento: etapas concluídas com carimbo de data/hora, etapa atual destacada e
etapas futuras previstas.

---

## 3. Requisitos Funcionais

* **RF-13.1 (Registro de histórico)**: toda mudança de status do pedido — incluindo a criação — grava um evento com o status resultante e o instante da mudança.
* **RF-13.2 (Etapa "Aceito" real)**: o fluxo do prestador passa a ser `Solicitado → Aceito → Em andamento → Concluído`. O prestador aceita um pedido pendente (`PENDING → CONFIRMED`) antes de iniciá-lo (`CONFIRMED → IN_PROGRESS`).
* **RF-13.3 (Consulta da jornada)**: cliente, prestador ou admin do pedido consultam a jornada — lista ordenada das quatro etapas canônicas, cada uma com seu estado (`DONE`, `CURRENT`, `PENDING`, `SKIPPED`) e a data/hora em que foi atingida (quando aplicável).
* **RF-13.4 (Jornada de pedido cancelado)**: quando o pedido é cancelado, a jornada mostra as etapas já cumpridas como `DONE`, um evento `CANCELLED` com data/hora, e as etapas seguintes como `SKIPPED`.
* **RF-13.5 (Visualização)**: no detalhe do pedido (cliente e prestador) é exibida uma linha do tempo com as etapas, marcadores de concluído/atual/futuro e as datas.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: criação do pedido registra a etapa "Solicitado"
```gherkin
Dado que a cliente "Maria" cria um pedido para um serviço ativo
Quando o pedido é persistido com status "PENDING"
Então a jornada do pedido deve conter um evento "PENDING" com a data/hora da criação
E a etapa "Solicitado" deve aparecer como DONE
E a etapa "Aceito" deve aparecer como CURRENT
```

### Cenário 2: prestador avança o status e a jornada reflete a mudança
```gherkin
Dado um pedido no status "PENDING"
Quando o prestador do pedido muda o status para "CONFIRMED"
Então a jornada deve registrar um evento "CONFIRMED" com data/hora
E a etapa "Aceito" deve aparecer como DONE
E a etapa "Em andamento" deve aparecer como CURRENT
```

### Cenário 3: cliente consulta a jornada completa
```gherkin
Dado um pedido que passou por "PENDING", "CONFIRMED", "IN_PROGRESS" e "COMPLETED"
Quando a cliente dona do pedido consulta "GET /orders/{id}/journey"
Então o sistema deve responder 200 (OK)
E o corpo deve conter as quatro etapas na ordem canônica, todas como DONE
E cada etapa deve ter a data/hora correspondente
```

### Caso de Borda 1 (Edge Case — Pedido cancelado):
```gherkin
Dado um pedido no status "PENDING"
Quando o status muda para "CANCELLED"
Então a jornada deve conter a etapa "Solicitado" como DONE
E um evento "CANCELLED" com data/hora
E as etapas "Aceito", "Em andamento" e "Concluído" como SKIPPED
```

### Caso de Borda 2 (Edge Case — Pedido legado sem histórico):
```gherkin
Dado um pedido criado antes desta funcionalidade, sem nenhum evento de histórico, com status atual "IN_PROGRESS"
Quando alguém consulta a jornada desse pedido
Então o sistema deve reconstruir a jornada a partir do status atual
E marcar "Solicitado", "Aceito" e "Em andamento" como DONE (usando a data de criação como referência quando não houver data específica)
E "Concluído" como CURRENT
E não deve lançar erro
```

### Caso de Borda 3 (Edge Case — Acesso indevido):
```gherkin
Dado um usuário autenticado que não é o cliente nem o prestador do pedido e não é ADMIN
Quando ele consulta "GET /orders/{id}/journey"
Então o sistema deve responder 403 (Forbidden)
```

### Caso de Borda 4 (Edge Case — Transição inválida para "Aceito"):
```gherkin
Dado um pedido no status "IN_PROGRESS"
Quando o prestador tenta mudar o status para "CONFIRMED"
Então o sistema deve recusar com 400 (Bad Request) e mensagem de transição inválida
E a jornada não deve ganhar nenhum evento novo
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/orders/{id}/journey` | CLIENT dono / PROVIDER do pedido / ADMIN | `200 OK` (`OrderJourneyResponseDTO`) | 404 (pedido não existe), 403 (não participante) |
| `PATCH` | `/orders/{id}/status` | *(já existe)* | `200 OK` (`OrderResponseDTO`) | 400 (transição inválida) |

**Transições de status após esta SPEC (prestador):**

| De | Para permitido |
| :--- | :--- |
| `PENDING` | `CONFIRMED`, `CANCELLED` |
| `CONFIRMED` | `IN_PROGRESS`, `CANCELLED` |
| `IN_PROGRESS` | `COMPLETED` |
| `COMPLETED` / `CANCELLED` | *(terminal)* |

Cliente: continua só podendo `PENDING → CANCELLED` (regra atual inalterada).

**`OrderJourneyResponseDTO`:**
```jsonc
{
  "orderId": 10,
  "currentStatus": "IN_PROGRESS",
  "steps": [
    { "key": "REQUESTED",   "label": "Solicitado",   "state": "DONE",    "reachedAt": "2026-09-01T10:00:00" },
    { "key": "ACCEPTED",    "label": "Aceito",       "state": "DONE",    "reachedAt": "2026-09-01T12:30:00" },
    { "key": "IN_PROGRESS", "label": "Em andamento", "state": "CURRENT", "reachedAt": "2026-09-02T09:00:00" },
    { "key": "COMPLETED",   "label": "Concluído",    "state": "PENDING", "reachedAt": null }
  ],
  "cancelledAt": null
}
```

---

## 6. Modelo de Dados

Nova entidade **`OrderStatusHistory`**:

| Campo | Tipo | Observação |
| :--- | :--- | :--- |
| `id` | bigint PK IDENTITY | |
| `order_id` | bigint FK → `orders` | não nulo |
| `status` | varchar (enum `OrderStatus`) | status resultante da transição |
| `changed_at` | timestamp | instante da mudança |
| `changed_by_role` | varchar (enum `Role`), nullable | quem disparou (CLIENT/PROVIDER/ADMIN), nulo na criação/sistema |

Índice/consulta principal: `findByOrderIdOrderByChangedAtAsc`.

`Order` ganha `@OneToMany(mappedBy = "order")` para o histórico (somente leitura no
mapeamento) — ou consulta direta pelo repositório, a decidir na ADR.

---

## 7. Plano de Tarefas (Tasklist)

- [x] **T1 (Modelo)**: criar `OrderStatusHistory` + enum reuse; relacionamento com `Order`.
- [x] **T2 (Repository)**: `OrderStatusHistoryRepository` com `findByOrderIdOrderByChangedAtAsc`.
- [x] **T3 (Registro de histórico)**: em `OrderService.create` e `OrderService.updateStatus`, gravar um `OrderStatusHistory` após cada mudança de status.
- [x] **T4 (Transições)**: ajustar `validateProviderStatusTransition` para o fluxo `PENDING → CONFIRMED → IN_PROGRESS → COMPLETED` (+ cancelamentos).
- [x] **T5 (Serviço de Jornada)**: `OrderJourneyService` que monta `OrderJourneyResponseDTO` a partir do histórico, com fallback para pedidos legados.
- [x] **T6 (Controller)**: `GET /orders/{id}/journey` com verificação de participante (cliente/prestador/ADMIN) → 403 caso contrário.
- [x] **T7 (DTOs)**: `OrderJourneyResponseDTO`, `OrderJourneyStepDTO`.
- [x] **T8 (Testes)**: `OrderJourneyServiceTest` (8) + `OrderServiceTest` (3) cobrindo os 3 cenários + casos de borda.
- [x] **T9 (Frontend — serviço)**: `OrderService.getJourney(orderId)` no Angular + tipos em `models/Order.ts`.
- [x] **T10 (Frontend — componente)**: `OrderJourneyComponent` (linha do tempo com estados/datas e destaque para cancelado), embutido no `order-details-modal` (prestador) e no `dashboard-client` (cliente, via "Ver jornada").
- [x] **T11 (Frontend — ações)**: botão "Aceitar pedido" no card do Kanban para pedidos pendentes (`PENDING → CONFIRMED`); opção "Confirmado" já presente nos selects de status.
- [x] **T12 (Doc)**: [ADR-003](../adr/ADR-003-historico-de-status-e-jornada-do-pedido.md); `docs/requisitos-funcionais.md` (RF-13) e [diagrama da jornada](../diagrams/jornada-do-pedido.md).
- [ ] **T13 (Homologação)**: validar todos os critérios de aceite (demo).

---

## 8. Fora de Escopo

* Notificações ao cliente na mudança de status (é o RF de Notificações, SPEC futura).
* Permitir que o cliente reabra/edite etapas.
* Migração de dados retroativa (o fallback em runtime cobre pedidos legados).

---

## 9. Decisões da Revisão (2026-09-09)

1. **Etapa "Aceito" real**: fluxo do prestador passa a ser
   `PENDING → CONFIRMED → IN_PROGRESS → COMPLETED` (+ cancelamentos em `PENDING` e
   `CONFIRMED`). Inclui a ação "Aceitar" no painel do prestador (T11).
2. **Jornada via endpoint separado** `GET /orders/{id}/journey`; o
   `OrderResponseDTO` não é alterado.
3. **Cancelamento pelo cliente**: mantido apenas em `PENDING`. Sem mudança em
   `validateClientStatusTransition`.
