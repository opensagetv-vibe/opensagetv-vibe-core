# OpenSageTV Vibe Core contributor rules

Read `README.md`, `HANDOFF.md`, `TASKS.md`, `WORKFLOW.md`, and the Ubuntu 26
audit before changing Core. `TASKS.md` is the only local backlog. Remove
completed work and record evidence in `CHANGELOG.md` and `HANDOFF.md`.

Do not create prompt/review/per-version notes. Preserve SageTV/JNI ABI, Java 11,
system-libpng behavior, LF executable files, and the one unified Docker
environment. Never commit output, user configuration, credentials, recordings,
or databases. Root launchers must work from any caller directory.


## Stock-server test-control policy

- For any new testing, commissioning, diagnostic, or automation control, first
  implement or extend the stock-compatible `opensagetv-vibe-core-MCP-Plugin`
  using supported `sage.SageTV.api`/`apiUI` calls and verify it against an
  unmodified stock SageTV server.
- Do not patch `Sage.jar`, add private MiniClient events, or change Core merely
  to make a test easier. Existing public APIs, the bounded MCP bridge, and
  external test tooling are the required first option.
- Change Core only when the required production runtime behavior cannot be
  expressed through the stock plugin/API boundary. Document the proven API
  gap, keep the extension optional and negotiated with a safe stock fallback,
  and verify older clients and installations remain unaffected.
