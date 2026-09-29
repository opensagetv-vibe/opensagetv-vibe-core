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

## Prepared review branches

All branches below are published to the Vibe fork, not directly to the
canonical repository. Except where noted, each is one commit based directly on
`2e5a8927` so reviewers can inspect, test, accept, or reject it independently.
Production changes include comments explaining non-obvious compatibility and
failure behavior; tests use descriptive names rather than repeating the code
in comments.

| Branch | Commit | Gate | Status |
|---|---:|---|---|
| `sagetv-review/linux-server-launcher` | `53729439` | `bash -n` | Ready |
| `sagetv-review/network-encoder-identity` | `6310ec98` | complete Java test suite | Ready |
| `sagetv-review/miniclient-discovery-interface` | `7d2df3ef` | complete Java test suite | Ready |
| `sagetv-review/native-gcc15-64bit` | `da9c75f5` | Java suite; native build reaches the known legacy bundled-JPEG baseline failure | Review with modernization stack |
| `sagetv-review/build-source-clean` | `60d2ae51` | complete Java suite; source restored after compile | Ready |
| `sagetv-review/ubuntu26-imageloader` | `081763da` | Java, all native libraries, JNI/ELF, ImageLoader fixtures, packaging, and smoke gate | Ready as a four-commit stack on the native branch |
| `sagetv-review/shutdown-hardening` | `7d2368aa` | complete Java suite | Validate failure injection |
| `sagetv-review/imported-metadata-repair` | `dad176ef` | focused Java tests | Validate physical malformed-import gate |
| `sagetv-review/dvd-vm-safety` | `aa6d903c` | focused Java tests | Ready |
| `sagetv-review/dvd-path-normalization` | `2ce6419d` | focused Java tests | Ready |
| `sagetv-review/dvd-runtime-correctness` | `e0bda13b` | focused Java tests | Validate physical DVD gate |
| `sagetv-review/dvd-main-feature-selection` | `79792c19` | complete Java suite | Validate broader disc corpus |
| `sagetv-review/miniclient-capability-protocol` | `a1201460` | focused Java protocol tests | Protocol review required |
| `sagetv-review/dvd-transform-provider` | `bf211a8c` | complete Java suite and `sageJar` on the explicit six-commit stack | Protocol/design review; stacked after the four DVD topics and capability protocol |

Canonical SageTV pull requests opened on 2026-09-29 after explicit approval:

- [#516 Linux server launcher](https://github.com/google/sagetv/pull/516)
- [#517 Network encoder identity](https://github.com/google/sagetv/pull/517)
- [#518 MiniClient discovery interface](https://github.com/google/sagetv/pull/518)
- [#519 Source-clean build state](https://github.com/google/sagetv/pull/519)
- [#520 DVD VM link safety](https://github.com/google/sagetv/pull/520)
- [#521 DVD path normalization](https://github.com/google/sagetv/pull/521)
- [#522 Shutdown completion hardening (draft)](https://github.com/google/sagetv/pull/522)
- [#523 Invalid imported-media metadata repair (draft)](https://github.com/google/sagetv/pull/523)
- [#524 DVD main-feature selection (draft)](https://github.com/google/sagetv/pull/524)
- [#525 MiniClient capability protocol (draft)](https://github.com/google/sagetv/pull/525)
- [#526 DVD runtime correctness (draft)](https://github.com/google/sagetv/pull/526)
- [#527 Optional DVD stream-transform provider (draft)](https://github.com/google/sagetv/pull/527)
- [#528 GCC 15 and 64-bit native compatibility (draft)](https://github.com/google/sagetv/pull/528)
- [#529 Ubuntu 26 and ImageLoader modernization (draft)](https://github.com/google/sagetv/pull/529)

Canonical SageTV's `check-changes` workflow passes on all fourteen head
branches. Google's CLA check currently fails uniformly and must be completed by
the submitting account or its organization before any topic can merge; this is
an external contributor-authorization gate, not a source or test failure. The
recorded local gates remain the functional validation evidence.

Every current, non-superseded review branch has now been submitted. The eight
validation-, native-modernization-, metadata-, shutdown-, and protocol-dependent
topics are drafts so their publication creates reviewable source without
representing that SageTV has accepted the behavior or protocol.
The older `upstream-review/network-encoder-discovery` branch is retained as a
non-destructive reference but is superseded by the two narrower network rows
above and should not be used for a pull request.

## Recommended upstream pull requests

### Network encoder identity and MiniClient discovery — Ready

- Send the capture device's local name for OpenDCT `AUTOSCAN` and
  `AUTOINFOSCAN` requests.
- Use the packet source's numeric address instead of reverse-DNS host names
  when registering network encoders.
- Reply to MiniClient discovery through the socket that received the request,
  allowing the kernel to select the correct source address on multi-interface,
  container, macvlan, and ipvlan hosts.

These are protocol and identity corrections with focused network tests. The
OpenDCT and discovery changes should be separate pull requests.

### DVD VM safety and path handling — Ready / Validate

Strong isolated candidates:

- Validate PGC link indices and null targets before following them.
- Clamp negative DVD and Blu-ray seeks to zero.
- Remove completed or failed DVD direct-control jobs deterministically.
- Normalize disc roots, `VIDEO_TS`, and paths inside `VIDEO_TS`.
- Avoid tight retry and repeated empty-read logging loops.
- Yield decoder/control locks while DVD control work is pending.

Changes requiring separate physical evidence and review:

- Preserve authored SetSTN audio and subpicture choices.
- Keep the DVD VM button authoritative across menu and PGC transitions.
- Hold a temporary STC anchor after seek until a new navigation pack supplies
  the clock.
- Select the main feature with the longest valid first-referenced PGC.

The Vibe Native/Hybrid/optional-transform transport is not part of these
generic fixes.

### Shutdown completion hardening — Validate

The terminal shutdown state and process-exit path now run even if linkage or
plugin cleanup throws. This prevents a headless server from remaining alive
after its listeners have already stopped. Upstream review should cover desktop,
service, and embedded modes and include failure-injection tests.

### Invalid imported-media metadata repair — Validate

The disabled-by-default repair is restricted to completed, imported, local,
single-segment video with corroborating invalid metadata. It excludes
recordings, live streams, discs, remote inputs, pictures, music, growing files,
and valid rows. It should not be proposed until the remaining physical gate
proves malformed imports recover without changing valid files.

### Ubuntu 26, Java 11, GCC 15, and 64-bit native fixes — Ready / Validate

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

### Build and CI hygiene — Ready / Validate

Suitable candidates include current GitHub Action runtimes, required-suite
enforcement, source-clean behavior, reproducible build-number restoration,
and line-ending checks. Vibe names, handoff commands, release policies, and
disabled historical deployment targets remain fork-specific.

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
- Removal or relocation of archived firmware, installers, generated binaries,
  and historical source snapshots; upstream owns its archival policy.
- Vibe-specific defaults or UI policy that are not general correctness fixes.
- Automatic XMLTV importer discovery and `epg/epg_import_plugin` repair. This
  remains temporary Vibe compatibility behavior until a stock-compatible
  SageTV Standard-plugin wrapper can own explicit setup and migration.
- Packaging that assumes the sibling Vibe build environment or container.

## Proposed pull-request sequence

1. OpenDCT scan identity.
2. Interface-safe MiniClient discovery.
3. DVD bounds checking, negative-seek clamping, and control-job cleanup.
4. DVD path normalization and empty-read containment.
5. Source-clean and line-ending build fixes.
6. Native compiler and 64-bit corrections, one subsystem at a time.
7. ImageLoader modernization.
8. Shutdown hardening after failure-injection coverage.
9. Imported-metadata repair after the physical gate.
10. Separate protocol proposal for any Vibe MiniClient capability OpenSageTV
    wants to standardize.

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
published as `sagetv-review/dvd-transform-provider` at `bf211a8c`. It is an
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
