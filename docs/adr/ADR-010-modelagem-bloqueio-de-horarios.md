# [ADR-010] Modelagem do Bloqueio de Horários Específicos na Disponibilidade

* **Status**: Aceito
* **Data**: 2026-09-12
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema

O RF-17 exige que o prestador bloqueie uma data/horário específico (ex.:
feriado, compromisso pessoal), mesmo quando esse horário está coberto por uma
regra semanal recorrente — sem apagar a regra semanal, que continua valendo
nas demais semanas. Era preciso decidir como representar esse bloqueio
pontual em relação às regras semanais já existentes em `Availability`.

## 2. Direcionadores de Decisão (Drivers)

* **Não alterar a regra semanal**: bloquear um dia não pode exigir editar ou
  remover a regra recorrente — o prestador volta a ficar disponível nesse
  horário na semana seguinte, automaticamente.
* **Reuso do modelo de disponibilidade já existente**: `Availability` já
  distingue regra semanal (`dayOfWeek` preenchido) de horário extra pontual
  (`specificDate` preenchido); um bloqueio é, estruturalmente, o oposto do
  horário extra — pontual, mas subtraindo disponibilidade em vez de somar.
* **Menor mudança de schema possível**: evitar uma tabela nova para um
  conceito que já cabe nos campos existentes de `Availability`.
* **Bloqueio deve valer para qualquer forma de disponibilidade do dia**: tanto
  para uma regra semanal quanto para um horário extra que caia na mesma data.

## 3. Decisão Considerada e Aprovada

1. **Bloqueio reaproveita a entidade `Availability`**: `specificDate`
   preenchido, `dayOfWeek = null`, `isAvailable = false`. Nenhuma tabela ou
   entidade nova (`AvailabilityBlock`) foi criada.
2. **Identificação por convenção, não por campo de tipo**: `isBlock(a)` é
   `a.getSpecificDate() != null && !a.getIsAvailable()` — reaproveita as
   colunas existentes em vez de adicionar um enum/flag de "tipo de registro".
3. **Aplicação em `generateAvailableSlots`**: os bloqueios do dia
   (`getBlocksForDate`) são coletados separadamente das regras (`getRulesForDate`)
   e cada slot candidato passa por `hasBlockConflict` antes de entrar na lista
   final — o bloqueio filtra o resultado já gerado pela regra semanal, sem
   tocar na regra em si.
4. **Dono validado pelo `providerId` do bloqueio comparado ao prestador
   autenticado** (`removeBlock` → 403 se não for dono), mesmo padrão de posse
   usado no restante do módulo de disponibilidade.

## 4. Consequências

### Positivas:
* Regra semanal nunca é tocada por um bloqueio — reversível por natureza (o
  bloqueio expira implicitamente: só vale pra aquela `specificDate`).
* Nenhuma tabela nova, nenhuma migração de schema além do já existente para
  `Availability`.
* `listBlocks`/`createBlock`/`removeBlock` reaproveitam o mesmo
  `AvailabilityRepository.findByProviderId` já usado pelo resto do módulo.

### Negativas / Trade-offs:
* Bloqueio e horário extra pontual só se distinguem pelo valor de
  `isAvailable` — um bug que inverta essa flag em outro ponto do código
  passaria a tratar um bloqueio como horário extra (ou vice-versa)
  silenciosamente, sem erro de tipo para pegar em compilação.
* `getBlocksForDate`/`hasBlockConflict` percorrem a lista de disponibilidades
  do prestador a cada geração de slots (`O(regras + bloqueios)` por dia) —
  aceitável no volume atual, mas cresce linearmente com o histórico de
  bloqueios se nada for arquivado.

## 5. Alternativas Consideradas

* **Entidade `AvailabilityBlock` separada**: descartada — duplicaria a busca
  por prestador/data já existente em `AvailabilityRepository.findByProviderId`,
  exigindo unir duas fontes (regras e bloqueios) em todo ponto que hoje só lê
  `Availability`.
* **Exceção embutida na própria regra semanal (lista de datas excluídas no
  registro recorrente)**: descartada — misturaria o ciclo de vida de uma
  regra recorrente com o de uma exceção pontual no mesmo registro,
  dificultando criar, listar e remover bloqueios independentemente da regra.
* **Campo de tipo explícito (`AvailabilityType`: `WEEKLY_RULE` / `EXTRA_SLOT` /
  `BLOCK`)**: considerado por deixar a intenção mais explícita, mas descartado
  nesta versão para não migrar os registros já existentes; fica como
  refino possível se o modelo por convenção (item 2) causar confusão.
