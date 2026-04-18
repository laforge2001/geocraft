/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.geocraft.rendering.jogl.SwtInputAdapter;
import org.geocraft.ui.volumeviewer.IVolumeViewer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


public class ViewCanvasFactory {

  public static ViewCanvasImplementor makeCanvas(final Composite canvasComposite,
      final IVolumeViewer viewer, final int depthBits) {

    final RenderBackend backend = lookupRenderBackend();

    final JoglSwtCanvas canvas = new JoglSwtCanvas(canvasComposite);

    if (backend != null) {
      backend.initialize(canvas);
    }

    final SwtInputAdapter inputAdapter = new SwtInputAdapter(canvas.getSwtControl());
    final ViewCanvasImplementor impl = new ViewCanvasImplementor(backend, canvas, inputAdapter, viewer);
    return impl;
  }

  private static RenderBackend lookupRenderBackend() {
    try {
      final BundleContext ctx = FrameworkUtil.getBundle(ViewCanvasFactory.class).getBundleContext();
      if (ctx == null) return null;
      final ServiceReference<RenderBackend> ref = ctx.getServiceReference(RenderBackend.class);
      if (ref == null) return null;
      return ctx.getService(ref);
    } catch (final Exception e) {
      return null;
    }
  }
}
