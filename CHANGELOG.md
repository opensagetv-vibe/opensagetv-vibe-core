# Change Log

## Unreleased

- Prepared and published thirteen focused `upstream-review/*` branches from the
  current OpenSageTV merge base. Generic Linux, network, native, build,
  ImageLoader, shutdown, metadata, and DVD fixes are isolated from the optional
  MiniClient capability proposal. No upstream pull request was opened. DVD MIM,
  Vibe CI/release tooling, commissioning controls, and container policy remain
  explicitly excluded until their separate contracts are appropriate for
  upstream review.

- Replaced the Vibe-branded DVD and playback-rate capability properties with
  functional protocol names: `DVD_DISC_TRANSPORTS`, `DVD_DISC_POLICY`,
  `DVD_DISC_SKIP_MENUS`, `DVD_DISC_SKIP_PREVIEWS`,
  `DVD_DISC_NATIVE_FALLBACK`, and `VIDEO_PLAYBACK_RATE`. No deprecated
  `VIBE_*` wire alias is retained; these optional extensions require a matched
  updated Core and Android client while stock-server fallback remains intact.
- Removed automatic XMLTV importer discovery/property repair from the proposed
  upstream Core change set. The existing behavior is now explicitly classified
  as temporary Vibe-only compatibility pending replacement by a
  stock-compatible SageTV Standard plugin.
- Added the measured Core/upstream/plugin evaluation for the stock-compatible
  Vibe Core MCP bridge. Stock `.175` retained its byte-identical `Sage.jar`
  while the separate plugin and Android adapter passed exact-path playback,
  seek, channel, caption, scan, watched-state, diagnostics, and physical
  non-Pro playback/live-TV gates. Removed private commissioning events 230-232
  and their Core handlers after the public `Watch`, `Seek`, and `ChannelSet`
  replacements passed on stock `.175`. Event 233 was also removed after the
  Android client replaced its DVD decoder-reload seek handshake with a local
  Media3 Surface refresh that leaves the server stream untouched. Public
  `Seek(long)` remains the tested automation and user-seek path; no private
  commissioning event remains in Core. The post-removal clean Java/native/JNI/
  ImageLoader/package/server gate passes; the resulting JAR is commissioned
  only on isolated `.232`, with stock `.175` unchanged.
- Routed `MiniDVDStreamTranscoder` through SageTV's existing
  `FFMPEGTranscoder.getTranscoderPath()` resolver instead of opening the stock
  `ffmpeg` path directly. This preserves SageTV's established
  `SageTVTranscoder`-first behavior, allowing the optional Vibe FFmpeg plugin
  bridge to serve negotiated DVD MIM main-feature playback without replacing
  stock `ffmpeg`. A focused regression proves bridge precedence, and the
  complete Core Java/native/JNI/package/server gate passes. The updated Core
  was commissioned only on isolated `.232`; stock `.175` was not modified.
- Restored executable Git metadata for the maintained Ubuntu/Linux Core test
  suite and added a repository-CI guard so direct Linux execution cannot regress.
- Updated the read-only repository workflow to the current Node 24-based
  `actions/checkout@v7` release, eliminating the obsolete-action warning while
  preserving the source-only, non-publishing CI boundary.
- Added a disabled-by-default playback repair for completed imported videos
  whose persisted database metadata is the historical one-millisecond,
  zero-stream failure state. The opt-in path reparses only a single local
  regular video file and excludes recordings, live streams, discs, pictures,
  music, remote inputs, and valid metadata. Valid MKVs continue to use normal
  stock-compatible playback without any repair or server requirement.
- Guaranteed headless JVM termination from `SageTV.exit()` when shutdown has
  already started and a later linkage/plugin error interrupts normal cleanup.
  This lets the container supervisor recover instead of leaving closed
  MiniClient/MediaServer listeners attached to a stranded JVM.
  The complete Ubuntu 26 gate passes Java/tests, all native libraries,
  JNI/ELF validation, PNG and malformed-image tests, packaging, server startup,
  MiniClient UDP/TCP service checks, and repeated shutdown.
- Made the opt-in Vibe exact-file commissioning event resume a stopped or
  paused redundant watch request for the same MediaFile. Stock watch behavior
  is unchanged: the correction is confined to the property-gated Vibe event,
  avoids issuing an extra play for an already active file, and has focused
  compatibility coverage.

- Standardized the source-only GitHub repository check as
  `.github/workflows/repository-checks.yml` and limited automatic branch
  triggers to the published Vibe `main` branch. The workflow validates policy,
  shell syntax, and absence of generated/private state; it never deploys or
  publishes artifacts.
- Preserved this repository as the `opensagetv-vibe-core` GitHub fork of
  `OpenSageTV/sagetv`, documented upstream/release boundaries, indexed bundled
  third-party licenses, and replaced the inherited Ubuntu 22/legacy deployment
  workflow with read-only repository checks. GitHub branch builds can no longer
  publish to or tag the historical project.
- Disabled the inherited `build/deploy.sh` publisher and redirected dormant
  manual Gradle release metadata and Debian source links to this fork, removing
  active publication targets for the historical repositories.
- Made the bundled x264 version helper tolerate the standard fork remote layout
  (`origin` for the fork and `upstream` for the parent) instead of printing a
  fatal `origin/master` lookup during otherwise successful clean builds.
- Added strict server-network gates for the MiniClient UDP discovery response,
  TCP service port, SageTV/OpenDCT `AUTOINFOSCAN` source contract, and an
  optional commissioned OpenDCT endpoint. On 2026-09-05 the commissioned
  protocol-3.0 endpoint returned real channel data across all queried scan
  indexes.
- Added opt-in MiniClient playback-rate negotiation through
  `VIDEO_PLAYBACK_RATE` and media command 30. Supporting Pull clients receive
  SageTV's existing Smooth FF/REW rate sequence; clients that omit or reject
  the property retain the established one-shot seek fallback. Empty and
  whitespace-only replies fail closed, and focused compatibility tests cover
  capability parsing.
- Added per-client DISC policy negotiation for Native, Hybrid, and MIM main
  feature, including skip-menu, skip-preview, and native-fallback preferences.
  Unavailable explicit Hybrid/MIM requests now fail closed when fallback is
  disabled instead of entering a partial MiniDVDPlayer session.
- Added deterministic DVD main-feature selection to the Java/Ogle VM by
  measuring the first referenced PGC duration for each authored title. Skip
  menus starts that longest title; skip previews remains a separate root-menu
  operation.

- Added negotiated remote DVD selection for clients advertising
  `DVD_REMOTE_NAV`, while preserving Windows local `DShowDVDPlayer` and legacy
  client behavior. Disc-root/`VIDEO_TS` normalization and focused tests cover
  authored and menu-less directory inputs. The Vibe from-beginning event now
  suppresses a saved resume only for its matching DVD/Blu-ray load, leaving
  ordinary STV resume unchanged. Real Amazon Fire TV commissioning passed the
  existing Java/Ogle `MiniDVDPlayer` path for authored and menu-less fixtures.
- Clamp DVD/Blu-ray seek time to zero before it reaches `MiniDVDPlayer` and the
  Java/Ogle VM. A Skip Back interval larger than the elapsed title time
  previously produced a negative PTS/sector, flushed the remote decoder, and
  left the session without replacement media. Added a focused TestNG regression;
  the exact Fire TV `ff_2`/`rew_2`, FF/PLAY, REW/PLAY, and chapter sequence now
  passes strict audio/video output recovery.

- Made the SageTV STV the authoritative closed-caption controller for Vibe
  MiniClients. `MiniPlayer` now retains its current CC state and forwards it as
  `VIDEO_CC_STATE` after media load and whenever the STV changes it. Unknown
  properties remain safely rejected by older MiniClients. Added a focused
  TestNG state regression; Media3 and legacy ExoPlayer physically rendered
  captions without using Android's debug subtitle selector.
- Extended the negotiated Vibe media-state URL with authoritative channel
  identity. Android hardware-in-loop tests can now distinguish an already
  active healthy channel from a failed switch without changing URLs sent to
  older MiniClients.
- Added opt-in MiniClient exact-channel event `231`, guarded by
  `miniclient/enable_vibe_channel_set_event=false`. It validates bounded dotted
  channel numbers and tunes only the requesting UI context. Physical 2.1/5.1
  stress tests passed across Media3, legacy ExoPlayer, and four GSY engines.
- Reload the current live file when exact-channel event 231 requests the
  already-selected channel. This gives a newly reconnected MiniClient a fresh
  Fixed stream instead of acknowledging an expired prior stream.
- Added an opt-in Vibe MiniClient exact-path event (`230`) for deterministic
  hardware-in-loop tests. It validates and resolves only indexed playable
  `MediaFile` paths and starts playback in the requesting UI context. Event
  `232` uses the same bounded path contract and queues the first segment time
  directly behind `Watch`, preventing resume-near-EOF test starts. Both enter
  the canonical `MediaPlayer OSD` on the UI event thread; using the toggle-like
  `TV` event here caused an MCP verification race that returned playback to
  the Main Menu preview. The feature defaults off through
  `miniclient/enable_vibe_watch_file_event=false`.
- Made that event wait for the newly connected UI's VideoFrame worker before
  calling `watch()`. This removes a reconnect race that could dereference an
  uninitialized Seeker and leave an accepted request on Main Menu.
- Added the common AI takeover, task, changed-files update, resumable gate, and
  handoff ZIP workflow while retaining the existing unified Core build.

## Ubuntu 26 modernization

- Enforced LF checkout for the extensionless SageTV server launchers, Debian
  maintainer scripts, and common bundled third-party build helpers. Fresh
  Windows clones can now package and execute `startsagecore` without the Linux
  loader misreading a CRLF shebang as a missing interpreter.
- Made every `compileJava` invocation restore the temporary build-number
  change and remove `SageConstants.java.bak` immediately, failing if cleanup
  is impossible. This covers direct test builds as well as `sageJar` and
  prevents stale ignored backups from changing tracked source later.
- Disabled documentation generation in the private legacy FFmpeg-minimal
  dependency build so clean native builds do not leave untracked manuals in
  the source checkout.
- Renamed the project and unified development environment references to the
  full `opensagetv-vibe-*` namespace without changing SageTV, JNI, or package
  ABI names.
- Changed `sagetv-dev.sh` to reuse the unified `opensagetv-vibe-dev` build container
  instead of creating and rebuilding a separate Core-only container.
- Discover an installed `xmltv.XMLTVImportPlugin` when the EPG import property
  is empty, and fall back to it when an obsolete configured importer cannot be
  loaded. This prevents the open-source server from entering the retired
  license-key EPG path merely because the optional XMLTV JAR was installed
  before its property was configured.

## Next

* Canonicalized discovered network encoders by numeric source IP instead of reverse-DNS hostname, preventing duplicate OpenDCT tuners such as `encoder-host:9000` and `192.0.2.10:9000` after restarts.
* Restored MiniClient discovery replies to the original single UDP socket, allowing the kernel to choose the correct response address on Docker host, macvlan, and Unraid ipvlan/br0 networks. This removes the competing same-port response socket that could lose broadcasts or retain a stale address after a network-mode change.
* Added a single Ubuntu 26.04/OpenJDK 11 Docker development environment for clean compilation, testing, diagnostics, packaging, and server smoke runs.
* Modernized `libImageLoader.so` to use Ubuntu libpng16, current PNG transformations, transformed channel/row metadata, explicit SageTV pixel conversion, and contained libpng error handling.
* Added RGB, RGBA, palette, grayscale, grayscale-alpha, tRNS, 16-bit, channel-logo analogue, malformed-file, and global-symbol-preemption ImageLoader regressions.
* Added amd64 ELF dependency inspection and required JNI export validation across every produced Linux shared library.
* Updated native code for current GCC/glibc headers, callback and pthread signatures, 64-bit handles, C prototypes, giflib 5, FreeType, and other compile-blocking ABI/type changes.
* Added same-container server startup and repeated shutdown tests plus deterministic artifact collection and `output/BUILD_REPORT.md`.
* Added LF policy for Unix `configure` scripts so Windows Docker Desktop checkouts remain executable in Linux containers.

## Version 9.2.10 (2025-04-04)
* SD EPG changes to correct Error 6000, 4009 and some other login issues with SD

## Version 9.2.9 (2025-03-11)
* Updated gradle script so that project could build in Netbeans
* Updated the FFMPEGTranscoder to fallback to frame count instead of time to calculate progress
* Allow IR blasters that support it to xmit non-numeric Tune strings (eg 42-1).
* SD EPG changes to correct image retrieval without token and other API corrections/updates
* SD EPG added debug_sd_support property to enable extra debug info when contacting SD support
* SD EPG added sdepg_core/bypassCelebrityImages to allow users in the future to bypass reteiving Celebrity images from SD if causing issues
* SD EPG added sdepg_core/bypassProgramImages to allow users in the future to bypass retrieving Program images from SD if causing issues
* SD EPG added sdepg_core/bypassEPGUpdates to allow users in the future to bypass retrieving EPG from SD if causing issues
* SD EPG added wizard/scheduled_maintenance and wizard/scheduled_maintenance_offset to allow users to set the hour that the daily maintenance will run
* SD EPG added code to support SD now passing back the current token along with its expiration
* SD EPG fix for send SD empty program lists as well as malformed endpoint for metadata/program
* SD EPG fix enpoint call for metadata/programs to use 14 character programID rather than shortended to 10
* Added seeker/duration_for_watchdog property to handle long running watchdog process for larger libraries (defaults to 60000)
* Windows installer build notes updated for location of missing files needed for the build
* Added ability to notify user if a new version is available on Github (defaults to enabled but can be disabled)

## Version 9.2.8 (2022-01-05)
* Update to build process to support Linux build on Ubuntu 18.04 and JDK 11
* removed Travis process as no longer used for builds

## Version 9.2.7 (2022-01-04)
* Update to build process to support build on JDK 11 while supporting Java 8 dockers to run SageTV
* Added DirecTVTuner DLL for http tuning (Windows)

## Version 9.2.6 (2021-09-13)
* Updated weather in STV to use OpenWeatherMap
* Added option to FFMPEGTranscoder to allow for a setting to copy video or audio
* Added a new option to the Miniclient for fixed remux profile.  This is used when the audio/video codec are supported, but the container is not
* Added some additional constraints on -aspect switch in FFMPEGTranscoder to make sure an invalid aspect ratio is not passed to the transcoder

## Version 9.2.5 (2021-05-24)
* Fixed 32-bit installer incorrectly removing uu_irsage.dll which broke USB-UIRT (Windows)
* Visual Studio launcher project cleanup (Windows)
* Added Detailed Setup -> Customize option to disable display of thumbnails/artwork for shows.
* Added Detailed Setup -> Customize option to disable display of channel logos.

## Version 9.2.4 (2021-04-16)
* Fixed database clearing of non-manual Wasted objects that were over a year old (comment indicated it happened, but was never implemented before)
* Fixed crash on extenders when loading 4K images that use diffused textures

## Version 9.2.3 (not released)
* Added ability to use fixed push format when transcoding is required, but not low bandwidth
* Added 720, 1080 and SOURCE (use video source resolution) options to FFMPEGTranscoder for fixed transcoding
* Added SOURCE option to FPS that calculates GOP automatically and uses FPS of source videos
* Added an option for Audio Channels.  Does not allow a value greater than source audio
* Fix for conversion to MKV. Removed Format substitution of "MATROSKA" -> "MATROSKA,WEBM"

## Version 9.2.2 (2020-05-16)
* Fixed MpegDeMux that crashed some MPEG2 playback (Windows)
* Change service launcher (Windows) to support local JRE
* Change watch ignore times from constants to properties
* Tidy up warnings in VS2015 for data type conversions (Windows)
* Fix for Hauppauge 885 tuners with Alt TS Capture Devices (Windows)
* Change maximum number of BDA tuners from 2 to 4 (HVR-5525 has 3 BDA tuners)
* Fix DirecTVSerialControl
* Fix 64-bit service launcher (Windows)
* Set default 1G heap for 64-bit (Windows)
* Updates for OPTUS D1 transponder changes to DVB-S2
* Fix: Schedules Direct EPG grabber failed to finish updating some satellite-based lineups
* Added Forced as a property to SubpictureFormat
* Added the ability to auto select forced subtitle track based on the default audio language
* Added the ability to use a Plugin for format detection of media files instead of built in ffmpeg.
* Added 2160p as a Pretty resolution to VideoFormat
* Added HEVC as a supported media format

## Version 9.2.1 (2019-03-23)
* 64-bit AVI playback and music fixes (Windows)
* Change: Allowlist LAV Audio and Video Decoders (Windows)
* Change: SageTV7 STV system information will indicate 32/64 bit
* Fix: Include Win10 in 'VISTA_OS' detection (Windows)
* Change: New installation properties default video/dvd_video renderer is 'EVR'
* Change: New installation properties default video/audio decoder are auto-detected
* New: Add EXEMultiTunerPlugin, HCWIRBlaster & USB-UUIRT VS projects (Windows)
* 64-bit code and VS project updates (Windows)
* Enabled MSYS2/MinGW compile for FFMPEG-based projects (Windows)
* Fix: IR interface hangs trying to send non-numeric (eg: 42-1-1) command.
* Removed dependency on SDK6.1 (Windows)
* Fix: Sage-x64 hang due to CableCARD tuners (Windows)
* Fix: Add support for HVR-4400 and other 885 variants (Windows)

## Version 9.1.10 (2018-10-13)
* removed old bytes properties for episodeName and desc to resolve potential crashes

## Version 9.1.9 (2018-05-18)
* Byte based seeking support for MPEG files
* update libhdhomerun to 20170930
* Fixed bug where file modification time can get set incorrectly

## Version 9.1.8 (2017-11-13) - windows only
* HD-PVR2 video capture device: add ability to select multiple audio inputs (Windows)
* HD PVR 60 video capture device: new device support (Windows)

## Version 9.1.7 (2017-09-24)
* Fix: add support for 2nd tuner of Hauppauge WinTV-dualHD usb tuner stick (Windows).
* Changes in the STV set 2017081201 for the next SageTV release v9.1.7.0:
    * malore menus: Removed random misc adjectives after show titles; only display misc textafter the title if it is a star rating.
	* Removed Zap2it logo from System Information.
	* EPG Lineup configuration: Changed help text above option buttons, put Schedules Direct option at top of list, old built-in EPG option renamed as plugin option and moved down.
	* Fixed Music by Artist filtering issue resulting in 0 songs per artist after entering 2nd and subsequent chars.
	* Disabled access to YouTube, Google videos, and channels.com.
	* Detailed Setup -> General: reworded the Sync System Clock option.
	* Detailed Setup -> Advanced: removed Debug Logging enable/disable option because it is always enabled now.
	* Configuration Wizard playback testing/configuration menu uses the "Default" decoder settings instead of SageTV MPEG decoders.
	* Detailed Setup -> Customize: renamed extra option to mark channels in guide with non-Zap2it channel IDs to refer to non-Tribune IDs.
	* Changed Zap2it text to Tribune elsewhere in the STV, since the EPG data fo the old built-in and new SD EPG data both ultimately come from Tribune.  
* Fix: resolved 'Grey-scale channel logos are green and half-width' for Windows releases (was fixed for linux in 9.0.8.423 and newer)

## Version 9.1.6 (2017-08-10)
* Fix: Various fixes and cleanup on Linux Firewire and DVB.
* Fix: Added support for all 4 tuners on the Hauppauge WinTV-quadHD tuner in Windows.
* New: Add Schedules Direct lineup by ID.
* Change: Removed ZZZ from Schedules Direct Regions because it doesn't do anything.
* Fix: VOB and MP4 subtitles locking methods were not being called.
* Fix: Fixes to HDHomeRun (and probably others) ATSC Scanning returning blank and garbled channels.
* Fix: Reduced Schedules Direct person image import threads to 4 (including the execution thread) and added logging for when new threads are created for during the process.
* Change: Removed unhelpful alias to original person log entries.
* Fix: Fixed issue with Schedules Direct forcing a full airing re-import on stations that do not have a No Data airing.
* Change: Lowered the priority of the Schedules Direct person image import threads.
* Fix: Removed use of G1GC in Windows due to possible memory leak issues.

## Version 9.1.5 (2017-06-19)
* Fix: Carny throws a null pointer exception if a show has a null title.

## Version 9.1.4 (2017-06-11)
* Fix: Schedules Direct deleted lineups were not removed from accounts correctly.
* Fix: When checking for existing lineups and a deleted lineup exists, a null pointer exception was thrown.
* Change: The SRT subtitle monitoring thread now uses Pooler.
* Fix: Index out of bounds exception while getting recommendations from Schedules Direct.

## Version 9.1.3 (2017-05-30)
* Fix: A missing space in an if test causes the Linux start script to fail.

## Version 9.1.2 (2017-05-30)
* Fix: Changed awk parsing to use sed to clean up the Java version check.
* Fix: API methods GetFavoriteAirings() and GetPotentialFavoriteAirings() were returning all airings for keyword favorites.
* New: Increased possible range for scheduling lookahead to 21 days. The default is still 14 days.
* Fix: Removed check in Scheduler that was preventing a future airing beyond lookahead from being considered to resolve a conflict.
* Fix: Fixed Carny not being marked prepped on startup when no agents exist.

## Version 9.1.1 (2017-05-22)
* Fix: Fixed a problem with awk parsing in Ubuntu

## Version 9.1.0 (2017-05-22)
* Fix: Transcoder crashing on Linux with signal 11.
* New: Added new API method to get enabled and disabled favorites.
    * public Airing[] GetPotentialFavoriteAirings(Favorite Favorite);
* Fix: Aliases without a non-alias would cause an NPE when searching.
* Fix: Schedules Direct aliasing logic was applied backwards.
* New: Carny is now multi-threaded and highly optimized.
* New: Schedules Direct movie length is now imported.
* New: Schedules Direct alternative channel logos can now be used by changing the property sdepg_core/use_alternate_logos=false to true.
    * This can also be changed in the UI via Setup > Detailed Setup > Customize > Use Alternative Schedules Direct Channel Logos.
* New: Enabled G1GC String deduplication for Java versions 8 and 9.

## Version 9.0.14 (2017-03-18)
* New: Added new API methods for in progress sports tracking using Schedules Direct.
  * public boolean IsSDEPGServiceAvailable();
  * public boolean[] IsSDEPGInProgressSport(String[] ExternalIDs);
  * public int[] GetSDEPGInProgressSportStatus(String[] ExternalIDs);
* New: Added editorials based on recommendations from Schedules Direct.
* Fix: Radio stations in Schedules Direct guide data now retain their prepended zeros in the guide data.
* Fix: Teams from Schedules Direct were being skipped because they do not have a person ID.

## Version 9.0.13 (2017-01-19)
* Fix: Schedules Direct was unable to distinguish between two lineups with the exact same name.
* Fix: Added handling for an unknown regular expression Schedules Direct was providing for the postal code for a few countries. The code also now skips the check if it does not recognize the regex formatting.
* Fix: Added better handling to Seeker when starting a recording and no directories are selectable for the desired encoder.
* Force debug logging to always be on.
* Fix: Watched calculation for movies with commercials is improved
* Fix: Prevent freezing between programs when playing back on Windows (matches V7 behavior, although not ideal, avoids freezing)
* New: Added more roles for Person objects.
* New: Schedules Direct Person images are now imported.
* New: Schedules Direct movie quality ratings are now a part of the bonus data.
* Fix: Schedules Direct movie images are now prioritized to use box art first.
* Fix: Schedules Direct now updates channels with No Data with previously saved hashes that happen to still be valid.
* Fix: Startup now explicitly adds lucene-core-3.6.0.jar before loading the JARs folder to address a common upgrade issue.

## Version 9.0.12 (2016-12-22)
* New: Schedules Direct now includes teams as people for favorite scheduling.
* New: SageTV server will no longer allow the server to go to sleep until video conversions are complete.
* New: Updated DVB-S & DVB-T frequencies for New Zealand
* New: Add STV support for enabling and disabling favorites
* Fix: Schedules Direct was not returning the saved country in some cases.
* Fix: Removed asterisks from password field when entering the password for Schedules Direct.
* Fix: Fixed so that Ministry will not allow sleep while converting.
* Fix: Allow mounting DVD iso images as non-root
* Linux Placeshifter: Added AC3 support

## Version 9.0.11 (2016-11-20)
* Fix: Enable streams with valid PAT packets and invalid PMT packets to be able to be detected by the built in remuxer.
* Fix: Linux tries a few more adapters when trying to get the primary server IP address.

## Version 9.0.9 (2016-10-10)
* Fix: GetSeriesID wasn't always returning a valid series ID
* New: Added logic to Schedules Direct program categories to ensure Movie is the first category for programs that start with MV
* Fix: Cleaned up the logic for determining when images from Schedules Direct should be in a Show or SeriesInfo object
* Fix: Clarified in logging when we can't process anything currently because Schedules Direct is offline
* Fix: Added random timeout when Schedules Direct token expires before getting a new token in case there are multiple SageTV servers using the same account

## Version 9.0.8.429 (2016-09-27)
* Fix: Fixed plugin bug that caused some upgraded plugins to be in a corrupted state
* Fix: Fixed bug in the EPG license detection logic

## Version 9.0.8 (2016-09-22)
* New: Added Schedules Direct EPG support as a core BETA feature

## Version 9.0.7 (2016-08-10)
* New: Added SageTVPluginsDev.d directory support (See [SageTVPluginsDev README](SageTVPluginsDev.md))
* New: Added direct JAR linking in SageTV Plugin Manifest (ie, no need to repackage library plugins as .zip files)


#### Notes about incrementing versions for developers:

* If you are the first to commit changes after a release, ensure that the following have been incremented beyond the last release:
    * MICRO_VERSION in sage/Version.java
* If you make any changes to stvs/SageTV7/SageTV7.xml, ensure that the following are updated in the STV:
    * AddGlobalContext( "STVversionText", "August 12, 2017" )
        * This should match the date of the commit.
    * AddGlobalContext( "ThisSTVSetVersionNum", "2017081201" )
        * This should match the date of the commit and if there was more than one commit the same day, the last two digits should be incremented.
        * The format is YYYYMMDDVV.
        * YYYY is the year.
        * MM if the month number.
        * DD is the day of the month.
        * VV is the commit version for this date. This resets to 01 if the date changes.
    * STVVersion [="9.1.7.0"]
        * This should start with MAJOR_VERSION.MINOR_VERSION.MICRO_VERSION in sage/Version.java
        * The last number should be incremented for each update of the STV for the MAJOR_VERSION.MINOR_VERSION.MICRO_VERSION SageTV release, starting with 0 for the first STV version of a new release.
