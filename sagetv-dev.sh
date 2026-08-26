#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UNIFIED_WRAPPER="${OPENSAGETV_BUILD_WRAPPER:-$ROOT/../opensagetv-build-env/opensagetv-dev.sh}"
CONTAINER="${OPENSAGETV_DEV_CONTAINER:-opensagetv-dev}"

if [[ ! -x "$UNIFIED_WRAPPER" ]]; then
  echo "Unified build wrapper not found: $UNIFIED_WRAPPER" >&2
  echo "Place opensagetv-core beside opensagetv-build-env or set OPENSAGETV_BUILD_WRAPPER." >&2
  exit 2
fi

container_start() { "$UNIFIED_WRAPPER" start >/dev/null; }
run() {
  container_start
  docker exec -e JDK_HOME=/usr/lib/jvm/java-11-openjdk-amd64 \
    -e JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 "$CONTAINER" "$@"
}

case "${1:-help}" in
  image) exec "$UNIFIED_WRAPPER" image ;;
  build) run bash tests/linux-modern/build.sh ;;
  test) run bash tests/linux-modern/test-all.sh ;;
  native-test) run bash tests/linux-modern/test-native.sh ;;
  image-test) run bash tests/linux-modern/test-image-loader.sh ;;
  package) run bash tests/linux-modern/package.sh ;;
  report) run bash tests/linux-modern/build-report.sh ;;
  shell) exec "$UNIFIED_WRAPPER" shell ;;
  clean) run bash tests/linux-modern/clean.sh ;;
  rebuild) run bash tests/linux-modern/clean.sh; run bash tests/linux-modern/build.sh ;;
  all) exec "$UNIFIED_WRAPPER" core ;;
  *)
    echo "Usage: $0 {image|build|test|native-test|image-test|package|report|shell|clean|rebuild|all}" >&2
    exit 2
    ;;
esac
