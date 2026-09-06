# Contributing to OpenSageTV Vibe Core

This project is a compatibility-focused fork of `OpenSageTV/sagetv`. Keep
changes narrowly scoped, preserve existing SageTV/JNI/protocol behavior unless
a documented modernization requires a change, and retain Java 11 compatibility.

## Development environment

Docker is the only host build prerequisite. Place this checkout beside
`opensagetv-vibe-build-env`, then run:

```bash
./sagetv-dev.sh all
```

That command builds Java and required Linux native libraries, runs the Java,
native, JNI, ELF, libpng, malformed-input, and server smoke/shutdown gates,
packages the server, and writes `output/BUILD_REPORT.md` in one Ubuntu 26.04
development container.

Windows users may run `dev.cmd all` from any current directory. Linux/WSL users
may run `./dev.sh all`. See `BUILDING.md`, `WORKFLOW.md`, and
`docs/UBUNTU26_BUILD.md` for focused commands.

## Change requirements

- Do not commit generated `output/`, appdata, recordings, credentials,
  `Sage.properties`, or `Wiz.bin`.
- Preserve LF endings for Linux launchers, build scripts, and package scripts.
- Add a focused regression test for a compatibility or crash fix.
- Record user-visible and build/release changes in `CHANGELOG.md`.
- Update `HANDOFF.md` when verified takeover state changes.
- Remove completed entries from `TASKS.md`; do not create per-version status
  Markdown or text files.
- Report skipped physical or platform checks as `SKIPPED`, never as passing.

Before submitting a change, run the smallest relevant focused test and the
complete `./sagetv-dev.sh all` gate. Include the relevant
`output/BUILD_REPORT.md` result in the review description without committing
the generated report.

## Upstream relationship

Use `origin` for `opensagetv-vibe/opensagetv-vibe-core` and a read-only
`upstream` remote for `OpenSageTV/sagetv`. Keep modernization commits reviewable
and avoid mixing unrelated formatting or application-behavior changes.
