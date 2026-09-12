# Checklist Final — Conformidade com a Especificação da Disciplina

Verificação item a item do que o professor Jean Mário pediu na especificação
**Desenvolvimento de Software com IA** (PPGTI/UFRN), seção por seção. Legenda:
✅ feito · ⚠️ parcial/gap reconhecido · ❌ pendente.

* **Data desta checagem**: 2026-09-12 (dia da apresentação)

---

## I. Informes

| Item | Status | Evidência |
| :--- | :---: | :--- |
| E-mail de checkpoint (05/09) com membros, ideia e frameworks | ✅ | enviado em 09/09 (ver histórico da conversa) |
| Controle de versão colaborativo (GitHub) com histórico real de commits | ✅ | `git log` — 14 PRs, dezenas de commits, sem "1 commit final" |
| Documento antes da apresentação: nomes + link repo + link do post | ⚠️ | `docs/documento-de-entrega.md` — matrículas e link do repo preenchidos; **falta colar o link do post do LinkedIn após publicar** |

## II. Proposta

| Item | Status | Evidência |
| :--- | :---: | :--- |
| Sistema completo e funcional, complexidade adequada ao tempo | ✅ | base existente + 9 RF novos (favoritos, busca avançada, jornada, notificações, relatório, chat, bloqueio de horários, cupons, denúncia) + histórico de interações |
| Ênfase em SDD e Harness | ✅ | `docs/specs/`, `docs/adr/`, `docs/harness/` |

## III. Requisitos do Sistema

| Item | Status | Evidência |
| :--- | :---: | :--- |
| Mínimo 10 RF claramente identificáveis e demonstráveis | ✅ | **20 RF** — `docs/requisitos-funcionais.md` |
| Plataforma livre e justificada | ✅ | Web (Angular + Spring Boot), evolução de base já web |
| Arquitetura: modularidade, baixo acoplamento, contratos claros | ✅ | `docs/diagrams/arquitetura-geral.md`, `docs/adr/ADR-001` |
| Controle de versão com histórico real ao longo do desenvolvimento | ✅ | commits incrementais por task, branches por domínio, 14 PRs |
| Testes automatizados cobrindo parte dos RF | ⚠️ | **65 testes automatizados no backend**, cobrindo os 10 RF construídos além da base (RF-05, RF-12 a RF-20); **frontend sem testes automatizados escritos** (gap reconhecido) |

## IV. Processo de Desenvolvimento Assistido por IA

### Spec-Driven Development (SDD)
| Item | Status | Evidência |
| :--- | :---: | :--- |
| Especificação com behavior, requisitos, critérios de aceite (G/W/T) com edge case, tasklist | ✅ | `SPEC-001` a `SPEC-006` (favoritos, jornada, notificações, relatório, bloqueio de horários, cupons) |
| Formato de spec estruturado + justificativa | ✅ | `docs/tools-and-prompts.md` seção 3 |
| **Gap reconhecido (fechado)** | ✅ | RF-05 (busca avançada), RF-16 (chat), RF-19 (histórico de interações) e RF-20 (denúncia) foram construídos **fora** do fluxo de SDD no momento da implementação — nenhum teve SPEC ou ADR escrito **antes** de codificar, como o fluxo padrão do projeto prevê. Todos os quatro receberam o pacote retroativo completo em 12/09: SPEC ([SPEC-007](specs/SPEC-007-busca-avancada-servicos.md), [SPEC-008](specs/SPEC-008-chat-cliente-prestador.md), [SPEC-009](specs/SPEC-009-historico-interacoes.md), [SPEC-010](specs/SPEC-010-denuncia-servico-usuario.md)), teste automatizado (`ServiceServiceSearchTest`, `ChatServiceTest`, `InteractionServiceTest`, `ReportServiceTest`) e ADR nos três com decisão de arquitetura real ([ADR-006](adr/ADR-006-modelagem-chat-cliente-prestador.md) chat, [ADR-007](adr/ADR-007-modelagem-historico-interacoes.md) histórico, [ADR-008](adr/ADR-008-modelagem-sistema-denuncias.md) denúncia — RF-05 fica sem ADR por decisão consciente, a própria SPEC-007 justifica que não há decisão nova o bastante). Retroatividade em si segue como aprendizado real do processo (seção V.7) |
| **Feature não mergeada, fora de escopo** | ⚠️ | "Recomendações de serviços" (Bianca) existe pronta na branch `origin/feature/recomendacoes-servicos`, merge sem conflito confirmado, mas **decisão consciente de não mergear** antes da apresentação — fica fora do escopo, não conta como RF, não demonstrar na demo |

### Harness e controle de agentes
| Item | Status | Evidência |
| :--- | :---: | :--- |
| Nível de autonomia definido e justificado | ✅ | `ADR-001` — Supervisionado/Human-in-the-Loop |
| Guardrail configurado de fato | ✅ | `.githooks/pre-commit` + `core.hooksPath` |
| Evidência de bloqueio real (não só descrito) | ✅ | `docs/harness/evidencia-guardrail-bloqueio.md` + reensaiado ao vivo em 12/09 (40 testes, exit code 1) |
| Observabilidade: transcript de sessão + revisão de diffs | ✅ | `docs/harness/sessao-2026-09-09-a-12.md` |

### Arquitetura e documentação
| Item | Status | Evidência |
| :--- | :---: | :--- |
| Pelo menos 1 ADR de decisão relevante | ✅ | `ADR-001` a `ADR-008` (8 ADRs) |
| Diagrama de arquitetura (C4/Mermaid), com apoio de IA | ✅ | `docs/diagrams/arquitetura-geral.md` + 3 diagramas por RF |

### Múltiplos agentes em paralelo
| Item | Status | Evidência |
| :--- | :---: | :--- |
| Estratégia de isolamento e coordenação descrita | ✅ | `docs/harness/coordenacao-multiagentes.md` |

## V. Apresentação (mínimo 7 pontos)

| Ponto | Status | Onde puxar o conteúdo |
| :--- | :---: | :--- |
| 1. Contexto e motivação | ✅ | `docs/post-linkedin.md` (Situação/Tarefa), e-mail de checkpoint |
| 2. Processo de especificação (SDD) | ✅ | `docs/specs/` |
| 3. Harness (autonomia, guardrails, controle) | ✅ | `docs/adr/ADR-001`, `docs/harness/` |
| 4. Decisões de arquitetura (ADR + diagrama) | ✅ | `docs/adr/`, `docs/diagrams/` |
| 5. Modelos, ferramentas e estratégias de IA | ✅ | `docs/tools-and-prompts.md` |
| 6. Demonstração funcional ao vivo | ✅ | `docs/roteiro-demo.md` |
| 7. Aprendizados, dificuldades, o que faria diferente | ✅ | rascunhado no `docs/post-linkedin.md` (gap de SDD em busca/chat como aprendizado); **detalhar no PPT** |
| **PPT (ou equivalente) montado** | ❌ | **pendente** — conteúdo dos 7 pontos já está todo escrito nos docs acima, falta só montar os slides |

## VI. Entregáveis

| Item | Status | Evidência |
| :--- | :---: | :--- |
| Código-fonte + histórico de commits real | ✅ | github.com/C-Fernandes/Servio |
| Apresentação (PPT) | ❌ | pendente |
| Documento de modelos/ferramentas/estratégias | ✅ | `docs/tools-and-prompts.md` |
| Post no LinkedIn (STAR) | ⚠️ | rascunho pronto em `docs/post-linkedin.md`; **falta publicar** e colar o link no documento de entrega |

## VII. Divulgação — Método STAR

| Item | Status |
| :--- | :---: |
| Situação, Tarefa, Ação, Resultado no texto | ✅ (rascunho) |
| Conteúdo visual (print/GIF/link da demo) | ⚠️ anexar ao publicar |
| Publicado de fato | ❌ pendente |

## VIII. Critérios de Avaliação — autoavaliação honesta

| Critério | Peso | Autoavaliação |
| :--- | :--- | :--- |
| Sistema completo, 10+ RF, criatividade/pertinência | 2,0 | Forte — 20 RF, 10 novos além da base |
| SDD: spec, critérios de aceite, plano de tarefas | 2,0 | Forte nos 6 RF via SDD desde o início; gap declarado nos outros 4 (SPEC/ADR/teste escritos depois, não antes) |
| Harness: autonomia, guardrail funcional, observabilidade | 2,0 | Forte — bloqueio real demonstrado 2x, transcript completo |
| Arquitetura: modularidade, ADR, diagrama | 1,5 | Forte — 8 ADRs, 4 diagramas |
| Demo ao vivo | 1,0 | Depende do ensaio — roteiro pronto |
| Qualidade da apresentação | 1,0 | Depende do PPT — ainda não montado |
| Entregáveis completos (repo, PPT, doc ferramentas, LinkedIn) | 0,5 | PPT e post ainda pendentes |

---

## O que falta literalmente fazer (em ordem)

1. **Montar o PPT** — todo o conteúdo já está escrito, é consolidar nos slides.
2. **Publicar o post no LinkedIn** (com print/GIF) e colar o link em `docs/documento-de-entrega.md`.
3. **Ensaiar a demo** uma vez seguindo `docs/roteiro-demo.md` (já cobre RF-17/18/19; falta RF-20 no roteiro).
4. **Limpar branches locais mortas** com `git branch -d ...` (lista dada pela IA) — bloqueado pro assistente rodar, pendente de vocês.

Gap de SPEC/ADR/teste retroativo em RF-05, RF-16, RF-19 e RF-20 já fechado
(12/09) — não é mais item pendente.
