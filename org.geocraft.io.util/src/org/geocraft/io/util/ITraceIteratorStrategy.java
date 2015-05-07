/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */

package org.geocraft.io.util;

import org.geocraft.core.model.datatypes.Trace;

/**
 * The interface defining a read strategy for a trace iterator.
 */
public interface ITraceIteratorStrategy {

	/**
	 * Reads the next inline, xline or slice.
	 * 
	 * @return the next inline, xline or slice.
	 */
	Trace[] readNext();

	/**
	 * Returns <i>true</i> if done reading; <i>false</i> if not.
	 * 
	 * @return <i>true</i> if done reading; <i>false</i> if not.
	 */
	boolean isDone();

	/**
	 * Returns the iterator status message.
	 * 
	 * @return the iterator status message.
	 */
	String getMessage();

	/**
	 * Returns the iterator completion status (in the range 0-100).
	 * 
	 * @return the iterator completion status (in the range 0-100).
	 */
	float getCompletion();
}
