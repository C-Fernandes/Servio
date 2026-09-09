# Diagramas de Arquitetura e Modelo - Sistema de Favoritos

Este documento apresenta os diagramas visuais gerados com apoio de Inteligência Artificial para representar o modelo de dados e o fluxo de interação do **Sistema de Favoritos** (Requisito 1).

---

## 1. Diagrama de Entidade-Relacionamento (ERD)

```mermaid
erDiagram
    USER ||--o{ FAVORITE : "salva"
    SERVICE ||--o{ FAVORITE : "é favoritado em"
    USER ||--o{ ORDER : "solicita"
    SERVICE ||--o{ ORDER : "pertence a"
    PROVIDER_PROFILE ||--o{ SERVICE : "oferece"
    CATEGORY ||--o{ SERVICE : "categoriza"

    USER {
        bigint id PK
        varchar name
        varchar email
        varchar role
        boolean deleted
    }

    SERVICE {
        bigint id PK
        varchar title
        text description
        numeric price
        int duration_in_minutes
        boolean active
        boolean deleted
        bigint category_id FK
        bigint provider_profile_id FK
    }

    FAVORITE {
        bigint id PK
        bigint user_id FK "uk_user_service_favorite"
        bigint service_id FK "uk_user_service_favorite"
        timestamp created_at
    }
```

---

## 2. Diagrama de Sequência (Fluxo de Favoritar Serviço)

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente (Navegador)
    participant UI as Angular (ServiceCard)
    participant FavService as Angular (FavoriteService)
    participant Controller as FavoriteController (Spring Boot)
    participant Service as FavoriteService
    participant Repo as FavoriteRepository
    participant DB as PostgreSQL

    Cliente->>UI: Clica no botão de coração (favoritar)
    UI->>FavService: toggleFavorite(serviceId)
    FavService->>Controller: POST /favorites/{serviceId} (Bearer JWT)
    
    alt Usuário não é CLIENT
        Controller-->>FavService: 403 Forbidden
        FavService-->>UI: Erro de permissão
    else Usuário autenticado é CLIENT
        Controller->>Service: addFavorite(serviceId)
        Service->>DB: Verifica existência e status do Service
        alt Serviço inativo ou deletado
            Service-->>Controller: BusinessException (400 Bad Request)
            Controller-->>FavService: 400 Bad Request
        else Serviço ativo
            Service->>Repo: existsByUserIdAndServiceId(userId, serviceId)
            alt Já favoritado (Idempotência)
                Service-->>Controller: Retorna favorito existente (200 OK)
            else Novo favorito
                Service->>Repo: save(new Favorite(user, service))
                Repo->>DB: INSERT INTO favorites (...)
                Service-->>Controller: Retorna novo favorito (201 Created)
            end
            Controller-->>FavService: 201 Created (FavoriteResponseDTO)
            FavService-->>UI: Sucesso
            UI-->>Cliente: Ícone preenchido e feedback visual (Toast)
        end
    end
```

---

## 3. Diagrama de Sequência (Listagem "Meus Favoritos")

```mermaid
sequenceDiagram
    autonumber
    actor Cliente as Cliente
    participant Page as FavoritesPage (Angular)
    participant API as Backend (/favorites)
    participant DB as PostgreSQL

    Cliente->>Page: Acessa rota /favorites
    Page->>API: GET /favorites (Header Authorization)
    API->>DB: SELECT f FROM Favorite f WHERE f.user.id = :userId ORDER BY f.createdAt DESC
    DB-->>API: Lista de favoritos com dados de serviço
    API-->>Page: 200 OK [FavoriteResponseDTO, ...]
    alt Lista vazia
        Page-->>Cliente: Renderiza estado vazio amigável ("Nenhum favorito salvo")
    else Lista com itens
        Page-->>Cliente: Renderiza grid responsivo com cards de serviços
    end
```
