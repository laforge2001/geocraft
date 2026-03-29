#!/bin/bash
# GeoCraft standalone launcher - bypasses Eclipse PDE
# Uses Java 11 under Rosetta (x86_64) with Eclipse RCP 3.5.1

set -e

WORKSPACE=/Users/ericgeordi/dev/geocraft/geocraft
PLUGINS_DIR=$WORKSPACE/org.geocraft.target/EclipseRCP3.5.1/plugins
JAVA=/Library/Java/JavaVirtualMachines/jdk-11.0.1.jdk/Contents/Home/bin/java
LAUNCHER=$PLUGINS_DIR/org.eclipse.equinox.launcher_1.0.201.R35x_v20090715.jar

# Configuration area (clean each launch for debugging)
CONFIG_DIR=/tmp/geocraft-config
rm -rf "$CONFIG_DIR"
mkdir -p "$CONFIG_DIR"

# Build osgi.bundles list with reference:file: URIs
# Target platform bundles (jars) - exclude the framework jar (org.eclipse.osgi) and launcher
TARGET_BUNDLES=""
for jar in "$PLUGINS_DIR"/*.jar; do
    base=$(basename "$jar")
    # Skip the framework itself and the launcher - they're loaded separately
    case "$base" in
        org.eclipse.osgi_*) continue ;;
        org.eclipse.equinox.launcher_*) continue ;;
    esac
    # Determine start level
    entry="reference:file:$jar"
    case "$base" in
        org.eclipse.equinox.common_*) entry="$entry@2:start" ;;
        org.eclipse.equinox.ds_*) entry="$entry@1:start" ;;
        org.eclipse.core.runtime_*) entry="$entry@start" ;;
        org.eclipse.equinox.simpleconfigurator_*) entry="$entry@1:start" ;;
        org.eclipse.update.configurator_*) entry="$entry@3:start" ;;
        *) ;; # no suffix = use default start level
    esac
    TARGET_BUNDLES="${TARGET_BUNDLES},${entry}"
done

# Target platform bundles that are directories (not jars)
for dir in "$PLUGINS_DIR"/*/; do
    [ -d "$dir" ] || continue
    entry="reference:file:${dir%/}"
    TARGET_BUNDLES="${TARGET_BUNDLES},${entry}"
done

# Workspace bundles (development mode - reference:file: pointing to project dirs)
WORKSPACE_BUNDLES=""
for bundle in \
    com.ardor3d \
    com.rcpquickstart.bundletestcollector \
    org.geocraft.abavo \
    org.geocraft.abavo.product \
    org.geocraft.algorithm \
    org.geocraft.core \
    org.geocraft.core.session \
    org.geocraft.geomath \
    org.geocraft.geomath.algorithm.calculator \
    org.geocraft.geomath.algorithm.curvature \
    org.geocraft.geomath.algorithm.example \
    org.geocraft.geomath.algorithm.horizon \
    org.geocraft.geomath.algorithm.iconviewer \
    org.geocraft.geomath.algorithm.texture \
    org.geocraft.geomath.algorithm.ui \
    org.geocraft.geomath.algorithm.util \
    org.geocraft.geomath.algorithm.utilities \
    org.geocraft.geomath.algorithm.velocity \
    org.geocraft.geomath.algorithm.volume \
    org.geocraft.geomath.help \
    org.geocraft.gnuplot \
    org.geocraft.io.ascii \
    org.geocraft.io.asciigrid \
    org.geocraft.io.asciipointset \
    org.geocraft.io.gocad \
    org.geocraft.io.javaseis \
    org.geocraft.io.jms \
    org.geocraft.io.las \
    org.geocraft.io.modspec \
    org.geocraft.io.remote \
    org.geocraft.io.segy \
    org.geocraft.io.util \
    org.geocraft.math \
    org.geocraft.product \
    org.geocraft.ui.chartviewer \
    org.geocraft.ui.color \
    org.geocraft.ui.common \
    org.geocraft.ui.form2 \
    org.geocraft.ui.io \
    org.geocraft.ui.mapviewer \
    org.geocraft.ui.model \
    org.geocraft.ui.multiplot \
    org.geocraft.ui.plot \
    org.geocraft.ui.property \
    org.geocraft.ui.repository \
    org.geocraft.ui.sectionviewer \
    org.geocraft.ui.traceviewer \
    org.geocraft.ui.viewer \
    org.geocraft.ui.volumeviewer \
    org.geocraft.ui.waveletviewer \
    org.geocraft.unittest.suite \
; do
    dir="$WORKSPACE/$bundle"
    [ -d "$dir/META-INF" ] || continue
    entry="reference:file:$dir"
    case "$bundle" in
        org.geocraft.core) entry="$entry@start" ;;
        org.geocraft.ui.io) entry="$entry@start" ;;
        *) ;; # no suffix = use default start level
    esac
    WORKSPACE_BUNDLES="${WORKSPACE_BUNDLES},${entry}"
done

ALL_BUNDLES="${TARGET_BUNDLES#,}${WORKSPACE_BUNDLES}"

# Generate config.ini
cat > "$CONFIG_DIR/config.ini" << CONFIGEOF
osgi.bundles=$ALL_BUNDLES
osgi.bundles.defaultStartLevel=4
eclipse.product=org.geocraft.product.product
osgi.splashPath=platform:/base/plugins/org.geocraft.product
osgi.configuration.area=@user.home/geocraft3/global
osgi.framework=file:$PLUGINS_DIR/org.eclipse.osgi_3.5.1.R35x_v20090827.jar
org.osgi.framework.system.packages.extra=javax.xml.parsers,javax.xml.transform,javax.xml.transform.dom,javax.xml.transform.sax,javax.xml.transform.stream,org.w3c.dom,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers,javax.naming,javax.naming.directory,javax.naming.event,javax.naming.ldap,javax.naming.spi,javax.net,javax.net.ssl,javax.security.auth,javax.security.auth.callback,javax.security.auth.login,javax.security.auth.spi,javax.security.auth.x500,javax.security.cert,javax.crypto,javax.crypto.spec,javax.management,javax.management.openmbean,javax.management.remote,javax.sql,javax.transaction.xa,javax.imageio,javax.imageio.stream,javax.print,javax.print.attribute
javax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
org.osgi.framework.bootdelegation=javax.xml.*,javax.xml.transform.*,javax.xml.parsers.*,org.xml.sax.*,org.w3c.dom.*,com.sun.org.apache.xalan.*,com.sun.org.apache.xerces.*,com.sun.org.apache.xml.*
CONFIGEOF

# Generate dev.properties - tells OSGi where to find classes for workspace bundles
# In development mode, Eclipse PDE generates this to map bundle IDs to bin/ directories
DEV_PROPS="$CONFIG_DIR/dev.properties"
echo "@ignoredot@=true" > "$DEV_PROPS"
for bundle in \
    com.rcpquickstart.bundletestcollector \
    org.geocraft.abavo \
    org.geocraft.algorithm \
    org.geocraft.core \
    org.geocraft.core.session \
    org.geocraft.geomath \
    org.geocraft.geomath.algorithm.calculator \
    org.geocraft.geomath.algorithm.curvature \
    org.geocraft.geomath.algorithm.example \
    org.geocraft.geomath.algorithm.horizon \
    org.geocraft.geomath.algorithm.iconviewer \
    org.geocraft.geomath.algorithm.texture \
    org.geocraft.geomath.algorithm.ui \
    org.geocraft.geomath.algorithm.util \
    org.geocraft.geomath.algorithm.utilities \
    org.geocraft.geomath.algorithm.velocity \
    org.geocraft.geomath.algorithm.volume \
    org.geocraft.geomath.help \
    org.geocraft.gnuplot \
    org.geocraft.io.ascii \
    org.geocraft.io.asciigrid \
    org.geocraft.io.asciipointset \
    org.geocraft.io.gocad \
    org.geocraft.io.javaseis \
    org.geocraft.io.jms \
    org.geocraft.io.las \
    org.geocraft.io.modspec \
    org.geocraft.io.remote \
    org.geocraft.io.segy \
    org.geocraft.io.util \
    org.geocraft.math \
    org.geocraft.product \
    org.geocraft.ui.chartviewer \
    org.geocraft.ui.color \
    org.geocraft.ui.common \
    org.geocraft.ui.form2 \
    org.geocraft.ui.io \
    org.geocraft.ui.mapviewer \
    org.geocraft.ui.model \
    org.geocraft.ui.multiplot \
    org.geocraft.ui.plot \
    org.geocraft.ui.property \
    org.geocraft.ui.repository \
    org.geocraft.ui.sectionviewer \
    org.geocraft.ui.traceviewer \
    org.geocraft.ui.viewer \
    org.geocraft.ui.volumeviewer \
    org.geocraft.ui.waveletviewer \
    org.geocraft.unittest.suite \
; do
    echo "$bundle=bin" >> "$DEV_PROPS"
done

echo ""
echo "=== GeoCraft Launch ==="
echo "Java: $($JAVA -version 2>&1 | head -1)"
echo "Config: $CONFIG_DIR/config.ini"
echo ""

# Launch with Rosetta (x86_64) since SWT is x86_64
exec arch -x86_64 "$JAVA" \
    -Xms256m \
    -Xmx1200m \
    -XstartOnFirstThread \
    -Dorg.eclipse.swt.internal.carbon.smallFonts \
    '-Dorg.osgi.framework.executionenvironment=OSGi/Minimum-1.0,OSGi/Minimum-1.1,JRE-1.1,J2SE-1.2,J2SE-1.3,J2SE-1.4,J2SE-1.5,JavaSE-1.6,JavaSE-1.7,JavaSE-1.8,JavaSE-9,JavaSE-10,JavaSE-11,CDC-1.0/Foundation-1.0,CDC-1.1/Foundation-1.1' \
    -Dorg.osgi.framework.system.packages.extra=javax.xml.parsers,javax.xml.transform,javax.xml.transform.dom,javax.xml.transform.sax,javax.xml.transform.stream,org.w3c.dom,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers,javax.naming,javax.naming.directory,javax.naming.event,javax.naming.ldap,javax.naming.spi,javax.net,javax.net.ssl,javax.security.auth,javax.security.auth.callback,javax.security.auth.login,javax.security.auth.spi,javax.security.auth.x500,javax.security.cert,javax.crypto,javax.crypto.spec,javax.management,javax.management.openmbean,javax.management.remote,javax.sql,javax.transaction.xa,javax.imageio,javax.imageio.stream,javax.print,javax.print.attribute \
    -Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl \
    -Dorg.osgi.framework.bootdelegation=javax.xml.*,javax.xml.transform.*,javax.xml.parsers.*,org.xml.sax.*,org.w3c.dom.*,com.sun.org.apache.xalan.*,com.sun.org.apache.xerces.*,com.sun.org.apache.xml.* \
    -jar "$LAUNCHER" \
    -os macosx \
    -ws cocoa \
    -arch x86_64 \
    -nl en_US \
    -console \
    -consoleLog \
    -configuration "file:$CONFIG_DIR" \
    -dev "file:$DEV_PROPS" \
    -product org.geocraft.product.product \
    -data /tmp/geocraft-workspace \
    2>&1
