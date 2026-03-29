#!/bin/bash
#
# deploy-geocraft-windows.sh — Deploy GeoCraft as a standalone Windows application
#
# This script assembles the deployment on any platform (macOS/Linux/Windows with bash).
# The resulting directory can be copied to a Windows x86_64 machine and run there.
#
# Prerequisites:
#   - Java 11 installed on the target Windows machine
#   - All workspace plugins compiled in Eclipse (bin/ directories must exist)
#
# Usage:
#   ./deploy-geocraft-windows.sh [export-directory]
#   Default export directory: ~/geocraft-deployed-windows/
#
# After deployment, launch on Windows with:
#   geocraft\GeoCraft.bat

set -euo pipefail

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
source "$WORKSPACE/deploy-geocraft-common.sh"

EXPORT_DIR="${1:-$HOME/geocraft-deployed-windows}"
TARGET_PLUGINS="$WORKSPACE/org.geocraft.target/EclipseRCP3.5.1/plugins"
ECLIPSE_DIR="$EXPORT_DIR/geocraft"
PLUGINS_DIR="$ECLIPSE_DIR/plugins"
CONFIG_DIR="$ECLIPSE_DIR/configuration"

echo "=== GeoCraft Deployment (Windows Win32 x86_64) ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

rm -rf "$EXPORT_DIR"
mkdir -p "$PLUGINS_DIR" "$CONFIG_DIR"

copy_target_plugins "$PLUGINS_DIR" "$TARGET_PLUGINS" win32 win32 x86_64
copy_workspace_plugins "$PLUGINS_DIR" "$WORKSPACE"
copy_config_and_branding "$CONFIG_DIR" "$PLUGINS_DIR" "$WORKSPACE"

# --- Create Windows batch launcher ---
echo "--- Creating launcher ---"
LAUNCHER_JAR=$(basename "$(ls "$PLUGINS_DIR"/org.eclipse.equinox.launcher_*.jar | head -1)")

# Use printf to ensure Windows line endings (CRLF)
printf '@echo off\r\n' > "$ECLIPSE_DIR/GeoCraft.bat"
printf 'setlocal\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf 'set SCRIPT_DIR=%%~dp0\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf 'rem Find Java - prefer JAVA_HOME, fall back to PATH\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf 'if defined JAVA_HOME (\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  set JAVA=%%JAVA_HOME%%\\bin\\java.exe\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf ') else (\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  set JAVA=java\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf ')\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '"%%JAVA%%" ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -Xms256m ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -Xmx1200m ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -Dorg.osgi.framework.executionenvironment="OSGi/Minimum-1.0,OSGi/Minimum-1.1,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8,JavaSE-9,JavaSE-10,JavaSE-11,CDC-1.0/Foundation-1.0,CDC-1.1/Foundation-1.1" ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  --add-opens=java.base/java.net=ALL-UNNAMED ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  --add-opens=java.base/java.lang=ALL-UNNAMED ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -jar "%%SCRIPT_DIR%%plugins\\%s" ^\r\n' "$LAUNCHER_JAR" >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -os win32 ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -ws win32 ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -arch x86_64 ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -console ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -consoleLog ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  -configuration "%%SCRIPT_DIR%%configuration" ^\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
printf '  %%*\r\n' >> "$ECLIPSE_DIR/GeoCraft.bat"
echo "  Created GeoCraft.bat launcher"

# --- Summary ---
TOTAL=$(ls "$PLUGINS_DIR" | wc -l | tr -d ' ')
echo ""
echo "=== Deployment Complete ==="
echo "Location:  $ECLIPSE_DIR"
echo "Plugins:   $TOTAL"
echo ""
echo "Copy the '$ECLIPSE_DIR' directory to a Windows machine."
echo "To launch on Windows:"
echo "  geocraft\\GeoCraft.bat"
