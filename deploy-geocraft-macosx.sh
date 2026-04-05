#!/bin/bash
#
# deploy-geocraft-macosx.sh — Deploy GeoCraft as a standalone macOS .app bundle
#
# Prerequisites:
#   - Run 'mvn clean verify' to build the product
#
# Usage:
#   ./deploy-geocraft-macosx.sh [export-directory]
#   Default export directory: ~/geocraft-deployed-macosx/
#
# After deployment, launch with:
#   open /path/to/export/GeoCraft.app
# Or double-click GeoCraft.app in Finder.

set -euo pipefail

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
source "$WORKSPACE/deploy-geocraft-common.sh"

EXPORT_DIR="${1:-$HOME/geocraft-deployed-macosx}"

echo "=== GeoCraft Deployment (macOS aarch64) ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

find_product macosx cocoa aarch64

rm -rf "$EXPORT_DIR"
mkdir -p "$EXPORT_DIR"

echo "--- Copying Tycho-built product ---"
# Tycho materializes macOS products as Eclipse.app — rename to GeoCraft.app
if [ -d "$PRODUCT_DIR/Eclipse.app" ]; then
  cp -R "$PRODUCT_DIR/Eclipse.app" "$EXPORT_DIR/GeoCraft.app"
elif [ -d "$PRODUCT_DIR/GeoCraft.app" ]; then
  cp -R "$PRODUCT_DIR/GeoCraft.app" "$EXPORT_DIR/GeoCraft.app"
else
  echo "ERROR: No .app bundle found in $PRODUCT_DIR"
  exit 1
fi
echo "  Copied product from $PRODUCT_DIR"

# --- Summary ---
TOTAL=$(find "$EXPORT_DIR/GeoCraft.app" -path "*/plugins/*" -maxdepth 4 2>/dev/null | wc -l | tr -d ' ')
echo ""
echo "=== Deployment Complete ==="
echo "Location:  $EXPORT_DIR/GeoCraft.app"
echo "Plugins:   $TOTAL"
echo ""
echo "To launch:"
echo "  open $EXPORT_DIR/GeoCraft.app"
echo ""
echo "Or double-click GeoCraft.app in Finder."
