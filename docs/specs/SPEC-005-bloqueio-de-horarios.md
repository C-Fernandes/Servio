# [SPEC-005] Bloqueio de Horários Específicos na Disponibilidade do Prestador

* **Autoras**: Bianca Antonelly, Maria Clara Fernandes
* **Data**: 2026-09-12
* **Status**: Aprovado
* **Requisito do Projeto**: RF-17 (item 10 da proposta de checkpoint — "Melhorias na disponibilidade do prestador, permitindo bloqueio de horários específicos")

---

## 1. Prompt Inicial de Comportamento (Behavior)

```text
Atue como um Engenheiro de Software Full Stack sênior especializado em Spring Boot e Angular.
Sua missão é especificar e implementar o Bloqueio de Horários Específicos no marketplace Servio.
Restrições de Comportamento:
- Um bloqueio é um registro de disponibilidade com specificDate preenchida e isAvailable = false.
- Um bloqueio é independente das regras semanais e dos horários extras (não os apaga, apenas some do que fica reservável).
- Um bloqueio some das opções de agendamento (generateAvailableSlots) para a data/horário bloqueado, mesmo que exista regra semanal cobrindo aquele horário.
- Apenas o prestador dono do bloqueio pode removê-lo.
- Cubra explicitamente os casos de borda com testes automatizados.
```

## 2. Contexto e Motivação

A entidade `Availability` já tinha o campo `isAvailable`, mas nada no sistema
criava um registro com `isAvailable = false` — não havia como o prestador
bloquear um horário específico (ex.: uma consulta médica, um feriado) sem
apagar a regra semanal inteira. Esta SPEC adiciona essa exceção pontual.

## 3. Requisitos Funcionais

* **RF-17.1**: prestador cria um bloqueio (data + intervalo de horário).
* **RF-17.2**: prestador lista seus bloqueios.
* **RF-17.3**: prestador remove um bloqueio, voltando o horário a ficar reservável.
* **RF-17.4**: um horário bloqueado não aparece em `generateAvailableSlots`, mesmo coberto por regra semanal.

## 4. Critérios de Aceite (Gherkin)

### Cenário 1: bloqueio remove o horário da disponibilidade
```gherkin
Dado que o prestador "João" tem disponibilidade semanal das 08:00 às 12:00 toda terça
E cria um bloqueio para a próxima terça das 09:00 às 10:00
Quando um cliente consulta os horários disponíveis do serviço de João
Então o horário 09:00 daquela terça não deve aparecer na lista
E os demais horários daquele dia devem continuar disponíveis
```

### Cenário 2: prestador lista e remove um bloqueio
```gherkin
Dado que o prestador "João" tem um bloqueio cadastrado
Quando ele consulta "GET /api/calendar/blocks"
Então o bloqueio deve aparecer na lista
Quando ele envia "DELETE /api/calendar/blocks/{id}"
Então o bloqueio deve deixar de existir
E o horário volta a aparecer na disponibilidade
```

### Caso de Borda 1 (Edge Case — Intervalo inválido):
```gherkin
Dado que o prestador tenta criar um bloqueio com horário de fim antes do início
Quando ele envia "POST /api/calendar/blocks"
Então o sistema deve recusar com 400 (Bad Request)
```

### Caso de Borda 2 (Edge Case — Remover bloqueio de outro prestador):
```gherkin
Dado um bloqueio pertencente ao prestador "João"
Quando o prestador "Ana" tenta remover esse bloqueio
Então o sistema deve recusar com 403 (Forbidden)
```

## 5. Contrato da API REST

| Método | Endpoint | Perfil | Resposta | Erros |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/calendar/blocks` | PROVIDER | `201 Created` (`AvailabilityDTO`) | 400 (intervalo inválido) |
| `GET` | `/api/calendar/blocks` | PROVIDER | `200 OK` (`List<AvailabilityDTO>`) | — |
| `DELETE` | `/api/calendar/blocks/{id}` | PROVIDER (dono) | `204 No Content` | 404, 403 |

## 6. Plano de Tarefas

- [x] **T1**: `AvailabilityService.createBlock/listBlocks/removeBlock`.
- [x] **T2**: exclusão de horários bloqueados em `generateAvailableSlots`/`generateSlotsForDay`.
- [x] **T3**: endpoints em `AvailabilityController`.
- [x] **T4**: `AvailabilityServiceTest` — cenário + casos de borda.
- [x] **T5**: frontend — seção "Bloqueios de horário" na página Agenda (criar/listar/remover).
- [ ] **T6**: homologação (demo).
