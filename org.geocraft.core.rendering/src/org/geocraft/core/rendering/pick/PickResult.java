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
