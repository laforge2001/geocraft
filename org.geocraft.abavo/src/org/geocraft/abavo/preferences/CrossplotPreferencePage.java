package org.geocraft.abavo.preferences;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.internal.abavo.PreferencesConstants;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.util.PlotUtil;


/**
 * The page for setting preferences for the ABAVO crossplot related to the general display.
 * This includes items such as background color, grid lines, fonts, etc.
 */
public class CrossplotPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage {

  public static PropertyStore CROSSPLOT_PREFERENCE_STORE = PropertyStoreFactory.getStore(CrossplotPreferencePage.ID);

  /** The preference page ID. */
  public static final String ID = "org.geocraft.abavo.crossplot1";

  /** The available options for the axis line styles of the crossplot. */
  private static final String[][] AXIS_LINE_STYLES = new String[][] { { "Solid", LineStyle.SOLID.getName() },
      { "Dashed", LineStyle.DASHED.getName() }, { "None", LineStyle.NONE.getName() } };

  static {
    setDefaults();
  }

  public CrossplotPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Page 1 of preferences for ABAVO crossplot properties (fonts, colors, etc).");
  }

  /**
   * Sets the default values for the crossplot preferences.
   */
  public static void setDefaults() {
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.PLOT_BACKGROUND_COLOR, PlotUtil.RGB_LIGHT_GRAY);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.MODEL_BACKGROUND_COLOR, PlotUtil.RGB_BLACK);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_LINE_WIDTH, 1);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_LINE_STYLE, LineStyle.DASHED.toString());
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_LINE_COLOR, PlotUtil.RGB_BLACK);
    Font systemFont = PlatformUI.getWorkbench().getDisplay().getSystemFont();
    FontData[] fontData = systemFont.getFontData();
    String font = fontData[0].toString();
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.PLOT_TITLE_TEXT_FONT, font);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.PLOT_TITLE_TEXT_COLOR, PlotUtil.RGB_BLACK);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_LABEL_TEXT_FONT, font);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_LABEL_TEXT_COLOR, PlotUtil.RGB_BLACK);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_RANGE_TEXT_FONT, font);
    PreferencesUtil.getService(ID).setDefault(PreferencesConstants.AXIS_RANGE_TEXT_COLOR, PlotUtil.RGB_BLACK);
  }

  /**
   * Creates the field editors. Field editors are abstractions of
   * the common GUI blocks needed to manipulate various types
   * of preferences. Each field editor knows how to save and
   * restore itself.
   */
  @Override
  public void createFieldEditors() {

    ColorFieldEditor plotBackgroundColor = new ColorFieldEditor(PreferencesConstants.PLOT_BACKGROUND_COLOR,
        "Plot Background Color:", getFieldEditorParent());
    addField(plotBackgroundColor);

    ColorFieldEditor modelBackgroundColor = new ColorFieldEditor(PreferencesConstants.MODEL_BACKGROUND_COLOR,
        "Model Background Color:", getFieldEditorParent());
    addField(modelBackgroundColor);

    IntegerFieldEditor axisLineWidth = new IntegerFieldEditor(PreferencesConstants.AXIS_LINE_WIDTH, "Axis Line Width:",
        getFieldEditorParent());

    axisLineWidth.getPreferenceStore();
    axisLineWidth.setValidRange(1, 10);
    addField(axisLineWidth);

    ComboFieldEditor axisLineStyle = new ComboFieldEditor(PreferencesConstants.AXIS_LINE_STYLE, "Axis Line Style:",
        AXIS_LINE_STYLES, getFieldEditorParent());
    addField(axisLineStyle);

    ColorFieldEditor axisLineColor = new ColorFieldEditor(PreferencesConstants.AXIS_LINE_COLOR, "Axis Line Color:",
        getFieldEditorParent());
    addField(axisLineColor);

    FontFieldEditor titleLabelFont = new FontFieldEditor(PreferencesConstants.PLOT_TITLE_TEXT_FONT, "Plot Title Font:",
        "Plot Title", getFieldEditorParent());
    addField(titleLabelFont);

    ColorFieldEditor titleLabelColor = new ColorFieldEditor(PreferencesConstants.PLOT_TITLE_TEXT_COLOR,
        "Plot Title Color:", getFieldEditorParent());
    addField(titleLabelColor);

    FontFieldEditor axisLabelFont = new FontFieldEditor(PreferencesConstants.AXIS_LABEL_TEXT_FONT, "Axis Label Font:",
        "Axis Label", getFieldEditorParent());
    addField(axisLabelFont);

    ColorFieldEditor axisLabelColor = new ColorFieldEditor(PreferencesConstants.AXIS_LABEL_TEXT_COLOR,
        "Axis Label Color:", getFieldEditorParent());
    addField(axisLabelColor);

    FontFieldEditor axisRangeFont = new FontFieldEditor(PreferencesConstants.AXIS_RANGE_TEXT_FONT, "Axis Range Font:",
        "Axis Range", getFieldEditorParent());
    addField(axisRangeFont);

    ColorFieldEditor axisRangeColor = new ColorFieldEditor(PreferencesConstants.AXIS_RANGE_TEXT_COLOR,
        "Axis Range Color:", getFieldEditorParent());
    addField(axisRangeColor);

    // Set the preference defaults.
    setDefaults();

    // Update the editor widgets from the preferences.
    plotBackgroundColor.load();
    modelBackgroundColor.load();
    axisLineWidth.load();
    axisLineStyle.load();
    axisLineColor.load();
    titleLabelFont.load();
    titleLabelColor.load();
    axisLabelFont.load();
    axisLabelColor.load();
    axisRangeFont.load();
    axisRangeColor.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = CROSSPLOT_PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    RGB plotBackgroundColor = PreferenceConverter.getColor(localStore, PreferencesConstants.PLOT_BACKGROUND_COLOR);
    prefState.put(PreferencesConstants.PLOT_BACKGROUND_COLOR, plotBackgroundColor.toString());

    RGB modelBackgroundColor = PreferenceConverter.getColor(localStore, PreferencesConstants.MODEL_BACKGROUND_COLOR);
    prefState.put(PreferencesConstants.MODEL_BACKGROUND_COLOR, modelBackgroundColor.toString());

    int axisLineWidth = localStore.getInt(PreferencesConstants.AXIS_LINE_WIDTH);
    prefState.put(PreferencesConstants.AXIS_LINE_WIDTH, Integer.toString(axisLineWidth));

    String axisLineStyle = localStore.getString(PreferencesConstants.AXIS_LINE_STYLE);
    prefState.put(PreferencesConstants.AXIS_LINE_STYLE, axisLineStyle);

    RGB axisLineColor = PreferenceConverter.getColor(localStore, PreferencesConstants.AXIS_LINE_COLOR);
    prefState.put(PreferencesConstants.AXIS_LINE_COLOR, axisLineColor.toString());

    String plotTitleFont = localStore.getString(PreferencesConstants.PLOT_TITLE_TEXT_FONT);
    prefState.put(PreferencesConstants.PLOT_TITLE_TEXT_FONT, plotTitleFont);

    RGB plotTitleColor = PreferenceConverter.getColor(localStore, PreferencesConstants.PLOT_TITLE_TEXT_COLOR);
    prefState.put(PreferencesConstants.PLOT_TITLE_TEXT_COLOR, plotTitleColor.toString());

    String axisLabelFont = localStore.getString(PreferencesConstants.AXIS_LABEL_TEXT_FONT);
    prefState.put(PreferencesConstants.AXIS_LABEL_TEXT_FONT, axisLabelFont);

    RGB axisLabelColor = PreferenceConverter.getColor(localStore, PreferencesConstants.AXIS_LABEL_TEXT_COLOR);
    prefState.put(PreferencesConstants.AXIS_LABEL_TEXT_COLOR, axisLabelColor.toString());

    String axisRangeFont = localStore.getString(PreferencesConstants.AXIS_RANGE_TEXT_FONT);
    prefState.put(PreferencesConstants.AXIS_RANGE_TEXT_FONT, axisRangeFont);

    RGB axisRangeColor = PreferenceConverter.getColor(localStore, PreferencesConstants.AXIS_RANGE_TEXT_COLOR);
    prefState.put(PreferencesConstants.AXIS_RANGE_TEXT_COLOR, axisRangeColor.toString());

    return prefState;
  }

  public void setPreferenceState(Map<String, String> prefs) {
    IPreferenceStore localStore = CROSSPLOT_PREFERENCE_STORE.getLocalStore();

    CROSSPLOT_PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs
        .get(PropertyStore.USE_PROJECT_SETTINGS));

    RGB plotBackgroundColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.PLOT_BACKGROUND_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.PLOT_BACKGROUND_COLOR, plotBackgroundColor);

    RGB modelBackgroundColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.MODEL_BACKGROUND_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.MODEL_BACKGROUND_COLOR, modelBackgroundColor);

    try {
      int axisLineWidth = Integer.parseInt(prefs.get(PreferencesConstants.AXIS_LINE_WIDTH));
      localStore.setValue(PreferencesConstants.AXIS_LINE_WIDTH, axisLineWidth);
    } catch (NumberFormatException nfe) {
      //leave value as default
    }

    localStore.setValue(PreferencesConstants.AXIS_LINE_STYLE, prefs.get(PreferencesConstants.AXIS_LINE_STYLE));

    RGB axisLineColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.AXIS_LINE_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.AXIS_LINE_COLOR, axisLineColor);

    String plotTitleFont = prefs.get(PreferencesConstants.PLOT_TITLE_TEXT_FONT);
    PreferenceConverter.setValue(localStore, PreferencesConstants.PLOT_TITLE_TEXT_FONT, PreferenceConverter
        .readFontData(plotTitleFont));

    RGB plotTitleColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.PLOT_TITLE_TEXT_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.PLOT_TITLE_TEXT_COLOR, plotTitleColor);

    String axisLabelFont = prefs.get(PreferencesConstants.AXIS_LABEL_TEXT_FONT);
    PreferenceConverter.setValue(localStore, PreferencesConstants.AXIS_LABEL_TEXT_FONT, PreferenceConverter
        .readFontData(axisLabelFont));

    RGB axisLabelColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.AXIS_LABEL_TEXT_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.AXIS_LABEL_TEXT_COLOR, axisLabelColor);

    String axisRangeFont = prefs.get(PreferencesConstants.AXIS_RANGE_TEXT_FONT);
    PreferenceConverter.setValue(localStore, PreferencesConstants.AXIS_RANGE_TEXT_FONT, PreferenceConverter
        .readFontData(axisRangeFont));

    RGB axisRangeColor = PropertyStore.rgbValue(prefs.get(PreferencesConstants.AXIS_RANGE_TEXT_COLOR));
    PreferenceConverter.setValue(localStore, PreferencesConstants.AXIS_RANGE_TEXT_COLOR, axisRangeColor);

  }
}
