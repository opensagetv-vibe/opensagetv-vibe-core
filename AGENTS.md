# OpenSageTV Vibe Core contributor rules

Read `README.md`, `HANDOFF.md`, `TASKS.md`, `WORKFLOW.md`, and the Ubuntu 26
audit before changing Core. `TASKS.md` is the only local backlog. Remove
completed work and record evidence in `CHANGELOG.md` and `HANDOFF.md`.

Do not create prompt/review/per-version notes. Preserve SageTV/JNI ABI, Java 11,
system-libpng behavior, LF executable files, and the one unified Docker
environment. Never commit output, user configuration, credentials, recordings,
or databases. Root launchers must work from any caller directory.
