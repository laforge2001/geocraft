#!/bin/bash
#
# deploy-geocraft-linux.sh — Deploy GeoCraft as a standalone Linux application
#
# Prerequisites:
#   - Java 11 installed
#   - GTK+ 2.x libraries installed
#   - All workspace plugins compiled in Eclipse (bin/ directories must exist)
#
# Usage:
#   ./deploy-geocraft-linux.sh [export-directory]
#   Default export directory: ~/geocraft-deployed-linux/
#
# After deployment, launch with:
#   /path/to/export/geocraft/GeoCraft

set -euo pipefail

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
source "$WORKSPACE/deploy-geocraft-common.sh"

EXPORT_DIR="${1:-$HOME/geocraft-deployed-linux}"
TARGET_PLUGINS="$WORKSPACE/org.geocraft.target/EclipseRCP3.5.1/plugins"
ECLIPSE_DIR="$EXPORT_DIR/geocraft"
PLUGINS_DIR="$ECLIPSE_DIR/plugins"
CONFIG_DIR="$ECLIPSE_DIR/configuration"

echo "=== GeoCraft Deployment (Linux GTK x86_64) ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

rm -rf "$EXPORT_DIR"
mkdir -p "$PLUGINS_DIR" "$CONFIG_DIR"

copy_target_plugins "$PLUGINS_DIR" "$TARGET_PLUGINS" linux gtk x86_64
copy_workspace_plugins "$PLUGINS_DIR" "$WORKSPACE"
copy_config_and_branding "$CONFIG_DIR" "$PLUGINS_DIR" "$WORKSPACE"

# --- Create Linux launcher ---
echo "--- Creating launcher ---"
LAUNCHER_JAR=$(basename "$(ls "$PLUGINS_DIR"/org.eclipse.equinox.launcher_*.jar | head -1)")

cat > "$ECLIPSE_DIR/GeoCraft" << LAUNCHER
#!/bin/bash
SCRIPT_DIR="\$(cd "\$(dirname "\$0")" && pwd)"

# Find Java 11
if [ -n "\${JAVA_HOME:-}" ] && [ -x "\$JAVA_HOME/bin/java" ]; then
  JAVA="\$JAVA_HOME/bin/java"
else
  JAVA=\$(which java 2>/dev/null)
  if [ -z "\$JAVA" ]; then
    echo "Error: Java not found. Please set JAVA_HOME or add java to PATH."
    exit 1
  fi
fi

exec "\$JAVA" \\
  -Xms256m \\
  -Xmx1200m \\
  -Dorg.osgi.framework.executionenvironment="OSGi/Minimum-1.0,OSGi/Minimum-1.1,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8,JavaSE-9,JavaSE-10,JavaSE-11,CDC-1.0/Foundation-1.0,CDC-1.1/Foundation-1.1" \\
  --add-opens=java.base/java.net=ALL-UNNAMED \\
  --add-opens=java.base/java.lang=ALL-UNNAMED \\
  -jar "\$SCRIPT_DIR/plugins/$LAUNCHER_JAR" \\
  -os linux \\
  -ws gtk \\
  -arch x86_64 \\
  -console \\
  -consoleLog \\
  -configuration "\$SCRIPT_DIR/configuration" \\
  "\$@"
LAUNCHER
chmod +x "$ECLIPSE_DIR/GeoCraft"
echo "  Created GeoCraft launcher"

# --- Summary ---
TOTAL=$(ls "$PLUGINS_DIR" | wc -l | tr -d ' ')
echo ""
echo "=== Deployment Complete ==="
echo "Location:  $ECLIPSE_DIR"
echo "Plugins:   $TOTAL"
echo ""
echo "To launch:"
echo "  $ECLIPSE_DIR/GeoCraft"
