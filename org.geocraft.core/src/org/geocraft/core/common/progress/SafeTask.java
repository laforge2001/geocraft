/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.core.common.progress;


import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.geocraft.core.service.ServiceProvider;


public class SafeTask extends Job implements ISafeRunnable {

  /** The task to wrap. */
  private Object _task;

  /** The value returned by the task. */
  private Object _value;

  /** The completion status of the task. */
  private IStatus _status;

  /** The progress monitor for the task. */
  private IProgressMonitor _monitor;

  private long _startTime = 0;

  private long _endTime = 0;

  /**
   * An internal implementation class that ensures tasks provided by plugins 
   * do not cripple the GeoCraft platform by throwing random exceptions.
   */
  public SafeTask(final Object task, final String taskName) {
    super(taskName);
    this._task = task;
  }

  @Override
  protected IStatus run(final IProgressMonitor monitor) {
    // Set the starting time.
    _startTime = System.currentTimeMillis();

    this._monitor = monitor;
    try {
      SafeRunner.run(this);
      if (monitor.isCanceled()) {
        _status = Status.CANCEL_STATUS;
        return Status.CANCEL_STATUS;
      }
      return Status.OK_STATUS;
    } finally {
      // Clean up a bit just to prevent leaks.
      _task = null;
      this._monitor = null;
    }
  }

  /**
   * Currently used for setting the status if the task has thrown an exception (failed). 
   * Assumes that the task is already reporting exceptions to the logging system.
   */
  public void handleException(final Throwable exception) {
    // Set the ending time.
    _endTime = System.currentTimeMillis();

    // Set the status as canceled.
    _status = ValidationStatus.cancel(exception.getMessage());

    // Log the full exception as a debug message.
    ServiceProvider.getLoggingService().getLogger(_task.getClass()).debug(exception.getMessage(), exception);
  }

  public void run() throws Exception {
    if (_task instanceof BackgroundTask) {
      _value = ((BackgroundTask) _task).compute(ServiceProvider.getLoggingService().getLogger(_task.getClass()),
          _monitor);

      // Set the ending time.
      _endTime = System.currentTimeMillis();

      // If the algorithm runs to completion (no exceptions thrown) it is considered a successful run.
      _status = Status.OK_STATUS;
    }
  }

  public long getElapsedTime() {
    return _endTime - _startTime;
  }

  /**
   * Gets the value returned from the task.
   */
  public Object getValue() {
    return _value;
  }

  /**
   * Returns the status of the algorithm.
   * This is null until the algorithm either completes or throws an exception.
   */
  public IStatus getStatus() {
    return _status;
  }
}
