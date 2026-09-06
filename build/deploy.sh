#!/usr/bin/env bash
set -euo pipefail

# Copyright 2017 The SageTV Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

cat >&2 <<'EOF'
ERROR: build/deploy.sh is disabled in OpenSageTV Vibe Core.

The inherited script could publish and tag the historical google/sagetv
project from a branch build. Vibe release candidates must instead pass the
Ubuntu 26 unified-container workflow documented in WORKFLOW.md. Publishing is
performed by the reviewed OpenSageTV Vibe release workflow, never by this
legacy entry point.
EOF
exit 2
