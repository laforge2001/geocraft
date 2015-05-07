package org.geocraft.internal.ui.waveletviewer;


import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.geocraft.core.model.Entity;
import org.geocraft.ui.repository.RepositoryViewData;
import org.geocraft.ui.viewer.IViewer;
import org.geocraft.ui.viewer.ViewerHelper;
import org.geocraft.ui.waveletviewer.PreferencePage;
import org.geocraft.ui.waveletviewer.WaveletViewPart;


public class WaveletPlotAction implements IWorkbenchWindowActionDelegate {

  static {
    PreferencePage.setDefaults();
  }

  public void dispose() {
    // Nothing to do.
  }

  public void init(final IWorkbenchWindow window) {
    // Nothing to do.
  }

  public void run(final IAction action) {

    try {
      String id = Integer.toString(ViewerHelper.getNextViewerId());
      IViewPart viewPart = ViewerHelper.getViewerWindow().getActivePage().showView(
          "org.geocraft.ui.waveletviewer.WaveletViewPart", id, IWorkbenchPage.VIEW_ACTIVATE);
      if (viewPart != null) {
        Entity[] entities = RepositoryViewData.getSelectedEntities();
        for (IViewer viewer : ((WaveletViewPart) viewPart).getViewers()) {
          viewer.addObjects(entities);
        }
      }
    } catch (PartInitException e) {
      e.printStackTrace();
    }

    //    try {
    //      // Create the multi-plot part to contain the chart viewers in a gridded layout.
    //      int numColumns = 3;
    //      MultiPlotPart part = MultiPlotFactory.createPart(numColumns);
    //      part.setPartTitle("Wavelet Plots");
    //
    //      // Create the viewers and add them to the multi-plot part.
    //      WaveletViewer waveletViewer = new WaveletViewer(part.getViewerParent(), "Wavelet");
    //      waveletViewer.setLayerTreeVisible(false);
    //      part.addViewer(waveletViewer);
    //
    //      AmplitudeSpectrumViewer amplitudeSpectrumViewer = new AmplitudeSpectrumViewer(part.getViewerParent(), "Amplitude Spectrum");
    //      amplitudeSpectrumViewer.setLayerTreeVisible(false);
    //      part.addViewer(amplitudeSpectrumViewer);
    //
    //      PhaseSpectrumViewer phaseSpectrumViewer = new PhaseSpectrumViewer(part.getViewerParent(), "Phase Spectrum");
    //      phaseSpectrumViewer.setLayerTreeVisible(false);
    //      part.addViewer(phaseSpectrumViewer);
    //
    //    } catch (PartInitException e) {
    //      e.printStackTrace();
    //    }
  }

  public void selectionChanged(final IAction action, final ISelection selection) {
    // does nothing for now
  }
}
