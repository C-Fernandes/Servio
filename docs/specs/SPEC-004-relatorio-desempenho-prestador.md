# [SPEC-004] Relatório de Desempenho do Prestador

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-12
* **Status**: Aprovado
* **Requisito do Projeto**: RF-15 (item 7 da proposta de checkpoint — "Relatório de desempenho para prestadores, com serviços mais contratados e avaliações médias")

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é especificar e implementar o Relatório de Desempenho do Prestador no marketplace Servio.
Restrições de Comportamento:
- Reaproveitar os dados já existentes (Order, Review); não criar nova tabela.
- Ranking por número de pedidos concluídos (COMPLETED) por serviço, do prestador autenticado.
- Cada item do ranking traz também a avaliação média e a quantidade de avaliações do serviço.
- Apenas prestadores acessam o relatório; sem pedidos concluídos, retorna lista vazia (não erro).
- Cubra com teste automatizado.
```

## 2. Contexto e Motivação

O painel do prestador já mostra ganhos totais e ticket médio
(`FinancialDashboardService`), mas não qual **serviço específico** está performando
melhor. O relatório de desempenho fecha essa lacuna reaproveitando `Order`
(contagem de concluídos por serviço) e `Review` (média e quantidade de
avaliações, já usados pelo RF-12/marketplace).

## 3. Requisitos Funcionais

* **RF-15.1**: o prestador autenticado consulta um ranking de seus serviços por número de pedidos concluídos.
* **RF-15.2**: cada item do ranking traz título do serviço, quantidade de pedidos concluídos, avaliação média e quantidade de avaliações.
* **RF-15.3**: prestador sem pedidos concluídos recebe lista vazia (200 OK), não erro.

## 4. Critérios de Aceite (Gherkin)

### Cenário 1: prestador com serviços concluídos vê o ranking ordenado
```gherkin
Dado que o prestador "João" tem 5 pedidos concluídos do serviço "Limpeza" e 2 do serviço "Pintura"
Quando "João" consulta "GET /financial-dashboard/provider/top-services"
Então a resposta deve trazer "Limpeza" antes de "Pintura"
E cada item deve conter a avaliação média e a quantidade de avaliações do serviço
```

### Caso de Borda 1 (Edge Case — Sem pedidos concluídos):
```gherkin
Dado que o prestador "Ana" não tem nenhum pedido concluído
Quando "Ana" consulta "GET /financial-dashboard/provider/top-services"
Então o sistema deve retornar 200 (OK) com lista vazia
```

### Caso de Borda 2 (Edge Case — Usuário sem perfil de prestador):
```gherkin
Dado um usuário autenticado sem perfil de prestador
Quando ele consulta "GET /financial-dashboard/provider/top-services"
Então o sistema deve recusar com 400 (Bad Request)
```

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta | Erros |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/financial-dashboard/provider/top-services` | PROVIDER | `200 OK` (`List<TopServiceResponseDTO>`, ordenado por pedidos concluídos desc) | 400 (sem perfil de prestador) |

`TopServiceResponseDTO`: `serviceId`, `title`, `completedOrders`, `averageRating`, `reviewCount`.

## 6. Plano de Tarefas

- [x] **T1**: `OrderRepository.countCompletedOrdersGroupedByService` (JPQL agrupado).
- [x] **T2**: `TopServiceResponseDTO`.
- [x] **T3**: `FinancialDashboardService.getTopServices()` (reaproveita `ReviewService`).
- [x] **T4**: endpoint em `FinancialDashboardController`.
- [x] **T5**: `FinancialDashboardServiceTest` — cenário + 2 casos de borda.
- [x] **T6**: frontend — `financial-dashboard.service.ts` + bloco "Serviços mais contratados" no painel do prestador.
- [ ] **T7**: homologação (demo).
