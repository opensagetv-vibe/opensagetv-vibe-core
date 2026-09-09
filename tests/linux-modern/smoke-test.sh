#!/usr/bin/env bash
set -euo pipefail
cd /work/sagetv/output/server
mkdir -p ../logs/smoke-home ../test-results/smoke
start_once() {
  HOME="$PWD/../logs/smoke-home" LD_LIBRARY_PATH="$PWD" \
    java -Djava.awt.headless=true -cp 'Sage.jar:JARs/*' sage.Sage 0 0 x 'sagetv Sage-smoke.properties' \
    > ../logs/sagetv-smoke.log 2>&1 &
  pid=$!
  sleep 12
  kill -0 "$pid"
  python3 - <<'PY'
import socket
import time

deadline = time.monotonic() + 10
last_error = None
while time.monotonic() < deadline:
    try:
        with socket.create_connection(("127.0.0.1", 42024), timeout=1):
            break
    except OSError as error:
        last_error = error
        time.sleep(0.25)
else:
    raise RuntimeError("SageTV TCP service did not start: %s" % last_error)

request = bytearray(32)
request[0:4] = b"STV\x01"
with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as udp:
    udp.settimeout(5)
    udp.sendto(request, ("127.0.0.1", 31100))
    response, source = udp.recvfrom(512)
assert source[0] == "127.0.0.1", source
assert len(response) == 15, len(response)
assert response[0:4] == b"STV\x02", response[0:4]
assert int.from_bytes(response[13:15], "big") == 31099
PY
  kill -TERM "$pid"
  for _ in $(seq 1 10); do kill -0 "$pid" 2>/dev/null || break; sleep 1; done
  kill -KILL "$pid" 2>/dev/null || true
  wait "$pid" 2>/dev/null || true
}
start_once
start_once
grep -q 'Main is starting' ../logs/sagetv-smoke.log
