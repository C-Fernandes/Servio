# [ADR-002] Modelagem e Idempotência do Sistema de Favoritos

* **Status**: Aceito
* **Data**: 2026-09-08
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema
O marketplace Servio necessita de uma funcionalidade que permita aos clientes salvarem serviços para contratação posterior (Requisito Funcional RF-01). Era necessário decidir a estratégia de persistência:
1. Usar uma relação simples `@ManyToMany` direta entre `User` e `Service` via `@JoinTable`.
2. Criar uma entidade intermediária explícita `Favorite` com metadados (`createdAt`) e controle granular de unicidade e ciclo de vida.

Além disso, precisávamos garantir a **idempotência** da ação de favoritar, evitando inserções duplicadas e condições de corrida no banco de dados.

## 2. Direcionadores de Decisão (Drivers)
* **Auditoria e Ordenação**: Necessidade de saber quando o cliente favoritou o serviço para permitir ordenação por data ("favoritados recentemente").
* **Idempotência e Integridade**: Impedir duplicação através de constraint única a nível de banco de dados (`uniqueConstraint` em `user_id` e `service_id`).
* **Performance em Consultas Frequentes**: Facilitar consultas rápidas de contagem (quantos usuários favoritaram um serviço) e verificação booleana (`existsByUserIdAndServiceId`).
* **Baixo Acoplamento**: Evitar poluir a entidade `User` e a entidade `Service` com coleções pesadas bidirecionais que pudessem gerar problemas de *lazy loading* ou serialização recursiva JSON.

## 3. Decisão Considerada e Aprovada
Optou-se pela **criação de uma entidade intermediária explícita `Favorite`**:

1. **Entidade Dedicada**:
   * Tabela: `favorites`.
   * Campos: `id` (PK IDENTITY), `user_id` (FK para `users`, não nulo), `service_id` (FK para `services`, não nulo), `created_at` (timestamp de registro).
   * Constraint de unicidade: `@Table(uniqueConstraints = @UniqueConstraint(name = "uk_user_service_favorite", columnNames = {"user_id", "service_id"}))`.
2. **Estratégia de Idempotência**:
   * No `FavoriteService`, antes de inserir, verifica-se `existsByUserIdAndServiceId`. Se já existir, a operação é tratada de forma idempotente (retornando o registro já existente sem lançar erro de integridade nem duplicar).
   * Em caso de concorrência, a restrição de chave única no banco atua como garantia final de consistência.
3. **Validação de Regras de Negócio**:
   * O serviço verifica se o `Service` existe, se `active == true` e `deleted == false`.
   * Apenas clientes autenticados (`Role.CLIENT`) podem acionar as operações.

## 4. Consequências
### Positivas:
* Histórico temporal preservado (`createdAt`).
* Consultas simples e otimizadas através do `FavoriteRepository` com queries derivadas do Spring Data.
* Proteção absoluta contra dados corrompidos ou registros duplicados.
* Facilidade para evoluir no futuro (ex: métricas de "serviços mais favoritados" para o painel do prestador).

### Negativas / Trade-offs:
* Exige uma classe de entidade, um repositório e um DTO dedicados em vez de um simples mapeamento `@ManyToMany` na entidade `User`.

## 5. Alternativas Consideradas
* **Mapeamento `@ManyToMany` simples em `User`**: Descartada por não armazenar a data do favorito, carregar coleções inteiras em memória e dificultar consultas agregadas de desempenho.
