# OpenSageTV Vibe Core tasks

> **Pre-commit task maintenance:** Immediately before every repository commit, move
> completed `[x]` items out of active sections and into
> `## Checklist change ledger`. Preserve IDs, evidence, and context; never
> discard completion history. Active sections contain unchecked work only.

This is the only active Core backlog. Completed work moves to the checklist
change ledger; release evidence is also recorded in `CHANGELOG.md` and
`HANDOFF.md`.

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
- [ ] Track the maintenance-triaged canonical Core proposals individually.
  Only #519 (source-clean build state) remains ready for review. #516-#517 and
  #525-#529, including #526, remain drafts
  pending focused container evidence, reproduction, protocol/design approval,
  physical DVD evidence, or a split native/platform matrix. #518, #520-#524
  are closed because the audit found behavior already present
  upstream, no reproduced user failure, an incomplete physical gate, or
  Vibe-specific policy. All current open PRs pass `check-changes` and
  `cla/google`; do not promote or recreate a closed/draft topic without the
  maintenance evidence required by `WORKFLOW.md`.
- [ ] Make the canonical/current-Ubuntu server and Vibe container path the top
  priority. Add a focused launcher regression harness for #516 covering root
  and non-root PID paths, optional `activkey`, environment overrides, and
  `sagesettings` precedence; then supply the evidence needed to promote the
  corrected container-configurable launcher proposal.
- [ ] Keep #528-#529 active as canonical SageTV modernization proposals. Split
  the GCC/64-bit, ImageLoader/native-library, and reproducible Ubuntu build
  work into the smallest reviewable changes while retaining the demonstrated
  Ubuntu 26 build/runtime goal needed for current GPU driver stacks. Draft
  status is a scope/evidence gate, not a decision to keep this work Vibe-only.
- [ ] Replace Core's temporary automatic XMLTV importer discovery/property
  repair with a stock-compatible SageTV Standard plugin migration, then remove
  the temporary Core policy after existing installations have a tested upgrade
  path.
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

## Checklist change ledger

- 2026-10-08 pre-commit source-sync review: only task-fix/stock-server workflow
  policy changes; runtime Core source is unchanged. Completed entries remain
  in this ledger and upstream/runtime acceptance stays open. Root order305
  prioritizes the approved Android release; no native build/matrix repeat.


### Archived completed checklist items (2026-09-30)

These completed items were moved from active task sections immediately
before commit. Stable IDs, acceptance evidence, and source context are
preserved; active sections contain unchecked work only.

#### From `# OpenSageTV Vibe Core tasks`

- [x] Stage and submit the provider-neutral DVD transform SPI as the explicit
  stacked `sagetv-review/dvd-transform-provider` draft topic. The external
  FFmpeg/MIM provider remains in its plugin repository; canonical PR #527 still
  requires `DVD_DISC_*` protocol/design review before promotion.
