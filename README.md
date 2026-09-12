# Servio

Marketplace de serviços locais que conecta clientes e prestadores: busca e
contratação de serviços, agenda, pedidos com jornada de status, notificações,
favoritos, avaliações, chat, dashboards e relatórios.

Projeto da disciplina **Desenvolvimento de Software com IA** (PPGTI / UFRN) —
evolução de uma base existente com foco em **Spec-Driven Development (SDD)** e
**harness de controle de agentes de IA**. Veja o processo completo em
[`docs/`](docs/README.md).

---

## Stack

| Camada | Tecnologia |
| :--- | :--- |
| Frontend | Angular 20 (standalone components) |
| Backend | Spring Boot 4 / Java 21 |
| Banco de dados | PostgreSQL |
| Documentação da API | springdoc-openapi (Swagger UI) |

## Estrutura

```
Servio/
├── servio-backend/     # API REST Spring Boot
├── servio-frontend/     # SPA Angular
├── docs/                 # SDD, ADRs, diagramas, harness, entregáveis
└── .githooks/            # guardrail de pre-commit (testes automatizados)
```

## Como rodar

### Pré-requisitos
PostgreSQL rodando localmente (o projeto assume a porta `5433`), Java 21, Node 18+.

### Backend
```bash
cd servio-backend
cp src/main/resources/application.properties.template src/main/resources/application.properties
# edite src/main/resources/application.properties com usuário/senha do seu Postgres
./mvnw spring-boot:run
```
API em `http://localhost:8080`; Swagger em `http://localhost:8080/swagger-ui.html`.

### Frontend
```bash
cd servio-frontend
npm install
npm start
```
App em `http://localhost:4200`.

### Guardrail (guia de contribuição)
```bash
git config core.hooksPath .githooks
```
Ativa o pre-commit que roda a suíte de testes do backend antes de cada commit
(ver [`docs/harness/guardrail-policy.md`](docs/harness/guardrail-policy.md)).

## Testes
```bash
cd servio-backend && ./mvnw test
```

---

## Documentação do processo

| O quê | Onde |
| :--- | :--- |
| Requisitos funcionais (16 RF) | [`docs/requisitos-funcionais.md`](docs/requisitos-funcionais.md) |
| Especificações (SDD) | [`docs/specs/`](docs/specs/) |
| Architecture Decision Records | [`docs/adr/`](docs/adr/) |
| Diagramas de arquitetura | [`docs/diagrams/`](docs/diagrams/) |
| Harness, guardrail e observabilidade | [`docs/harness/`](docs/harness/) |
| Ferramentas e estratégias de IA | [`docs/tools-and-prompts.md`](docs/tools-and-prompts.md) |
| Roteiro de demonstração | [`docs/roteiro-demo.md`](docs/roteiro-demo.md) |
| Documento de entrega | [`docs/documento-de-entrega.md`](docs/documento-de-entrega.md) |

**Autoras**: Bianca Antonelly, Maria Clara Fernandes
