/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.ui.volumeviewer.canvas;


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.opengl.GLData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.geocraft.ui.volumeviewer.IVolumeViewer;

import com.ardor3d.framework.FrameWork;
import com.ardor3d.framework.swt.SwtCanvas;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.SwtFocusWrapper;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.swt.SwtKeyboardWrapper;
import com.ardor3d.input.swt.SwtMouseWrapper;
import com.ardor3d.util.Timer;


/**
 * Simple generator class for jME canvases.
 * @author Joshua Slack
 */
public class ViewCanvasFactory {

  /**
   * @param canvasComposite
   * @param view 
   * @return
   */
  public static ViewCanvasImplementor makeCanvas(final Composite canvasComposite, final IVolumeViewer viewer,
      final int depthBits) {

    final FrameWork frameWork = new FrameWork(new Timer());

    final GLData data = new GLData();
    data.depthSize = depthBits;
    data.doubleBuffer = true;

    final SwtMouseWrapper mouseWrapper = new SwtMouseWrapper();
    final SwtKeyboardWrapper keyboardWrapper = new SwtKeyboardWrapper();
    final SwtFocusWrapper focusWrapper = new SwtFocusWrapper();

    final PhysicalLayer<SwtCanvas> physicalLayer = new PhysicalLayer<SwtCanvas>(keyboardWrapper, mouseWrapper,
        focusWrapper);

    LogicalLayer logicLayer = new LogicalLayer();

    final SwtCanvas swtCanvas = new SwtCanvas(canvasComposite, SWT.NONE, data);
    final ViewCanvasImplementor impl = new ViewCanvasImplementor(swtCanvas, viewer, logicLayer);
    swtCanvas.setCanvasRenderer(impl);

    logicLayer.registerInput(swtCanvas, physicalLayer);
    physicalLayer.listenTo(swtCanvas);

    frameWork.registerCanvas(swtCanvas);
    frameWork.registerUpdater(impl);

    // Add resize listener
    swtCanvas.addControlListener(new ControlAdapter() {

      @Override
      public void controlResized(@SuppressWarnings("unused") final ControlEvent e) {
        impl.resizeCanvas(swtCanvas.getSize().x, swtCanvas.getSize().y);
      }
    });

    frameWork.init();

    Display.getCurrent().asyncExec(new Runnable() {

      private long lastRender;

      private final double syncNS = 1000000000.0 / 60;

      @Override
      public void run() {
        frameWork.updateFrame();

        // sync to 60 fps max.
        if (!swtCanvas.isDisposed()) {
          long sinceLast = System.nanoTime() - lastRender;
          if (sinceLast < syncNS) {
            try {
              Thread.sleep(Math.round((syncNS - sinceLast) / 1000000L));
            } catch (InterruptedException e) {
              // ignore
            }
          }
          lastRender = System.nanoTime();
          Display.getCurrent().asyncExec(this);
        }
      }
    });
    return impl;
  }
}
