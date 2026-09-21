# OpenSageTV Vibe Core tasks

This is the only active Core backlog. Completed work is removed and recorded
in `CHANGELOG.md` and `HANDOFF.md`.

- [ ] Prove the opt-in repair for clearly invalid completed-import metadata on
  isolated server `.232`; it remains disabled by default because valid MKVs
  already play and seek through unmodified stock SageTV. When explicitly
  enabled, repair on first playback and prove `The Lion King.mkv` reports its
  real duration and obeys remote timeline seeks. The current database row is
  1 ms/zero-stream while FFmpeg and Android both detect 1:58:14.588; exclude
  recordings, live streams, and discs. Also
  prove a shutdown-time linkage failure cannot leave the server alive after
  its MiniClient and MediaServer listeners have already closed.
- [ ] Restart isolated test server `.232`, physically prove repeated exact-file
  playback after STOP through SageMC, then publish the opt-in Vibe
  redundant-watch correction. The full Core build and focused unit gate pass;
  stock `.175` remains untouched.
- [ ] Address important remaining compiler/Gradle warnings in narrowly scoped,
  tested changes without enabling global `-Werror`.
- [ ] Track review of OpenSageTV PRs #1-#6 and collect the physical,
  cross-platform, or design evidence required for the remaining published
  `upstream-review/*` branches. Do not submit validation-dependent or protocol
  branches without separate readiness and approval.
- [ ] Replace Core's temporary automatic XMLTV importer discovery/property
  repair with a stock-compatible SageTV Standard plugin migration, then remove
  the temporary Core policy after existing installations have a tested upgrade
  path.
- [x] Stage the provider-neutral DVD transform SPI as the explicit stacked
  `upstream-review/dvd-transform-provider` topic. The external FFmpeg/MIM
  provider remains in its plugin repository; opening an upstream pull request
  still waits for explicit approval and `DVD_DISC_*` protocol review.
- [ ] Create `docs/CLIENT_MODERNIZATION_AUDIT.md` covering the Java
  MiniClient/PlaceShifter, native Linux MiniClient, Windows client,
  launchers/installers, renderer/player backends, protocols, dependencies, and
  supported-versus-archival decisions.
- [ ] Add reproducible Java MiniClient and Ubuntu 26 Linux PlaceShifter
  build/package gates, including Java 11 and virtual-display smoke tests.
- [ ] Characterize the Java MiniClient protocol/UI on Java 11 before removing
  obsolete Java compatibility branches.
- [ ] Replace unpinned or bundled Linux client inputs and unsafe launcher
  assumptions while retaining growing/circular-file behavior.
- [ ] Add bounded child-process, thread, reconnect, renderer teardown, and JVM
  shutdown tests for desktop clients.
- [ ] Inventory and decide support for Linux OpenGL/X11, Windows DirectX 9,
  Java2D, and historical Quartz renderers.
- [ ] Decide whether `native/elf/newminiclient` is supported; add compiler,
  sanitizer, malformed-command, reconnect, and shutdown gates if retained.
- [ ] Establish a current-Windows-SDK client build and make x86/x64 support an
  explicit release decision.
- [ ] Audit PlaceShifter locator, authentication, UPnP exposure, secrets, and
  remote-access security without breaking the existing wire protocol.
- [ ] Add shared malformed-input/protocol conformance tests and a cross-client
  media corpus covering completed, growing, circular, malformed, captioned,
  multilingual, and slow-network cases.
