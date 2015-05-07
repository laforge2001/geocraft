/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.core.common.progress;


import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;


public class TaskRunner {

  /** Run the given task asynchronously without joining the job to wait for the result **/
  public static final int NO_JOIN = 0;

  /** Run the given task asynchronously and join the job to wait for the result **/
  public static final int JOIN = 1;

  /** Run the given task synchronously without using Jobs or Threads and wait for the result **/
  public static final int INLINE = 2;

  /** Run the given task in interactive mode (i.e., allow user interaction with job progress etc.
   * This is only relevant if the task is being run asynchronously.  **/
  public static final int INTERACTIVE = 4;

  public static final int LONG = 5;

  /** 
   * Run a simple task. 
   * 
   * @param task
   * @param taskName
   * @return
   */
  public static Object runTask(final BackgroundTask task, final String taskName) {
    return runTask(task, taskName, JOIN);
  }

  private static Object scheduleTask(final SafeTask job, final int flags) {
    if ((flags & INLINE) > 0) {
      return job.run(null);
    }

    job.setUser(true);
    if ((flags & INTERACTIVE) > 0) {
      job.setPriority(Job.INTERACTIVE);
    }
    job.schedule();
    if ((flags & JOIN) > 0) {
      try {
        job.join();
        return job.getValue();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    // return the job for some callers to be able to add a job listener on this
    return job;
  }

  public static Object runTask(final Object task, final String taskName, final int flags, final ISchedulingRule rule) {
    SafeTask job = new SafeTask(task, taskName);
    job.setRule(rule);
    return scheduleTask(job, flags);
  }

  /**
   * Run the given task according to the given flags.
   * 
   * @param task the task to run. 
   * @param taskName a name that describes the task that does not have to be unique. 
   * @return the result from the computation. The job object is returned if the 
   * task is run without JOIN or INLINE such as method callers to be able to add a job listener to it.
   */
  public static Object runTask(final Object task, final String taskName, final int flags) {
    SafeTask job = new SafeTask(task, taskName);
    return scheduleTask(job, flags);
  }
}
