# [SPEC-010] Recomendações de Serviços por Perfil/Interesse do Cliente

* **Autora**: Bianca Antonelly
* **Data da implementação**: 2026-09-12
* **Data desta especificação**: 2026-09-12 (**retroativa** — ver seção 7)
* **Status**: Aprovado (retroativo)
* **Requisito do Projeto**: Requisito Funcional 20 (RF-20)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é recomendar serviços do marketplace Servio a cada cliente, com base no perfil de
interesse dele, sem depender de infraestrutura de Machine Learning externa.
Restrições de Comportamento:
- O sinal de interesse do cliente vem de dados que já existem no sistema: serviços favoritados e
  serviços já contratados (pedidos).
- Serviços com os quais o cliente já tem alguma relação (já favoritou ou já contratou) não devem
  ser recomendados novamente.
- Só serviços ativos e não excluídos entram como candidatos.
- Um cliente novo, sem favoritos nem pedidos, ainda deve receber recomendações — não pode ficar
  com uma lista vazia por falta de sinal de interesse.
- A funcionalidade é exclusiva de clientes.
```

---

## 2. Contexto e Motivação

Com o catálogo crescendo (favoritos, busca avançada, múltiplas categorias), o
cliente que já demonstrou interesse em um tipo de serviço não tinha nenhum
atalho para descobrir outros parecidos — precisava repetir manualmente os
mesmos filtros de busca. As **Recomendações** usam os sinais que o próprio
sistema já coleta (favoritos e pedidos) para sugerir proativamente serviços
similares, sem exigir nenhuma ação extra do cliente.

---

## 3. Requisitos Funcionais

* **RF-20.1 (Recomendação por interesse)**: o sistema recomenda serviços
  ativos cuja categoria ou tags coincidem com as dos serviços que o cliente já
  favoritou ou contratou.
* **RF-20.2 (Exclusão do já conhecido)**: serviços já favoritados ou já
  contratados pelo cliente não aparecem nas recomendações.
* **RF-20.3 (Fallback para cliente novo)**: sem nenhum sinal de interesse
  (cliente sem favoritos e sem pedidos), o sistema recomenda os serviços mais
  bem avaliados do catálogo.
* **RF-20.4 (Exclusividade de perfil)**: apenas usuários com role `CLIENT`
  recebem recomendações.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: Cliente com favorito em uma categoria recebe recomendações da mesma categoria
```gherkin
Dado que o cliente "Bianca" favoritou um serviço da categoria "Beleza"
E existem outros serviços ativos da categoria "Beleza" que ela nunca favoritou nem contratou
Quando "Bianca" consulta "GET /services/recommendations"
Então o sistema deve retornar status HTTP 200 (OK)
E os serviços da categoria "Beleza" devem aparecer entre as primeiras posições da lista
```

### Cenário 2: Serviço já favoritado não é recomendado novamente
```gherkin
Dado que o cliente "Bianca" já favoritou o serviço "Corte de Cabelo"
Quando "Bianca" consulta "GET /services/recommendations"
Então o serviço "Corte de Cabelo" não deve aparecer na lista de recomendações
```

### Cenário 3: Cliente novo, sem favoritos nem pedidos, recebe recomendações por popularidade
```gherkin
Dado que o cliente "Carlos" acabou de se cadastrar e nunca favoritou nem contratou nada
Quando "Carlos" consulta "GET /services/recommendations"
Então o sistema deve retornar status HTTP 200 (OK) com uma lista não vazia (se houver serviços
  ativos cadastrados)
E os serviços devem estar ordenados pela melhor avaliação média
```

### Caso de Borda 1 (Edge Case — Perfil incompatível):
```gherkin
Dado um usuário autenticado com role "PROVIDER"
Quando ele tenta consultar "GET /services/recommendations"
Então o sistema deve recusar com status HTTP 403 (Forbidden) ou 400 (Bad Request),
  informando que recomendações são exclusivas para clientes
```

### Caso de Borda 2 (Edge Case — Nenhum serviço ativo disponível):
```gherkin
Dado que não existe nenhum serviço ativo e não excluído no catálogo
Quando um cliente consulta "GET /services/recommendations"
Então o sistema deve retornar status HTTP 200 (OK) com uma lista vazia, sem erro
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/services/recommendations` | CLIENT | `200 OK` (List<ServiceResponseDTO>, no máximo 8 itens) | 400 (perfil não é cliente) |

---

## 6. Plano de Tarefas (Tasklist)

- [x] **T1 (Coleta de sinal de interesse)**: reaproveitar `FavoriteRepository` e `OrderRepository` para extrair categorias e tags de interesse do cliente.
- [x] **T2 (Pontuação)**: implementar `interestScoreOf` (peso maior para categoria, menor para tag) em `ServiceService`.
- [x] **T3 (Fallback de popularidade)**: quando não há sinal de interesse, ordenar por avaliação média.
- [x] **T4 (Camada de Controller)**: adicionar `GET /services/recommendations` ao `ServiceController`.
- [x] **T5 (Frontend — seção no marketplace)**: seção "Recomendados para você", visível só para clientes e só sem filtros ativos, com scroll horizontal.
- [ ] **T6 (Testes automatizados)**: cobrir `ServiceService.getRecommendations`, incluindo o fallback de cliente novo — **pendente, gap reconhecido**.

---

## 7. Nota de Transparência (Retroatividade)

Escrita após a implementação. A funcionalidade foi validada manualmente via
chamada direta à API (confirmando a filtragem de serviços já conhecidos e o
fallback de popularidade) e visualmente no marketplace, mas não recebeu
testes automatizados nem ADR dedicado antes de ser proposta em Pull Request —
mesmo padrão de gap das SPEC-007, SPEC-008 e SPEC-009. Não há uma decisão de
arquitetura nova o suficiente para justificar um ADR: a pontuação por
categoria/tag é feita em memória, reaproveitando entidades e repositórios já
existentes no projeto.
