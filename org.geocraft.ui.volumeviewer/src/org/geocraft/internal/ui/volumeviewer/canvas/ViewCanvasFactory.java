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

    System.out.println("[ViewCanvasFactory] Looking up RenderBackend...");
    final RenderBackend backend = lookupRenderBackend();
    System.out.println("[ViewCanvasFactory] Backend: " + (backend != null ? backend.getClass().getName() : "null"));

    System.out.println("[ViewCanvasFactory] Creating JoglSwtCanvas...");
    final JoglSwtCanvas canvas = new JoglSwtCanvas(canvasComposite);

    if (backend != null) {
      backend.initialize(canvas);
    }

    final SwtInputAdapter inputAdapter = new SwtInputAdapter(canvas.getSwtControl());
    System.out.println("[ViewCanvasFactory] Creating ViewCanvasImplementor...");
    final ViewCanvasImplementor impl = new ViewCanvasImplementor(backend, canvas, inputAdapter, viewer);
    System.out.println("[ViewCanvasFactory] Done.");
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
