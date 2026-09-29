# OpenSageTV Vibe Core upstream evaluation

**Evaluation date:** 2026-09-29
**Repository:** `opensagetv-vibe-core`
**Upstream:** `google/sagetv`
**Merge base:** `2e5a892703eaf1f56b1efb7f509021377e65ab69`

## Purpose

This document identifies Vibe Core changes that can be proposed to the main
SageTV Core repository. It deliberately excludes Vibe commissioning tooling,
deployment architecture, and external automation design. Each upstream change
should be isolated, tested, and reviewed on its own merits.

The current branch should not be submitted as one pull request. It combines
general correctness fixes, Linux/native modernization, Vibe MiniClient
protocol work, project packaging, and removal of historical assets.

## Classification

| Class | Meaning |
|---|---|
| **Ready** | General fix with focused coverage; suitable for an isolated pull request. |
| **Validate** | Promising general fix that needs more physical proof or design review. |
| **Protocol** | Requires a documented, versioned MiniClient protocol agreement. |
| **Vibe-only** | Repository, packaging, policy, or product behavior that should remain in Vibe. |
| **Closed** | Withdrawn because the demonstrated maintenance benefit did not outweigh upstream regression risk. |

## Required approvals for a current-Ubuntu canonical container

The updated container is a two-repository upstream change. Core source and
build fixes belong in `google/sagetv`; the runtime image, entrypoint,
supervisor, packages, device mappings, and deployment documentation belong in
`OpenSageTV/sagetv-dockers`. The working Vibe Ubuntu 26 image is integration
evidence, but neither repository should be asked to merge the full Vibe fork.

| Order | Upstream proposal | Why the canonical container needs it | Approval state / remaining gate |
|---:|---|---|---|
| 1 | [`google/sagetv#516`](https://github.com/google/sagetv/pull/516) — container-configurable Linux launcher | A non-root runtime cannot write the old `/var/run/sagetv.pid` default, and orchestrator-provided PID/headless/heap/JVM settings must not be overwritten. | Open draft. Add a focused root/non-root shell harness for defaults, environment overrides, optional `activkey`, and `sagesettings` precedence. |
| 2 | [`google/sagetv#519`](https://github.com/google/sagetv/pull/519) — source-clean build state | A reproducible container build must restore generated version state and must not depend on ignored backup files left by an earlier build. | Open and ready for individual review; canonical checks and CLA pass. |
| 3 | [`google/sagetv#528`](https://github.com/google/sagetv/pull/528) — GCC 15/64-bit native compatibility | Current Ubuntu compilers reject or warn on obsolete prototypes, thread signatures, pointer/handle conversions, IPC assumptions, and bundled native code used by the server package. | Open draft. Split the 18-file patch by native subsystem and attach the exact compiler diagnostic plus affected-platform test for each part. |
| 4 | [`google/sagetv#529`](https://github.com/google/sagetv/pull/529) — Ubuntu 26/ImageLoader stack | Builds SageTV on the current Ubuntu base, replaces obsolete bundled image-codec linkage with maintained system libraries, validates JNI/ELF/image handling, and packages required Ubuntu media utilities. | Open four-commit draft stacked on the native work. Separate the Ubuntu build lane, ImageLoader/system-library change, and runtime utility packaging after upstream agrees on the Linux/Windows/macOS library matrix. |
| 5 | Future `OpenSageTV/sagetv-dockers` current-Ubuntu server image PR | Delivers the approved Core artifact in a non-root current-Ubuntu/Java 11 runtime with current Intel/AMD/NVIDIA userspace packages, stock FFmpeg fallback, supervision, health checks, and documented device mappings. | Not yet submitted. First add a stock-Core lane and prove clean startup, discovery/TCP, restart recovery, no zombies, plugin-absent fallback, and project-scoped cleanup. Introduce it beside the legacy image before proposing any default replacement. |

Dependency order:

```text
#516 launcher ------------------------------+
#519 reproducible build --------------------+--> stock-Core Ubuntu gate
#528 GCC/64-bit --> #529 Ubuntu/ImageLoader-+            |
                                                        v
                              OpenSageTV/sagetv-dockers current-Ubuntu PR
```

PR #517 (network encoder identity) is conditional OpenDCT/container work, not a
base-image dependency. It stays draft until an affected installation supplies
before/after scan evidence. The closed discovery proposal #518 is not required:
canonical Core already receives and replies on the same discovery socket. DVD,
MiniClient capability, and optional stream-transform PRs are also independent
of the current-Ubuntu container approval path.

The acceptance gate must build and test both payloads from the same pinned
environment:

- canonical stock Core plus only the proposed upstream patches;
- Vibe Core with its optional negotiated extensions;
- clean non-root startup and writable PID/supervisor state;
- Java 11, UDP discovery, TCP service, restart recovery, and zero zombies;
- stock FFmpeg with no Vibe plugins installed;
- optional FFmpeg/Core-MCP plugins installed without a modified stock JAR;
- Intel, AMD, and NVIDIA device exposure where matching hardware exists, with
  deterministic software fallback when acceleration is absent or fails;
- no user appdata, properties, database, recordings, credentials, or license
  data in any image or test artifact.

## Prepared review branches

All branches below are published to the Vibe fork, not directly to the
canonical repository. Except where noted, each is one commit based directly on
`2e5a8927` so reviewers can inspect, test, accept, or reject it independently.
Production changes include comments explaining non-obvious compatibility and
failure behavior; tests use descriptive names rather than repeating the code
in comments.

| Branch | Commit | Gate | Status |
|---|---:|---|---|
| `sagetv-review/linux-server-launcher` | `53729439` | `bash -n`; non-root Ubuntu 26 path check | Draft: corrected container scope; add focused launcher regression coverage |
| `sagetv-review/network-encoder-identity` | `c953de27` | complete Java test suite | Draft: needs an affected installation and before/after scan evidence |
| `sagetv-review/miniclient-discovery-interface` | `57ce81f6` | complete Java test suite | Closed: upstream already replies through the receiving socket; no demonstrated benefit remained |
| `sagetv-review/native-gcc15-64bit` | `d0d9de2a` | Java suite; native build reaches the known legacy bundled-JPEG baseline failure | Draft: split by native subsystem and prove supported-platform behavior |
| `sagetv-review/build-source-clean` | `089984ed` | complete Java suite; focused `compileJava` restores identical source and removes its backup | Ready for individual review; unrelated line-ending policy removed |
| `sagetv-review/ubuntu26-imageloader` | `538d9527` | Java, all native libraries, JNI/ELF, ImageLoader fixtures, packaging, and smoke gate | Draft: requires an upstream platform/library decision; line-ending policy removed |
| `sagetv-review/shutdown-hardening` | `4de3ed64` | complete Java suite | Closed: no reproduced shutdown failure or failure-injection evidence |
| `sagetv-review/imported-metadata-repair` | `bbc2b22c` | focused Java tests | Closed: observed only on a modified server; no stock-server reproduction |
| `sagetv-review/dvd-vm-safety` | `a188a3d9` | focused Java tests | Closed: synthetic coverage only; no affected disc or user-visible failure |
| `sagetv-review/dvd-path-normalization` | `eef4c917` | focused Java tests | Closed: commissioning-only parent-root input is already normalized by the stock-compatible plugin |
| `sagetv-review/dvd-runtime-correctness` | `6dedff4f` | focused Java tests | Draft: observed issue exists, but the patch must be reduced and physically validated |
| `sagetv-review/dvd-main-feature-selection` | `40a6ddf8` | complete Java suite | Closed: Vibe-specific policy heuristic without adequate disc-corpus evidence |
| `sagetv-review/miniclient-capability-protocol` | `175ed345` | focused Java protocol tests | Draft: protocol/design review required before implementation review |
| `sagetv-review/dvd-transform-provider` | `3b8d0cc6` | complete Java suite and `sageJar` on the explicit stack | Draft: stacked behind protocol acceptance and provider evidence |

Canonical SageTV pull requests opened on 2026-09-29 after explicit approval:

- [#516 Container-configurable Linux server launcher (draft)](https://github.com/google/sagetv/pull/516)
- [#517 Network encoder identity (draft)](https://github.com/google/sagetv/pull/517)
- [#518 MiniClient discovery interface (closed)](https://github.com/google/sagetv/pull/518)
- [#519 Source-clean build state (ready)](https://github.com/google/sagetv/pull/519)
- [#520 DVD VM link safety (closed)](https://github.com/google/sagetv/pull/520)
- [#521 DVD path normalization (closed)](https://github.com/google/sagetv/pull/521)
- [#522 Shutdown completion hardening (closed)](https://github.com/google/sagetv/pull/522)
- [#523 Invalid imported-media metadata repair (closed)](https://github.com/google/sagetv/pull/523)
- [#524 DVD main-feature selection (closed)](https://github.com/google/sagetv/pull/524)
- [#525 MiniClient capability protocol (draft)](https://github.com/google/sagetv/pull/525)
- [#526 DVD runtime correctness (draft)](https://github.com/google/sagetv/pull/526)
- [#527 Optional DVD stream-transform provider (draft)](https://github.com/google/sagetv/pull/527)
- [#528 GCC 15 and 64-bit native compatibility (draft)](https://github.com/google/sagetv/pull/528)
- [#529 Ubuntu 26 and ImageLoader modernization (draft)](https://github.com/google/sagetv/pull/529)

Canonical SageTV's `check-changes` and Google CLA checks pass on every open
pull request. Following maintainer feedback about SageTV's maintenance state,
the set was reduced to one small ready change, seven explicit drafts, and six
closed proposals. Closed branches remain as auditable references but must not
be reopened without a reproduced user-visible problem, before/after evidence,
focused regression coverage, and a patch whose description exactly matches its
scope.

The only ready pull request is #519. PRs #516-#517 and #525-#529 remain drafts.
PRs #518 and #520-#524 are closed. The launcher and
Ubuntu/GCC/native
changes in #528-#529 are still intended for canonical SageTV; draft status
means they must be split and supported by an upstream platform matrix, not that
the newer-Ubuntu requirement has been abandoned. The recorded local gates
remain useful implementation evidence, but passing tests alone is not enough to
establish that a maintenance change belongs upstream.
The older `upstream-review/network-encoder-discovery` branch is retained as a
non-destructive reference but is superseded by the two narrower network rows
above and should not be used for a pull request.

## Recommended upstream pull requests

### Linux server launcher — Draft, Docker priority

PR #516 is now scoped to the patch it actually contains. Canonical
`startsagecore` hard-codes `/var/run/sagetv.pid` and overwrites container-
provided `PIDFILE`, `HEADLESS`, `JAVAMEM`, and `JAVAOPTS` values. The Ubuntu 26
development container proves its non-root runtime identity cannot write
`/var/run` but can write `/tmp`. The patch also avoids an error for the obsolete
optional `activkey` file while retaining `sagesettings` as the final override.

This is active canonical Docker maintenance work, not a Vibe-only policy. Keep
the PR draft until a focused shell harness proves default and explicit values
for root and non-root launches, then present it independently from the broader
Ubuntu/native stack.

### Network encoder identity — Draft; MiniClient discovery — Closed

- Send the capture device's local name for OpenDCT `AUTOSCAN` and
  `AUTOINFOSCAN` requests.
- Use the packet source's numeric address instead of reverse-DNS host names
  when registering network encoders.
The encoder-identity patch remains a draft until an affected OpenDCT
installation demonstrates the old failure and the corrected request identity.
The discovery proposal was closed because canonical upstream already receives
and replies through `miniDiscoverySocket`; the remaining explicit reuse/bind
change had no demonstrated user benefit.

### DVD path handling — Closed; other DVD runtime changes — Closed / Draft

The path-normalization proposal was closed after maintainer review. Its parent-
disc-root input came from external exact-path commissioning rather than a
reproduced normal SageTV import/playback failure. Stock SageTV intentionally
indexes `VIDEO_TS`, and the stock-compatible Core MCP plugin already resolves a
commissioning parent root to that indexed MediaFile before calling the public
API. The focused Core test proved only the proposed normalization, not a user
problem, so changing `Wizard` would add risk without demonstrated benefit.

The PGC-link guard and longest-title main-feature policy were closed because
they lacked affected-disc evidence or were Vibe-specific policy. The broader
runtime-correctness proposal remains draft because a real negative DVD skip was
observed, but its multiple behavioral changes must be reduced to the smallest
fix and validated on a physical disc before upstream review.

Candidate runtime behaviors that still require that evidence include:

- Preserve authored SetSTN audio and subpicture choices.
- Keep the DVD VM button authoritative across menu and PGC transitions.
- Hold a temporary STC anchor after seek until a new navigation pack supplies
  the clock.
- Clamp invalid seeks and make direct-control job completion deterministic only
  where they are necessary to reproduce and fix the reported failure.

The Vibe Native/Hybrid/optional-transform transport is not part of these
generic fixes.

### Shutdown completion hardening — Closed

The proposal was closed because there was no reproduced affected installation
or failure-injection proof. It can be reconsidered only if a real shutdown hang
is captured and a focused fix can be demonstrated across desktop, service, and
embedded modes.

### Invalid imported-media metadata repair — Closed

The proposal was closed because the invalid metadata was observed only on the
modified Vibe server while stock `.175` handled the tested MKV files normally.
Keep any repair in Vibe until a malformed import is reproduced on canonical
Core and valid-file non-regression is proven.

### Ubuntu 26, Java 11, GCC 15, and 64-bit native fixes — Draft

This is an active upstream maintenance requirement. Canonical SageTV needs the
applicable compiler, native-library, ImageLoader, and reproducible-build changes
to build and run on a current Ubuntu base where current GPU kernel/userspace
drivers and hardware-acceleration packages are available. The working Vibe
Ubuntu 26 container and its passing gates provide integration evidence. PRs
#528-#529 remain open while the stack is separated into reviewable changes;
they must not be reclassified as Vibe-only merely because the current proposal
is too broad.

Candidates include:

- Modern function prototypes and POSIX thread entry signatures.
- 64-bit-safe native handles, pointer conversions, IPC structures, and iovec
  handling.
- JNI export, dependency, and ELF validation.
- Source-clean build-number restoration and complete clean builds.
- LF enforcement for executable Linux launchers checked out on Windows.

Submit by native subsystem and preserve every platform contract supported by
upstream. Vibe's sibling-project build orchestration is not an upstream change.

### ImageLoader modernization — Validate

Vibe builds ImageLoader against maintained system PNG, GIF, JPEG, and TIFF
libraries and adds generated fixtures, JNI checks, and malformed-input
containment tests. This should be a dedicated series because it changes native
dependencies, packaging, and behavior together. Upstream must agree on its
minimum Linux, Windows, and macOS library matrix.

### Build and CI hygiene — One ready fix; broader work requires evidence

PR #519 contains the isolated source-clean and reproducible build-number fix
and is ready for individual review. Current GitHub Action runtimes,
required-suite enforcement, and line-ending checks should be proposed only
against upstream's actual workflow with a demonstrated maintenance need. Vibe
names, handoff commands, release policies, and disabled historical deployment
targets remain fork-specific.

## MiniClient changes requiring protocol review

These are not ordinary bug fixes and should remain in Vibe until OpenSageTV
accepts a documented capability negotiation and compatibility contract:

- `MEDIA_STATE_URL`
- `DVD_REMOTE_NAV`
- `DVD_DISC_TRANSPORTS`
- `DVD_DISC_POLICY`
- `DVD_DISC_SKIP_MENUS`
- `DVD_DISC_SKIP_PREVIEWS`
- `DVD_DISC_NATIVE_FALLBACK`
- `VIDEO_PLAYBACK_RATE`
- `VIDEO_CC_STATE` and any new caption/subtitle payload delivery
- MiniPlayer media command 30 for negotiated playback rate
- Native/Hybrid DVD policy and the optional `DVDStreamTransformProvider` SPI

Old private commissioning reply events 230–233 have been removed from Core.
They are not upstream proposals. Exact watch, tune, and seek operations can use
the supported SageTV API, while Android display recovery now refreshes its
local video output without replacing or seeking the server stream.

## Vibe-only changes

- OpenSageTV Vibe repository names, workflow wrappers, handoff format, release
  properties, and publication policy.
- The external component-update and container commissioning model.
- Vibe's exact container composition and deployment policy. Applicable Core
  changes required for current Ubuntu and GPU-driver compatibility remain
  canonical upstream candidates through #528-#529.
- Removal or relocation of archived firmware, installers, generated binaries,
  and historical source snapshots; upstream owns its archival policy.
- Vibe-specific defaults or UI policy that are not general correctness fixes.
- Automatic XMLTV importer discovery and `epg/epg_import_plugin` repair. This
  remains temporary Vibe compatibility behavior until a stock-compatible
  SageTV Standard-plugin wrapper can own explicit setup and migration.
- Packaging that assumes the sibling Vibe build environment or container.

## Proposed pull-request sequence

1. Review source-clean build state (#519) independently.
2. Keep OpenDCT identity (#517) draft until an affected installation supplies
   before/after evidence.
3. Reduce the DVD runtime draft (#526) to the smallest fix for the reproduced
   negative-skip problem and add physical evidence.
4. Discuss the MiniClient capability contract (#525) before reviewing any
   dependent DVD provider work (#527).
5. Split native/compiler and ImageLoader modernization (#528-#529) by subsystem
   only after upstream agrees on its supported platform and library matrix.
6. Do not reopen closed topics without new user-visible evidence.

## Minimal `sage.jar` target

The Vibe Core delta should converge toward:

1. General fixes accepted by upstream.
2. A small, explicit compatibility patch set only for runtime capabilities
   that cannot be expressed by existing public APIs.
3. No private commissioning-only MiniClient events.
4. No secrets, local paths, user databases, or deployment state in Core.

## Staged protocol/design topic

### Optional DVD stream transform

Core now exposes a provider-neutral `DVDStreamTransformProvider` SPI instead of
embedding `MiniDVDStreamTranscoder`. The Core half has no MIM/FFmpeg imports,
executable names, private command-line switches, capability JSON, or process
management. It discovers providers through the existing extension classloader,
requires an exact `DVD_DISC_TRANSPORTS` intersection, and otherwise stays on
native DVD. An in-memory test provider proves the Core build/runtime contract
without external software.

The separately packaged FFmpeg plugin implements `dvd_mpegts_v1` and owns all
MIM behavior. Its Standard-plugin entry point remains loadable on stock Core;
only updated Core discovers the lazy SPI implementation. The review source is
published as `sagetv-review/dvd-transform-provider` at `3b8d0cc6`. It is an
explicit six-commit stack over canonical `2e5a8927`: DVD VM safety, path
normalization, runtime correctness, main-feature selection, MiniClient
capability negotiation, then the provider SPI. This ordering is intentional;
the final commit is not a standalone patch and must not be proposed before the
`DVD_DISC_*` contract is accepted. Plugin and physical-disc evidence should be
linked as integration evidence, not introduced as Core dependencies.

## Changes deliberately not staged as standalone upstream code

### Repository workflow changes

The Vibe `repository-checks.yml`, `dev.*`, `update.*`, handoff tooling,
`release.properties`, sibling build-container delegation, and action-runtime
updates enforce the Vibe repository contract. Upstream currently has a
different `ci.yml` and release process. Copying the Vibe workflow would require
files and policies upstream does not have, so it is not a portable Core patch.
Individual action-runtime updates should be made in upstream's own workflow.

### Commissioning and deployment changes

Exact-path/channel commissioning, MCP control adapters, container composition,
historical-asset relocation, and release packaging are intentionally excluded.
They can exercise public SageTV APIs without changing `sage.jar` and therefore
do not belong in an upstream Core pull request.

## Verification required for every extraction

- Focused Java or native regression coverage.
- Complete clean Ubuntu 26 / Java 11 build and packaging gate.
- Server startup, discovery, and repeated shutdown smoke tests.
- Physical playback evidence when timing, DVD VM, captions, or metadata are
  affected.
- Review against upstream-supported Windows, Linux, macOS, client, and server
  configurations.
