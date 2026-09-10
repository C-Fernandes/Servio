# Política de Guardrail e Observabilidade do Harness — Servio

Documento que descreve o mecanismo de guardrail técnico configurado no projeto,
o nível de autonomia adotado para os agentes de IA e a forma de observação do
processo. Complementa a [ADR-001](../adr/ADR-001-adocao-sdd-e-harness-de-ia.md).

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09

---

## 1. Nível de Autonomia

**Supervisionado / Human-in-the-Loop (Plan → Review → Execute).**

| Fase | O que o agente pode fazer | O que exige humano |
| :--- | :--- | :--- |
| **Plan** | Ler arquivos, pesquisar a base, propor plano de tarefas e contrato de API. | — |
| **Review** | Apresentar diffs propostos. | Aprovação explícita do plano e de cada diff. |
| **Execute** | Aplicar edições aprovadas, rodar build e testes. | Autorização do commit; o commit em si é feito pela dupla. |

Autonomia total (agente comitando direto) foi descartada na ADR-001 pelo risco
de alucinação de regras de negócio e regressão silenciosa.

---

## 2. Guardrail Técnico: Git pre-commit hook

### 2.1 Localização e ativação
* Script: [`.githooks/pre-commit`](../../.githooks/pre-commit)
* Ativação no repositório:

  ```bash
  git config core.hooksPath .githooks
  ```

  Já configurado neste repositório (verificável com
  `git config --get core.hooksPath`, que retorna `.githooks`).

### 2.2 O que o guardrail faz
1. Localiza a raiz do repositório e o módulo `servio-backend`.
2. Garante um `JAVA_HOME` válido para o ambiente do hook.
3. Executa a suíte de testes do backend:

   ```bash
   mvn test -Dtest=*Test -DfailIfNoTests=false -q
   ```

4. **Se os testes falharem ou o código não compilar**, o hook imprime um bloco
   de bloqueio e encerra com `exit 1` — o commit é **abortado**.
5. Se todos os testes passarem, encerra com `exit 0` e o commit prossegue.

### 2.3 Objetivo
Impedir que código quebrado, sem compilar ou que cause regressão em regras de
negócio existentes entre na ramificação. É a segunda barreira automática depois
da revisão humana de diff.

---

## 3. Evidência de Bloqueio Real

Registrada em [`evidencia-guardrail-bloqueio.md`](evidencia-guardrail-bloqueio.md):
em 2026-09-09 uma asserção do `FavoriteServiceTest` foi quebrada de propósito, o
`pre-commit` executou `mvn test`, detectou a falha, imprimiu `COMMIT REJEITADO` e
encerrou com `exit 1`; o `git log` confirma que o HEAD não avançou.

Procedimento para gerar a evidência:

1. Introduzir uma falha proposital em um teste (ex.: alterar uma asserção em
   `FavoriteServiceTest` para um valor incorreto).
2. Executar `git add` e `git commit -m "teste: evidência de guardrail"`.
3. Observar a saída:

   ```text
   ==================================================================
   🛡️  [GUARDRAIL DE IA] Executando verificações de integridade...
   ==================================================================
   🔍 [GUARDRAIL] Executando testes automatizados do Backend...
   ...
   🚨 [GUARDRAIL BLOQUEIO ATIVO] COMMIT REJEITADO!
   ```

4. Confirmar com `git log` que **nenhum commit foi criado**.
5. Reverter a falha proposital, refazer o commit e confirmar que agora passa.

Guardar o log de terminal (texto ou screenshot) em `docs/harness/` como
`evidencia-guardrail-bloqueio.md` / `.png`.

---

## 4. Observabilidade

| Artefato | Onde |
| :--- | :--- |
| Transcrições de sessão do agente (comandos, planos, decisões) | `docs/harness/sessao-*.md` |
| Decisões de arquitetura | `docs/adr/ADR-*.md` |
| Especificações e critérios de aceite | `docs/specs/SPEC-*.md` |
| Rastreabilidade spec ↔ código | Mensagens de commit referenciando as tasks (`T1`…`Tn`) |
| Diagramas gerados/revisados com IA | `docs/diagrams/*.md` |

---

## 5. Checklist de Conformidade (seção IV da especificação da disciplina)

- [x] Nível de autonomia definido e justificado (ADR-001, seção 1 deste doc)
- [x] Guardrail configurado de fato (`.githooks/pre-commit` + `core.hooksPath`)
- [x] Evidência de bloqueio real registrada (seção 3 / `evidencia-guardrail-bloqueio.md`)
- [x] Observabilidade: transcript de sessão + revisão de diffs antes de aceitar
- [x] Pelo menos um ADR de decisão relevante (ADR-001, ADR-002)
- [x] Diagrama de arquitetura/modelo com apoio de IA (`docs/diagrams/`)
