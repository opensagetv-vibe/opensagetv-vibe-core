# Common project workflow

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
OpenSageTV Vibe fork and its read-only `upstream` is `OpenSageTV/sagetv`.
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

The GitHub Actions workflow performs read-only repository checks. It does not
replace the Ubuntu 26 Docker build and has no release credentials or deployment
step.
