Launch GeoCraft from the command line using Java 11 under Rosetta, bypassing Eclipse PDE.

Run the launch script and capture the output to diagnose any OSGi bundle resolution errors or startup failures:

```
gtimeout 30 /Users/ericgeordi/dev/geocraft/geocraft/launch-geocraft.sh 2>&1
```

After launching, analyze the output for:

1. **Bundle resolution errors** — look for "not resolved" messages and identify missing dependencies
2. **Service startup** — confirm these core services initialize:
   - "Algorithms service created"
   - "Repository created"
   - "Log4J logging service started"
   - "Default message service created"
   - "Datastore accessor service started"
   - "Default Color Format service created"
   - "Default Color Map service created"
3. **Execution environment errors** — "Missing Constraint: Bundle-RequiredExecutionEnvironment" means the `org.osgi.framework.executionenvironment` VM arg is wrong or missing
4. **Package resolution errors** — "Missing imported package" means `system.packages.extra` in config.ini or VM args needs updating
5. **Application start** — look for "Starting GeoCraft" from org.geocraft.product

Known non-critical unresolved bundles (missing native code for macOS):
- `com.ardor3d` — 3D graphics, needs native libs
- `org.geocraft.io.javaseis` — JavaSeis I/O, needs native libs
- `org.eclipse.core.net` — missing org.eclipse.equinox.security (not in target platform)

If the output is too large, focus on lines containing "!MESSAGE" or "!STACK" for errors, and "DEBUG:" or "INFO:" for service status.

The launch script uses:
- Java 11 at `/Library/Java/JavaVirtualMachines/jdk-11.0.1.jdk/Contents/Home`
- `arch -x86_64` (Rosetta) since SWT is x86_64
- Eclipse Equinox launcher from the target platform
- dev.properties mapping workspace bundles to their bin/ directories
- Config with `org.osgi.framework.executionenvironment` listing all compatible execution environments

If you need to modify launch behavior, edit `/Users/ericgeordi/dev/geocraft/geocraft/launch-geocraft.sh`.
