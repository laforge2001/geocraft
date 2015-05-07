package org.geocraft.ui.mapviewer.renderer.well;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.geocraft.core.common.preferences.FieldEditorOverlayPage;
import org.geocraft.core.common.preferences.GeocraftPreferenceService;
import org.geocraft.core.common.preferences.PreferencesUtil;
import org.geocraft.core.common.preferences.PropertyStore;
import org.geocraft.core.common.preferences.PropertyStoreFactory;
import org.geocraft.core.preferences.IGeocraftPreferencePage;
import org.geocraft.ui.plot.defs.LineStyle;
import org.geocraft.ui.plot.defs.PointStyle;


public class WellRendererPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage,
    IGeocraftPreferencePage, IWellRendererConstants {

  public static final String ID = "org.geocraft.ui.mapviewer.well";

  public static PropertyStore PREFERENCE_STORE = PropertyStoreFactory.getStore(WellRendererPreferencePage.ID);

  static {
    setDefaults();
  }

  public WellRendererPreferencePage() {
    super(GRID);
    setDefaults();
  }

  @Override
  protected String getPageId() {
    return ID;
  }

  public void init(final IWorkbench workbench) {
    setDescription("Preferences for well plotting in the map viewer.");
  }

  public static void setDefaults() {
    GeocraftPreferenceService service = PreferencesUtil.getService(ID);

    // Initialize the well preferences.
    service.setDefault(BORE_LINE_STYLE, LineStyle.SOLID.getName());
    service.setDefault(BORE_LINE_WIDTH, 2);
    service.setDefault(BORE_LINE_COLOR, new RGB(0, 255, 0));
    service.setDefault(TOP_HOLE_SYMBOL, PointStyle.DIAMOND.getName());
    service.setDefault(TOP_HOLE_SYMBOL_SIZE, 4);
    service.setDefault(TOP_HOLE_SYMBOL_COLOR, new RGB(255, 255, 0));
    service.setDefault(BOTTOM_HOLE_SYMBOL, PointStyle.FILLED_CIRCLE.getName());
    service.setDefault(BOTTOM_HOLE_SYMBOL_SIZE, 4);
    service.setDefault(BOTTOM_HOLE_SYMBOL_COLOR, new RGB(255, 255, 0));
  }

  @Override
  protected void createFieldEditors() {
    Composite parent = getFieldEditorParent();

    Group wellBoreGroup = createGroup(parent, "Well Bore");

    LineStyle[] lineStyles = LineStyle.values();
    String[][] lineStyleOptions = new String[lineStyles.length][2];
    for (int i = 0; i < lineStyles.length; i++) {
      lineStyleOptions[i][0] = lineStyles[i].getName();
      lineStyleOptions[i][1] = lineStyles[i].getName();
    }

    PointStyle[] pointStyles = PointStyle.values();
    String[][] pointStyleOptions = new String[pointStyles.length][2];
    for (int i = 0; i < pointStyles.length; i++) {
      pointStyleOptions[i][0] = pointStyles[i].getName();
      pointStyleOptions[i][1] = pointStyles[i].getName();
    }

    ComboFieldEditor boreLineStyleField = new ComboFieldEditor(BORE_LINE_STYLE, BORE_LINE_STYLE, lineStyleOptions,
        wellBoreGroup);
    addField(boreLineStyleField);

    IntegerFieldEditor boreLineWidthField = new IntegerFieldEditor(BORE_LINE_WIDTH, BORE_LINE_WIDTH, wellBoreGroup);
    addField(boreLineWidthField);

    ColorFieldEditor boreLineColorField = new ColorFieldEditor(BORE_LINE_COLOR, BORE_LINE_COLOR, wellBoreGroup);
    addField(boreLineColorField);

    ComboFieldEditor topSymbolField = new ComboFieldEditor(TOP_HOLE_SYMBOL, TOP_HOLE_SYMBOL, pointStyleOptions,
        wellBoreGroup);
    addField(topSymbolField);

    IntegerFieldEditor topSymbolSizeField = new IntegerFieldEditor(TOP_HOLE_SYMBOL_SIZE, TOP_HOLE_SYMBOL_SIZE,
        wellBoreGroup);
    addField(topSymbolSizeField);

    ColorFieldEditor topSymbolColorField = new ColorFieldEditor(TOP_HOLE_SYMBOL_COLOR, TOP_HOLE_SYMBOL_COLOR,
        wellBoreGroup);
    addField(topSymbolColorField);

    ComboFieldEditor bottomSymbolField = new ComboFieldEditor(BOTTOM_HOLE_SYMBOL, BOTTOM_HOLE_SYMBOL,
        pointStyleOptions, wellBoreGroup);
    addField(bottomSymbolField);

    IntegerFieldEditor bottomSymbolSizeField = new IntegerFieldEditor(BOTTOM_HOLE_SYMBOL_SIZE, BOTTOM_HOLE_SYMBOL_SIZE,
        wellBoreGroup);
    addField(bottomSymbolSizeField);

    ColorFieldEditor bottomSymbolColorField = new ColorFieldEditor(BOTTOM_HOLE_SYMBOL_COLOR, BOTTOM_HOLE_SYMBOL_COLOR,
        wellBoreGroup);
    addField(bottomSymbolColorField);

    setDefaults();

    boreLineStyleField.load();
    boreLineWidthField.load();
    boreLineColorField.load();
    topSymbolField.load();
    topSymbolSizeField.load();
    topSymbolColorField.load();
    bottomSymbolField.load();
    bottomSymbolSizeField.load();
    bottomSymbolColorField.load();
  }

  public Map<String, String> getPreferenceState() {
    HashMap<String, String> prefState = new HashMap<String, String>();
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    String useLocalPrefs = localStore.getString(PropertyStore.USE_PROJECT_SETTINGS);
    prefState.put(PropertyStore.USE_PROJECT_SETTINGS, useLocalPrefs);

    String boreLineStyle = localStore.getString(BORE_LINE_STYLE);
    prefState.put(BORE_LINE_STYLE, boreLineStyle);

    int boreLineWidth = localStore.getInt(BORE_LINE_WIDTH);
    prefState.put(BORE_LINE_WIDTH, Integer.toString(boreLineWidth));

    RGB boreLineColor = PreferenceConverter.getColor(localStore, BORE_LINE_COLOR);
    prefState.put(BORE_LINE_COLOR, boreLineColor.toString());

    String topHoleSymbol = localStore.getString(TOP_HOLE_SYMBOL);
    prefState.put(TOP_HOLE_SYMBOL, topHoleSymbol);

    int topHoleSymbolSize = localStore.getInt(TOP_HOLE_SYMBOL_SIZE);
    prefState.put(TOP_HOLE_SYMBOL_SIZE, Integer.toString(topHoleSymbolSize));

    RGB topHoleSymbolColor = PreferenceConverter.getColor(localStore, TOP_HOLE_SYMBOL_COLOR);
    prefState.put(TOP_HOLE_SYMBOL_COLOR, topHoleSymbolColor.toString());

    String bottomHoleSymbol = localStore.getString(BOTTOM_HOLE_SYMBOL);
    prefState.put(BOTTOM_HOLE_SYMBOL, bottomHoleSymbol);

    int bottomHoleSSymbolSize = localStore.getInt(BOTTOM_HOLE_SYMBOL_SIZE);
    prefState.put(BOTTOM_HOLE_SYMBOL_SIZE, Integer.toString(bottomHoleSSymbolSize));

    RGB bottomHoleSymbolColor = PreferenceConverter.getColor(localStore, BOTTOM_HOLE_SYMBOL_COLOR);
    prefState.put(BOTTOM_HOLE_SYMBOL_COLOR, bottomHoleSymbolColor.toString());

    return prefState;
  }

  public void setPreferenceState(final Map<String, String> prefs) {
    IPreferenceStore localStore = PREFERENCE_STORE.getLocalStore();

    try {
      PREFERENCE_STORE.setValue(PropertyStore.USE_PROJECT_SETTINGS, prefs.get(PropertyStore.USE_PROJECT_SETTINGS));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      localStore.setValue(BORE_LINE_STYLE, prefs.get(BORE_LINE_STYLE));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int lineLineWidth = Integer.parseInt(prefs.get(BORE_LINE_WIDTH));
      localStore.setValue(BORE_LINE_WIDTH, lineLineWidth);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      RGB boreLineColor = PropertyStore.rgbValue(prefs.get(BORE_LINE_COLOR));
      PreferenceConverter.setValue(localStore, BORE_LINE_COLOR, boreLineColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      localStore.setValue(TOP_HOLE_SYMBOL, prefs.get(TOP_HOLE_SYMBOL));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int topHoleSymbolSize = Integer.parseInt(prefs.get(TOP_HOLE_SYMBOL_SIZE));
      localStore.setValue(TOP_HOLE_SYMBOL_SIZE, topHoleSymbolSize);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      RGB topHoleSymbolColor = PropertyStore.rgbValue(prefs.get(TOP_HOLE_SYMBOL_COLOR));
      PreferenceConverter.setValue(localStore, TOP_HOLE_SYMBOL_COLOR, topHoleSymbolColor);
    } catch (Exception ex) {
      // Leave value as default.
    }

    try {
      localStore.setValue(BOTTOM_HOLE_SYMBOL, prefs.get(BOTTOM_HOLE_SYMBOL));
    } catch (Exception ex) {
      // Leave as default.
    }

    try {
      int bottomHoleSymbolSize = Integer.parseInt(prefs.get(BOTTOM_HOLE_SYMBOL_SIZE));
      localStore.setValue(BOTTOM_HOLE_SYMBOL_SIZE, bottomHoleSymbolSize);
    } catch (Exception nfe) {
      // Leave as default.
    }

    try {
      RGB bottomHoleSymbolColor = PropertyStore.rgbValue(prefs.get(BOTTOM_HOLE_SYMBOL_COLOR));
      PreferenceConverter.setValue(localStore, BOTTOM_HOLE_SYMBOL_COLOR, bottomHoleSymbolColor);
    } catch (Exception ex) {
      // Leave value as default.
    }
  }

  /**
   * Create a buttons group.
   * 
   * @param parent composite
   * @param title the group title
   */
  protected Group createGroup(final Composite parent, final String title) {
    Group buttonGroup = new Group(parent, SWT.NONE);
    buttonGroup.setText(title);
    GridLayout layout = new GridLayout();
    buttonGroup.setLayout(layout);
    buttonGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    return buttonGroup;
  }
}
