#!/bin/bash

# Simple GeoCraft Launch - bypassing complex OSGi issues
# This will try to run the core application with minimal dependencies

WORKSPACE_DIR="/tmp/geocraft-workspace"
ECLIPSE_BASE="org.geocraft.target/EclipseSDK3.5.1"

echo "Attempting simplified GeoCraft launch..."
echo "Java version: $(java -version 2>&1 | head -1)"

# Create workspace
mkdir -p "$WORKSPACE_DIR"

# Set up classpath with essential jars
CLASSPATH="$ECLIPSE_BASE/plugins/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar"
CLASSPATH="$CLASSPATH:$ECLIPSE_BASE/plugins/org.eclipse.osgi_3.5.1.R35x_v20090827.jar"
CLASSPATH="$CLASSPATH:$ECLIPSE_BASE/plugins/org.eclipse.swt.cocoa.macosx_3.5.1.v3555a.jar"

# Add project bundles
for bundle in org.geocraft.core org.geocraft.product org.geocraft.ui.common; do
    if [ -d "$bundle/bin" ]; then
        CLASSPATH="$CLASSPATH:$bundle/bin"
    fi
done

echo "Trying to run GeoCraft with minimal setup..."

java -cp "$CLASSPATH" \
     -XstartOnFirstThread \
     -Djava.awt.headless=false \
     -Dosgi.ws=cocoa \
     -Dosgi.os=macosx \
     -Dosgi.arch=x86_64 \
     --add-opens java.base/java.net=ALL-UNNAMED \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-opens java.desktop/java.awt=ALL-UNNAMED \
     --add-opens java.desktop/javax.swing=ALL-UNNAMED \
     org.eclipse.equinox.launcher.Main \
     -application org.geocraft.application \
     -data "$WORKSPACE_DIR"