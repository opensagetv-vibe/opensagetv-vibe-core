# Common project workflow

## Task fix and server-boundary policy

Apply this policy to every task workflow, including dependency fixes across
Vibe repositories. Fix and test necessary plugins and update the test-server
plugin without asking again solely for repository-boundary approval.

Prefer the Android client, then a stock-compatible plugin. Change non-stock
`.232` Core only for a proven production defect that neither can correct;
document the API gap and alternatives, keep optional negotiation and safe
stock/older-client fallback, and run affected compatibility tests. Never patch
Core merely to simplify testing.

Stock `.175` installation changes are limited to plugin installation/update.
Do not modify its stock Sage.jar, stock FFmpeg, Core binaries, or server
installation/configuration files. Preserve user settings, recordings and
unrelated clients; reversible supported SageTV playback APIs remain allowed.

Non-stock `.232` restarts are authorized for task updates without asking
again; coordinate them with active test guards and preserve data/settings.
  Always ask the user before restarting stock `.175`, even when it appears idle,
  unless an explicit user-granted bounded restart window is active. Record
  its UTC expiry in the task/handoff, and check expiry and revocation before
  every restart. After expiry or revocation, ask again; stock files stay protected.

Update owning TASKS.md, linked dependencies and the workspace suggested order
as work changes; move completed checkoffs into the checklist change ledger.
Test only affected gates, preserve unrelated completed matrices, and do not
stop independent authorized work for a status question or a dependency-only
permission request. Unrelated work, publication, destructive actions and
interruption of recordings/other users still require their own authority.

Use `dev.cmd` on Windows or `./dev.sh` on Linux/WSL with
`test`, `validate`, `build`, `install`, or `all`. Commands resolve this checkout
from the launcher location and reuse the sibling unified image/container.
Core `install` is intentionally `SKIPPED`; the tested server archive is consumed
by `opensagetv-vibe-container`.

Put update ZIPs in `artifacts/downloads` and run `update.cmd` or `./update.sh`.
The runner verifies the ZIP and full manifest before extraction, then resumes
test/validate/build/install. Create a changed-files package with
`create_ai_handoff_zip.cmd`. See the sibling build environment's
`WORKFLOW.md` for the shared format and safety rules.

## Fork and publication workflow

The canonical repository name is `opensagetv-vibe-core`. Its `origin` is the
OpenSageTV Vibe fork and its read-only `upstream` is the canonical SageTV Core
repository, `google/sagetv`. `OpenSageTV/sagetv` is an intermediate fork and is
not the pull-request target.
Never configure the inherited `build/deploy.sh` as branch CI: it belongs to the
historical upstream release process and is not the Vibe publisher.

Before pushing or tagging a release candidate:

1. Fetch `upstream` and review the divergence without rewriting published
   history.
2. Run `./sagetv-dev.sh all` using the sibling unified build environment.
3. Confirm every required line in `output/BUILD_REPORT.md` is `PASS` and review
   all `SKIPPED` lines explicitly.
4. Run `git diff --check` and verify generated output, credentials, appdata,
   recordings, `Sage.properties`, and `Wiz.bin` are absent.
5. Update `CHANGELOG.md`, `HANDOFF.md`, `TASKS.md`, and `release.properties`.
6. Create and validate the source/handoff package from an independent checkout.
7. Push the reviewed modernization branch. Create a tag or GitHub release only
   after the branch and packaged artifact reproduce the recorded report.

## Canonical upstream pull-request gate

Never open an upstream Core PR merely because static or AI-assisted analysis
found a plausible defect. Before staging a topic, record the affected user or
maintenance behavior, repeatable reproduction, before/after evidence, narrow
compatibility boundary, and remaining physical/platform gaps. If there is no
demonstrated benefit, keep the change out of the upstream merge queue.

Never open an upstream Core PR batch directly. First run:

```bash
python3 tests/upstream-review/preflight.py
```

The manifest-driven preflight rejects an incorrect target/base, stale or
unpushed branch, non-canonical merge base, wrong author or committer email,
changed patch boundary, whitespace error, duplicate intermediate-fork PR, or
GitHub compare mismatch. Open only the manifest's pilot PR after that passes.
Then run:

```bash
python3 tests/upstream-review/preflight.py --require-pilot-green
```

Do not open any remaining PR until `build`, `check-changes`, and `cla/google`
all pass on the pilot. Submit the rest one at a time and stop immediately on a failed
required check. This makes a new external-account problem produce at most one
failure notification instead of one notification per topic.

The topic manifest records `OPEN` and `CLOSED` dispositions. Closed topics stay
auditable so a later automated run cannot silently reopen them. Only one open
topic may be the pilot, and draft protocol/modernization topics are not merge
requests until their documented evidence and design gates pass.

The GitHub Actions workflow performs read-only repository checks. It does not
replace the Ubuntu 26 Docker build and has no release credentials or deployment
step.
