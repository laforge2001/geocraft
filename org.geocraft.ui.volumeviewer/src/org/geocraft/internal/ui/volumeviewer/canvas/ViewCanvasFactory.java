/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.geocraft.rendering.jogl.SwtInputAdapter;
import org.geocraft.ui.volumeviewer.IVolumeViewer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


public class ViewCanvasFactory {

  private static final ILogger LOGGER = ServiceProvider.getLoggingService().getLogger(ViewCanvasFactory.class);

  public static ViewCanvasImplementor makeCanvas(final Composite canvasComposite,
      final IVolumeViewer viewer, final int depthBits) {

    // Construct the JoglSwtCanvas *before* looking up the RenderBackend
    // service. Referencing JoglSwtCanvas is the first class load out of
    // the org.geocraft.rendering.jogl bundle, which triggers its lazy
    // activation (Bundle-ActivationPolicy: lazy). Lazy activation is what
    // causes Declarative Services to publish the JoglRenderBackend service.
    // If we looked up the service first, the bundle would still be in
    // RESOLVED state, the DS component would not yet be active, and the
    // lookup would return null — the exact failure observed in issue #35.
    final JoglSwtCanvas canvas = new JoglSwtCanvas(canvasComposite);

    final RenderBackend backend = lookupRenderBackend();
    if (backend == null) {
      LOGGER.warn("No RenderBackend OSGi service registered — 3D viewer canvas will render black. "
          + "Check that org.geocraft.rendering.jogl bundle is active and its DS component started.");
    }

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
      if (ctx == null) {
        LOGGER.warn("ViewCanvasFactory has no BundleContext — RenderBackend lookup skipped.");
        return null;
      }
      final ServiceReference<RenderBackend> ref = ctx.getServiceReference(RenderBackend.class);
      if (ref == null) {
        LOGGER.warn("RenderBackend service reference not found in OSGi registry.");
        return null;
      }
      return ctx.getService(ref);
    } catch (final Exception e) {
      LOGGER.error("Failed to look up RenderBackend OSGi service: " + e.getMessage(), e);
      return null;
    }
  }
}
