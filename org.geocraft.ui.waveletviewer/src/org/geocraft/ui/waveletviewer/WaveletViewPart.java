/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.waveletviewer;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.geocraft.core.color.ColorUtil;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.multiplot.MultiPlotPart;
import org.geocraft.ui.viewer.IViewer;


/**
 * The wavelet viewer part extends the multi-plot part, as it consists of 3 different plots.
 * The 1st plot as the basic view (amplitude vs time) that shows the shape of the wavelet.
 * The 2nd plot is an amplitude spectrum view.
 * The 3rd plot is a phase spectrum view.
 * The viewer part is different from the base multi-plot part, in that drag-and-drop events
 * need to add the selected objects to all 3 of the plots, rather than just the individual
 * plot in which the drop occurred.
 */
public class WaveletViewPart extends MultiPlotPart {

  public static Map<Object, RGB> _rgbMap = Collections.synchronizedMap(new HashMap<Object, RGB>());

  private static IPreferenceStore _preferenceStore = PropertyStoreFactory.getStore(PreferencePage.ID);

  public static synchronized RGB getRGB(final Object object) {
    if (_rgbMap.containsKey(object)) {
      return _rgbMap.get(object);
    }
    RGB rgb = ColorUtil.getCommonRGB();
    _rgbMap.put(object, rgb);
    return rgb;
  }

  @Override
  public void createPartControl(final Composite parent) {
    super.createPartControl(parent);
    boolean displayPhasePlot = _preferenceStore.getBoolean(PreferencePage.DISPLAY_PHASE_PLOT);

    // Display only 2 columns if user does not want the phase plot
    int numColumns = 3;
    if (!displayPhasePlot) {
      numColumns = 2;
    }
    // Create the multi-plot part to contain the chart viewers in a gridded layout.
    setNumColumns(numColumns);

    // Create the basic wavelet viewer as the 1st plot.
    WaveletViewer waveletViewer = new WaveletViewer(getViewerParent(), "Wavelet");
    waveletViewer.setLayerTreeVisible(false);
    addViewer(waveletViewer);

    // Create the amplitude spectrum viewer as the 2nd plot.
    String plotName = "Amplitude Spectrum";
    String spectrumType = _preferenceStore.getString(PreferencePage.SPECTRUM_TYPE);

    // Change amplitude plot name based on the spectrum type  
    if (spectrumType.equals(PreferencePage.AMPLITUDE_SPECTRUM)) {
      plotName = "Amplitude Spectrum";
    } else if (spectrumType.equals(PreferencePage.LINEAR_POWER_SPECTRUM)) {
      plotName = "Linear Power Spectrum";
    } else if (spectrumType.equals(PreferencePage.DECIBEL_POWER_SPECTRUM)) {
      plotName = "Decibel Power Spectrum (dB)";
    }

    AmplitudeSpectrumViewer amplitudeSpectrumViewer = new AmplitudeSpectrumViewer(getViewerParent(), plotName);
    amplitudeSpectrumViewer.setLayerTreeVisible(false);
    addViewer(amplitudeSpectrumViewer);

    // Create the phase spectrum viewer as the 3rd plot (if user wants to see the plot)
    if (displayPhasePlot) {
      PhaseSpectrumViewer phaseSpectrumViewer = new PhaseSpectrumViewer(getViewerParent(), "Phase Spectrum");
      phaseSpectrumViewer.setLayerTreeVisible(false);
      addViewer(phaseSpectrumViewer);
    }

    //register wavelet view part which contains all 3 plots
    ServiceProvider.getViewersService().add(this);
  }

  @Override
  protected void initDragAndDrop(final IViewer viewer) {
    DropTarget target = new DropTarget(viewer.getComposite(), DND.DROP_COPY | DND.DROP_MOVE);
    target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
    target.addDropListener(new DropTargetAdapter() {

      @Override
      public void dragOver(DropTargetEvent event) {
        event.detail = DND.DROP_COPY;
      }

      @Override
      public void drop(final DropTargetEvent event) {
        if (TextTransfer.getInstance().isSupportedType(event.currentDataType)) {
          String vars = (String) event.data;
          List<Entity> entities = new ArrayList<Entity>();
          Scanner scanner = new Scanner(vars).useDelimiter(",");
          while (scanner.hasNext()) {
            String item = scanner.next();
            IRepository repository = ServiceProvider.getRepository();
            Entity entity = (Entity) repository.get(item);
            if (entity != null) {
              entities.add(entity);
            }
          }
          // The wavelet viewer part consists of 3 plots, so drag-and-drop events need to
          // add the selected wavelets into each of the 3 plots.
          for (IViewer otherViewer : _viewers) {
            otherViewer.addObjects(entities.toArray(new Entity[0]));
          }
        }
      }
    });
  }
}
