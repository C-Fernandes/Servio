# Rascunho — Post no LinkedIn (Método STAR)

Publicar como Bianca ou Maria Clara (ou os dois perfis, cada um do seu jeito).
Anexar print/GIF do sino de notificações, da linha do tempo da jornada, ou do
guardrail bloqueando um commit — conteúdo visual real aumenta o alcance.

---

**Situação**
Marketplaces de serviços locais costumam parar na contratação: o cliente
contrata, mas não sabe o que está acontecendo com o pedido até o prestador
avisar manualmente. Foi o problema que escolhemos atacar na disciplina
**Desenvolvimento de Software com IA** (PPGTI/UFRN), evoluindo o **Servio**, um
marketplace que já conectava clientes e prestadores.

**Tarefa**
Em dupla, tínhamos ~1 semana para implementar novos requisitos praticando
**Spec-Driven Development** e um **harness de controle de agentes de IA** — não
bastava "funcionar", tinha que existir processo auditável por trás.

**Ação**
Usamos o **Claude Code** com autonomia supervisionada (Plan → Review → Execute):
o agente propunha o plano e o diff, nós revisávamos e commitávamos — nunca o
contrário. Cada funcionalidade nova nasceu de uma especificação (comportamento
+ critérios de aceite em Gherkin, incluindo casos de borda) antes de qualquer
linha de código. Um **guardrail** (Git hook) roda a suíte de testes automatizados
antes de cada commit e barra o commit se algo quebrar — testamos isso de
propósito quebrando um teste e vendo o commit ser rejeitado de verdade.
Implementamos assim: sistema de favoritos, jornada do pedido com linha do
tempo (Solicitado → Aceito → Em andamento → Concluído), notificações in-app
de mudança de status, relatório de desempenho do prestador, além de busca
avançada e chat entre cliente e prestador.

**Resultado**
Chegamos a **16 requisitos funcionais**, 4 deles com ciclo completo de SDD
(spec, ADR, diagrama e testes automatizados — 33 no total), guardrail com
evidência real de bloqueio, e um repositório com o processo inteiro
documentado (specs, ADRs, diagramas Mermaid, transcript de sessão do agente).
O maior aprendizado: SDD dá trabalho no começo, mas paga o preço rápido — as
duas funcionalidades que fizemos fora do fluxo estrito de spec foram
justamente onde sentimos mais falta de rastreabilidade depois. Da próxima vez,
nenhuma linha de código sem spec antes.

🔗 Repositório: https://github.com/C-Fernandes/Servio
🎥 Demo: [link ou GIF]

#DesenvolvimentoDeSoftware #IA #ClaudeCode #SpecDrivenDevelopment #UFRN #PPGTI
