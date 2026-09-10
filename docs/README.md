# Documentação de Engenharia e Governança de IA - Servio

Este diretório centraliza a documentação de engenharia de software, processos assistidos por Inteligência Artificial, decisões arquiteturais e especificações funcionais do projeto **Servio**, desenvolvido para a disciplina de **Desenvolvimento de Software com IA** (PPGTI / UFRN).

---

## Estrutura do Diretório

```
docs/
├── README.md               # Este arquivo (índice geral)
├── adr/                    # Architecture Decision Records (ADRs)
│   ├── template-adr.md
│   ├── ADR-001-adocao-sdd-e-harness-de-ia.md
│   └── ADR-002-modelagem-sistema-favoritos.md
├── specs/                  # Especificações funcionais (Spec-Driven Development)
│   └── SPEC-001-sistema-de-favoritos.md
├── harness/                # Configurações de Harness, Guardrails e Observabilidade
│   └── guardrail-policy.md
├── diagrams/               # Diagramas de arquitetura e modelo (Mermaid)
│   └── diagrama-favoritos.md
└── tools-and-prompts.md    # Registro de ferramentas, modelos de IA e estratégias de prompt
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
O projeto atende a todos os requisitos solicitados:
* [x] Mínimo de 10 requisitos funcionais claramente identificáveis e demonstráveis.
* [x] Aplicação prática de Spec-Driven Development (SDD) com casos de borda.
* [x] Harness de controle com guardrail ativo e evidência de bloqueio real.
* [x] Architecture Decision Records (ADRs) e diagramas Mermaid.
* [x] Testes automatizados cobrindo regras de negócio.
* [x] Apresentação técnica e divulgação pelo método STAR no LinkedIn.
