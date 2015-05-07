package org.geocraft.ui.traceviewer;


import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.ui.viewer.IRenderer;


public class RendererRegistry {

  public static IRenderer createRenderer(final IConfigurationElement configElement) {
    // TODO Auto-generated method stub
    return null;
  }

  public static List<IConfigurationElement> findRenderer(final Shell shell, final Object object) {
    // TODO Auto-generated method stub
    return null;
  }

  public static TraceViewRenderer findRenderer(final String klass) {
    // TODO Auto-generated method stub
    return null;
  }

}
