# Evidência de Bloqueio Real do Guardrail

Registro exigido pela seção IV da especificação da disciplina: o guardrail deve
ter **evidência de bloqueio real, não apenas descrito**.

* **Data:** 2026-09-09
* **Guardrail:** `.githooks/pre-commit` (ativo via `git config core.hooksPath .githooks`)
* **Ação do guardrail:** executa `mvn test` no backend antes de cada commit e
  aborta (`exit 1`) se qualquer teste falhar.

---

## Procedimento

1. Introduzida uma falha proposital em um teste automatizado — a asserção do
   caso "serviço inexistente retorna 404" foi alterada para esperar `999`:

   ```java
   // FavoriteServiceTest.addFavorite_serviceNotFound_throws404
   assertEquals(999, ex.getStatusCode().value());   // valor correto: 404
   ```

2. Tentativa de registrar a alteração (`git add` + `git commit`). O hook
   `pre-commit` foi disparado automaticamente.

---

## Saída do guardrail (trecho decisivo)

```text
==================================================================
🛡️  [GUARDRAIL DE IA] Executando verificações de integridade...
==================================================================
🔍 [GUARDRAIL] Executando testes automatizados do Backend...

[ERROR] Tests run: 8, Failures: 1, Errors: 0, Skipped: 0 <<< FAILURE! -- in FavoriteService - SPEC-001 (RF-01 Sistema de Favoritos)
[ERROR] com.ufrn.ppgti.servio.service.FavoriteServiceTest.addFavorite_serviceNotFound_throws404 <<< FAILURE!
org.opentest4j.AssertionFailedError: expected: <999> but was: <404>
	at com.ufrn.ppgti.servio.service.FavoriteServiceTest.addFavorite_serviceNotFound_throws404(FavoriteServiceTest.java:171)

[ERROR] Tests run: 9, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:3.5.5:test (default-test) on project servio: There are test failures.

❌ ==============================================================
🚨 [GUARDRAIL BLOQUEIO ATIVO] COMMIT REJEITADO!
   Os testes automatizados falharam ou o código contém erros.
   O agente de IA ou desenvolvedor deve corrigir as falhas antes
   de integrar alterações na ramificação.
============================================================== ❌
```

Código de saída do hook: **`1`** → o `git commit` é abortado.

---

## Confirmação de que nada foi integrado

```text
$ git log --oneline -1
a9baab4 feat(favoritos): frontend do RF-01
```

O HEAD permaneceu no último commit legítimo. Nenhum commit com código quebrado
entrou na ramificação.

---

## Reversão

A asserção foi restaurada para o valor correto (`404`) e a suíte voltou a passar:

```text
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## Como reproduzir

```bash
# 1. quebrar a asserção citada acima em FavoriteServiceTest.java
git add -A
git commit -m "teste: reproduzir bloqueio do guardrail"   # será rejeitado
git log --oneline -1                                       # HEAD inalterado
# 2. desfazer a alteração no teste
```
