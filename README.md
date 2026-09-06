# OpenSageTV Vibe Core - Ubuntu 26 / Java 11

This repository is the OpenSageTV Vibe fork of
[`OpenSageTV/sagetv`](https://github.com/OpenSageTV/sagetv). It preserves the
upstream Git history and contains the modernized Linux server source. The
repository and package identifier remain `opensagetv-vibe-core`; SageTV's Java,
JNI, protocol, and runtime identities are retained for compatibility. The
default development target is Ubuntu 26.04, amd64, and OpenJDK 11. The
historical directory guide remains in [`README`](README).

The commissioned release target is the Linux SageTV server. Historical Java
MiniClient, native Linux MiniClient/PlaceShifter, and Windows client sources are
retained, but they are not represented as commissioned Vibe release artifacts
until their active tasks in [`TASKS.md`](TASKS.md) pass.

## One-command verification

Docker is the only host prerequisite:

```bash
./sagetv-dev.sh all
```

The command builds one development image, compiles Java and every required Linux native library, runs Java/JNI/ELF/ImageLoader tests, starts and repeatedly stops the server in the same environment, packages the result, and writes `output/BUILD_REPORT.md`. Any required-stage failure returns non-zero.

See [`BUILDING.md`](BUILDING.md), [`docs/UBUNTU26_BUILD.md`](docs/UBUNTU26_BUILD.md), and [`docs/UBUNTU26_BUILD_AUDIT.md`](docs/UBUNTU26_BUILD_AUDIT.md).

The repository also follows the common takeover/update interface in
[`WORKFLOW.md`](WORKFLOW.md): `dev.cmd`/`dev.sh`, `update.cmd`/`update.sh`, and
`create_ai_handoff_zip.cmd` all resolve from their own location.

## Outputs

- `output/server/` — runnable server tree
- `output/native/` — Linux shared libraries
- `output/packages/` — distributable archive
- `output/test-results/` — ELF/JNI/PNG evidence
- `output/logs/` — build and smoke-test logs

Generated output is deliberately not committed. Runtime images consume a pinned package produced from this source; they do not download an unpinned `latest` release.

When the optional XMLTV importer JAR is installed, Core discovers
`xmltv.XMLTVImportPlugin` even if the EPG import property is missing or contains
an obsolete class name. XMLTV setup therefore does not require a historical
SageTV EPG license key.

## Contributing and licensing

Changes intended for this fork should target the modernization branch and pass
the Docker verification gate before release. See [`CONTRIBUTING.md`](CONTRIBUTING.md),
[`SECURITY.md`](SECURITY.md), and [`WORKFLOW.md`](WORKFLOW.md).

SageTV source is provided under the repository [`LICENSE`](LICENSE). Bundled
third-party code retains its own licenses; [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md)
indexes the authoritative license files included in this checkout.
