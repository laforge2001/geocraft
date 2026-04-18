# SWT Migration Log - GeoCraft on Apple Silicon macOS

## Problem
GeoCraft is an Eclipse RCP 3.5.1 app. The original SWT cocoa x86_64 3.5.1 jars don't work on modern macOS (Sequoia / Darwin 24.6.0) on Apple Silicon (arm64).

## Environment
- macOS 15 (Darwin 24.6.0), Apple Silicon (arm64)
- Default JDK: OpenJDK 21 Temurin
- Eclipse launch uses Java 11 (x86_64 under Rosetta) — confirmed by class version 55 limit
- Target platform: Eclipse SDK 3.5.1 (directory-based at `org.geocraft.target/EclipseSDK3.5.1/`)
- `osgi.arch` reports `x86_64` at runtime (Rosetta), not `aarch64`

## What We've Tried

### Attempt 1: Replace x86_64 SWT with newer x86_64 version (3.127.0)
- Downloaded `org.eclipse.swt.cocoa.macosx.x86_64_3.127.0.jar` from Maven Central
- **Result**: Maven Central artifact missing MANIFEST.MF — not a valid OSGi bundle
- Re-downloaded from Eclipse p2 repo — proper bundle
- **Result**: Fragment-Host `[3.0.0,4.0.0)` matched old host, but...
- SWT 3.127.0 is compiled for Java 17 (class file version 61.0)
- Runtime is Java 11 → `UnsupportedClassVersionError`
- **Status**: FAILED — Java version mismatch

### Attempt 2: Add aarch64 SWT fragment (3.127.0)
- Downloaded `org.eclipse.swt.cocoa.macosx.aarch64_3.127.0.jar`
- Fragment-Host required `[3.127.0,4.0.0)` but host is 3.5.1
- **Status**: FAILED — host version mismatch, also Java 17 class files

### Attempt 3: Replace SWT host with 3.127.0
- Downloaded `org.eclipse.swt_3.127.0.jar` from Maven Central
- Moved old `org.eclipse.swt_3.5.1.v3555a.jar` to backup
- **Result**: New host couldn't resolve: `Missing imported package org.eclipse.swt.accessibility2`
- Caused cascade failure — everything depending on SWT failed
- **Status**: FAILED — new host incompatible with 3.5.1 platform

### Attempt 4: Restore old host + patched aarch64 3.122.0 (Java 11 compatible)
- Restored `org.eclipse.swt_3.5.1.v3555a.jar` host
- Downloaded `org.eclipse.swt.cocoa.macosx.aarch64_3.122.0.jar` from Eclipse p2 (4.26)
- Confirmed class file version 55 (Java 11 compatible)
- Patched Fragment-Host from `[3.116.0,4.0.0)` to `[3.0.0,4.0.0)`
- Patched Eclipse-PlatformFilter to accept both `aarch64` and `x86_64`
- Removed signatures (ECLIPSE_.SF, ECLIPSE_.RSA)
- Moved old x86_64 3.127.0 fragment to backup
- **Runtime result**: aarch64 fragment resolves, no more UnsupportedClassVersionError or cascade failures
- **Compilation result**: Still getting "import org.eclipse.swt cannot be resolved" in Eclipse IDE
- **Status**: PARTIAL — runtime improved, but IDE still can't find SWT classes on build path

## Current State of Target Platform Plugins (SWT-related)
```
org.eclipse.swt_3.5.1.v3555a.jar                    ← host bundle (original)
org.eclipse.swt.cocoa.macosx_3.5.1.v3555a.jar       ← 32-bit cocoa (x86/ppc filter)
org.eclipse.swt.cocoa.macosx.source_3.5.1.v3555a.jar
org.eclipse.swt.cocoa.macosx.aarch64_3.122.0.jar    ← patched: host [3.0.0,4.0.0), filter accepts x86_64+aarch64
backup/org.eclipse.swt_3.5.1.v3555a.jar             ← copy of host
backup/org.eclipse.swt.cocoa.macosx.x86_64_3.5.1.v3555a.jar  ← original x86_64 (broken on modern macOS)
backup/org.eclipse.swt.cocoa.macosx.x86_64.source_3.5.1.v3555a.jar
backup/org.eclipse.swt.cocoa.macosx.x86_64_3.127.0.jar ← Java 17, removed
```

## Current Problem (Compilation)
Eclipse IDE PDE target platform still can't resolve `org.eclipse.swt` packages for compilation.
All errors are "The import org.eclipse.swt cannot be resolved" — meaning the SWT fragment
isn't being picked up as a source of `org.eclipse.swt.*` packages by the PDE build path resolver.

This is likely because:
- The old x86_64 fragment was deleted (it provided classes for the IDE's build path)
- The new aarch64 fragment is there but PDE may not resolve it due to platform filter
- PDE target platform resolver uses the IDE's `osgi.arch` (could be aarch64 if IDE is native)
  vs. the runtime's `osgi.arch` (x86_64 under Rosetta)

### Attempt 5: Change target definition arch to aarch64
- Target definition had `<arch>x86_64</arch>` — PDE only resolves fragments matching that arch
- Eclipse IDE is native aarch64, so the aarch64 fragment exists but PDE skips it due to target arch
- Changed `Geocraft.target` arch from `x86_64` to `aarch64`
- **Result**: Same compilation errors — SWT still not resolved
- **Status**: FAILED — target arch change alone doesn't fix it

### Attempt 6: Fix corrupt manifest + remove arch from platform filter
- Previous jar rebuild had corrupt manifest due to sed not handling MANIFEST line wrapping
- Line 19-20 were: `Eclipse-PlatformFilter: (& ... (osgi.arch=a` / ` arch64) )`
- sed only replaced line 19, leaving orphaned continuation `arch64) )` → malformed filter
- Rebuilt jar from clean source using Edit tool instead of sed
- Removed arch constraint entirely: `Eclipse-PlatformFilter: (& (osgi.ws=cocoa) (osgi.os=macosx) )`
- Fragment-Host: `[3.0.0,4.0.0)` (correct)
- Java 11 compatible (class version 55)
- **Result**: Same compilation errors — SWT still not resolved
- **Status**: FAILED — even with clean manifest and no arch filter, PDE still can't resolve fragment

### Attempt 7: Use x86_64 SWT 3.122.0 + target arch x86_64
- Realized aarch64 target causes JTK bundle resolution failure (Bundle-NativeCode only has macosx/x64, not aarch64)
- Downloaded `org.eclipse.swt.cocoa.macosx.x86_64_3.122.0.v20221123-2302.jar` from Eclipse 4.26 p2
- Confirmed Java 11 compatible (class version 55)
- Patched Fragment-Host from `[3.116.0,4.0.0)` to `[3.0.0,4.0.0)`
- Removed signatures, stripped per-entry digests
- Platform filter left as-is: `(& (osgi.ws=cocoa) (osgi.os=macosx) (osgi.arch=x86_64) )`
- Changed target arch back to `x86_64`, bumped sequenceNumber to 3
- Rationale: x86_64 target resolves both SWT (x86_64 fragment) and JTK (macosx/x64 native code)
- Modern 3.122.0 x86_64 native code should work on macOS Sequoia under Rosetta (unlike old 3.5.1)
- **Compilation result**: SUCCESS — SWT compilation errors resolved, PDE resolves the x86_64 fragment
- **Runtime result**: Fragment resolved, but crashed with `UnsatisfiedLinkError` — stale arm64 jnilib cached at `~/.swt/lib/macosx/x86_64/` from previous aarch64 attempt. Deleted cache, retesting.
- Also: `com.ardor3d` fails with `Missing host Bundle-NativeCode_0.0.0` (separate issue)
- After cache clear, still got arm64 jnilib — the aarch64 fragment (attempt 6, no arch filter) was also resolving and providing arm64 native libs
- Moved aarch64 fragment to backup, cleared `~/.swt/lib/macosx/x86_64/` again
- **Result**: GeoCraft launches successfully on macOS Sequoia under Rosetta/x86_64 Java 11
- **Status**: SUCCESS

## Current State of Target Platform Plugins (SWT-related)
```
org.eclipse.swt_3.5.1.v3555a.jar                        ← host bundle (original)
org.eclipse.swt.cocoa.macosx_3.5.1.v3555a.jar           ← 32-bit cocoa (x86/ppc filter)
org.eclipse.swt.cocoa.macosx.source_3.5.1.v3555a.jar
org.eclipse.swt.cocoa.macosx.aarch64_3.122.0.jar        ← patched aarch64 (attempt 6, kept)
org.eclipse.swt.cocoa.macosx.x86_64_3.122.0.jar         ← NEW: patched x86_64 (attempt 7)
jtk.edu.mines.boole_1.0.0.201001131316/                  ← JTK (has macosx/x64 native only)
backup/org.eclipse.swt_3.5.1.v3555a.jar
backup/org.eclipse.swt.cocoa.macosx.x86_64_3.5.1.v3555a.jar
backup/org.eclipse.swt.cocoa.macosx.x86_64.source_3.5.1.v3555a.jar
backup/org.eclipse.swt.cocoa.macosx.x86_64_3.127.0.jar
```

## Eclipse 4 Migration (2026-04-01)

### Full Platform Migration to Eclipse 2025-12 (4.34)

Migrated the entire GeoCraft application from Eclipse 3.5.1 to Eclipse 2025-12 (4.34):

- **Target platform**: Replaced checked-in EclipseRCP3.5.1/ binaries with declarative P2-based target definition (`Geocraft-e4.target`) pointing to Eclipse 2025-12 release train
- **Build system**: Added Tycho 5.0.2 / Maven build (59 modules, all building successfully)
- **JDK**: Upgraded from Java 11 (x86_64 under Rosetta) to JDK 21 Temurin (native aarch64)
- **SWT**: Now using `org.eclipse.swt.cocoa.macosx.aarch64_3.132.0` from Eclipse 2025-12 — native Apple Silicon, no Rosetta needed
- **Compatibility layer**: Using Eclipse 3.x compat APIs (ActionBarAdvisor, IPerspectiveFactory, actionSets, etc.) that ship with Eclipse 4.x
- **Internal API fixes**: Removed 6 of 8 `org.eclipse.ui.internal` imports. Remaining 2 (`WorkbenchWindow` in Session.java and SessionManager.java) retained with `@SuppressWarnings("restriction")` for state serialization — stubbed out, needs redesign
- **Third-party deps**: Apache Commons (beanutils 1.8.3, collections 3.2.2) from Eclipse Orbit P2; jtk.edu.mines.boole kept as local reactor module with aarch64 native support added
- **Product**: Materialized for macOS aarch64, Linux x86_64, Windows x86_64 via `org.geocraft.repository`

### Known Issues
- `WorkbenchWindow.saveState()`/`restoreState()` were removed in Eclipse 4.x — session save/restore is stubbed out and needs redesign
- `CoreSVG has logged an error` warning on macOS (cosmetic, Eclipse platform SVG rendering)

### Status: SUCCESS — GeoCraft launches natively on Apple Silicon with Eclipse 4

### Attempt 8: Patch missing TableTree classes into SWT 3.122.0
- SWT 3.122.0 removed deprecated `TableTree`, `TableTreeItem`, `TableTreeEditor` classes
- JFace 3.5.1's `OpenStrategy.initializeHandler()` still references `TableTreeItem` → `ClassNotFoundException`
- This broke all views: LogView, PropertiesView, RepositoryView, AlgorithmsView
- Fix: extracted TableTree/TableTreeItem/TableTreeEditor source from `org.eclipse.swt.cocoa.macosx.source_3.5.1.v3555a.jar`
- Compiled against SWT 3.122.0 with Java 11 (`javac -source 11 -target 11`)
- Injected 8 class files (3 main + 5 inner/anonymous) into `org.eclipse.swt.cocoa.macosx.x86_64_3.122.0.jar`
- Backup saved as `org.eclipse.swt.cocoa.macosx.x86_64_3.122.0.jar.bak`
- **Status**: SUCCESS — all four views (LogView, PropertiesView, RepositoryView, AlgorithmsView) load correctly

### Attempt 9: Fix Eclipse Help system for Java 11+
- Help > Help Contents opened browser but showed `TransformerFactoryConfigurationError: Provider org.apache.xalan.processor.TransformerFactoryImpl not found`
- Root cause: Java 11 removed the bundled Xalan XSLT processor; Eclipse 3.5.1 help system's `DocumentWriter` calls `TransformerFactory.newInstance()` which defaulted to Xalan
- Fix part 1: Added `-Djavax.xml.transform.TransformerFactory=com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl` to use Java's built-in XSLTC
- This caused `ClassCastException`: OSGi's `javax.xml` bundle loaded a separate copy of `TransformerFactory` from a different classloader than the JDK's
- Fix part 2: Added `org.osgi.framework.bootdelegation=javax.xml.*,...` to force OSGi to delegate XML packages to the boot classloader
- Both properties added to `org.geocraft.product/config.ini` for persistence (launch config is gitignored)
- Also need to add both `-D` properties to VM arguments in Eclipse Run Configuration (Arguments tab)
- Added `org.eclipse.help.appserver` bundle to launch config
- **Status**: SUCCESS

## Key Constraints
- SWT fragment must be compiled for Java 11 (class version ≤ 55)
- Fragment-Host must accept `[3.0.0,4.0.0)` to attach to 3.5.1 host
- Platform filter must match whatever arch the Eclipse IDE reports
- Old SWT 3.5.1 x86_64 native code doesn't work on modern macOS

## 2026-04-11: JOGL Migration — Replacing Ardor3D for Native Apple Silicon
**Problem**: `com.ardor3d` bundle used LWJGL 2 which had no aarch64 macOS natives.
Volume viewer could only run under Rosetta 2 with an x86_64 JVM. LWJGL 2 is
unmaintained and Ardor3D is effectively abandoned.

**Solution**: Two-layer rendering abstraction with JOGL backend.
- `org.geocraft.core.rendering` — Layer 1 API (scene graph, materials, camera,
  picking, bounds, backend interfaces). Uses JOML 1.10.5 for all math. Pure
  interface bundle with no rendering engine dependency.
- `org.geocraft.rendering.jogl` — JOGL 2.6.0 implementation of Layer 1.
  Registers as OSGi DS service providing `RenderBackend`. Includes
  `JoglSwtCanvas`, `SwtInputAdapter`, `JoglTextureLoader`, `JoglRenderBackend`,
  `JoglMaterialApplier`, `JoglSceneWalker`, `JoglGeometryUpload`.
- `org.geocraft.ui.volumeviewer` refactored: removed all `com.ardor3d` imports,
  consumes `RenderBackend` via OSGi service lookup. 22 source files refactored.
- `com.ardor3d` bundle deleted.

**Target platform changes** (`org.geocraft.target.target`):
- JOML 1.10.5, GlueGen 2.6.0, JOGL 2.6.0 added via Maven locations.
- JOGL wrap uses explicit bnd instructions (`Export-Package: *`, `DynamicImport-Package: *`) because default bnd wrap only exports a subset of packages.

**Test infrastructure**:
- 18 unit tests in `org.geocraft.core.rendering.tests` covering math, camera,
  scene graph, materials, bounds, picking. Pass cleanly.
- Level 2 visual regression tests (offscreen GL) deferred — JOGL native loading
  inside Tycho surefire OSGi on Apple Silicon macOS is brittle
  (`libnativewindow_awt.dylib` has `@rpath/libjawt.dylib` and macOS SIP strips
  `DYLD_LIBRARY_PATH` from subprocess environments). Real rendering validation
  happens in the production GeoCraft launcher in Phase 6.
- `JoglSwtCanvasIntegrationTest` is gated behind
  `-Dgeocraft.jogl.integration=true` so it doesn't fail automated builds.

**Stubs remaining (Phase 6 work)**:
Volume viewer compiles and loads without Ardor3D, but many renderer bodies are
stubbed with `TODO: port from Ardor3D` comments. No geometry renders yet. Files
needing completion: ViewCanvasImplementor (camera controls, render loop wiring),
PostStack3dRenderer, Grid3dRenderer, FaultRenderer, WellRenderer, WellPickRenderer,
PointSetRenderer, SelectionRenderer, FocusRods, SceneText (billboarding),
RibbonFactory.generateRibbon.

**Git tags**: `jogl-phase-1-complete` through `jogl-phase-5-complete`,
`jogl-phase-7-complete` (phase 6 manual validation pending).

**Status**: Architecture complete. Builds clean on native aarch64 JVM with zero
Ardor3D/LWJGL 2 dependencies. Volume viewer requires geometry-rendering code
fill-in before functional parity with the (broken) Ardor3D implementation.
