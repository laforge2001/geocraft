#!/bin/bash
# GeoCraft standalone launcher - Eclipse 4 + JDK 21 native on Apple Silicon
set -e

WORKSPACE=$(cd "$(dirname "$0")" && pwd)
JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home}"

# Check for JDK 21 first, fall back to 17
if [ -d "/Library/Java/JavaVirtualMachines/temurin-21.jdk" ]; then
  JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
elif [ -d "$HOME/.sdkman/candidates/java/21-tem" ]; then
  JAVA_HOME="$HOME/.sdkman/candidates/java/21-tem"
fi

JAVA="$JAVA_HOME/bin/java"

echo ""
echo "=== GeoCraft Launch (Eclipse 4) ==="
echo "Java: $($JAVA -version 2>&1 | head -1)"
echo "Arch: $(uname -m)"
echo ""

# Build the product first if needed
if [ ! -d "$WORKSPACE/org.geocraft.product/target/products" ]; then
  echo "Product not built yet. Run: mvn clean verify"
  echo "Then re-run this script."
  exit 1
fi

# Find the built product
PRODUCT_DIR=$(find "$WORKSPACE/org.geocraft.product/target/products/org.geocraft.product.product" -maxdepth 1 -type d 2>/dev/null | head -1)
if [ -z "$PRODUCT_DIR" ]; then
  echo "Cannot find built product. Run: mvn clean verify"
  exit 1
fi

# On macOS, the product is inside a .app bundle
if [ -d "$PRODUCT_DIR/macosx/cocoa/aarch64/GeoCraft.app" ]; then
  APP_DIR="$PRODUCT_DIR/macosx/cocoa/aarch64/GeoCraft.app/Contents/Eclipse"
elif [ -d "$PRODUCT_DIR/macosx/cocoa/aarch64" ]; then
  APP_DIR="$PRODUCT_DIR/macosx/cocoa/aarch64"
else
  echo "Cannot find macOS aarch64 product"
  ls -la "$PRODUCT_DIR/"
  exit 1
fi

LAUNCHER=$(find "$APP_DIR/plugins" -name "org.eclipse.equinox.launcher_*.jar" 2>/dev/null | head -1)
if [ -z "$LAUNCHER" ]; then
  echo "Cannot find equinox launcher"
  exit 1
fi

exec "$JAVA" \
    -Xms256m \
    -Xmx1200m \
    -XstartOnFirstThread \
    -Dorg.eclipse.swt.internal.carbon.smallFonts \
    --add-opens=java.base/java.net=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    -jar "$LAUNCHER" \
    -os macosx \
    -ws cocoa \
    -arch aarch64 \
    -nl en_US \
    -console \
    -consoleLog \
    -product org.geocraft.product.product \
    -data /tmp/geocraft-workspace \
    2>&1
