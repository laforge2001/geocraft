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
