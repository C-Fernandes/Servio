# [SPEC-009] Histórico Detalhado de Interações entre Cliente e Prestador

* **Autora**: Bianca Antonelly
* **Data da implementação**: 2026-09-12
* **Data desta especificação**: 2026-09-12 (**retroativa** — ver seção 7)
* **Status**: Aprovado (retroativo)
* **Requisito do Projeto**: Requisito Funcional 19 (RF-19)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é implementar um histórico detalhado de interações entre um cliente e um prestador
específicos, reunindo tudo o que já aconteceu entre os dois em uma única linha do tempo.
Restrições de Comportamento:
- O histórico deve juntar três fontes de dados já existentes: mensagens de chat (todas as
  conversas entre o par, não só uma), mudanças de status de pedidos e avaliações.
- Só os dois participantes (o próprio cliente ou o próprio prestador da relação) podem consultar
  esse histórico.
- Pedidos legados sem histórico de status registrado não podem desaparecer da linha do tempo:
  devem ser reconstruídos a partir do status atual.
- O histórico é somente leitura — não é um lugar para responder mensagens.
```

---

## 2. Contexto e Motivação

O Chat (RF-16) mostra apenas as mensagens de **uma** conversa (ligada a um
serviço específico), e a Jornada do Pedido (RF-13) mostra apenas as etapas de
**um** pedido. Nenhuma tela reúne o panorama completo do relacionamento entre
um cliente e um prestador — quantas vezes já conversaram, quantos pedidos já
fizeram, que avaliações já trocaram. O **Histórico de Interações** cobre essa
lacuna com uma visão agregada e cronológica.

---

## 3. Requisitos Funcionais

* **RF-19.1 (Agregação de eventos)**: o histórico reúne mudanças de status de
  pedidos, avaliações e mensagens de chat entre um cliente e um prestador
  específicos, em qualquer serviço.
* **RF-19.2 (Ordenação cronológica)**: os eventos são ordenados do mais
  recente para o mais antigo.
* **RF-19.3 (Restrição de acesso)**: apenas o próprio cliente ou o próprio
  prestador da relação podem consultar o histórico entre os dois.
* **RF-19.4 (Reconstrução de pedidos legados)**: pedidos sem histórico de
  status registrado aparecem no histórico com um evento sintetizado a partir
  do status atual, em vez de serem omitidos.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: Histórico junta mensagens, pedido e avaliação em ordem cronológica
```gherkin
Dado que "Bianca" (cliente) e "Julliane" (prestadora) trocaram mensagens, tiveram um pedido
  concluído e "Bianca" avaliou esse pedido
Quando "Bianca" consulta "GET /interactions/{idDeJulliane}"
Então o sistema deve retornar status HTTP 200 (OK)
E o corpo deve conter os três tipos de evento (mensagem, status de pedido, avaliação)
E os eventos devem estar ordenados do mais recente para o mais antigo
```

### Cenário 2: Sem nenhuma interação prévia, o histórico retorna vazio sem erro
```gherkin
Dado que "Bianca" nunca interagiu com o prestador "Pedro"
Quando "Bianca" consulta "GET /interactions/{idDePedro}"
Então o sistema deve retornar status HTTP 200 (OK) com uma lista vazia
```

### Cenário 3: Prestador consulta o histórico do seu lado
```gherkin
Dado que "Julliane" (prestadora) já interagiu com a cliente "Bianca"
Quando "Julliane" consulta "GET /interactions/{idDeBianca}"
Então o sistema deve identificar automaticamente que "Julliane" é o lado prestador da relação
E retornar o mesmo conjunto de eventos que "Bianca" veria, do seu próprio ponto de vista
```

### Caso de Borda 1 (Edge Case — Pedido legado sem histórico de status):
```gherkin
Dado um pedido entre o par cliente/prestador criado antes da funcionalidade de jornada,
  sem nenhum evento em OrderStatusHistory, com status atual "COMPLETED"
Quando o histórico de interações desse par é consultado
Então o pedido deve aparecer na linha do tempo com um evento sintetizado a partir de
  "createdAt" e do status atual, em vez de ser omitido
```

### Caso de Borda 2 (Edge Case — Usuário sem role compatível):
```gherkin
Dado um usuário autenticado com role "ADMIN"
Quando ele tenta consultar "GET /interactions/{otherUserId}"
Então o sistema deve recusar com status HTTP 403 (Forbidden), pois o histórico é exclusivo
  para a relação direta entre um cliente e um prestador
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/interactions/{otherUserId}` | CLIENT/PROVIDER | `200 OK` (List<InteractionEventDTO>) | 403 (role incompatível), 404 (otherUserId não encontrado) |

`InteractionEventDTO`: `type` (`ORDER_STATUS` \| `REVIEW` \| `MESSAGE`),
`timestamp`, `description`, `referenceId`.

---

## 6. Plano de Tarefas (Tasklist)

- [x] **T1 (Repository queries)**: adicionar consultas por par cliente+prestador em `OrderRepository`, `OrderStatusHistoryRepository`, `ReviewRepository` e `MessageRepository`.
- [x] **T2 (DTO)**: criar `InteractionEventDTO`, único para os três tipos de evento.
- [x] **T3 (Camada de Serviço)**: `InteractionService` — resolve qual lado (cliente/prestador) é o usuário autenticado, agrega e ordena os eventos, reconstrói pedidos legados.
- [x] **T4 (Camada de Controller)**: `InteractionController` com restrição de role.
- [x] **T5 (Frontend — modal de histórico)**: `InteractionHistoryModalComponent`, com ícone por tipo de evento.
- [x] **T6 (Frontend — gatilho)**: botão "Ver histórico" no cabeçalho da conversa, na página Mensagens.
- [ ] **T7 (Testes automatizados)**: cobrir `InteractionService`, incluindo o caso de borda de pedido legado — **pendente, gap reconhecido**.

---

## 7. Nota de Transparência (Retroatividade)

Escrita após a implementação. A feature foi validada manualmente via chamadas
diretas à API (com dados reais de mensagens já trocadas) e via navegador, mas
não recebeu testes automatizados nem ADR dedicado antes de ser mesclada em
`main` — mesmo padrão de gap das SPEC-007 e SPEC-008.
