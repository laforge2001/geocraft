Deploy GeoCraft as a standalone macOS application bundle.

This bypasses the Eclipse PDE product export wizard (which has unresolvable dependency issues with the legacy Eclipse 3.5 target platform on modern Eclipse) and directly assembles the product.

## Arguments

- `$ARGUMENTS` — Export directory path (default: `/Users/ericgeordi/geocraft-deployed/`)

## Steps

1. Set `EXPORT_DIR` to `$ARGUMENTS` if provided, otherwise `/Users/ericgeordi/geocraft-deployed/`
2. Clean and create the export directory structure:
   - `$EXPORT_DIR/GeoCraft.app/Contents/Eclipse/plugins/`
   - `$EXPORT_DIR/GeoCraft.app/Contents/Eclipse/configuration/`
   - `$EXPORT_DIR/GeoCraft.app/Contents/MacOS/`

3. **Copy target platform plugins** from `org.geocraft.target/EclipseRCP3.5.1/plugins/` to the export plugins dir.
   - Skip: `org.eclipse.core.net*` (missing equinox.security dep), `org.junit*`, `org.hamcrest*`

4. **Copy workspace plugins** — for each plugin ID referenced in all 4 feature.xml files (`org.geocraft.feature`, `org.geocraft.geomath.feature`, `org.geocraft.abavo.feature`, `org.geocraft.ui.viewer.feature`):
   - Skip if already copied from target platform
   - Skip feature directories (no META-INF/MANIFEST.MF)
   - Create `$PLUGINS_DIR/${plugin_id}_1.0.0/`
   - Copy everything EXCEPT: `src/`, `bin/`, `.settings/`, `.classpath`, `.project`, `build.properties`, `pom.xml`
   - Then overlay `bin/*` (Eclipse pre-compiled classes) on top
   - This ensures all resources are included: `META-INF/`, `OSGI-INF-*/` (DS component XMLs), `plugin.xml`, `*.jar` (Bundle-ClassPath), `icons/`, etc.

5. **Copy configuration** — copy `org.geocraft.product/config.ini` to `configuration/config.ini`

6. **Copy branding** — ensure `splash.bmp` and `icons/` are in the product plugin directory

7. **Create launcher script** at `Contents/MacOS/GeoCraft`:
   ```bash
   #!/bin/bash
   SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
   ECLIPSE_DIR="$SCRIPT_DIR/../Eclipse"
   JAVA_HOME=$(/usr/libexec/java_home -v 11 2>/dev/null)
   exec arch -x86_64 "$JAVA_HOME/bin/java" \
     -Xms256m -Xmx1200m \
     -XstartOnFirstThread \
     -Dorg.eclipse.swt.internal.carbon.smallFonts \
     -Dorg.osgi.framework.executionenvironment="OSGi/Minimum-1.0,OSGi/Minimum-1.1,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8,JavaSE-9,JavaSE-10,JavaSE-11,CDC-1.0/Foundation-1.0,CDC-1.1/Foundation-1.1" \
     --add-opens=java.base/java.net=ALL-UNNAMED \
     --add-opens=java.base/java.lang=ALL-UNNAMED \
     -jar "$ECLIPSE_DIR/plugins/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar" \
     -os macosx -ws cocoa -arch x86_64 \
     -console -consoleLog \
     -configuration "$ECLIPSE_DIR/configuration" "$@"
   ```
   Make it executable with `chmod +x`.

8. **Create Info.plist** at `Contents/Info.plist` with bundle name GeoCraft, identifier org.geocraft.product

9. **Smoke test** — launch the app for ~10 seconds in background, capture output, verify:
   - OSGi framework starts
   - "Algorithms service created" appears
   - "Starting GeoCraft" appears
   - Report any bundle resolution errors

10. Report the export path and plugin count.

## Important Notes

- All workspace plugins MUST be compiled in Eclipse first (bin/ directories must exist)
- Use `bash` (not zsh) for glob patterns in the copy scripts to avoid zsh expansion issues
- The launcher uses `arch -x86_64` (Rosetta) because SWT is x86_64-only
- Known non-critical errors: log4j NoClassDefFoundError in org.geocraft.core (missing log4j bundle)
- `org.eclipse.core.net` is intentionally excluded — it requires `org.eclipse.equinox.security` which isn't in the target platform
