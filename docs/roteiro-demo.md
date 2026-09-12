# Roteiro de Demonstração ao Vivo — Servio

Roteiro para a seção "Demonstração funcional" da apresentação (~10-12 min).
Cobre os RF novos (via SDD) e os principais da base. Usa **duas sessões**
lado a lado: aba/navegador A = cliente ("Maria"), aba/navegador B = prestador
("João").

* **Pré-requisitos**: Postgres na porta 5433 no ar; backend (`mvn spring-boot:run`)
  e frontend (`npm start`) rodando; 2 contas já cadastradas (1 CLIENT, 1
  PROVIDER com ao menos 1 serviço ativo e agenda configurada); 1 conta ADMIN.

---

## 0. Abertura (30s)
"O Servio já tinha autenticação, cadastro, serviços, pedidos, avaliações,
agenda e dashboards. Evoluímos com 5 requisitos novos: favoritos, busca
avançada, jornada do pedido, notificações e relatório de desempenho — os 4
últimos com Spec-Driven Development completo, que vamos mostrar depois."

## 1. Cliente busca e favorita (RF-04, RF-05, RF-12) — 2 min
1. Login como Maria (CLIENT) → "Explorar serviços".
2. Digitar um termo na busca → mostra debounce, resultado atualiza sozinho.
3. Aplicar filtro de categoria + faixa de preço + localização → resultado muda.
4. Clicar no coração de um serviço → toast "adicionado aos favoritos".
5. Ir em "Meus favoritos" na sidebar → o serviço aparece.

## 2. Contratação e chat (RF-06, RF-16) — 1 min
1. Voltar ao serviço, abrir detalhes.
2. (Opcional) Abrir "Mensagens" e mandar uma pergunta ao prestador antes de
   contratar.
3. Selecionar horário disponível e criar o pedido.

## 3. Prestador aceita e jornada começa (RF-07, RF-13) — 2 min
1. Trocar para a aba do João (PROVIDER) → "Pedidos" (Kanban).
2. O pedido novo aparece na coluna "Pendente" com botão **"Aceitar pedido"**.
3. Clicar em aceitar → card move pra "Confirmado".
4. Abrir os detalhes do pedido → mostrar a **linha do tempo da jornada**:
   "Solicitado" e "Aceito" concluídos com data/hora, "Em andamento" como
   próxima etapa.

## 4. Cliente recebe a notificação (RF-14) — 1 min
1. Voltar pra aba da Maria, navegar pra outra tela (dispara o refetch).
2. Apontar o **sino** com o badge de não lida.
3. Abrir o sino → notificação "Seu pedido mudou para: Aceito" → clicar →
   marca como lida e leva ao painel.
4. No painel do cliente, abrir "Ver jornada" no card do pedido → mesma
   linha do tempo, do lado do cliente.

## 5. Fechar o ciclo do pedido (RF-13, RF-08) — 2 min
1. Aba do João: avançar pra "Em andamento" e depois "Concluído".
2. Aba da Maria: "Avaliar" o pedido concluído → nota + comentário.
3. Mostrar a avaliação refletida no serviço (média/contador no card).

## 6. Relatório de desempenho e dashboards (RF-15, RF-10) — 1 min
1. Aba do João: painel do prestador → aba **"Finanças"**.
2. Mostrar a tabela **"Serviços mais contratados"** (pedidos concluídos +
   avaliação média) e o resumo de ganhos/ticket médio.

## 7. Administração (RF-11) — opcional, 30s
1. Login ADMIN → "Categorias e Tags" → mostrar validação que impede excluir
   categoria em uso.

---

## Bônus — Harness ao vivo (se der tempo, reforça a seção de Harness)

1. Abrir o terminal no repositório.
2. Quebrar de propósito uma asserção em `FavoriteServiceTest` (ex.: trocar
   `404` por `999` na asserção de "serviço inexistente").
3. `git add -A && git commit -m "demo: quebrar teste de proposito"`.
4. Mostrar o guardrail rodando os testes e **rejeitando o commit** com
   `🚨 COMMIT REJEITADO`.
5. `git log --oneline -1` → mostrar que o HEAD não mudou.
6. Reverter a alteração (`git checkout -- ...`).

> Isso é a mesma evidência documentada em
> `docs/harness/evidencia-guardrail-bloqueio.md`, mas ao vivo tem mais impacto.

---

## Se algo falhar ao vivo

* Tenha o navegador com as telas já visitadas em abas abertas como plano B
  (screenshot/gravação de apoio).
* Se o backend cair, o guardrail e os testes automatizados (`mvn test`, 33
  verdes) já são evidência documentada — pode seguir mostrando pelo código e
  pelos docs sem travar a apresentação.
