---
name: sync-fork-upstream
description: Atualiza a master deste fork (asaasdev/Java_NFe) com as novidades do repositório oficial Samuel-Oliveira/Java_NFe, recriando os commits assinados e abrindo o PR. Use quando o usuário pedir para atualizar/sincronizar a lib, sync com o upstream ou "sync fork", já que o botão Sync fork do GitHub não está atualizando a master.
---

# Sync do fork com o repositório oficial

O botão **Sync fork** do GitHub não está atualizando a `master` deste fork, e o fork exige commits assinados. Esta skill sincroniza via PR, recriando os commits do upstream com a assinatura de quem executa. O conteúdo final fica idêntico ao do upstream; autor, data de autoria, mensagem e merges são preservados. Só mudam o committer e a assinatura (e, portanto, os SHAs).

## Pré-requisitos

- Assinatura de commits configurada (`user.signingkey`, `commit.gpgsign true`) e a chave cadastrada no GitHub como **Signing Key**, com e-mail verificado. Sem isso o PR não mostra "Verified".
- `gh` autenticado e acesso SSH ao GitHub (veja o passo 4).

## Passos

1. **Preparar remotes e verificar a divergência**
   ```
   git remote get-url upstream 2>/dev/null || git remote add upstream https://github.com/Samuel-Oliveira/Java_NFe.git
   git fetch upstream origin
   git rev-list --left-right --count origin/master...upstream/master
   ```
   O resultado é `<à frente> <atrás>`. Se o fork estiver à frente (primeiro número > 0), **pare e avise o usuário**: o fork tem commits próprios e o script não cobre esse caso. Se o segundo número for 0, não há nada para sincronizar.

2. **Recriar os commits assinados** numa branch nova, com nome datado:
   ```
   zsh .claude/skills/sync-fork-upstream/scripts/resign-upstream.zsh sync-assinado-$(date +%Y%m%d)
   ```
   Use `zsh`, não `bash`. Confira na saída: a diferença de conteúdo vs upstream deve ser vazia e todas as linhas de assinatura devem ser `G`.

   **Não use `git rebase`** para isso: ele não recria os merge commits que o upstream resolveu manualmente e dá conflito.

3. **Conferir** que a branch tem o mesmo conteúdo do upstream (tem que sair vazio):
   ```
   git diff --stat upstream/master sync-assinado-$(date +%Y%m%d)
   ```

4. **Publicar a branch.** Tente HTTPS e, se falhar com `without workflow scope` (o upstream altera `.github/workflows`), use SSH:
   ```
   git push -u git@github.com:asaasdev/Java_NFe.git sync-assinado-$(date +%Y%m%d)
   ```
   Alternativa: `gh auth refresh -s workflow`, que o usuário precisa rodar.

5. **Abrir o PR** para `master` com `gh pr create -R asaasdev/Java_NFe --base master`, título `Sync fork Java_NFe`. **Não use o template Asaas** nesse PR: escreva uma descrição curta dizendo que o PR foi aberto manualmente porque o Sync fork do GitHub não atualiza a `master`, que não há alterações de código próprias do fork, e que o merge deve ser feito com "Create a merge commit".

6. **Verificar as assinaturas no GitHub:**
   ```
   gh api repos/asaasdev/Java_NFe/pulls/<N>/commits --paginate --jq '.[] | [.commit.verification.verified, .commit.verification.reason] | @tsv' | sort | uniq -c
   ```
   Todos devem ser `true valid`. Se não forem, a chave de assinatura provavelmente não está cadastrada como Signing Key.

7. **Merge:** deve ser feito pelo usuário na interface, com **"Create a merge commit"**. Squash e rebase recriam os commits e perdem a assinatura. Não faça o merge sem o usuário pedir.

## Observações

- Os SHAs da `master` do fork não batem com os do upstream. O Sync fork do GitHub pode continuar reclamando de divergência; por isso este fluxo existe.
- Após cada sync, a próxima execução só recria os commits novos, pois a base é a `master` do fork.
- Se o repositório local estiver sujo, faça o trabalho num clone limpo.
