/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


public class ExportStandaloneAlgorithmAsBatch extends Action {

  /** A shell used for popup error messages. */
  private final Shell _shell;

  /** The algorithm description, used to create a algorithm instance for execution. */
  private IStandaloneAlgorithmDescription _algorithmDescription;

  private StandaloneAlgorithm _algorithm;

  public ExportStandaloneAlgorithmAsBatch(final Shell shell) {
    _shell = shell;
    setText("Export to batch...");
    setToolTipText("Export the current algorithm to a batch file");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_EXPORT));
  }

  /**
   * Sets the algorithm description needed for execution.
   * The algorithm provided in this method is the source algorithm connected to the editor UI.
   * When the action is triggered, a copy of this algorithm is made, and it is the copy
   * that is actually run, so that subsequent updates to the UI do not affect the
   * running task.
   * @param algorithmDescription the algorithm description.
   * @param algorithm the source algorithm.
   */
  public void setDescription(final IStandaloneAlgorithmDescription algorithmDescription,
      final StandaloneAlgorithm algorithm) {
    _algorithmDescription = algorithmDescription;
    _algorithm = algorithm;
  }

  @Override
  public void run() {
    System.out.println("Exporting Algorithm: " + _algorithmDescription.getName());
    if (_algorithmDescription != null) {
      try {
        _algorithm.saveAsBatchFile(_shell);
      } catch (Exception ex) {
        String title = "Batch Export Error: " + _algorithmDescription.getName();
        String message = ex.getMessage();
        MessageDialog.openError(_shell, title, message);
        ServiceProvider.getLoggingService().getLogger(getClass()).error(title + "\n" + message, ex);
        return;
      }
    }
  }
}
