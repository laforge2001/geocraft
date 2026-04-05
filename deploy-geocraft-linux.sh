#!/bin/bash
#
# deploy-geocraft-linux.sh — Deploy GeoCraft as a standalone Linux application
#
# Prerequisites:
#   - Run 'mvn clean verify' to build the product
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

echo "=== GeoCraft Deployment (Linux GTK x86_64) ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

find_product linux gtk x86_64

rm -rf "$EXPORT_DIR"
mkdir -p "$EXPORT_DIR"

echo "--- Copying Tycho-built product ---"
cp -R "$PRODUCT_DIR" "$EXPORT_DIR/geocraft"
echo "  Copied product from $PRODUCT_DIR"

# --- Summary ---
TOTAL=$(ls "$EXPORT_DIR/geocraft/plugins" 2>/dev/null | wc -l | tr -d ' ')
echo ""
echo "=== Deployment Complete ==="
echo "Location:  $EXPORT_DIR/geocraft"
echo "Plugins:   $TOTAL"
echo ""
echo "To launch:"
echo "  $EXPORT_DIR/geocraft/GeoCraft"
