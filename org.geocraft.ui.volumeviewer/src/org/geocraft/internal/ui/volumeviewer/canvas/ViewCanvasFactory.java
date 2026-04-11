/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.geocraft.core.rendering.backend.RenderBackend;
import org.geocraft.rendering.jogl.JoglSwtCanvas;
import org.geocraft.rendering.jogl.SwtInputAdapter;
import org.geocraft.ui.volumeviewer.IVolumeViewer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


/**
 * Factory for creating volume-view canvas implementors backed by the
 * Layer 1 rendering API and JoglSwtCanvas.
 *
 * TODO: wire up a render loop that invokes ViewCanvasImplementor.render()
 * via SWT Display.timerExec.
 */
public class ViewCanvasFactory {

  /**
   * Create a ViewCanvasImplementor for the volume viewer.
   *
   * @param canvasComposite parent SWT composite.
   * @param viewer the IVolumeViewer callback.
   * @param depthBits the desired depth buffer bits (currently informational).
   * @return a new ViewCanvasImplementor.
   */
  public static ViewCanvasImplementor makeCanvas(final Composite canvasComposite,
      final IVolumeViewer viewer, final int depthBits) {

    final RenderBackend backend = lookupRenderBackend();
    final JoglSwtCanvas canvas = new JoglSwtCanvas(canvasComposite);
    if (backend != null) {
      backend.initialize(canvas);
    }
    final Control swtControl = (Control) (Object) canvas.getSwtCanvas();
    final SwtInputAdapter inputAdapter = new SwtInputAdapter(swtControl);

    return new ViewCanvasImplementor(backend, canvas, inputAdapter, viewer);
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
