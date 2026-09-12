# [SPEC-008] Chat entre Cliente e Prestador

* **Autora**: Bianca Antonelly
* **Data**: 2026-09-12
* **Status**: Aprovado
* **Requisito do Projeto**: Requisito Funcional 16 (RF-16)
* **ADR Relacionada**: [ADR-006: Modelagem do Chat entre Cliente e Prestador](../adr/ADR-006-modelagem-chat-cliente-prestador.md)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é implementar um canal de mensagens entre cliente e prestador, usado antes da
contratação de um serviço.
Restrições de Comportamento:
- Quem inicia a conversa é sempre o cliente, a partir da página de um serviço específico.
- O prestador nunca inicia conversas; ele apenas responde às que já foram abertas.
- Uma conversa é única por par (cliente, serviço) — reabrir a página do mesmo serviço deve
  reaproveitar a conversa existente, não criar uma nova.
- Apenas os dois participantes da conversa (o cliente que a abriu e o prestador do serviço)
  podem ler ou enviar mensagens nela.
- Mensagens não lidas devem ser marcadas como lidas quando o destinatário abre a conversa.
```

---

## 2. Contexto e Motivação

Antes de contratar um serviço, é comum o cliente ter dúvidas (disponibilidade,
detalhes do escopo, possibilidade de desconto) que a página de detalhes do
serviço não resolve sozinha. Sem um canal direto, esse contato acontecia fora
da plataforma. O **Chat** resolve isso permitindo que o cliente fale
diretamente com o prestador antes de fechar negócio, mantendo o histórico
dentro do próprio Servio.

---

## 3. Requisitos Funcionais

* **RF-16.1 (Iniciar conversa)**: o cliente inicia uma conversa a partir da
  página de um serviço; se já existir uma conversa com aquele prestador sobre
  aquele serviço, ela é reaproveitada.
* **RF-16.2 (Listar conversas)**: cliente e prestador visualizam a lista de
  suas conversas, ordenada pela mais recentemente ativa, com contagem de
  mensagens não lidas.
* **RF-16.3 (Enviar/receber mensagens)**: os dois participantes de uma
  conversa podem enviar e visualizar mensagens de texto, em ordem cronológica.
* **RF-16.4 (Restrição de acesso)**: apenas os dois participantes de uma
  conversa podem lê-la ou enviar mensagens nela.
* **RF-16.5 (Marcação de leitura)**: mensagens não lidas são marcadas como
  lidas quando o destinatário abre a conversa.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: Cliente inicia uma conversa a partir de um serviço
```gherkin
Dado que o cliente "Bianca" está na página do serviço "Lancha", de outro prestador
Quando "Bianca" envia "POST /chat/conversations" com o id desse serviço
Então o sistema deve retornar status HTTP 201 (Created)
E uma nova conversa deve ser criada entre "Bianca" e o prestador do serviço
```

### Cenário 2: Reabrir a mesma página reaproveita a conversa existente
```gherkin
Dado que "Bianca" já tem uma conversa aberta sobre o serviço "Lancha"
Quando "Bianca" envia novamente "POST /chat/conversations" para o mesmo serviço
Então o sistema deve retornar a conversa já existente, sem criar uma duplicata
```

### Cenário 3: Prestador responde e cliente vê a mensagem
```gherkin
Dado que existe uma conversa entre "Bianca" (cliente) e "Julliane" (prestadora)
Quando "Julliane" envia "POST /chat/conversations/{id}/messages" com um texto
Então a mensagem deve aparecer para "Bianca" ao consultar "GET /chat/conversations/{id}/messages"
E a contagem de não lidas de "Bianca" para essa conversa deve aumentar em 1
```

### Caso de Borda 1 (Edge Case — Acesso de quem não participa da conversa):
```gherkin
Dado que existe uma conversa entre "Bianca" e "Julliane"
Quando um terceiro usuário autenticado tenta acessar "GET /chat/conversations/{id}/messages"
Então o sistema deve recusar com status HTTP 403 (Forbidden)
```

### Caso de Borda 2 (Edge Case — Prestador tentando iniciar conversa):
```gherkin
Dado que um usuário autenticado com role "PROVIDER" tenta iniciar uma conversa
Quando ele envia "POST /chat/conversations"
Então o sistema deve recusar com status HTTP 403 (Forbidden)
E a mensagem deve indicar que apenas clientes podem iniciar uma conversa
```

### Caso de Borda 3 (Edge Case — Serviço sem prestador associado ou já excluído):
```gherkin
Dado que o serviço informado está com status "deleted = true"
Quando o cliente tenta iniciar uma conversa sobre esse serviço
Então o sistema deve recusar a operação com uma mensagem de negócio adequada
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/chat/conversations` | CLIENT | `201 Created` (ConversationResponseDTO) | 403 (não é cliente), 404 (serviço/provider não encontrado), 400 (serviço indisponível ou autodenúncia) |
| `GET` | `/chat/conversations` | CLIENT/PROVIDER | `200 OK` (List<ConversationResponseDTO>) | — |
| `GET` | `/chat/conversations/{id}/messages` | CLIENT/PROVIDER | `200 OK` (List<MessageResponseDTO>) | 403 (não participa), 404 |
| `POST` | `/chat/conversations/{id}/messages` | CLIENT/PROVIDER | `201 Created` (MessageResponseDTO) | 403 (não participa), 404 |

---

## 6. Plano de Tarefas (Tasklist)

- [x] **T1 (Modelo de Dados)**: Criar entidades `Conversation` (única por cliente+serviço) e `Message`.
- [x] **T2 (Repository)**: `ConversationRepository`, `MessageRepository`.
- [x] **T3 (Camada de Serviço)**: `ChatService` — iniciar/reaproveitar conversa, listar, ler (marcando como lida), enviar.
- [x] **T4 (Camada de Controller)**: `ChatController` com verificação de participante.
- [x] **T5 (Frontend — página Mensagens)**: lista de conversas + thread de mensagens, com polling para atualização.
- [x] **T6 (Frontend — gatilho)**: botão "Enviar mensagem" na página de detalhes do serviço.
- [x] **T7 (Correções pós-teste manual)**: Enter para enviar mensagem; correção de variável CSS que deixava nomes invisíveis.
- [x] **T8 (Testes automatizados)**: `ChatServiceTest` (5) — cobre os 3 cenários e os 2 casos de borda de acesso.

---

## 7. Nota de Arquitetura

O Chat foi validado por testes manuais extensivos (via navegador e chamadas
diretas à API), incluindo os dois casos de borda de acesso (T7 documenta
correções encontradas nesse processo). As decisões de modelagem estão
detalhadas no [ADR-006](../adr/ADR-006-modelagem-chat-cliente-prestador.md).
