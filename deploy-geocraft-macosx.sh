#!/bin/bash
#
# deploy-geocraft-macosx.sh — Deploy GeoCraft as a standalone macOS .app bundle
#
# Prerequisites:
#   - Java 11 installed (for Rosetta/x86_64 SWT compatibility)
#   - All workspace plugins compiled in Eclipse (bin/ directories must exist)
#
# Usage:
#   ./deploy-geocraft-macosx.sh [export-directory]
#   Default export directory: ~/geocraft-deployed-macosx/
#
# After deployment, launch with:
#   /path/to/export/GeoCraft.app/Contents/MacOS/GeoCraft
# Or double-click GeoCraft.app in Finder.

set -euo pipefail

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
source "$WORKSPACE/deploy-geocraft-common.sh"

EXPORT_DIR="${1:-$HOME/geocraft-deployed-macosx}"
TARGET_PLUGINS="$WORKSPACE/org.geocraft.target/EclipseRCP3.5.1/plugins"
PLUGINS_DIR="$EXPORT_DIR/GeoCraft.app/Contents/Eclipse/plugins"
CONFIG_DIR="$EXPORT_DIR/GeoCraft.app/Contents/Eclipse/configuration"
MACOS_DIR="$EXPORT_DIR/GeoCraft.app/Contents/MacOS"

echo "=== GeoCraft Deployment (macOS x86_64) ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

rm -rf "$EXPORT_DIR"
mkdir -p "$PLUGINS_DIR" "$CONFIG_DIR" "$MACOS_DIR"

copy_target_plugins "$PLUGINS_DIR" "$TARGET_PLUGINS" macosx cocoa x86_64
copy_workspace_plugins "$PLUGINS_DIR" "$WORKSPACE"
copy_config_and_branding "$CONFIG_DIR" "$PLUGINS_DIR" "$WORKSPACE"

# --- Create macOS launcher ---
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

# --- Create Info.plist ---
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
echo "Plugins:   $TOTAL"
echo ""
echo "To launch:"
echo "  $EXPORT_DIR/GeoCraft.app/Contents/MacOS/GeoCraft"
echo ""
echo "Or double-click GeoCraft.app in Finder."
