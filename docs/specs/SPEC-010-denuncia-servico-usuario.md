# [SPEC-010] Denúncia de Serviço ou Usuário

* **Autora**: Bianca Antonelly
* **Data da implementação**: 2026-09-08 (PR #15)
* **Data desta especificação**: 2026-09-12 (**retroativa** — ver seção 7)
* **Status**: Aprovado (retroativo)
* **Requisito do Projeto**: Requisito Funcional 20 (RF-20)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é implementar um mecanismo de denúncia de conteúdo inadequado, permitindo que
clientes reportem serviços ou usuários da plataforma para moderação por um administrador.
Restrições de Comportamento:
- Cliente denuncia um serviço ou um usuário, com motivo pré-definido e comentário livre.
- Cliente não pode denunciar o próprio serviço nem a si mesmo.
- Não deve ser possível acumular denúncias duplicadas: se já existe uma denúncia do mesmo
  denunciante para o mesmo alvo ainda pendente, reaproveitar essa denúncia em vez de criar outra.
- Só o admin lista e analisa todas as denúncias (com filtro por status); o cliente só vê as
  próprias.
- Ao marcar uma denúncia como "Revisada", a parte denunciada (dono do serviço ou o usuário)
  recebe uma notificação in-app.
```

---

## 2. Contexto e Motivação

A plataforma não tinha nenhum mecanismo de moderação: um serviço com conteúdo
impróprio ou um usuário com comportamento abusivo (fraude, assédio, perfil
falso) não podia ser reportado por ninguém além de contato direto com a
equipe. A **Denúncia** dá ao cliente um canal formal para reportar, e ao
admin uma fila de moderação com decisão e retorno automático pro denunciado.

---

## 3. Requisitos Funcionais

* **RF-20.1 (Criação de denúncia)**: cliente denuncia um serviço ou um
  usuário, escolhendo motivo (`INAPPROPRIATE_CONTENT`, `SPAM`, `FRAUD`,
  `HARASSMENT`, `FAKE_PROFILE`, `OTHER`) e descrição livre.
* **RF-20.2 (Prevenção de auto-denúncia)**: cliente não pode denunciar o
  próprio serviço nem a si mesmo.
* **RF-20.3 (Deduplicação de pendentes)**: uma nova denúncia do mesmo
  denunciante pro mesmo alvo, enquanto a anterior ainda estiver `PENDING`,
  reaproveita a denúncia existente em vez de criar uma nova.
* **RF-20.4 (Moderação pelo admin)**: admin lista todas as denúncias
  (com filtro opcional por status) e altera o status pra `REVIEWED` ou
  `DISMISSED`.
* **RF-20.5 (Notificação da parte denunciada)**: ao marcar como `REVIEWED`,
  o dono do serviço (se o alvo for `SERVICE`) ou o próprio usuário (se o
  alvo for `USER`) recebe notificação in-app informando que foi analisado
  pela moderação — sem revelar quem denunciou.

---

## 4. Critérios de Aceite (Gherkin — Given / When / Then)

### Cenário 1: Cliente denuncia um serviço de outro prestador
```gherkin
Dado que "Bianca" (cliente) está vendo o serviço "Encanamento" de outro prestador
Quando "Bianca" envia "POST /reports" com targetType=SERVICE, motivo=SPAM e uma descrição
Então o sistema deve retornar status HTTP 201 (Created)
E a denúncia deve aparecer em "GET /reports/my" pra Bianca com status PENDING
```

### Cenário 2: Admin revisa e o denunciado é notificado
```gherkin
Dado uma denúncia PENDING contra o serviço "Encanamento", de propriedade de "Julliane"
Quando o admin envia "PATCH /reports/{id}/status" com status=REVIEWED
Então o sistema deve retornar HTTP 200 com a denúncia atualizada e reviewedAt/reviewedBy preenchidos
E "Julliane" deve receber uma notificação in-app sobre a análise do serviço
```

### Cenário 3: Admin filtra denúncias por status
```gherkin
Dado que existem denúncias PENDING, REVIEWED e DISMISSED no sistema
Quando o admin consulta "GET /reports?status=PENDING"
Então o sistema deve retornar HTTP 200 apenas com as denúncias PENDING
```

### Caso de Borda 1 (Edge Case — Denúncia duplicada ainda pendente):
```gherkin
Dado que "Bianca" já denunciou o serviço "Encanamento" e essa denúncia segue PENDING
Quando "Bianca" tenta denunciar o mesmo serviço de novo, com motivo diferente
Então o sistema não deve criar uma segunda denúncia
E deve retornar HTTP 201 com os dados da denúncia PENDING já existente
```

### Caso de Borda 2 (Edge Case — Auto-denúncia bloqueada):
```gherkin
Dado que "Julliane" (prestadora) é dona do serviço "Encanamento"
Quando "Julliane" tenta denunciar o próprio serviço "Encanamento"
Então o sistema deve recusar com uma BusinessException ("Você não pode denunciar o próprio serviço.")
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/reports` | CLIENT | `201 Created` (ReportResponseDTO) | 404 (alvo não encontrado), 400 (auto-denúncia, `BusinessException`) |
| `GET` | `/reports/my` | CLIENT | `200 OK` (List\<ReportResponseDTO\>) | — |
| `GET` | `/reports?status=` | ADMIN | `200 OK` (List\<ReportResponseDTO\>) | — |
| `PATCH` | `/reports/{id}/status` | ADMIN | `200 OK` (ReportResponseDTO) | 404 (denúncia não encontrada) |

`ReportTargetType`: `SERVICE` \| `USER`. `ReportReason`: `INAPPROPRIATE_CONTENT`,
`SPAM`, `FRAUD`, `HARASSMENT`, `FAKE_PROFILE`, `OTHER`. `ReportStatus`:
`PENDING`, `REVIEWED`, `DISMISSED`.

---

## 6. Plano de Tarefas (Tasklist)

- [x] **T1 (Modelo)**: entidade `Report` + enums `ReportTargetType`, `ReportReason`, `ReportStatus`.
- [x] **T2 (Repository)**: `ReportRepository` com queries por status, por denunciante+alvo+status (dedupe).
- [x] **T3 (Camada de Serviço)**: `ReportService` — cria com validação de auto-denúncia e dedupe de pendente; lista própria/todas; atualiza status e dispara notificação.
- [x] **T4 (Camada de Controller)**: `ReportController` com `@Client`/`@Admin` por rota.
- [x] **T5 (Frontend — denunciar)**: `report-modal` acionável a partir de serviço/usuário.
- [x] **T6 (Frontend — moderação)**: página "Denúncias" (`/reports`, admin) — listar, filtrar por status, decidir.
- [ ] **T7 (Testes automatizados)**: cobrir `ReportService` (dedupe de pendente, auto-denúncia, notificação ao revisar) — **pendente, gap reconhecido**.
- [ ] **T8 (ADR)**: decisão de modelagem (motivo fixo vs. livre, deduplicação por pendente) — **pendente, gap reconhecido**.

---

## 7. Nota de Transparência (Retroatividade)

Escrita após a implementação — e depois de já estar mergeada em `main` desde
08/09 (PR #15). Diferente das SPEC-007/008/009, RF-20 não tinha **nenhuma**
documentação (nem RF na lista, nem SPEC) até a checagem de conformidade de
12/09 identificar o gap. Validado manualmente via chamadas à API e navegador;
não recebeu testes automatizados nem ADR dedicado. Maior gap de processo do
projeto, reconhecido como aprendizado (ver seção V.7 da apresentação).
