# [SPEC-003] Notificações de Mudança de Status do Pedido

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09
* **Status**: Aprovado
* **Requisito do Projeto**: RF-14 (item 6 da proposta de checkpoint — "Notificações para mudanças de status dos pedidos")
* **SPEC Relacionada**: [SPEC-002 — Jornada do Pedido](SPEC-002-jornada-do-pedido.md) (reaproveita o ponto de mudança de status)
* **ADR Relacionada**: [ADR-004 — Notificações In-App com Geração em Melhor Esforço](../adr/ADR-004-notificacoes-in-app-melhor-esforco.md)
* **Diagramas**: [notificacoes-de-status.md](../diagrams/notificacoes-de-status.md)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é especificar e implementar Notificações de Mudança de Status do Pedido no marketplace Servio.
Restrições de Comportamento:
- Uma notificação é criada para cada participante do pedido afetado pela mudança de status, EXCETO para quem fez a mudança.
- Notificação é in-app (persistida no banco); não há envio de e-mail ou push.
- Cada notificação pertence a um único usuário destinatário e nasce como não lida.
- O usuário só enxerga e altera as próprias notificações; acesso a notificação de terceiro retorna 403.
- Marcar como lida é idempotente: marcar de novo não gera erro nem altera a data.
- Não bloquear a mudança de status caso a criação da notificação falhe (melhor esforço), mas registrar log.
- Cubra explicitamente os casos de borda com testes automatizados.
```

---

## 2. Contexto e Motivação

Quando o prestador avança um pedido (`aceitar`, `iniciar`, `concluir`) ou o
cliente cancela, a outra parte só descobre a mudança se abrir a tela de pedidos e
conferir manualmente. Não há aviso.

Com a **SPEC-002** o sistema já registra cada transição em `OrderStatusHistory`.
Esta SPEC usa **o mesmo ponto** (`OrderService.updateStatus`) para gerar uma
**notificação in-app** ao participante afetado, exibida num sino com contador de
não lidas e numa lista.

---

## 3. Requisitos Funcionais

* **RF-14.1 (Gerar notificação)**: toda mudança de status de um pedido cria uma notificação para cada participante do pedido (cliente e prestador) que **não** seja o autor da mudança.
* **RF-14.2 (Listar)**: o usuário autenticado lista suas notificações, mais recentes primeiro.
* **RF-14.3 (Contador de não lidas)**: o sistema informa quantas notificações não lidas o usuário tem (para o badge do sino).
* **RF-14.4 (Marcar como lida)**: o usuário marca uma notificação como lida; operação idempotente.
* **RF-14.5 (Marcar todas como lidas)**: o usuário marca todas as suas notificações como lidas de uma vez.
* **RF-14.6 (Exibição)**: um sino no layout mostra o contador de não lidas e abre a lista; ao abrir uma notificação de pedido, o usuário é levado ao pedido correspondente.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: prestador avança o status e o cliente é notificado
```gherkin
Dado um pedido da cliente "Maria" no status "PENDING"
Quando o prestador do pedido muda o status para "CONFIRMED"
Então deve existir uma notificação não lida para "Maria"
E a mensagem deve citar o serviço e o novo status ("Aceito")
E o prestador (autor da mudança) não deve receber notificação
```

### Cenário 2: cliente cancela e o prestador é notificado
```gherkin
Dado um pedido no status "PENDING"
Quando a cliente muda o status para "CANCELLED"
Então deve existir uma notificação não lida para o prestador do pedido
E a cliente (autora da mudança) não deve receber notificação
```

### Cenário 3: usuário lista suas notificações e vê o contador
```gherkin
Dado que "Maria" tem 3 notificações, sendo 2 não lidas
Quando "Maria" consulta "GET /notifications"
Então a resposta deve conter as 3 notificações, mais recentes primeiro
E "GET /notifications/unread-count" deve retornar 2
```

### Cenário 4: marcar como lida reduz o contador
```gherkin
Dado que "Maria" tem 2 notificações não lidas
Quando "Maria" envia "PATCH /notifications/{id}/read" para uma delas
Então "GET /notifications/unread-count" deve retornar 1
E a notificação marcada deve constar como lida
```

### Caso de Borda 1 (Edge Case — Mudança feita por ADMIN):
```gherkin
Dado um pedido com cliente "Maria" e prestador "João"
Quando um ADMIN muda o status do pedido
Então tanto "Maria" quanto "João" devem receber uma notificação não lida
```

### Caso de Borda 2 (Edge Case — Acesso a notificação de terceiro):
```gherkin
Dado que existe uma notificação pertencente a "Maria"
Quando outro usuário autenticado tenta "PATCH /notifications/{id}/read" nessa notificação
Então o sistema deve responder 403 (Forbidden)
E a notificação não deve ser alterada
```

### Caso de Borda 3 (Edge Case — Marcar como lida de novo):
```gherkin
Dado que "Maria" já marcou uma notificação como lida às 10:00
Quando "Maria" envia "PATCH /notifications/{id}/read" novamente
Então o sistema deve responder 200 (OK)
E a data de leitura deve continuar 10:00 (operação idempotente)
```

### Caso de Borda 4 (Edge Case — Marcar todas sem ter nenhuma):
```gherkin
Dado que "Maria" não tem nenhuma notificação
Quando "Maria" envia "PATCH /notifications/read-all"
Então o sistema deve responder 200 (OK) sem erro
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/notifications` | autenticado | `200 OK` (`List<NotificationResponseDTO>`, recentes primeiro) | — |
| `GET` | `/notifications/unread-count` | autenticado | `200 OK` (`{ "count": number }`) | — |
| `PATCH` | `/notifications/{id}/read` | dono da notificação | `200 OK` (`NotificationResponseDTO`) | 404 (não existe), 403 (não é dono) |
| `PATCH` | `/notifications/read-all` | autenticado | `200 OK` (`{ "updated": number }`) | — |

**`NotificationResponseDTO`:**
```jsonc
{
  "id": 12,
  "message": "Seu pedido \"Limpeza Residencial\" mudou para: Aceito",
  "orderId": 30,
  "read": false,
  "createdAt": "2026-09-09T14:00:00"
}
```

---

## 6. Modelo de Dados

Nova entidade **`Notification`**:

| Campo | Tipo | Observação |
| :--- | :--- | :--- |
| `id` | bigint PK IDENTITY | |
| `recipient_id` | bigint FK → `users` | não nulo |
| `message` | varchar(280) | texto pronto para exibição |
| `order_id` | bigint FK → `orders`, nullable | contexto (permite navegar ao pedido) |
| `read` | boolean | default `false` |
| `read_at` | timestamp, nullable | preenchido na primeira leitura |
| `created_at` | timestamp | |

Consultas: `findByRecipientIdOrderByCreatedAtDesc`, `countByRecipientIdAndReadFalse`,
`markAllAsReadByRecipientId` (update em lote).

---

## 7. Regras de Destinatário

Na mudança de status do pedido, gerar uma notificação para cada um destes que **não** seja o autor:

| Autor da mudança | Notifica |
| :--- | :--- |
| PROVIDER | o cliente do pedido |
| CLIENT | o prestador do pedido (via `provider.user`) |
| ADMIN | o cliente **e** o prestador |

Mensagem: `Seu pedido "<serviceTitle>" mudou para: <rótulo do status>`
(rótulos: Solicitado, Aceito, Em andamento, Concluído, Cancelado).

---

## 8. Plano de Tarefas (Tasklist)

- [x] **T1 (Modelo)**: entidade `Notification` (coluna `is_read`, `read_at`).
- [x] **T2 (Repository)**: `NotificationRepository` (`findByRecipientIdOrderByCreatedAtDesc`, `countByRecipientIdAndReadFalse`, `markAllAsRead` em lote).
- [x] **T3 (Serviço)**: `NotificationService` — `notifyOrderStatusChange` (REQUIRES_NEW), listar, contar não lidas, marcar como lida (checagem de dono → 403), marcar todas.
- [x] **T4 (Gatilho)**: em `OrderService.updateStatus`, após o histórico, chama `NotificationService` em `try/catch` + log (melhor esforço).
- [x] **T5 (Controller + DTOs)**: `NotificationController` (4 endpoints); `NotificationResponseDTO`.
- [x] **T6 (Testes)**: `NotificationServiceTest` (10) cobrindo os 4 cenários + 4 casos de borda. Suíte: 30 verdes.
- [x] **T7 (Frontend — serviço)**: `NotificationService` no Angular (`list`, `fetchUnreadCount`, `markRead`, `markAllRead`, signal `unreadCount`).
- [x] **T8 (Frontend — sino)**: `NotificationBellComponent` no topo da sidebar — badge de não lidas, dropdown com a lista, "marcar todas como lidas", clique marca como lida e leva aos pedidos do perfil.
- [x] **T9 (Frontend — atualização)**: `refreshUnread()` no mount e a cada `NavigationEnd` do Router (sem WebSocket/polling).
- [x] **T10 (Doc)**: [ADR-004](../adr/ADR-004-notificacoes-in-app-melhor-esforco.md); `docs/requisitos-funcionais.md` (RF-14); [diagrama de notificações](../diagrams/notificacoes-de-status.md).
- [ ] **T11 (Homologação)**: validar todos os critérios de aceite (demo).

---

## 9. Fora de Escopo

* Envio por e-mail, push ou WebSocket (tempo real).
* Preferências de notificação por usuário (silenciar tipos).
* Notificações de outros eventos (nova avaliação, novo favorito etc.).

---

## 10. Decisões da Revisão (2026-09-09)

1. **Melhor esforço**: a geração da notificação é envolvida em `try/catch` com
   log; uma falha **não** reverte a mudança de status do pedido.
2. **Atualização do contador no front**: refetch ao navegar entre telas e após
   ações que mudam status de pedido. Sem polling/timer.
3. **Sino no topo da sidebar** (componente já presente em todas as telas
   autenticadas).
4. Mensagens em português fixo no `NotificationService` (sem i18n por ora).
