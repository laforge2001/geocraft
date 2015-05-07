/*
 * Copyright (C) ConocoPhillips 2007 All Rights Reserved.
 */
package org.geocraft.ui.common.util.validation;


import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JTextField;

import org.geocraft.ui.common.image.ImageRegistryUtil;


/**
 * A text field that validates the input data.
 */
public class ValidTextField extends JTextField {

  /** The warning image. */
  private static ImageIcon _image = ImageRegistryUtil.createImageIcon("icons/misc/stop.png");

  /** The warning image size. */
  private static final int SIZE = 10;

  /** Default number of characters to display in the textfield. */
  private static final int FIELD_LENGTH = 15;

  /** The current validator. */
  private final IValidator _validator;

  /**
   * The constructor.
   * 
   * @param validator the current validator
   */
  public ValidTextField(final IValidator validator) {
    super(FIELD_LENGTH);
    _validator = validator;
  }

  /**
   * The constructor.
   * 
   * @param validator the current validator
   * @param text the initial text
   */
  public ValidTextField(final IValidator validator, final String text) {
    super(text);
    _validator = validator;
  }

  public IValidator getValidator() {
    return _validator;
  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);
    if (!_validator.validate(getText().trim())) {
      if (_image != null) {
        g.drawImage(_image.getImage(), 0, getHeight() - SIZE, SIZE, SIZE, null);
      } else {
        g.setFont(g.getFont().deriveFont(9f));
        g.drawString("Warning", 0, getHeight() - 4);
      }
    }
  }

}
