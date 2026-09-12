# Documento de Entrega — Projeto Servio

Disciplina: **Desenvolvimento de Software com IA** (PPGTI / UFRN)
Professor: Jean Mário Moreira de Lima
Data da apresentação: **12/09/2026**

---

## Dupla

| Nome completo | Matrícula |
| :--- | :--- |
| Bianca Antonelly | 20261009476 |
| Maria Clara Fernandes | 20261009547 |

## Links

| Item | Link |
| :--- | :--- |
| Repositório do projeto | https://github.com/C-Fernandes/Servio |
| Post no LinkedIn (método STAR) | [preencher após publicar] |

## Entregáveis no repositório

| Entregável | Onde |
| :--- | :--- |
| Código-fonte + histórico de commits real | raiz do repositório |
| Apresentação (PPT) | [preencher — anexar ou linkar] |
| Documento de modelos, ferramentas e estratégias de IA | `docs/tools-and-prompts.md` |
| Especificações (SDD) | `docs/specs/SPEC-001` a `SPEC-011` |
| Architecture Decision Records | `docs/adr/ADR-001` a `ADR-010` |
| Diagramas de arquitetura (Mermaid) | `docs/diagrams/` |
| Política e evidência de guardrail | `docs/harness/guardrail-policy.md`, `docs/harness/evidencia-guardrail-bloqueio.md` |
| Transcript de sessão do agente | `docs/harness/sessao-2026-09-09-a-12.md` |
| Coordenação de trabalho em paralelo | `docs/harness/coordenacao-multiagentes.md` |
| Lista de requisitos funcionais | `docs/requisitos-funcionais.md` |
| Roteiro de demonstração | `docs/roteiro-demo.md` |

## Resumo do projeto

O Servio é uma plataforma de marketplace de serviços locais que conecta
clientes e prestadores. A dupla evoluiu uma base já funcional (autenticação,
serviços, pedidos, avaliações, agenda, dashboards) implementando **21
requisitos funcionais** no total, todos construídos via Spec-Driven
Development (RF-05, RF-12 a RF-21), com guardrail de IA ativo com evidência
real de bloqueio e observabilidade completa do processo (specs, ADRs,
transcript de sessão) — ver `docs/checklist-final.md`.
