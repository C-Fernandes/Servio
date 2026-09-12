# [ADR-006] Modelagem do Chat entre Cliente e Prestador

* **Status**: Aceito
* **Data**: 2026-09-12
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema

O RF-16 exige um canal de mensagens entre cliente e prestador antes da
contratação. Era preciso decidir:
1. O que identifica uma conversa de forma única — evitar duplicatas quando o
   cliente reabre a página do mesmo serviço.
2. Se qualquer um dos dois lados pode iniciar uma conversa, ou só um.
3. Como marcar mensagens como lidas sem exigir uma ação explícita separada
   do usuário ("marcar como lida").
4. Se o chat vive dentro do domínio de `Service` ou é um módulo à parte.

## 2. Direcionadores de Decisão (Drivers)

* **Evitar conversas fragmentadas**: se o cliente reabrir a página do serviço
  várias vezes, não pode virar uma conversa nova a cada vez — perderia o
  histórico e confundiria o prestador.
* **Papel de negócio claro**: no marketplace, é o cliente que demonstra
  interesse; o prestador reage. Permitir que o prestador inicie contato frio
  com qualquer cliente abriria espaço para spam comercial.
* **UX de leitura sem atrito**: o usuário não deveria precisar clicar em
  "marcar como lida" — abrir a conversa já deveria refletir isso, como em
  qualquer app de mensagens.
* **Consistência com o padrão de acesso do projeto**: igual a
  `AvailabilityService`/`CouponService`, o dono do recurso (aqui, participante
  da conversa) é validado pelo *id* do usuário autenticado, não por um campo
  de permissão à parte.

## 3. Decisão Considerada e Aprovada

1. **Conversa é única pela combinação `(clientId, serviceId)`**
   (`ConversationRepository.findByClientIdAndServiceId`), não por
   `(clientId, providerId)`. Um mesmo cliente pode ter conversas diferentes
   com o mesmo prestador se forem sobre serviços diferentes — o contexto da
   conversa é o serviço, não só o par de pessoas.
2. **Só `CLIENT` pode chamar `startConversation`**; a tentativa de um
   `PROVIDER` retorna `403` antes mesmo de consultar o serviço-alvo. O
   prestador só participa de conversas que o cliente já abriu, nunca inicia
   uma sozinho.
3. **Leitura implícita**: `getMessages` marca como lidas, em lote
   (`messageRepository.saveAll`), todas as mensagens da conversa que não são
   do próprio usuário e ainda não foram lidas — como efeito colateral da
   consulta, não como uma operação `PATCH` separada. Simplifica o contrato
   da API (uma chamada a menos no frontend) ao custo de uma escrita dentro de
   uma operação nominalmente de leitura.
4. **Módulo próprio (`Conversation`/`Message`), referenciando `Service` só
   por FK**, em vez de aninhar o chat dentro de `ServiceService`. Mantém
   `ChatService` independente e testável sem carregar as dependências
   pesadas de `ServiceService` (mapper, disponibilidade, avaliações etc.).

## 4. Consequências

### Positivas:
* Nenhuma duplicata de conversa por reabertura de página — comportamento idempotente e previsível.
* Contrato de API simples: abrir a conversa (`GET /chat/conversations/{id}/messages`) já resolve a leitura, sem endpoint extra.
* `ChatService` isolado, com só 4 dependências — fácil de testar (ver `ChatServiceTest`).

### Negativas / Trade-offs:
* Se o prestador tiver vários serviços e o mesmo cliente conversar sobre dois deles, existem duas conversas distintas — o histórico não é unificado por relação cliente-prestador (isso é coberto separadamente pelo RF-19, Histórico de Interações, que agrega as duas).
* Marcar como lida dentro de uma leitura (`getMessages`) significa que **toda** consulta à conversa gera uma escrita potencial no banco — aceitável no volume atual, mas não é uma operação verdadeiramente somente-leitura.

## 5. Alternativas Consideradas

* **Conversa única por `(clientId, providerId)`** (ignorando o serviço): descartada porque misturaria o contexto de negociações diferentes na mesma thread, dificultando pro prestador saber de qual serviço o cliente está falando.
* **Endpoint `PATCH /messages/{id}/read` explícito**: descartada por adicionar uma chamada de rede extra ao frontend sem ganho real — nenhum caso de uso do projeto precisa que "ler" e "marcar como lida" sejam ações separadas no tempo.
* **Chat como sub-recurso de `Service`** (`/services/{id}/chat`): descartada porque uma conversa sobrevive a mudanças no serviço (ex.: serviço desativado depois) e teria ciclo de vida próprio, melhor representado por um módulo independente.
