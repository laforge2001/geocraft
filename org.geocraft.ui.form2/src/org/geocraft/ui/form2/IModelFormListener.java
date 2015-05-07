/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


public interface IModelFormListener {

  /**
   * Invoked when the model form is updated.
   * 
   * @param key the key of the property that triggered the update.
   */
  void modelFormUpdated(String key);
}
