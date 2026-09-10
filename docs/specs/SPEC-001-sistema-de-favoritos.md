# [SPEC-001] Sistema de Favoritos para Clientes

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-08
* **Status**: Aprovado
* **Requisito do Projeto**: Requisito Funcional 1 (RF-01)
* **ADR Relacionada**: [ADR-002: Modelagem e Idempotência do Sistema de Favoritos](../adr/ADR-002-modelagem-sistema-favoritos.md)

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é especificar e implementar o Sistema de Favoritos no marketplace Servio.
Restrições de Comportamento:
- Apenas usuários com a role CLIENT podem favoritar serviços.
- Não duplique registros de favoritos para o mesmo par (cliente, serviço).
- Serviços inativos (active=false) ou excluídos (deleted=true) não podem ser favoritados.
- Retorne respostas HTTP semânticas (201 Created para adição, 204 No Content para remoção, 404 para serviço não encontrado, 400 para regras violadas).
- Cubra explicitamente casos de borda com testes automatizados.
```

---

## 2. Contexto e Motivação
No marketplace **Servio**, clientes frequentemente navegam por diversos prestadores antes de fechar um pedido. Sem uma forma de salvar prestadores e serviços de interesse, o cliente precisa refazer buscas manuais a cada visita. O **Sistema de Favoritos** permite que o cliente marque serviços com um clique, acesse rapidamente sua lista personalizada ("Meus Favoritos") e decida o momento ideal de contratação.

---

## 3. Requisitos Funcionais
* **RF-01.1 (Adicionar Favorito)**: O cliente autenticado deve poder marcar um serviço ativo como favorito.
* **RF-01.2 (Remover Favorito)**: O cliente autenticado deve poder desmarcar um serviço de seus favoritos.
* **RF-01.3 (Listar Favoritos)**: O cliente autenticado deve visualizar a lista completa de seus serviços favoritados com os mesmos detalhes visuais dos cards do marketplace (foto, título, preço, prestador, avaliação).
* **RF-01.4 (Verificar Status)**: O sistema deve permitir consultar se um serviço específico já está favoritado pelo cliente autenticado (para renderizar o coração preenchido ou vazado nos cards).

---

## 4. Critérios de Aceite (Gherkin - Given / When / Then)

### Cenário 1: Cliente favorita um serviço ativo com sucesso
```gherkin
Dado que o usuário "Maria" está autenticado com a role "CLIENT"
E existe um serviço "Limpeza Residencial" com status ativo e não deletado
Quando a cliente envia uma requisição "POST /favorites/{serviceId}" para o serviço
Então o sistema deve retornar status HTTP 201 (Created)
E o serviço deve ser adicionado à lista de favoritos de "Maria"
E o registro deve conter a data/hora em que foi favoritado
```

### Cenário 2: Cliente remove um serviço dos seus favoritos
```gherkin
Dado que o usuário "Maria" possui o serviço "Limpeza Residencial" em sua lista de favoritos
Quando a cliente envia uma requisição "DELETE /favorites/{serviceId}"
Então o sistema deve retornar status HTTP 204 (No Content)
E o serviço não deve mais constar na lista de favoritos de "Maria"
```

### Cenário 3: Cliente visualiza sua lista de favoritos
```gherkin
Dado que o usuário "Maria" possui 3 serviços favoritados
Quando a cliente envia uma requisição "GET /favorites"
Então o sistema deve retornar status HTTP 200 (OK)
E o corpo da resposta deve conter uma lista com os 3 serviços com dados completos (título, preço, categoria, prestador e imagem)
```

### Caso de Borda 1 (Edge Case - Idempotência / Tentativa Duplicada):
```gherkin
Dado que o serviço "Pintura de Parede" já está favoritado por "Maria"
Quando "Maria" envia novamente uma requisição "POST /favorites/{serviceId}" para o mesmo serviço
Então o sistema não deve gerar registros duplicados no banco de dados
E deve responder com status HTTP 200 (OK) ou 201 (Created) mantendo o registro existente intacto
```

### Caso de Borda 2 (Edge Case - Serviço Inativo ou Deletado):
```gherkin
Dado que o serviço "Manutenção Elétrica" está desativado (active = false) ou marcado como deletado (deleted = true)
Quando o cliente tenta enviar "POST /favorites/{serviceId}"
Então o sistema deve recusar a operação retornando status HTTP 400 (Bad Request) ou 404 (Not Found)
E informar a mensagem de erro "Serviço não está disponível para ser favoritado"
```

### Caso de Borda 3 (Edge Case - Perfil Incompatível):
```gherkin
Dado que um usuário autenticado possui a role "PROVIDER" (sem perfil de cliente)
Quando o usuário tenta acessar qualquer endpoint de favoritos
Então o sistema deve barrar a requisição com status HTTP 403 (Forbidden)
```

---

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta Sucesso | Erros Previstos |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/favorites/{serviceId}` | CLIENT | `201 Created` (FavoriteResponseDTO) | 400 (Inativo), 404 (Não existe), 403 (Não é client) |
| `DELETE` | `/favorites/{serviceId}` | CLIENT | `204 No Content` | 404 (Não encontrado), 403 |
| `GET` | `/favorites` | CLIENT | `200 OK` (List<FavoriteResponseDTO>) | 403 |
| `GET` | `/favorites/check/{serviceId}` | CLIENT | `200 OK` (boolean: true/false) | 403 |

---

## 6. Plano de Tarefas (Tasklist)
- [x] **T1 (Modelo de Dados)**: Criar entidade `Favorite` com constraints de unicidade e relacionamento JPA.
- [x] **T2 (Repository & DTOs)**: Criar `FavoriteRepository` e `FavoriteResponseDTO`.
- [x] **T3 (Camada de Serviço)**: Implementar `FavoriteService` cobrindo cenários e casos de borda.
- [x] **T4 (Camada de Controller)**: Criar `FavoriteController` anotado com `@Client`.
- [x] **T5 (Testes Automatizados)**: Implementar `FavoriteServiceTest` validando regras e casos de borda.
- [x] **T6 (Serviço Frontend)**: Criar `FavoriteService` no Angular consumindo a API.
- [x] **T7 (Componente Service Card)**: Adicionar botão de favoritar com estado dinâmico nos cards.
- [x] **T8 (Página Meus Favoritos)**: Desenvolver página `/favorites` com grid responsivo e estado vazio.
- [x] **T9 (Navegação)**: Incluir atalho na Sidebar condicional ao perfil `CLIENT`.
- [ ] **T10 (Homologação)**: Teste funcional e validação de todos os critérios de aceite.
