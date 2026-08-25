# OpenSageTV Core — Ubuntu 26 / Java 11

This repository preserves SageTV's upstream history and contains the modernized Linux server source. The default development target is Ubuntu 26.04, amd64, and OpenJDK 11. The historical directory guide remains in [`README`](README).

## One-command verification

Docker is the only host prerequisite:

```bash
./sagetv-dev.sh all
```

The command builds one development image, compiles Java and every required Linux native library, runs Java/JNI/ELF/ImageLoader tests, starts and repeatedly stops the server in the same environment, packages the result, and writes `output/BUILD_REPORT.md`. Any required-stage failure returns non-zero.

See [`BUILDING.md`](BUILDING.md), [`docs/UBUNTU26_BUILD.md`](docs/UBUNTU26_BUILD.md), and [`docs/UBUNTU26_BUILD_AUDIT.md`](docs/UBUNTU26_BUILD_AUDIT.md).

## Outputs

- `output/server/` — runnable server tree
- `output/native/` — Linux shared libraries
- `output/packages/` — distributable archive
- `output/test-results/` — ELF/JNI/PNG evidence
- `output/logs/` — build and smoke-test logs

Generated output is deliberately not committed. Runtime images consume a pinned package produced from this source; they do not download an unpinned `latest` release.
