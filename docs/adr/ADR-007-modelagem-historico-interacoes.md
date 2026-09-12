# [ADR-007] Modelagem do Histórico de Interações entre Cliente e Prestador

* **Status**: Aceito (retroativo — ver seção 5)
* **Data**: 2026-09-12
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema

O RF-19 exige uma linha do tempo única reunindo tudo que já aconteceu entre
um cliente e um prestador específicos — mensagens de chat, mudanças de
status de pedido e avaliações. Era preciso decidir:
1. Se o histórico é uma entidade persistida própria ou uma agregação
   calculada sob demanda a partir de dados já existentes.
2. Como resolver "quem é o cliente e quem é o prestador" quando o usuário
   autenticado pode ser qualquer um dos dois lados.
3. O que fazer com pedidos criados antes do RF-13 (Jornada do Pedido)
   existir, que não têm `OrderStatusHistory` registrado.
4. Que perfis podem consultar o histórico.

## 2. Direcionadores de Decisão (Drivers)

* **Não duplicar dado que já existe**: mensagens, mudanças de status e
  avaliações já são persistidas por `ChatService`, `OrderService` e
  `ReviewService` — criar uma tabela própria de "eventos de histórico"
  significaria manter duas fontes de verdade sincronizadas.
* **Simetria de consulta**: tanto o cliente quanto o prestador devem ver
  exatamente o mesmo histórico ao consultar um sobre o outro — a URL
  (`GET /interactions/{otherUserId}`) é a mesma para os dois lados, só muda
  quem é "eu" e quem é "o outro".
* **Continuidade histórica**: pedidos antigos não podem sumir da linha do
  tempo só porque foram criados antes de uma funcionalidade (RF-13) existir.
* **Privacidade da relação**: o histórico entre A e B não pode vazar pra um
  terceiro C, nem pra um ADMIN sem relação direta com nenhum dos dois.

## 3. Decisão Considerada e Aprovada

1. **Sem entidade própria — agregação em tempo real de três fontes**
   (`Order`/`OrderStatusHistory`, `Review`, `Message`), unificadas num DTO
   comum (`InteractionEventDTO` com `type`/`timestamp`/`description`/
   `referenceId`) e ordenadas por timestamp depois da consulta. Cada consulta
   ao histórico é O(3 queries) + um sort em memória — aceitável no volume
   atual, sem risco de inconsistência entre o histórico e a fonte real.
2. **Resolução de lado por papel do usuário autenticado**: se `me.getRole()
   == CLIENT`, `me` é o `clientId` e `otherUserId` é o `providerId` — e
   vice-versa se `me` for `PROVIDER`. Não existe um campo "quem é cliente
   nesta relação" persistido; é inferido a cada chamada a partir do papel de
   quem está logado.
3. **Reconstrução de pedidos legados por diferença de conjuntos**: todo
   pedido do par é buscado (`findByClient_IdAndProvider_Id...`); os que **não
   aparecem** em `OrderStatusHistory` do mesmo par recebem um evento único
   sintetizado a partir de `order.getCreatedAt()` e `order.getStatus()` atual
   — em vez de tentar reconstruir uma jornada completa que nunca existiu.
4. **Restrito a `CLIENT`/`PROVIDER`, nunca `ADMIN`**: o histórico é sobre a
   relação **direta** entre os dois participantes; um ADMIN não tem uma
   relação cliente-prestador com ninguém, então a rota devolve `403` pra
   qualquer role fora dessas duas — diferente do padrão usado em RF-20
   (Denúncia), onde o ADMIN modera terceiros.

## 4. Consequências

### Positivas:
* Zero risco de o histórico divergir da fonte real (chat, pedidos, avaliações) — não há cópia pra ficar desatualizada.
* Mesmo endpoint e mesma lógica servem os dois lados da relação, sem duplicar código de consulta por perfil.
* Pedidos anteriores ao RF-13 continuam visíveis na linha do tempo, sem exigir migração de dados histórica.

### Negativas / Trade-offs:
* Custo de leitura maior por chamada (3 queries + agregação em memória) em vez de uma tabela já pré-agregada — aceitável porque o histórico é consultado sob demanda (clique em "Ver histórico"), não em toda navegação.
* Evento sintetizado de pedido legado carrega só um marco ("status atual"), não a jornada completa que o RF-13 oferece pra pedidos novos — uma limitação assumida, não escondida (ver RF-19.4 na SPEC-009).

## 5. Nota de Transparência (Retroatividade)

Este ADR foi escrito em 2026-09-12, depois da funcionalidade já estar
implementada e mergeada em `main` (PR #16, 2026-09-12) — junto com a
[SPEC-009](../specs/SPEC-009-historico-interacoes.md), também retroativa. As
decisões acima refletem o código como foi efetivamente implementado por
Bianca Antonelly, reconstruídas a partir da leitura de `InteractionService` e
confirmadas nos testes escritos nesta mesma revisão (`InteractionServiceTest`).

## 6. Alternativas Consideradas

* **Tabela própria `InteractionEvent`, populada por listener em cada evento de origem** (nova mensagem, mudança de status, nova avaliação): descartada por introduzir uma segunda fonte de verdade e o risco de o listener falhar silenciosamente e o histórico ficar incompleto — o padrão de "melhor esforço" já usado em RF-14 (Notificações) mostrou que esse risco é real no projeto.
* **Reconstruir a jornada completa dos pedidos legados** (inferir todas as etapas intermediárias a partir de heurísticas): descartada por exigir suposições sobre datas que não existem — sintetizar um único evento no status atual é honesto sobre o que realmente se sabe.
* **Permitir ADMIN consultar qualquer histórico, para fins de moderação**: descartada nesta versão por não haver, ainda, um fluxo de moderação que precise disso (RF-20 já cobre denúncia sem precisar expor o histórico completo da relação).
