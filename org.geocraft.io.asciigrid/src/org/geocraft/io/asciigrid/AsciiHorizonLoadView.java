package org.geocraft.io.asciigrid;


import org.geocraft.core.model.datatypes.OnsetType;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.io.asciigrid.AsciiHorizonMapperModel.IndexType;
import org.geocraft.ui.form2.AbstractModelView;
import org.geocraft.ui.form2.FormSection;
import org.geocraft.ui.form2.IModelForm;
import org.geocraft.ui.form2.field.ComboField;
import org.geocraft.ui.form2.field.LabelField;
import org.geocraft.ui.form2.field.RadioGroupField;
import org.geocraft.ui.form2.field.SpinnerField;
import org.geocraft.ui.form2.field.TextField;


/**
 * A view of the mapper model for loading Ascii horizon files.
 */
public class AsciiHorizonLoadView extends AbstractModelView {

  /** Units array to use depending on whether the index type is depth or time */
  protected final Unit[] _depthUnits = { Unit.METER, Unit.FOOT, Unit.KILOMETER };

  protected final Unit[] _timeUnits = { Unit.MILLISECONDS, Unit.SECOND };

  /**
   * Creates a view of the Ascii Horizon mapper model for loading.
   */
  public AsciiHorizonLoadView() {
    // No action required.
  }

  //filter setter for the data units
  private void setDataUnitsFilter(Unit[] units) {
    setFieldOptions(AsciiHorizonMapperModel.DATA_UNITS, units);
  }

  @Override
  public void buildView(IModelForm form) {
    // Add the main section.
    FormSection inputSection = form.addSection("Ascii Horizon Properties", false);

    String comment = "An Ascii file for horizons is stored on disk.";
    inputSection.addCommentField(comment);

    LabelField directory = inputSection.addLabelField(AsciiHorizonMapperModel.DIRECTORY);
    directory.setTooltip("The storage directory for the Ascii Horizon file");

    LabelField fileName = inputSection.addLabelField(AsciiHorizonMapperModel.FILE_NAME);
    fileName.setTooltip("The Ascii Horizon file name");

    RadioGroupField indexType = inputSection.addRadioGroupField(AsciiHorizonMapperModel.INDEX_TYPE, IndexType.values());
    indexType.setTooltip("Index Type");

    Unit[] xyUnits = new Unit[] { Unit.FOOT, Unit.METER };
    RadioGroupField xyUnitsField = inputSection.addRadioGroupField(AsciiHorizonMapperModel.XY_UNITS, xyUnits);
    xyUnitsField.setTooltip("The unit of measurement for x,y values in the Ascii Horizon file");

    ComboField dataUnits = inputSection.addComboField(AsciiHorizonMapperModel.DATA_UNITS, _depthUnits);
    dataUnits.setTooltip("The unit of measurement for data values in the Ascii Horizon file");
    dataUnits.setTooltip("Data Units");

    ComboField onsetType = inputSection.addComboField(AsciiHorizonMapperModel.ONSET_TYPE, OnsetType.values());
    onsetType.setTooltip("The onset type of the grid (e.g. minimum, maximum, zero crossing, etc.)");

    LabelField orientation = inputSection.addLabelField(AsciiHorizonMapperModel.ORIENTATION);
    orientation.setTooltip("The grid orientation in the Ascii Horizon file");

    SpinnerField startingLineNum = inputSection
        .addSpinnerField(AsciiHorizonMapperModel.STARTING_LINE_NUM, 0, 100, 0, 1);
    startingLineNum.setTooltip("Starting line number in the ascii file");

    SpinnerField numOfHorizons = inputSection.addSpinnerField(AsciiHorizonMapperModel.NUM_OF_HORIZONS, 1, 5, 0, 1);
    numOfHorizons.setTooltip("Number of horizons to create from the ascii file");

    FormSection horizonSection = form.addSection("Horizon Name(s)", false);

    TextField horName1 = horizonSection.addTextField(AsciiHorizonMapperModel.H1_NAME);
    horName1.setTooltip("Enter the name of the horizon to create.");

    TextField horName2 = horizonSection.addTextField(AsciiHorizonMapperModel.H2_NAME);
    horName2.setTooltip("Enter the name of the horizon to create.");
    horName2.setEnabled(false);

    TextField horName3 = horizonSection.addTextField(AsciiHorizonMapperModel.H3_NAME);
    horName3.setTooltip("Enter the name of the horizon to create.");
    horName3.setEnabled(false);

    TextField horName4 = horizonSection.addTextField(AsciiHorizonMapperModel.H4_NAME);
    horName4.setTooltip("Enter the name of the horizon to create.");
    horName4.setEnabled(false);

    TextField horName5 = horizonSection.addTextField(AsciiHorizonMapperModel.H5_NAME);
    horName5.setTooltip("Enter the name of the horizon to create.");
    horName5.setEnabled(false);

    FormSection colLocationSection = form.addSection("Column Locations", false);

    SpinnerField xColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.X_COLUMN_NUM, 1, 50, 0, 1);
    xColumnNum.setTooltip("Column # in the ascii file containing X locations");

    SpinnerField yColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.Y_COLUMN_NUM, 1, 50, 0, 1);
    yColumnNum.setTooltip("Column # in the ascii file containing Y locations");

    SpinnerField h1ColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.H1_COLUMN_NUM, 1, 50, 0, 1);
    h1ColumnNum.setTooltip("Column # in the ascii file containing Horizon #1 data");

    SpinnerField h2ColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.H2_COLUMN_NUM, 1, 50, 0, 1);
    h2ColumnNum.setTooltip("Column # in the ascii file containing Horizon #2 data");
    h2ColumnNum.setEnabled(false);

    SpinnerField h3ColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.H3_COLUMN_NUM, 1, 50, 0, 1);
    h3ColumnNum.setTooltip("Column # in the ascii file containing Horizon #3 data");
    h3ColumnNum.setEnabled(false);

    SpinnerField h4ColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.H4_COLUMN_NUM, 1, 50, 0, 1);
    h4ColumnNum.setTooltip("Column # in the ascii file containing Horizon #4 data");
    h4ColumnNum.setEnabled(false);

    SpinnerField h5ColumnNum = colLocationSection.addSpinnerField(AsciiHorizonMapperModel.H5_COLUMN_NUM, 1, 50, 0, 1);
    h5ColumnNum.setTooltip("Column # in the ascii file containing Horizon #5 data");
    h5ColumnNum.setEnabled(false);

    // Build the output horizon parameters section.
    FormSection outputParmsSection = form.addSection("Horizon Parameters", false);

    TextField xOrigin = outputParmsSection.addTextField(AsciiHorizonMapperModel.X_ORIGIN);
    xOrigin.setTooltip("X Origin of the Horizon(s) to create");

    TextField yOrigin = outputParmsSection.addTextField(AsciiHorizonMapperModel.Y_ORIGIN);
    yOrigin.setTooltip("Y Origin of the Horizon(s) to create");

    TextField colSpacing = outputParmsSection.addTextField(AsciiHorizonMapperModel.COL_SPACING);
    colSpacing.setTooltip("Column spacing of the Horizon(s) to create");

    TextField rowSpacing = outputParmsSection.addTextField(AsciiHorizonMapperModel.ROW_SPACING);
    rowSpacing.setTooltip("Row spacing of the Output Horizon(s)");

    TextField numOfColumns = outputParmsSection.addTextField(AsciiHorizonMapperModel.NUM_OF_COLUMNS);
    numOfColumns.setTooltip("Number of columns of the Output Horizon(s)");

    TextField numOfRows = outputParmsSection.addTextField(AsciiHorizonMapperModel.NUM_OF_ROWS);
    numOfRows.setTooltip("Number of rows of the Output Horizon(s)");

    TextField primaryAngle = outputParmsSection.addTextField(AsciiHorizonMapperModel.PRIMARY_ANGLE);
    primaryAngle.setTooltip("Primary angle of the Output Horizon(s)");

    TextField nullValue = outputParmsSection.addTextField(AsciiHorizonMapperModel.NULL_VALUE);
    nullValue.setTooltip("Null value of the Output Horizon(s)");
  }

  @Override
  public void updateView(String key) {
    AsciiHorizonMapperModel model = (AsciiHorizonMapperModel) getModel();
    if (key.equals(AsciiHorizonMapperModel.INDEX_TYPE)) {
      IndexType indexType = model.getIndexType();
      if (indexType.equals(IndexType.DEPTH)) {
        setDataUnitsFilter(_depthUnits);
        model.setDataUnits(_depthUnits[0]);
      } else if (indexType.equals(IndexType.TIME)) {
        setDataUnitsFilter(_timeUnits);
        model.setDataUnits(_timeUnits[0]);
      }
    } else if (key.equals(AsciiHorizonMapperModel.NUM_OF_HORIZONS)) {
      int numOfHorizons = model.getNumOfHorizons();
      setFieldEnabled(AsciiHorizonMapperModel.H2_NAME, numOfHorizons > 1);
      setFieldEnabled(AsciiHorizonMapperModel.H2_COLUMN_NUM, numOfHorizons > 1);
      setFieldEnabled(AsciiHorizonMapperModel.H3_NAME, numOfHorizons > 2);
      setFieldEnabled(AsciiHorizonMapperModel.H3_COLUMN_NUM, numOfHorizons > 2);
      setFieldEnabled(AsciiHorizonMapperModel.H4_NAME, numOfHorizons > 3);
      setFieldEnabled(AsciiHorizonMapperModel.H4_COLUMN_NUM, numOfHorizons > 3);
      setFieldEnabled(AsciiHorizonMapperModel.H5_NAME, numOfHorizons > 4);
      setFieldEnabled(AsciiHorizonMapperModel.H5_COLUMN_NUM, numOfHorizons > 4);
    }
  }

}
