/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


public interface IFieldListener {

  void fieldChanged(String key, Object valueObject);

  void fieldEnabled(String key, boolean enabled);
}
