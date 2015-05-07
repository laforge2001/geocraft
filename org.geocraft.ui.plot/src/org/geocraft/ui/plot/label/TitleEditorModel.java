/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.label;


import org.geocraft.core.model.Model;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.ui.plot.defs.Alignment;


public class TitleEditorModel extends Model {

  private StringProperty _text;

  private EnumProperty<Alignment> _alignment;

  public TitleEditorModel(final ILabel label) {
    super();
    _text = addStringProperty(Label.TEXT, label.getText());
    _alignment = addEnumProperty(Label.ALIGNMENT, Alignment.class, label.getAlignment());
  }

  public void validate(final IValidation results) {
    if (_alignment == null) {
      results.error(Label.ALIGNMENT, "Invalid alignment: " + _alignment);
    }
  }

  public String getText() {
    return _text.get();
  }

  public Alignment getAlignment() {
    return _alignment.get();
  }

}
