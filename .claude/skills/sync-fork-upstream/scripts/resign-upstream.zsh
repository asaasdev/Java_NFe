#!/bin/zsh
# Recria, assinados, os commits do upstream que ainda não estão na master do fork.
# Mantém árvore, autor, data de autoria, mensagem e topologia (inclusive merges).
#
# Como identifica o que já foi sincronizado: os commits da master do fork são cópias
# assinadas (SHAs diferentes dos do upstream), então a comparação é feita por
# (árvore, data de autoria, e-mail do autor). Commits do upstream com chave já
# presente no fork são reaproveitados; só os restantes são recriados.
#
# Se o fork tiver commits próprios (ex.: esta skill), o resultado é um merge
# commit assinado de <base> com o upstream recriado.
#
# Uso: resign-upstream.zsh <branch-destino> [base=origin/master] [upstream=upstream/master]
# Rodar com zsh: o bash do macOS (3.2) não suporta arrays associativos.
set -e
BRANCH=${1:?"informe o nome da branch de destino"}
BASE=${2:-origin/master}
UP=${3:-upstream/master}

[[ "$(git config --get commit.gpgsign)" == "true" || -n "$(git config --get user.signingkey)" ]] || {
  echo "ERRO: assinatura de commits não configurada (user.signingkey)." >&2
  exit 1
}

typeset -A FORK MAP
while read -r sha tree adate aemail; do
  key="$tree|$adate|$aemail"
  FORK[$key]=$sha
done < <(git log --format='%H %T %aI %ae' "$BASE")

NEW=0
for c in $(git rev-list --reverse --topo-order "$UP"); do
  k="$(git log -1 --format='%T|%aI|%ae' $c)"
  if [[ -n "${FORK[$k]}" ]]; then
    MAP[$c]=${FORK[$k]}
    continue
  fi
  args=()
  for p in $(git log -1 --format=%P $c); do args+=(-p "${MAP[$p]:-$p}"); done
  export GIT_AUTHOR_NAME="$(git log -1 --format=%an $c)" \
         GIT_AUTHOR_EMAIL="$(git log -1 --format=%ae $c)" \
         GIT_AUTHOR_DATE="$(git log -1 --format=%aI $c)"
  MAP[$c]=$(git log -1 --format=%B $c | git commit-tree -S "$c^{tree}" "${args[@]}")
  NEW=$((NEW + 1))
done

if [[ $NEW -eq 0 ]]; then
  echo "Nada a sincronizar: $BASE já contém todos os commits de $UP."
  exit 0
fi

TIP="${MAP[$(git rev-parse $UP)]}"
if git merge-base --is-ancestor "$BASE" "$TIP"; then
  RESULT=$TIP
else
  echo "O fork tem commits próprios: criando merge commit assinado."
  TREE=$(git merge-tree --write-tree "$BASE" "$TIP") || {
    echo "ERRO: conflito ao mesclar $BASE com o upstream; resolva manualmente." >&2
    exit 1
  }
  RESULT=$(git commit-tree -S "$TREE" -p "$BASE" -p "$TIP" -m "Merge de $UP (sync fork)")
fi
git update-ref "refs/heads/$BRANCH" "$RESULT"

echo "Branch $BRANCH criada em $RESULT"
echo "Commits novos vs $BASE: $(git rev-list --count "$BASE..$BRANCH")"
echo "Diferença de conteúdo vs $UP (só deve listar arquivos próprios do fork, ex.: .claude/):"
git diff --stat "$UP" "$BRANCH"
echo "Assinaturas (G = válida):"
git log "$BASE..$BRANCH" --format='%G?' | sort | uniq -c
