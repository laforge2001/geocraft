# GeoCraft Eclipse 4 Migration Design

**Date:** 2026-04-01
**Branch:** 9-need-to-transition-target-platform-to-eclipse-4
**Goal:** Run GeoCraft natively on Apple Silicon using Eclipse 4.x target platform with JDK 21

## Target Stack

| Component | Version | Source |
|-----------|---------|--------|
| Eclipse RCP | 2025-12 (4.34) | P2 repository |
| JDK | 21 (Temurin) | Already installed |
| Build system | Tycho 5.0.2 / Maven | New |
| SWT | cocoa aarch64 | Via P2 target platform |
| Compatibility layer | Eclipse 3.x compat (ships with 4.x) | Included in target |

## Approach: Compatibility-First

Eclipse 4.x ships a compatibility layer that supports Eclipse 3.x workbench APIs
(`ActionBarAdvisor`, `IPerspectiveFactory`, `org.eclipse.ui.actionSets`, etc.).
GeoCraft runs on this layer with minimal code changes. No migration to e4
application model (e4xmi) or dependency injection.

## Work Breakdown

### 1. Tycho/Maven Build System

Add Maven/Tycho build infrastructure:

- **Parent POM** (`pom.xml` at repo root): Defines Tycho 5.0.2 plugin, Java 21,
  Eclipse 2025-12 P2 repository URL, and Eclipse Orbit repository for third-party
  OSGi bundles.
- **Module POMs**: One `pom.xml` per bundle, feature, and product project. These are
  minimal - typically just `groupId`, `artifactId`, `version`, and `packaging` type
  (`eclipse-plugin`, `eclipse-feature`, `eclipse-repository`).
- **Packaging types**:
  - Bundles/plugins: `eclipse-plugin`
  - Features: `eclipse-feature`
  - Products: `eclipse-repository`
  - Test bundles: `eclipse-test-plugin`
  - Target platform project: `eclipse-target-definition`

### 2. Target Platform Definition

Replace `org.geocraft.target/EclipseRCP3.5.1/` with a declarative `.target` file:

- **Eclipse 2025-12 P2 repo**: `https://download.eclipse.org/releases/2025-12`
  - Core RCP bundles: `org.eclipse.rcp` feature
  - Equinox runtime, SWT (cocoa aarch64 resolved automatically)
  - Eclipse 3.x compatibility included in the RCP feature
- **Eclipse Orbit P2 repo**: `https://download.eclipse.org/tools/orbit/simrel/orbit-aggregation/2025-12`
  - `org.apache.commons.beanutils`
  - `org.apache.commons.collections`
  - Other Apache Commons bundles as needed
- **Local bundle**: jtk.edu.mines.boole stays as a workspace project (unchanged)

The old `EclipseRCP3.5.1/` directory will be removed from version control (add to
`.gitignore`).

### 3. Bundle Manifest Updates

All ~50 bundle `MANIFEST.MF` files need:

- `Bundle-RequiredExecutionEnvironment: JavaSE-1.8` changed to `JavaSE-21`
- Eclipse dependency version constraints updated to match 4.x ranges, e.g.:
  - `org.eclipse.core.runtime;bundle-version="3.4.0"` -> `"3.20.0"` (or unversioned)
  - `org.eclipse.ui;bundle-version="3.4.1"` -> `"3.200.0"` (or unversioned)
- Verify all `Import-Package` and `Require-Bundle` entries resolve against the new
  target platform

### 4. Internal API Fixes (6 files)

These files import from `org.eclipse.ui.internal.*` and must be rewritten:

1. **`org.geocraft.core.session/SessionManager.java`** - Uses `IWorkbenchConstants`,
   `Workbench`, `WorkbenchWindow`. Replace with public `IWorkbench`,
   `IWorkbenchWindow` APIs from `PlatformUI`.
2. **`org.geocraft.core.session/Session.java`** - Uses `Workbench`, `WorkbenchWindow`.
   Same approach as SessionManager.
3. **`org.geocraft.ui.viewer/ViewerHelper.java`** - Uses `Workbench`. Replace with
   `PlatformUI.getWorkbench()`.
4. **`org.geocraft.product/Application.java`** - Uses `WorkbenchPlugin`. Find
   equivalent public API or remove usage.
5. **`org.geocraft.product/PerspectiveAction.java`** - Accesses internal intro view.
   Use `org.eclipse.ui.intro.IIntroManager` public API.
6. **`org.geocraft.ui.property/PropertiesView.java`** - Uses `ViewsPlugin`. Replace
   with public view API.

Each fix is case-by-case but the pattern is: replace internal class references with
their public API equivalents from `PlatformUI` and `IWorkbench*` interfaces.

### 5. Java Compliance Updates

- All `.classpath` files: Update JRE container to JavaSE-21
- All `.settings/org.eclipse.jdt.core.prefs`: Update compiler compliance to 21
- No Java language modernization (no records, var, sealed classes, etc.)

### 6. Product and Launch Updates

- **`GeoCraft.product`**: Update launcher args, remove Rosetta-specific flags,
  set `-arch aarch64`
- **`launch-geocraft.sh`**: Rewrite to use JDK 21 native, Eclipse 4.x equinox
  launcher, no `arch -x86_64`
- **`config.ini`**: Update framework jar reference, bundle start levels for
  Eclipse 4.x equinox runtime
- **Deploy scripts**: Update for Eclipse 4.x product export via Tycho

### 7. Third-Party Dependencies

| Library | Current | After |
|---------|---------|-------|
| Apache Commons Beanutils | 1.8.0 jar in target dir | Eclipse Orbit P2 |
| Apache Commons Collections | 3.2.1 jar in target dir | Eclipse Orbit P2 |
| Apache Commons Jexl | 1.1.0 jar in target dir | Eclipse Orbit P2 |
| jtk.edu.mines.boole | 1.0.0 local project | Unchanged (local project) |
| log4j | 1.2.14 embedded in org.geocraft.core | Unchanged |

Note: Verify that Orbit versions of Apache Commons are API-compatible with the
versions GeoCraft currently uses.

## What Doesn't Change

- All `plugin.xml` extension points (`actionSets`, `views`, `perspectives`, etc.) -
  compatibility layer handles them
- `ActionBarAdvisor`, `IPerspectiveFactory` implementations
- All SWT widget code (API stable across Eclipse versions)
- Application structure and UI behavior
- Feature definitions (content unchanged, just add Tycho POMs)
- jtk bundle

## Risk Areas

1. **Internal API rewrites** - The 6 files need case-by-case analysis. Some internal
   APIs may not have direct public equivalents; may need alternative approaches.
2. **JOGL/native libraries in jtk** - The jtk bundle includes JOGL and LAPACK natives.
   If `org.geocraft.io.javaseis` exercises OpenGL paths, we may need aarch64 natives.
   However, GeoCraft only uses `Parameter` and `ParameterSet` (utility classes, no
   OpenGL), so this is low risk.
3. **Apache Commons version compatibility** - Orbit may ship newer versions than 1.8.0.
   Need to verify no breaking API changes.
4. **Declarative Services runtime** - Eclipse 4.x DS runtime differs from 3.5.1.
   GeoCraft's `Service-Component` declarations in `org.geocraft.core` need to be
   verified.
5. **SWT event loop / threading** - Eclipse 4.x may have subtle threading differences.
   The 3D viewers (volumeviewer, mapviewer) with OpenGL/JOGL bindings are most at risk.

## Out of Scope

- Converting to e4 application model (e4xmi)
- Dependency injection migration
- Java language modernization (records, sealed classes, pattern matching, etc.)
- Removing deprecated API usage beyond what's required to compile/run
- Upgrading jtk to 1.1.0
- Replacing log4j 1.2.14 (embedded in org.geocraft.core)

## Success Criteria

1. `mvn clean verify` builds all bundles, features, and products without errors
2. GeoCraft launches natively on Apple Silicon (aarch64) without Rosetta
3. Main UI (workbench, perspectives, views) renders and functions correctly
4. All existing plugin.xml extensions load via compatibility layer
5. No `org.eclipse.ui.internal` imports remain in source code
