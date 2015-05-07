/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.algorithm.AlgorithmUsageManager;
import org.geocraft.algorithm.IStandaloneAlgorithmDescription;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.algorithm.StandaloneAlgorithmTask;
import org.geocraft.core.common.progress.SafeTask;
import org.geocraft.core.common.progress.TaskRunner;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.model.validation.Validation;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.session.AlgorithmParameterStore;
import org.geocraft.ui.common.image.ISharedImages;
import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * Triggers the algorithm to run.
 */
public class RunStandaloneAlgorithm extends Action {

  /** A shell used for popup error messages. */
  private final Shell _shell;

  /** The algorithm description, used to create a algorithm instance for execution. */
  private IStandaloneAlgorithmDescription _algorithmDescription;

  private StandaloneAlgorithm _algorithm;

  public RunStandaloneAlgorithm(final Shell shell) {
    _shell = shell;
    setText("Run");
    setToolTipText("Run the current algorithm");
    setImageDescriptor(ImageRegistryUtil.getSharedImages().getImageDescriptor(ISharedImages.IMG_RUN));
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
    System.out.println("Running Algorithm: " + _algorithmDescription.getName());
    if (_algorithmDescription != null) {
      try {

        StandaloneAlgorithm algorithm = _algorithm;
        if (_algorithm.createCopy()) {
          // Create a copy of the algorithm and updates its parameters.
          // This copy is done so that subsequent updates to the UI
          // do not affect the task once it has started.
          algorithm = _algorithmDescription.createAlgorithm();
          algorithm.updateFrom(_algorithm);
        }

        // Perform a last-minute validation to make sure there are no errors.
        IValidation results = new Validation();
        algorithm.validate(results);
        if (results.containsError()) {
          // If there are, throw an exception.
          throw new IllegalArgumentException(results.getStatusMessages(IStatus.WARNING));
        }

        // Save the parameters used to run
        try {
          AlgorithmParameterStore.save(_algorithm);
        } catch (Exception ex) {
          // If an exception occurs, popup an error dialog.
          String message = "Algorithm parameters not saved:\n" + ex.getMessage();
          MessageDialog.openError(_shell, "Save Algorithm Parameters Error", message);
        }

        // Create a task to run the algorithm.
        StandaloneAlgorithmTask task = new StandaloneAlgorithmTask(algorithm, _algorithmDescription.getName());

        // Run the task.
        Object job = TaskRunner.runTask(task, _algorithmDescription.getName(), TaskRunner.INTERACTIVE);

        if (job instanceof SafeTask) {
          // Notify the usage manager the algorithm has started.
          AlgorithmUsageManager.algorithmStarted(job, _algorithmDescription);
          reportJobCompletion((SafeTask) job);
        }

      } catch (Exception ex) {
        String title = "Algorithm Error: " + _algorithmDescription.getName();
        String message = ex.getMessage();
        MessageDialog.openError(_shell, title, message);
        ServiceProvider.getLoggingService().getLogger(getClass()).error(title + "\n" + message, ex);
        return;
      }
    }
  }

  /**
   * Creates a new thread and waits for the job to complete.
   * It then checks the job status and posts corresponding completion message to log.
   * 
   * @param task the algorithm task.
   */
  private void reportJobCompletion(final SafeTask task) {
    Thread t = new Thread() {

      @Override
      public void run() {
        IStatus status;
        while ((status = task.getStatus()) == null) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        if (status.equals(Status.OK_STATUS)) {
          // If the task completed with an OK status, then log a "success" message.
          ServiceProvider.getLoggingService().getLogger(task.getClass()).info(
              _algorithmDescription.getName() + " completed successfully.");
        } else if (status.equals(Status.CANCEL_STATUS)) {
          // If the task was canceled, then log a "canceled" message.
          ServiceProvider.getLoggingService().getLogger(task.getClass()).warn(
              _algorithmDescription.getName() + " canceled.");
        } else {
          // Otherwise the task did not complete, so log an error message.
          ServiceProvider.getLoggingService().getLogger(task.getClass()).error(
              _algorithmDescription.getName() + " failed: " + status.getMessage());
        }
        // Notify the usage manager the algorithm has ended.
        AlgorithmUsageManager.algorithmEnded(task, status);
      }
    };
    t.start();

  }
}
