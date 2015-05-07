package org.geocraft.ui.viewer;


import org.geocraft.core.repository.specification.AbstractSpecification;
import org.geocraft.ui.viewer.layer.IViewLayer;


public class RendererSpecification extends AbstractSpecification {

  private final IRenderer _renderer;

  public RendererSpecification(final IRenderer renderer) {
    _renderer = renderer;
  }

  public boolean isSatisfiedBy(final Object obj) {
    if (obj instanceof IViewLayer) {
      IViewLayer layer = (IViewLayer) obj;
      if (layer.getName().equals(_renderer.getName())) {
        return true;
      }
    }
    return false;
  }

}
