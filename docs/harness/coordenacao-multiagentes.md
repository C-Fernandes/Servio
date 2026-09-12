# Coordenação de Múltiplos Agentes/Sessões em Paralelo

Registro da estratégia de isolamento e coordenação usada pela dupla, conforme
pedido pela seção IV da especificação da disciplina ("se a dupla trabalhou com
múltiplos agentes em paralelo").

- **Autoras**: Bianca Antonelly, Maria Clara Fernandes
- **Data**: 2026-09-12

---

## 1. Contexto

As duas integrantes trabalharam **em paralelo**, cada uma com sua própria sessão
de agente de IA, em domínios diferentes do sistema:

| Integrante  | Domínio                                              | Branches                                                                                                                               |
| :---------- | :--------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------- |
| Maria Clara | Favoritos, Jornada do Pedido, Notificações de Status | `docs/sdd-harness-guardrail`, `feat/rf01-favoritos`, `feat/rf01-favoritos-front`, `feat/rf13-jornada-pedido`, `feat/rf14-notificacoes` |
| Bianca      | Busca avançada de serviços, Chat cliente-prestador   | `feature/busca-avancada-servicos`, `feature/chat-cliente-prestador`                                                                    |

## 2. Estratégia de Isolamento

- **Isolamento por Git branch**, uma branch por funcionalidade/domínio — não
  houve execução simultânea de agentes sobre os mesmos arquivos.
- Cada branch parte da `main` (ou de uma branch anterior já revisada) e só
  entra na `main` via **Pull Request**.
- Os domínios foram escolhidos para **minimizar sobreposição de arquivos**:
  Maria Clara em `Order*`, `Favorite*`, `Notification*` e telas de
  pedidos/favoritos; Bianca em `Service*`, `ServiceSpecifications`,
  marketplace e o novo módulo de chat (`Conversation`, `Message`).
- Verificação prática: ao mergear `feature/busca-avancada-servicos` na branch de
  jornada/notificações, o `git diff --stat` entre as duas não mostrou nenhum
  arquivo em comum — o merge entrou sem conflito algum.

## 3. Coordenação

- **Guardrail independente por branch**: o `.githooks/pre-commit` roda a suíte
  de testes do backend antes de cada commit, em qualquer branch — cada agente
  tem sua própria barreira automática, não uma checagem centralizada só no
  final.
- **Revisão via Pull Request**: toda mudança passa por PR antes de entrar na
  `main`, com o diff revisado por quem não escreveu aquele trecho (Human-in-the-
  Loop também entre as integrantes, não só entre humano e agente).
- **Especificações como contrato de comunicação**: as SPECs em `docs/specs/`
  documentam o contrato de API de cada funcionalidade, permitindo que uma
  integrante entenda o que a outra construiu sem precisar ler todo o código.

## 4. Consequência Observada

- Nenhum conflito de merge nas integrações realizadas (favoritos → jornada →
  notificações → busca avançada → chat), confirmando que a divisão por domínio
  funcionou na prática.
