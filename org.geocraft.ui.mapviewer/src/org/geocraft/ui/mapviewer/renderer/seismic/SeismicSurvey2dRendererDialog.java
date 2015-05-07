/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved.
 */
package org.geocraft.ui.mapviewer.renderer.seismic;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.geocraft.core.model.seismic.SeismicLine2d;
import org.geocraft.core.model.seismic.SeismicSurvey2d;
import org.geocraft.ui.common.GridLayoutHelper;
import org.geocraft.ui.plot.settings.LinePropertiesPanel;
import org.geocraft.ui.plot.settings.PointPropertiesPanel;
import org.geocraft.ui.plot.settings.TextPropertiesPanel;
import org.geocraft.ui.viewer.dialog.AbstractSettingsDialog;


/**
 * Settings dialog for the 2D seismic survey renderer.
 */
public class SeismicSurvey2dRendererDialog extends AbstractSettingsDialog {

  /** The dialog main panel. */
  private TabFolder _mainPanel;

  /** The volume renderer. */
  private final SeismicSurvey2dRenderer _renderer;

  private Composite _lineSelectionPanel;

  private PointPropertiesPanel _pointPropertiesPanel;

  private TextPropertiesPanel _textPropertiesPanel;

  private LinePropertiesPanel _linePropertiesPanel;

  private List<Button> _lineButtons;

  private Map<String, Boolean> _linesChecked;

  public SeismicSurvey2dRendererDialog(final Shell shell, final String title, final SeismicSurvey2dRenderer renderer) {
    super(shell, title);
    _renderer = renderer;
  }

  @Override
  protected void createPanel(final Composite parent) {
    _mainPanel = new TabFolder(parent, SWT.TOP);
    TabItem linesTab = new TabItem(_mainPanel, SWT.NONE);
    Composite c = new Composite(_mainPanel, SWT.NONE);
    c.setLayout(GridLayoutHelper.createLayout(2, true));

    Button checkAllButton = new Button(c, SWT.PUSH);
    checkAllButton.setText("Select All");
    checkAllButton.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));

    Button uncheckAllButton = new Button(c, SWT.PUSH);
    uncheckAllButton.setText("Deselect All");
    uncheckAllButton.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));

    //ScrolledComposite scroll = new ScrolledComposite(c, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    //scroll.setAlwaysShowScrollBars(true);
    //scroll.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1));
    _lineSelectionPanel = new Composite(c, SWT.NONE);
    _lineSelectionPanel.setLayout(GridLayoutHelper.createLayout(1, true));
    _lineSelectionPanel.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1));

    _lineButtons = new ArrayList<Button>();
    _linesChecked = new HashMap<String, Boolean>();
    SeismicSurvey2d survey = _renderer.getSurvey();
    List<String> lineNames = new ArrayList<String>();
    for (SeismicLine2d seismicLine : survey.getLines()) {
      lineNames.add(seismicLine.getDisplayName());
    }
    Collections.sort(lineNames);
    for (String lineName : lineNames) {
      SeismicLine2d seismicLine = survey.getLineByName(lineName);
      _linesChecked.put(seismicLine.getDisplayName(), _renderer.isShown(seismicLine));
      Button button = new Button(_lineSelectionPanel, SWT.CHECK);
      button.setText(lineName);
      button.setData(seismicLine);
      button.setSelection(_renderer.isShown(seismicLine));
      button.setLayoutData(GridLayoutHelper.createLayoutData(true, false, SWT.FILL, SWT.FILL, 1, 1));
      button.addListener(SWT.Selection, new Listener() {

        @Override
        public void handleEvent(final Event event) {
          Button btn = (Button) event.widget;
          SeismicLine2d line = (SeismicLine2d) btn.getData();
          _linesChecked.put(line.getDisplayName(), btn.getSelection());
          //_renderer.showSeismicLine((SeismicLine2d) btn.getData(), btn.getSelection());
        }

      });
      _lineButtons.add(button);
    }

    checkAllButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        for (Button btn : _lineButtons) {
          btn.setSelection(true);
          SeismicLine2d seismicLine = (SeismicLine2d) btn.getData();
          _linesChecked.put(seismicLine.getDisplayName(), true);
        }
      }

    });

    uncheckAllButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(final Event event) {
        for (Button btn : _lineButtons) {
          btn.setSelection(false);
          SeismicLine2d seismicLine = (SeismicLine2d) btn.getData();
          _linesChecked.put(seismicLine.getDisplayName(), false);
        }
      }

    });

    //_lineSelectionPanel.setSize(400, 400);
    //scroll.setContent(_lineSelectionPanel);
    linesTab.setText("Seismic Lines");
    linesTab.setControl(c);
    if (_renderer.getPointProperties() != null) {
      TabItem pointTab = new TabItem(_mainPanel, SWT.NONE);
      _pointPropertiesPanel = new PointPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getPointProperties(), true);
      pointTab.setText("Point Properties");
      pointTab.setControl(_pointPropertiesPanel);
    }
    if (_renderer.getTextProperties() != null) {
      TabItem textTab = new TabItem(_mainPanel, SWT.NONE);
      _textPropertiesPanel = new TextPropertiesPanel(_mainPanel, SWT.NONE, _renderer.getTextProperties(), false);
      textTab.setText("Text Properties");
      textTab.setControl(_textPropertiesPanel);
    }
    if (_renderer.getLineProperties() != null) {
      TabItem lineTab = new TabItem(_mainPanel, SWT.NONE);
      _linePropertiesPanel = new LinePropertiesPanel(_mainPanel, SWT.NONE, _renderer.getLineProperties());
      lineTab.setText("Line Properties");
      lineTab.setControl(_linePropertiesPanel);
    }
  }

  @Override
  public void applySettings() {
    _renderer.showSeismicLines(_linesChecked);
    if (_pointPropertiesPanel != null) {
      _renderer.setPointProperties(_pointPropertiesPanel.getProperties());
    }
    if (_textPropertiesPanel != null) {
      _renderer.setTextProperties(_textPropertiesPanel.getProperties());
    }
    if (_linePropertiesPanel != null) {
      _renderer.setLineProperties(_linePropertiesPanel.getProperties());
    }
  }

  @Override
  protected void undoSettings() {
    // TODO Auto-generated method stub

  }

}
