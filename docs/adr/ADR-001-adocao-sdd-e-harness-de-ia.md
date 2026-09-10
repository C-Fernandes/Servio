# [ADR-001] Adoção de Spec-Driven Development (SDD) e Harness de Controle de Agentes de IA

* **Status**: Aceito
* **Data**: 2026-09-08
* **Autores**: Bianca Antonelly, Maria Clara Fernandes
* **Contexto**: Projeto de Evolução da Plataforma Servio (Desenvolvimento de Software com IA - PPGTI / UFRN)

---

## 1. Contexto e Declaração do Problema
O desenvolvimento assistido por modelos de linguagem e agentes autônomos de código pode introduzir riscos como alucinação de regras de negócio, regressão em código preexistente, modificações desestruturadas e commits contendo código quebrado ou sem cobertura de testes. Para mitigar esses riscos e assegurar a qualidade e auditabilidade do software no projeto Servio, fez-se necessária a definição de uma governança rigorosa sobre a interação humana com os agentes de IA.

## 2. Direcionadores de Decisão (Drivers)
* **Previsibilidade e Determinismo**: Evitar que a IA gere código sem entendimento explícito dos requisitos e casos de borda.
* **Segurança e Integridade**: Impedir que código incompleto ou com testes quebrando entre na ramificação principal (`main`).
* **Auditabilidade e Observabilidade**: Manter rastreabilidade completa entre as solicitações (prompts), especificações de requisitos, planos de tarefas e os diffs gerados.
* **Conformidade Acadêmica**: Atendimento aos critérios avaliativos da disciplina em relação a SDD e Harness.

## 3. Decisão Considerada e Aprovada
Adotou-se o modelo de **Spec-Driven Development (SDD)** integrado a um **Harness de Controle com Nível de Autonomia Supervisionado (Human-in-the-Loop)**:

1. **Nível de Autonomia**:
   * **Supervisionado / Human-in-the-Loop (Plan-Review-Execute)**:
     * O agente tem permissão para inspecionar arquivos, pesquisar a base de código e propor planos de implementação detalhados (`implementation_plan.md`).
     * A execução do plano só é iniciada após aprovação humana explícita.
     * Diffs de código e commits no repositório exigem revisão humana prévia.
2. **Ciclo SDD**:
   * Nenhuma funcionalidade nova é codificada sem uma especificação estruturada prévia em `docs/specs/SPEC-XXX.md`.
   * A especificação deve conter obrigatoriamente: Behavior prompt, Requisitos funcionais, Critérios de aceite em formato Gherkin (`Given / When / Then`), casos de borda e tasklist.
3. **Mecanismo de Guardrail Técnico**:
   * Implementação de um Git pre-commit hook localizado em `.githooks/pre-commit`.
   * O guardrail executa compilação e suítes de testes unitários do backend antes de aceitar qualquer comando de commit. Caso haja erro de sintaxe ou teste quebrado, o commit é abortado automaticamente (`exit 1`).
4. **Observabilidade**:
   * Registro sistemático de logs de sessões, transcrições de comandos e decisões de arquitetura via ADR.

## 4. Consequências
### Positivas:
* **Zero regressões silenciosas**: O guardrail impede que falhas em regras existentes sejam integradas inadvertidamente.
* **Alinhamento preciso de escopo**: A especificação prévia esclarece dúvidas e ambiguidades de negócio antes do esforço de codificação.
* **Histórico limpo e rastreável**: Commits atômicos e associados a planos de tarefas concluídos.

### Negativas / Trade-offs:
* **Overhead de tempo inicial**: Escrever a especificação e os critérios Gherkin antes da codificação exige planejamento cuidadoso antes da implementação imediata.
* **Tempo de validação pré-commit**: A execução de testes unitários a cada commit adiciona alguns segundos ao ciclo de desenvolvimento local.

## 5. Alternativas Consideradas
* **Autonomia Total (Unsupervised)**: Agente com permissão irrestrita para gerar código e comitar diretamente. *Descartada* pelo alto risco de alucinação de regras de negócio e falta de controle sobre a base de código.
* **Desenvolvimento Ad-hoc orientado apenas a prompts informais no chat**: *Descartada* por violar a metodologia de SDD e dificultar a manutenção de critérios de aceite claros e testáveis.
