package org.geocraft.algorithm.example;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.geocraft.algorithm.StandaloneAlgorithm;
import org.geocraft.core.model.grid.Grid3d;
import org.geocraft.core.model.property.BooleanProperty;
import org.geocraft.core.model.property.ColorProperty;
import org.geocraft.core.model.property.EntityProperty;
import org.geocraft.core.model.property.EnumArrayProperty;
import org.geocraft.core.model.property.EnumProperty;
import org.geocraft.core.model.property.FileProperty;
import org.geocraft.core.model.property.FloatProperty;
import org.geocraft.core.model.property.FontProperty;
import org.geocraft.core.model.property.IntegerProperty;
import org.geocraft.core.model.property.ObjectArrayProperty;
import org.geocraft.core.model.property.StringArrayProperty;
import org.geocraft.core.model.property.StringProperty;
import org.geocraft.core.model.seismic.PostStack3d;
import org.geocraft.core.model.validation.IValidation;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.logging.ILogger;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;


public class FormWidgets extends StandaloneAlgorithm {

  private static final String FILE_PROPERTY = "File Property";

  private static final String DIRECTORY_PROPERTY = "Directory Property";

  private static final String BOOLEAN_PROPERTY = "Boolean Property";

  private static final String COLOR_PROPERTY = "Color Property";

  private static final String FONT_PROPERTY = "Font Property";

  private static final String GRID_PROPERTY = "Grid Property";

  private static final String POSTSTACK_PROPERTY = "PostStack Property";

  //private static final String GRIDS_PROPERTY = "Grids Property";

  //private static final String POSTSTACKS_PROPERTY = "PostStacks Property";

  private static final String STRING_ARRAY_PROPERTY = "String Array Property";

  private static final String ENUM_ARRAY_PROPERTY = "Enum Array Property";

  private static final String GRID_ARRAY_PROPERTY = "Grid Array Property";

  private static final String STRING_PROPERTY = "String Property";

  private static final String INTEGER_PROPERTY = "Integer Property";

  private static final String FLOAT_PROPERTY = "Float Property";

  private static final String RADIO_ENUM_PROPERTY = "Radio Enum Property";

  //private static final String CHECK_ENUM_PROPERTY = "Check Enumerated Property";

  //private static final String CHECK_STRING_PROPERTY = "Check String Property";

  private static enum Fruit {
    Apples,
    Oranges,
    Bananas;
  }

  private FileProperty _fileProperty;

  private FileProperty _directoryProperty;

  private BooleanProperty _booleanProperty;

  private ColorProperty _colorProperty;

  private FontProperty _fontProperty;

  private EntityProperty<Grid3d> _gridProperty;

  private EntityProperty<PostStack3d> _postStackProperty;

  private StringArrayProperty _stringArrayProperty;

  private EnumArrayProperty<Fruit> _enumArrayProperty;

  private ObjectArrayProperty<Grid3d> _gridArrayProperty;

  private StringProperty _stringProperty;

  private IntegerProperty _integerProperty;

  private FloatProperty _floatProperty;

  private EnumProperty<Fruit> _radioEnumProperty;

  private List<Fruit> _checkEnums;

  //private List<String> _checkStrings;

  public FormWidgets() {
    super();
    _fileProperty = addFileProperty(FILE_PROPERTY);
    _directoryProperty = addFileProperty(DIRECTORY_PROPERTY);
    _booleanProperty = addBooleanProperty(BOOLEAN_PROPERTY, false);
    _colorProperty = addColorProperty(COLOR_PROPERTY, new RGB(255, 0, 255));
    _fontProperty = addFontProperty(FONT_PROPERTY, null);
    _gridProperty = addEntityProperty(GRID_PROPERTY, Grid3d.class);
    _postStackProperty = addEntityProperty(POSTSTACK_PROPERTY, PostStack3d.class);
    _stringArrayProperty = addStringArrayProperty(STRING_ARRAY_PROPERTY);
    _enumArrayProperty = addEnumArrayProperty(ENUM_ARRAY_PROPERTY, Fruit.class);
    _gridArrayProperty = addEntityArrayProperty(GRID_ARRAY_PROPERTY, Grid3d.class);
    _stringProperty = addStringProperty(STRING_PROPERTY, "");
    _integerProperty = addIntegerProperty(INTEGER_PROPERTY, 0);
    _floatProperty = addFloatProperty(FLOAT_PROPERTY, Float.NaN);
    _radioEnumProperty = addEnumProperty(RADIO_ENUM_PROPERTY, Fruit.class, Fruit.Oranges);
    _checkEnums = new ArrayList<Fruit>();
    _checkEnums.add(Fruit.Bananas);
    //_checkStrings = new ArrayList<String>();
  }

  @Override
  public void buildView(final IModelForm form) {
    FormSection fileSection = form.addSection("File Parameters");

    String[][] filters = new String[1][2];
    filters[0][0] = "All Files";
    filters[0][1] = "*.*";
    fileSection.addFileField(FILE_PROPERTY, "/home/walucas/data", filters);

    fileSection.addDirectoryField(DIRECTORY_PROPERTY, null);

    FormSection booleanSection = form.addSection("Boolean Parameters");
    booleanSection.addCheckboxField(BOOLEAN_PROPERTY);

    FormSection colorSection = form.addSection("Color Parameters");
    colorSection.addColorField(COLOR_PROPERTY);

    FormSection fontSection = form.addSection("Font Parameters");
    fontSection.addFontField(FONT_PROPERTY);

    FormSection entitySection = form.addSection("Entity Parameters");
    entitySection.addEntityComboField(GRID_PROPERTY, Grid3d.class);
    entitySection.addEntityComboField(POSTSTACK_PROPERTY, new TypeSpecification(PostStack3d.class));
    //entitySection.addEntityListField(GRIDS_PROPERTY, Grid.class);
    //entitySection.addEntityListField(POSTSTACKS_PROPERTY, new TypeSpecification(PostStack3d.class));

    //form.addComboField(entitySection, "String Combo", new String[] { "Apples", "Oranges", "Bananas" });

    //form.addComboField(entitySection, "Enum Combo", Fruit.values());

    //form.addEnhancedComboField(entitySection, "Enhanced Combo", new String[] { "Apples", "Oranges" }, new String[] {
    //    "Apples", "Oranges", "Bananas", "Pineapples" });

    FormSection listSection = form.addSection("List Parameters");
    listSection.addOrderedListField(STRING_ARRAY_PROPERTY, new String[] { "Apples", "Oranges" });
    listSection.addOrderedListField(ENUM_ARRAY_PROPERTY, Fruit.values());
    listSection.addEntityListField(GRID_ARRAY_PROPERTY, Grid3d.class);

    FormSection alphanumericSection = form.addSection("Strings and Numbers");
    alphanumericSection.addTextField(STRING_PROPERTY);
    alphanumericSection.addTextField(INTEGER_PROPERTY);
    alphanumericSection.addTextField(FLOAT_PROPERTY);
    alphanumericSection.addRadioGroupField(RADIO_ENUM_PROPERTY, Fruit.values()).setEnabled(false);
    //alphanumericSection.addCheckGroupField(CHECK_ENUM_PROPERTY, Fruit.values());
    String[] options = new String[3];
    options[0] = Fruit.Apples.toString();
    options[1] = Fruit.Oranges.toString();
    options[2] = Fruit.Bananas.toString();
    //alphanumericSection.addCheckGroupField(CHECK_STRING_PROPERTY, options);

  }

  @Override
  public void run(final IProgressMonitor monitor, final ILogger logger, final IRepository repository) {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        if (shell == null) {
          shell = Display.getCurrent().getActiveShell();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(DIRECTORY_PROPERTY + ": " + _directoryProperty + "\n");
        builder.append(FILE_PROPERTY + ": " + _fileProperty + "\n");
        builder.append(BOOLEAN_PROPERTY + ": " + _booleanProperty + "\n");
        builder.append(COLOR_PROPERTY + ": " + _colorProperty + "\n");
        builder.append(FONT_PROPERTY + ": " + _fontProperty + "\n");
        builder.append(GRID_PROPERTY + ": " + _gridProperty + "\n");
        builder.append(POSTSTACK_PROPERTY + ": " + _postStackProperty + "\n");
        //builder.append(GRIDS_PROPERTY + ": " + _grids + "\n");
        //builder.append(POSTSTACKS_PROPERTY + ": " + _poststacks + "\n");
        builder.append(STRING_ARRAY_PROPERTY + ": " + _stringArrayProperty + "\n");
        builder.append(ENUM_ARRAY_PROPERTY + ": " + _enumArrayProperty + "\n");
        builder.append(GRID_ARRAY_PROPERTY + ": " + _gridArrayProperty + "\n");
        builder.append(STRING_PROPERTY + ": " + _stringProperty + "\n");
        builder.append(INTEGER_PROPERTY + ": " + _integerProperty + "\n");
        builder.append(FLOAT_PROPERTY + ": " + _floatProperty + "\n");
        builder.append(RADIO_ENUM_PROPERTY + ": " + _radioEnumProperty + "\n");
        //builder.append(CHECK_ENUM_PROPERTY + ": " + _checkEnums.toString() + "\n");
        //builder.append(CHECK_STRING_PROPERTY + ": " + _checkStrings.toString() + "\n");
        MessageDialog.openInformation(shell, "Form Widget Parameters", builder.toString());
      }
    });
  }

  public void validate(final IValidation results) {
    if (_fileProperty.isNull()) {
      results.error(FILE_PROPERTY, "No file specified");
    } else if (!_fileProperty.canRead()) {
      results.error(FILE_PROPERTY, "Cannot read the file");
    }

    if (_directoryProperty.isNull()) {
      results.error(DIRECTORY_PROPERTY, "Not directory specified");
    } else {
      if (!_directoryProperty.canRead()) {
        results.error(DIRECTORY_PROPERTY, "Cannot read from the directory");
      }
      if (!_directoryProperty.canWrite()) {
        results.error(DIRECTORY_PROPERTY, "Cannot write to the directory");
      }
    }

    if (_colorProperty.isNull()) {
      results.error(COLOR_PROPERTY, "No color specified");
    }

    if (_fontProperty.isEmpty()) {
      results.error(FONT_PROPERTY, "No font specified");
    }

    if (_gridProperty.isNull()) {
      results.warning(GRID_PROPERTY, "Grid should be specified");
    }

    if (_postStackProperty.isNull()) {
      results.warning(POSTSTACK_PROPERTY, "PostStack should be specified");
    }

    // if (_grids.isNull() || _grids.length == 0) {
    //   results.warning(GRIDS_PROPERTY, "Grids should be specified");
    //}

    //if (_poststacks.isNull() || _poststacks.length == 0) {
    //   results.warning(POSTSTACKS_PROPERTY, "PostStacks should be specified");
    //}

    if (_stringProperty.isEmpty()) {
      results.error(STRING_PROPERTY, "String must be specified");
    }

    if (Float.isNaN(_floatProperty.get())) {
      results.error(FLOAT_PROPERTY, "Float cannot be NaN");
    }

    if (Float.isInfinite(_floatProperty.get())) {
      results.error(FLOAT_PROPERTY, "Float cannot be infinite");
    }

    if (_radioEnumProperty.isNull()) {
      results.error(RADIO_ENUM_PROPERTY, "Radio enum must be specified");
    }

    //    if (_checkEnums.isNull() || _checkEnums.size() == 0) {
    //      results.warning(CHECK_ENUM_PROPERTY, "Check enums are not specified");
    //    }
    //
    //    if (_checkStrings.isNull() || _checkStrings.size() == 0) {
    //      results.warning(CHECK_STRING_PROPERTY, "Check strings are not specified");
    //    }
  }

  public void propertyChanged(String key) {
    // TODO Auto-generated method stub

  }
}
