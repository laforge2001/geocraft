#!/bin/bash

# GeoCraft Launch Script
# Updated for modern Java and Eclipse

WORKSPACE_DIR="/tmp/geocraft-workspace"
ECLIPSE_BASE="org.geocraft.target/EclipseSDK3.5.1"
LAUNCHER_JAR="$ECLIPSE_BASE/plugins/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar"

# Create workspace if it doesn't exist
mkdir -p "$WORKSPACE_DIR"

echo "Launching GeoCraft with Java $(java -version 2>&1 | head -1)"
echo "Workspace: $WORKSPACE_DIR"
echo "Using Eclipse base: $ECLIPSE_BASE"

# Launch the application with Java 21 compatibility flags
java -Xmx1200m \
     -Dfile.encoding=UTF-8 \
     -Dosgi.requiredJavaVersion=21 \
     -Dosgi.instance.area.default="$WORKSPACE_DIR" \
     -Dosgi.ws=cocoa \
     -Dosgi.os=macosx \
     -Dosgi.arch=aarch64 \
     -Djava.awt.headless=false \
     -XstartOnFirstThread \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.base/java.util=ALL-UNNAMED \
     --add-opens java.base/java.text=ALL-UNNAMED \
     --add-opens java.base/java.time=ALL-UNNAMED \
     --add-opens java.base/java.io=ALL-UNNAMED \
     --add-opens java.base/java.nio=ALL-UNNAMED \
     --add-opens java.base/sun.nio.ch=ALL-UNNAMED \
     --add-opens java.desktop/java.awt=ALL-UNNAMED \
     --add-opens java.desktop/javax.swing=ALL-UNNAMED \
     --add-opens java.desktop/sun.awt=ALL-UNNAMED \
     -jar "$LAUNCHER_JAR" \
     -os macosx \
     -ws cocoa \
     -arch aarch64 \
     -data "$WORKSPACE_DIR" \
     -application org.geocraft.application \
     -consoleLog \
     -vmargs \
     -Xms256m \
     -Xmx1200m