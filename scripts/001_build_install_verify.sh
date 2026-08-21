#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
MAVEN="${MAVEN:-mvn}"
FIJI_DIR="${1:-${FIJI_DIR:-}}"

if [[ -z "$FIJI_DIR" ]]; then
  printf 'Usage: FIJI_DIR=/absolute/path/to/Fiji %s\n' "$0" >&2
  exit 2
fi

FIJI_BIN=""
for candidate in \
  "$FIJI_DIR/fiji-linux-x64" \
  "$FIJI_DIR/fiji" \
  "$FIJI_DIR/Contents/MacOS/fiji-macos-arm64" \
  "$FIJI_DIR/Contents/MacOS/fiji-macos-x64" \
  "$FIJI_DIR/fiji-windows-x64.exe"
do
  if [[ -x "$candidate" ]]; then
    FIJI_BIN="$candidate"
    break
  fi
done

if [[ -z "$FIJI_BIN" ]]; then
  printf 'Could not find a Fiji launcher under %s\n' "$FIJI_DIR" >&2
  exit 2
fi

cd "$PROJECT_DIR"
"$MAVEN" -q clean test package

JARS=("$PROJECT_DIR"/target/opengridseg-*.jar)
if [[ ${#JARS[@]} -ne 1 || ! -f "${JARS[0]}" ]]; then
  printf 'Expected one OpenGridSeg JAR under target/.\n' >&2
  exit 1
fi
JAR="${JARS[0]}"
INSTALLED="$FIJI_DIR/plugins/OpenGridSeg.jar"

install -m 0644 "$JAR" "$INSTALLED"
OUTPUT="$($FIJI_BIN --headless --console --class-path "$PROJECT_DIR/target/test-classes" --main-class org.opengridseg.FijiPluginVerifier)"
printf '%s\n' "$OUTPUT"
case "$OUTPUT" in
  *"FIJI_PLUGIN_FOUND"*"menu=[Plugins, OpenGridSeg]"*) ;;
  *) printf 'Fiji did not discover OpenGridSeg.\n' >&2; exit 1;;
esac

mkdir -p "$PROJECT_DIR/dist"
install -m 0644 "$JAR" "$PROJECT_DIR/dist/OpenGridSeg.jar"
sha256sum "$PROJECT_DIR/dist/OpenGridSeg.jar" > "$PROJECT_DIR/dist/OpenGridSeg.jar.sha256"
printf 'Installed: %s\n' "$INSTALLED"
printf 'Package: %s\n' "$PROJECT_DIR/dist/OpenGridSeg.jar"
