/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.util;


import org.geocraft.core.model.datatypes.Trace;


public abstract class TraceConsumer implements Runnable {

  /** The trace producer from which to consume. */
  private final TraceProducer _producer;

  /** The flag indicating if the consumer has been started. */
  private boolean _isStarted;

  /**
   * Constructs a trace consumer.
   * 
   * @param producer the trace producer from which to consume.
   */
  public TraceConsumer(final TraceProducer producer) {
    _producer = producer;
    _isStarted = false;
  }

  /**
   * Starts the consumer in a separate thread.
   */
  public synchronized void startInThread() {
    if (!_isStarted) {
      _isStarted = true;
      Thread thread = new Thread(this);
      thread.start();
    }
  }

  public final void run() {
    // Consume traces as long as the producer is not done.
    while (!_producer.isDone()) {
      Trace[] traces = _producer.get();
      if (traces != null) {
        processTraces(traces);
      }
    }
  }

  /**
   * Returns the current message from the associated producer.
   * 
   * @return the current message.
   */
  protected String getMessage() {
    return _producer.getMessage();
  }

  /**
   * Processes the given traces consumed from the producer.
   * 
   * @param traces the array of traces to process.
   */
  protected abstract void processTraces(Trace[] traces);
}
