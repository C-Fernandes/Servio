# [ADR-005] Modelagem do Sistema de Cupons de Desconto e Limite de Uso por Cliente

* **Status**: Aceito
* **Data**: 2026-09-12
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema

O RF-18 exige que prestadores criem cupons de desconto percentual restritos aos próprios
serviços, que cada cliente use um mesmo cupom no máximo uma vez, e que a validação do cupom
aconteça antes da criação do pedido (para o cliente ver o preço com desconto antes de
confirmar a reserva). Era preciso decidir:
1. Como impedir o reuso de um cupom pelo mesmo cliente.
2. Onde gravar o preço com desconto: só no cupom, ou também no pedido.
3. Se a validação e a aplicação do cupom seriam a mesma chamada ou chamadas separadas.

## 2. Direcionadores de Decisão (Drivers)

* **Integridade e concorrência**: o limite de uso por cliente precisa ser garantido no banco, não só na aplicação, para resistir a requisições concorrentes.
* **Auditoria do pedido**: o preço pago não pode mudar retroativamente se o prestador editar ou desativar o cupom depois.
* **UX de checkout**: o cliente precisa ver o desconto antes de confirmar a reserva (RF-18.3), não só descobrir o preço final depois de criar o pedido.
* **Reaproveitamento de regras**: a mesma validação (cupom ativo, não expirado, do serviço certo, não usado pelo cliente) vale tanto para o preview quanto para a aplicação real — não deveria haver duas implementações da mesma regra.

## 3. Decisão Considerada e Aprovada

1. **Entidade `CouponUsage` dedicada, com constraint única `(coupon_id, client_id)`**, em vez de contar linhas em `Order` por `couponCode` + `client`. A constraint única no banco é a garantia final contra corrida (duas requisições simultâneas tentando usar o mesmo cupom); a checagem em `CouponService.findUsableCoupon` é apenas a validação de negócio anterior a isso.
2. **`Order` grava `originalPrice`, `discountPercentage`, `finalPrice` e `couponCode` no momento da criação** (denormalizado), em vez de o pedido apenas referenciar o `Coupon` por FK e recalcular o preço sob demanda. Assim, se o prestador desativar ou apagar o cupom depois, o histórico do pedido não muda.
3. **Endpoint de validação (`GET /coupons/validate`) separado da criação do pedido**, reaproveitando o mesmo método de domínio (`CouponService.findUsableCoupon`) que `OrderService.create` chama internamente ao receber um `couponCode`. A validação não persiste `CouponUsage` (é solicitada quantas vezes o cliente quiser antes de decidir reservar); só a criação do pedido persiste o uso, via `registerUsage`.
4. **Cupom é escopado a um único serviço** (`Coupon.service`), não ao catálogo inteiro do prestador, mantendo o modelo de permissão simples (dono do serviço = dono do cupom, igual ao padrão já usado em `AvailabilityService`/bloqueios).

## 4. Consequências

### Positivas:
* Reuso indevido do cupom é impossível mesmo sob concorrência (constraint de banco).
* Preço do pedido nunca é recalculado retroativamente — auditável e estável.
* Uma única implementação de regra de validação (`findUsableCoupon`) é compartilhada entre o preview (`validate`) e a aplicação real (`create` do pedido), evitando divergência entre as duas.

### Negativas / Trade-offs:
* Duas chamadas de rede no fluxo de reserva (validar, depois criar o pedido) em vez de uma só — aceito porque o cliente precisa ver o desconto antes de confirmar.
* Denormalizar preço no pedido significa que, se a regra de cálculo de desconto mudar no futuro, pedidos antigos mantêm o valor já calculado (comportamento desejado aqui, mas exige atenção em migrações futuras).

## 5. Alternativas Consideradas

* **Contar uso do cupom via `COUNT(*) FROM orders WHERE coupon_code = ? AND client_id = ?`**: descartada por não ter garantia de unicidade no banco (uma condição de corrida entre duas reservas simultâneas poderia deixar ambas passarem) e por acoplar a regra de limite de uso à tabela de pedidos.
* **Cupom global do prestador (vale para todos os serviços dele)**: descartada nesta versão por exigir decidir como ratear o desconto entre serviços de preços diferentes; escopo por serviço único mantém a regra de desconto sem ambiguidade.
* **Validar e aplicar em uma única chamada (`POST /orders` já validando o cupom sem preview)**: descartada porque o cliente ficaria sem visibilidade do preço final antes de confirmar a reserva.
