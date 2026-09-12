# [ADR-009] Modelagem do Relatório de Desempenho do Prestador

* **Status**: Aceito
* **Data**: 2026-09-12
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema

O RF-15 exige um ranking dos próprios serviços do prestador por pedidos
concluídos, com avaliação média e quantidade de avaliações de cada um. Era
preciso decidir onde e como agregar esses dados, que já existem espalhados
entre `Order` e `Review`, sem duplicar informação nem introduzir uma tabela
só para servir um relatório.

## 2. Direcionadores de Decisão (Drivers)

* **Sem nova tabela**: os dados-fonte (pedidos concluídos, notas) já existem;
  criar uma tabela de resumo geraria um segundo lugar de verdade a manter
  sincronizado.
* **Consistência com o restante do dashboard financeiro**: o painel já expõe
  `ProviderFinancialDashboardResponseDTO` a partir de `Order`; o relatório de
  desempenho é mais uma visão sobre a mesma base, não um domínio novo.
* **Baixo volume, leitura pouco frequente**: o relatório é consultado sob
  demanda pelo próprio prestador, não em tempo real por terceiros — não
  justifica cache ou pré-cálculo.
* **Reuso de lógica já testada**: a média de avaliação e a contagem de
  avaliações por serviço já são calculadas em `ReviewService` para a página de
  detalhes do serviço; o relatório reaproveita os mesmos métodos em vez de
  recalcular.

## 3. Decisão Considerada e Aprovada

1. **Agregação sob demanda via `OrderRepository.countCompletedOrdersGroupedByService`**
   (consulta agrupada por serviço, filtrando pedidos `COMPLETED` do prestador
   autenticado), sem tabela ou coluna nova.
2. **`FinancialDashboardService.getTopServices()` monta o DTO combinando o
   agregado de pedidos com `ReviewService.getAverageRatingByServiceId` e
   `getReviewCountByServiceId`** — reuso direto do serviço de avaliações já
   existente, em vez de duplicar a query de média em `Order`/`Review`.
3. **Relatório vive no `FinancialDashboardService`**, junto do dashboard
   financeiro do prestador, em vez de um serviço/controller novo — mesma
   dependência de `AuthService` e mesmo guard de acesso (`providerProfile ==
   null` → `BusinessException`) já usado nos outros métodos da classe.
4. **Escopo restrito ao próprio prestador autenticado**: a consulta agrupada
   já recebe o `providerId` do usuário logado, sem parâmetro de outro
   prestador — o mesmo padrão de "dono implícito pelo token" usado em
   `AvailabilityService`/`CouponService`.

## 4. Consequências

### Positivas:
* Nenhuma tabela nova para manter sincronizada — o relatório reflete `Order`
  e `Review` em tempo real.
* Reuso de `ReviewService` evita duas implementações divergentes de "média de
  avaliação por serviço" no código.
* Implementação pequena e testável isoladamente (`FinancialDashboardServiceTest`).

### Negativas / Trade-offs:
* A consulta agrupada roda por completo a cada chamada — aceitável no volume
  atual (poucos serviços por prestador), mas não escala para um catálogo
  muito grande sem paginação ou cache.
* `getTopServices` faz uma chamada a `ReviewService` por serviço do
  resultado (N+1 lógico) em vez de uma única consulta agregada de avaliações
  — simplicidade de código em troca de mais idas ao banco.

## 5. Alternativas Consideradas

* **Tabela de resumo (`ServicePerformanceSummary`) recalculada por trigger ou
  evento ao concluir pedido**: descartada — adiciona complexidade de
  consistência (o resumo pode ficar desatualizado se o evento falhar) para um
  relatório de baixo volume que a consulta direta já atende.
* **Cache com recomputação periódica (job agendado)**: descartada como
  prematura; não há indício de carga que justifique cache antes de medir o
  comportamento em produção.
* **Nova consulta agregada trazendo também a média de avaliação em uma única
  query (join com `Review`)**: considerada, mas descartada nesta primeira
  versão para não duplicar a lógica de agregação de nota que já vive em
  `ReviewService` — fica como possível otimização futura se o N+1 se tornar
  um problema real.
