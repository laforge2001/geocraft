#!/bin/bash
#
# deploy-geocraft-windows.sh — Deploy GeoCraft as a standalone Windows application
#
# This script packages the Tycho-built product for distribution to Windows.
# It can be run on any platform (macOS/Linux/Windows with bash).
#
# Prerequisites:
#   - Run 'mvn clean verify' to build the product
#
# Usage:
#   ./deploy-geocraft-windows.sh [export-directory]
#   Default export directory: ~/geocraft-deployed-windows/
#
# After deployment, copy the directory to a Windows machine and launch:
#   geocraft\GeoCraft.exe

set -euo pipefail

WORKSPACE="$(cd "$(dirname "$0")" && pwd)"
source "$WORKSPACE/deploy-geocraft-common.sh"

EXPORT_DIR="${1:-$HOME/geocraft-deployed-windows}"

echo "=== GeoCraft Deployment (Windows Win32 x86_64) ==="
echo "Workspace: $WORKSPACE"
echo "Export to: $EXPORT_DIR"
echo ""

find_product win32 win32 x86_64

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
echo "Copy the '$EXPORT_DIR/geocraft' directory to a Windows machine."
echo "To launch on Windows:"
echo "  geocraft\\GeoCraft.exe"
