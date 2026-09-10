# Diagramas de Arquitetura Geral — Servio

Visão arquitetural do sistema **Servio**, no estilo **C4** (Contexto → Contêineres
→ Componentes), com um diagrama de sequência do fluxo de requisição autenticada.
Diagramas produzidos em Mermaid com apoio de IA (Claude) e revisados pela dupla.

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-09
* **Relacionados**: [ADR-001](../adr/ADR-001-adocao-sdd-e-harness-de-ia.md) · [diagrama de favoritos](diagrama-favoritos.md)

---

## 1. Nível 1 — Contexto

```mermaid
flowchart TB
    cliente["Cliente<br/><i>busca e contrata serviços</i>"]
    prestador["Prestador<br/><i>oferece serviços, gerencia agenda e pedidos</i>"]
    admin["Administrador<br/><i>gerencia categorias e tags</i>"]

    servio(["<b>Servio</b><br/>Marketplace de serviços locais<br/>(aplicação web)"])

    db[("PostgreSQL<br/><i>dados da aplicação</i>")]
    disco[["Sistema de arquivos<br/><i>imagens dos serviços</i>"]]

    cliente -->|"HTTPS"| servio
    prestador -->|"HTTPS"| servio
    admin -->|"HTTPS"| servio
    servio -->|"JDBC"| db
    servio -->|"leitura/escrita"| disco
```

---

## 2. Nível 2 — Contêineres

```mermaid
flowchart TB
    subgraph browser["Navegador do usuário"]
        spa["SPA Angular 20<br/><i>standalone components</i><br/>guards de rota + interceptor JWT<br/>servviço HTTP por domínio"]
    end

    subgraph servidor["Servidor de aplicação"]
        api["API REST — Spring Boot 4 / Java 21<br/><i>stateless, autenticação por JWT</i><br/>springdoc / Swagger UI"]
    end

    db[("PostgreSQL 14<br/>porta 5433")]
    disco[["Diretório uploads/"]]

    spa -->|"JSON sobre HTTP<br/>Authorization: Bearer &lt;JWT&gt;<br/>base http://localhost:8080"| api
    api -->|"Spring Data JPA / Hibernate"| db
    api -->|"salva imagem e devolve em base64"| disco
```

**Contrato entre contêineres:** API REST com recursos por domínio
(`/auth`, `/users`, `/services`, `/orders`, `/reviews`, `/favorites`,
`/category`, `/tags`, `/api/calendar`, `/financial-dashboard`). O front-end
depende apenas desse contrato HTTP; não há acoplamento de código entre SPA e API.

---

## 3. Nível 3 — Componentes da API (backend)

```mermaid
flowchart TB
    req["Requisição HTTP<br/>+ Bearer JWT"]

    subgraph security["Segurança"]
        filter["SecurityFilter<br/><i>OncePerRequestFilter</i><br/>valida o token e popula o SecurityContext"]
        jwt["JwtService<br/><i>assina e verifica HS256</i>"]
        cfg["SecurityConfig<br/><i>stateless, CORS, rotas públicas</i>"]
    end

    subgraph web["Camada web"]
        controllers["Controllers REST<br/><i>@RestController por domínio</i><br/>anotações de papel (@Client/@Provider/@Admin)"]
        handler["GlobalExceptionHandler<br/><i>@ControllerAdvice → StandardError</i>"]
    end

    subgraph app["Camada de aplicação"]
        services["Services<br/><i>@Service, @Transactional</i><br/>regras de negócio + validações"]
        mappers["Mappers / DTOs<br/><i>request ↔ entidade ↔ response</i>"]
        authsvc["AuthService<br/><i>usuário autenticado a partir do contexto</i>"]
    end

    subgraph data["Camada de dados"]
        repos["Repositories<br/><i>Spring Data JPA</i>"]
        entities["Entidades JPA<br/><i>User, ProviderProfile, Service, Category, Tag,<br/>Order, Payment, Review, Availability, Favorite, ...</i>"]
    end

    db[("PostgreSQL")]

    req --> filter
    filter --> jwt
    filter --> controllers
    cfg -.->|"configura"| filter
    controllers --> services
    controllers --> handler
    services --> authsvc
    services --> mappers
    services --> repos
    repos --> entities
    repos --> db
```

**Princípios aplicados (ADR-001):**
- Camadas com responsabilidade única e dependências em uma só direção
  (web → aplicação → dados).
- Baixo acoplamento: controllers não acessam repositórios; regras ficam nos
  services.
- Contrato explícito de erro (`GlobalExceptionHandler` → `StandardError`).

---

## 4. Fluxo de uma requisição autenticada

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuário
    participant SPA as SPA Angular
    participant INT as HTTP Interceptor
    participant F as SecurityFilter
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant DB as PostgreSQL

    U->>SPA: ação na interface
    SPA->>INT: requisição HTTP
    INT->>INT: anexa "Authorization: Bearer <JWT>"
    INT->>F: envia requisição

    alt token ausente ou inválido
        F-->>SPA: 401 / 403
    else token válido
        F->>F: extrai sub + role, popula SecurityContext
        F->>C: encaminha requisição
        C->>S: chama caso de uso
        S->>S: valida regras de negócio
        S->>R: consulta / persiste
        R->>DB: SQL
        DB-->>R: linhas
        R-->>S: entidades
        S-->>C: DTO de resposta
        C-->>SPA: 2xx + JSON
        SPA-->>U: atualiza a interface
    end
```

---

## 5. Observações de arquitetura

- **Autenticação** por JWT HS256 (claims `sub`, `role`, `name`, expiração 24h);
  API sem sessão (`SessionCreationPolicy.STATELESS`).
- **Autorização de papel**: as anotações `@Client` / `@Provider` / `@Admin`
  expressam a intenção de acesso por endpoint. Hoje a verificação por método
  (`@PreAuthorize`) não está ativa (falta `@EnableMethodSecurity`); regras
  sensíveis por papel são reforçadas na camada de serviço (ex.: favoritos
  restrito a `CLIENT`). Ponto registrado para correção.
- **Imagens** dos serviços são gravadas em disco (`uploads/`) e trafegam para o
  front-end como base64 dentro do DTO.
- **Documentação viva da API** via springdoc em `/swagger-ui.html`.
- **Esquema do banco** gerado pelo Hibernate (`ddl-auto=update`) a partir das
  entidades JPA.
