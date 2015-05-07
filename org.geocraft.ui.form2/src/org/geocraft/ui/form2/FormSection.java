/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2;


import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.core.model.Entity;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.FileProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.ObjectArrayProperty;
import org.geocraft.core.model.property.ObjectProperty;
import org.geocraft.core.model.property.OutputEntityProperty;
import org.geocraft.core.model.property.Property;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.ui.common.TableWrapLayoutHelper;
import org.geocraft.ui.form2.field.ButtonField;
import org.geocraft.ui.form2.field.CheckGroupField;
import org.geocraft.ui.form2.field.CheckboxField;
import org.geocraft.ui.form2.field.ColorField;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.DatastoreComboField;
import org.geocraft.ui.form2.field.DefinedListField;
import org.geocraft.ui.form2.field.DirectoryField;
import org.geocraft.ui.form2.field.EnhancedComboField;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.EntityListField;
import org.geocraft.ui.form2.field.FileField;
import org.geocraft.ui.form2.field.FontField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.ListSelectionField;
import org.geocraft.ui.form2.field.OrderedListField;
import org.geocraft.ui.form2.field.OutputEntityTextField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.ScaleField;
import org.geocraft.ui.form2.field.SpinnerField;
import org.geocraft.ui.form2.field.TableField;
import org.geocraft.ui.form2.field.TextBox;
import org.geocraft.ui.form2.field.TextField;
import org.geocraft.ui.form2.field.aoi.AOIComboField;


/**
 * Defines a sub-section of a model form that can be used to group related property fields
 */
public class FormSection {

  public static enum SortType {
    NONE,
    BY_NAME,
  }

  /** The UI section widget. */
  private final Section _section;

  /** The parent parameter form. */
  private ModelForm _form;

  /** The toolkit to use for adapting. */
  private FormToolkit _formToolkit;

  /** The font to use for comments. */
  private static Font _commentFont;

  public FormSection(final Section section, ModelForm form, FormToolkit formToolkit) {
    _section = section;
    _form = form;
    _formToolkit = formToolkit;
  }

  /**
   * Returns the UI section widget.
   */
  public Section getSection() {
    return _section;
  }

  /**
   * Returns the composite contained by the section.
   */
  public Composite getComposite() {
    return (Composite) _section.getClient();
  }

  /**
   * Sets the label text for the section.
   * 
   * @param label the label text to set.
   */
  public void setLabel(final String label) {
    _section.setText(label);
  }

  /**
   * Adds a comment field to the form section.
   * The comment field is used to display informational text, just like the label field.
   * However, a comment field is not associated with any model property.
   * 
   * @param comment the comment to display.
   */
  public void addCommentField(String comment) {
    addCommentField(comment, false);
  }

  /**
   * Adds a comment field to the form section.
   * The comment field is used to display informational text, just like the label field.
   * However, a comment field is not associated with any model property.
   * 
   * @param comment the comment to display.
   */
  public void addCommentField(String comment, boolean italics) {
    Label commentLabel = new Label(getComposite(), SWT.LEFT | SWT.WRAP);
    commentLabel.setText(comment);
    if (italics) {
      commentLabel.setFont(getCommentFont());
    }
    TableWrapData layoutData = TableWrapLayoutHelper.createLayoutData(true, false, TableWrapData.FILL,
        TableWrapData.FILL);
    layoutData.colspan = 4;
    layoutData.rowspan = 1;
    layoutData.maxWidth = 200;
    commentLabel.setLayoutData(layoutData);
  }

  /**
   * Adds a label field to the form section.
   * The label field is used to display (not edit) informational text.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @return the label field.
   */
  public LabelField addLabelField(final Property prop) {
    return addLabelField(prop.getKey());
  }

  /**
   * Adds a label field to the form section.
   * The label field is used to display (not edit) informational text.
   * 
   * @param key the key of the numeric or string <code>Property</code> in the associated model.
   * @return the label field.
   */
  public LabelField addLabelField(final String key) {
    String label = key;
    LabelField labelField = new LabelField(getComposite(), _form, key, label, false);
    labelField.adapt(_formToolkit);
    _form.mapField(key, labelField);
    return labelField;
  }

  /**
   * @deprecated the table field API is incomplete.
   */
  @Deprecated
  public TableField addTableField(final String key) {
    TableField tf = new TableField(getComposite(), _form, key, key, false);
    tf.adapt(_formToolkit);
    _form.mapField(key, tf);
    return tf;
  }

  /**
   * Adds a color field to the form section.
   * The color field contains a button to popup a color selection dialog.
   * 
   * @param key the key of the <code>ColorProperty</code> in the associated model.
   * @return the color field.
   */
  public ColorField addColorField(final String key) {
    String label = key;
    ColorField colorField = new ColorField(getComposite(), _form, key, label, false);
    colorField.adapt(_formToolkit);
    _form.mapField(key, colorField);
    return colorField;
  }

  /**
   * Adds a font field to the form section.
   * The font field contains a button to popup a font selection dialog.
   * 
   * @param key the key of the <code>FontProperty</code> in the associated model.
   * @return the font field.
   */
  public FontField addFontField(final String key) {
    String label = key;
    FontField fontField = new FontField(getComposite(), _form, key, label, false);
    fontField.adapt(_formToolkit);
    _form.mapField(key, fontField);
    return fontField;
  }

  /**
   * Adds a checkbox field to the form section.
   * The check field consists of a single toggle button.
   * 
   * @param property the <code>BooleanProperty</code> in the associated model.
   * @return the checkbox field.
   */
  public CheckboxField addCheckboxField(final BooleanProperty property) {
    return addCheckboxField(property.getKey());
  }

  /**
   * Adds a checkbox field to the form section.
   * The check field consists of a single toggle button.
   * 
   * @param key the key of the <code>BooleanProperty</code> in the associated model.
   * @return the checkbox field.
   */
  public CheckboxField addCheckboxField(final String key) {
    String label = key;
    CheckboxField buttonField = new CheckboxField(getComposite(), _form, key, label, false);
    buttonField.adapt(_formToolkit);
    _form.mapField(key, buttonField);
    return buttonField;
  }

  public Button addPushButton(String text) {
    Button button = new Button(getComposite(), SWT.PUSH);
    button.setText(text);
    TableWrapData layoutData = TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.FILL,
        TableWrapData.FILL);
    layoutData.colspan = 4;
    layoutData.rowspan = 1;
    layoutData.maxWidth = 200;
    button.setLayoutData(layoutData);
    return button;
  }

  /**
   * Adds a button field to the form section, which consists of a
   * single push button.
   * 
   * @param property the <code>BooleanProperty</code> in the associated model.
   * @param text Button's text.
   * @return The push button field.
   */
  public ButtonField addButtonField(final Property property, String text, int rightMargin) {
    return addButtonField(property.getKey(), text, rightMargin);
  }

  /**
   * Adds a button field to the form section.
   * The button field consists of a single push button.
   * 
   * @param key The key of the <code>BooleanProperty</code> in the associated model.
   * @param text The button's text.
   * @param rightMargin Position of the right side of the button.
   * @return The push button field.
   */
  public ButtonField addButtonField(final String key, final String text, final int rightMargin) {
    ButtonField buttonField = new ButtonField(getComposite(), _form, key, text, rightMargin);
    buttonField.adapt(_formToolkit);
    _form.mapField(key, buttonField);
    return buttonField;
  }

  /**
   * Adds a button field to the form section.
   * The button field consists of a single push button.
   * 
   * Calls the other add button field with the label coming from the passed in key and a
   * default right margin of 0
   * 
   * @param key The key of the <code>BooleanProperty</code> in the associated model.
   * 
   * @return The push button field.
   */
  public ButtonField addButtonField(final String key) {
    return addButtonField(key, key, 0);
  }

  /**
   * Adds a button field to the form section.
   * The button field consists of a single push button.
   * 
   * Calls the other add button field with the label coming from the passed in property and a
   * default right margin of 0
   * 
   * @param key The key of the <code>BooleanProperty</code> in the associated model.
   * 
   * @return The push button field.
   */
  public ButtonField addButtonField(final Property prop) {
    return addButtonField(prop.getKey());
  }

  /**
   * Adds a button field to the form section, which consists of a set
   * of push buttons.
   * 
   * @param property the <code>BooleanProperty</code> in the associated model.
   * @param key Property key for set of buttons.
   * @param texts Text for each push button to be created in the field.
   * @param rightMargin Position of the right side of the button.
   * @return The push button field.
   */
  public ButtonField addButtonsField(final Property[] properties, String key, String[] texts, int rightMargin) {
    String[] keys = new String[texts.length];
    for (int i = 0; i < texts.length; i++) {
      keys[i] = properties[i].getKey();
    }
    return addButtonsField(keys, key, texts, rightMargin);
  }

  /**
   * Adds a button field to the form section, which consists of a set
   * of push buttons.
   * 
   * @param keys The keys of the set of <code>BooleanProperty</code> in the associated model.
   * @param texts Text for each push button to be created in the field.
   * @param rightMargin Position of the right side of the button.
   * @return The push button field.
   */
  public ButtonField addButtonsField(final String[] keys, final String key, final String[] texts, final int rightMargin) {
    ButtonField buttonField = new ButtonField(getComposite(), _form, key, keys, texts, rightMargin);
    buttonField.adapt(_formToolkit);
    for (int i = 0; i < texts.length; i++) {
      _form.mapField(keys[i], buttonField);
    }
    return buttonField;
  }

  /**
   * Adds a text field to the form section.
   * The text field is used to enter numeric or string values.
   * 
   * @param property the numeric or string <code>Property</code> in the associated model.
   * @return the text field.
   */
  public TextField addTextField(final Property property) {
    return addTextField(property.getKey());
  }

  /**
   * Adds a text field to the form section.
   * The text field is used to enter numeric or string values.
   * 
   * @param key the key of the numeric or string <code>Property</code> in the associated model.
   * @return the text field.
   */
  public TextField addTextField(final String key) {
    String label = key;
    TextField textField = new TextField(getComposite(), _form, key, label, false);
    textField.adapt(_formToolkit);
    _form.mapField(key, textField);
    return textField;
  }

  /**
   * Adds a text box to the form section.
   * The text box is used to enter multi-line text.
   * 
   * @param property the <code>StringProperty</code> in the associated model.
   * @return the text box.
   */
  public TextBox addTextBox(final StringProperty property) {
    return addTextBox(property.getKey());
  }

  /**
   * Adds a text box to the form section.
   * The text box is used to enter multi-line text.
   * 
   * @param key the key of the numeric <code>StringProperty</code> in the associated model.
   * @return the text box.
   */
  public TextBox addTextBox(final String key) {
    String label = key;
    TextBox textBox = new TextBox(getComposite(), _form, key, label, false);
    textBox.adapt(_formToolkit);
    _form.mapField(key, textBox);
    return textBox;
  }

  /**
   * Adds a text box to the form section when a prefered height
   * The text box is used to enter multi-line text.
   * 
   * @param property the <code>StringProperty</code> in the associated model.
   * @param height The prefered height of the text box
   * @return the text box.
   */
  public TextBox addTextBox(final StringProperty property, int height) {
    return addTextBox(property.getKey(), height);
  }

  /**
   * Adds a text box to the form section with a preferred height.
   * The text box is used to enter multi-line text.
   * 
   * @param key the key of the numeric <code>StringProperty</code> in the associated model.
   * @param height The prefered height of the text box
   * @return the text box.
   */
  public TextBox addTextBox(final String key, final int height) {
    String label = key;
    TextBox textBox = new TextBox(getComposite(), _form, key, label, false, height);
    textBox.adapt(_formToolkit);
    _form.mapField(key, textBox);
    return textBox;
  }

  /**
   * Adds an area-of-interest combo field to the form section.
   * The combo field contains a set of areas-of-interest to choose in a popdown menu.
   * 
   * @param property the <code>EntityProperty</code> in the associated model.
   * @param dimension the dimension of the AOI to create (2=2D, 3=3D).
   * @return the AOI combo field.
   */
  public AOIComboField addAOIComboField(EntityProperty<AreaOfInterest> property, int dimension) {
    return addAOIComboField(property.getKey(), dimension);
  }

  /**
   * Adds an area-of-interest combo field to the form section.
   * The combo field contains a set of areas-of-interest to choose in a popdown menu.
   * 
   * @param property the <code>EntityProperty</code> in the associated model.
   * @param dimension the dimension of the AOI to create (2=2D, 3=3D).
   * @param aoiClass the class of the allowed AOI's.
   * @return the AOI combo field.
   */
  public AOIComboField addAOIComboField(EntityProperty<AreaOfInterest> property, int dimension, Class aoiClass) {
    return addAOIComboField(property.getKey(), dimension, aoiClass);
  }

  /**
   * Adds an area-of-interest combo field to the form section.
   * The combo field contains a set of areas-of-interest to choose in a popdown menu.
   * 
   * @param property the <code>EntityProperty</code> in the associated model.
   * @param dimension the dimension of the AOI to create (2=2D, 3=3D).
   * @return the AOI combo field.
   */
  public AOIComboField addAOIComboField(String key, int dimension) {
    return addAOIComboField(key, dimension, AreaOfInterest.class);
  }

  /**
   * Adds an area-of-interest combo field to the form section.
   * The combo field contains a set of areas-of-interest to choose in a popdown menu.
   * 
   * @param property the <code>EntityProperty</code> in the associated model.
   * @param dimension the dimension of the AOI to create (2=2D, 3=3D).
   * @return the AOI combo field.
   */
  public AOIComboField addAOIComboField(String key, int dimension, Class aoiClass) {
    Entity[] validObjects = getFilteredEntities(new TypeSpecification(aoiClass));
    final AOIComboField comboField = addAOIComboField(key, key, dimension, aoiClass);
    comboField.setOptions(validObjects);
    return comboField;
  }

  private AOIComboField addAOIComboField(final String key, final String label, int dimension, Class aoiClass) {
    AOIComboField comboField = new AOIComboField(getComposite(), _form, key, label, dimension, aoiClass);
    comboField.adapt(_formToolkit);
    _form.mapField(key, comboField);
    return comboField;
  }

  /**
   * Adds a datastore combo field to the form section.
   * The combo field contains a set of entities to choose from in a popdown menu.
   * 
   * @param property the <code>DatastoreProperty</code> in the associated model.
   * @param filter the datastore class used to create a specification used to filter the available entities.
   * @return the datastore combo field.
   * 
   * @deprecated on hold for now!
   */
  @Deprecated
  private <T extends Entity> DatastoreComboField addDatastoreFields(final DatastoreProperty property, final Class T) {
    return addDatastoreFields(property.getKey(), T);
  }

  /**
   * Adds a datastore combo field to the form section.
   * The combo field contains a set of entities to choose from in a popdown menu.
   * 
   * @param key the key of the <code>ObjectProperty</code> in the associated model.
   * @param filter the specification used to filter the available entities.
   * @return the datastore combo field.
   * 
   * @deprecated on hold for now!
   */
  @Deprecated
  private <T extends Entity> DatastoreComboField addDatastoreFields(final String key, final Class T) {
    final DatastoreComboField comboField = addDatastoreComboField(key, key, T);
    return comboField;
  }

  /**
   * @deprecated on hold for now!
   */
  @Deprecated
  private <T extends Entity> DatastoreComboField addDatastoreComboField(final String key, final String label,
      final Class T) {
    DatastoreComboField comboField = new DatastoreComboField(getComposite(), _form, key, label, false, T);
    comboField.adapt(_formToolkit);
    _form.mapField(key, comboField);
    return comboField;
  }

  /**
   * Adds a entity combo field to the form section.
   * The combo field contains a set of entities to choose from in a popdown menu.
   * 
   * @param property the <code>EntityProperty</code> in the associated model.
   * @param filter the entity class used to create a specification used to filter the available entities.
   * @return the entity combo field.
   */
  public <T extends Entity> EntityComboField addEntityComboField(final EntityProperty<T> property, final Class filter) {
    return addEntityComboField(property.getKey(), new TypeSpecification(filter));
  }

  /**
   * Adds a entity combo field to the form section.
   * The combo field contains a set of entities to choose from in a popdown menu.
   * 
   * @param key the key of the <code>EntityProperty</code> in the associated model.
   * @param filter the entity class used to create a specification used to filter the available entities.
   * @return the entity combo field.
   */
  public EntityComboField addEntityComboField(final String key, final Class filter) {
    return addEntityComboField(key, new TypeSpecification(filter));
  }

  /**
   * Adds a entity combo field to the form section.
   * The combo field contains a set of entities to choose from in a popdown menu.
   * 
   * @param property the <code>EntityProperty</code> in the associated model.
   * @param filter the specification used to filter the available entities.
   * @return the entity combo field.
   */
  public <T extends Entity> EntityComboField addEntityComboField(final EntityProperty<T> property,
      final ISpecification filter) {
    return addEntityComboField(property.getKey(), filter);
  }

  /**
   * Adds a entity combo field to the form section.
   * The combo field contains a set of entities to choose from in a popdown menu.
   * 
   * @param key the key of the <code>ObjectProperty</code> in the associated model.
   * @param filter the specification used to filter the available entities.
   * @return the entity combo field.
   */
  public EntityComboField addEntityComboField(final String key, final ISpecification filter) {
    Entity[] validObjects = getFilteredEntities(filter);
    final EntityComboField comboField = addEntityComboField(key, key, filter);
    comboField.setOptions(validObjects);
    return comboField;
  }

  private EntityComboField addEntityComboField(final String key, final String label, final ISpecification filter) {
    EntityComboField comboField = new EntityComboField(getComposite(), _form, key, label, false, filter);
    comboField.adapt(_formToolkit);
    _form.mapField(key, comboField);
    return comboField;
  }

  public OutputEntityTextField addOutputEntityTextField(final OutputEntityProperty property) {
    return addOutputEntityTextField(property.getKey());
  }

  public OutputEntityTextField addOutputEntityTextField(final String key) {
    return addOutputEntityTextField(key, key);
  }

  private OutputEntityTextField addOutputEntityTextField(final String key, final String label) {
    OutputEntityTextField textField = new OutputEntityTextField(getComposite(), _form, key, label, false);
    textField.adapt(_formToolkit);
    _form.mapField(key, textField);
    return textField;
  }

  /**
   * Adds a combo field to the form section.
   * The combo field contains a set of options to choose from in a popdown menu.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the combo field.
   */
  public ComboField addComboField(Property property, Object[] options) {
    return addComboField(property.getKey(), options);
  }

  /**
   * Adds a combo field to the form section.
   * The combo field contains a set of options to choose from in a popdown menu.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the combo field.
   */
  public ComboField addComboField(Property property, Object[] options, boolean readOnly) {
    return addComboField(property.getKey(), options, readOnly);
  }

  /**
   * Adds a combo field to the form section.
   * The combo field contains a set of options to choose from in a popdown menu.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the combo field.
   */
  public ComboField addComboField(final String key, final Object[] options) {
    return addComboField(key, options, true);
  }

  /**
   * Adds a combo field to the form section.
   * The combo field contains a set of options to choose from in a popdown menu.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the combo field.
   */
  public ComboField addComboField(final String key, final Object[] options, boolean readOnly) {
    String label = key;
    ComboField comboField = new ComboField(getComposite(), _form, key, label, false, readOnly);
    comboField.adapt(_formToolkit);
    _form.mapField(key, comboField);
    comboField.setOptions(options);
    return comboField;
  }

  /**
   * Adds an enhanced combo field to the form section.
   * The enhanced combo field contains a "limited" set of options to choose from in a popdown menu.
   * It also contains a button to popup a dialog to select from a "complete" set of options.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @param someOptions the "limited" array of options from which to choose.
   * @param allOptions the "complete" array of options from which to choose.
   * @return the enhanced combo field.
   */
  public EnhancedComboField addEnhancedComboField(final Property property, final Object[] someOptions,
      final Object[] allOptions) {
    return addEnhancedComboField(property.getKey(), someOptions, allOptions);
  }

  /**
   * Adds an enhanced combo field to the form section.
   * The enhanced combo field contains a "limited" set of options to choose from in a popdown menu.
   * It also contains a button to popup a dialog to select from a "complete" set of options.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param someOptions the "limited" array of options from which to choose.
   * @param allOptions the "complete" array of options from which to choose.
   * @return the enhanced combo field.
   */
  public EnhancedComboField addEnhancedComboField(final String key, final Object[] someOptions,
      final Object[] allOptions) {
    String label = key;
    EnhancedComboField comboField = addEnhancedComboField(key, label);
    comboField.setOptions(someOptions, allOptions);
    return comboField;
  }

  private EnhancedComboField addEnhancedComboField(final String key, final String label) {
    EnhancedComboField comboField = new EnhancedComboField(getComposite(), _form, key, label, false);
    comboField.adapt(_formToolkit);
    _form.mapField(key, comboField);
    return comboField;
  }

  /**
   * Adds an enhanced combo field to the form section.
   * The enhanced combo field contains a "limited" set of options to choose from in a popdown menu.
   * It also contains a button to popup a dialog to select from a "complete" set of options.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param someOptions the "limited" array of options from which to choose.
   * @param allOptions the "complete" array of options from which to choose.
   * @return the enhanced combo field.
   */
  public ListSelectionField addListSelectionField(final String key, final Object[] options) {
    String label = key;
    ListSelectionField field = addListSelectionField(key, label);
    field.setOptions(options);
    return field;
  }

  private ListSelectionField addListSelectionField(final String key, final String label) {
    ListSelectionField comboField = new ListSelectionField(getComposite(), _form, key, label, false);
    comboField.adapt(_formToolkit);
    _form.mapField(key, comboField);
    return comboField;
  }

  public <T extends Entity> EntityListField addEntityListField(ObjectArrayProperty<T> property, Class<T> filter) {
    return addEntityListField(property.getKey(), filter);
  }

  public <T extends Entity> EntityListField addEntityListField(final String key, final Class<T> filter) {
    return addEntityListField(key, new TypeSpecification(filter));
  }

  public <T extends Entity> EntityListField addEntityListField(ObjectArrayProperty<T> property, ISpecification filter) {
    return addEntityListField(property.getKey(), filter);
  }

  public <T extends Entity> EntityListField addEntityListField(final ObjectProperty<T> property,
      final ISpecification filter) {
    return addEntityListField(property.getKey(), filter);
  }

  public EntityListField addEntityListField(final String key, final ISpecification filter) {
    Entity[] validObjects = getFilteredEntities(filter);
    final EntityListField listField = addEntityListField(key, key, filter);
    listField.setOptions(validObjects);
    return listField;
  }

  private EntityListField addEntityListField(final String key, final String label, final ISpecification filter) {
    EntityListField listField = new EntityListField(getComposite(), _form, key, label, false, filter);
    listField.adapt(_formToolkit);
    _form.mapField(key, listField);
    return listField;
  }

  /**
   * Adds an ordered list field to the form section.
   * The ordered list field contains a list box and buttons for adding/removing/shifting the
   * elements of the selection list.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @param options the array of options to select from in generating the ordered list.
   * @return the ordered list field.
   */
  public OrderedListField addOrderedListField(final Property property, final Object[] options) {
    return addOrderedListField(property.getKey(), options);
  }

  /**
   * Adds a defined list field to the form section.
   * The list field contains a list box and buttons for removing and shifting the
   * elements of the defined list.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param options the array of options to select from in generating the ordered list.
   * @return the ordered list field.
   */
  public DefinedListField addDefinedListField(final String key) {
    String label = key;
    DefinedListField listField = new DefinedListField(getComposite(), _form, key, label, false);
    listField.adapt(_formToolkit);
    _form.mapField(key, listField);
    return listField;
  }

  /**
   * Adds an ordered list field to the form section.
   * The ordered list field contains a list box and buttons for adding/removing/shifting the
   * elements of the selection list.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param options the array of options to select from in generating the ordered list.
   * @return the ordered list field.
   */
  public OrderedListField addOrderedListField(final String key, final Object[] options) {
    String label = key;
    OrderedListField listField = new OrderedListField(getComposite(), _form, key, label, false);
    listField.adapt(_formToolkit);
    _form.mapField(key, listField);
    listField.setOptions(options);
    return listField;
  }

  /**
   * Adds a spinner field to the form section.
   * The spinner field contains a slider for selecting a numeric value.
   * Note: Set digits=0 for integers, or digits>0 for floats.
   * 
   * @param property the numeric <code>Property</code> in the associated model.
   * @param minimum the minimum spinner value.
   * @param maximum the maximum spinner value.
   * @param digits the number of decimal places used.
   * @param increment the spinner increment.
   * @return the spinner field.
   */
  public SpinnerField addSpinnerField(final Property property, final int minimum, final int maximum, final int digits,
      final int increment) {
    return addSpinnerField(property.getKey(), minimum, maximum, digits, increment);
  }

  /**
   * Adds a spinner field to the form section.
   * The spinner field contains a slider for selecting a numeric value.
   * Note: Set digits=0 for integers, or digits>0 for floats.
   * 
   * @param key the key of the numeric <code>Property</code> in the associated model.
   * @param minimum the minimum spinner value.
   * @param maximum the maximum spinner value.
   * @param digits the number of decimal places used.
   * @param increment the spinner increment.
   * @return the spinner field.
   */
  public SpinnerField addSpinnerField(final String key, final int minimum, final int maximum, final int digits,
      final int increment) {
    String label = key;
    SpinnerField spinnerField = new SpinnerField(getComposite(), _form, key, label, false);
    spinnerField.adapt(_formToolkit);
    _form.mapField(key, spinnerField);
    spinnerField.setRange(minimum, maximum, digits, increment, increment);
    return spinnerField;
  }

  /**
   * Adds a scale field to the form section.
   * The scale field contains a slider for selecting a numeric value.
   * 
   * @param property the <code>IntegerProperty</code> in the associated model.
   * @param minimum the minimum spinner value.
   * @param maximum the maximum spinner value.
   * @param increment the spinner increment.
   * @return the scale field.
   */
  public ScaleField addScaleField(final IntegerProperty property, final int minimum, final int maximum,
      final int increment) {
    return addScaleField(property.getKey(), minimum, maximum, increment);
  }

  /**
   * Adds a scale field to the form section.
   * The scale field contains a slider for selecting a numeric value.
   * 
   * @param key the key of the <code>IntegerProperty</code> in the associated model.
   * @param minimum the minimum spinner value.
   * @param maximum the maximum spinner value.
   * @param increment the spinner increment.
   * @return the scale field.
   */
  public ScaleField addScaleField(final String key, final int minimum, final int maximum, final int increment) {
    String label = key;
    ScaleField scaleField = new ScaleField(getComposite(), _form, key, label, false);
    scaleField.adapt(_formToolkit);
    _form.mapField(key, scaleField);
    scaleField.setRange(minimum, maximum);
    scaleField.setIncrement(increment);
    return scaleField;
  }

  /**
   * Adds a radio group field to the form section.
   * The radio group field contains a set of option buttons, of which only one can be toggled on
   * at a time.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the radio group field.
   */
  public RadioGroupField addRadioGroupField(final Property property, final Object[] options) {
    return addRadioGroupField(property.getKey(), options);
  }

  /**
   * Adds a radio group field to the form section.
   * The radio group field contains a set of option buttons, of which only one can be toggled on
   * at a time.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the radio group field.
   */
  public RadioGroupField addRadioGroupField(final String key, final Object[] options) {
    String label = key;
    RadioGroupField groupField = new RadioGroupField(getComposite(), _form, key, label, false);
    groupField.adapt(_formToolkit);
    _form.mapField(key, groupField);
    groupField.addButtons(options);
    return groupField;
  }

  /**
   * Adds a check group field to the form section.
   * The check group field contains a set of option buttons that can be toggled on/off
   * independently of one another.
   * 
   * @param property the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the check group field.
   */
  public CheckGroupField addCheckGroupField(final Property property, final Object[] options) {
    return addCheckGroupField(property.getKey(), options);
  }

  /**
   * Adds a check group field to the form section.
   * The check group field contains a set of option buttons that can be toggled on/off
   * independently of one another.
   * 
   * @param key the key of the <code>Property</code> in the associated model.
   * @param options the array of options from which to choose.
   * @return the check group field.
   */
  public CheckGroupField addCheckGroupField(final String key, final Object[] options) {
    String label = key;
    CheckGroupField groupField = new CheckGroupField(getComposite(), _form, key, label, false);
    groupField.adapt(_formToolkit);
    _form.mapField(key, groupField);
    groupField.addButtons(options);
    return groupField;
  }

  /**
   * Adds a file field to the form section.
   * The file field has a button used to bring up a file selection dialog.
   * 
   * @param property the <code>FileProperty</code> in the associated model.
   * @param directory the default search directory.
   * @param filters the array of file search filters.
   * @return the file field.
   */
  public FileField addFileField(final FileProperty property, final String directory, final String[][] filters) {
    return addFileField(property.getKey(), directory, filters);
  }

  /**
   * Adds a file field to the form section.
   * The file field has a button used to bring up a file selection dialog.
   * 
   * @param key the key of the <code>FileProperty</code> in the associated model.
   * @param directory the default search directory.
   * @param filters the array of file search filters.
   * @return the file field.
   */
  public FileField addFileField(final String key, final String directory, final String[][] filters) {
    String label = key;
    FileField fileField = new FileField(getComposite(), _form, key, label, directory, filters, false);
    fileField.adapt(_formToolkit);
    _form.mapField(key, fileField);
    return fileField;
  }

  /**
   * Adds a directory field to the form section.
   * The directory field has a button used to bring up a directory selection dialog.
   * 
   * @param property the <code>FileProperty</code> in the associated model.
   * @param directory the default search directory.
   * @return the directory field.
   */
  public DirectoryField addDirectoryField(final FileProperty property, final String directory) {
    return addDirectoryField(property.getKey(), directory);
  }

  /**
   * Adds a directory field to the form section.
   * The directory field has a button used to bring up a directory selection dialog.
   * 
   * @param key the key of the <code>DirectoryProperty</code> in the associated model.
   * @param directory the default search directory.
   * @return the directory field.
   */
  public DirectoryField addDirectoryField(final String key, final String directory) {
    String label = key;
    DirectoryField directoryField = new DirectoryField(getComposite(), _form, key, label, directory, false);
    directoryField.adapt(_formToolkit);
    _form.mapField(key, directoryField);
    return directoryField;
  }

  public static Entity[] getFilteredEntities(final ISpecification filter) {
    return getFilteredEntities(filter, SortType.BY_NAME);
  }

  /**
   * Returns an array of entities from the repository based on the specified filter specification.
   * 
   * @param filter the specification to use to filter the entities.
   * @return the array of filtered entities.
   */
  public static Entity[] getFilteredEntities(final ISpecification filter, final SortType sortType) {
    // Get the repository.
    IRepository repository = ServiceProvider.getRepository();
    Map<String, Object> map = null;
    if (filter != null) {
      // If the filter is not null, then use it.
      map = repository.get(filter);
    } else {
      // Otherwise, get all the entities.
      map = repository.getAll();
    }
    Entity[] entities = map.values().toArray(new Entity[0]);
    if (sortType == SortType.NONE) {
      return entities;
    }

    // Sort the list of entities.
    int i, j;
    int numEntities = entities.length;
    for (i = 1; i < numEntities; i++) {
      Entity temp = entities[i];
      for (j = i; j > 0; j--) {
        String tempName = temp.getDisplayName();
        String itemName = entities[j - 1].getDisplayName();
        if (tempName.compareTo(itemName) < 0) {
          entities[j] = entities[j - 1];
        } else {
          break;
        }
      }
      entities[j] = temp;
    }
    // Return the filtered results an an entity array.
    return entities;
  }

  /**
   * Returns the font used for all comment fields.
   * If none currently exists, then a default one will be created.
   * 
   * @return the comment font.
   */
  private static Font getCommentFont() {
    if (_commentFont == null) {
      FontData fontData = new FontData("SansSerif", 10, SWT.ITALIC);
      _commentFont = new Font(Display.getCurrent(), fontData);
    }
    return _commentFont;
  }

  /**
   * Expands the form section.
   */
  public void expand() {
    getSection().setExpanded(true);
  }

  /**
   * Collapses the form section.
   */
  public void collapse() {
    getSection().setExpanded(false);
  }

  /**
   * Re-do the section's layout. Called when the visibility of fields contained
   * in the section changes.
   */
  public void redoLayout() {
    // re-do the layout of the section's composite
    Composite comp = getComposite();
    _section.redraw();
    comp.layout();

    // re-do the layout of the composite containing all the sections
    Composite parent = comp;
    while (parent != null && !(parent instanceof Form)) {
      if (parent instanceof Shell) {
        break;
      }
      parent = parent.getParent();
    }
    if (parent != null && parent instanceof Form) {
      ((Form) parent).layout();
    }
  }

}
