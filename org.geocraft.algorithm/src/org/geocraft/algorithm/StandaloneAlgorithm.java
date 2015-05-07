/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.algorithm;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.geocraft.algorithm.thread.IDataSource;
import org.geocraft.algorithm.thread.MultiThreadedAlgorithmWorker;
import org.geocraft.core.common.util.Utilities;
import org.geocraft.core.model.IModel;
import org.geocraft.core.model.IModelListener;
import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.core.session.SessionManager;
import org.geocraft.ui.form2.IModelForm;


/**
 * The abstract base class for standalone algorithms.
 * For the simplest of algorithms, developers will have only 5 methods to implement.
 * These include <code>buildUI()</code>, <code>initialize()</code>, <code>update()</code>,
 * <code>validate()</code> and <code>run()</code>.
 */
public abstract class StandaloneAlgorithm extends Model implements IModelListener {

  private IModelForm _modelForm;

  public StandaloneAlgorithm() {
    super();
  }

  /**
   * Runs the domain logic of the algorithm.
   * 
   * @param monitor the progress monitor.
   * @param logger the logger to log messages.
   * @param repository the repository in which to add output entities.
   */
  public abstract void run(IProgressMonitor monitor, ILogger logger, IRepository repository) throws CoreException;

  /**
   * Constructs the view of fields used to edit the algorithm parameters.
   * The related fields can be grouped together into sections.
   * 
   * @param modelForm the form in which to add the sections the field editors.
   */
  public abstract void buildView(IModelForm modelForm);

  public void setModelForm(IModelForm modelForm) {
    _modelForm = modelForm;
  }

  public void setModel(IModel model) {
    updateFrom(model);
  }

  /**
   * Hides/Shows a field.
   * 
   * @param property The property of the field to hide/show.
   * @param visible <i>true</i> to show the field; <i>false</i> to hide the field.
   */
  public void setFieldVisible(Property property, boolean visible) {
    setFieldVisible(property, visible, true);
  }

  /**
   * Hides/Shows a field.
   * 
   * @param property The property of the field to hide/show.
   * @param visible <i>true</i> to show the field; <i>false</i> to hide the field.
   * @param redoLayout <i>true</i> to force a redo of the layout; otherwise <i>false</i>.
   */
  public void setFieldVisible(Property property, boolean visible, boolean redoLayout) {
    if (_modelForm != null) {
      _modelForm.setFieldVisible(property.getKey(), visible, redoLayout);
    }
  }

  /**
   * Redo the layout of the Composite housing the field associated with the property.
   */
  public void redoLayout(Property property) {
    if (_modelForm != null) {
      _modelForm.redoLayout(property.getKey());
    }
  }

  /**
   * Enables/disables a field.
   * 
   * @param property the property of the field to enable/disable.
   * @param enabled <i>true</i> to enable the field; <i>false</i> to disable the field.
   */
  public void setFieldEnabled(Property property, boolean enabled) {
    setFieldEnabled(property.getKey(), enabled);
  }

  /**
   * Enables/disables a field.
   * 
   * @param propertyKey the key of the field to enable/disable.
   * @param enabled <i>true</i> to enable the field; <i>false</i> to disable the field.
   */
  public void setFieldEnabled(String propertyKey, boolean enabled) {
    if (_modelForm != null) {
      _modelForm.setFieldEnabled(propertyKey, enabled);
    }
  }

  public void setAllFieldsEnabled(boolean enabled) {
    String[] propertyKeys = getPropertyKeys();
    for (String key : propertyKeys) {
      setFieldEnabled(key, enabled);
    }
  }

  /**
   * Sets the label on a field.
   * 
   * @param property The property of the field to update.
   * @param label the label to set.
   */
  public void setFieldLabel(Property property, String label) {
    setFieldLabel(property.getKey(), label);
  }

  /**
   * Sets the label on a field.
   * 
   * @param propertyKey the key of the field to update.
   * @param label the label to set.
   */
  public void setFieldLabel(String propertyKey, String label) {
    if (_modelForm != null) {
      _modelForm.setFieldLabel(propertyKey, label);
    }
  }

  /**
   * Sets the options available on an entity combo field.
   * 
   * @param property The property of the field to update.
   * @param filter the filter to set.
   */
  public void setFieldFilter(Property property, ISpecification filter) {
    if (_modelForm != null) {
      _modelForm.setFieldFilter(property.getKey(), filter);
    }
  }

  /**
   * Sets the options available on a combo field.
   * 
   * @param property The property of the field to update.
   * @param options the array of options to set.
   */
  public void setFieldOptions(Property property, Object[] options) {
    setFieldOptions(property.getKey(), options);
  }

  /**
   * Sets the options available on a combo field.
   * 
   * @param propertyKey the key of the field to update.
   * @param options the array of options to set.
   */
  public void setFieldOptions(String propertyKey, Object[] options) {
    if (_modelForm != null) {
      _modelForm.setFieldOptions(propertyKey, options);
    }
  }

  /**
   * By default, always run using a copy of the UI-edited properties.
   * Override at your own risk.
   */
  public boolean createCopy() {
    return true;
  }

  protected final synchronized void runMultiThreadedSubTask(String taskName, int totalWork, int ticks,
      List<MultiThreadedAlgorithmWorker> workers, ILogger logger, IProgressMonitor monitor) throws InterruptedException {
    runMultiThreadedSubTask(taskName, totalWork, ticks, workers, null, logger, monitor);
  }

  protected final synchronized void runMultiThreadedSubTask(String taskName, int totalWork, int ticks,
      List<MultiThreadedAlgorithmWorker> workerList, IDataSource dataSource, ILogger logger, IProgressMonitor monitor) throws InterruptedException {

    monitor.subTask(taskName);
    SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, ticks,
        SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
    subMonitor.beginTask(taskName, totalWork);

    List<Thread> threads = new ArrayList<Thread>();
    final MultiThreadedAlgorithmWorker[] workers = workerList.toArray(new MultiThreadedAlgorithmWorker[0]);
    for (MultiThreadedAlgorithmWorker worker : workers) {
      worker.init(logger, subMonitor);
      Thread t = new Thread(worker);
      threads.add(t);
      t.start();
    }

    Runnable workerTimer = new WorkerMessageQuery(subMonitor, workers);
    new Thread(workerTimer).start();
    // TODO: Implement this worker controller to adapt to the data source buffer.
    //    if (dataSource != null) {
    //      MultiThreadedWorkerController workerController = new MultiThreadedWorkerController(monitor, dataSource, workers);
    //      new Thread(workerController).start();
    //    }

    // Join all the threads to prevent continuation until all the threads complete.
    for (int i = 0; i < workers.length; i++) {
      System.out.println("Worker " + workers[i].getWorkerId() + " joined. " + threads.get(i).isAlive() + " "
          + threads.get(i).isInterrupted());
      threads.get(i).join();
    }
    System.out.println("All workers done.");
    subMonitor.done();
  }

  public void saveAsBatchFile(final Shell shell) {
    // Prompt the user to specify a file.
    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
    String workspace = Utilities.getWorkspaceDirectory();
    dialog.setFilterPath(workspace);
    dialog.setFilterExtensions(new String[] { "*" + SessionManager.BATCH_SUFFIX });
    String filePath = dialog.open();
    if (filePath != null) {
      // Check that that the file name has the proper extension.
      if (!filePath.endsWith(SessionManager.BATCH_SUFFIX)) {
        filePath = filePath + SessionManager.BATCH_SUFFIX;
      }
      // Save the batch file.
      SessionManager.getInstance().saveAlgorithmAsBatchSession(filePath, new Model[] { this });
    }
  }
}
