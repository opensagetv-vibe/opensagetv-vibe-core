# OpenSageTV Vibe Core: Upstream and MCP Control Evaluation

**Evaluation date:** 2026-09-20  
**Repository:** `opensagetv-vibe-core`  
**Compared with:** `OpenSageTV/sagetv` `master`  
**Merge base:** `e95c495d5d5948b8db7f78f4cdc7c563440162ab`  
**Evaluated Vibe revision:** `9f4484ca` (`Route DVD MIM through transcoder resolver`)

## Purpose

This document evaluates the current Vibe Core changes for two separate goals:

1. Return generally useful, low-risk fixes to the main OpenSageTV repository.
2. Move server commissioning and automation controls out of `sage.jar` and into a stock-compatible SageTV plugin with an external MCP adapter.

The document began as a read-only evaluation. It now also records the completed
stock-compatible control-plugin implementation and physical commissioning
results. It still does not claim that all Vibe behavior belongs upstream.

## Executive conclusion

The current Vibe branch is **25 commits ahead and 0 commits behind** the current upstream merge base. The complete diff is 221 files, 3,205 insertions, and 200,684 deletions. Most deletions are historical firmware, installers, and source archives moved out of the active Vibe project; they are not useful upstream changes. The meaningful runtime delta is much smaller.

The changes should not be submitted as one pull request. They divide into four groups:

| Group | Recommendation |
|---|---|
| General SageTV correctness fixes | Extract into small upstream pull requests with focused tests. |
| Linux/Ubuntu 26 and native-code modernization | Upstream in independent build/native pull requests after confirming the upstream platform matrix. |
| Server commissioning and automation controls | Replace with a stock-compatible SageTV control plugin plus an external MCP adapter where the public SageTV API is sufficient. |
| Vibe MiniClient protocol, DVD transport, and caption extensions | Keep in the Vibe fork unless OpenSageTV accepts a documented, versioned MiniClient protocol extension. |

The preferred long-term shape is therefore:

```text
OpenSageTV upstream sage.jar
    + generally useful Vibe fixes accepted upstream
    + optional stock-compatible Vibe Control Bridge plugin
          + authenticated control API
          + external MCP server/adapter
    + Vibe Core compatibility patch set only for capabilities that cannot be plugins
```

This minimizes the maintained `sage.jar` delta without pretending that an ordinary SageTV plugin can replace code inside `MiniClientSageRenderer`, `MiniPlayer`, `VideoFrame`, or `MiniDVDPlayer`.

## Classification key

| Class | Meaning |
|---|---|
| **U1** | Strong upstream candidate now, after isolating it and retaining focused tests. |
| **U2** | Upstream candidate after more physical validation, extraction, or maintainer design agreement. |
| **P1** | Can be implemented through supported stock SageTV plugin/API behavior. |
| **P2** | Partially possible as a plugin, but semantics are weaker or STV-dependent. |
| **C1** | Requires a Core/MiniClient protocol change; could be proposed as a versioned upstream extension. |
| **V1** | Vibe-specific repository, packaging, or policy change; do not upstream. |

## Recommended upstream changes

### 1. Network encoder identity and discovery — U1

Current changes:

- `NetworkCaptureDevice` sends the capture device's local name for `AUTOSCAN` and `AUTOINFOSCAN`, matching the identity expected by OpenDCT.
- `NetworkEncoderManager` uses the packet source's numeric IP address instead of reverse-DNS host names. This prevents the same encoder from being registered under inconsistent names.
- `SageTV` replies to MiniClient discovery through the same bound socket and lets the kernel select the correct source address. This improves Docker, macvlan, ipvlan, and multi-interface discovery.

Why it belongs upstream:

- These are protocol and network-identity corrections, not Vibe features.
- They improve stock SageTV/OpenDCT deployments.
- Focused tuner/network tests already exist in the Vibe workflow.

Suggested upstream split:

- One pull request for OpenDCT scan-name and numeric-address identity.
- One pull request for interface-safe MiniClient discovery.

### 2. DVD VM and playback safety — U1/U2

General fixes that can be extracted from the Vibe DVD work:

- Validate DVD PGC link indices and null targets before following them, preventing malformed navigation data from causing array-bound or null failures.
- Clamp negative DVD/Blu-ray seeks to zero.
- Always remove completed/failed DVD direct-control jobs, preventing a malformed command from entering a tight retry loop.
- Preserve an authored menu's audio/subpicture choice instead of immediately overwriting its SetSTN decision with SageTV defaults.
- Yield the decoder/control lock correctly while DVD control work is pending.
- Keep the current VM button authoritative after menu/PGC transitions.
- Force a temporary STC anchor after a seek until a new navigation pack establishes the updated clock.
- Throttle repeated empty-read logging.
- Normalize DVD paths that point at a volume root, `VIDEO_TS`, or a file inside `VIDEO_TS`.

Assessment:

- Bounds checking, negative-seek clamping, control-job cleanup, path normalization, and lock correctness are strong upstream candidates.
- STC anchoring and authored SetSTN preservation should be submitted separately with regression tests because they alter playback timing or selection policy.
- The new “main feature” heuristic in the DVD VM (longest first-referenced PGC, with malformed-title containment) is useful but policy-bearing. It should be a separate U2 proposal.
- The Vibe MIM-to-TS transport itself is not part of this group and should not be hidden inside a generic DVD-fix pull request.

### 3. Shutdown completion hardening — U2

`SageTV` now guarantees that the terminal shutdown state and process-exit path run even when linkage or plugin cleanup throws during shutdown.

Potential benefit:

- Prevents a partially stopped server from remaining alive after a cleanup failure.

Reason for separate review:

- Shutdown ordering is sensitive.
- Upstream should review whether forced exit is correct for all desktop, service, and embedded modes.
- This should have failure-injection tests rather than be included with unrelated Linux changes.

### 4. XMLTV importer discovery — U2

Current behavior:

- If `epg/epg_import_plugin` is empty and `xmltv.XMLTVImportPlugin` is installed, SageTV selects it.
- If the configured importer is invalid and the XMLTV importer is installed, SageTV falls back to XMLTV and repairs the property.
- `hasEPGPlugin()` uses the same discovery behavior.

Potential benefit:

- Removes an obsolete setup/license assumption and makes an installed XMLTV importer discoverable.

Upstream concern:

- Automatically persisting a fallback is policy, not only a bug fix.
- It should be discussed alongside future multi-importer or per-data-source EPG work.
- It must not unexpectedly replace an intentionally disabled or temporarily missing custom importer.

### 5. Invalid imported-media metadata repair — U2

Current behavior:

- Disabled by default through `videoframe/repair_invalid_imported_metadata_on_playback`.
- Applies only to an imported, local, non-recording, non-live, non-disc media file with one regular segment of at least 64 KiB.
- Requires clearly invalid metadata: duration at or below 1 ms together with an invalid format duration or no streams.
- Reinitializes metadata immediately before the watch operation.

Assessment:

- The narrow eligibility checks are appropriate.
- The current project task state still calls for physical proof on real malformed imported files.
- Direct metadata reinitialization uses Core internals and is not a clean public-plugin operation.
- Submit upstream only after physical validation and tests proving valid metadata and growing recordings remain untouched.

### 6. Ubuntu 26, OpenJDK 11, GCC 15, and 64-bit native fixes — U1/U2

The branch contains the following modernization work:

- Reproducible Ubuntu 26 development/build workflow.
- OpenJDK 11 Core build and Linux server packaging validation.
- GCC 15-compatible function prototypes and callback signatures.
- Correct POSIX thread entry signatures.
- 64-bit-safe native handles, pointer conversions, IPC structures, and iovec conversions.
- Targeted fixes in capture, demux, transcoder, Freetype/JNI, JTux, and the retained private FFmpeg tree.
- JNI export validation and maintained native-library tests.
- Source-clean build-number restoration and source-clean full builds.
- LF enforcement for executable Linux launchers checked out on Windows.

Assessment:

- Compiler correctness and 64-bit safety are upstream-worthy when split by subsystem.
- The upstream build matrix may still include platforms and compilers not commissioned by Vibe; every native pull request must preserve those contracts.
- Vibe's single-container orchestration and sibling `opensagetv-vibe-build-env` dependency are project workflow choices, not suitable as-is for upstream.

### 7. ImageLoader modernization — U2

Current changes:

- Build the LGPL ImageLoader against maintained system PNG, GIF, JPEG, and TIFF libraries.
- Update transformations and error handling for modern codec behavior.
- Add generated image fixtures, native tests, JNI export checks, and error-containment tests.

Assessment:

- This removes dependence on obsolete codec snapshots and has meaningful automated coverage.
- It should be proposed as its own upstream series because it changes dependencies, packaging, and native image behavior together.
- Upstream maintainers must agree on minimum distro/library versions and Windows/macOS implications before merging.

### 8. Build and CI hygiene — U1/U2

Potentially upstreamable pieces:

- Current GitHub Action runtimes.
- Tests that fail when maintained Linux suites are accidentally skipped.
- Source-clean build behavior.
- Reproducible build-number restoration.
- Launcher line-ending enforcement.

Fork-only pieces:

- OpenSageTV Vibe names, release properties, handoff commands, and repository policies.
- Disabled historical deployment behavior aimed at `google/sagetv`.
- Vibe-specific release deletion lists and unified sibling-project tooling.
- Removal of archived firmware/installers/source bundles. Upstream must decide its own archive policy.

## Vibe Core changes that should not be submitted as ordinary bug fixes

### MiniClient events and properties — C1

The Vibe branch adds raw MiniClient events:

| Event | Purpose |
|---:|---|
| 230 | Watch an exact indexed media file. |
| 231 | Tune an exact channel. |
| 232 | Watch an exact indexed media file from the beginning. |
| 233 | Perform an exact server-side seek using a signed 64-bit millisecond value. |

It also adds or consumes these negotiated properties/capabilities:

- `MEDIA_STATE_URL`
- `DVD_REMOTE_NAV`
- `VIBE_DISC_TRANSPORTS`
- `VIBE_DISC_POLICY`
- `VIBE_DISC_SKIP_MENUS`
- `VIBE_DISC_SKIP_PREVIEWS`
- `VIBE_DISC_NATIVE_FALLBACK`
- `VIBE_PLAYBACK_RATE`
- `VIBE_CURRENT_CHANNEL`
- `VIDEO_CC_STATE`

These touch the MiniClient wire protocol and server/player state. They should either:

1. remain Vibe-specific; or
2. be proposed as one documented, versioned, capability-negotiated MiniClient extension with compatibility tests for old clients and HD200/HD300 behavior.

They should not be mixed into unrelated upstream bug-fix pull requests.

### Playback-rate media command 30 — C1

The stock SageTV API already exposes `GetPlaybackRate()` and `SetPlaybackRate(float)` to server-side UI-context code. Vibe's media command 30 is different: it carries that capability over the MiniClient media-command path to the Android player.

- An MCP control plugin can set the server UI's playback rate without event 30.
- The Android client still needs a negotiated wire command if it must request or execute player-specific rate changes through the MiniClient transport.

### Native/Hybrid/MIM DVD transport — C1

The following are internal media-pipeline changes and cannot be installed as an ordinary plugin:

- Selection between `MiniDVDPlayer` and `MiniPlayer` in `VideoFrame`.
- Native/Hybrid/MIM disc-policy negotiation.
- Remote Android DVD navigation selection.
- `MiniDVDStreamTranscoder` integration into the MiniClient push loop.
- Server-side DVD seeking that directly controls the DVD reader/VM.
- MPEG program-stream to transport-stream transformation for DVD MIM playback.
- Native fallback policy and menu/preview skipping inside the playback path.

The latest local revision also resolves the DVD transform executable through SageTV's existing transcoder resolver. That correctly honors an installed SageTV FFmpeg plugin, but it does not make the DVD pipeline itself a plugin.

### Caption state and new subtitle delivery — C1

The public SageTV API exposes stock closed-caption state calls, including:

- `GetMediaPlayerClosedCaptionState()`
- `SetMediaPlayerClosedCaptionState(String)`

That is sufficient for an MCP plugin to select an existing stock caption state. It is not sufficient to add a new client callback producer, deliver DVB bitmap subtitles, deliver decoded Teletext cues, or push a new `VIDEO_CC_STATE` protocol property. Those require agreed MiniClient/Core/client behavior.

### Core playback races and internal state — U1/U2 or C1, not P1

Changes inside `VideoFrame`, `MiniPlayer`, `MiniDVDPlayer`, `MiniClientSageRenderer`, or the DVD VM cannot be reproduced safely by a normal plugin when they alter:

- decoder locking or frame scheduling;
- STC/timestamp anchoring;
- stream URL construction;
- player-class selection;
- reconnect and redundant-watch state;
- media command parsing;
- raw MiniClient messages;
- DVD VM state or navigation packs.

Generic fixes in this group should be extracted for upstream. Vibe-only semantics should remain a small compatibility patch set.

## MCP control plugin evaluation

### Terminology and recommended architecture

A SageTV plugin is not natively an MCP server. The safest design is two cooperating components:

```text
MCP client / AI test tooling
          |
          | MCP
          v
External Vibe SageTV MCP adapter
          |
          | authenticated JSON/HTTP control API
          v
Vibe Control Bridge standard SageTV plugin
          |
          | supported SageTV API calls and UI contexts
          v
Unmodified stock sage.jar
```

Reasons to keep MCP outside the SageTV JVM:

- SageTV plugins already have a defined lifecycle; MCP transports and client sessions do not need to run inside that lifecycle.
- The server plugin can expose a small, auditable capability surface instead of arbitrary Sage expression execution.
- MCP libraries, JSON dependencies, and protocol updates do not have to be placed on SageTV's classpath.
- A crash or incompatibility in the MCP adapter cannot take down the SageTV server.
- The same adapter can fall back to Sagex or the legacy Web Interface when the bridge plugin is absent.

The phrase **MCP plugin** in this evaluation therefore means a stock-compatible SageTV Control Bridge accompanied by an external MCP adapter, not a replacement `sage.jar`.

### Controls available through the stock SageTV API — P1

The current Core source confirms that the stock API already exposes the important building blocks below. A plugin should invoke them in a selected UI context and return structured results.

| Proposed capability | Stock API foundation | Notes |
|---|---|---|
| List connected clients/UI contexts | `GetConnectedClients()`, `GetUIContextNames()` | Required before targeting a client. |
| Resolve an exact indexed media path | `GetMediaFiles()`, `GetFileForSegment()` | Compare canonical segment paths; reject files not indexed by SageTV. |
| Start exact media playback | `Watch(Object)` | Avoid STV text search and ambiguous titles. |
| Start from beginning | `Watch(Object)` then `Seek(...)` | Functional but not atomic; see P2 caveat below. |
| Play and pause | `Play()`, `Pause()`, `PlayPause()` | UI-context scoped. |
| Stop/close playback | `CloseAndWaitUntilClosed()` or a mapped UI command | The correct choice depends on whether STV navigation must also change. |
| Seek | `Seek(long)` | Time semantics differ for DVD content and ordinary MediaFiles. |
| Skip/scan | `SkipForward()`, `SkipForward2()`, `SkipBackwards()`, `SkipBackwards2()`, playback-rate APIs | Confirm player support before claiming success. |
| Tune a channel | `ChannelSet(String)` | Return the asynchronous watch result/failure rather than only HTTP success. |
| Query playback | `IsMediaPlayerFullyLoaded()`, `IsMediaPlayerLoading()`, `GetCurrentMediaFile()`, `GetMediaTime()`, `GetMediaDuration()`, `GetPlaybackRate()` | Suitable for deterministic commissioning gates. |
| Query seek window | `GetAvailableSeekingStart()`, `GetAvailableSeekingEnd()` | Important for growing/live recordings. |
| Select existing CC state | `GetMediaPlayerClosedCaptionState()`, `SetMediaPlayerClosedCaptionState(String)` | Does not add unsupported caption formats. |
| Control stock DVD operations | `DVDMenu()`, `DVDTitleMenu()`, `DVDReturn()`, chapter/title/audio/subpicture calls | Works only when the active stock player already supports the operation. |
| Send UI commands | SageTV event/UI APIs | Use a strict allowlist; do not expose arbitrary expression evaluation. |
| Run a media import scan | `RunLibraryImportScan(boolean)` | Suitable for commissioning fixtures. |
| Inspect server configuration and inventory | Public SageTV property, capture-device, EPG, plugin, and library APIs | Secrets must be redacted. |
| Collect diagnostics | Selected logs, version data, capabilities, current state | Bundle only allowlisted files and redact credentials. |

This is enough to replace the **test/commissioning purpose** of events 230–233 when an MCP tool is driving the SageTV server. It does not make those events disappear from the Android client if the client itself depends on them during normal use.

### Controls possible with weaker semantics — P2

| Behavior | Plugin implementation | Difference from current Vibe Core behavior |
|---|---|---|
| Exact watch from beginning | Resolve path, call `Watch`, wait for load, then `Seek` to the valid start. | Not one atomic MiniClient event; the STV may briefly expose resume state. |
| Force full screen | Send an allowlisted Full Screen command or invoke a known widget chain in the selected UI context. | STV-dependent; stock STV and SageMC can respond differently. |
| Same-channel reload/recovery | Close and re-watch, or retune the current channel. | Not equivalent to private `VideoFrame.reloadFile()` state handling. |
| Restart server | Ask SageTV to exit/restart and rely on Docker/systemd/Unraid supervision. | The plugin cannot guarantee the external supervisor policy. |
| Metadata recovery | Request an import scan or supported metadata refresh. | Cannot safely reproduce direct internal `MediaFile.reinitializeMetadata()` behavior through the public API. |
| DVD navigation | Call the existing DVD APIs. | Cannot add Android-native menus, a new DVD transport, or repair the DVD VM from a plugin. |

Every P2 response should state the actual operation performed. It must not report equivalence with the private Core path when the behavior is only an approximation.

### Changes that an ordinary plugin cannot replace

- Registering and parsing new raw MiniClient events 230–233 inside `MiniClientSageRenderer`.
- Adding MiniClient property negotiation or server-pushed properties.
- Adding media command 30 to `MiniPlayer`.
- Constructing a new `MEDIA_STATE_URL` inside the MiniClient media pipeline.
- Selecting a player implementation inside `VideoFrame`.
- Implementing native/hybrid/MIM DVD transport in the MiniClient push path.
- Driving the DVD reader or VM during server-side push seeking.
- Delivering Teletext or DVB bitmap cues over a new MiniClient callback.
- Fixing decoder locks, timestamps, reconnect races, redundant-watch behavior, or frame scheduling inside Core.
- Reliably intercepting those internals through reflection. Reflection would merely turn a source patch into an unsupported, version-fragile binary patch and is not recommended.

### Relationship to Sagex and the Web Interface

The external MCP adapter should use capability discovery rather than assume one server layout:

1. Prefer the Vibe Control Bridge when installed and compatible.
2. Fall back to Sagex Remote API for supported Sage API calls.
3. Fall back to the legacy Web Interface/Web Remote for a smaller, explicitly tested command set.
4. Report a capability as unavailable instead of silently using title search or an unrelated UI action.

Sagex can already expose many Sage API calls, which is why some server controls have previously worked without Core changes. A dedicated bridge is still useful because it can provide exact-path resolution, stable typed responses, transactional watch/wait/seek behavior, capability/version reporting, redaction, authentication, and diagnostics without exposing arbitrary Sage expression execution.

### Proposed bridge capability surface

These are conceptual capabilities, not committed endpoint names:

```text
server.capabilities
server.status
server.scan_library

ui.list
ui.state
ui.send_command

media.resolve_exact_path
media.watch
media.watch_from_beginning
media.seek
media.play
media.pause
media.stop

channel.current
channel.tune

captions.get_state
captions.set_state

diagnostics.collect
diagnostics.describe_environment
```

Each response should include the targeted server, UI context, operation ID, capability version, success/failure result, SageTV watch error when applicable, and observed postcondition.

### Compatibility and fallback rules

- Compile and test the bridge against an unmodified stock SageTV release, initially the commissioned 9.2.17 line.
- Use supported SageTV APIs and plugin lifecycle hooks only.
- When the plugin is absent, normal SageTV and MiniClient behavior remains unchanged.
- When MCP control is requested without the bridge, the adapter may use Sagex or Web Remote only for capabilities it can verify.
- Never substitute fuzzy title search for an exact-path request without explicitly reporting the fallback.
- Discover capabilities at runtime; do not infer them only from server version strings.
- Keep UI context/client identifiers explicit so commands never affect the wrong television.
- Preserve compatibility with legacy MiniClients, HD200/HD300 extenders, and the original Android MiniClient by avoiding changes to stock protocol behavior.

### Security requirements

The bridge controls active playback and exposes server state, so it must not be an unauthenticated general-purpose Sage API proxy.

- Disabled by default after installation until configured.
- Bind to loopback by default; require an explicit setting for LAN access.
- Authenticate with a generated high-entropy token or mutually authenticated reverse proxy.
- Never log tokens, SMB credentials, Web Interface credentials, or sensitive Sage properties.
- Restrict playback to SageTV-indexed MediaFiles; do not accept arbitrary executable or filesystem paths.
- Use an allowlist for UI commands and diagnostics.
- Do not expose arbitrary Sage expressions, Java reflection, class loading, or shell execution.
- Enforce request-size limits, timeouts, rate limits, and concurrency limits.
- Return structured operation IDs and maintain an audit log without private media credentials.
- Redact diagnostic bundles before export.
- Use TLS through a trusted reverse proxy when the endpoint is reachable outside a protected LAN.

## Implemented Core MCP bridge and measured result

The proposed split architecture is now implemented in the sibling project
`opensagetv-vibe-core-MCP-Plugin` as version 0.1.1:

```text
Android commissioning MCP
    -> bridge-first control adapter
    -> authenticated HTTP form/JSON endpoint
    -> SageTV Standard plugin
    -> sage.SageTV.api / sage.SageTV.apiUI
    -> unmodified stock SageTV 9.2.17.1056
```

The implementation deliberately does not expose arbitrary Sage API names,
expressions, reflection, filesystem access, class loading, or shell execution.
It binds to loopback by default, requires an explicit LAN opt-in, authenticates
with a bearer token, limits request bodies, allowlists commands/actions, and
accepts only exact paths already indexed as SageTV MediaFiles.

### Stock `.175` proof

- The plugin compiled with Java 8 language/API compatibility against the exact
  unmodified stock `.175` `Sage.jar`.
- Host source, Java contract, Python adapter, deterministic JSON, package, and
  no-bundled-`sage/` class gates passed.
- Version 0.1.1 installed through the normal SageTV plugin lifecycle and
  restarted successfully.
- The stock `Sage.jar` SHA-256 remained
  `d76ded981b9bc51e25b9cec821b6abeb771b46c2996dc45e453349b5e703fcb0`
  before and after installation.
- Authenticated capabilities, UI contexts/state/commands, exact path resolve,
  watch/from-beginning, media-relative seek, play/pause, channel tune, caption
  get/set, import scan, guarded watched-state clearing, and diagnostics passed.
- A cold full-library exact-path lookup measured approximately 7.1 seconds;
  the one-minute cache reduced a repeat lookup to approximately 16 ms. A
  library scan invalidates that cache.

### Physical Android proof

The Android commissioning MCP now discovers the bridge first when explicitly
configured, otherwise retaining the existing Sagex/Web and private-event
fallbacks. On non-Pro Fire TV `.25` against stock `.175`:

- exact fixture path resolved to MediaFile ID `65513439`;
- Media3 hardware Pull rendered advancing 1080i MPEG-2 video and AC-3 audio;
- from-beginning, fullscreen, forward/back recovery, and pause/resume passed;
- the crash-log gate was empty;
- stock live TV and bridge-driven exact channel `2.1` produced advancing A/V.

### Confirmed remaining gaps

- The public API exposes UI contexts reliably, but `GetConnectedClients()` may
  be empty for an otherwise active extender. Target selection therefore uses
  `GetUIContextNames()` as the authority.
- Exact-path lookup is intentionally exact-only. Human title search remains a
  Sagex/Web compatibility fallback rather than becoming a fuzzy bridge action.
- Watch then seek-from-beginning is observationally correct but not atomic.
- Fullscreen remains STV-dependent; the Android harness verifies the observed
  destination instead of treating command acceptance as proof.
- Native/hybrid/MIM DVD transport, media command 30, `MEDIA_STATE_URL`, new
  Teletext/DVB/CC payload delivery, decoder scheduling, reconnect/redundant
  watch corrections, and DVD VM behavior still require Core/client work.

### Core disposition

Private events 230-232 are now deprecated for automation and regression
testing. They remain temporarily as disabled-by-default compatibility fallbacks
so existing Vibe commissioning workflows are not broken before every Vibe
server has the plugin installed. Event 233 is also replaced for ordinary
stock-server seeks, but is retained until the server-owned DVD Push seek path
has an equivalent physical plugin gate. No stock or legacy client behavior is
changed when the plugin is absent.

## Complete Vibe Core change inventory

### Active Java/Core behavior

| File/area | Current Vibe change | Classification |
|---|---|---|
| `EPG.java` | Installed XMLTV importer discovery and invalid-property fallback. | U2 |
| `ImportedMediaMetadataRepair.java` | Narrow, opt-in repair of clearly invalid imported metadata before playback. | U2; not cleanly P1 |
| `MiniClientSageRenderer.java` | Events 230–233, exact-path/channel control, watch readiness/recovery, negotiated Vibe properties, current-channel acknowledgement. | P1 replacement for commissioning controls; C1 for wire behavior |
| `MiniDVDPlayer.java` | DVD control/lock/STC robustness plus MIM/hybrid stream behavior. | Extract generic U1/U2 fixes; C1 transport |
| `MiniDVDPlayerSelection.java` | Central native/hybrid/MIM selection and fallback policy. | C1 |
| `MiniDVDStreamTranscoder.java` | DVD PS-to-TS transformation and SageTV transcoder-path resolution. | C1 |
| `MiniPlayer.java` | Media command 30, playback-rate negotiation, media-state URL data, caption-state push. | C1 |
| `NetworkCaptureDevice.java` | Correct OpenDCT scan identity. | U1 |
| `NetworkEncoderManager.java` | Numeric source-address identity. | U1 |
| `SageTV.java` | Interface-safe discovery, shutdown completion, metadata-repair setting initialization. | Split U1/U2 |
| `VideoFrame.java` | Remote DVD policy, from-beginning behavior, DVD safety, metadata repair hook, watch/recovery behavior. | Split U1/U2/C1/P2 |
| `Wizard.java` | DVD volume/`VIDEO_TS` path normalization. | U1 |
| Ogle `VM.java` | Malformed PGC safety and main-feature selection heuristic. | U1 safety; U2 heuristic |

### Native and media-library behavior

- GCC 15 prototype and callback correctness across demux/capture/transcoder code — U1/U2 by subsystem.
- POSIX thread signature fixes — U1.
- 64-bit native-handle and pointer correctness — U1, with platform testing.
- JTux network, SysV IPC, iovec, and conversion corrections — U1/U2.
- Small retained-FFmpeg compiler compatibility fixes — U2 because SageTV retains a private FFmpeg ABI.
- System-codec ImageLoader conversion and tests — U2 as an independent dependency migration.
- JNI export verification and native smoke tests — U1 where portable.

### Build, packaging, tests, and repository behavior

- Ubuntu 26 development container and test orchestration.
- OpenJDK 11 build and server packaging workflow.
- Media utilities included in the Ubuntu build environment.
- Container-safe `startsagecore` behavior.
- Reproducible build-number restoration.
- Source-clean builds.
- Linux launcher LF enforcement.
- Unified build wrapper, update scripts, handoff archive script, release properties, and build reports.
- Tuner/network integration test.
- ImageLoader fixtures and tests.
- Vibe MiniClient/DVD/caption compatibility tests.
- Current GitHub Actions runtime and maintained-test guards.
- Vibe project identity, security/contribution documentation, third-party notices, and deployment safeguards.

Only portable build/test improvements should be offered upstream. Vibe branding, sibling-project orchestration, release procedures, and deployment policy remain V1.

### Historical asset removal

The large deletion count comes primarily from removal of:

- archived HD200/HD300/STX firmware binaries;
- legacy SageTV installers and packages;
- old `pubcode` toolchains and third-party source archives.

This is V1 repository hygiene. It must not be included in functional upstream pull requests. If upstream wants to reduce repository weight, that should be a separate maintainer decision with license and archival review.

## Commit ledger

The evaluated branch contains these 25 commits beyond the current upstream merge base:

| Commit | Summary | Primary disposition |
|---|---|---|
| `e8055f65` | Ubuntu 26 single-container workflow | Extract portable portions; otherwise V1 |
| `e17c7725` | GCC 15 and 64-bit native compatibility | U1/U2 split by subsystem |
| `bf466793` | System-codec ImageLoader and modern Linux tests | U2 |
| `42a804c9` | Package Ubuntu media utilities | V1/build-env policy |
| `b996c331` | Initialize project scaffold | V1 |
| `b6906b6f` | Connect scaffold to SageTV history | V1 |
| `b3c7eb50` | Establish Ubuntu 26 Core handoff | V1 documentation |
| `5e058eed` | Move historical assets out of active Core | V1 |
| `08c7dd39` | Container-safe server launcher | U2 after service-mode review |
| `41970a15` | Network encoder identity and discovery | U1 |
| `c4b9caa8` | Discover installed XMLTV importer | U2 |
| `50ce5708` | Delegate Core builds to unified container | V1 |
| `303e0b5e` | Adopt OpenSageTV Vibe Core identity | V1 |
| `1854fb09` | Reproducible build-number restoration | U1/U2 |
| `9f4270bc` | Source-clean full Core builds | U1/U2 |
| `4c593710` | Keep launchers LF in Windows clones | U1 |
| `d5a794ae` | Negotiated Vibe MiniClient and disc controls | Split generic U1/U2 fixes from C1 features |
| `e7a98f8d` | Unified Core validation workflow | Mostly V1; tests may be upstreamable |
| `f2d9c92f` | Prepare Vibe Core fork for public development | V1; selected safeguards may inspire upstream changes |
| `172914be` | Activate Core checks on `main` | V1/U2 CI pattern |
| `4b4e6f28` | Resume stopped Vibe watch requests | Evaluate generic race separately; protocol behavior C1 |
| `96610439` | Opt-in invalid imported-metadata repair | U2 |
| `6d1635bd` | Update repository Action runtime | U1 conceptually |
| `26f7849e` | Preserve maintained Linux test execution | U1 conceptually |
| `9f4484ca` | Route DVD MIM through transcoder resolver | C1 class; resolver use is correct integration behavior |

## Proposed upstream pull-request boundaries

The following boundaries minimize review risk. They are recommendations, not started tasks.

1. **Network encoder and discovery correctness** — scan identity, numeric source identity, and interface-safe discovery, with network tests.
2. **DVD VM safety** — PGC validation, negative-seek clamp, failed-control cleanup, and malformed-disc tests.
3. **DVD path normalization** — volume/`VIDEO_TS` normalization and unit tests.
4. **DVD timing/control correctness** — STC re-anchor, control-lock yielding, authored stream-selection preservation, each with dedicated tests.
5. **Native GCC/64-bit corrections** — split further by capture, demux, JTux, JNI, and transcoder ownership if requested.
6. **ImageLoader system-codec migration** — dependency decision, implementation, and fixture tests together.
7. **Build reproducibility and CI guards** — only upstream-neutral portions.
8. **XMLTV importer discovery** — only after policy agreement.
9. **Imported metadata repair** — only after the remaining physical-validation gate.
10. **MiniClient Vibe extension proposal** — a protocol specification and compatibility discussion, not a hidden dependency of the bug-fix pull requests.

Do not open an upstream request from the Vibe `main` branch. Create clean topic branches from current upstream `master` and cherry-pick or reimplement only the relevant hunks and tests. This avoids including branding, archive deletion, build-environment coupling, or unrelated protocol work.

## Minimal-`sage.jar` target

If the objective is to run as close to stock SageTV as possible, the evaluated end state is:

1. Upstream the generic network, DVD safety, native, and build fixes.
2. Use the implemented Control Bridge plus MCP adapter instead of events
   230-233 for **automation and regression testing**; retain event 233 only for
   the unproven server-owned DVD Push seek boundary.
3. Keep stock Sagex/Web Remote fallbacks for older installations.
4. Retain only the irreducible MiniClient/DVD/caption/player-pipeline patch set in Vibe Core.
5. Propose that remaining patch set as a documented optional protocol extension rather than silently altering the stock protocol.

This approach preserves old clients when the plugin is absent, lets stock `sage.jar` perform most commissioning controls, and makes the unavoidable Core delta small enough to review and maintain.

## Evaluation status

- The repository and all project Markdown guidance were reviewed.
- The complete 25-commit divergence from current upstream was inspected.
- Active Java, native, build, repository, and test changes were classified.
- Stock SageTV API support was verified in source and through the commissioned
  version 0.1.1 plugin on stock `.175`.
- The sibling plugin project and Android bridge-first adapter are implemented
  and tested locally. No upstream branch, pull request, GitHub publication, or
  SageTV plugin-catalog publication was started.
