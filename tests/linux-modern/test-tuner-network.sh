#!/usr/bin/env bash
set -euo pipefail
cd /work/sagetv
mkdir -p output/test-results
python3 tests/linux-modern/test-tuner-network.py
