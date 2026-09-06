# OpenSageTV Vibe Core tasks

This is the only active Core backlog. Completed work is removed and recorded
in `CHANGELOG.md` and `HANDOFF.md`.

- [ ] Address important remaining compiler/Gradle warnings in narrowly scoped,
  tested changes without enabling global `-Werror`.
- [ ] Prepare an upstream-review branch, tag, release notes, and published
  artifact after all local release gates pass.
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
