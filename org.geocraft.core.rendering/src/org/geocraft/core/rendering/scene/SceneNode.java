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
