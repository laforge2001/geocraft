package org.geocraft.core.rendering.pick;

import java.util.List;
import org.geocraft.core.rendering.scene.GroupNode;

public interface PickEngine {
    List<PickResult> pickTriangles(GroupNode root, Ray ray);
    List<PickResult> pickBounds(GroupNode root, Ray ray);
}
