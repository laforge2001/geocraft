# JOGL Migration Design

**Date:** 2026-04-11
**Status:** Approved for implementation
**Context:** GeoCraft's 3D volume viewer currently depends on Ardor3D + LWJGL 2. Neither supports Apple Silicon natively. LWJGL 2 is unmaintained and Ardor3D is effectively abandoned. The volume viewer is currently broken and needs to be re-enabled for native ARM64 macOS.

## Goals

1. Replace Ardor3D/LWJGL 2 with a JOGL-based renderer that runs natively on Apple Silicon (aarch64 macOS).
2. Introduce a clean rendering abstraction layer so the volume viewer no longer depends on any specific 3D engine.
3. Clean up the `IVolumeViewer` public API so it stops leaking third-party 3D types.
4. Achieve comprehensive test coverage (unit, visual regression, integration, behavioral) using TDD.
5. Remove Ardor3D and LWJGL 2 from the codebase entirely.

## Non-Goals

- Not replacing the volume viewer UI/UX — the same features must render the same way.
- Not building a general-purpose 3D engine — only what the volume viewer needs.
- Not maintaining Ardor3D compatibility during migration. Parity testing against the old implementation is not required because the volume viewer is currently broken anyway.
- Not supporting anything beyond OpenGL 3.0/3.3 (macOS caps at GL 4.1).

## Approach

Two-layer abstraction with TDD, incremental migration via OSGi services.

**Layer 1 — Lightweight scene graph primitives:** `SceneNode`, `MeshGeometry`, `LineGeometry`, `RenderMaterial`, `Camera`, `PickEngine`. Generic but GeoCraft-owned. Uses JOML for math.

**Layer 2 — Domain renderers unchanged:** `PostStack3dRenderer`, `FaultRenderer`, etc. keep their structure but construct Layer 1 types instead of Ardor3D types.

The JOGL backend implements Layer 1 with raw OpenGL calls. The volume viewer consumes the backend via OSGi service lookup.

## Bundle Structure

```
org.geocraft.core.rendering (NEW)
├── Layer 1 interfaces and types
├── JOML dependency
├── Pure API — no rendering backend
└── Exported to all consumers

org.geocraft.rendering.jogl (NEW)
├── JOGL implementation of Layer 1
├── GLOffscreenAutoDrawable for offscreen/testing
├── SWT-JOGL canvas integration
├── Depends on: org.geocraft.core.rendering, JOGL, GlueGen
└── Registered as OSGi service

org.geocraft.rendering.jogl.tests (NEW)
├── JUnit 5 test bundle (Tycho eclipse-test-plugin)
├── Levels 1-4 test coverage
└── Golden images committed to test-resources/

org.geocraft.ui.volumeviewer (EXISTING — refactored)
├── Depends on org.geocraft.core.rendering (NOT com.ardor3d)
├── Consumes RenderBackend via OSGi service
├── IVolumeViewer API no longer leaks Ardor3D types
└── Domain renderers target Layer 1 types

com.ardor3d (EXISTING — removed in phase 7)
└── No bridge needed since there's no working baseline to compare against
```

JOGL, GlueGen, and JOML added to `org.geocraft.target/org.geocraft.target.target` via the existing Maven location mechanism.

## Layer 1 — Rendering Abstraction API

### Scene Graph

- `SceneNode` — base type with transform (JOML `Vector3f`/`Quaternionf`/`Matrix4f`), parent/children, visibility hint (`ALWAYS_VISIBLE`/`ALWAYS_CULLED`/`DYNAMIC`), optional `RenderMaterial`.
- `GroupNode extends SceneNode` — container only, no geometry.
- `MeshGeometry extends SceneNode` — vertex buffer, index buffer, normal buffer, texture coordinate buffer. Holds raw `FloatBuffer`/`IntBuffer`.
- `LineGeometry extends SceneNode` — line segments with width and color.
- `SphereGeometry extends SceneNode` — position + radius (for point clouds).
- `TextOverlay extends SceneNode` — 2D text in screen space.

### Render Materials

- `RenderMaterial` — composable configuration attached to any `SceneNode`.
  - `BlendMode` — src/dest blend factors (replaces Ardor3D `BlendState`)
  - `TextureLayer` — texture handle, min/mag filter, combine mode (replaces `TextureState`)
  - `LightingConfig` — enable flag, ambient/diffuse/specular colors, shininess (replaces `LightState` + `MaterialState`)
  - `WireframeMode` — enable flag, line width, antialiased (replaces `WireframeState`)
  - `DepthTestConfig` — enable flag, comparison function (replaces `ZBufferState`)

### Rendering

```java
interface RenderBackend {
    void initialize(RenderSurface surface);
    void renderScene(GroupNode root, Camera camera, Light[] lights);
    void renderPass(GroupNode root, RenderMaterial overrideMaterial);
    RenderSurface createOffscreenSurface(int width, int height);
    BufferedImage readPixels(RenderSurface surface);
    void dispose();
}

interface RenderSurface {
    int getWidth();
    int getHeight();
    void makeCurrent();
    void swapBuffers();
    void dispose();
}
```

The 3-pass rendering pipeline (scene, wireover, widget) stays in the volume viewer — it calls `renderPass()` three times with different root nodes and override materials.

### Camera

```java
class Camera {
    void setPerspective(float fov, float aspect, float near, float far);
    void setParallel(float left, float right, float bottom, float top, float near, float far);
    void setLocation(Vector3f location);
    void setRotation(Quaternionf rotation);
    Matrix4f getViewMatrix();
    Matrix4f getProjectionMatrix();
    Vector3f getWorldCoordinates(Vector2f screenPos, float depth);
    Ray getPickRay(Vector2f screenPos);
}
```

### Picking

```java
interface PickEngine {
    List<PickResult> pickTriangles(GroupNode root, Ray ray);
    List<PickResult> pickBounds(GroupNode root, Ray ray);
}

class PickResult {
    SceneNode getNode();
    float getDistance();
    Vector3f getWorldPosition();
    PickType getType(); // TRIANGLE or BOUNDS
}

class Ray {
    Vector3f origin;
    Vector3f direction;
}
```

### Texture Loading

```java
interface TextureLoader {
    TextureHandle loadTexture(BufferedImage image, FilterMode mag, FilterMode min);
    void disposeTexture(TextureHandle handle);
}

enum FilterMode { NEAREST, BILINEAR, TRILINEAR }
```

### Input

```java
class SwtInputAdapter implements MouseListener, MouseMoveListener,
                                  MouseWheelListener, KeyListener {
    // Translates SWT events into InputEvent model
    // Replaces Ardor3D LogicalLayer/PhysicalLayer/InputTrigger system
}
```

## JOGL Backend Design

### Canvas Integration

- `JoglSwtCanvas` — wraps `com.jogamp.opengl.swt.GLCanvas`, used for live rendering in the volume view. Implements `RenderSurface`.
- `JoglOffscreenSurface` — wraps `com.jogamp.opengl.GLOffscreenAutoDrawable`, used for visual regression tests and headless rendering. Implements `RenderSurface`.

Both share the same `JoglRenderBackend` — the backend is surface-agnostic.

### Backend Internals

- `JoglRenderBackend` — manages GL context lifecycle, walks scene graph applying transforms, translates `RenderMaterial` to GL state calls (`glEnable`/`glBlendFunc`/`glLightfv`/etc.), uploads geometry to VBOs on first render (cached per `SceneNode`), dirty-tracks geometry for re-upload. Targets OpenGL 2.1/3.0 compatibility profile (fits within macOS GL 4.1 cap).
- `JoglTextureLoader` — converts `BufferedImage` to GL texture via `TextureIO`, caches by content hash (equivalent to Ardor3D `TextureKey`), disposes via `glDeleteTextures`.
- `JoglPickEngine` — software ray-triangle intersection using JOML's `Intersectionf`, walks scene graph testing ray against each `MeshGeometry`'s triangles, sorts results by distance. No GPU picking — fast enough for the data volumes the viewer handles.
- `JoglTextOverlay` — uses JOGL's `TextRenderer` utility for Java2D font rasterization.

### OSGi Service Registration

`JoglRenderBackend` registers as an OSGi service implementing `RenderBackend`. The volume viewer looks it up via `BundleContext.getServiceReference`. This keeps the viewer bundle decoupled from the specific backend implementation.

## Test Strategy (TDD)

Tests live in `org.geocraft.rendering.jogl.tests` (Tycho `eclipse-test-plugin`). Four test levels:

### Level 1 — Unit Tests (No GL Context)

Pure-logic tests, run in milliseconds, no display required:

- `CameraTest` — perspective/parallel matrix math, screen→world projection
- `RayTest` — ray construction, normalization
- `PickEngineTest` — ray-triangle intersection with synthetic meshes
- `SceneNodeTest` — transform propagation, parent/child traversal, visibility
- `RenderMaterialTest` — composition and override semantics
- `BoundingVolumeTest` — bounding box/sphere calculations from vertex buffers

### Level 2 — Visual Regression Tests (Offscreen GL)

Uses `JoglOffscreenSurface` (256×256 or 512×512 FBO), renders known scenes, reads pixels, compares to golden PNGs committed in `test-resources/golden/`:

- `empty_scene.png` — black background
- `single_triangle.png` — one red triangle
- `textured_quad.png` — quad with checker texture
- `blended_quads.png` — two transparent overlapping quads
- `lit_sphere.png` — sphere with directional light
- `wireframe_cube.png` — cube in wireframe mode
- `line_primitives.png` — line geometry
- `multi_pass_scene.png` — opaque + wireover + widget passes

Tolerance: per-pixel RGB diff with configurable threshold to handle driver variance across platforms. On mismatch: write actual + diff images to `target/` for debugging. Initial golden generation: first run produces goldens, human reviews and commits.

### Level 3 — Integration Tests (JOGL + SWT)

Live SWT Display required; skipped on CI if no display available via `Assume.assumeTrue(displayAvailable())`:

- `JoglSwtCanvasIntegrationTest` — creates SWT `Shell` + `JoglSwtCanvas`, verifies GL context initializes, renders frame with no errors, resize updates viewport, disposal frees resources.
- `SwtInputAdapterTest` — programmatically dispatches SWT `MouseEvent`s via `Display.post()`, verifies `InputEvent` translation.
- `TextureLoaderIntegrationTest` — loads `BufferedImage`, renders textured quad, verifies pixels match.
- `RenderLoopTest` — starts async render loop, verifies N frames render in bounded time, stops cleanly.

### Level 4 — Behavioral Tests (End-to-End Scenarios)

Domain-level tests mirroring actual volume viewer usage, using offscreen rendering:

- `SeismicVolumeRenderTest` — synthetic 32×32×32 float array, construct via `PostStack3dRenderer` logic, render offscreen, verify texture slices at expected positions.
- `FaultSurfaceRenderTest` — synthetic triangulated fault mesh, render offscreen, verify triangles at expected screen positions.
- `WellTrajectoryRenderTest` — synthetic well path as line geometry, verify line visible and colored correctly.
- `PickingBehavioralTest` — render scene with known geometry, pick at specific screen coordinates, verify correct node + world position.
- `MultiPassRenderTest` — scene with opaque + transparent + wireframe passes, verify render order and wireframe overlay.

### TDD Flow

For each Layer 1 or backend feature:
1. Write the test first with expected API shape.
2. Run it, see it fail.
3. Implement the minimum to make it pass.
4. Refactor if needed.
5. Commit.

Layer 1 unit tests drive API design. Backend tests drive implementation. First visual regression run after implementing each feature generates the golden, which is reviewed and committed.

## Migration Phases

Each phase has clear entry/exit criteria. Tests are written before implementation (TDD). Build and all tests must pass before proceeding to the next phase.

### Phase 1 — Foundation: `org.geocraft.core.rendering`

- Create new bundle, add to parent pom and target platform
- Add JOML dependency via target platform Maven location
- Write Layer 1 interfaces and types (scene graph, materials, camera, ray, picking, surface, backend)
- **Tests:** Level 1 unit tests for all pure-logic types
- **Exit:** Bundle compiles, all unit tests green

### Phase 2 — JOGL Backend Foundation: `org.geocraft.rendering.jogl`

- Create bundle, add JOGL + GlueGen to target platform
- Implement `JoglRenderBackend`, `JoglOffscreenSurface`, `JoglTextureLoader`
- Implement basic scene graph walking + GL state translation (no pick engine yet)
- Register as OSGi service
- **Tests:** Level 2 visual regression — empty scene, single triangle, textured quad, blended quads, wireframe
- **Exit:** Goldens committed, offscreen rendering matches goldens within tolerance

### Phase 3 — JOGL Picking + Advanced Features

- Implement `JoglPickEngine` with software ray-triangle intersection
- Implement lighting, multi-pass rendering, text overlay
- **Tests:** `PickEngineTest` (Level 1), `lit_sphere.png` + `multi_pass_scene.png` (Level 2), `PickingBehavioralTest` (Level 4)
- **Exit:** All Layer 1 primitives + picking validated

### Phase 4 — JOGL SWT Integration

- Implement `JoglSwtCanvas` wrapping `com.jogamp.opengl.swt.GLCanvas`
- Implement `SwtInputAdapter`
- Implement render loop on SWT Display
- **Tests:** Level 3 integration tests
- **Exit:** JOGL canvas embeds in SWT `Shell`, renders frames, handles input, cleans up

### Phase 5 — Volume Viewer Refactor

- Refactor `org.geocraft.ui.volumeviewer` to depend on `org.geocraft.core.rendering` instead of `com.ardor3d`
- Clean up `IVolumeViewer` to stop leaking Ardor3D types
- Refactor all domain renderers (`PostStack3dRenderer`, `FaultRenderer`, `Grid3dRenderer`, `WellRenderer`, `PointSetRenderer`, `WellPickRenderer`) to construct Layer 1 types
- Refactor `ViewCanvasImplementor`/`ViewCanvasFactory` to use `RenderBackend` OSGi service
- Replace `VolumeMouseLook` Ardor3D `LogicalLayer` with SwtInputAdapter
- Replace `SceneText`, `FocusRods`, `SelectionRenderer` Ardor3D extensions with Layer 1 equivalents
- **Tests:** Level 4 behavioral tests
- **Exit:** No more `com.ardor3d` imports in volume viewer code, behavioral tests pass

### Phase 6 — End-to-End Validation

- Launch GeoCraft natively on Apple Silicon (no Rosetta)
- Open volume viewer, load test data, verify interactive rendering
- Verify mouse interaction (pick, drag, zoom, pan)
- Verify all geometry types: seismic volumes, grids, faults, wells, point sets
- Verify selection and focus visualization
- **Tests:** Manual exploratory + all automated tests from phases 1-5
- **Exit:** Functional parity with historical Ardor3D implementation, running native ARM64

### Phase 7 — Remove Ardor3D

- Delete `com.ardor3d` bundle (JARs, native libs, manifest, pom)
- Remove from parent pom's module list
- Clean up remaining Ardor3D references
- Update `SWT_MIGRATION_LOG.md`
- **Tests:** Full build passes, all tests green, volume viewer still works
- **Exit:** Zero references to Ardor3D or LWJGL 2

## Key Design Properties

- Each phase is independently verifiable — the project is buildable and tests green at every phase boundary.
- Phases 1-4 produce a working JOGL backend entirely independent of GeoCraft's viewer code — could be validated in isolation.
- Phase 5 is the only phase touching the existing volume viewer. By then the backend is fully tested.
- Ardor3D stays in the tree through phase 6, so phase 5 can be reverted if issues arise.
- Phase 7 is pure cleanup — safe to delay.

## Risks

- **macOS OpenGL deprecation.** Apple deprecated OpenGL in 10.14; it still works but is frozen at 4.1. Long-term risk: Apple could remove it. Mitigation: the `RenderBackend` abstraction makes a future Vulkan-via-MoltenVK migration much easier than the current Ardor3D situation.
- **Driver variance in visual regression.** Different GL drivers produce slightly different pixels. Mitigation: tolerance-based comparison, not exact match.
- **JOGL SWT canvas maturity on macOS aarch64.** JOGL 2.5+ officially supports macOS aarch64, but the SWT canvas path is less commonly tested than the NEWT path. Mitigation: integration tests run against real SWT canvas early (Phase 4) before committing to full volume viewer migration in Phase 5.
- **OSGi service timing.** The `RenderBackend` service must be registered before the volume viewer activates. Mitigation: use OSGi Declarative Services (DS) with cardinality 1..1 and mandatory dependency, so the viewer bundle waits for the service.
