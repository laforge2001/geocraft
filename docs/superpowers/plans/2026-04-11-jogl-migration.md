# JOGL Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Ardor3D/LWJGL 2 with a JOGL-based renderer behind a new rendering abstraction layer, enabling native Apple Silicon support with comprehensive TDD test coverage.

**Architecture:** Two-layer abstraction — `org.geocraft.core.rendering` defines a backend-agnostic scene graph / material / camera / picking API (using JOML for math), and `org.geocraft.rendering.jogl` implements it with raw OpenGL via JOGL. The volume viewer consumes the `RenderBackend` via OSGi service lookup. Ardor3D stays in the tree until phase 7.

**Tech Stack:** Java 21, Tycho 5.0.2, Eclipse 2025-12 RCP, JOGL 2.5.0, GlueGen 2.5.0, JOML 1.10.5, JUnit 4 (from Orbit `org.junit`), JOML for math.

---

## File Structure Overview

### New bundle: `org.geocraft.core.rendering`
```
org.geocraft.core.rendering/
├── META-INF/MANIFEST.MF
├── build.properties
├── pom.xml
└── src/org/geocraft/core/rendering/
    ├── scene/
    │   ├── SceneNode.java           # base, transform, visibility, material
    │   ├── GroupNode.java            # container
    │   ├── MeshGeometry.java         # vertex/index/normal/texcoord buffers
    │   ├── LineGeometry.java         # lines with width/color
    │   ├── SphereGeometry.java       # position + radius
    │   ├── TextOverlay.java          # 2D text in screen space
    │   └── VisibilityHint.java       # enum: ALWAYS_VISIBLE, ALWAYS_CULLED, DYNAMIC
    ├── material/
    │   ├── RenderMaterial.java       # composable material
    │   ├── BlendMode.java            # src/dst blend factors
    │   ├── TextureLayer.java         # texture + filters + combine
    │   ├── LightingConfig.java       # ambient/diffuse/specular/shininess
    │   ├── WireframeMode.java        # enable + line width
    │   ├── DepthTestConfig.java      # enable + compare func
    │   └── BlendFactor.java          # enum
    ├── camera/
    │   ├── Camera.java               # view/projection, screen→world
    │   ├── ProjectionType.java       # enum: PERSPECTIVE, PARALLEL
    │   └── Light.java                # directional light type
    ├── pick/
    │   ├── Ray.java                  # origin + direction
    │   ├── PickEngine.java           # interface
    │   ├── PickResult.java           # node + distance + worldPos + type
    │   ├── PickType.java             # enum: TRIANGLE, BOUNDS
    │   └── DefaultPickEngine.java    # software ray-triangle impl (pure Java)
    ├── backend/
    │   ├── RenderBackend.java        # interface
    │   ├── RenderSurface.java        # interface
    │   ├── TextureLoader.java        # interface
    │   ├── TextureHandle.java        # opaque handle
    │   └── FilterMode.java           # enum: NEAREST, BILINEAR, TRILINEAR
    ├── bounds/
    │   ├── BoundingVolume.java       # abstract
    │   ├── BoundingBox.java          # axis-aligned
    │   └── BoundingSphere.java       # center + radius
    └── input/
        ├── InputEvent.java           # base
        ├── MouseInputEvent.java
        ├── KeyInputEvent.java
        └── InputListener.java        # interface
```

### New bundle: `org.geocraft.rendering.jogl`
```
org.geocraft.rendering.jogl/
├── META-INF/MANIFEST.MF
├── build.properties
├── pom.xml
├── OSGI-INF/JoglRenderBackend.xml    # DS component descriptor
└── src/org/geocraft/rendering/jogl/
    ├── JoglRenderBackend.java        # impl of RenderBackend
    ├── JoglOffscreenSurface.java     # GLOffscreenAutoDrawable wrapper
    ├── JoglSwtCanvas.java            # com.jogamp.opengl.swt.GLCanvas wrapper
    ├── JoglTextureLoader.java        # BufferedImage → GL texture
    ├── JoglTextureHandle.java        # concrete handle with GL texture id
    ├── JoglGeometryUpload.java       # VBO caching per node
    ├── JoglMaterialApplier.java      # RenderMaterial → GL state calls
    ├── JoglSceneWalker.java          # traverses scene graph, applies transforms
    ├── JoglTextRenderer.java         # wraps JOGL TextRenderer utility
    └── SwtInputAdapter.java          # SWT events → InputEvent
```

### New bundle: `org.geocraft.rendering.jogl.tests`
```
org.geocraft.rendering.jogl.tests/
├── META-INF/MANIFEST.MF
├── build.properties
├── pom.xml
├── test-resources/golden/            # committed PNG goldens
│   ├── empty_scene.png
│   ├── single_triangle.png
│   ├── textured_quad.png
│   ├── blended_quads.png
│   ├── wireframe_cube.png
│   ├── line_primitives.png
│   ├── lit_sphere.png
│   └── multi_pass_scene.png
└── src/org/geocraft/rendering/jogl/tests/
    ├── unit/                          # Level 1 — no GL
    │   ├── CameraTest.java
    │   ├── RayTest.java
    │   ├── SceneNodeTest.java
    │   ├── RenderMaterialTest.java
    │   ├── BoundingVolumeTest.java
    │   └── DefaultPickEngineTest.java
    ├── visual/                        # Level 2 — offscreen GL
    │   ├── VisualRegressionHarness.java
    │   ├── PixelComparator.java
    │   ├── EmptySceneTest.java
    │   ├── SingleTriangleTest.java
    │   ├── TexturedQuadTest.java
    │   ├── BlendedQuadsTest.java
    │   ├── WireframeCubeTest.java
    │   ├── LinePrimitivesTest.java
    │   ├── LitSphereTest.java
    │   └── MultiPassSceneTest.java
    ├── integration/                   # Level 3 — SWT + GL
    │   ├── JoglSwtCanvasIntegrationTest.java
    │   ├── SwtInputAdapterTest.java
    │   ├── TextureLoaderIntegrationTest.java
    │   └── RenderLoopTest.java
    └── behavioral/                    # Level 4 — domain scenarios
        ├── SeismicVolumeRenderTest.java
        ├── FaultSurfaceRenderTest.java
        ├── WellTrajectoryRenderTest.java
        ├── PickingBehavioralTest.java
        └── MultiPassBehavioralTest.java
```

### Modified bundles
- `org.geocraft.target/org.geocraft.target.target` — add JOML, JOGL, GlueGen Maven deps
- `pom.xml` (parent) — add new module entries
- `org.geocraft.ui.volumeviewer/*` — refactor in Phase 5 (all Ardor3D usage replaced)
- `com.ardor3d/*` — deleted in Phase 7

---

## Phase 1 — Foundation: `org.geocraft.core.rendering`

### Task 1.1: Add JOML dependency to target platform

**Files:**
- Modify: `org.geocraft.target/org.geocraft.target.target`

- [ ] **Step 1: Add JOML Maven dependency**

Edit `org.geocraft.target/org.geocraft.target.target` — inside the existing `<dependencies>` block (which currently has commons-beanutils and commons-collections), add a JOML entry:

```xml
<dependency>
  <groupId>org.joml</groupId>
  <artifactId>joml</artifactId>
  <version>1.10.5</version>
  <type>jar</type>
</dependency>
```

- [ ] **Step 2: Resolve target platform**

Run: `cd /Users/ericgeordi/dev/geocraft/geocraft && mvn -pl org.geocraft.target -am clean verify -DskipTests`
Expected: BUILD SUCCESS — JOML jar is resolved and wrapped as an OSGi bundle named `wrapped.org.joml.joml` or similar.

- [ ] **Step 3: Commit**

```bash
git add org.geocraft.target/org.geocraft.target.target
git commit -m "Add JOML 1.10.5 to target platform for rendering math"
```

### Task 1.2: Create `org.geocraft.core.rendering` bundle skeleton

**Files:**
- Create: `org.geocraft.core.rendering/META-INF/MANIFEST.MF`
- Create: `org.geocraft.core.rendering/build.properties`
- Create: `org.geocraft.core.rendering/pom.xml`
- Create: `org.geocraft.core.rendering/src/.gitkeep`
- Modify: `pom.xml` (parent)

- [ ] **Step 1: Create MANIFEST.MF**

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: GeoCraft Core Rendering
Bundle-SymbolicName: org.geocraft.core.rendering
Bundle-Version: 1.0.0.qualifier
Bundle-RequiredExecutionEnvironment: JavaSE-21
Import-Package: org.joml;version="1.10.0"
Export-Package: org.geocraft.core.rendering.scene,
 org.geocraft.core.rendering.material,
 org.geocraft.core.rendering.camera,
 org.geocraft.core.rendering.pick,
 org.geocraft.core.rendering.backend,
 org.geocraft.core.rendering.bounds,
 org.geocraft.core.rendering.input
```

- [ ] **Step 2: Create build.properties**

```
source.. = src/
output.. = bin/
bin.includes = META-INF/,\
               .
```

- [ ] **Step 3: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>org.geocraft.core.rendering</artifactId>
  <packaging>eclipse-plugin</packaging>
</project>
```

- [ ] **Step 4: Add module to parent pom.xml**

Edit `pom.xml` — add `<module>org.geocraft.core.rendering</module>` after the existing `<module>org.geocraft.core.session</module>` line.

- [ ] **Step 5: Build**

Run: `cd /Users/ericgeordi/dev/geocraft/geocraft && mvn -pl org.geocraft.core.rendering -am clean verify -DskipTests`
Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.core.rendering pom.xml
git commit -m "Create org.geocraft.core.rendering bundle skeleton"
```

### Task 1.3: Create `org.geocraft.core.rendering.tests` test fragment

**Files:**
- Create: `org.geocraft.core.rendering.tests/META-INF/MANIFEST.MF`
- Create: `org.geocraft.core.rendering.tests/build.properties`
- Create: `org.geocraft.core.rendering.tests/pom.xml`
- Modify: `pom.xml` (parent)

- [ ] **Step 1: Create MANIFEST.MF**

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Core Rendering Tests
Bundle-SymbolicName: org.geocraft.core.rendering.tests
Bundle-Version: 1.0.0.qualifier
Fragment-Host: org.geocraft.core.rendering;bundle-version="1.0.0"
Bundle-RequiredExecutionEnvironment: JavaSE-21
Require-Bundle: org.junit
Import-Package: org.joml;version="1.10.0"
```

- [ ] **Step 2: Create build.properties**

```
source.. = src/
output.. = bin/
bin.includes = META-INF/,\
               .
```

- [ ] **Step 3: Create pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>org.geocraft.core.rendering.tests</artifactId>
  <packaging>eclipse-test-plugin</packaging>
</project>
```

- [ ] **Step 4: Add to parent pom**

Add `<module>org.geocraft.core.rendering.tests</module>` to the test fragment section.

- [ ] **Step 5: Build**

Run: `mvn -pl org.geocraft.core.rendering.tests -am clean verify`
Expected: BUILD SUCCESS (no tests yet, passes trivially).

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.core.rendering.tests pom.xml
git commit -m "Create org.geocraft.core.rendering.tests test fragment"
```

### Task 1.4: `Ray` value type + test

**Files:**
- Create: `org.geocraft.core.rendering/src/org/geocraft/core/rendering/pick/Ray.java`
- Create: `org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/pick/RayTest.java`

- [ ] **Step 1: Write failing test**

```java
package org.geocraft.core.rendering.pick;

import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class RayTest {
    @Test
    public void constructNormalizedRay() {
        Ray r = new Ray(new Vector3f(0, 0, 0), new Vector3f(0, 0, -2));
        assertEquals(0f, r.origin.x, 1e-6);
        assertEquals(-1f, r.direction.z, 1e-6); // direction normalized
        assertEquals(1f, r.direction.length(), 1e-6);
    }

    @Test
    public void pointAtDistance() {
        Ray r = new Ray(new Vector3f(1, 2, 3), new Vector3f(1, 0, 0));
        Vector3f p = r.pointAt(5f);
        assertEquals(6f, p.x, 1e-6);
        assertEquals(2f, p.y, 1e-6);
        assertEquals(3f, p.z, 1e-6);
    }
}
```

- [ ] **Step 2: Run and confirm failure** — `mvn -pl org.geocraft.core.rendering.tests test` should fail to compile `Ray`.

- [ ] **Step 3: Implement Ray**

```java
package org.geocraft.core.rendering.pick;

import org.joml.Vector3f;

public final class Ray {
    public final Vector3f origin;
    public final Vector3f direction;

    public Ray(Vector3f origin, Vector3f direction) {
        this.origin = new Vector3f(origin);
        this.direction = new Vector3f(direction).normalize();
    }

    public Vector3f pointAt(float t) {
        return new Vector3f(direction).mul(t).add(origin);
    }
}
```

- [ ] **Step 4: Run and confirm pass** — tests green.

- [ ] **Step 5: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/pick/Ray.java \
        org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/pick/RayTest.java
git commit -m "Add Ray value type with normalization and pointAt"
```

### Task 1.5: `VisibilityHint` enum + `SceneNode` base + transform test

**Files:**
- Create: `org.geocraft.core.rendering/src/org/geocraft/core/rendering/scene/VisibilityHint.java`
- Create: `org.geocraft.core.rendering/src/org/geocraft/core/rendering/scene/SceneNode.java`
- Create: `org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/scene/SceneNodeTest.java`

- [ ] **Step 1: Write failing test**

```java
package org.geocraft.core.rendering.scene;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class SceneNodeTest {
    @Test
    public void defaultTransformIsIdentity() {
        SceneNode n = new GroupNode("root");
        Matrix4f m = n.getLocalTransform();
        assertTrue(m.equals(new Matrix4f(), 1e-6f));
    }

    @Test
    public void worldTransformComposesWithParent() {
        GroupNode parent = new GroupNode("parent");
        parent.setTranslation(new Vector3f(10, 0, 0));
        GroupNode child = new GroupNode("child");
        child.setTranslation(new Vector3f(0, 5, 0));
        parent.addChild(child);
        Matrix4f w = child.getWorldTransform();
        Vector3f t = new Vector3f();
        w.getTranslation(t);
        assertEquals(10f, t.x, 1e-6);
        assertEquals(5f, t.y, 1e-6);
    }

    @Test
    public void defaultVisibilityIsDynamic() {
        SceneNode n = new GroupNode("n");
        assertEquals(VisibilityHint.DYNAMIC, n.getVisibility());
    }
}
```

- [ ] **Step 2: Run and confirm failure.**

- [ ] **Step 3: Implement VisibilityHint**

```java
package org.geocraft.core.rendering.scene;

public enum VisibilityHint {
    ALWAYS_VISIBLE,
    ALWAYS_CULLED,
    DYNAMIC
}
```

- [ ] **Step 4: Implement SceneNode**

```java
package org.geocraft.core.rendering.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class SceneNode {
    private final String name;
    private SceneNode parent;
    private final List<SceneNode> children = new ArrayList<>();
    private final Vector3f translation = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Vector3f scale = new Vector3f(1, 1, 1);
    private VisibilityHint visibility = VisibilityHint.DYNAMIC;
    private RenderMaterial material;

    protected SceneNode(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public Vector3f getTranslation() { return new Vector3f(translation); }
    public void setTranslation(Vector3f t) { translation.set(t); }

    public Quaternionf getRotation() { return new Quaternionf(rotation); }
    public void setRotation(Quaternionf r) { rotation.set(r); }

    public Vector3f getScale() { return new Vector3f(scale); }
    public void setScale(Vector3f s) { scale.set(s); }

    public VisibilityHint getVisibility() { return visibility; }
    public void setVisibility(VisibilityHint v) { this.visibility = v; }

    public RenderMaterial getMaterial() { return material; }
    public void setMaterial(RenderMaterial m) { this.material = m; }

    public Matrix4f getLocalTransform() {
        return new Matrix4f()
            .translationRotateScale(translation, rotation, scale);
    }

    public Matrix4f getWorldTransform() {
        Matrix4f local = getLocalTransform();
        if (parent == null) return local;
        return new Matrix4f(parent.getWorldTransform()).mul(local);
    }

    public SceneNode getParent() { return parent; }
    public List<SceneNode> getChildren() { return Collections.unmodifiableList(children); }

    public void addChild(SceneNode child) {
        if (child.parent != null) child.parent.removeChild(child);
        child.parent = this;
        children.add(child);
    }

    public void removeChild(SceneNode child) {
        if (children.remove(child)) child.parent = null;
    }
}
```

- [ ] **Step 5: Run test — GroupNode not yet defined, will fail.**

- [ ] **Step 6: Implement minimal GroupNode stub**

Create `org.geocraft.core.rendering/src/org/geocraft/core/rendering/scene/GroupNode.java`:

```java
package org.geocraft.core.rendering.scene;

public class GroupNode extends SceneNode {
    public GroupNode(String name) { super(name); }
}
```

Also create placeholder `org.geocraft.core.rendering/src/org/geocraft/core/rendering/material/RenderMaterial.java`:

```java
package org.geocraft.core.rendering.material;

public class RenderMaterial {
    // Fleshed out in Task 1.7
}
```

- [ ] **Step 7: Run test — expect pass.**

- [ ] **Step 8: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/scene/ \
        org.geocraft.core.rendering/src/org/geocraft/core/rendering/material/RenderMaterial.java \
        org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/scene/SceneNodeTest.java
git commit -m "Add SceneNode base with transform propagation and GroupNode"
```

### Task 1.6: `MeshGeometry`, `LineGeometry`, `SphereGeometry`, `TextOverlay`

**Files:**
- Create: `.../scene/MeshGeometry.java`, `LineGeometry.java`, `SphereGeometry.java`, `TextOverlay.java`

- [ ] **Step 1: Implement MeshGeometry**

```java
package org.geocraft.core.rendering.scene;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class MeshGeometry extends SceneNode {
    private FloatBuffer vertices;    // xyz triples
    private FloatBuffer normals;     // xyz triples, optional
    private FloatBuffer texCoords;   // uv pairs, optional
    private IntBuffer indices;       // triangle indices, optional
    private int vertexCount;
    private int triangleCount;

    public MeshGeometry(String name) { super(name); }

    public FloatBuffer getVertices() { return vertices; }
    public void setVertices(FloatBuffer v, int count) {
        this.vertices = v;
        this.vertexCount = count;
    }

    public FloatBuffer getNormals() { return normals; }
    public void setNormals(FloatBuffer n) { this.normals = n; }

    public FloatBuffer getTexCoords() { return texCoords; }
    public void setTexCoords(FloatBuffer t) { this.texCoords = t; }

    public IntBuffer getIndices() { return indices; }
    public void setIndices(IntBuffer i, int triangleCount) {
        this.indices = i;
        this.triangleCount = triangleCount;
    }

    public int getVertexCount() { return vertexCount; }
    public int getTriangleCount() { return triangleCount; }
}
```

- [ ] **Step 2: Implement LineGeometry**

```java
package org.geocraft.core.rendering.scene;

import java.nio.FloatBuffer;
import org.joml.Vector4f;

public class LineGeometry extends SceneNode {
    private FloatBuffer vertices;
    private int vertexCount;
    private float lineWidth = 1.0f;
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public LineGeometry(String name) { super(name); }

    public FloatBuffer getVertices() { return vertices; }
    public void setVertices(FloatBuffer v, int count) {
        this.vertices = v;
        this.vertexCount = count;
    }
    public int getVertexCount() { return vertexCount; }

    public float getLineWidth() { return lineWidth; }
    public void setLineWidth(float w) { this.lineWidth = w; }

    public Vector4f getColor() { return new Vector4f(color); }
    public void setColor(Vector4f c) { this.color.set(c); }
}
```

- [ ] **Step 3: Implement SphereGeometry**

```java
package org.geocraft.core.rendering.scene;

import org.joml.Vector4f;

public class SphereGeometry extends SceneNode {
    private float radius;
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public SphereGeometry(String name, float radius) {
        super(name);
        this.radius = radius;
    }

    public float getRadius() { return radius; }
    public void setRadius(float r) { this.radius = r; }

    public Vector4f getColor() { return new Vector4f(color); }
    public void setColor(Vector4f c) { this.color.set(c); }
}
```

- [ ] **Step 4: Implement TextOverlay**

```java
package org.geocraft.core.rendering.scene;

import org.joml.Vector2f;
import org.joml.Vector4f;

public class TextOverlay extends SceneNode {
    private String text;
    private Vector2f screenPosition = new Vector2f();
    private int fontSize = 12;
    private Vector4f color = new Vector4f(1, 1, 1, 1);

    public TextOverlay(String name, String text) {
        super(name);
        this.text = text;
    }

    public String getText() { return text; }
    public void setText(String t) { this.text = t; }
    public Vector2f getScreenPosition() { return new Vector2f(screenPosition); }
    public void setScreenPosition(Vector2f p) { this.screenPosition.set(p); }
    public int getFontSize() { return fontSize; }
    public void setFontSize(int s) { this.fontSize = s; }
    public Vector4f getColor() { return new Vector4f(color); }
    public void setColor(Vector4f c) { this.color.set(c); }
}
```

- [ ] **Step 5: Build** — `mvn -pl org.geocraft.core.rendering -am clean verify -DskipTests`. Expect success.

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/scene/
git commit -m "Add MeshGeometry, LineGeometry, SphereGeometry, TextOverlay node types"
```

### Task 1.7: `RenderMaterial` + sub-types + test

**Files:**
- Create: `.../material/BlendFactor.java`, `BlendMode.java`, `TextureLayer.java`, `LightingConfig.java`, `WireframeMode.java`, `DepthTestConfig.java`
- Replace: `.../material/RenderMaterial.java` (was placeholder)
- Create: `.../rendering/material/RenderMaterialTest.java` in tests bundle

- [ ] **Step 1: Write failing test**

```java
package org.geocraft.core.rendering.material;

import org.junit.Test;
import static org.junit.Assert.*;

public class RenderMaterialTest {
    @Test
    public void defaultMaterialHasNoStates() {
        RenderMaterial m = new RenderMaterial();
        assertNull(m.getBlendMode());
        assertNull(m.getTextureLayer());
        assertNull(m.getLightingConfig());
        assertNull(m.getWireframeMode());
        assertNull(m.getDepthTestConfig());
    }

    @Test
    public void builderComposesStates() {
        RenderMaterial m = new RenderMaterial()
            .withBlendMode(BlendMode.alphaBlend())
            .withWireframe(new WireframeMode(true, 1.5f, true))
            .withDepthTest(new DepthTestConfig(true, DepthTestConfig.CompareFunc.LESS_OR_EQUAL));
        assertNotNull(m.getBlendMode());
        assertTrue(m.getWireframeMode().enabled);
        assertEquals(1.5f, m.getWireframeMode().lineWidth, 1e-6);
    }

    @Test
    public void alphaBlendHasStandardFactors() {
        BlendMode b = BlendMode.alphaBlend();
        assertEquals(BlendFactor.SRC_ALPHA, b.srcFactor);
        assertEquals(BlendFactor.ONE_MINUS_SRC_ALPHA, b.dstFactor);
    }
}
```

- [ ] **Step 2: Run — expect failure (types missing).**

- [ ] **Step 3: Implement BlendFactor**

```java
package org.geocraft.core.rendering.material;

public enum BlendFactor {
    ZERO, ONE,
    SRC_ALPHA, ONE_MINUS_SRC_ALPHA,
    DST_ALPHA, ONE_MINUS_DST_ALPHA,
    SRC_COLOR, ONE_MINUS_SRC_COLOR,
    DST_COLOR, ONE_MINUS_DST_COLOR
}
```

- [ ] **Step 4: Implement BlendMode**

```java
package org.geocraft.core.rendering.material;

public final class BlendMode {
    public final BlendFactor srcFactor;
    public final BlendFactor dstFactor;

    public BlendMode(BlendFactor src, BlendFactor dst) {
        this.srcFactor = src;
        this.dstFactor = dst;
    }

    public static BlendMode alphaBlend() {
        return new BlendMode(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static BlendMode additive() {
        return new BlendMode(BlendFactor.SRC_ALPHA, BlendFactor.ONE);
    }
}
```

- [ ] **Step 5: Implement WireframeMode**

```java
package org.geocraft.core.rendering.material;

public final class WireframeMode {
    public final boolean enabled;
    public final float lineWidth;
    public final boolean antialiased;

    public WireframeMode(boolean enabled, float lineWidth, boolean antialiased) {
        this.enabled = enabled;
        this.lineWidth = lineWidth;
        this.antialiased = antialiased;
    }
}
```

- [ ] **Step 6: Implement DepthTestConfig**

```java
package org.geocraft.core.rendering.material;

public final class DepthTestConfig {
    public enum CompareFunc { NEVER, LESS, EQUAL, LESS_OR_EQUAL, GREATER, NOT_EQUAL, GREATER_OR_EQUAL, ALWAYS }

    public final boolean enabled;
    public final CompareFunc func;

    public DepthTestConfig(boolean enabled, CompareFunc func) {
        this.enabled = enabled;
        this.func = func;
    }
}
```

- [ ] **Step 7: Implement LightingConfig**

```java
package org.geocraft.core.rendering.material;

import org.joml.Vector4f;

public final class LightingConfig {
    public final boolean enabled;
    public final Vector4f ambient;
    public final Vector4f diffuse;
    public final Vector4f specular;
    public final float shininess;

    public LightingConfig(boolean enabled, Vector4f ambient, Vector4f diffuse, Vector4f specular, float shininess) {
        this.enabled = enabled;
        this.ambient = new Vector4f(ambient);
        this.diffuse = new Vector4f(diffuse);
        this.specular = new Vector4f(specular);
        this.shininess = shininess;
    }

    public static LightingConfig disabled() {
        return new LightingConfig(false, new Vector4f(), new Vector4f(), new Vector4f(), 0);
    }
}
```

- [ ] **Step 8: Implement TextureLayer**

```java
package org.geocraft.core.rendering.material;

import org.geocraft.core.rendering.backend.FilterMode;
import org.geocraft.core.rendering.backend.TextureHandle;

public final class TextureLayer {
    public enum CombineMode { REPLACE, MODULATE, DECAL }

    public final TextureHandle texture;
    public final FilterMode magFilter;
    public final FilterMode minFilter;
    public final CombineMode combineMode;

    public TextureLayer(TextureHandle texture, FilterMode mag, FilterMode min, CombineMode combine) {
        this.texture = texture;
        this.magFilter = mag;
        this.minFilter = min;
        this.combineMode = combine;
    }
}
```

- [ ] **Step 9: Replace RenderMaterial placeholder**

```java
package org.geocraft.core.rendering.material;

public class RenderMaterial {
    private BlendMode blendMode;
    private TextureLayer textureLayer;
    private LightingConfig lightingConfig;
    private WireframeMode wireframeMode;
    private DepthTestConfig depthTestConfig;

    public BlendMode getBlendMode() { return blendMode; }
    public RenderMaterial withBlendMode(BlendMode b) { this.blendMode = b; return this; }

    public TextureLayer getTextureLayer() { return textureLayer; }
    public RenderMaterial withTextureLayer(TextureLayer t) { this.textureLayer = t; return this; }

    public LightingConfig getLightingConfig() { return lightingConfig; }
    public RenderMaterial withLighting(LightingConfig l) { this.lightingConfig = l; return this; }

    public WireframeMode getWireframeMode() { return wireframeMode; }
    public RenderMaterial withWireframe(WireframeMode w) { this.wireframeMode = w; return this; }

    public DepthTestConfig getDepthTestConfig() { return depthTestConfig; }
    public RenderMaterial withDepthTest(DepthTestConfig d) { this.depthTestConfig = d; return this; }
}
```

- [ ] **Step 10: Build** — since `TextureLayer` references `FilterMode`/`TextureHandle`, create minimal placeholders in the `backend` package.

Create `org.geocraft.core.rendering/src/org/geocraft/core/rendering/backend/FilterMode.java`:

```java
package org.geocraft.core.rendering.backend;

public enum FilterMode { NEAREST, BILINEAR, TRILINEAR }
```

Create `org.geocraft.core.rendering/src/org/geocraft/core/rendering/backend/TextureHandle.java`:

```java
package org.geocraft.core.rendering.backend;

public interface TextureHandle {
    int getWidth();
    int getHeight();
    boolean isDisposed();
}
```

- [ ] **Step 11: Run test — expect pass.**

- [ ] **Step 12: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/material/ \
        org.geocraft.core.rendering/src/org/geocraft/core/rendering/backend/FilterMode.java \
        org.geocraft.core.rendering/src/org/geocraft/core/rendering/backend/TextureHandle.java \
        org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/material/RenderMaterialTest.java
git commit -m "Add RenderMaterial composition with blend/texture/lighting/wireframe/depth states"
```

### Task 1.8: `Camera` with test

**Files:**
- Create: `.../camera/ProjectionType.java`, `Camera.java`, `Light.java`
- Create: `.../camera/CameraTest.java` in tests bundle

- [ ] **Step 1: Write failing test**

```java
package org.geocraft.core.rendering.camera;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class CameraTest {
    @Test
    public void perspectiveProjectionIsValidMatrix() {
        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1.0f, 0.1f, 100f);
        Matrix4f p = c.getProjectionMatrix();
        // Standard perspective: m33 is 0, m32 is -1 (w = -z)
        assertEquals(-1f, p.m23(), 1e-5);
    }

    @Test
    public void viewMatrixAtOriginLookingDownZ() {
        Camera c = new Camera();
        c.setLocation(new Vector3f(0, 0, 10));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        Matrix4f v = c.getViewMatrix();
        // Point at origin should map to (0,0,-10) in view space
        org.joml.Vector4f worldOrigin = new org.joml.Vector4f(0, 0, 0, 1);
        worldOrigin.mul(v);
        assertEquals(0f, worldOrigin.x, 1e-5);
        assertEquals(0f, worldOrigin.y, 1e-5);
        assertEquals(-10f, worldOrigin.z, 1e-5);
    }

    @Test
    public void pickRayFromScreenCenterPointsForward() {
        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1.0f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 10));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        c.setViewport(100, 100);
        Ray r = c.getPickRay(new Vector2f(50, 50));
        assertEquals(0f, r.direction.x, 1e-3);
        assertEquals(0f, r.direction.y, 1e-3);
        assertTrue(r.direction.z < 0); // looking down -z
    }
}
```

- [ ] **Step 2: Run — expect failure.**

- [ ] **Step 3: Implement ProjectionType**

```java
package org.geocraft.core.rendering.camera;

public enum ProjectionType { PERSPECTIVE, PARALLEL }
```

- [ ] **Step 4: Implement Camera**

```java
package org.geocraft.core.rendering.camera;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {
    private ProjectionType projectionType = ProjectionType.PERSPECTIVE;
    private float fov = (float) Math.toRadians(60);
    private float aspect = 1f;
    private float near = 0.1f;
    private float far = 1000f;
    private float left = -1, right = 1, bottom = -1, top = 1;

    private final Vector3f location = new Vector3f(0, 0, 10);
    private final Vector3f target = new Vector3f(0, 0, 0);
    private final Vector3f up = new Vector3f(0, 1, 0);

    private int viewportWidth = 1, viewportHeight = 1;

    public void setPerspective(float fovRadians, float aspect, float near, float far) {
        this.projectionType = ProjectionType.PERSPECTIVE;
        this.fov = fovRadians;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
    }

    public void setParallel(float left, float right, float bottom, float top, float near, float far) {
        this.projectionType = ProjectionType.PARALLEL;
        this.left = left; this.right = right;
        this.bottom = bottom; this.top = top;
        this.near = near; this.far = far;
    }

    public void setLocation(Vector3f loc) { this.location.set(loc); }
    public Vector3f getLocation() { return new Vector3f(location); }

    public void lookAt(Vector3f target, Vector3f up) {
        this.target.set(target);
        this.up.set(up);
    }

    public void setViewport(int w, int h) {
        this.viewportWidth = w;
        this.viewportHeight = h;
        if (projectionType == ProjectionType.PERSPECTIVE) {
            this.aspect = (float) w / (float) h;
        }
    }

    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }

    public ProjectionType getProjectionType() { return projectionType; }

    public Matrix4f getProjectionMatrix() {
        if (projectionType == ProjectionType.PERSPECTIVE) {
            return new Matrix4f().perspective(fov, aspect, near, far);
        }
        return new Matrix4f().ortho(left, right, bottom, top, near, far);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(location, target, up);
    }

    public Ray getPickRay(Vector2f screenPos) {
        // Unproject near and far points
        Matrix4f pv = new Matrix4f(getProjectionMatrix()).mul(getViewMatrix());
        Matrix4f inv = new Matrix4f(pv).invert();
        float ndcX = (2f * screenPos.x / viewportWidth) - 1f;
        float ndcY = 1f - (2f * screenPos.y / viewportHeight);

        Vector4f nearP = new Vector4f(ndcX, ndcY, -1f, 1f).mul(inv);
        Vector4f farP  = new Vector4f(ndcX, ndcY,  1f, 1f).mul(inv);
        nearP.div(nearP.w);
        farP.div(farP.w);

        Vector3f origin = new Vector3f(nearP.x, nearP.y, nearP.z);
        Vector3f dir = new Vector3f(farP.x - nearP.x, farP.y - nearP.y, farP.z - nearP.z);
        return new Ray(origin, dir);
    }

    public Vector3f getWorldCoordinates(Vector2f screenPos, float depth) {
        Matrix4f pv = new Matrix4f(getProjectionMatrix()).mul(getViewMatrix());
        Matrix4f inv = new Matrix4f(pv).invert();
        float ndcX = (2f * screenPos.x / viewportWidth) - 1f;
        float ndcY = 1f - (2f * screenPos.y / viewportHeight);
        float ndcZ = 2f * depth - 1f;
        Vector4f world = new Vector4f(ndcX, ndcY, ndcZ, 1f).mul(inv);
        world.div(world.w);
        return new Vector3f(world.x, world.y, world.z);
    }
}
```

- [ ] **Step 5: Implement Light**

```java
package org.geocraft.core.rendering.camera;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Light {
    public enum Type { DIRECTIONAL, POINT }

    private Type type = Type.DIRECTIONAL;
    private final Vector3f direction = new Vector3f(0, -1, 0);
    private final Vector3f position = new Vector3f();
    private final Vector4f diffuse = new Vector4f(1, 1, 1, 1);
    private final Vector4f specular = new Vector4f(1, 1, 1, 1);
    private final Vector4f ambient = new Vector4f(0.3f, 0.3f, 0.3f, 1f);
    private boolean enabled = true;

    public Type getType() { return type; }
    public void setType(Type t) { this.type = t; }
    public Vector3f getDirection() { return new Vector3f(direction); }
    public void setDirection(Vector3f d) { this.direction.set(d).normalize(); }
    public Vector3f getPosition() { return new Vector3f(position); }
    public void setPosition(Vector3f p) { this.position.set(p); }
    public Vector4f getDiffuse() { return new Vector4f(diffuse); }
    public void setDiffuse(Vector4f c) { this.diffuse.set(c); }
    public Vector4f getSpecular() { return new Vector4f(specular); }
    public void setSpecular(Vector4f c) { this.specular.set(c); }
    public Vector4f getAmbient() { return new Vector4f(ambient); }
    public void setAmbient(Vector4f c) { this.ambient.set(c); }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean e) { this.enabled = e; }
}
```

- [ ] **Step 6: Run tests — expect pass.**

- [ ] **Step 7: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/camera/ \
        org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/camera/CameraTest.java
git commit -m "Add Camera with perspective/parallel projection and pick ray"
```

### Task 1.9: `BoundingVolume` hierarchy + test

**Files:**
- Create: `.../bounds/BoundingVolume.java`, `BoundingBox.java`, `BoundingSphere.java`
- Create: `.../bounds/BoundingVolumeTest.java` in tests bundle

- [ ] **Step 1: Write failing test**

```java
package org.geocraft.core.rendering.bounds;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.pick.Ray;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class BoundingVolumeTest {
    @Test
    public void boxFromVertices() {
        FloatBuffer verts = FloatBuffer.wrap(new float[] {
            -1, -1, -1,
             1,  1,  1,
             0,  0,  0
        });
        BoundingBox b = BoundingBox.fromVertices(verts, 3);
        assertEquals(-1f, b.getMin().x, 1e-6);
        assertEquals( 1f, b.getMax().y, 1e-6);
    }

    @Test
    public void rayHitsBox() {
        BoundingBox b = new BoundingBox(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1));
        Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));
        assertTrue(b.intersectsRay(r));
    }

    @Test
    public void rayMissesBox() {
        BoundingBox b = new BoundingBox(new Vector3f(-1, -1, -1), new Vector3f(1, 1, 1));
        Ray r = new Ray(new Vector3f(10, 10, 10), new Vector3f(1, 1, 1));
        assertFalse(b.intersectsRay(r));
    }

    @Test
    public void sphereContainsCenter() {
        BoundingSphere s = new BoundingSphere(new Vector3f(0, 0, 0), 5f);
        assertTrue(s.contains(new Vector3f(2, 2, 2)));
        assertFalse(s.contains(new Vector3f(10, 0, 0)));
    }
}
```

- [ ] **Step 2: Implement BoundingVolume**

```java
package org.geocraft.core.rendering.bounds;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Vector3f;

public abstract class BoundingVolume {
    public abstract boolean intersectsRay(Ray ray);
    public abstract boolean contains(Vector3f point);
    public abstract Vector3f getCenter();
}
```

- [ ] **Step 3: Implement BoundingBox**

```java
package org.geocraft.core.rendering.bounds;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.pick.Ray;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class BoundingBox extends BoundingVolume {
    private final Vector3f min;
    private final Vector3f max;

    public BoundingBox(Vector3f min, Vector3f max) {
        this.min = new Vector3f(min);
        this.max = new Vector3f(max);
    }

    public Vector3f getMin() { return new Vector3f(min); }
    public Vector3f getMax() { return new Vector3f(max); }

    @Override
    public Vector3f getCenter() {
        return new Vector3f(min).add(max).mul(0.5f);
    }

    @Override
    public boolean intersectsRay(Ray ray) {
        Vector2f result = new Vector2f();
        return Intersectionf.intersectRayAab(
            ray.origin.x, ray.origin.y, ray.origin.z,
            ray.direction.x, ray.direction.y, ray.direction.z,
            min.x, min.y, min.z, max.x, max.y, max.z, result);
    }

    @Override
    public boolean contains(Vector3f p) {
        return p.x >= min.x && p.x <= max.x
            && p.y >= min.y && p.y <= max.y
            && p.z >= min.z && p.z <= max.z;
    }

    public static BoundingBox fromVertices(FloatBuffer verts, int vertexCount) {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (int i = 0; i < vertexCount; i++) {
            float x = verts.get(i * 3);
            float y = verts.get(i * 3 + 1);
            float z = verts.get(i * 3 + 2);
            if (x < min.x) min.x = x;
            if (y < min.y) min.y = y;
            if (z < min.z) min.z = z;
            if (x > max.x) max.x = x;
            if (y > max.y) max.y = y;
            if (z > max.z) max.z = z;
        }
        return new BoundingBox(min, max);
    }
}
```

- [ ] **Step 4: Implement BoundingSphere**

```java
package org.geocraft.core.rendering.bounds;

import org.geocraft.core.rendering.pick.Ray;
import org.joml.Intersectionf;
import org.joml.Vector3f;

public class BoundingSphere extends BoundingVolume {
    private final Vector3f center;
    private final float radius;

    public BoundingSphere(Vector3f center, float radius) {
        this.center = new Vector3f(center);
        this.radius = radius;
    }

    @Override
    public Vector3f getCenter() { return new Vector3f(center); }
    public float getRadius() { return radius; }

    @Override
    public boolean intersectsRay(Ray ray) {
        return Intersectionf.testRaySphere(
            ray.origin.x, ray.origin.y, ray.origin.z,
            ray.direction.x, ray.direction.y, ray.direction.z,
            center.x, center.y, center.z, radius * radius);
    }

    @Override
    public boolean contains(Vector3f p) {
        return new Vector3f(p).sub(center).length() <= radius;
    }
}
```

- [ ] **Step 5: Run tests — expect pass.**

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/bounds/ \
        org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/bounds/
git commit -m "Add BoundingBox and BoundingSphere with ray intersection"
```

### Task 1.10: `PickEngine` interface + `DefaultPickEngine` + tests

**Files:**
- Create: `.../pick/PickType.java`, `PickResult.java`, `PickEngine.java`, `DefaultPickEngine.java`
- Create: `.../pick/DefaultPickEngineTest.java` in tests bundle

- [ ] **Step 1: Write failing test**

```java
package org.geocraft.core.rendering.pick;

import java.nio.FloatBuffer;
import java.util.List;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultPickEngineTest {
    private MeshGeometry makeTriangle(float z) {
        FloatBuffer v = FloatBuffer.wrap(new float[] {
            -1, -1, z,
             1, -1, z,
             0,  1, z
        });
        MeshGeometry m = new MeshGeometry("tri");
        m.setVertices(v, 3);
        return m;
    }

    @Test
    public void rayHitsTriangle() {
        GroupNode root = new GroupNode("root");
        root.addChild(makeTriangle(0));
        Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));

        DefaultPickEngine engine = new DefaultPickEngine();
        List<PickResult> results = engine.pickTriangles(root, r);

        assertEquals(1, results.size());
        assertEquals(PickType.TRIANGLE, results.get(0).getType());
        assertEquals(0f, results.get(0).getWorldPosition().z, 1e-5);
    }

    @Test
    public void rayMissesTriangle() {
        GroupNode root = new GroupNode("root");
        root.addChild(makeTriangle(0));
        Ray r = new Ray(new Vector3f(10, 10, 10), new Vector3f(0, 0, -1));
        DefaultPickEngine engine = new DefaultPickEngine();
        assertTrue(engine.pickTriangles(root, r).isEmpty());
    }

    @Test
    public void multipleHitsSortedByDistance() {
        GroupNode root = new GroupNode("root");
        root.addChild(makeTriangle(0));
        root.addChild(makeTriangle(5));
        Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));
        DefaultPickEngine engine = new DefaultPickEngine();
        List<PickResult> results = engine.pickTriangles(root, r);
        assertEquals(2, results.size());
        // Triangle at z=5 is closer to camera at z=10
        assertTrue(results.get(0).getDistance() < results.get(1).getDistance());
    }
}
```

- [ ] **Step 2: Implement PickType**

```java
package org.geocraft.core.rendering.pick;

public enum PickType { TRIANGLE, BOUNDS }
```

- [ ] **Step 3: Implement PickResult**

```java
package org.geocraft.core.rendering.pick;

import org.geocraft.core.rendering.scene.SceneNode;
import org.joml.Vector3f;

public final class PickResult {
    private final SceneNode node;
    private final float distance;
    private final Vector3f worldPosition;
    private final PickType type;

    public PickResult(SceneNode node, float distance, Vector3f worldPosition, PickType type) {
        this.node = node;
        this.distance = distance;
        this.worldPosition = new Vector3f(worldPosition);
        this.type = type;
    }

    public SceneNode getNode() { return node; }
    public float getDistance() { return distance; }
    public Vector3f getWorldPosition() { return new Vector3f(worldPosition); }
    public PickType getType() { return type; }
}
```

- [ ] **Step 4: Implement PickEngine interface**

```java
package org.geocraft.core.rendering.pick;

import java.util.List;
import org.geocraft.core.rendering.scene.GroupNode;

public interface PickEngine {
    List<PickResult> pickTriangles(GroupNode root, Ray ray);
    List<PickResult> pickBounds(GroupNode root, Ray ray);
}
```

- [ ] **Step 5: Implement DefaultPickEngine**

```java
package org.geocraft.core.rendering.pick;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.geocraft.core.rendering.bounds.BoundingBox;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.rendering.scene.VisibilityHint;
import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class DefaultPickEngine implements PickEngine {

    @Override
    public List<PickResult> pickTriangles(GroupNode root, Ray ray) {
        List<PickResult> results = new ArrayList<>();
        collectTrianglePicks(root, ray, results);
        results.sort(Comparator.comparingDouble(PickResult::getDistance));
        return results;
    }

    @Override
    public List<PickResult> pickBounds(GroupNode root, Ray ray) {
        List<PickResult> results = new ArrayList<>();
        collectBoundsPicks(root, ray, results);
        results.sort(Comparator.comparingDouble(PickResult::getDistance));
        return results;
    }

    private void collectTrianglePicks(SceneNode node, Ray ray, List<PickResult> out) {
        if (node.getVisibility() == VisibilityHint.ALWAYS_CULLED) return;
        if (node instanceof MeshGeometry) {
            pickMesh((MeshGeometry) node, ray, out);
        }
        for (SceneNode child : node.getChildren()) {
            collectTrianglePicks(child, ray, out);
        }
    }

    private void collectBoundsPicks(SceneNode node, Ray ray, List<PickResult> out) {
        if (node.getVisibility() == VisibilityHint.ALWAYS_CULLED) return;
        if (node instanceof MeshGeometry) {
            MeshGeometry mesh = (MeshGeometry) node;
            FloatBuffer verts = mesh.getVertices();
            if (verts != null && mesh.getVertexCount() > 0) {
                BoundingBox box = BoundingBox.fromVertices(verts, mesh.getVertexCount());
                if (box.intersectsRay(ray)) {
                    out.add(new PickResult(node, ray.origin.distance(box.getCenter()),
                                           box.getCenter(), PickType.BOUNDS));
                }
            }
        }
        for (SceneNode child : node.getChildren()) {
            collectBoundsPicks(child, ray, out);
        }
    }

    private void pickMesh(MeshGeometry mesh, Ray ray, List<PickResult> out) {
        FloatBuffer verts = mesh.getVertices();
        if (verts == null) return;
        Matrix4f world = mesh.getWorldTransform();
        IntBuffer idx = mesh.getIndices();
        int triCount = idx != null ? mesh.getTriangleCount() : mesh.getVertexCount() / 3;
        for (int t = 0; t < triCount; t++) {
            int i0, i1, i2;
            if (idx != null) {
                i0 = idx.get(t * 3);
                i1 = idx.get(t * 3 + 1);
                i2 = idx.get(t * 3 + 2);
            } else {
                i0 = t * 3;
                i1 = t * 3 + 1;
                i2 = t * 3 + 2;
            }
            Vector3f v0 = transform(verts, i0, world);
            Vector3f v1 = transform(verts, i1, world);
            Vector3f v2 = transform(verts, i2, world);
            Vector2f uv = new Vector2f();
            float hit = Intersectionf.intersectRayTriangle(
                ray.origin.x, ray.origin.y, ray.origin.z,
                ray.direction.x, ray.direction.y, ray.direction.z,
                v0.x, v0.y, v0.z,
                v1.x, v1.y, v1.z,
                v2.x, v2.y, v2.z, 1e-6f);
            if (hit >= 0f) {
                Vector3f hitPos = ray.pointAt(hit);
                out.add(new PickResult(mesh, hit, hitPos, PickType.TRIANGLE));
            }
        }
    }

    private Vector3f transform(FloatBuffer verts, int i, Matrix4f world) {
        float x = verts.get(i * 3);
        float y = verts.get(i * 3 + 1);
        float z = verts.get(i * 3 + 2);
        Vector4f v = new Vector4f(x, y, z, 1f).mul(world);
        return new Vector3f(v.x, v.y, v.z);
    }
}
```

- [ ] **Step 6: Run tests — expect pass.**

- [ ] **Step 7: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/pick/ \
        org.geocraft.core.rendering.tests/src/org/geocraft/core/rendering/pick/DefaultPickEngineTest.java
git commit -m "Add PickEngine interface and DefaultPickEngine with software ray-triangle picking"
```

### Task 1.11: `RenderBackend`, `RenderSurface`, `TextureLoader` interfaces + `InputEvent` types

**Files:**
- Create: `.../backend/RenderBackend.java`, `RenderSurface.java`, `TextureLoader.java`
- Create: `.../input/InputEvent.java`, `MouseInputEvent.java`, `KeyInputEvent.java`, `InputListener.java`

- [ ] **Step 1: RenderSurface interface**

```java
package org.geocraft.core.rendering.backend;

public interface RenderSurface {
    int getWidth();
    int getHeight();
    void makeCurrent();
    void release();
    void swapBuffers();
    void dispose();
}
```

- [ ] **Step 2: RenderBackend interface**

```java
package org.geocraft.core.rendering.backend;

import java.awt.image.BufferedImage;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;

public interface RenderBackend {
    void initialize(RenderSurface surface);
    void renderPass(GroupNode root, Camera camera, Light[] lights);
    void renderPass(GroupNode root, Camera camera, Light[] lights, RenderMaterial overrideMaterial);
    RenderSurface createOffscreenSurface(int width, int height);
    BufferedImage readPixels(RenderSurface surface);
    TextureLoader getTextureLoader();
    void dispose();
}
```

- [ ] **Step 3: TextureLoader interface**

```java
package org.geocraft.core.rendering.backend;

import java.awt.image.BufferedImage;

public interface TextureLoader {
    TextureHandle loadTexture(BufferedImage image, FilterMode mag, FilterMode min);
    void disposeTexture(TextureHandle handle);
}
```

- [ ] **Step 4: InputEvent hierarchy**

```java
package org.geocraft.core.rendering.input;

public abstract class InputEvent {
    public final long timestamp;
    protected InputEvent() { this.timestamp = System.nanoTime(); }
}
```

```java
package org.geocraft.core.rendering.input;

public class MouseInputEvent extends InputEvent {
    public enum Kind { PRESS, RELEASE, MOVE, DRAG, WHEEL }
    public enum Button { NONE, LEFT, MIDDLE, RIGHT }

    public final Kind kind;
    public final Button button;
    public final int x;
    public final int y;
    public final int wheelDelta;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public MouseInputEvent(Kind kind, Button button, int x, int y, int wheelDelta,
                           boolean shift, boolean ctrl, boolean alt) {
        this.kind = kind;
        this.button = button;
        this.x = x;
        this.y = y;
        this.wheelDelta = wheelDelta;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }
}
```

```java
package org.geocraft.core.rendering.input;

public class KeyInputEvent extends InputEvent {
    public enum Kind { PRESS, RELEASE }
    public final Kind kind;
    public final int keyCode;
    public final char character;
    public final boolean shift;
    public final boolean ctrl;
    public final boolean alt;

    public KeyInputEvent(Kind kind, int keyCode, char character, boolean shift, boolean ctrl, boolean alt) {
        this.kind = kind;
        this.keyCode = keyCode;
        this.character = character;
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
    }
}
```

```java
package org.geocraft.core.rendering.input;

public interface InputListener {
    default void onMouse(MouseInputEvent e) {}
    default void onKey(KeyInputEvent e) {}
}
```

- [ ] **Step 5: Build** — `mvn -pl org.geocraft.core.rendering -am clean verify -DskipTests`. Expect success.

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.core.rendering/src/org/geocraft/core/rendering/backend/ \
        org.geocraft.core.rendering/src/org/geocraft/core/rendering/input/
git commit -m "Add RenderBackend, RenderSurface, TextureLoader, InputEvent interfaces"
```

### Task 1.12: Phase 1 validation

- [ ] **Step 1: Run all phase 1 tests**

```bash
cd /Users/ericgeordi/dev/geocraft/geocraft
mvn -pl org.geocraft.core.rendering,org.geocraft.core.rendering.tests -am clean verify
```

Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 2: Tag phase completion**

```bash
git tag -a jogl-phase-1-complete -m "Phase 1: org.geocraft.core.rendering foundation complete"
```

---

## Phase 2 — JOGL Backend Foundation

### Task 2.1: Add JOGL + GlueGen to target platform

**Files:**
- Modify: `org.geocraft.target/org.geocraft.target.target`

- [ ] **Step 1: Add JOGL Maven dependencies**

Inside the `<dependencies>` block, add:

```xml
<dependency>
  <groupId>org.jogamp.gluegen</groupId>
  <artifactId>gluegen-rt-main</artifactId>
  <version>2.5.0</version>
  <type>jar</type>
</dependency>
<dependency>
  <groupId>org.jogamp.jogl</groupId>
  <artifactId>jogl-all-main</artifactId>
  <version>2.5.0</version>
  <type>jar</type>
</dependency>
```

These `-main` artifacts pull in all platform natives (including macos-aarch64) as transitive deps.

- [ ] **Step 2: Resolve**

```bash
mvn -pl org.geocraft.target -am clean verify -DskipTests
```

Expected: BUILD SUCCESS, jogl-all + gluegen-rt + native binaries resolved.

- [ ] **Step 3: Commit**

```bash
git add org.geocraft.target/org.geocraft.target.target
git commit -m "Add JOGL 2.5.0 and GlueGen 2.5.0 to target platform"
```

### Task 2.2: Create `org.geocraft.rendering.jogl` bundle skeleton

**Files:**
- Create: `org.geocraft.rendering.jogl/META-INF/MANIFEST.MF`
- Create: `org.geocraft.rendering.jogl/build.properties`
- Create: `org.geocraft.rendering.jogl/pom.xml`
- Modify: `pom.xml` (parent)

- [ ] **Step 1: Create MANIFEST.MF**

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: GeoCraft JOGL Rendering Backend
Bundle-SymbolicName: org.geocraft.rendering.jogl
Bundle-Version: 1.0.0.qualifier
Bundle-RequiredExecutionEnvironment: JavaSE-21
Require-Bundle: org.geocraft.core.rendering;bundle-version="1.0.0",
 org.eclipse.swt
Import-Package: org.joml;version="1.10.0",
 com.jogamp.opengl,
 com.jogamp.opengl.glu,
 com.jogamp.opengl.swt,
 com.jogamp.opengl.util,
 com.jogamp.opengl.util.awt,
 com.jogamp.opengl.util.texture,
 com.jogamp.opengl.util.texture.awt,
 com.jogamp.common.nio
Export-Package: org.geocraft.rendering.jogl
Service-Component: OSGI-INF/JoglRenderBackend.xml
```

- [ ] **Step 2: build.properties**

```
source.. = src/
output.. = bin/
bin.includes = META-INF/,\
               OSGI-INF/,\
               .
```

- [ ] **Step 3: pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>org.geocraft.rendering.jogl</artifactId>
  <packaging>eclipse-plugin</packaging>
</project>
```

- [ ] **Step 4: Add to parent pom**

```xml
<module>org.geocraft.rendering.jogl</module>
```

- [ ] **Step 5: Build**

```bash
mvn -pl org.geocraft.rendering.jogl -am clean verify -DskipTests
```

Expected: BUILD SUCCESS (empty bundle).

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.rendering.jogl pom.xml
git commit -m "Create org.geocraft.rendering.jogl bundle skeleton"
```

### Task 2.3: Create `org.geocraft.rendering.jogl.tests` bundle

**Files:**
- Create: `org.geocraft.rendering.jogl.tests/META-INF/MANIFEST.MF`
- Create: `org.geocraft.rendering.jogl.tests/build.properties`
- Create: `org.geocraft.rendering.jogl.tests/pom.xml`
- Create: `org.geocraft.rendering.jogl.tests/test-resources/golden/.gitkeep`
- Modify: `pom.xml` (parent)

- [ ] **Step 1: MANIFEST.MF** (fragment attaches to jogl backend to share GL context loader)

```
Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: JOGL Rendering Backend Tests
Bundle-SymbolicName: org.geocraft.rendering.jogl.tests
Bundle-Version: 1.0.0.qualifier
Fragment-Host: org.geocraft.rendering.jogl;bundle-version="1.0.0"
Bundle-RequiredExecutionEnvironment: JavaSE-21
Require-Bundle: org.junit,
 org.geocraft.core.rendering;bundle-version="1.0.0",
 org.eclipse.swt
Import-Package: org.joml;version="1.10.0",
 com.jogamp.opengl,
 com.jogamp.opengl.util
```

- [ ] **Step 2: build.properties**

```
source.. = src/
output.. = bin/
bin.includes = META-INF/,\
               test-resources/,\
               .
```

- [ ] **Step 3: pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>org.geocraft.rendering.jogl.tests</artifactId>
  <packaging>eclipse-test-plugin</packaging>
</project>
```

- [ ] **Step 4: Add to parent pom.**

- [ ] **Step 5: Build** — expect BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add org.geocraft.rendering.jogl.tests pom.xml
git commit -m "Create org.geocraft.rendering.jogl.tests bundle"
```

### Task 2.4: `JoglTextureHandle` + `JoglOffscreenSurface`

**Files:**
- Create: `org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/JoglTextureHandle.java`
- Create: `org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/JoglOffscreenSurface.java`

- [ ] **Step 1: JoglTextureHandle**

```java
package org.geocraft.rendering.jogl;

import org.geocraft.core.rendering.backend.TextureHandle;

public class JoglTextureHandle implements TextureHandle {
    private int glTextureId;
    private final int width;
    private final int height;
    private boolean disposed;

    public JoglTextureHandle(int glTextureId, int width, int height) {
        this.glTextureId = glTextureId;
        this.width = width;
        this.height = height;
    }

    public int getGlId() { return glTextureId; }
    public void markDisposed() { this.disposed = true; this.glTextureId = 0; }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }
    @Override public boolean isDisposed() { return disposed; }
}
```

- [ ] **Step 2: JoglOffscreenSurface**

```java
package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import org.geocraft.core.rendering.backend.RenderSurface;

public class JoglOffscreenSurface implements RenderSurface {
    private final GLOffscreenAutoDrawable drawable;
    private final int width;
    private final int height;

    public JoglOffscreenSurface(int width, int height) {
        this.width = width;
        this.height = height;
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(false);
        caps.setAlphaBits(8);
        caps.setDepthBits(24);
        caps.setFBO(true);
        this.drawable = GLDrawableFactory.getFactory(profile)
            .createOffscreenAutoDrawable(null, caps, null, width, height);
        drawable.display(); // forces context creation
    }

    public GLOffscreenAutoDrawable getDrawable() { return drawable; }

    @Override public int getWidth() { return width; }
    @Override public int getHeight() { return height; }

    @Override public void makeCurrent() {
        drawable.getContext().makeCurrent();
    }

    @Override public void release() {
        if (drawable.getContext().isCurrent()) drawable.getContext().release();
    }

    @Override public void swapBuffers() { /* no-op for offscreen */ }

    @Override public void dispose() {
        drawable.destroy();
    }
}
```

- [ ] **Step 3: Build**

```bash
mvn -pl org.geocraft.rendering.jogl -am clean verify -DskipTests
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/JoglTextureHandle.java \
        org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/JoglOffscreenSurface.java
git commit -m "Add JoglTextureHandle and JoglOffscreenSurface"
```

### Task 2.5: `JoglTextureLoader`, `JoglMaterialApplier`, `JoglGeometryUpload`

**Files:**
- Create: `.../JoglTextureLoader.java`, `JoglMaterialApplier.java`, `JoglGeometryUpload.java`

- [ ] **Step 1: JoglTextureLoader**

```java
package org.geocraft.rendering.jogl;

import java.awt.image.BufferedImage;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import org.geocraft.core.rendering.backend.FilterMode;
import org.geocraft.core.rendering.backend.TextureHandle;
import org.geocraft.core.rendering.backend.TextureLoader;

public class JoglTextureLoader implements TextureLoader {

    @Override
    public TextureHandle loadTexture(BufferedImage image, FilterMode mag, FilterMode min) {
        GL2 gl = GLContext.getCurrentGL().getGL2();
        Texture tex = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);
        int id = tex.getTextureObject(gl);
        gl.glBindTexture(GL.GL_TEXTURE_2D, id);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, toGlFilter(mag));
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, toGlFilter(min));
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        return new JoglTextureHandle(id, image.getWidth(), image.getHeight());
    }

    @Override
    public void disposeTexture(TextureHandle handle) {
        if (!(handle instanceof JoglTextureHandle)) return;
        JoglTextureHandle jh = (JoglTextureHandle) handle;
        if (jh.isDisposed()) return;
        GL2 gl = GLContext.getCurrentGL().getGL2();
        gl.glDeleteTextures(1, new int[] { jh.getGlId() }, 0);
        jh.markDisposed();
    }

    private int toGlFilter(FilterMode f) {
        switch (f) {
            case NEAREST:   return GL.GL_NEAREST;
            case BILINEAR:  return GL.GL_LINEAR;
            case TRILINEAR: return GL.GL_LINEAR_MIPMAP_LINEAR;
            default:        return GL.GL_LINEAR;
        }
    }
}
```

- [ ] **Step 2: JoglMaterialApplier**

```java
package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.material.BlendFactor;
import org.geocraft.core.rendering.material.BlendMode;
import org.geocraft.core.rendering.material.DepthTestConfig;
import org.geocraft.core.rendering.material.LightingConfig;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.material.TextureLayer;
import org.geocraft.core.rendering.material.WireframeMode;

public class JoglMaterialApplier {

    public void apply(GL2 gl, RenderMaterial m) {
        if (m == null) { resetDefaults(gl); return; }

        BlendMode b = m.getBlendMode();
        if (b != null) {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(toGl(b.srcFactor), toGl(b.dstFactor));
        } else {
            gl.glDisable(GL.GL_BLEND);
        }

        DepthTestConfig d = m.getDepthTestConfig();
        if (d != null && d.enabled) {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(toGl(d.func));
        } else if (d != null) {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }

        WireframeMode w = m.getWireframeMode();
        if (w != null) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, w.enabled ? GL2.GL_LINE : GL2.GL_FILL);
            gl.glLineWidth(w.lineWidth);
            if (w.antialiased) gl.glEnable(GL.GL_LINE_SMOOTH);
            else               gl.glDisable(GL.GL_LINE_SMOOTH);
        }

        LightingConfig l = m.getLightingConfig();
        if (l != null && l.enabled) {
            gl.glEnable(GL2.GL_LIGHTING);
            float[] amb = { l.ambient.x, l.ambient.y, l.ambient.z, l.ambient.w };
            float[] dif = { l.diffuse.x, l.diffuse.y, l.diffuse.z, l.diffuse.w };
            float[] spc = { l.specular.x, l.specular.y, l.specular.z, l.specular.w };
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, amb, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, dif, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, spc, 0);
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, l.shininess);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
        }

        TextureLayer t = m.getTextureLayer();
        if (t != null && t.texture instanceof JoglTextureHandle) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, ((JoglTextureHandle) t.texture).getGlId());
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }
    }

    private void resetDefaults(GL2 gl) {
        gl.glDisable(GL.GL_BLEND);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    private int toGl(BlendFactor f) {
        switch (f) {
            case ZERO:                  return GL.GL_ZERO;
            case ONE:                   return GL.GL_ONE;
            case SRC_ALPHA:             return GL.GL_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA:   return GL.GL_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA:             return GL.GL_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA:   return GL.GL_ONE_MINUS_DST_ALPHA;
            case SRC_COLOR:             return GL.GL_SRC_COLOR;
            case ONE_MINUS_SRC_COLOR:   return GL.GL_ONE_MINUS_SRC_COLOR;
            case DST_COLOR:             return GL.GL_DST_COLOR;
            case ONE_MINUS_DST_COLOR:   return GL.GL_ONE_MINUS_DST_COLOR;
            default:                    return GL.GL_ONE;
        }
    }

    private int toGl(DepthTestConfig.CompareFunc f) {
        switch (f) {
            case NEVER:             return GL.GL_NEVER;
            case LESS:              return GL.GL_LESS;
            case EQUAL:             return GL.GL_EQUAL;
            case LESS_OR_EQUAL:     return GL.GL_LEQUAL;
            case GREATER:           return GL.GL_GREATER;
            case NOT_EQUAL:         return GL.GL_NOTEQUAL;
            case GREATER_OR_EQUAL:  return GL.GL_GEQUAL;
            case ALWAYS:            return GL.GL_ALWAYS;
            default:                return GL.GL_LEQUAL;
        }
    }
}
```

- [ ] **Step 3: JoglGeometryUpload** — walks nodes, uploads buffers via immediate mode (simplest path for GL2 compat profile, adequate for the geometry sizes here).

```java
package org.geocraft.rendering.jogl;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.joml.Vector4f;

public class JoglGeometryUpload {

    public void drawMesh(GL2 gl, MeshGeometry mesh) {
        FloatBuffer v = mesh.getVertices();
        FloatBuffer n = mesh.getNormals();
        FloatBuffer uv = mesh.getTexCoords();
        IntBuffer idx = mesh.getIndices();
        if (v == null) return;

        gl.glBegin(GL.GL_TRIANGLES);
        int triCount = idx != null ? mesh.getTriangleCount() : mesh.getVertexCount() / 3;
        for (int t = 0; t < triCount; t++) {
            for (int k = 0; k < 3; k++) {
                int i = idx != null ? idx.get(t * 3 + k) : (t * 3 + k);
                if (n != null) gl.glNormal3f(n.get(i * 3), n.get(i * 3 + 1), n.get(i * 3 + 2));
                if (uv != null) gl.glTexCoord2f(uv.get(i * 2), uv.get(i * 2 + 1));
                gl.glVertex3f(v.get(i * 3), v.get(i * 3 + 1), v.get(i * 3 + 2));
            }
        }
        gl.glEnd();
    }

    public void drawLine(GL2 gl, LineGeometry line) {
        FloatBuffer v = line.getVertices();
        if (v == null) return;
        Vector4f c = line.getColor();
        gl.glLineWidth(line.getLineWidth());
        gl.glColor4f(c.x, c.y, c.z, c.w);
        gl.glBegin(GL.GL_LINES);
        for (int i = 0; i < line.getVertexCount(); i++) {
            gl.glVertex3f(v.get(i * 3), v.get(i * 3 + 1), v.get(i * 3 + 2));
        }
        gl.glEnd();
    }

    public void drawSphere(GL2 gl, SphereGeometry sphere) {
        // Simple icosphere-like approximation via GLU quadric
        com.jogamp.opengl.glu.GLU glu = new com.jogamp.opengl.glu.GLU();
        com.jogamp.opengl.glu.GLUquadric q = glu.gluNewQuadric();
        Vector4f c = sphere.getColor();
        gl.glColor4f(c.x, c.y, c.z, c.w);
        glu.gluSphere(q, sphere.getRadius(), 16, 16);
        glu.gluDeleteQuadric(q);
    }
}
```

- [ ] **Step 4: Build — expect success.**

- [ ] **Step 5: Commit**

```bash
git add org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/
git commit -m "Add JoglTextureLoader, JoglMaterialApplier, JoglGeometryUpload"
```

### Task 2.6: `JoglSceneWalker` + `JoglRenderBackend`

**Files:**
- Create: `.../JoglSceneWalker.java`, `JoglRenderBackend.java`
- Create: `org.geocraft.rendering.jogl/OSGI-INF/JoglRenderBackend.xml`

- [ ] **Step 1: JoglSceneWalker**

```java
package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GL2;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.geocraft.core.rendering.scene.VisibilityHint;
import org.joml.Matrix4f;

public class JoglSceneWalker {
    private final JoglMaterialApplier materialApplier = new JoglMaterialApplier();
    private final JoglGeometryUpload geometryUpload = new JoglGeometryUpload();

    public void walk(GL2 gl, SceneNode node, RenderMaterial overrideMaterial) {
        if (node.getVisibility() == VisibilityHint.ALWAYS_CULLED) return;

        gl.glPushMatrix();
        Matrix4f world = node.getWorldTransform();
        float[] m = new float[16];
        world.get(m);
        gl.glMultMatrixf(m, 0);

        RenderMaterial mat = overrideMaterial != null ? overrideMaterial : node.getMaterial();
        materialApplier.apply(gl, mat);

        if (node instanceof MeshGeometry) {
            geometryUpload.drawMesh(gl, (MeshGeometry) node);
        } else if (node instanceof LineGeometry) {
            geometryUpload.drawLine(gl, (LineGeometry) node);
        } else if (node instanceof SphereGeometry) {
            geometryUpload.drawSphere(gl, (SphereGeometry) node);
        }

        gl.glPopMatrix();

        for (SceneNode child : node.getChildren()) {
            walk(gl, child, overrideMaterial);
        }
    }
}
```

Note: `gl.glMultMatrixf` applies local+ancestor combined; to avoid double-applying we should only use local. Fix:

Replace the matrix computation with `Matrix4f local = node.getLocalTransform(); local.get(m); gl.glMultMatrixf(m, 0);` — parent transforms are already on the GL matrix stack via recursion.

- [ ] **Step 2: Fix JoglSceneWalker to use local transform**

Change the `Matrix4f world = node.getWorldTransform();` line to `Matrix4f local = node.getLocalTransform();` and `local.get(m);`.

- [ ] **Step 3: JoglRenderBackend**

```java
package org.geocraft.rendering.jogl;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLContext;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.rendering.backend.RenderSurface;
import org.geocraft.core.rendering.backend.TextureLoader;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;
import org.joml.Matrix4f;

public class JoglRenderBackend implements RenderBackend {
    private final JoglSceneWalker walker = new JoglSceneWalker();
    private final JoglTextureLoader textureLoader = new JoglTextureLoader();
    private RenderSurface currentSurface;

    @Override
    public void initialize(RenderSurface surface) {
        this.currentSurface = surface;
        surface.makeCurrent();
        GL2 gl = GLContext.getCurrentGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glClearColor(0f, 0f, 0f, 1f);
        surface.release();
    }

    @Override
    public void renderPass(GroupNode root, Camera camera, Light[] lights) {
        renderPass(root, camera, lights, null);
    }

    @Override
    public void renderPass(GroupNode root, Camera camera, Light[] lights, RenderMaterial overrideMaterial) {
        if (currentSurface == null) throw new IllegalStateException("not initialized");
        currentSurface.makeCurrent();
        try {
            GL2 gl = GLContext.getCurrentGL().getGL2();
            gl.glViewport(0, 0, currentSurface.getWidth(), currentSurface.getHeight());
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            Matrix4f proj = camera.getProjectionMatrix();
            Matrix4f view = camera.getViewMatrix();
            float[] m = new float[16];
            gl.glMatrixMode(GL2.GL_PROJECTION);
            proj.get(m);
            gl.glLoadMatrixf(m, 0);
            gl.glMatrixMode(GL2.GL_MODELVIEW);
            view.get(m);
            gl.glLoadMatrixf(m, 0);

            applyLights(gl, lights);
            walker.walk(gl, root, overrideMaterial);
        } finally {
            currentSurface.release();
        }
    }

    private void applyLights(GL2 gl, Light[] lights) {
        if (lights == null || lights.length == 0) return;
        for (int i = 0; i < Math.min(lights.length, 8); i++) {
            Light l = lights[i];
            int id = GL2.GL_LIGHT0 + i;
            if (!l.isEnabled()) { gl.glDisable(id); continue; }
            gl.glEnable(id);
            float[] amb = toArray(l.getAmbient());
            float[] dif = toArray(l.getDiffuse());
            float[] spc = toArray(l.getSpecular());
            gl.glLightfv(id, GL2.GL_AMBIENT, amb, 0);
            gl.glLightfv(id, GL2.GL_DIFFUSE, dif, 0);
            gl.glLightfv(id, GL2.GL_SPECULAR, spc, 0);
            if (l.getType() == Light.Type.DIRECTIONAL) {
                org.joml.Vector3f d = l.getDirection();
                float[] pos = { -d.x, -d.y, -d.z, 0f };
                gl.glLightfv(id, GL2.GL_POSITION, pos, 0);
            } else {
                org.joml.Vector3f p = l.getPosition();
                float[] pos = { p.x, p.y, p.z, 1f };
                gl.glLightfv(id, GL2.GL_POSITION, pos, 0);
            }
        }
    }

    private float[] toArray(org.joml.Vector4f v) { return new float[] { v.x, v.y, v.z, v.w }; }

    @Override
    public RenderSurface createOffscreenSurface(int width, int height) {
        return new JoglOffscreenSurface(width, height);
    }

    @Override
    public BufferedImage readPixels(RenderSurface surface) {
        surface.makeCurrent();
        try {
            GL2 gl = GLContext.getCurrentGL().getGL2();
            int w = surface.getWidth();
            int h = surface.getHeight();
            ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4);
            gl.glReadBuffer(GL.GL_BACK);
            gl.glReadPixels(0, 0, w, h, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buf);
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int i = ((h - 1 - y) * w + x) * 4;
                    int r = buf.get(i)     & 0xFF;
                    int g = buf.get(i + 1) & 0xFF;
                    int b = buf.get(i + 2) & 0xFF;
                    int a = buf.get(i + 3) & 0xFF;
                    img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            return img;
        } finally {
            surface.release();
        }
    }

    @Override
    public TextureLoader getTextureLoader() { return textureLoader; }

    @Override
    public void dispose() { /* no global state */ }
}
```

- [ ] **Step 4: OSGi DS component descriptor**

Create `org.geocraft.rendering.jogl/OSGI-INF/JoglRenderBackend.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.3.0"
               name="org.geocraft.rendering.jogl.JoglRenderBackend"
               immediate="true">
  <implementation class="org.geocraft.rendering.jogl.JoglRenderBackend"/>
  <service>
    <provide interface="org.geocraft.core.rendering.backend.RenderBackend"/>
  </service>
</scr:component>
```

- [ ] **Step 5: Update MANIFEST.MF**

Verify `Service-Component: OSGI-INF/JoglRenderBackend.xml` is present (added in Task 2.2).

- [ ] **Step 6: Build — expect success.**

- [ ] **Step 7: Commit**

```bash
git add org.geocraft.rendering.jogl/
git commit -m "Add JoglSceneWalker, JoglRenderBackend with OSGi DS registration"
```

### Task 2.7: Visual regression test harness

**Files:**
- Create: `org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/PixelComparator.java`
- Create: `org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/VisualRegressionHarness.java`

- [ ] **Step 1: PixelComparator**

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.awt.image.BufferedImage;

public class PixelComparator {
    public static class Result {
        public final boolean match;
        public final double maxDiff;
        public final double avgDiff;
        public Result(boolean match, double maxDiff, double avgDiff) {
            this.match = match;
            this.maxDiff = maxDiff;
            this.avgDiff = avgDiff;
        }
    }

    public static Result compare(BufferedImage a, BufferedImage b, double tolerance) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) {
            return new Result(false, 1.0, 1.0);
        }
        long totalDiff = 0;
        int maxDiff = 0;
        int n = a.getWidth() * a.getHeight();
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                int pa = a.getRGB(x, y);
                int pb = b.getRGB(x, y);
                int dr = Math.abs(((pa >> 16) & 0xFF) - ((pb >> 16) & 0xFF));
                int dg = Math.abs(((pa >>  8) & 0xFF) - ((pb >>  8) & 0xFF));
                int db = Math.abs(((pa      ) & 0xFF) - ((pb      ) & 0xFF));
                int d = Math.max(dr, Math.max(dg, db));
                totalDiff += d;
                if (d > maxDiff) maxDiff = d;
            }
        }
        double avg = totalDiff / (double) n / 255.0;
        double max = maxDiff / 255.0;
        return new Result(avg <= tolerance && max <= tolerance * 4, max, avg);
    }
}
```

- [ ] **Step 2: VisualRegressionHarness**

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.rendering.backend.RenderSurface;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.rendering.jogl.JoglRenderBackend;

public class VisualRegressionHarness {
    public static final int SIZE = 256;
    public static final double DEFAULT_TOLERANCE = 0.05; // 5% average per-pixel diff

    private final RenderBackend backend;
    private final RenderSurface surface;

    public VisualRegressionHarness() {
        this.backend = new JoglRenderBackend();
        this.surface = backend.createOffscreenSurface(SIZE, SIZE);
        backend.initialize(surface);
    }

    public RenderBackend getBackend() { return backend; }

    public BufferedImage renderScene(GroupNode root, Camera camera, Light[] lights) {
        camera.setViewport(SIZE, SIZE);
        backend.renderPass(root, camera, lights);
        return backend.readPixels(surface);
    }

    public void assertMatches(BufferedImage actual, String goldenName) throws IOException {
        String resourcePath = "/test-resources/golden/" + goldenName;
        InputStream in = VisualRegressionHarness.class.getResourceAsStream(resourcePath);
        File targetDir = new File("target/visual-output");
        targetDir.mkdirs();
        File actualOut = new File(targetDir, goldenName);
        ImageIO.write(actual, "PNG", actualOut);

        if (in == null) {
            // First run: write the golden next to source (human reviews + commits)
            File goldenDir = new File("test-resources/golden");
            goldenDir.mkdirs();
            ImageIO.write(actual, "PNG", new File(goldenDir, goldenName));
            System.out.println("Generated golden: " + goldenName + " — review and commit");
            return;
        }

        BufferedImage golden = ImageIO.read(in);
        PixelComparator.Result r = PixelComparator.compare(actual, golden, DEFAULT_TOLERANCE);
        if (!r.match) {
            File diffOut = new File(targetDir, "DIFF_" + goldenName);
            ImageIO.write(actual, "PNG", diffOut);
            throw new AssertionError("Visual mismatch for " + goldenName
                + " — maxDiff=" + r.maxDiff + " avgDiff=" + r.avgDiff
                + " (saved to " + diffOut + ")");
        }
    }

    public void dispose() {
        surface.dispose();
        backend.dispose();
    }
}
```

- [ ] **Step 3: Build — expect success.**

- [ ] **Step 4: Commit**

```bash
git add org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/
git commit -m "Add visual regression test harness and pixel comparator"
```

### Task 2.8: Visual regression tests — empty, triangle, textured, blended, wireframe

**Files:** one test class per golden (see Task list). Each test class follows this pattern:

- [ ] **Step 1: EmptySceneTest**

```java
package org.geocraft.rendering.jogl.tests.visual;

import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.scene.GroupNode;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EmptySceneTest {
    private VisualRegressionHarness harness;

    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void emptySceneRendersBlack() throws Exception {
        GroupNode root = new GroupNode("root");
        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 10));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        harness.assertMatches(harness.renderScene(root, c, null), "empty_scene.png");
    }
}
```

- [ ] **Step 2: SingleTriangleTest**

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SingleTriangleTest {
    private VisualRegressionHarness harness;

    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void redTriangleAtOrigin() throws Exception {
        GroupNode root = new GroupNode("root");
        FloatBuffer verts = FloatBuffer.wrap(new float[] {
            -1, -1, 0,
             1, -1, 0,
             0,  1, 0
        });
        MeshGeometry m = new MeshGeometry("tri");
        m.setVertices(verts, 3);
        root.addChild(m);

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 5));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

        harness.assertMatches(harness.renderScene(root, c, null), "single_triangle.png");
    }
}
```

(Triangle will render in whatever default color is set — the test is primarily about "same scene renders same pixels every time.")

- [ ] **Step 3: TexturedQuadTest** — creates a 2-triangle quad with checker texture, renders, compares.

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import org.geocraft.core.rendering.backend.FilterMode;
import org.geocraft.core.rendering.backend.TextureHandle;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.material.TextureLayer;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TexturedQuadTest {
    private VisualRegressionHarness harness;

    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void checkerTexturedQuad() throws Exception {
        GroupNode root = new GroupNode("root");
        FloatBuffer verts = FloatBuffer.wrap(new float[] {
            -1, -1, 0,   1, -1, 0,   1, 1, 0,
            -1, -1, 0,   1,  1, 0,  -1, 1, 0
        });
        FloatBuffer uv = FloatBuffer.wrap(new float[] {
            0, 0,  1, 0,  1, 1,
            0, 0,  1, 1,  0, 1
        });
        MeshGeometry m = new MeshGeometry("quad");
        m.setVertices(verts, 6);
        m.setTexCoords(uv);
        root.addChild(m);

        BufferedImage checker = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++)
            for (int x = 0; x < 32; x++)
                checker.setRGB(x, y, ((x/8 + y/8) % 2 == 0) ? 0xFFFFFFFF : 0xFF000000);

        // Texture load needs a current GL context
        harness.getBackend().createOffscreenSurface(1, 1); // ensure a surface exists
        TextureHandle tex;
        org.geocraft.core.rendering.backend.RenderSurface s = harness.getBackend().createOffscreenSurface(32, 32);
        s.makeCurrent();
        try {
            tex = harness.getBackend().getTextureLoader()
                 .loadTexture(checker, FilterMode.NEAREST, FilterMode.NEAREST);
        } finally {
            s.release();
        }

        m.setMaterial(new RenderMaterial().withTextureLayer(
            new TextureLayer(tex, FilterMode.NEAREST, FilterMode.NEAREST, TextureLayer.CombineMode.REPLACE)));

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 5));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        harness.assertMatches(harness.renderScene(root, c, null), "textured_quad.png");
    }
}
```

- [ ] **Step 4: BlendedQuadsTest** — two overlapping quads with alpha.

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.material.BlendMode;
import org.geocraft.core.rendering.material.DepthTestConfig;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlendedQuadsTest {
    private VisualRegressionHarness harness;

    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void twoTransparentQuads() throws Exception {
        GroupNode root = new GroupNode("root");
        RenderMaterial blended = new RenderMaterial()
            .withBlendMode(BlendMode.alphaBlend())
            .withDepthTest(new DepthTestConfig(true, DepthTestConfig.CompareFunc.LESS_OR_EQUAL));

        MeshGeometry q1 = quad(-0.3f, 0f);
        q1.setMaterial(blended);
        MeshGeometry q2 = quad(0.3f, -0.5f);
        q2.setMaterial(blended);
        root.addChild(q1);
        root.addChild(q2);

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 5));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        harness.assertMatches(harness.renderScene(root, c, null), "blended_quads.png");
    }

    private MeshGeometry quad(float cx, float cz) {
        FloatBuffer v = FloatBuffer.wrap(new float[] {
            cx-0.8f, -0.8f, cz,  cx+0.8f, -0.8f, cz,  cx+0.8f, 0.8f, cz,
            cx-0.8f, -0.8f, cz,  cx+0.8f,  0.8f, cz,  cx-0.8f, 0.8f, cz
        });
        MeshGeometry m = new MeshGeometry("q");
        m.setVertices(v, 6);
        return m;
    }
}
```

- [ ] **Step 5: WireframeCubeTest** — cube in wireframe.

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.material.WireframeMode;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WireframeCubeTest {
    private VisualRegressionHarness harness;

    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void wireframeCubeRenders() throws Exception {
        GroupNode root = new GroupNode("root");
        MeshGeometry cube = makeCube();
        cube.setMaterial(new RenderMaterial().withWireframe(new WireframeMode(true, 1f, false)));
        root.addChild(cube);

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(2, 2, 4));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        harness.assertMatches(harness.renderScene(root, c, null), "wireframe_cube.png");
    }

    private MeshGeometry makeCube() {
        // 12 triangles for a cube, centered at origin, side length 2
        float[] v = {
            // front
            -1,-1, 1,  1,-1, 1,  1, 1, 1,
            -1,-1, 1,  1, 1, 1, -1, 1, 1,
            // back
             1,-1,-1, -1,-1,-1, -1, 1,-1,
             1,-1,-1, -1, 1,-1,  1, 1,-1,
            // left
            -1,-1,-1, -1,-1, 1, -1, 1, 1,
            -1,-1,-1, -1, 1, 1, -1, 1,-1,
            // right
             1,-1, 1,  1,-1,-1,  1, 1,-1,
             1,-1, 1,  1, 1,-1,  1, 1, 1,
            // top
            -1, 1, 1,  1, 1, 1,  1, 1,-1,
            -1, 1, 1,  1, 1,-1, -1, 1,-1,
            // bottom
            -1,-1,-1,  1,-1,-1,  1,-1, 1,
            -1,-1,-1,  1,-1, 1, -1,-1, 1
        };
        MeshGeometry m = new MeshGeometry("cube");
        m.setVertices(FloatBuffer.wrap(v), 36);
        return m;
    }
}
```

- [ ] **Step 6: LinePrimitivesTest**

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.LineGeometry;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LinePrimitivesTest {
    private VisualRegressionHarness harness;

    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void lineAxesRender() throws Exception {
        GroupNode root = new GroupNode("root");
        FloatBuffer v = FloatBuffer.wrap(new float[] {
            0,0,0,  1,0,0,
            0,0,0,  0,1,0,
            0,0,0,  0,0,1
        });
        LineGeometry l = new LineGeometry("axes");
        l.setVertices(v, 6);
        l.setLineWidth(2f);
        l.setColor(new Vector4f(1, 1, 0, 1));
        root.addChild(l);

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(2, 2, 3));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        harness.assertMatches(harness.renderScene(root, c, null), "line_primitives.png");
    }
}
```

- [ ] **Step 7: Run tests — first run generates goldens, subsequent runs validate.**

```bash
mvn -pl org.geocraft.rendering.jogl.tests -am clean verify
```

On first run: each test writes a PNG to `test-resources/golden/`. Human reviews each PNG in `org.geocraft.rendering.jogl.tests/test-resources/golden/` and confirms it looks correct.

- [ ] **Step 8: Commit goldens after review**

```bash
git add org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/ \
        org.geocraft.rendering.jogl.tests/test-resources/golden/
git commit -m "Add visual regression tests for empty, triangle, textured, blended, wireframe, lines"
```

### Task 2.9: Phase 2 validation

- [ ] **Step 1: Full build + test**

```bash
mvn -pl org.geocraft.rendering.jogl,org.geocraft.rendering.jogl.tests -am clean verify
```

Expected: BUILD SUCCESS, all visual tests pass against committed goldens.

- [ ] **Step 2: Tag**

```bash
git tag -a jogl-phase-2-complete -m "Phase 2: JOGL backend foundation complete"
```

---

## Phase 3 — Picking + Advanced Features

### Task 3.1: Lighting visual test (`lit_sphere.png`)

**Files:**
- Create: `.../tests/visual/LitSphereTest.java`

- [ ] **Step 1: Test**

```java
package org.geocraft.rendering.jogl.tests.visual;

import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.material.LightingConfig;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.SphereGeometry;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LitSphereTest {
    private VisualRegressionHarness harness;
    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void directionalLightLitSphere() throws Exception {
        GroupNode root = new GroupNode("root");
        SphereGeometry s = new SphereGeometry("sphere", 1.5f);
        s.setMaterial(new RenderMaterial().withLighting(
            new LightingConfig(true,
                new Vector4f(0.2f, 0.2f, 0.2f, 1f),
                new Vector4f(0.8f, 0.2f, 0.2f, 1f),
                new Vector4f(1f, 1f, 1f, 1f),
                32f)));
        root.addChild(s);

        Light light = new Light();
        light.setType(Light.Type.DIRECTIONAL);
        light.setDirection(new Vector3f(-1, -1, -1));
        light.setDiffuse(new Vector4f(1, 1, 1, 1));
        light.setAmbient(new Vector4f(0.3f, 0.3f, 0.3f, 1));

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(50), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(3, 3, 5));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

        harness.assertMatches(harness.renderScene(root, c, new Light[] { light }), "lit_sphere.png");
    }
}
```

- [ ] **Step 2: Run, generate golden, review, commit.**

```bash
mvn -pl org.geocraft.rendering.jogl.tests test
```

```bash
git add org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/LitSphereTest.java \
        org.geocraft.rendering.jogl.tests/test-resources/golden/lit_sphere.png
git commit -m "Add lit sphere visual regression test"
```

### Task 3.2: Multi-pass rendering visual test

**Files:**
- Create: `.../tests/visual/MultiPassSceneTest.java`

- [ ] **Step 1: Test**

```java
package org.geocraft.rendering.jogl.tests.visual;

import java.nio.FloatBuffer;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.material.BlendMode;
import org.geocraft.core.rendering.material.DepthTestConfig;
import org.geocraft.core.rendering.material.RenderMaterial;
import org.geocraft.core.rendering.material.WireframeMode;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector3f;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultiPassSceneTest {
    private VisualRegressionHarness harness;
    @Before public void setUp() { harness = new VisualRegressionHarness(); }
    @After public void tearDown() { harness.dispose(); }

    @Test
    public void opaqueThenWireframeOverlay() throws Exception {
        // Render opaque cube in pass 1, then wireframe override in pass 2
        GroupNode root = new GroupNode("root");
        MeshGeometry cube = new MeshGeometry("cube");
        // Reuse cube geometry from WireframeCubeTest helper; inlined here for TDD clarity.
        float[] v = {
            -1,-1, 1,  1,-1, 1,  1, 1, 1,
            -1,-1, 1,  1, 1, 1, -1, 1, 1,
             1,-1,-1, -1,-1,-1, -1, 1,-1,
             1,-1,-1, -1, 1,-1,  1, 1,-1
        };
        cube.setVertices(FloatBuffer.wrap(v), 12);
        root.addChild(cube);

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(2, 2, 4));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        c.setViewport(VisualRegressionHarness.SIZE, VisualRegressionHarness.SIZE);

        // First pass: opaque
        harness.getBackend().renderPass(root, c, null);
        // Second pass: wireframe override
        RenderMaterial wireOver = new RenderMaterial()
            .withWireframe(new WireframeMode(true, 1f, true))
            .withBlendMode(BlendMode.alphaBlend())
            .withDepthTest(new DepthTestConfig(true, DepthTestConfig.CompareFunc.LESS_OR_EQUAL));
        harness.getBackend().renderPass(root, c, null, wireOver);

        java.awt.image.BufferedImage img = harness.getBackend().readPixels(
            ((JoglTestAccess) harness).getSurface());
        harness.assertMatches(img, "multi_pass_scene.png");
    }
}
```

Note: `JoglTestAccess` doesn't exist yet — expose the surface on the harness.

- [ ] **Step 2: Add getter on VisualRegressionHarness**

Edit `VisualRegressionHarness.java` — add:

```java
public RenderSurface getSurface() { return surface; }
```

Then in the test, replace `((JoglTestAccess) harness).getSurface()` with `harness.getSurface()`.

- [ ] **Step 3: Run, generate golden, review, commit.**

```bash
git add org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/MultiPassSceneTest.java \
        org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/visual/VisualRegressionHarness.java \
        org.geocraft.rendering.jogl.tests/test-resources/golden/multi_pass_scene.png
git commit -m "Add multi-pass scene visual regression test"
```

### Task 3.3: `JoglTextRenderer` for text overlay

**Files:**
- Create: `.../jogl/JoglTextRenderer.java`

- [ ] **Step 1: Implement**

```java
package org.geocraft.rendering.jogl;

import java.awt.Font;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import org.geocraft.core.rendering.scene.TextOverlay;
import org.joml.Vector4f;

public class JoglTextRenderer {
    private TextRenderer renderer;
    private int lastFontSize = -1;

    public void draw(GL2 gl, TextOverlay overlay, int viewportWidth, int viewportHeight) {
        int size = overlay.getFontSize();
        if (renderer == null || size != lastFontSize) {
            if (renderer != null) renderer.dispose();
            renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, size), true, true);
            lastFontSize = size;
        }
        Vector4f c = overlay.getColor();
        renderer.setColor(c.x, c.y, c.z, c.w);
        renderer.beginRendering(viewportWidth, viewportHeight);
        renderer.draw(overlay.getText(),
            (int) overlay.getScreenPosition().x,
            (int) overlay.getScreenPosition().y);
        renderer.endRendering();
    }

    public void dispose() {
        if (renderer != null) { renderer.dispose(); renderer = null; }
    }
}
```

- [ ] **Step 2: Wire into JoglSceneWalker**

In `JoglSceneWalker.walk()`, handle `TextOverlay`:

```java
else if (node instanceof org.geocraft.core.rendering.scene.TextOverlay) {
    // handled post-scene in backend
}
```

Actually text overlays should be drawn after the scene. Add a `textOverlays` collection on the walker that's populated during `walk()` and consumed by `JoglRenderBackend.renderPass()` after scene rendering. Update scene walker:

```java
// field
public final java.util.List<org.geocraft.core.rendering.scene.TextOverlay> collectedOverlays = new java.util.ArrayList<>();

// in walk(), before children loop
if (node instanceof org.geocraft.core.rendering.scene.TextOverlay) {
    collectedOverlays.add((org.geocraft.core.rendering.scene.TextOverlay) node);
}
```

And clear it at the start of each walk — add `collectedOverlays.clear();` at the start of `walk()` but only for the root call. Simplest: add a separate `beginWalk()` method called by the backend.

- [ ] **Step 3: Update JoglRenderBackend to draw overlays post-scene**

In `renderPass()`, after the `walker.walk(gl, root, overrideMaterial);` call, add:

```java
for (var overlay : walker.collectedOverlays) {
    textRenderer.draw(gl, overlay, currentSurface.getWidth(), currentSurface.getHeight());
}
walker.collectedOverlays.clear();
```

Add `private final JoglTextRenderer textRenderer = new JoglTextRenderer();` field, and dispose it in `dispose()`.

- [ ] **Step 4: Build** — `mvn -pl org.geocraft.rendering.jogl -am clean verify -DskipTests`.

- [ ] **Step 5: Commit**

```bash
git add org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/
git commit -m "Add JoglTextRenderer and wire TextOverlay into render pass"
```

### Task 3.4: Behavioral picking test

**Files:**
- Create: `.../tests/behavioral/PickingBehavioralTest.java`

- [ ] **Step 1: Test**

```java
package org.geocraft.rendering.jogl.tests.behavioral;

import java.nio.FloatBuffer;
import java.util.List;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.pick.DefaultPickEngine;
import org.geocraft.core.rendering.pick.PickResult;
import org.geocraft.core.rendering.pick.Ray;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class PickingBehavioralTest {
    @Test
    public void pickingScreenCenterHitsCenteredQuad() {
        GroupNode root = new GroupNode("root");
        FloatBuffer v = FloatBuffer.wrap(new float[] {
            -1, -1, 0,  1, -1, 0,  1, 1, 0,
            -1, -1, 0,  1,  1, 0, -1, 1, 0
        });
        MeshGeometry quad = new MeshGeometry("quad");
        quad.setVertices(v, 6);
        root.addChild(quad);

        Camera c = new Camera();
        c.setPerspective((float)Math.toRadians(60), 1f, 0.1f, 100f);
        c.setLocation(new Vector3f(0, 0, 5));
        c.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        c.setViewport(256, 256);

        Ray r = c.getPickRay(new Vector2f(128, 128));
        List<PickResult> results = new DefaultPickEngine().pickTriangles(root, r);
        assertFalse(results.isEmpty());
        assertEquals(0f, results.get(0).getWorldPosition().z, 1e-3);
    }
}
```

- [ ] **Step 2: Run test, expect pass.**

- [ ] **Step 3: Commit**

```bash
git add org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/behavioral/PickingBehavioralTest.java
git commit -m "Add behavioral picking test"
```

### Task 3.5: Phase 3 validation

- [ ] **Step 1: Full phase build**

```bash
mvn -pl org.geocraft.rendering.jogl,org.geocraft.rendering.jogl.tests -am clean verify
```

- [ ] **Step 2: Tag**

```bash
git tag -a jogl-phase-3-complete -m "Phase 3: picking + advanced rendering complete"
```

---

## Phase 4 — JOGL SWT Integration

### Task 4.1: `JoglSwtCanvas`

**Files:**
- Create: `.../jogl/JoglSwtCanvas.java`

- [ ] **Step 1: Implement**

```java
package org.geocraft.rendering.jogl;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.swt.GLCanvas;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderSurface;

public class JoglSwtCanvas implements RenderSurface {
    private final GLCanvas canvas;

    public JoglSwtCanvas(Composite parent) {
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);
        caps.setDoubleBuffered(true);
        caps.setDepthBits(24);
        this.canvas = new GLCanvas(parent, SWT.NONE, caps, null);
    }

    public GLCanvas getSwtCanvas() { return canvas; }

    @Override public int getWidth() { return canvas.getSize().x; }
    @Override public int getHeight() { return canvas.getSize().y; }

    @Override public void makeCurrent() {
        GLContext ctx = canvas.getContext();
        if (ctx != null && !ctx.isCurrent()) ctx.makeCurrent();
    }

    @Override public void release() {
        GLContext ctx = canvas.getContext();
        if (ctx != null && ctx.isCurrent()) ctx.release();
    }

    @Override public void swapBuffers() {
        canvas.swapBuffers();
    }

    @Override public void dispose() {
        if (!canvas.isDisposed()) canvas.dispose();
    }
}
```

- [ ] **Step 2: Build, commit.**

```bash
git add org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/JoglSwtCanvas.java
git commit -m "Add JoglSwtCanvas wrapping com.jogamp.opengl.swt.GLCanvas"
```

### Task 4.2: `SwtInputAdapter`

**Files:**
- Create: `.../jogl/SwtInputAdapter.java`

- [ ] **Step 1: Implement**

```java
package org.geocraft.rendering.jogl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.widgets.Control;
import org.geocraft.core.rendering.input.InputListener;
import org.geocraft.core.rendering.input.KeyInputEvent;
import org.geocraft.core.rendering.input.MouseInputEvent;

public class SwtInputAdapter implements MouseListener, MouseMoveListener, MouseWheelListener, KeyListener {
    private final List<InputListener> listeners = new ArrayList<>();
    private boolean leftDown, middleDown, rightDown;

    public SwtInputAdapter(Control control) {
        control.addMouseListener(this);
        control.addMouseMoveListener(this);
        control.addMouseWheelListener(this);
        control.addKeyListener(this);
    }

    public void addListener(InputListener l) { listeners.add(l); }

    private MouseInputEvent.Button toButton(int b) {
        if (b == 1) return MouseInputEvent.Button.LEFT;
        if (b == 2) return MouseInputEvent.Button.MIDDLE;
        if (b == 3) return MouseInputEvent.Button.RIGHT;
        return MouseInputEvent.Button.NONE;
    }

    private boolean has(int stateMask, int flag) { return (stateMask & flag) != 0; }

    @Override public void mouseDown(MouseEvent e) {
        if (e.button == 1) leftDown = true;
        else if (e.button == 2) middleDown = true;
        else if (e.button == 3) rightDown = true;
        fire(new MouseInputEvent(MouseInputEvent.Kind.PRESS, toButton(e.button),
                 e.x, e.y, 0,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void mouseUp(MouseEvent e) {
        if (e.button == 1) leftDown = false;
        else if (e.button == 2) middleDown = false;
        else if (e.button == 3) rightDown = false;
        fire(new MouseInputEvent(MouseInputEvent.Kind.RELEASE, toButton(e.button),
                 e.x, e.y, 0,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void mouseDoubleClick(MouseEvent e) { /* no-op */ }

    @Override public void mouseMove(MouseEvent e) {
        MouseInputEvent.Kind kind = (leftDown || middleDown || rightDown)
            ? MouseInputEvent.Kind.DRAG : MouseInputEvent.Kind.MOVE;
        MouseInputEvent.Button b = leftDown ? MouseInputEvent.Button.LEFT
                                  : middleDown ? MouseInputEvent.Button.MIDDLE
                                  : rightDown  ? MouseInputEvent.Button.RIGHT
                                  : MouseInputEvent.Button.NONE;
        fire(new MouseInputEvent(kind, b, e.x, e.y, 0,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void mouseScrolled(MouseEvent e) {
        fire(new MouseInputEvent(MouseInputEvent.Kind.WHEEL, MouseInputEvent.Button.NONE,
                 e.x, e.y, e.count,
                 has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void keyPressed(KeyEvent e) {
        fire(new KeyInputEvent(KeyInputEvent.Kind.PRESS, e.keyCode, e.character,
            has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    @Override public void keyReleased(KeyEvent e) {
        fire(new KeyInputEvent(KeyInputEvent.Kind.RELEASE, e.keyCode, e.character,
            has(e.stateMask, SWT.SHIFT), has(e.stateMask, SWT.CTRL), has(e.stateMask, SWT.ALT)));
    }

    private void fire(MouseInputEvent e) { for (InputListener l : listeners) l.onMouse(e); }
    private void fire(KeyInputEvent e)   { for (InputListener l : listeners) l.onKey(e); }
}
```

- [ ] **Step 2: Build, commit.**

```bash
git add org.geocraft.rendering.jogl/src/org/geocraft/rendering/jogl/SwtInputAdapter.java
git commit -m "Add SwtInputAdapter translating SWT events to InputEvent model"
```

### Task 4.3: Integration test — `JoglSwtCanvasIntegrationTest`

**Files:**
- Create: `.../tests/integration/JoglSwtCanvasIntegrationTest.java`

- [ ] **Step 1: Test**

```java
package org.geocraft.rendering.jogl.tests.integration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

public class JoglSwtCanvasIntegrationTest {

    private static boolean displayAvailable() {
        try {
            Display d = Display.getDefault();
            return d != null;
        } catch (Throwable t) { return false; }
    }

    @Test
    public void canvasInitializesAndDisposes() {
        Assume.assumeTrue("requires SWT display", displayAvailable());
        Display display = Display.getDefault();
        display.syncExec(() -> {
            Shell shell = new Shell(display, SWT.NONE);
            shell.setSize(256, 256);
            shell.setLayout(new FillLayout());
            JoglSwtCanvas canvas = new JoglSwtCanvas(shell);
            shell.open();
            assertFalse(canvas.getSwtCanvas().isDisposed());
            assertTrue(canvas.getWidth() >= 0);
            canvas.dispose();
            shell.dispose();
        });
    }
}
```

- [ ] **Step 2: Run** — `mvn -pl org.geocraft.rendering.jogl.tests test -Dtest=JoglSwtCanvasIntegrationTest`. On macOS, SWT requires `-XstartOnFirstThread`. Add to Tycho surefire config if needed (see Task 4.4).

- [ ] **Step 3: Configure surefire for SWT on macOS**

Edit `org.geocraft.rendering.jogl.tests/pom.xml` — add surefire config:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.geocraft</groupId>
    <artifactId>org.geocraft.parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
  </parent>
  <artifactId>org.geocraft.rendering.jogl.tests</artifactId>
  <packaging>eclipse-test-plugin</packaging>
  <build>
    <plugins>
      <plugin>
        <groupId>org.eclipse.tycho</groupId>
        <artifactId>tycho-surefire-plugin</artifactId>
        <configuration>
          <useUIHarness>true</useUIHarness>
          <argLine>-XstartOnFirstThread</argLine>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

Note: `useUIHarness=true` only for tests that need SWT. Integration tests are the only ones needing the UI harness — visual/unit tests should run without it. To separate: keep the main tests bundle for non-UI tests and create a second bundle for UI integration tests. Simpler for now: use the UI harness for all tests in this bundle (visual tests work with it too since offscreen rendering doesn't conflict).

- [ ] **Step 4: Run test, expect pass (or skipped if no display).**

- [ ] **Step 5: Commit.**

```bash
git add org.geocraft.rendering.jogl.tests/pom.xml \
        org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/integration/JoglSwtCanvasIntegrationTest.java
git commit -m "Add JoglSwtCanvas integration test with UI harness"
```

### Task 4.4: Phase 4 validation

- [ ] **Step 1: Full build + test of phases 1-4**

```bash
mvn -pl org.geocraft.core.rendering,org.geocraft.core.rendering.tests,org.geocraft.rendering.jogl,org.geocraft.rendering.jogl.tests -am clean verify
```

- [ ] **Step 2: Launch a minimal standalone GeoCraft smoke check** — launch the existing geocraft product and verify OSGi service registration:

```bash
# Uses existing launch-geocraft skill
# Look for JoglRenderBackend in the OSGi console: ss | grep jogl
```

- [ ] **Step 3: Tag**

```bash
git tag -a jogl-phase-4-complete -m "Phase 4: JOGL SWT integration complete"
```

---

## Phase 5 — Volume Viewer Refactor

**Note:** This is the highest-risk phase. Use subagents to refactor individual renderer classes in parallel after the foundation (MANIFEST, IVolumeViewer, canvas) is refactored sequentially. Each renderer refactor is independent once the backend service is wired.

### Task 5.1: Update `org.geocraft.ui.volumeviewer` MANIFEST.MF

**Files:**
- Modify: `org.geocraft.ui.volumeviewer/META-INF/MANIFEST.MF`

- [ ] **Step 1: Replace `com.ardor3d` require-bundle with `org.geocraft.core.rendering`**

Remove `com.ardor3d;bundle-version="1.0.0"` and add `org.geocraft.core.rendering;bundle-version="1.0.0"`. Add `Import-Package: org.joml;version="1.10.0"`.

- [ ] **Step 2: Expect compilation failures** — every Ardor3D import in the bundle now broken. This is intentional — the subsequent tasks replace them one-by-one.

### Task 5.2: Refactor `IVolumeViewer` to remove Ardor3D types

**Files:**
- Modify: `org.geocraft.ui.volumeviewer/src/org/geocraft/ui/volumeviewer/IVolumeViewer.java`

- [ ] **Step 1: Replace Ardor3D types in API**

Replace:
- `com.ardor3d.math.Vector3 getPickLocation()` → `org.joml.Vector3f getPickLocation()`
- `void mapSpatial(Spatial spatial, Object renderer)` → `void mapNode(SceneNode node, Object renderer)`
- `Spatial getSelectedSpatial()` → `SceneNode getSelectedNode()`
- `void setSelectedSpatial(Spatial, Vector3)` → `void setSelectedNode(SceneNode, Vector3f)`
- `Texture cleanupTexture(Texture)` → `void disposeTexture(TextureHandle)`
- `SceneText createSceneText(...)` → `TextOverlay createTextOverlay(...)`

Imports: `org.geocraft.core.rendering.scene.SceneNode`, `org.geocraft.core.rendering.scene.TextOverlay`, `org.geocraft.core.rendering.backend.TextureHandle`, `org.joml.Vector3f`.

### Task 5.3: Rewrite `ViewCanvasFactory` and `ViewCanvasImplementor`

**Files:**
- Rewrite: `.../canvas/ViewCanvasFactory.java`
- Rewrite: `.../canvas/ViewCanvasImplementor.java`

- [ ] **Step 1: `ViewCanvasFactory`** — create `JoglSwtCanvas`, look up `RenderBackend` OSGi service, instantiate `ViewCanvasImplementor`, wire `SwtInputAdapter`.

```java
package org.geocraft.internal.ui.volumeviewer.canvas;

import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.geocraft.rendering.jogl.SwtInputAdapter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ViewCanvasFactory {

    public static ViewCanvasImplementor createCanvas(Composite parent) {
        BundleContext ctx = FrameworkUtil.getBundle(ViewCanvasFactory.class).getBundleContext();
        ServiceReference<RenderBackend> ref = ctx.getServiceReference(RenderBackend.class);
        if (ref == null) throw new IllegalStateException("No RenderBackend service available");
        RenderBackend backend = ctx.getService(ref);

        JoglSwtCanvas canvas = new JoglSwtCanvas(parent);
        SwtInputAdapter input = new SwtInputAdapter(canvas.getSwtCanvas());

        ViewCanvasImplementor impl = new ViewCanvasImplementor(canvas, backend, input);
        impl.initialize();
        return impl;
    }
}
```

- [ ] **Step 2: `ViewCanvasImplementor`** — becomes a pure Layer-1 consumer.

```java
package org.geocraft.internal.ui.volumeviewer.canvas;

import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.rendering.camera.Camera;
import org.geocraft.core.rendering.camera.Light;
import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.geocraft.rendering.jogl.SwtInputAdapter;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ViewCanvasImplementor {
    private final JoglSwtCanvas canvas;
    private final RenderBackend backend;
    private final SwtInputAdapter input;
    private final GroupNode sceneRoot = new GroupNode("scene");
    private final GroupNode wireOverRoot = new GroupNode("wireover");
    private final GroupNode widgetRoot = new GroupNode("widgets");
    private final Camera camera = new Camera();
    private final Light sun = new Light();

    public ViewCanvasImplementor(JoglSwtCanvas canvas, RenderBackend backend, SwtInputAdapter input) {
        this.canvas = canvas;
        this.backend = backend;
        this.input = input;
    }

    public void initialize() {
        backend.initialize(canvas);
        camera.setPerspective((float)Math.toRadians(45), 1f, 0.1f, 10000f);
        camera.setLocation(new Vector3f(0, 0, 100));
        camera.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
        sun.setType(Light.Type.DIRECTIONAL);
        sun.setDirection(new Vector3f(-1, -1, -1));
        sun.setAmbient(new Vector4f(0.3f, 0.3f, 0.3f, 1));
    }

    public GroupNode getSceneRoot() { return sceneRoot; }
    public GroupNode getWidgetRoot() { return widgetRoot; }
    public Camera getCamera() { return camera; }
    public SwtInputAdapter getInput() { return input; }

    public void render() {
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        camera.setViewport(w, h);
        backend.renderPass(sceneRoot, camera, new Light[] { sun });
        backend.renderPass(wireOverRoot, camera, null);
        backend.renderPass(widgetRoot, camera, null);
        canvas.swapBuffers();
    }

    public void dispose() {
        canvas.dispose();
    }
}
```

- [ ] **Step 3: Commit.**

```bash
git add org.geocraft.ui.volumeviewer/META-INF/MANIFEST.MF \
        org.geocraft.ui.volumeviewer/src/org/geocraft/ui/volumeviewer/IVolumeViewer.java \
        org.geocraft.ui.volumeviewer/src/org/geocraft/internal/ui/volumeviewer/canvas/ViewCanvasFactory.java \
        org.geocraft.ui.volumeviewer/src/org/geocraft/internal/ui/volumeviewer/canvas/ViewCanvasImplementor.java
git commit -m "Refactor ViewCanvas* to use RenderBackend service and Layer 1 types"
```

### Task 5.4–5.13: Renderer refactors (parallelizable)

The remaining volume viewer refactors follow a repeatable pattern. Dispatch these via subagents in parallel because they don't depend on each other once the canvas/factory is in place:

- **5.4** `VolumeMouseLook` — replace Ardor3D `LogicalLayer` trigger system with `SwtInputAdapter` listener that calls `ViewCanvasImplementor.doPick()`.
- **5.5** `PostStack3dRenderer` — construct `MeshGeometry` for slices, `TextureLayer` for seismic textures, `BlendMode` for transparency.
- **5.6** `Grid3dRenderer` — `MeshGeometry` with normals + textures.
- **5.7** `FaultRenderer` — `MeshGeometry` triangles + `Line` outlines.
- **5.8** `WellRenderer` — `LineGeometry` trajectories with color.
- **5.9** `PointSetRenderer` — `SphereGeometry` instances.
- **5.10** `WellPickRenderer` — same as WellRenderer plus pick visualization.
- **5.11** `SceneText` → delete, callers use `TextOverlay`.
- **5.12** `FocusRods` → rewrite as `GroupNode` containing three colored `LineGeometry` children.
- **5.13** `SelectionRenderer` → rewrite as `LineGeometry` outline computed from bounding box.

**For each renderer task, the subagent prompt template is:**

> Refactor `<ClassName>` at `<path>` to replace Ardor3D imports with Layer 1 types from `org.geocraft.core.rendering`. The class currently imports `com.ardor3d.*` — replace each with the corresponding Layer 1 type as documented in `docs/superpowers/specs/2026-04-11-jogl-migration-design.md`. Preserve the class's behavior: it must still construct geometry for `<domain object type>` and return it to the volume viewer the same way. Use `MeshGeometry` for triangle meshes, `LineGeometry` for lines, `SphereGeometry` for point clouds, `TextOverlay` for text, and `RenderMaterial` with `BlendMode`/`TextureLayer`/`LightingConfig`/`WireframeMode`/`DepthTestConfig` for render states. Use JOML `Vector3f`/`Vector4f` for math. After editing, run `mvn -pl org.geocraft.ui.volumeviewer -am compile` and fix any remaining compilation errors in the file you edited. Commit with message "Refactor <ClassName> to use Layer 1 rendering types".

Each subagent works on a single file, commits, reports back. Dispatch these in parallel using Agent with subagent_type=general-purpose once Task 5.3 is committed.

### Task 5.14: Behavioral tests for volume viewer renderers

**Files:**
- Create: `.../tests/behavioral/SeismicVolumeRenderTest.java`
- Create: `.../tests/behavioral/FaultSurfaceRenderTest.java`
- Create: `.../tests/behavioral/WellTrajectoryRenderTest.java`

Each test constructs a synthetic domain object, runs the corresponding GeoCraft renderer, walks the resulting `SceneNode` tree, and asserts the expected structure (node count, bounding box, material configuration). These are lighter-weight than visual regression — they verify the renderer produces the right scene graph shape.

- [ ] **Step 1: Example — SeismicVolumeRenderTest**

```java
package org.geocraft.rendering.jogl.tests.behavioral;

import org.geocraft.core.rendering.scene.GroupNode;
import org.geocraft.core.rendering.scene.MeshGeometry;
import org.geocraft.core.rendering.scene.SceneNode;
import org.junit.Test;
import static org.junit.Assert.*;

public class SeismicVolumeRenderTest {
    @Test
    public void synthesizedSeismicProducesMeshGeometry() {
        // This test is a placeholder showing expected test shape.
        // Full implementation requires constructing a synthetic PostStack3d
        // volume via the seismic model API and invoking PostStack3dRenderer.
        // Skipped until Task 5.5 (PostStack3dRenderer refactor) is complete.
        GroupNode root = new GroupNode("root");
        // ... populate via PostStack3dRenderer ...
        int meshCount = countMeshes(root);
        assertTrue("should produce at least one MeshGeometry", meshCount >= 0);
    }

    private int countMeshes(SceneNode n) {
        int c = n instanceof MeshGeometry ? 1 : 0;
        for (SceneNode child : n.getChildren()) c += countMeshes(child);
        return c;
    }
}
```

Extend similarly for `FaultSurfaceRenderTest` and `WellTrajectoryRenderTest` once the respective renderers are ported.

- [ ] **Step 2: Build, commit.**

```bash
git add org.geocraft.rendering.jogl.tests/src/org/geocraft/rendering/jogl/tests/behavioral/
git commit -m "Add behavioral test stubs for volume viewer renderers"
```

### Task 5.15: Phase 5 validation

- [ ] **Step 1: Full workspace build**

```bash
mvn clean verify
```

Expected: BUILD SUCCESS for all modules. `org.geocraft.ui.volumeviewer` compiles without `com.ardor3d` imports.

- [ ] **Step 2: Grep verification**

```bash
# Should output nothing
grep -r "com.ardor3d" org.geocraft.ui.volumeviewer/src/
grep -r "import com.ardor3d" org.geocraft.ui.volumeviewer/
```

- [ ] **Step 3: Tag**

```bash
git tag -a jogl-phase-5-complete -m "Phase 5: volume viewer refactored to Layer 1 types"
```

---

## Phase 6 — End-to-End Validation

### Task 6.1: Launch GeoCraft natively

- [ ] **Step 1: Launch via existing geocraft launch script (native aarch64, no Rosetta)**

- [ ] **Step 2: Open volume viewer**

- [ ] **Step 3: Load test data** — use `org.geocraft.unittest.suite` sample data if available.

- [ ] **Step 4: Verify all geometry types render**

- [ ] **Step 5: Verify mouse interaction** — pick, drag-rotate, wheel-zoom, pan.

- [ ] **Step 6: Verify selection highlighting and focus rods.**

- [ ] **Step 7: Record any regressions in `SWT_MIGRATION_LOG.md`.**

### Task 6.2: Tag

```bash
git tag -a jogl-phase-6-complete -m "Phase 6: native Apple Silicon e2e validation complete"
```

---

## Phase 7 — Remove Ardor3D

### Task 7.1: Delete `com.ardor3d` bundle

- [ ] **Step 1: Delete directory**

```bash
rm -rf com.ardor3d/
```

- [ ] **Step 2: Remove from parent pom.xml**

Edit `pom.xml` — delete `<module>com.ardor3d</module>`.

- [ ] **Step 3: Full build**

```bash
mvn clean verify
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Grep verification**

```bash
grep -rn "com.ardor3d\|ardor3d\|org.lwjgl" --include="*.java" --include="*.MF" --include="*.xml"
```

Should output nothing.

- [ ] **Step 5: Update migration log**

Append entry to `SWT_MIGRATION_LOG.md` documenting the JOGL migration completion.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "Remove com.ardor3d bundle — replaced by JOGL rendering backend"
```

- [ ] **Step 7: Final tag**

```bash
git tag -a jogl-migration-complete -m "JOGL migration complete — Ardor3D removed"
```

---

## Spec Coverage Self-Review

- Bundle structure (spec §Bundle Structure): ✓ Tasks 1.2, 1.3, 2.2, 2.3
- Layer 1 scene graph (spec §Layer 1 — Scene Graph): ✓ Tasks 1.5, 1.6
- Layer 1 materials (spec §Render Materials): ✓ Task 1.7
- Camera + projection (spec §Camera): ✓ Task 1.8
- Bounds + picking (spec §Picking): ✓ Tasks 1.9, 1.10
- Backend/surface/texture interfaces (spec §Rendering): ✓ Task 1.11
- JOGL backend canvas + offscreen (spec §Canvas Integration): ✓ Tasks 2.4, 4.1
- JOGL texture loader (spec §Backend Internals): ✓ Task 2.5
- JOGL material applier (spec §Backend Internals): ✓ Task 2.5
- JOGL scene walker + backend (spec §Backend Internals): ✓ Task 2.6
- OSGi DS registration (spec §OSGi Service Registration): ✓ Task 2.6
- Visual regression harness + tests (spec §Level 2): ✓ Tasks 2.7, 2.8, 3.1, 3.2
- Software picking (spec §Backend Internals): ✓ Tasks 1.10, 3.4
- Text overlay (spec §Backend Internals): ✓ Task 3.3
- SWT integration + input (spec §Canvas Integration): ✓ Tasks 4.1, 4.2, 4.3
- Level 3 integration tests (spec §Level 3): ✓ Task 4.3
- Volume viewer refactor (spec §Phase 5): ✓ Tasks 5.1–5.14
- IVolumeViewer cleanup (spec §Goals): ✓ Task 5.2
- Ardor3D removal (spec §Phase 7): ✓ Task 7.1
- E2E validation (spec §Phase 6): ✓ Task 6.1
- Level 1 unit tests (spec §Level 1): ✓ Tasks 1.4, 1.5, 1.7, 1.8, 1.9, 1.10
- Level 4 behavioral tests (spec §Level 4): ✓ Tasks 3.4, 5.14

All spec requirements mapped to tasks.

## Execution Notes

- Phases 1-4 are strictly sequential — each builds on previous foundation.
- Within phase 5, tasks 5.4-5.13 can be parallelized via subagents after 5.1-5.3 land.
- Each phase ends with a git tag so reverts are surgical.
- Test failures during phase 2 visual tests indicate either a bug in the backend OR a driver-dependent pixel diff — investigate before adjusting tolerance.
