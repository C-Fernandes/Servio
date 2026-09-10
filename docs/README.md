# Documentação de Engenharia e Governança de IA - Servio

Este diretório centraliza a documentação de engenharia de software, processos assistidos por Inteligência Artificial, decisões arquiteturais e especificações funcionais do projeto **Servio**, desenvolvido para a disciplina de **Desenvolvimento de Software com IA** (PPGTI / UFRN).

---

## Estrutura do Diretório

```
docs/
├── README.md                     # Este arquivo (índice geral)
├── requisitos-funcionais.md      # Lista dos 13 RF do sistema e evidências
├── tools-and-prompts.md          # Ferramentas, modelos de IA e estratégias de prompt
├── adr/                          # Architecture Decision Records (ADRs)
│   ├── template-adr.md
│   ├── ADR-001-adocao-sdd-e-harness-de-ia.md
│   ├── ADR-002-modelagem-sistema-favoritos.md
│   ├── ADR-003-historico-de-status-e-jornada-do-pedido.md
│   └── ADR-004-notificacoes-in-app-melhor-esforco.md
├── specs/                        # Especificações funcionais (Spec-Driven Development)
│   ├── SPEC-001-sistema-de-favoritos.md
│   ├── SPEC-002-jornada-do-pedido.md
│   └── SPEC-003-notificacoes-de-status.md
├── harness/                      # Harness, guardrails e observabilidade
│   ├── guardrail-policy.md
│   └── evidencia-guardrail-bloqueio.md
└── diagrams/                     # Diagramas (Mermaid)
    ├── diagrama-favoritos.md
    ├── arquitetura-geral.md
    ├── jornada-do-pedido.md
    └── notificacoes-de-status.md
```

---

## 1. Processo de Spec-Driven Development (SDD)
O desenvolvimento das novas funcionalidades segue o fluxo:
1. **Definição de Prompt e Comportamento (Behavior)**: Definição clara do papel do agente de IA e escopo da tarefa.
2. **Especificação de Requisitos e Critérios de Aceite**: Formalização no formato Gherkin (`Given / When / Then`), incluindo explicitamente casos de borda (*edge cases*).
3. **Plano de Tarefas e Contrato de API**: Mapeamento prévio dos endpoints REST e estrutura de dados antes de qualquer código ser escrito.
4. **Implementação Guiada**: Execução orientada a testes no Backend e componentes no Frontend.
5. **Validação de Critérios de Aceite**: Homologação dos critérios estabelecidos na especificação.

---

## 2. Harness e Nível de Autonomia
* **Nível de Autonomia**: **Supervisionado / Human-in-the-Loop (Plan-Review-Execute)**.
* **Guardrails**: Automações via Git Hooks (`.githooks/pre-commit`) que impedem a integração de código se os testes automatizados ou verificações de integridade falharem.
* **Observabilidade**: Registro de logs de sessão do agente, documentação de diffs e rastreabilidade entre commits e especificações.

---

## 3. Matriz de Requisitos da Disciplina

| Requisito da disciplina | Situação |
| :--- | :--- |
| Mínimo de 10 requisitos funcionais demonstráveis | ✅ 14 RF (ver [requisitos-funcionais.md](requisitos-funcionais.md)) |
| SDD com critérios de aceite e casos de borda | ✅ [SPEC-001](specs/SPEC-001-sistema-de-favoritos.md), [SPEC-002](specs/SPEC-002-jornada-do-pedido.md), [SPEC-003](specs/SPEC-003-notificacoes-de-status.md) |
| Harness com guardrail ativo e evidência de bloqueio real | ✅ [guardrail-policy.md](harness/guardrail-policy.md), [evidencia-guardrail-bloqueio.md](harness/evidencia-guardrail-bloqueio.md) |
| Pelo menos um ADR de decisão relevante | ✅ ADR-001 a ADR-004 |
| Diagrama de arquitetura (C4/Mermaid) | ✅ [arquitetura-geral.md](diagrams/arquitetura-geral.md) e diagramas por RF |
| Testes automatizados cobrindo parte dos RF | ✅ 30 testes (RF-12, RF-13, RF-14) |
| Transcript de sessão do agente (observabilidade) | ⏳ pendente |
| Apresentação (PPT) e post no LinkedIn (STAR) | ⏳ pendente |
