#!/bin/bash
#
# deploy-geocraft-common.sh — Shared functions for GeoCraft deployment scripts
#
# Sourced by platform-specific deploy scripts. Do not run directly.
#
# These scripts package the Tycho-built product for distribution.
# Prerequisites: Run 'mvn clean verify' first to build the product.

# Locate the Tycho-materialized product for a given platform
# Args: $1=os, $2=ws, $3=arch
# Sets: PRODUCT_DIR (path to the materialized product directory)
find_product() {
  local PLAT_OS="$1"
  local PLAT_WS="$2"
  local PLAT_ARCH="$3"

  PRODUCT_DIR="$WORKSPACE/org.geocraft.repository/target/products/org.geocraft.product.product/$PLAT_OS/$PLAT_WS/$PLAT_ARCH"

  if [ ! -d "$PRODUCT_DIR" ]; then
    echo "ERROR: Product not found at $PRODUCT_DIR"
    echo "Run 'mvn clean verify' first to build the product."
    exit 1
  fi
}
