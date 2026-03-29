#!/bin/bash
#
# deploy-geocraft-common.sh — Shared functions for GeoCraft deployment scripts
#
# Sourced by platform-specific deploy scripts. Do not run directly.

# Copy target platform plugins, filtering by platform
# Args: $1=PLUGINS_DIR, $2=TARGET_PLUGINS, $3=os, $4=ws, $5=arch
copy_target_plugins() {
  local PLUGINS_DIR="$1"
  local TARGET_PLUGINS="$2"
  local PLAT_OS="$3"
  local PLAT_WS="$4"
  local PLAT_ARCH="$5"
  local TARGET_COUNT=0

  echo "--- Copying target platform plugins ---"
  for jar in "$TARGET_PLUGINS"/*; do
    local name=$(basename "$jar")
    # Skip plugins with unresolvable deps or test-only plugins
    case "$name" in
      org.eclipse.core.net*|org.junit*|org.hamcrest*)
        echo "  SKIP: $name"
        continue
        ;;
    esac
    # Skip SWT/launcher fragments for OTHER platforms
    case "$name" in
      org.eclipse.swt.cocoa.*|org.eclipse.swt.gtk.*|org.eclipse.swt.win32.*)
        # Only include the fragment matching our target platform
        if [[ "$name" != *"${PLAT_WS}.${PLAT_OS}.${PLAT_ARCH}"* ]] && \
           [[ "$name" != *"${PLAT_WS}.${PLAT_WS}.${PLAT_ARCH}"* ]] && \
           [[ "$name" != *"${PLAT_OS}.${PLAT_ARCH}"* ]]; then
          echo "  SKIP (other platform): $name"
          continue
        fi
        ;;
      org.eclipse.equinox.launcher.cocoa.*|org.eclipse.equinox.launcher.gtk.*|org.eclipse.equinox.launcher.win32.*)
        if [[ "$name" != *"${PLAT_WS}.${PLAT_OS}.${PLAT_ARCH}"* ]] && \
           [[ "$name" != *"${PLAT_WS}.${PLAT_WS}.${PLAT_ARCH}"* ]] && \
           [[ "$name" != *"${PLAT_OS}.${PLAT_ARCH}"* ]]; then
          echo "  SKIP (other platform): $name"
          continue
        fi
        ;;
    esac
    cp -R "$jar" "$PLUGINS_DIR/"
    TARGET_COUNT=$((TARGET_COUNT + 1))
  done
  echo "  Copied $TARGET_COUNT target platform plugins"
  echo ""
}

# Copy workspace plugins referenced by features
# Args: $1=PLUGINS_DIR, $2=WORKSPACE
copy_workspace_plugins() {
  local PLUGINS_DIR="$1"
  local WORKSPACE="$2"
  local WS_COUNT=0
  local WS_FAIL=0

  echo "--- Copying workspace plugins ---"

  # Collect all plugin IDs from all 4 feature.xml files
  local FEATURE_PLUGINS=$(grep -h 'id="' \
    "$WORKSPACE"/org.geocraft.feature/feature.xml \
    "$WORKSPACE"/org.geocraft.geomath.feature/feature.xml \
    "$WORKSPACE"/org.geocraft.abavo.feature/feature.xml \
    "$WORKSPACE"/org.geocraft.ui.viewer.feature/feature.xml \
    | sed -n 's/.*id="\([^"]*\)".*/\1/p' | sort -u)

  for plugin_id in $FEATURE_PLUGINS; do
    local plugin_dir="$WORKSPACE/$plugin_id"

    # Skip if already present from target platform
    if ls "$PLUGINS_DIR/${plugin_id}_"* 1>/dev/null 2>&1; then
      continue
    fi

    # Skip feature directories (no MANIFEST.MF)
    if [ ! -f "$plugin_dir/META-INF/MANIFEST.MF" ]; then
      continue
    fi

    if [ ! -d "$plugin_dir" ]; then
      echo "  MISSING: $plugin_id (no workspace directory)"
      WS_FAIL=$((WS_FAIL + 1))
      continue
    fi

    # Check that Eclipse has compiled this plugin
    if [ ! -d "$plugin_dir/bin" ]; then
      echo "  WARNING: $plugin_id has no bin/ directory — not compiled in Eclipse?"
    fi

    local DEST="$PLUGINS_DIR/${plugin_id}_1.0.0"
    mkdir -p "$DEST"

    # Copy everything except source, build artifacts, and IDE files
    for item in "$plugin_dir"/*; do
      local item_name=$(basename "$item")
      case "$item_name" in
        src|bin|.settings|.classpath|.project|build.properties|.gitignore|pom.xml|feature.xml)
          continue ;;
      esac
      cp -R "$item" "$DEST/"
    done
    # Copy compiled classes from bin/ on top
    if [ -d "$plugin_dir/bin" ]; then
      cp -R "$plugin_dir/bin/"* "$DEST/" 2>/dev/null || true
    fi

    WS_COUNT=$((WS_COUNT + 1))
  done

  echo "  Copied $WS_COUNT workspace plugins"
  if [ $WS_FAIL -gt 0 ]; then
    echo "  WARNING: $WS_FAIL plugins could not be found"
  fi
  echo ""
}

# Copy configuration and branding
# Args: $1=CONFIG_DIR, $2=PLUGINS_DIR, $3=WORKSPACE
copy_config_and_branding() {
  local CONFIG_DIR="$1"
  local PLUGINS_DIR="$2"
  local WORKSPACE="$3"

  echo "--- Setting up configuration ---"
  cp "$WORKSPACE/org.geocraft.product/config.ini" "$CONFIG_DIR/config.ini"
  echo "  Copied config.ini"

  # Ensure branding resources
  local PRODUCT_DEST="$PLUGINS_DIR/org.geocraft.product_1.0.0"
  if [ -d "$PRODUCT_DEST" ]; then
    cp "$WORKSPACE/org.geocraft.product/splash.bmp" "$PRODUCT_DEST/" 2>/dev/null || true
    mkdir -p "$PRODUCT_DEST/icons"
    cp "$WORKSPACE/org.geocraft.product/icons/"* "$PRODUCT_DEST/icons/" 2>/dev/null || true
  fi
}
