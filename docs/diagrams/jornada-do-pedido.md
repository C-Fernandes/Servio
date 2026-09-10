# Diagramas — Jornada do Pedido (RF-13)

Diagramas do requisito **RF-13**, produzidos em Mermaid com apoio de IA e
revisados pela dupla. Referências: [SPEC-002](../specs/SPEC-002-jornada-do-pedido.md) ·
[ADR-003](../adr/ADR-003-historico-de-status-e-jornada-do-pedido.md).

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09

---

## 1. Máquina de estados do pedido (após a SPEC-002)

```mermaid
stateDiagram-v2
    [*] --> PENDING: cliente cria o pedido

    PENDING --> CONFIRMED: prestador aceita
    PENDING --> CANCELLED: cliente ou prestador cancela
    CONFIRMED --> IN_PROGRESS: prestador inicia
    CONFIRMED --> CANCELLED: prestador cancela
    IN_PROGRESS --> COMPLETED: prestador conclui

    COMPLETED --> [*]
    CANCELLED --> [*]

    note right of PENDING
        Toda transição (incluindo a criação)
        grava um OrderStatusHistory
        (status + changedAt + changedByRole)
    end note
```

---

## 2. Etapas canônicas da jornada

```mermaid
flowchart LR
    R["Solicitado<br/>(PENDING)"] --> A["Aceito<br/>(CONFIRMED)"]
    A --> P["Em andamento<br/>(IN_PROGRESS)"]
    P --> C["Concluído<br/>(COMPLETED)"]

    X(["Cancelado<br/>(CANCELLED)"])
    R -.->|cancelamento| X
    A -.->|cancelamento| X

    classDef done fill:#dcfce7,stroke:#22c55e,color:#166534;
    classDef current fill:#e0e7ff,stroke:#6366f1,color:#3730a3;
    classDef cancel fill:#fee2e2,stroke:#ef4444,color:#991b1b;

    class R,A done
    class P current
    class X cancel
```

> Estados por etapa retornados pela API: `DONE` (concluída), `CURRENT` (etapa
> atual), `PENDING` (futura), `SKIPPED` (pulada por cancelamento).

---

## 3. Consulta da jornada — `GET /orders/{id}/journey`

```mermaid
sequenceDiagram
    autonumber
    actor U as Cliente ou Prestador
    participant SPA as Angular (OrderJourneyComponent)
    participant API as OrderController
    participant SVC as OrderJourneyService
    participant OR as OrderRepository
    participant HR as OrderStatusHistoryRepository
    participant DB as PostgreSQL

    U->>SPA: abre "Ver jornada" / detalhe do pedido
    SPA->>API: GET /orders/{id}/journey (Bearer JWT)
    API->>SVC: getJourney(id)
    SVC->>OR: findById(id)
    alt pedido não existe
        SVC-->>API: 404 Not Found
    else pedido existe
        SVC->>SVC: ensureParticipant(usuário, pedido)
        alt não é cliente/prestador do pedido nem ADMIN
            SVC-->>API: 403 Forbidden
        else participante
            SVC->>HR: findByOrderIdOrderByChangedAtAsc(id)
            HR->>DB: SELECT ... ORDER BY changed_at
            DB-->>HR: eventos de histórico
            alt sem histórico (pedido legado)
                SVC->>SVC: reconstrói a partir do status atual + createdAt
            else com histórico
                SVC->>SVC: mapeia cada status para a etapa e o carimbo de data
            end
            SVC-->>API: OrderJourneyResponseDTO (steps + cancelledAt)
            API-->>SPA: 200 OK
            SPA-->>U: linha do tempo com etapas, datas e destaque da atual
        end
    end
```
