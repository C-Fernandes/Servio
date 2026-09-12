# [SPEC-006] Sistema de Cupons de Desconto por Serviço

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-12
* **Status**: Aprovado
* **Requisito do Projeto**: RF-18 (novo requisito levantado após o checkpoint, via SDD)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é especificar e implementar o Sistema de Cupons de Desconto no marketplace Servio.
Restrições de Comportamento:
- Um cupom é criado pelo prestador e vale apenas para um dos seus próprios serviços.
- O desconto é sempre percentual (1% a 100%).
- Cada cliente pode usar um mesmo cupom no máximo uma vez.
- A validação do cupom acontece em um endpoint dedicado, separado da criação do pedido; o
  código validado é então enviado junto ao POST /orders para ser efetivamente aplicado.
- Cubra explicitamente os casos de borda com testes automatizados.
```

## 2. Contexto e Motivação

O marketplace não tinha nenhum mecanismo de desconto: o preço cobrado era sempre o preço
cadastrado no serviço (`Service.price`). Cupons de desconto são um requisito comum em
marketplaces de serviço, permitindo que o prestador promova seus próprios serviços (ex.:
atrair cliente novo, esvaziar agenda ociosa) sem depender de um administrador central.

## 3. Requisitos Funcionais

* **RF-18.1**: prestador cria um cupom de desconto percentual para um serviço próprio.
* **RF-18.2**: prestador lista seus cupons e pode desativá-los.
* **RF-18.3**: cliente valida um código de cupom para um serviço antes de reservar, vendo o
  preço original e o preço com desconto.
* **RF-18.4**: cliente aplica o cupom validado na criação do pedido; o pedido grava o preço
  original, o percentual de desconto e o preço final.
* **RF-18.5**: um cupom não pode ser usado mais de uma vez pelo mesmo cliente.

## 4. Critérios de Aceite (Gherkin)

### Cenário 1: prestador cria cupom para o próprio serviço
```gherkin
Dado que o prestador "João" tem o serviço "Corte de cabelo" (R$100,00)
Quando ele cria o cupom "PROMO10" com 10% de desconto para esse serviço
Então o cupom deve ser criado e listado em "Meus cupons"
```

### Cenário 2: cliente valida e aplica o cupom no pedido
```gherkin
Dado o cupom "PROMO10" (10% de desconto) válido para "Corte de cabelo" (R$100,00)
Quando o cliente "Ana" consulta "GET /coupons/validate?code=PROMO10&serviceId=1"
Então o sistema retorna preço original R$100,00 e preço final R$90,00
Quando Ana envia "POST /orders" informando o serviço, horário e o código "PROMO10"
Então o pedido é criado com originalPrice=100.00, discountPercentage=10, finalPrice=90.00
```

### Caso de Borda 1 (Edge Case — Reuso do mesmo cupom pelo cliente):
```gherkin
Dado que a cliente "Ana" já usou o cupom "PROMO10" em um pedido anterior
Quando ela tenta usar "PROMO10" novamente (validação ou criação de pedido)
Então o sistema deve recusar com 400 (Bad Request) e mensagem "Você já utilizou este cupom."
```

### Caso de Borda 2 (Edge Case — Cupom de outro prestador):
```gherkin
Dado um serviço pertencente ao prestador "João"
Quando o prestador "Marcos" tenta criar um cupom para esse serviço
Então o sistema deve recusar com 403 (Forbidden)
```

### Caso de Borda 3 (Edge Case — Cupom expirado ou de outro serviço):
```gherkin
Dado o cupom "PROMO10" com expiresAt no passado
Quando qualquer cliente tenta validá-lo ou aplicá-lo
Então o sistema deve recusar com 400 (Bad Request) e mensagem "Cupom expirado."

Dado o cupom "PROMO10" cadastrado para o serviço 1
Quando um cliente tenta aplicá-lo ao serviço 2
Então o sistema deve recusar com 400 (Bad Request) e mensagem "Cupom não é válido para este serviço."
```

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta | Erros |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/coupons` | PROVIDER (dono do serviço) | `201 Created` (`CouponResponseDTO`) | 400 (código duplicado/expiração inválida), 403, 404 |
| `GET` | `/coupons/my-coupons` | PROVIDER | `200 OK` (`List<CouponResponseDTO>`) | — |
| `DELETE` | `/coupons/{id}` | PROVIDER (dono) | `204 No Content` (desativa, não remove) | 403, 404 |
| `GET` | `/coupons/validate?code=&serviceId=` | autenticado | `200 OK` (`CouponValidationResponseDTO`) | 400 (inválido/expirado/já usado/serviço errado) |
| `POST` | `/orders` (existente) | autenticado | `couponCode` opcional no body; resposta traz `originalPrice`/`discountPercentage`/`finalPrice`/`couponCode` | 400 (mesmas validações do cupom) |

## 6. Modelagem

* `Coupon`: `code` (único), `discountPercentage`, `service` (FK), `active`, `expiresAt`, `createdAt`.
* `CouponUsage`: `coupon` (FK), `client` (FK), `order` (FK), `usedAt`; unicidade em `(coupon_id, client_id)` garante o limite de uso por cliente.
* `Order` ganha `originalPrice`, `discountPercentage`, `finalPrice`, `couponCode` (denormalizado, preservado mesmo se o cupom for desativado depois).

Ver [ADR-005](../adr/ADR-005-modelagem-sistema-cupons.md) para a decisão de modelagem.

## 7. Plano de Tarefas

- [x] **T1**: entidades `Coupon`/`CouponUsage` + repositórios.
- [x] **T2**: `CouponService` (criar, listar, desativar, validar, `findUsableCoupon`/`registerUsage` reaproveitados pelo pedido).
- [x] **T3**: `CouponController` (`POST/GET/DELETE /coupons`, `GET /coupons/validate`).
- [x] **T4**: `OrderService.create` aplica o cupom e grava preço original/desconto/final no pedido.
- [x] **T5**: `CouponServiceTest` — cenários + casos de borda.
- [x] **T6**: frontend — seção "Meus cupons" (criar/listar/desativar) e campo de cupom na reserva do serviço.
- [ ] **T7**: homologação (demo).
