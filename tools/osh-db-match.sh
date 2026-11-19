#!/usr/bin/env bash
# osh-db-match.sh - find the osh-core / osh-addons versions that match an OSH H2 database file.
#
#   ./osh-db-match.sh <database-file>           show what matches
#   ./osh-db-match.sh <database-file> --apply   switch osh-core and osh-addons to the match
#
# Run it from inside your osh-node-dev-template. The database file is only read, never changed.

set -uo pipefail

STORAGE_PATHS=(sensorhub-datastore-h2)
STORAGE_WORDS='h2|mvstore|kryo|serializ|datastore|database|storage|on-disk|persist|format|compat|migrat'

die()  { echo "error: $*" >&2; exit 1; }
head1(){ printf '\n== %s ==\n' "$1"; }
field(){ printf '%s\n' "$1" | tr ',' '\n' | sed -n "s/^$2://p" | head -n1; }
fmt()  { date -u -d "@$1" '+%Y-%m-%d %H:%M:%S UTC'; }

DB=""; APPLY=0
for a in "$@"; do
  case "$a" in
    --apply) APPLY=1 ;;
    -*)      die "unknown option: $a (only --apply is supported)" ;;
    *)       DB="$a" ;;
  esac
done
[ -n "$DB" ] || { sed -n '2,7p' "$0" | sed 's/^# \{0,1\}//'; exit 1; }
[ -r "$DB" ] || die "cannot read $DB"

# Template = first folder at or above here whose .gitmodules mentions osh-core
T=$(pwd)
while [ "$T" != "/" ] && ! { [ -f "$T/.gitmodules" ] && grep -q osh-core "$T/.gitmodules"; }; do T=$(dirname "$T"); done
[ "$T" != "/" ] || die "run this from inside your osh-node-dev-template"

# ------------------------------------------------------------ database file
head1 "Database file"
HEADER=$(head -c 4096 "$DB" | tr '\0' '\n' | grep -a -m1 '^H:2,' || true)
[ -n "$HEADER" ] || die "$DB is not an H2 MVStore file, so it can't be dated"

CREATED_MS=$((16#$(field "$HEADER" created)))
FORMAT=$(field "$HEADER" format)
TARGET_S=$((CREATED_MS / 1000))
echo "Created:      $(fmt "$TARGET_S")"

BS=$((16#$(field "$HEADER" blockSize))); BLK=$((16#$(field "$HEADER" block)))
CHUNK=$(dd if="$DB" bs="$BS" skip="$BLK" count=1 2>/dev/null | tr '\0' '\n' | grep -a -m1 '^chunk:' || true)
T_HEX=$(field "$CHUNK" time)
if [ -n "$T_HEX" ]; then
  TARGET_S=$(((CREATED_MS + 16#$T_HEX) / 1000))
  echo "Last written: $(fmt "$TARGET_S")"
fi
case "$FORMAT" in 1) H2_NEED="1.4"; H2_LABEL="1.4.x" ;; *) H2_NEED="2."; H2_LABEL="2.x" ;; esac
echo "H2 format:    $FORMAT (needs H2 $H2_LABEL)"
TARGET_ISO=$(date -u -d "@$TARGET_S" '+%Y-%m-%d %H:%M:%S +0000')

# ------------------------------------------------------------ helpers
h2_at() {
  git -C "$1" grep -h -o -E 'com\.h2database:h2(-mvstore)?:[0-9][0-9.]*' "$2" \
    -- '*.gradle' '*.gradle.kts' '*.toml' 2>/dev/null | sed 's/.*://' | sort -u | paste -sd' ' -
}
desc() { git -C "$1" show -s --format='%h  %cd  %s' --date=short "$2"; }

# ------------------------------------------------------------ repos
for NAME in osh-core osh-addons; do
  head1 "$NAME"
  P=$(git -C "$T" config -f .gitmodules --get-regexp '\.path$' | awk '{print $2}' | grep -E "(^|/)$NAME\$" | head -n1)
  R="$T/$P"
  [ -n "$P" ] && [ -e "$R/.git" ] || { echo "Not checked out (run: git submodule update --init --recursive)"; continue; }

  git -C "$R" fetch --all --quiet 2>/dev/null || echo "(fetch failed, using local copy)"
  BR=$(git -C "$R" symbolic-ref -q --short refs/remotes/origin/HEAD || echo origin/master)
  MATCH=$(git -C "$R" rev-list -1 --before="$TARGET_ISO" "$BR")
  [ -n "$MATCH" ] || { echo "No commit on $BR before the file's date"; continue; }

  echo "Current: $(desc "$R" HEAD)"
  echo "Match:   $(desc "$R" "$MATCH")"
  H2=$(h2_at "$R" "$MATCH")
  [ -n "$H2" ] && case " $H2" in *" $H2_NEED"*) echo "H2:      $H2 (OK)";; *) echo "H2:      $H2 (WRONG for this file)";; esac

  if [ "$(git -C "$R" rev-parse HEAD)" = "$(git -C "$R" rev-parse "$MATCH")" ]; then
    echo "Result:  already on the match"
    continue
  fi

  if git -C "$R" merge-base --is-ancestor "$MATCH" HEAD; then RANGE="$MATCH..HEAD"
  elif git -C "$R" merge-base --is-ancestor HEAD "$MATCH"; then RANGE="HEAD..$MATCH"
  else RANGE="$MATCH...HEAD"; fi
  FLAGS=$( { git -C "$R" log --oneline "$RANGE" -- "${STORAGE_PATHS[@]}"
             git -C "$R" log --oneline -i -E --grep="$STORAGE_WORDS" "$RANGE"; } | sort -u | head -n 15)

  if [ "$APPLY" = 1 ]; then
    if ! git -C "$R" diff --quiet || ! git -C "$R" diff --cached --quiet; then
      echo "Result:  NOT switched - $NAME has uncommitted changes"
    elif git -C "$R" -c advice.detachedHead=false checkout --quiet "$MATCH"; then
      git -C "$R" submodule update --init --recursive --quiet || true
      echo "Result:  switched to the match"
    else
      echo "Result:  checkout failed"
    fi
  elif [ -z "$FLAGS" ]; then
    echo "Result:  different commit, but no storage changes in between - should work as-is"
  else
    echo "Result:  different commit, with possible storage changes in between:"
    echo "$FLAGS" | sed 's/^/           /'
    echo "         Run again with --apply to switch to the match."
  fi
done

[ "$APPLY" = 1 ] && printf '\nNow rebuild:  ./gradlew clean build\n'
exit 0
