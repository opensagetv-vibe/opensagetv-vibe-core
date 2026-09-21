# Core handoff

## Functional MiniClient capability names (2026-09-20)

Core now queries `DVD_DISC_TRANSPORTS`, `DVD_DISC_POLICY`,
`DVD_DISC_SKIP_MENUS`, `DVD_DISC_SKIP_PREVIEWS`,
`DVD_DISC_NATIVE_FALLBACK`, and `VIDEO_PLAYBACK_RATE`. The previous
Vibe-branded property names are not retained as wire aliases. These optional
extensions therefore require a matched updated Core and Android client;
unmodified stock clients and servers retain their established fallback paths.

Focused compatibility tests and the complete Ubuntu 26 Java/native/JNI/
ImageLoader/package/server gate pass. The rebuilt `Sage.jar` SHA-256 is
`07e727cc4467bef1dd3c08b41f94944bccd61f8c0a4750e01d684614994b002f`.
It has not been commissioned to a server by this change.

## Stock-compatible Core MCP bridge closure (2026-09-20)

The sibling `opensagetv-vibe-core-MCP-Plugin` project now implements the
commissioning controls that can be expressed through stock SageTV APIs. Version
0.1.1 passed its clean build/contracts, direct authenticated control suite, and
the non-Pro Fire TV exact-path playback/live-TV gates against stock `.175`.
The `.175` `Sage.jar` remained byte-identical with SHA-256
`d76ded981b9bc51e25b9cec821b6abeb771b46c2996dc45e453349b5e703fcb0`.

Core private commissioning events 230-232 and their Android emitters have been
removed after exact indexed-file `Watch`, from-beginning `Watch` plus `Seek`,
and dotted-channel `ChannelSet` passed through the stock-compatible plugin on
`.175`. Public `Seek(long)` also physically repositioned ALADDIN DVD Push from
621,386 ms to 240,000 ms immediately and continued normally; repeated stable
targets passed in both directions. Event 233 is also removed: Android now
refreshes its local Media3 video Surface without replacing or seeking the
server-owned DVD stream. Events 230-233 no longer exist in the Vibe Core
MiniClient receiver. Public `Seek(long)` remains the validated path for actual
DVD repositioning.
The complete clean Java/native/JNI/ImageLoader/package/server gate passes after
that removal. The current `Sage.jar` SHA-256 is
`a72e371b7eab4313148d3983317f40835f8ab37ee9972bf1ed18da4626a7f8f9`.
That exact JAR is running in the rebuilt isolated `.232` Vibe container, which
is healthy and also loads the separately packaged Core MCP plugin. Stock
`.175` remains unchanged.
The plugin cannot replace native/hybrid/MIM DVD transport, media command 30,
`MEDIA_STATE_URL`, new caption payloads, decoder scheduling, reconnect fixes,
or DVD VM behavior. See `docs/UPSTREAM_EVALUATION.md` for the upstream-only
change assessment.

## DVD MIM plugin-resolver commissioning (2026-09-20)

`MiniDVDStreamTranscoder` now resolves its executable through the same stock
`FFMPEGTranscoder.getTranscoderPath()` path used by ordinary SageTV
transcoding. This fixes an integration defect where negotiated DVD MIM bypassed
the installed `SageTVTranscoder` plugin bridge and tried stock `ffmpeg`
directly. Focused coverage proves `SageTVTranscoder` wins when present, and the
complete clean Core Java/native/JNI/package/server gate passes.

The resulting `Sage.jar` SHA-256 is
`8d146702c1a8d5361c815ae925b9bf9a3a768ee0414e1011c7a198a419e741cb`.
It is installed only on isolated `.232`; its recoverable pre-install backup is
`/mnt/user/appdata/sagetv-vibe-server-u26-gpu-j11/.component-backups/core-20260920-183549`.
Stock `.175` was not changed, and `.232`'s stock `ffmpeg` remained
byte-identical with SHA-256
`bdf6aabffdba7411edff8d36c389d695257fcdf823d196020176e117612862f6`.

Non-Pro Fire TV `.25` physically passed the generated authored DVD through
explicit MIM main-feature policy: VAAPI `h264_vaapi` server encoding, Android
hardware AVC decoding, 1.002x cadence, zero dropped frames, and recovery after
pause/play, FF, REW, and chapter-up. This optional DVD integration requires the
updated Vibe Core because unmodified stock Core has no DVD-to-plugin transform
hook; ordinary prerecorded/live plugin transcoding remains stock-Sage.jar
compatible.

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

## Upstream review staging

Twelve focused `upstream-review/*` branches are published on the Vibe fork.
They cover the Linux launcher, network encoder discovery, native GCC/64-bit
compatibility, source-clean builds, Ubuntu 26/ImageLoader modernization,
shutdown hardening, imported metadata repair, four independent DVD correctness
topics, and the optional MiniClient capability protocol. Exact branch names,
commits, validation, dependencies, and exclusions are recorded in
`docs/UPSTREAM_EVALUATION.md`.

No pull request has been opened against `OpenSageTV/sagetv`. The review branches
must not be merged as one omnibus change. Generic fixes are independent;
protocol, timing, metadata, and DVD behavior retain their explicit validation
requirements. DVD MIM remains Vibe-only until its capability and external
FFmpeg provider can be reviewed as a complete stacked contract.

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

Core now negotiates `VIDEO_PLAYBACK_RATE` after the existing DVD DISC properties.
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

Core no longer contains private MiniClient commissioning events 230–233 or
their enablement properties. Exact indexed-file playback, from-beginning
playback, channel selection, and explicit seek use supported SageTV APIs.
Android display-mode recovery is local to the client and does not seek or
replace the server-owned stream.

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
