# Core handoff

## Verified baseline

- Base: `ubuntu:26.04` (`resolute`)
- Architecture: Linux amd64
- Java: Ubuntu OpenJDK 11
- Builder tag used locally: `opensagetv-core-builder:u26-j11`
- Verification: `./sagetv-dev.sh all`

The latest clean run completed Java tests, all native builds, dependency/JNI checks, PNG format and symbol-preemption regressions, malformed-PNG containment, packaging, server startup, and repeated shutdown. The generated report is `output/BUILD_REPORT.md`.

## Important decisions

`libImageLoader.so` intentionally links Ubuntu's `libpng16.so.16`; it does not export an embedded `png_*` implementation. `LoadPNG` normalizes transformed input before inspecting channel/row sizes and contains libpng errors with `setjmp` cleanup so corrupt images cannot abort the JVM.

The old minimal FFmpeg libraries remain only for `libMpeg2Transcoder.so` because that JNI code consumes the historical private ABI. Modern FFmpeg/MIM work is owned by `opensagetv-ffmpeg-mim` and is not silently substituted here.

Core treats an installed `xmltv.XMLTVImportPlugin` as the default external EPG
provider when `epg/epg_import_plugin` is empty. If that property names an old or
invalid importer while the XMLTV class is available, Core falls back to XMLTV
and repairs the property. This prevents setup from entering the retired
license-key EPG service. Container-level missing/invalid-property integration
tests live in `opensagetv-container/tests/core-xmltv-autodiscovery.sh` because
the optional plugin JAR is intentionally not bundled in Core itself.

Windows hosts must preserve LF endings for executable build files. `.gitattributes` explicitly covers generated/configure entry points, and a repository-local `core.autocrlf=false` is recommended.

## Next maintainer checks

Run the clean command after changing native code, inspect `output/test-results/elf`, and update both the Ubuntu audit and changelog when dependencies or exports change. Do not commit `output/`, Gradle state, credentials, appdata, recordings, or a user `Sage.properties`/`Wiz.bin`.
