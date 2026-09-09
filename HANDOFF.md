# Core handoff

## Pending opt-in imported-metadata and shutdown gate

Valid imported MKVs physically pass Media3 and legacy Exo hardware Pull against
unmodified stock server `.175`; stock remains the primary compatibility path.
The isolated `.232` database separately contains one-millisecond/zero-stream
rows that make its STV clamp timeline seeks before the Android player sees
them. A narrowly bounded Core repair now exists behind disabled-by-default
property `videoframe/repair_invalid_imported_metadata_on_playback`. It only
reparses a completed, imported, single local regular video with corroborating
invalid metadata and excludes recordings, live streams, discs, pictures,
music, remote inputs, and valid rows. `SageTV.exit()` also guarantees final JVM
termination if a shutdown-time linkage/plugin failure interrupts cleanup.

Focused tests and the complete Ubuntu 26 build/native/package/server-smoke gate
pass. The staged isolated-server `Sage.jar` SHA-256 is
`0ad22a25a03e2d1cb3a3ddb16eea84691e9f15575be77d9027ad93f8604da419`;
the immediately prior build is recoverable from
`.component-backups/core-full-gate-20260909-1255`. The `.232` container
is currently stopped/unreachable and must receive a normal restart before the
opt-in Lion King metadata, repeated seek, thumbnail, and shutdown gates can be
commissioned. Stock `.175` was not modified.

## Pending physical Vibe redundant-watch gate

The opt-in Vibe exact-file event now resumes the current MediaFile when the
request is redundant but its player is stopped or paused. This addresses
alternate STVs such as SageMC that retain a stopped MiniPlayer session; normal
SageTV watch behavior and already-playing requests are unchanged. Focused unit
coverage and the complete Core test/build gates pass. The resulting
`build/release/Sage.jar` SHA-256 is
`d5a929688f681f5b337c0896d0d6d2b9327675afbf73657ebe38cb4518aeccf4`.

That JAR is staged only on isolated `.232`, with rollback copy
`Sage.jar.pre-redundant-watch-20260908-051446` and an intermediate staged-JAR
copy `Sage.jar.pre-watch-unit-helper-20260908`. The server JVM still needs a
restart before the physical repeated-playback gate can run; stock `.175` was
not modified.

## Repository identity

- GitHub repository: `opensagetv-vibe/opensagetv-vibe-core`
- Fork parent: `OpenSageTV/sagetv`
- Historical source lineage: `google/sagetv`
- Read-only repository CI uses the current `actions/checkout@v7`; it does not
  build, deploy, publish, or modify external systems.
- Vibe default/development branch: `main`
- `origin`: writable Vibe fork
- `upstream`: read-only original OpenSageTV repository

The fork exists publicly with inherited upstream history retained on `master`
and reviewed Vibe work on the default `main` branch. Complete source, Docker,
test, package, and independent-checkout gates passed before `main` was
published. Do not push to the original OpenSageTV repository and do not invoke
the inherited legacy deployment script from CI.

## Standard takeover

Read `AGENTS.md`, `README.md`, `TASKS.md`, and `WORKFLOW.md`, then use the root
`dev`, `update`, and handoff-package commands. Packages are exchanged only
through `artifacts/downloads`; Core installation into an image belongs to the
container repository.

## Verified baseline

- Base: `ubuntu:26.04` (`resolute`)
- Architecture: Linux amd64
- Java: Ubuntu OpenJDK 11
- Unified builder tag used locally: `opensagetv-vibe-build-env:u26-j11`
- Reusable development container: `opensagetv-vibe-dev`
- Verification: `./sagetv-dev.sh all`

The latest clean run completed Java tests, all native builds, dependency/JNI checks, PNG format and symbol-preemption regressions, malformed-PNG containment, packaging, server startup, and repeated shutdown. The generated report is `output/BUILD_REPORT.md`.

The 2026-09-05 publication audit also passed the live OpenDCT protocol-3.0
`AUTOINFOSCAN` integration against the commissioned HDHomeRun-backed endpoint,
which returned real channel rows. Core now runs that test strictly whenever
`OPENDCT_TEST_HOST`, `OPENDCT_TEST_PORT`, and `OPENDCT_TEST_ENCODER` are supplied;
without all three it records the physical endpoint as `SKIPPED`. Server smoke
testing also verifies the UDP MiniClient discovery reply and TCP service port.

The clean post-DISC-policy build produced `build/release/Sage.jar` SHA-256
`fe4a97f265a942e0019e457b1588b8581c9f77d7394ac2909aaa8116431cb33a`;
its `output/BUILD_REPORT.md` records PASS for Java/tests, every required native
library, JNI/ELF, system-libpng PNG regressions, malformed-input containment,
server startup, and repeated shutdown.

Core now negotiates `VIBE_PLAYBACK_RATE` after the existing DISC properties.
Only a non-empty client reply enables MiniPlayer command 30 for Pull Smooth
FF/REW; older clients keep the historical one-shot seek behavior. The focused
compatibility test and `sageJar` task pass. The commissioned Vibe test server
currently runs `Sage.jar` SHA-256
`32563ce0ae9e174b1212a9c7fa0bb4255483bc81410276ab78e3e2d311683c48`.

Core now makes the SageTV STV caption state authoritative for supporting Vibe
MiniClients. `MiniPlayer.setClosedCaptioningState()` retains the state and sends
`VIDEO_CC_STATE`; Android maps Off/On to decoder track state. Both Exo backends
passed physical caption rendering with the Android debug selector unused.

Core now selects its existing Java/Ogle `MiniDVDPlayer` for a remote client
that negotiates `DVD_REMOTE_NAV`, without changing Windows local
`DShowDVDPlayer`. `Wizard` normalizes a disc root and `VIDEO_TS` consistently,
and Vibe's explicit from-beginning event suppresses the saved resume only for
the matching disc load. Focused tests and real authored/menu-less Fire TV
commissioning pass. The commissioned share contains no BDMV fixture, so
Blu-ray physical validation is SKIPPED and must not be represented as passing.
DVD/Blu-ray seek time is now clamped to zero before entering the Java/Ogle VM;
this fixes remote Skip Back near title start passing a negative sector and
stalling after FLUSH. The focused Core test and the complete physical Fire TV
transport sequence pass. The isolated Unraid test server currently runs the
rebuilt `Sage.jar` SHA-256
`3ac9592dcfbf10cd65a72f91334ea663a979afcb153d1b1f8deaab2829b818bc`.
It also negotiates client DISC policy, native fallback, skip menus, and skip
previews; skip menus selects the longest authored VM title. The preceding JARs
remain available as `Sage.jar.backup-before-disc-seek-20260901` and
`Sage.jar.backup-before-disc-fail-closed-20260901`.

Core also contains opt-in MiniClient events 230 and 232 for exact indexed-file playback
during hardware-in-loop testing. It is gated by
`miniclient/enable_vibe_watch_file_event`, rejects invalid/NUL/oversized and
unindexed paths, calls `VideoFrame.watch()` only in the requesting UI context,
and enters `MediaPlayer OSD` directly on the UI event thread. Event 232 queues
the first media-segment time behind `Watch` for deterministic from-beginning
tests. Direct menu entry avoids a double-`TV` toggle with MCP fullscreen
verification. A clean Core build and real Fire TV Fixed MPEG-2/AC-3 test passed
on 2026-08-30; deployed JAR SHA-256 is
`929c1f57c48d532e849769a604c73613dd8e232b5171e3277f411a80b28764de`.
Keep the property false outside explicitly commissioned test servers. The
event also waits up to 15 seconds for the new UI's VideoFrame worker during
reconnect, preventing the observed null-Seeker race before `watch()`.

Core also has opt-in exact-channel event 231, gated by
`miniclient/enable_vibe_channel_set_event`. It accepts only bounded dotted
channel numbers and calls `surfToChan()` in the requesting UI. Negotiated media
URLs carry `channel=` identity for supporting clients. On 2026-08-29 the
isolated Unraid server and Amazon AFTMM passed 10 alternating 2.1/5.1 Pull
changes on Media3 and legacy ExoPlayer, plus bounded tests of all four GSY
engines. Both test-control properties must remain false outside commissioned
debug servers.
An event 231 request for the already-current channel reloads the live file so a
newly connected client does not inherit an expired Fixed stream.

Core's `sagetv-dev.sh` delegates to the sibling build-environment wrapper. Keep
`opensagetv-vibe-dev` as the only development container; do not reintroduce
phase-specific or Core-only containers.

The `.gitattributes` LF rules for `build/serverfiles/*`, Debian maintainer
scripts, and extensionless third-party build helpers are runtime/build
requirements. In particular, a Windows fresh clone otherwise packages a CRLF
`startsagecore`; Linux then reports the existing launcher as `No such file or
directory` because its shebang names `bash\r`.

## Important decisions

`libImageLoader.so` intentionally links Ubuntu's `libpng16.so.16`; it does not export an embedded `png_*` implementation. `LoadPNG` normalizes transformed input before inspecting channel/row sizes and contains libpng errors with `setjmp` cleanup so corrupt images cannot abort the JVM.

The old minimal FFmpeg libraries remain only for `libMpeg2Transcoder.so` because that JNI code consumes the historical private ABI. Modern FFmpeg/MIM work is owned by `opensagetv-vibe-ffmpeg-mim` and is not silently substituted here.

Core treats an installed `xmltv.XMLTVImportPlugin` as the default external EPG
provider when `epg/epg_import_plugin` is empty. If that property names an old or
invalid importer while the XMLTV class is available, Core falls back to XMLTV
and repairs the property. This prevents setup from entering the retired
license-key EPG service. Container-level missing/invalid-property integration
tests live in `opensagetv-vibe-container/tests/core-xmltv-autodiscovery.sh` because
the optional plugin JAR is intentionally not bundled in Core itself.

Windows hosts must preserve LF endings for executable build files. `.gitattributes` explicitly covers generated/configure entry points, and a repository-local `core.autocrlf=false` is recommended.

## Next maintainer checks

Run the clean command after changing native code, inspect `output/test-results/elf`, and update both the Ubuntu audit and changelog when dependencies or exports change. Do not commit `output/`, Gradle state, credentials, appdata, recordings, or a user `Sage.properties`/`Wiz.bin`.
