#!/bin/bash
#
# deploy-geocraft.sh — Build and deploy GeoCraft as a standalone macOS .app bundle
#
# This bypasses the Eclipse PDE product export wizard, which has unresolvable
# dependency issues with the legacy Eclipse 3.5 target platform on modern Eclipse.
# Instead, it directly assembles the product from pre-compiled workspace plugins
# and the target platform.
#
# Prerequisites:
#   - Java 11 installed (for Rosetta/x86_64 SWT compatibility)
#   - All workspace plugins compiled in Eclipse (bin/ directories must exist)
#
# Usage:
#   ./deploy-geocraft.sh [export-directory]
#   Default export directory: /Users/ericgeordi/geocraft-deployed/
#
# After deployment, launch with:
#   /path/to/export/GeoCraft.app/Contents/MacOS/GeoCraft
#
# Or double-click GeoCraft.app in Finder.

set -euo pipefail

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
EXPORT_DIR="${1:-/Users/ericgeordi/geocraft-deployed}"
TARGET_PLUGINS="$WORKSPACE/org.geocraft.target/EclipseRCP3.5.1/plugins"
PLUGINS_DIR="$EXPORT_DIR/GeoCraft.app/Contents/Eclipse/plugins"
CONFIG_DIR="$EXPORT_DIR/GeoCraft.app/Contents/Eclipse/configuration"
MACOS_DIR="$EXPORT_DIR/GeoCraft.app/Contents/MacOS"

echo "=== GeoCraft Deployment ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

# --- Clean and create directory structure ---
rm -rf "$EXPORT_DIR"
mkdir -p "$PLUGINS_DIR" "$CONFIG_DIR" "$MACOS_DIR"

# --- Step 1: Copy target platform plugins ---
echo "--- Copying target platform plugins ---"
TARGET_COUNT=0
for jar in "$TARGET_PLUGINS"/*; do
  name=$(basename "$jar")
  # Skip plugins with unresolvable deps or test-only plugins
  case "$name" in
    org.eclipse.core.net*|org.junit*|org.hamcrest*)
      echo "  SKIP: $name"
      continue
      ;;
  esac
  cp -R "$jar" "$PLUGINS_DIR/"
  TARGET_COUNT=$((TARGET_COUNT + 1))
done
echo "  Copied $TARGET_COUNT target platform plugins"
echo ""

# --- Step 2: Copy workspace plugins referenced by features ---
echo "--- Copying workspace plugins ---"

# Collect all plugin IDs from all 4 feature.xml files
FEATURE_PLUGINS=$(grep -h 'id="' \
  "$WORKSPACE"/org.geocraft.feature/feature.xml \
  "$WORKSPACE"/org.geocraft.geomath.feature/feature.xml \
  "$WORKSPACE"/org.geocraft.abavo.feature/feature.xml \
  "$WORKSPACE"/org.geocraft.ui.viewer.feature/feature.xml \
  | sed -n 's/.*id="\([^"]*\)".*/\1/p' | sort -u)

WS_COUNT=0
WS_FAIL=0

for plugin_id in $FEATURE_PLUGINS; do
  plugin_dir="$WORKSPACE/$plugin_id"

  # Skip if already present from target platform
  if ls "$PLUGINS_DIR/${plugin_id}_"* 1>/dev/null 2>&1; then
    continue
  fi

  # Skip feature directories (no MANIFEST.MF)
  if [ ! -f "$plugin_dir/META-INF/MANIFEST.MF" ]; then
    continue
  fi

  if [ ! -d "$plugin_dir" ]; then
    echo "  MISSING: $plugin_id (no workspace directory)"
    WS_FAIL=$((WS_FAIL + 1))
    continue
  fi

  # Check that Eclipse has compiled this plugin
  if [ ! -d "$plugin_dir/bin" ]; then
    echo "  WARNING: $plugin_id has no bin/ directory — not compiled in Eclipse?"
    # Still copy resources, just won't have classes
  fi

  DEST="$PLUGINS_DIR/${plugin_id}_1.0.0"
  mkdir -p "$DEST"

  # Copy compiled classes
  if [ -d "$plugin_dir/bin" ]; then
    cp -R "$plugin_dir/bin/"* "$DEST/" 2>/dev/null || true
  fi

  # Copy everything except source, build artifacts, and IDE files
  for item in "$plugin_dir"/*; do
    name=$(basename "$item")
    case "$name" in
      src|bin|.settings|.classpath|.project|build.properties|.gitignore|pom.xml|feature.xml)
        continue ;;
    esac
    cp -R "$item" "$DEST/"
  done
  # Copy compiled classes from bin/ on top
  if [ -d "$plugin_dir/bin" ]; then
    cp -R "$plugin_dir/bin/"* "$DEST/" 2>/dev/null || true
  fi

  WS_COUNT=$((WS_COUNT + 1))
done

echo "  Copied $WS_COUNT workspace plugins"
if [ $WS_FAIL -gt 0 ]; then
  echo "  WARNING: $WS_FAIL plugins could not be found"
fi
echo ""

# --- Step 3: Copy configuration ---
echo "--- Setting up configuration ---"
cp "$WORKSPACE/org.geocraft.product/config.ini" "$CONFIG_DIR/config.ini"
echo "  Copied config.ini"

# --- Step 4: Ensure branding resources ---
PRODUCT_DEST="$PLUGINS_DIR/org.geocraft.product_1.0.0"
if [ -d "$PRODUCT_DEST" ]; then
  cp "$WORKSPACE/org.geocraft.product/splash.bmp" "$PRODUCT_DEST/" 2>/dev/null || true
  mkdir -p "$PRODUCT_DEST/icons"
  cp "$WORKSPACE/org.geocraft.product/icons/"* "$PRODUCT_DEST/icons/" 2>/dev/null || true
fi

# --- Step 5: Create launcher script ---
echo "--- Creating launcher ---"
LAUNCHER_JAR=$(basename "$(ls "$PLUGINS_DIR"/org.eclipse.equinox.launcher_*.jar | head -1)")

cat > "$MACOS_DIR/GeoCraft" << LAUNCHER
#!/bin/bash
SCRIPT_DIR="\$(cd "\$(dirname "\$0")" && pwd)"
ECLIPSE_DIR="\$SCRIPT_DIR/../Eclipse"

# Use Java 11 under Rosetta for SWT cocoa x86_64 compatibility
JAVA_HOME=\$(/usr/libexec/java_home -v 11 2>/dev/null)
if [ -z "\$JAVA_HOME" ]; then
  echo "Error: Java 11 not found. Please install Java 11."
  exit 1
fi

exec arch -x86_64 "\$JAVA_HOME/bin/java" \\
  -Xms256m \\
  -Xmx1200m \\
  -XstartOnFirstThread \\
  -Dorg.eclipse.swt.internal.carbon.smallFonts \\
  -Dorg.osgi.framework.executionenvironment="OSGi/Minimum-1.0,OSGi/Minimum-1.1,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8,JavaSE-9,JavaSE-10,JavaSE-11,CDC-1.0/Foundation-1.0,CDC-1.1/Foundation-1.1" \\
  --add-opens=java.base/java.net=ALL-UNNAMED \\
  --add-opens=java.base/java.lang=ALL-UNNAMED \\
  -jar "\$ECLIPSE_DIR/plugins/$LAUNCHER_JAR" \\
  -os macosx \\
  -ws cocoa \\
  -arch x86_64 \\
  -console \\
  -consoleLog \\
  -configuration "\$ECLIPSE_DIR/configuration" \\
  "\$@"
LAUNCHER
chmod +x "$MACOS_DIR/GeoCraft"
echo "  Created GeoCraft launcher"

# --- Step 6: Create Info.plist ---
cat > "$EXPORT_DIR/GeoCraft.app/Contents/Info.plist" << 'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>CFBundleName</key>
    <string>GeoCraft</string>
    <key>CFBundleIdentifier</key>
    <string>org.geocraft.product</string>
    <key>CFBundleVersion</key>
    <string>1.0.0</string>
    <key>CFBundleExecutable</key>
    <string>GeoCraft</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>NSHighResolutionCapable</key>
    <true/>
</dict>
</plist>
PLIST
echo "  Created Info.plist"

# --- Summary ---
TOTAL=$(ls "$PLUGINS_DIR" | wc -l | tr -d ' ')
echo ""
echo "=== Deployment Complete ==="
echo "Location:  $EXPORT_DIR/GeoCraft.app"
echo "Plugins:   $TOTAL ($TARGET_COUNT target + $WS_COUNT workspace)"
echo ""
echo "To launch:"
echo "  $EXPORT_DIR/GeoCraft.app/Contents/MacOS/GeoCraft"
echo ""
echo "Or double-click GeoCraft.app in Finder."
