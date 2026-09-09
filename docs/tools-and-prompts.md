# Modelos, Ferramentas e Estratégias de IA — Servio

Documento de entrega exigido pela seção VI da especificação da disciplina
**Desenvolvimento de Software com IA** (PPGTI / UFRN). Registra, de forma
objetiva, os modelos de IA, as ferramentas e as estratégias de prompt
efetivamente utilizadas ao longo do desenvolvimento do projeto de evolução da
plataforma **Servio**.

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09
* **Documentos relacionados**: [ADR-001](adr/ADR-001-adocao-sdd-e-harness-de-ia.md), [Política de Guardrail](harness/guardrail-policy.md)

---

## 1. Modelos de IA

| Modelo | Uso no projeto |
| :--- | :--- |
| **Claude Sonnet 5** (Anthropic) | Modelo principal. Exploração da base de código, redação de especificações (SPEC), geração de código guiada por spec, escrita de testes automatizados, revisão de diffs e produção de documentação (ADR, diagramas). |
| **ChatGPT / GPT-4-class** (OpenAI) | Apoio pontual: brainstorming de requisitos, revisão de redação de textos e conferência cruzada de decisões de arquitetura. |

Nenhum modelo teve permissão para commitar diretamente. Todo código gerado
passou por revisão humana antes de ser integrado (ver seção 4).

---

## 2. Ferramentas

### 2.1 Ambiente de desenvolvimento assistido
| Ferramenta | Papel |
| :--- | :--- |
| **Claude Code** (CLI agent) | Harness principal de controle do agente. Executa o ciclo Plan → Review → Execute, roda comandos de build/test, aplica edições em arquivos mediante aprovação e mantém transcrição da sessão para observabilidade. |
| **VS Code** | Editor, navegação de código e revisão visual de diffs antes da aceitação. |
| **Git + GitHub** | Controle de versão, histórico de commits atômicos ligados às tasks das SPECs e revisão via Pull Request. |

### 2.2 Build, teste e qualidade
| Ferramenta | Papel |
| :--- | :--- |
| **Maven** (`mvnw`) | Build e ciclo de testes do backend. |
| **JUnit 5** | Framework de testes automatizados do backend. |
| **Mockito** | Dublês de teste (mocks) para isolar a camada de serviço nos testes unitários. |
| **Git pre-commit hook** (`.githooks/pre-commit`) | Guardrail técnico: executa a suíte de testes do backend e aborta o commit em caso de falha. Ver [guardrail-policy.md](harness/guardrail-policy.md). |

### 2.3 Stack da aplicação (contexto)
Angular (frontend), Spring Boot 4 / Java 21 (backend), PostgreSQL (banco),
springdoc-openapi (documentação da API).

---

## 3. Formato de Especificação (SDD) e Justificativa

O projeto adota um **formato próprio de especificação estruturada em Markdown**,
versionado em `docs/specs/SPEC-NNN-*.md`, com seções obrigatórias:

1. Prompt inicial de comportamento (*behavior*)
2. Contexto e motivação
3. Requisitos funcionais identificados
4. Critérios de aceite em **Gherkin** (`Given / When / Then`), incluindo ao
   menos um caso de borda explícito
5. Contrato da API REST (método, endpoint, perfil, respostas, erros)
6. Plano de tarefas (*tasklist* rastreável)

**Por que um formato próprio em vez de OpenSpec / GitHub Spec-Kit / Traycer.ai:**

* **Aderência ao critério da disciplina sem dependência externa** — o formato
  cobre exatamente os itens exigidos (behavior, requisitos, critérios de aceite
  com edge case, tasklist) sem introduzir uma ferramenta adicional para instalar,
  aprender e manter no curto prazo do projeto (apresentação em 12/09).
* **Rastreabilidade direta no repositório** — cada SPEC vive junto ao código,
  entra no mesmo fluxo de Pull Request e é referenciada nas mensagens de commit
  pelas tasks (`T1`…`Tn`), tornando o processo SDD auditável sem tooling próprio.
* **Integração com o harness** — o agente de IA lê a SPEC como contrato de
  entrada e valida os critérios de aceite via testes automatizados, sem precisar
  de um parser ou formato específico de terceiros.
* **Equivalência funcional** — a estrutura espelha os blocos centrais de
  OpenSpec e do GitHub Spec-Kit (intenção → requisitos → critérios → plano),
  atendendo ao requisito de "formato equivalente documentado pela dupla".

Cada SPEC é acompanhada, quando há decisão de arquitetura relevante, de um ADR
correspondente em `docs/adr/`.

---

## 4. Nível de Autonomia e Estratégias de Prompt

### 4.1 Nível de autonomia
**Supervisionado / Human-in-the-Loop (Plan → Review → Execute)**, conforme
[ADR-001](adr/ADR-001-adocao-sdd-e-harness-de-ia.md):

* O agente inspeciona a base de código e propõe um plano de implementação.
* A execução só começa após aprovação humana explícita do plano.
* Todo diff é revisado antes de ser aceito; nenhum commit é feito pelo agente
  sem revisão.

### 4.2 Estratégias de prompt adotadas
| Estratégia | Descrição |
| :--- | :--- |
| **Spec-Driven prompting** | O prompt inicial de cada funcionalidade é a própria SPEC (behavior + restrições de negócio + critérios de aceite). O agente implementa contra o contrato, não contra uma descrição livre. |
| **Restrições de comportamento explícitas** | Cada SPEC declara regras invioláveis (ex.: "apenas role CLIENT", "não duplicar registros", "respostas HTTP semânticas") que o agente deve respeitar. |
| **Plan-first / aprovação de escopo** | Antes de editar arquivos, o agente apresenta o plano de tarefas e aguarda confirmação da dupla. |
| **Test-first nos casos de borda** | Os edge cases do Gherkin são convertidos em testes automatizados antes ou junto da implementação, e usados como critério de "pronto". |
| **Revisão de diff obrigatória** | Toda alteração é lida e aprovada manualmente; o guardrail de pre-commit atua como segunda barreira automática. |
| **Commits atômicos rastreáveis** | Uma task da SPEC por commit, com a mensagem referenciando o identificador da task. |

---

## 5. Observabilidade do Processo

* **Transcrições de sessão do agente** arquivadas em `docs/harness/` (logs de
  comandos, planos propostos e decisões).
* **ADRs** registrando decisões de arquitetura relevantes.
* **Histórico de commits** refletindo o processo real (commits incrementais por
  task, não um único commit final).
* **Diagramas Mermaid** em `docs/diagrams/` gerados e revisados com apoio de IA.
