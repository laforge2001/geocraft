# Eclipse 4 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate GeoCraft from Eclipse 3.5.1 to Eclipse 2025-12 (4.34) with Tycho 5.0.2 build, JDK 21, native Apple Silicon support.

**Architecture:** Compatibility-first migration using Eclipse 3.x compat layer (ships with Eclipse 4.x). Replace checked-in Eclipse binaries with declarative P2-based target platform. Add Tycho/Maven build. Fix 5 files with internal API usage. Update all bundle manifests to JavaSE-21.

**Tech Stack:** Eclipse RCP 2025-12 (4.34), JDK 21 Temurin, Tycho 5.0.2, Maven 3.9+, SWT cocoa aarch64

**Spec:** `docs/superpowers/specs/2026-04-01-eclipse4-migration-design.md`

---

## File Structure

### New files
- `pom.xml` (parent POM) - Tycho build configuration, module list, P2 repos
- `org.geocraft.target/pom.xml` - target definition project POM
- `org.geocraft.target/Geocraft-e4.target` - new Eclipse 4 target platform definition
- One `pom.xml` per bundle/feature/product (~55 files, all boilerplate)

### Modified files
- All `META-INF/MANIFEST.MF` files (~55) - BREE update to JavaSE-21, version constraint updates
- All `.classpath` files (~55) - JRE container update
- `org.geocraft.product/src/org/geocraft/Application.java` - remove internal API
- `org.geocraft.ui.viewer/src/org/geocraft/ui/viewer/ViewerHelper.java` - remove internal API
- `org.geocraft.ui.property/src/org/geocraft/ui/property/PropertiesView.java` - remove internal API
- `org.geocraft.core.session/src/org/geocraft/core/session/SessionManager.java` - remove internal API
- `org.geocraft.core.session/src/org/geocraft/core/session/Session.java` - remove internal API
- `org.geocraft.feature/feature.xml` - update for Eclipse 4 bundles
- `org.geocraft.product/GeoCraft.product` - update for Eclipse 4
- `launch-geocraft.sh` - rewrite for Eclipse 4 + JDK 21 native
- `.gitignore` - add EclipseRCP3.5.1 directory

### Removed files
- `org.geocraft.target/EclipseRCP3.5.1/` directory (after migration verified)

---

## Task 1: Create Parent POM and Tycho Build Infrastructure

**Files:**
- Create: `pom.xml`

- [ ] **Step 1.1: Create parent POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.geocraft</groupId>
  <artifactId>org.geocraft.parent</artifactId>
  <version>1.0.0-SNAPSHOT</version>
  <packaging>pom</packaging>
  <name>GeoCraft Parent</name>

  <properties>
    <tycho.version>5.0.2</tycho.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
  </properties>

  <repositories>
    <repository>
      <id>eclipse-2025-12</id>
      <url>https://download.eclipse.org/releases/2025-12</url>
      <layout>p2</layout>
    </repository>
    <repository>
      <id>eclipse-orbit-2025-12</id>
      <url>https://download.eclipse.org/tools/orbit/simrel/orbit-aggregation/2025-12</url>
      <layout>p2</layout>
    </repository>
  </repositories>

  <build>
    <plugins>
      <plugin>
        <groupId>org.eclipse.tycho</groupId>
        <artifactId>tycho-maven-plugin</artifactId>
        <version>${tycho.version}</version>
        <extensions>true</extensions>
      </plugin>
      <plugin>
        <groupId>org.eclipse.tycho</groupId>
        <artifactId>target-platform-configuration</artifactId>
        <version>${tycho.version}</version>
        <configuration>
          <target>
            <file>org.geocraft.target/Geocraft-e4.target</file>
          </target>
          <environments>
            <environment>
              <os>macosx</os>
              <ws>cocoa</ws>
              <arch>aarch64</arch>
            </environment>
            <environment>
              <os>linux</os>
              <ws>gtk</ws>
              <arch>x86_64</arch>
            </environment>
            <environment>
              <os>win32</os>
              <ws>win32</ws>
              <arch>x86_64</arch>
            </environment>
          </environments>
        </configuration>
      </plugin>
    </plugins>
  </build>

  <modules>
    <!-- Target platform -->
    <module>org.geocraft.target</module>

    <!-- Third-party bundles -->
    <module>com.ardor3d</module>
    <module>com.rcpquickstart.bundletestcollector</module>

    <!-- Core bundles -->
    <module>org.geocraft.core</module>
    <module>org.geocraft.core.session</module>
    <module>org.geocraft.math</module>
    <module>org.geocraft.algorithm</module>

    <!-- IO bundles -->
    <module>org.geocraft.io.ascii</module>
    <module>org.geocraft.io.asciigrid</module>
    <module>org.geocraft.io.asciipointset</module>
    <module>org.geocraft.io.gocad</module>
    <module>org.geocraft.io.javaseis</module>
    <module>org.geocraft.io.jms</module>
    <module>org.geocraft.io.las</module>
    <module>org.geocraft.io.modspec</module>
    <module>org.geocraft.io.remote</module>
    <module>org.geocraft.io.segy</module>
    <module>org.geocraft.io.util</module>

    <!-- GeoMath bundles -->
    <module>org.geocraft.geomath</module>
    <module>org.geocraft.geomath.algorithm.calculator</module>
    <module>org.geocraft.geomath.algorithm.curvature</module>
    <module>org.geocraft.geomath.algorithm.example</module>
    <module>org.geocraft.geomath.algorithm.horizon</module>
    <module>org.geocraft.geomath.algorithm.iconviewer</module>
    <module>org.geocraft.geomath.algorithm.texture</module>
    <module>org.geocraft.geomath.algorithm.ui</module>
    <module>org.geocraft.geomath.algorithm.util</module>
    <module>org.geocraft.geomath.algorithm.utilities</module>
    <module>org.geocraft.geomath.algorithm.velocity</module>
    <module>org.geocraft.geomath.algorithm.volume</module>
    <module>org.geocraft.geomath.help</module>

    <!-- UI bundles -->
    <module>org.geocraft.ui.chartviewer</module>
    <module>org.geocraft.ui.color</module>
    <module>org.geocraft.ui.common</module>
    <module>org.geocraft.ui.form2</module>
    <module>org.geocraft.ui.io</module>
    <module>org.geocraft.ui.mapviewer</module>
    <module>org.geocraft.ui.model</module>
    <module>org.geocraft.ui.multiplot</module>
    <module>org.geocraft.ui.plot</module>
    <module>org.geocraft.ui.property</module>
    <module>org.geocraft.ui.repository</module>
    <module>org.geocraft.ui.sectionviewer</module>
    <module>org.geocraft.ui.traceviewer</module>
    <module>org.geocraft.ui.viewer</module>
    <module>org.geocraft.ui.volumeviewer</module>
    <module>org.geocraft.ui.waveletviewer</module>

    <!-- Misc bundles -->
    <module>org.geocraft.gnuplot</module>
    <module>org.geocraft.unittest.suite</module>

    <!-- Product and features -->
    <module>org.geocraft.product</module>
    <module>org.geocraft.abavo</module>
    <module>org.geocraft.abavo.product</module>
    <module>org.geocraft.feature</module>
    <module>org.geocraft.geomath.feature</module>
    <module>org.geocraft.abavo.feature</module>
    <module>org.geocraft.ui.viewer.feature</module>
  </modules>
</project>
```

Write this to `pom.xml` at the repo root.

- [ ] **Step 1.2: Verify Maven is available**

Run: `mvn --version`
Expected: Maven 3.9+ with Java 21

If Maven is not installed or wrong Java, install via: `brew install maven` and ensure `JAVA_HOME` points to JDK 21.

- [ ] **Step 1.3: Commit**

```bash
git add pom.xml
git commit -m "Add parent POM with Tycho 5.0.2 build configuration"
```

---

## Task 2: Create Eclipse 4 Target Platform Definition

**Files:**
- Create: `org.geocraft.target/pom.xml`
- Create: `org.geocraft.target/Geocraft-e4.target`

- [ ] **Step 2.1: Create target platform POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>

  <artifactId>org.geocraft.target</artifactId>
  <packaging>eclipse-target-definition</packaging>
  <name>GeoCraft Target Platform</name>
</project>
```

Write to `org.geocraft.target/pom.xml`.

- [ ] **Step 2.2: Create Eclipse 4 target definition**

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<?pde version="3.8"?>
<target name="GeoCraft Eclipse 2025-12" sequenceNumber="1">
  <locations>
    <location includeAllPlatforms="false" includeConfigurePhase="true" includeMode="planner" includeSource="false" type="InstallableUnit">
      <repository location="https://download.eclipse.org/releases/2025-12"/>
      <unit id="org.eclipse.rcp.feature.group" version="0.0.0"/>
      <unit id="org.eclipse.equinox.p2.core.feature.feature.group" version="0.0.0"/>
      <unit id="org.eclipse.help.feature.group" version="0.0.0"/>
      <unit id="org.eclipse.equinox.executable.feature.group" version="0.0.0"/>
      <unit id="org.eclipse.platform.feature.group" version="0.0.0"/>
    </location>
    <location includeAllPlatforms="false" includeConfigurePhase="true" includeMode="planner" includeSource="false" type="InstallableUnit">
      <repository location="https://download.eclipse.org/tools/orbit/simrel/orbit-aggregation/2025-12"/>
      <unit id="org.apache.commons.beanutils" version="0.0.0"/>
      <unit id="org.apache.commons.collections" version="0.0.0"/>
    </location>
    <location path="${project_loc}/jtk.edu.mines.boole/plugins/jtk.edu.mines.boole_1.0.0.201001131316" type="Directory"/>
  </locations>
  <environment>
    <os>macosx</os>
    <ws>cocoa</ws>
    <arch>aarch64</arch>
  </environment>
  <targetJRE path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-21"/>
</target>
```

Write to `org.geocraft.target/Geocraft-e4.target`.

- [ ] **Step 2.3: Commit**

```bash
git add org.geocraft.target/pom.xml org.geocraft.target/Geocraft-e4.target
git commit -m "Add Eclipse 2025-12 target platform definition with Orbit dependencies"
```

---

## Task 3: Create Module POMs for All Bundles

**Files:**
- Create: `pom.xml` in each of ~55 bundle/feature/product directories

Each module POM follows this template (adjust `artifactId` and `packaging` per module):

**Plugin POM template:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>${bundle.symbolic.name}</artifactId>
  <packaging>eclipse-plugin</packaging>
</project>
```

**Feature POM template** (same but `<packaging>eclipse-feature</packaging>`).

- [ ] **Step 3.1: Create plugin POMs**

Create `pom.xml` for each of these plugin directories using the plugin template above, setting `<artifactId>` to the directory name:

- `com.ardor3d`
- `com.rcpquickstart.bundletestcollector`
- `org.geocraft.abavo`
- `org.geocraft.abavo.product`
- `org.geocraft.algorithm`
- `org.geocraft.core`
- `org.geocraft.core.session`
- `org.geocraft.geomath`
- `org.geocraft.geomath.algorithm.calculator`
- `org.geocraft.geomath.algorithm.curvature`
- `org.geocraft.geomath.algorithm.example`
- `org.geocraft.geomath.algorithm.horizon`
- `org.geocraft.geomath.algorithm.iconviewer`
- `org.geocraft.geomath.algorithm.texture`
- `org.geocraft.geomath.algorithm.ui`
- `org.geocraft.geomath.algorithm.util`
- `org.geocraft.geomath.algorithm.utilities`
- `org.geocraft.geomath.algorithm.velocity`
- `org.geocraft.geomath.algorithm.volume`
- `org.geocraft.geomath.help`
- `org.geocraft.gnuplot`
- `org.geocraft.io.ascii`
- `org.geocraft.io.asciigrid`
- `org.geocraft.io.asciipointset`
- `org.geocraft.io.gocad`
- `org.geocraft.io.javaseis`
- `org.geocraft.io.jms`
- `org.geocraft.io.las`
- `org.geocraft.io.modspec`
- `org.geocraft.io.remote`
- `org.geocraft.io.segy`
- `org.geocraft.io.util`
- `org.geocraft.math`
- `org.geocraft.product`
- `org.geocraft.ui.chartviewer`
- `org.geocraft.ui.color`
- `org.geocraft.ui.common`
- `org.geocraft.ui.form2`
- `org.geocraft.ui.io`
- `org.geocraft.ui.mapviewer`
- `org.geocraft.ui.model`
- `org.geocraft.ui.multiplot`
- `org.geocraft.ui.plot`
- `org.geocraft.ui.property`
- `org.geocraft.ui.repository`
- `org.geocraft.ui.sectionviewer`
- `org.geocraft.ui.traceviewer`
- `org.geocraft.ui.viewer`
- `org.geocraft.ui.volumeviewer`
- `org.geocraft.ui.waveletviewer`
- `org.geocraft.unittest.suite`

- [ ] **Step 3.2: Create feature POMs**

Create `pom.xml` for each feature directory using the feature template (`eclipse-feature` packaging):

- `org.geocraft.feature`
- `org.geocraft.geomath.feature`
- `org.geocraft.abavo.feature`
- `org.geocraft.ui.viewer.feature`

- [ ] **Step 3.3: Commit**

```bash
git add */pom.xml
git commit -m "Add Tycho module POMs for all bundles and features"
```

---

## Task 4: Update All Bundle Manifests for Java 21

**Files:**
- Modify: All `META-INF/MANIFEST.MF` files (~55 bundles)

- [ ] **Step 4.1: Update BREE in all manifests**

For every `MANIFEST.MF` in the workspace (excluding `org.geocraft.target/EclipseRCP3.5.1/`), replace:
```
Bundle-RequiredExecutionEnvironment: JavaSE-1.8
```
with:
```
Bundle-RequiredExecutionEnvironment: JavaSE-21
```

Also replace any `J2SE-1.5` or `JavaSE-1.6` BREE values with `JavaSE-21`.

- [ ] **Step 4.2: Remove version constraints on Eclipse bundles**

In all `MANIFEST.MF` files, remove the `bundle-version` constraints on Eclipse platform bundles since the target platform pins the versions. Change patterns like:

```
 org.eclipse.core.runtime;bundle-version="3.4.0"
```
to:
```
 org.eclipse.core.runtime
```

Do this for all `org.eclipse.*` dependencies. Keep version constraints on `org.geocraft.*` bundles.

- [ ] **Step 4.3: Update .classpath files**

For every `.classpath` file in the workspace, replace the JRE container entry:
```xml
<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8"/>
```
with:
```xml
<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-21"/>
```

Also handle variants like `J2SE-1.5` or `JavaSE-1.6`.

- [ ] **Step 4.4: Verify no BREE or classpath entries were missed**

Run: `grep -r "JavaSE-1\." --include="MANIFEST.MF" --include=".classpath" . | grep -v EclipseRCP3.5.1 | grep -v target`
Expected: No output (all updated)

- [ ] **Step 4.5: Commit**

```bash
git add -A
git commit -m "Update all bundles to JavaSE-21 execution environment"
```

---

## Task 5: Fix Internal API Usage - Application.java

**Files:**
- Modify: `org.geocraft.product/src/org/geocraft/Application.java`

The file uses `WorkbenchPlugin.getSplashShell(display)` (line 61) to get the splash shell during startup. In Eclipse 4.x, the splash shell is managed by the application context.

- [ ] **Step 5.1: Replace WorkbenchPlugin.getSplashShell**

In `org.geocraft.product/src/org/geocraft/Application.java`, replace:

```java
import org.eclipse.ui.internal.WorkbenchPlugin;
```

with nothing (remove the import).

Replace lines 59-65:
```java
      // this is currently discouraged... do we have a way around it?
      Shell shell = WorkbenchPlugin.getSplashShell(display);
      if (shell != null) {
        shell.setText(ChooseWorkspaceDialog.getWindowTitle());
        shell.setImages(Window.getDefaultImages());
      }
```

with:
```java
      Shell shell = display.getActiveShell();
      if (shell == null) {
        shell = new Shell(display);
      }
      shell.setText(ChooseWorkspaceDialog.getWindowTitle());
      shell.setImages(Window.getDefaultImages());
```

- [ ] **Step 5.2: Verify no internal imports remain**

Run: `grep "org.eclipse.ui.internal" org.geocraft.product/src/org/geocraft/Application.java`
Expected: No output

- [ ] **Step 5.3: Commit**

```bash
git add org.geocraft.product/src/org/geocraft/Application.java
git commit -m "Replace WorkbenchPlugin.getSplashShell with public Display API"
```

---

## Task 6: Fix Internal API Usage - ViewerHelper.java

**Files:**
- Modify: `org.geocraft.ui.viewer/src/org/geocraft/ui/viewer/ViewerHelper.java`

Line 57 casts `IWorkbench` to internal `Workbench` to call `getDefaultPageInput()`. In Eclipse 4.x, we can pass `null` as the input to `openWorkbenchWindow()` - Eclipse will use its default.

- [ ] **Step 6.1: Replace Workbench cast**

In `ViewerHelper.java`, remove the import:
```java
import org.eclipse.ui.internal.Workbench;
```

And remove the unused import (will no longer be needed after the change):
```java
import org.eclipse.core.runtime.IAdaptable;
```

Replace the `initViewerPerspective()` method (lines 55-63):
```java
  private static void initViewerPerspective() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IAdaptable input = ((Workbench) workbench).getDefaultPageInput();
    try {
      _plotWindow = workbench.openWorkbenchWindow("Viewer.perspective", input);
    } catch (WorkbenchException e) {
      ServiceProvider.getLoggingService().getLogger(ViewerHelper.class).warn("Cannot open viewer perspective", e);
    }
  }
```

with:
```java
  private static void initViewerPerspective() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    try {
      _plotWindow = workbench.openWorkbenchWindow("Viewer.perspective", null);
    } catch (WorkbenchException e) {
      ServiceProvider.getLoggingService().getLogger(ViewerHelper.class).warn("Cannot open viewer perspective", e);
    }
  }
```

- [ ] **Step 6.2: Verify no internal imports remain**

Run: `grep "org.eclipse.ui.internal" org.geocraft.ui.viewer/src/org/geocraft/ui/viewer/ViewerHelper.java`
Expected: No output

- [ ] **Step 6.3: Commit**

```bash
git add org.geocraft.ui.viewer/src/org/geocraft/ui/viewer/ViewerHelper.java
git commit -m "Replace internal Workbench cast with null input in ViewerHelper"
```

---

## Task 7: Fix Internal API Usage - PropertiesView.java

**Files:**
- Modify: `org.geocraft.ui.property/src/org/geocraft/ui/property/PropertiesView.java`

Lines 196 and 214 use `ViewsPlugin.getViewImageDescriptor()` to load toolbar icons from the internal views plugin. Replace with loading icons from the plugin's own bundle or using platform shared images.

- [ ] **Step 7.1: Replace ViewsPlugin image loading**

In `PropertiesView.java`, remove the import:
```java
import org.eclipse.ui.internal.views.ViewsPlugin;
```

Add this import:
```java
import org.eclipse.jface.resource.ImageDescriptor;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
```

Replace line 196:
```java
    _showCategoriesAction.setImageDescriptor(ViewsPlugin.getViewImageDescriptor("elcl16/tree_mode.gif"));
```
with:
```java
    _showCategoriesAction.setImageDescriptor(getLocalImageDescriptor("icons/tree_mode.gif"));
```

Replace line 214:
```java
    _showTreeAction.setImageDescriptor(ViewsPlugin.getViewImageDescriptor("elcl16/filter_ps.gif"));
```
with:
```java
    _showTreeAction.setImageDescriptor(getLocalImageDescriptor("icons/filter_ps.gif"));
```

Add this helper method to the class:
```java
  private static ImageDescriptor getLocalImageDescriptor(String path) {
    Bundle bundle = FrameworkUtil.getBundle(PropertiesView.class);
    URL url = FileLocator.find(bundle, new Path(path), null);
    if (url != null) {
      return ImageDescriptor.createFromURL(url);
    }
    return ImageDescriptor.getMissingImageDescriptor();
  }
```

- [ ] **Step 7.2: Copy the icon files into the plugin**

Copy the two GIF icons from the Eclipse views plugin into the PropertiesView bundle:

```bash
mkdir -p org.geocraft.ui.property/icons
```

Create simple 16x16 placeholder icons (these are standard Eclipse icons - tree_mode.gif and filter_ps.gif). The actual icons can be extracted from the Eclipse platform later, or we can use PlatformUI shared images as alternatives:

Alternative approach if icons are not available - use platform shared images instead:
```java
    _showCategoriesAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
        .getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_OBJ_ELEMENT));
```
```java
    _showTreeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
        .getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_ELCL_SYNCED));
```

Use the platform shared images approach since it avoids needing to ship the icons.

- [ ] **Step 7.3: Update build.properties**

Add `icons/` to `bin.includes` in `org.geocraft.ui.property/build.properties` if using local icons.

- [ ] **Step 7.4: Verify no internal imports remain**

Run: `grep "org.eclipse.ui.internal" org.geocraft.ui.property/src/org/geocraft/ui/property/PropertiesView.java`
Expected: No output

- [ ] **Step 7.5: Commit**

```bash
git add org.geocraft.ui.property/
git commit -m "Replace ViewsPlugin internal API with platform shared images in PropertiesView"
```

---

## Task 8: Fix Internal API Usage - SessionManager.java

**Files:**
- Modify: `org.geocraft.core.session/src/org/geocraft/core/session/SessionManager.java`

This file has the heaviest internal API usage: `IWorkbenchConstants.TAG_WINDOW`, `WorkbenchWindow.saveState()`, `WorkbenchWindow.restoreState()`, `Workbench` cast. These are used for serializing/restoring workbench window layouts.

Strategy: Since `WorkbenchWindow.saveState(IMemento)` and `restoreState(IMemento, IPerspectiveDescriptor)` are internal APIs with no public equivalent, we'll use `@SuppressWarnings("restriction")` and access them via the compatibility layer which still exposes these methods. Eclipse 4.x compat layer preserves the `WorkbenchWindow` class. We'll minimize the internal API surface by removing the `IWorkbenchConstants` import and hardcoding the string constant.

- [ ] **Step 8.1: Remove IWorkbenchConstants import**

In `SessionManager.java`, remove:
```java
import org.eclipse.ui.internal.IWorkbenchConstants;
```

Replace line 433:
```java
      XMLMemento memento = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_WINDOW);
```
with:
```java
      XMLMemento memento = XMLMemento.createWriteRoot("window");
```

- [ ] **Step 8.2: Replace Workbench cast at line 647**

Replace lines 647-648:
```java
      Workbench workbench = (Workbench) PlatformUI.getWorkbench();
      IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
```
with:
```java
      IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
```

Remove the import:
```java
import org.eclipse.ui.internal.Workbench;
```

- [ ] **Step 8.3: Add @SuppressWarnings for remaining WorkbenchWindow usage**

The `WorkbenchWindow.saveState()` and `restoreState()` casts at lines 434, 1259, 1260 have no public API replacement. Add `@SuppressWarnings("restriction")` to the methods containing them:

- The `saveSession(String pathname)` method (contains line 434)
- The `restoreWindowState(...)` method (contains lines 1259-1260) - already has `@SuppressWarnings("restriction")`

The `WorkbenchWindow` import stays since it's still needed:
```java
import org.eclipse.ui.internal.WorkbenchWindow;
```

- [ ] **Step 8.4: Verify reduced internal API surface**

Run: `grep "org.eclipse.ui.internal" org.geocraft.core.session/src/org/geocraft/core/session/SessionManager.java`
Expected: Only `import org.eclipse.ui.internal.WorkbenchWindow;` remains (IWorkbenchConstants and Workbench removed)

- [ ] **Step 8.5: Commit**

```bash
git add org.geocraft.core.session/src/org/geocraft/core/session/SessionManager.java
git commit -m "Reduce internal API usage in SessionManager - keep WorkbenchWindow for state serialization"
```

---

## Task 9: Fix Internal API Usage - Session.java

**Files:**
- Modify: `org.geocraft.core.session/src/org/geocraft/core/session/Session.java`

Same pattern as SessionManager. The `Workbench` cast can be removed. `WorkbenchWindow` casts for `saveState`/`restoreState` must remain with `@SuppressWarnings`.

- [ ] **Step 9.1: Replace Workbench cast in restoreWorkbenchState**

In `Session.java`, replace lines 287-289 in `restoreWorkbenchState()`:
```java
    Workbench workbench = (Workbench) PlatformUI.getWorkbench();
    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
    int numWindows = workbench.getWorkbenchWindowCount();
```
with:
```java
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    int numWindows = windows.length;
```

- [ ] **Step 9.2: Replace Workbench.getDefaultPageInput at line 312**

Replace line 312:
```java
        IAdaptable input = workbench.getDefaultPageInput();
```
with:
```java
        IAdaptable input = null;
```

Remove the import:
```java
import org.eclipse.ui.internal.Workbench;
```

The `IAdaptable` import can also be removed if no longer used elsewhere in the file. Check first.

- [ ] **Step 9.3: Add @SuppressWarnings to methods with WorkbenchWindow casts**

Add `@SuppressWarnings("restriction")` to:
- `saveWorkbenchState()` method (line 187 area)
- `restoreWorkbenchState()` method (line 286 area)

The `WorkbenchWindow` import stays:
```java
import org.eclipse.ui.internal.WorkbenchWindow;
```

- [ ] **Step 9.4: Verify reduced internal API surface**

Run: `grep "org.eclipse.ui.internal" org.geocraft.core.session/src/org/geocraft/core/session/Session.java`
Expected: Only `import org.eclipse.ui.internal.WorkbenchWindow;` remains

- [ ] **Step 9.5: Commit**

```bash
git add org.geocraft.core.session/src/org/geocraft/core/session/Session.java
git commit -m "Reduce internal API usage in Session - keep WorkbenchWindow for state serialization"
```

---

## Task 10: Update Feature Definitions for Eclipse 4

**Files:**
- Modify: `org.geocraft.feature/feature.xml`

The feature currently lists individual Eclipse platform plugins explicitly. For Eclipse 4.x, several of these have been renamed, merged, or removed. The feature should include `org.eclipse.rcp` feature as an included feature and remove individual Eclipse plugin entries that are now provided transitively.

- [ ] **Step 10.1: Simplify feature.xml**

In `org.geocraft.feature/feature.xml`, make these changes:

1. Remove all individual `org.eclipse.*` plugin entries (lines 20-410) - these are provided by the Eclipse RCP feature and target platform
2. Remove `javax.servlet`, `javax.servlet.jsp`, `org.apache.commons.el`, `org.apache.commons.logging`, `org.apache.jasper`, `org.apache.lucene*`, `org.mortbay.jetty*` entries - these help system dependencies are resolved via the target platform
3. Add an `<includes>` for the Eclipse RCP feature
4. Keep all `org.geocraft.*` and `com.ardor3d` and `jtk.*` plugin entries
5. Replace `org.eclipse.swt.cocoa.macosx.x86_64` with architecture-independent `org.eclipse.swt` (platform fragments are resolved by the target platform)

The updated feature.xml should look like:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<feature
      id="org.geocraft.feature"
      label="GeoCraft Feature"
      version="1.0.0"
      provider-name="ConocoPhillips">

   <description url="http://www.example.com/description">
      [Enter Feature Description here.]
   </description>

   <copyright url="http://www.example.com/copyright">
      [Enter Copyright Description here.]
   </copyright>

   <license url="http://www.example.com/license">
      [Enter License Description here.]
   </license>

   <requires>
      <import feature="org.eclipse.rcp" version="4.34.0" match="greaterOrEqual"/>
      <import feature="org.eclipse.help" version="2.4.0" match="greaterOrEqual"/>
   </requires>

   <plugin
         id="org.apache.commons.collections"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.apache.commons.beanutils"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="jtk.edu.mines.boole"
         download-size="0"
         install-size="0"
         version="0.0.0"/>

   <plugin
         id="com.ardor3d"
         download-size="0"
         install-size="0"
         version="0.0.0"/>

   <plugin
         id="com.rcpquickstart.bundletestcollector"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.core"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.core.session"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.math"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.algorithm"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.product"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.ascii"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.asciigrid"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.asciipointset"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.gocad"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.javaseis"
         os="linux"
         ws="gtk"
         download-size="0"
         install-size="0"
         version="0.0.0"/>

   <plugin
         id="org.geocraft.io.jms"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.las"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.modspec"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.remote"
         os="linux"
         ws="gtk"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.segy"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.io.util"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.gnuplot"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.common"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.color"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.form2"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.io"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.model"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.multiplot"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.plot"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.property"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.ui.repository"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

   <plugin
         id="org.geocraft.unittest.suite"
         download-size="0"
         install-size="0"
         version="0.0.0"
         unpack="false"/>

</feature>
```

- [ ] **Step 10.2: Commit**

```bash
git add org.geocraft.feature/feature.xml
git commit -m "Simplify feature.xml - use Eclipse RCP feature dependency instead of individual plugins"
```

---

## Task 11: Update Product Definition

**Files:**
- Modify: `org.geocraft.product/GeoCraft.product`

- [ ] **Step 11.1: Update product for Eclipse 4**

In `GeoCraft.product`, update:

1. Change the `<vm>` section to specify Java 21
2. Update launcher args to remove Rosetta/x86_64 references
3. Update the `vmArgs` to remove legacy execution environment string

Replace the full content with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?pde version="3.5"?>

<product name="GeoCraft" id="org.geocraft.product.product" application="org.geocraft.application" type="features" includeLaunchers="true" autoIncludeRequirements="true">

   <aboutInfo>
      <image path="/org.geocraft.product/splash.bmp"/>
      <text>
         GeoCraft is a framework for efficiently prototyping and deploying 
new geoscience applications. 

GeoCraft is built on top of the Eclipse Rich Client Platform.
      </text>
   </aboutInfo>

   <configIni use="default">
      <macosx>/org.geocraft.product/config.ini</macosx>
      <solaris>/org.geocraft.product/config.ini</solaris>
      <win32>/org.geocraft.product/config.ini</win32>
   </configIni>

   <launcherArgs>
      <programArgs>-console
-consoleLog
      </programArgs>
      <vmArgs>-Xms256m
-Xmx1200m
--add-opens=java.base/java.net=ALL-UNNAMED
--add-opens=java.base/java.lang=ALL-UNNAMED
      </vmArgs>
      <vmArgsMac>-XstartOnFirstThread -Dorg.eclipse.swt.internal.carbon.smallFonts
      </vmArgsMac>
   </launcherArgs>

   <windowImages i16="/org.geocraft.product/icons/GC-mark-16.png" i32="/org.geocraft.product/icons/GC-mark-32.png"/>

   <splash
      location="org.geocraft.product"
      startupProgressRect="120,250,345,15"
      startupMessageRect="120,230,345,20"
      startupForegroundColor="1E37FF" />
   <launcher name="GeoCraft">
      <win useIco="false">
         <bmp/>
      </win>
   </launcher>

   <intro introId="org.geocraft.product.intro"/>

   <vm>
   </vm>

   <plugins>
   </plugins>

   <features>
      <feature id="org.geocraft.feature" version="1.0.0"/>
      <feature id="org.geocraft.geomath.feature" version="1.0.0"/>
      <feature id="org.geocraft.abavo.feature" version="1.0.0"/>
      <feature id="org.geocraft.ui.viewer.feature" version="1.0.0.qualifier"/>
      <feature id="org.eclipse.rcp" version="0.0.0"/>
      <feature id="org.eclipse.help" version="0.0.0"/>
   </features>


</product>
```

- [ ] **Step 11.2: Commit**

```bash
git add org.geocraft.product/GeoCraft.product
git commit -m "Update product definition for Eclipse 4 - add RCP feature, clean up launcher args"
```

---

## Task 12: Update Launch Script for Eclipse 4 + JDK 21 Native

**Files:**
- Modify: `launch-geocraft.sh`

- [ ] **Step 12.1: Rewrite launch script**

The launch script needs to:
1. Use JDK 21 native (no Rosetta)
2. Point to Eclipse 4 equinox launcher from the Tycho-resolved target platform (or from a local Maven repository)
3. Use the correct framework bundle

Since Tycho resolves the target platform during build, for development we need to point to the Maven local repository or use `mvn tycho:eclipserun`. The simplest approach is to have the build produce a runnable product, and update the launch script to run it.

Create a new `launch-geocraft.sh` that runs the Tycho-built product:

```bash
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
PRODUCT_DIR=$(find "$WORKSPACE/org.geocraft.product/target/products/org.geocraft.product.product" -maxdepth 1 -type d | head -1)
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

LAUNCHER=$(find "$APP_DIR/plugins" -name "org.eclipse.equinox.launcher_*.jar" | head -1)
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
```

- [ ] **Step 12.2: Commit**

```bash
git add launch-geocraft.sh
git commit -m "Rewrite launch script for Eclipse 4 + JDK 21 native on Apple Silicon"
```

---

## Task 13: Add .gitignore for Eclipse 3.5.1 Target

**Files:**
- Modify: `.gitignore` (create if not exists)

- [ ] **Step 13.1: Update .gitignore**

Add these entries to `.gitignore`:

```
# Eclipse 3.5.1 legacy target (replaced by P2 target definition)
org.geocraft.target/EclipseRCP3.5.1/

# Maven/Tycho build output
target/
*/target/

# Eclipse IDE
.metadata/
```

- [ ] **Step 13.2: Commit**

```bash
git add .gitignore
git commit -m "Add .gitignore for legacy target platform and build output"
```

---

## Task 14: First Build Attempt and Fix Compilation Errors

**Files:**
- Potentially many, depending on build errors

- [ ] **Step 14.1: Run Tycho build**

Run: `mvn clean verify -fn 2>&1 | tee /tmp/geocraft-build.log`

The `-fn` (fail-never) flag ensures all modules are attempted even if some fail, giving us a complete picture.

Expected: Multiple compilation errors on first attempt. Common issues will be:
1. Missing packages in target platform (e.g., `org.apache.commons.jexl` may not be in Orbit)
2. Deprecated/removed APIs
3. Missing `Service-Component` XML namespace declarations

- [ ] **Step 14.2: Analyze and fix build errors**

Review `/tmp/geocraft-build.log` for:
- `[ERROR]` lines - compilation failures
- `Missing requirement` - unresolved bundle dependencies
- `Access restriction` - internal API warnings treated as errors

Fix each error category:
- For missing packages: add them to the target definition or adjust Import-Package
- For access restrictions on `WorkbenchWindow`: add `x-internal:=true` to the consuming bundle's manifest or configure Tycho to allow restricted access
- For compilation errors: fix source code

- [ ] **Step 14.3: Configure Tycho to allow internal API access**

If the build fails on `org.eclipse.ui.internal.WorkbenchWindow` access restrictions, add this to the parent POM's `<build><plugins>` section:

```xml
<plugin>
  <groupId>org.eclipse.tycho</groupId>
  <artifactId>tycho-compiler-plugin</artifactId>
  <version>${tycho.version}</version>
  <configuration>
    <compilerArgs>
      <arg>-warn:-restriction</arg>
    </compilerArgs>
  </configuration>
</plugin>
```

- [ ] **Step 14.4: Re-run build until clean**

Run: `mvn clean verify 2>&1 | tail -50`
Expected: `BUILD SUCCESS`

- [ ] **Step 14.5: Commit all fixes**

```bash
git add -A
git commit -m "Fix compilation errors from Eclipse 4 migration - resolve missing deps and API restrictions"
```

---

## Task 15: Test Launch and Verify

- [ ] **Step 15.1: Launch the built product**

Run: `./launch-geocraft.sh`

Expected: GeoCraft window opens natively on Apple Silicon without Rosetta.

- [ ] **Step 15.2: Verify key functionality**

Check:
1. Workbench opens with correct perspectives
2. Views load (Repository, Properties, etc.)
3. Menu items and toolbar actions work
4. Perspectives can be switched
5. No ClassNotFoundException or NoClassDefFoundError in console

- [ ] **Step 15.3: Fix any runtime issues**

Address any issues found during testing. Common runtime issues:
- Missing bundles in the product (add to feature.xml)
- DS component registration failures (check component XML format)
- SWT threading issues (verify `-XstartOnFirstThread`)

- [ ] **Step 15.4: Final commit**

```bash
git add -A
git commit -m "Fix runtime issues from Eclipse 4 migration testing"
```

---

## Task 16: Update Deploy Scripts

**Files:**
- Modify: `deploy-geocraft-macosx.sh`, `deploy-geocraft-linux.sh`, `deploy-geocraft-windows.sh`, `deploy-geocraft-common.sh`

- [ ] **Step 16.1: Update deploy scripts for Tycho-based builds**

The deploy scripts should now use `mvn clean verify` to build the product instead of the old PDE export. The Tycho build produces platform-specific archives in `org.geocraft.product/target/products/`.

Update the scripts to:
1. Run `mvn clean verify` if not already built
2. Copy/package from `org.geocraft.product/target/products/`
3. Remove references to Eclipse 3.5.1 target

- [ ] **Step 16.2: Commit**

```bash
git add deploy-geocraft-*.sh
git commit -m "Update deploy scripts for Tycho-based product builds"
```

---

## Task 17: Update launch-geocraft Skill

**Files:**
- Modify: `.claude/commands/launch-geocraft.md`

- [ ] **Step 17.1: Update the Claude command for launching**

Update the launch-geocraft command to reflect the new Eclipse 4 + JDK 21 setup.

- [ ] **Step 17.2: Commit**

```bash
git add .claude/commands/launch-geocraft.md
git commit -m "Update launch-geocraft command for Eclipse 4"
```

---

## Summary of Internal API Disposition

| File | Internal API | Action |
|------|-------------|--------|
| Application.java | `WorkbenchPlugin.getSplashShell` | **Replaced** with `Display.getActiveShell()` |
| ViewerHelper.java | `Workbench.getDefaultPageInput` | **Replaced** with `null` input |
| PropertiesView.java | `ViewsPlugin.getViewImageDescriptor` | **Replaced** with platform shared images |
| SessionManager.java | `IWorkbenchConstants.TAG_WINDOW` | **Replaced** with hardcoded `"window"` string |
| SessionManager.java | `Workbench` cast | **Replaced** with `PlatformUI.getWorkbench()` |
| SessionManager.java | `WorkbenchWindow.saveState/restoreState` | **Kept** with `@SuppressWarnings("restriction")` - no public API equivalent |
| Session.java | `Workbench` cast | **Replaced** with public API |
| Session.java | `WorkbenchWindow.saveState/restoreState` | **Kept** with `@SuppressWarnings("restriction")` - no public API equivalent |
