# [SPEC-007] Busca Avançada de Serviços

* **Autora**: Bianca Antonelly
* **Data da implementação**: 2026-09-10
* **Data desta especificação**: 2026-09-12 (**retroativa** — escrita após a
  implementação, a partir do código e dos testes manuais já realizados;
  ver nota de transparência na seção 7)
* **Status**: Aprovado (retroativo)
* **Requisito do Projeto**: Requisito Funcional 5 (RF-05)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é ampliar a busca de serviços do marketplace Servio, hoje limitada a um filtro simples
client-side, para uma busca avançada resolvida no backend.
Restrições de Comportamento:
- A busca deve suportar termo textual (título/descrição), categoria, faixa de preço, avaliação
  mínima, localização (cidade/estado) e ordenação.
- Apenas serviços ativos e não excluídos podem aparecer nos resultados.
- A ordenação por avaliação depende de um cálculo agregado (média de reviews) que não pode ser
  delegado diretamente ao banco na mesma consulta que os demais filtros.
- Preço mínimo maior que o preço máximo é uma combinação inválida e deve ser rejeitada.
- Mantenha compatibilidade com o indicador de "favorito" já exibido nos cards.
```

---

## 2. Contexto e Motivação

O marketplace já permitia listar todos os serviços ativos (RF-04), mas o filtro por
categoria/preço/avaliação era feito inteiramente no frontend, sobre a lista já
carregada — não escalava e não permitia combinar critérios com busca textual.
A **Busca Avançada** move essa responsabilidade para o backend, usando
`Specification` do Spring Data para compor filtros dinamicamente, e adiciona
busca por localização e ordenação, tornando a descoberta de serviços viável
mesmo com um catálogo maior.

---

## 3. Requisitos Funcionais

* **RF-05.1 (Busca textual)**: o cliente pode buscar serviços por termo livre,
  que casa com título ou descrição.
* **RF-05.2 (Filtro por categoria)**: o cliente pode restringir os resultados a
  uma categoria específica.
* **RF-05.3 (Filtro por faixa de preço)**: o cliente pode informar preço
  mínimo e/ou máximo.
* **RF-05.4 (Filtro por avaliação mínima)**: o cliente pode exigir uma nota
  média mínima (3, 4 ou 5 estrelas).
* **RF-05.5 (Filtro por localização)**: o cliente pode restringir por cidade
  e estado do prestador.
* **RF-05.6 (Ordenação)**: o cliente pode ordenar por mais recentes, menor
  preço, maior preço, melhor avaliados ou ordem alfabética.
* **RF-05.7 (Combinação de filtros)**: todos os filtros acima podem ser
  usados em conjunto na mesma consulta.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: Busca por termo retorna apenas serviços correspondentes
```gherkin
Dado que existem os serviços "servico" e "servico 1" e outros sem essa palavra no título
Quando o cliente envia "GET /services/search?term=servico"
Então o sistema deve retornar status HTTP 200 (OK)
E o corpo deve conter exatamente os serviços cujo título ou descrição contém "servico"
```

### Cenário 2: Combinação de categoria e preço mínimo restringe corretamente
```gherkin
Dado que existem serviços em categorias diferentes e com preços diferentes
Quando o cliente envia "GET /services/search?categoryId=3&minPrice=500"
Então o sistema deve retornar apenas serviços da categoria 3 com preço maior ou igual a 500
```

### Cenário 3: Ordenação por melhor avaliados
```gherkin
Dado que existem serviços com médias de avaliação distintas
Quando o cliente envia "GET /services/search?sortBy=rating_desc"
Então o sistema deve retornar os serviços ordenados da maior para a menor média de avaliação
E o cálculo de ordenação deve ocorrer após a consulta principal, pois a média é agregada por serviço
```

### Caso de Borda 1 (Edge Case — Preço mínimo maior que o máximo):
```gherkin
Dado que o cliente informa minPrice=1000 e maxPrice=100
Quando o cliente envia "GET /services/search?minPrice=1000&maxPrice=100"
Então o sistema deve rejeitar a requisição com status HTTP 400 (Bad Request)
E a mensagem deve indicar "O preço mínimo não pode ser maior que o preço máximo."
```

### Caso de Borda 2 (Edge Case — Nenhum filtro aplicado):
```gherkin
Dado que o cliente não informa nenhum parâmetro de busca
Quando o cliente envia "GET /services/search"
Então o sistema deve retornar todos os serviços ativos e não excluídos, sem erro
```

### Caso de Borda 3 (Edge Case — Nenhum resultado):
```gherkin
Dado que nenhum serviço ativo casa com os filtros informados
Quando o cliente envia uma busca com essa combinação de filtros
Então o sistema deve retornar status HTTP 200 (OK) com uma lista vazia
E o frontend deve exibir "Nenhum serviço encontrado com esses filtros."
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Parâmetros | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `GET` | `/services/search` | CLIENT/PROVIDER/ADMIN | `term, categoryId, minPrice, maxPrice, minRating, city, state, sortBy` | `200 OK` (List<ServiceResponseDTO>) | 400 (minPrice > maxPrice) |
| `GET` | `/services/locations` | CLIENT/PROVIDER/ADMIN | — | `200 OK` (List<LocalityDTO>) | — |

---

## 6. Plano de Tarefas (Tasklist)

- [x] **T1 (DTO de filtros)**: Criar `ServiceSearchRequestDTO` com validação de preço.
- [x] **T2 (Specification)**: Criar `ServiceSpecifications.withFilters` compondo os predicados dinamicamente.
- [x] **T3 (Repository)**: Estender `ServiceRepository` com `JpaSpecificationExecutor` e query de localidades disponíveis.
- [x] **T4 (Serviço)**: Implementar `ServiceService.search`, incluindo a ordenação por avaliação pós-consulta.
- [x] **T5 (Controller)**: Adicionar `GET /services/search` e `GET /services/locations` ao `ServiceController`.
- [x] **T6 (Frontend — painel de filtros)**: Substituir o filtro client-side por chamadas reativas (debounce) ao endpoint de busca.
- [x] **T7 (Frontend — contador e estado vazio)**: Exibir contagem de resultados e mensagem de "nenhum resultado".
- [ ] **T8 (Testes automatizados)**: cobrir `ServiceService.search` e `ServiceSpecifications` com testes unitários — **pendente, gap reconhecido**.

---

## 7. Nota de Transparência (Retroatividade)

Esta especificação foi escrita **depois** da implementação e dos testes manuais
(via UI e chamadas diretas à API), não antes, como o fluxo de SDD do projeto
prevê (ver `ADR-001`). O código e o comportamento descritos aqui refletem o
que já está em produção na branch `main`; a lacuna reconhecida é a ausência de
testes automatizados (T8) e de um ADR dedicado, já que não houve uma decisão
de arquitetura relevante o suficiente para justificar um novo ADR (a
`Specification` do Spring Data é um padrão já em uso no projeto).
