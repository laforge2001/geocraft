Deploy GeoCraft as a standalone application for one or more platforms.

This bypasses the Eclipse PDE product export wizard (which has unresolvable dependency issues with the legacy Eclipse 3.5 target platform on modern Eclipse) and directly assembles the product.

## Arguments

- `$ARGUMENTS` — Platform(s) to deploy: `macosx`, `linux`, `windows`, or `all`. Optionally followed by export directory. Default: `all` with platform-specific default directories.

Examples:
- `/deploy-geocraft macosx` — deploy macOS to ~/geocraft-deployed-macosx/
- `/deploy-geocraft linux /tmp/geocraft` — deploy Linux to /tmp/geocraft
- `/deploy-geocraft all` — deploy all three platforms

## Steps

1. Parse `$ARGUMENTS` to determine platform(s) and optional export directory.

2. For each requested platform, run the corresponding deploy script:
   - **macOS**: `./deploy-geocraft-macosx.sh [export-dir]`
   - **Linux**: `./deploy-geocraft-linux.sh [export-dir]`
   - **Windows**: `./deploy-geocraft-windows.sh [export-dir]`

3. **Smoke test** (macOS only, since we're running on macOS) — launch the app for ~10 seconds in background, capture output, verify:
   - OSGi framework starts
   - "Default message service created" appears
   - "Starting GeoCraft" appears
   - Report any bundle resolution errors

4. Report the export path(s) and plugin counts.

## Platform Differences

| | macOS | Linux | Windows |
|---|---|---|---|
| Structure | GeoCraft.app bundle | Flat geocraft/ dir | Flat geocraft/ dir |
| SWT fragment | cocoa.macosx.x86_64 | gtk.linux.x86_64 | win32.win32.x86_64 |
| Launcher | bash + arch -x86_64 | bash | .bat |
| Extra flags | -XstartOnFirstThread | — | — |
| Java discovery | /usr/libexec/java_home | JAVA_HOME or PATH | JAVA_HOME or PATH |

## Important Notes

- All workspace plugins MUST be compiled in Eclipse first (bin/ directories must exist)
- Use `bash` (not zsh) for running the deploy scripts to avoid zsh glob expansion issues
- The macOS launcher uses `arch -x86_64` (Rosetta) because SWT is x86_64-only
- `org.eclipse.core.net` is intentionally excluded — it requires `org.eclipse.equinox.security` which isn't in the target platform
- Platform-specific SWT/launcher fragments from the Eclipse 3.5.1 Delta Pack are in the target platform
- The deploy scripts share common logic via `deploy-geocraft-common.sh`
