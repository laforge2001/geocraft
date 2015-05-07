/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.internal.algorithm;


import org.eclipse.jface.action.Action;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geocraft.ui.form2.ModelForm;


public class CollapseAllAction extends Action {

  ModelForm _form;

  public CollapseAllAction() {
    setToolTipText("Collapse all sections");
    setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin("org.geocraft.algorithm",
        "icons/collapse_all_16x16.gif"));
    _form = null;
  }

  public void setForm(ModelForm form) {
    _form = form;
  }

  @Override
  public void run() {
    if (_form != null) {
      Section[] sections = _form.getSections();
      for (Section formSection : sections) {
        if (formSection.isExpanded()) {
          formSection.setExpanded(false);
        }
      }
    }
  }
}
