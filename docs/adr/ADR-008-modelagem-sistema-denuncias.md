# [ADR-008] Modelagem do Sistema de Denúncia de Serviço ou Usuário

* **Status**: Aceito
* **Data**: 2026-09-12
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema

O RF-20 exige um canal formal pra clientes reportarem serviço ou usuário
inadequado, com fila de moderação pro admin. Era preciso decidir:
1. Modelar `Report` como entidade única polimórfica (serviço **ou** usuário)
   ou como duas entidades separadas (`ServiceReport`, `UserReport`).
2. Motivo da denúncia como enum fechado ou texto livre.
3. Como evitar que o mesmo cliente acumule várias denúncias pendentes contra
   o mesmo alvo.
4. Se — e o quê — o denunciado deveria saber quando sua denúncia é analisada.

## 2. Direcionadores de Decisão (Drivers)

* **Simplicidade de moderação**: o admin precisa de uma fila única (`GET
  /reports`) pra revisar tudo, não duas telas separadas por tipo de alvo.
* **Abuso do canal de denúncia**: um cliente insatisfeito não pode enfileirar
  denúncias repetidas contra o mesmo alvo só reenviando o formulário.
* **Privacidade do denunciante**: a notificação ao denunciado não pode expor
  quem denunciou, só que houve moderação.
* **Consistência com o resto do domínio**: o padrão de dono de recurso
  (`@Client`/`@Admin` por rota, dono valida por *id* do usuário autenticado)
  já usado em `CouponService`/`AvailabilityService` deveria se repetir aqui.

## 3. Decisão Considerada e Aprovada

1. **Entidade única `Report` com `targetType` (`SERVICE`\|`USER`) e duas FKs
   opcionais** (`reportedService`, `reportedUser`, só uma preenchida por
   registro), em vez de duas tabelas separadas. Mantém a fila de moderação do
   admin (`GET /reports`) em uma única query, ao custo de duas colunas FK
   opcionais em vez de uma obrigatória.
2. **Motivo como enum fechado** (`ReportReason`: `INAPPROPRIATE_CONTENT`,
   `SPAM`, `FRAUD`, `HARASSMENT`, `FAKE_PROFILE`, `OTHER`), com descrição
   livre complementar. Enum fechado dá ao admin um filtro/relatório
   estruturado; a descrição livre cobre o que o enum não prevê.
3. **Deduplicação por reaproveitamento, não por rejeição**: uma nova denúncia
   do mesmo denunciante pro mesmo alvo, enquanto a anterior segue `PENDING`,
   **retorna a denúncia já existente** (`201 Created` com o registro
   reaproveitado) em vez de lançar erro. Evita ruído de "você já denunciou
   isso" pro cliente e evita duplicar linhas na fila do admin — mas depois
   que uma denúncia é decidida (`REVIEWED`/`DISMISSED`), uma nova denúncia
   pro mesmo alvo cria um registro novo (o histórico de decisões anteriores
   não se perde).
4. **Notificação genérica, sem detalhes da denúncia**: ao marcar `REVIEWED`,
   `NotificationService.notifyGeneric` envia só "seu serviço/conta foi
   analisado pela nossa equipe de moderação" — sem motivo, sem quem
   denunciou. Reaproveita a mesma notificação genérica de RF-14 em vez de
   criar um tipo de notificação dedicado.
5. **Sem restrição por perfil no alvo `USER`**: qualquer usuário autenticado
   como `CLIENT` pode denunciar outro usuário (cliente ou prestador), não só
   prestadores. Mantém a regra simples: a única checagem é "não pode
   denunciar a si mesmo".

## 4. Consequências

### Positivas:
* Fila de moderação única e simples pro admin, sem UI duplicada por tipo de alvo.
* Reaproveitamento de denúncia pendente evita fila poluída com duplicatas do mesmo cliente.
* Motivo estruturado permite filtro/futuro relatório por tipo de problema mais reportado.

### Negativas / Trade-offs:
* `Report` tem duas FKs opcionais (`reportedService`/`reportedUser`) em vez de uma obrigatória — validade cruzada (exatamente uma preenchida, de acordo com `targetType`) é garantida só na camada de serviço, não por constraint de banco.
* Reaproveitar a denúncia pendente em vez de rejeitar significa que o cliente não consegue "atualizar" o motivo/descrição de uma denúncia já enviada sem esperar ela ser decidida — aceito porque o caso de uso é raro e a alternativa (permitir edição) complicaria o fluxo do admin.

## 5. Alternativas Consideradas

* **Duas entidades separadas (`ServiceReport`, `UserReport`)**: descartada por forçar o admin a consultar/unir duas fontes pra ter uma fila única de moderação.
* **Rejeitar denúncia duplicada com erro** (em vez de reaproveitar a pendente): descartada por gerar atrito de UX sem benefício — o cliente só quer garantir que o alvo seja revisado, não criar múltiplos registros.
* **Notificar o denunciado com o motivo detalhado**: descartada por risco de retaliação contra quem denunciou, mesmo sem revelar identidade — o motivo específico já entrega pistas.
