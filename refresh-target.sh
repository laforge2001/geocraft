#!/bin/bash

# GeoCraft Target Platform Refresh Script
# Run this script whenever you encounter target platform timestamp errors

echo "🔄 Refreshing GeoCraft target platform..."

WORKSPACE_DIR="/Users/ericgeordi/dev/geocraft/geocraft"

# Step 1: Clean all PDE and p2 caches
echo "📁 Cleaning target platform caches..."
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.pde.core/.p2"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.pde.core/.local_targets"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.pde.core/.bundle_pool"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.equinox.p2.engine"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.equinox.p2.core"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.equinox.p2.metadata.repository"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.equinox.p2.artifact.repository"
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.equinox.p2.touchpoint.natives"

# Step 2: Remove target project workspace state
echo "🗂️  Cleaning target project state..."
rm -rf "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.core.resources/.projects/org.geocraft.target"

# Step 3: Remove PDE preferences
echo "⚙️  Cleaning PDE preferences..."
rm -f "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.pde.core.prefs"
rm -f "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.pde.ui.prefs"

# Step 4: Update target file timestamp
echo "🕐 Updating target file timestamp..."
touch "$WORKSPACE_DIR/org.geocraft.target/Geocraft.target"

# Step 5: Clear any cached target definitions
echo "🎯 Cleaning target definition caches..."
find "$WORKSPACE_DIR/.metadata/.plugins/org.eclipse.pde.core" -name "*target*" -exec rm -rf {} + 2>/dev/null || true

echo "✅ Target platform refresh completed!"
echo ""
echo "Next steps:"
echo "1. Restart Eclipse IDE"
echo "2. Go to Window → Preferences → Plug-in Development → Target Platform"
echo "3. Select 'Eclipse Platform 2025-09' and click 'Reload...'"
echo "4. Click 'Apply and Close'"
echo ""
echo "The target platform should now load without timestamp errors."