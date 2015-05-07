/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.sectionviewer.navigation;


import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.geocraft.core.model.datatypes.Domain;
import org.geocraft.core.model.datatypes.TraceAxisKey;
import org.geocraft.core.model.datatypes.Unit;
import org.geocraft.core.model.preferences.UnitPreferences;
import org.geocraft.core.model.seismic.PostStack2d;
import org.geocraft.core.model.seismic.PostStack2dLine;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.TraceSection;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.sectionviewer.ISectionViewer;
import org.geocraft.ui.sectionviewer.factory.PostStack2dSectionFactory;


public class PostStack2dNavigationDialog extends AbstractNavigationDialog {

  /** The poststack volume from which to get the inline,xline ranges. */
  private final PostStack2d _poststack;

  /** The section viewer to update with the selected sections. */
  private final ISectionViewer _viewer;

  /** The text for specifying traces-per-inch. */
  private Text _tpiText;

  /** The text for specifying inches-per-second. */
  private Text _ipsText;

  /** The Combo for selecting the desired inline. */
  private Combo _inlineCombo;

  public PostStack2dNavigationDialog(final Shell shell, final PostStack2d poststackCollection, final ISectionViewer viewer) {
    super(shell, "PostStack2d Navigation: " + poststackCollection.getDisplayName());
    _poststack = poststackCollection;
    _viewer = viewer;
  }

  @Override
  protected void createPanel(final Composite parent) {
    parent.setLayout(GridLayoutHelper.createLayout(1, false));

    float horizontalScale = _viewer.getHorizontalDisplayScale();
    float verticalScale = _viewer.getVerticalDisplayScale();
    Group resolutionGroup = createGroup(parent, "Resolution");
    _tpiText = addLabeledText(resolutionGroup, "Traces/Inch", horizontalScale);
    Domain zDomain = _poststack.getZDomain();
    if (zDomain.equals(Domain.TIME)) {
      _ipsText = addLabeledText(resolutionGroup, "Inches/Second", verticalScale);
    } else if (zDomain.equals(Domain.DISTANCE)) {
      Unit zUnit = UnitPreferences.getInstance().getVerticalDistanceUnit();
      if (zUnit.equals(Unit.FOOT)) {
        _ipsText = addLabeledText(resolutionGroup, "Inches/Kilofoot", verticalScale);
      } else {
        _ipsText = addLabeledText(resolutionGroup, "Inches/Kilometer", verticalScale);
      }
    }
    Listener resolutionListener = new Listener() {

      public void handleEvent(final Event e) {
        if (e.keyCode == SWT.CR || e.keyCode == NUMERIC_ENTER) {
          String tpiStr = _tpiText.getText().trim();
          String ipsStr = _ipsText.getText().trim();
          float tpiScale = 100f;
          float ipsScale = 0.001f;
          StringBuilder errors = new StringBuilder("");
          try {
            tpiScale = Float.parseFloat(tpiStr);
            if (tpiScale < 1) {
              errors.append("Horizontal scale cannot be less than 1");
            }
          } catch (NumberFormatException e1) {
            errors.append("Invalid horizontal scale: " + tpiStr + "\n");
          }
          try {
            ipsScale = Float.parseFloat(ipsStr);
            if (ipsScale > MAX_VERTICAL_SCALE) {
              errors.append("Vertical scale cannot be greater than " + MAX_VERTICAL_SCALE);
            }
          } catch (NumberFormatException e1) {
            errors.append("Invalid vertical scale: " + ipsStr + "\n");
          }
          if (errors.length() == 0) {
            updateScales(tpiScale, ipsScale);
          } else {
            MessageDialog.openError(getShell(), "Scale Error", errors.toString());
          }
        }
      }

    };
    _tpiText.addListener(SWT.KeyDown, resolutionListener);
    _ipsText.addListener(SWT.KeyDown, resolutionListener);

    // Create the inline group composite.
    Group inlineGroup = createGroup(parent, "Line");
    _inlineCombo = createCombo(inlineGroup, _poststack.getLineNames(true));

    // Add listener for the inline Combo.
    _inlineCombo.addListener(SWT.Selection, new Listener() {

      public void handleEvent(final Event event) {
        int index = _inlineCombo.getSelectionIndex();
        String lineName = _inlineCombo.getItem(index);
        SeismicLine2d seismicLine = _poststack.getSurvey().getLineByName(lineName);
        int lineNo = seismicLine.getNumber();
        int xlineDecimation = 1;
        PostStack2dLine poststack = _poststack.getPostStack2dLine(lineNo);
        TraceSection section = PostStack2dSectionFactory.createInlineSection(poststack, xlineDecimation);
        updateViewer(section);
      }

    });

    TraceSection section = _viewer.getTraceSection();
    if (section != null) {
      float inline = section.getTraceAxisKeyValue(0, TraceAxisKey.INLINE);
      for (int index = 0; index < _inlineCombo.getItemCount(); index++) {
        String lineName = _inlineCombo.getItem(index);
        SeismicLine2d seismicLine = _poststack.getSurvey().getLineByName(lineName);
        int lineNo = seismicLine.getNumber();
        if (lineNo == Math.round(inline)) {
          _inlineCombo.select(index);
          break;
        }
      }
    }
  }

  private Combo createCombo(final Group group, final String[] lineNames) {
    Combo combo = new Combo(group, SWT.READ_ONLY);
    combo.setItems(lineNames);
    combo.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 2, 1));
    return combo;
  }

  protected void updateViewer() {
    int index = _inlineCombo.getSelectionIndex();
    int lineNo = _poststack.getLineNumbers()[index];
    int xlineDecimation = 1;
    PostStack2dLine poststack = _poststack.getPostStack2dLine(lineNo);
    TraceSection section = PostStack2dSectionFactory.createInlineSection(poststack, xlineDecimation);
    updateViewer(section);
  }

  private void updateViewer(final TraceSection section) {
    _viewer.setTraceSection(section);
  }

  private void updateScales(final float tpiScale, final float ipsScale) {
    _viewer.setScales(tpiScale, ipsScale);
  }
}
