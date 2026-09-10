# Diagramas — Notificações de Mudança de Status (RF-14)

Diagramas do requisito **RF-14**, produzidos em Mermaid com apoio de IA e
revisados pela dupla. Referências: [SPEC-003](../specs/SPEC-003-notificacoes-de-status.md) ·
[ADR-004](../adr/ADR-004-notificacoes-in-app-melhor-esforco.md).

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09

---

## 1. Geração da notificação na mudança de status

```mermaid
sequenceDiagram
    autonumber
    actor A as Autor (prestador / cliente / admin)
    participant OC as OrderController
    participant OS as OrderService (tx principal)
    participant HR as OrderStatusHistoryRepository
    participant NS as NotificationService (tx REQUIRES_NEW)
    participant NR as NotificationRepository
    participant DB as PostgreSQL

    A->>OC: PATCH /orders/{id}/status
    OC->>OS: updateStatus(id, novoStatus)
    OS->>OS: valida transição
    OS->>DB: UPDATE orders SET status = ...
    OS->>HR: save(OrderStatusHistory)

    OS->>NS: notifyOrderStatusChange(order, novoStatus, papelDoAutor)
    activate NS
    NS->>NS: resolve destinatários (participantes ≠ autor)
    loop para cada destinatário
        NS->>NR: save(Notification: recipient, message, order)
        NR->>DB: INSERT INTO notifications
    end
    NS-->>OS: ok
    deactivate NS

    alt notificação falhou
        OS->>OS: log.warn (melhor esforço) e segue
    end

    OS-->>OC: OrderResponseDTO
    OC-->>A: 200 OK
```

---

## 2. Regra de destinatário

```mermaid
flowchart TD
    C{Quem mudou<br/>o status?}
    C -->|PROVIDER| N1[Notifica o cliente]
    C -->|CLIENT| N2[Notifica o prestador]
    C -->|ADMIN| N3[Notifica cliente e prestador]

    N1 --> M["message = 'Seu pedido \"&lt;serviço&gt;\" mudou para: &lt;status&gt;'"]
    N2 --> M
    N3 --> M
```

> O autor da mudança **nunca** recebe notificação da própria ação.

---

## 3. Exibição no cliente (sino da sidebar)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário
    participant SPA as Angular (NotificationBell)
    participant RT as Router
    participant NAPI as /notifications

    Note over SPA: ao montar e a cada NavigationEnd
    RT-->>SPA: NavigationEnd
    SPA->>NAPI: GET /notifications/unread-count
    NAPI-->>SPA: { count }
    SPA-->>U: badge no sino

    U->>SPA: clica no sino
    SPA->>NAPI: GET /notifications
    NAPI-->>SPA: lista (recentes primeiro)
    SPA-->>U: dropdown com as notificações

    U->>SPA: clica numa notificação não lida
    SPA->>NAPI: PATCH /notifications/{id}/read
    SPA->>NAPI: GET /notifications/unread-count
    SPA-->>U: navega para os pedidos do perfil
```
