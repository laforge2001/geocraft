/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.plot.settings;


import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.plot.defs.FillStyle;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


public class PropertiesSectionFactory {

  /**
   * Creates a form section for editing display properties for points.
   * This includes the point style, size and color.
   * 
   * @param form the parent form.
   * @param sectionLabel the section label.
   * @param pointStyleKey the key for the point style property.
   * @param pointSizeKey the key for the point size property.
   * @param pointColorKey the key for the point color property.
   * @return the form section.
   */
  public static FormSection addPointSection(final IModelForm form, final String sectionLabel,
      final String pointStyleKey, final String pointSizeKey, final String pointColorKey) {

    // Create the form section.
    FormSection section = form.addSection(sectionLabel, false);

    // Add the controls for editing the point style, size and color.
    section.addComboField(pointStyleKey, PointStyle.values()).setLabel("Point Style");
    section.addSpinnerField(pointSizeKey, 0, 100, 0, 1).setLabel("Point Size");
    section.addColorField(pointColorKey).setLabel("Point Color");

    return section;
  }

  /**
  * Creates a form section for editing display properties for lines.
  * This includes the line style, width and color.
  * 
  * @param form the parent form.
  * @param sectionLabel the section label.
  * @param lineStyleKey the key for the line style property.
  * @param lineWidthKey the key for the line width property.
  * @param lineColorKey the key for the line color property.
  * @return the form section.
  */
  public static FormSection addLineSection(final IModelForm form, final String sectionLabel, final String lineStyleKey,
      final String lineWidthKey, final String lineColorKey) {

    // Create the form section.
    FormSection section = form.addSection(sectionLabel, false);

    // Add the controls for editing the line style, width and color.
    section.addComboField(lineStyleKey, LineStyle.values()).setLabel("Line Style");
    section.addSpinnerField(lineWidthKey, 0, 100, 0, 1).setLabel("Line Width");
    section.addColorField(lineColorKey).setLabel("Line Color");

    return section;
  }

  /**
   * Creates a form section for editing display properties for fills.
   * This includes the fill style and color.
   * 
   * @param form the parent form.
   * @param sectionLabel the section label.
   * @param fillStyleKey the key for the fill style property.
   * @param fillColorKey the key for the fill color property.
   * @return the form section.
   */
  public static FormSection addFillSection(final IModelForm form, final String sectionLabel, final String fillStyleKey,
      final String fillColorKey) {

    // Create the form section.
    FormSection section = form.addSection(sectionLabel, false);

    // Add the controls for editing the fill style and color.
    section.addComboField(fillStyleKey, FillStyle.values()).setLabel("Fill Style");
    section.addColorField(fillColorKey).setLabel("Fill Color");

    return section;
  }

  /**
   * Creates a form section for editing display properties for text.
   * This includes the text font and color.
   * 
   * @param form the parent form.
   * @param sectionLabel the section label.
   * @param textFontKey the key for the text font property.
   * @param textColorKey the key for the text color property.
   * @return the form section.
   */
  public static FormSection addTextSection(final IModelForm form, final String sectionLabel, final String textFontKey,
      final String textColorKey) {

    // Create the form section.
    FormSection section = form.addSection(sectionLabel, false);

    // Add the controls for editing the text font and color.
    section.addFontField(textFontKey).setLabel("Text Font");
    section.addColorField(textColorKey).setLabel("Text Color");

    return section;
  }
}
