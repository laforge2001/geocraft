/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.io.segy;


import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.geocraft.core.model.datatypes.Header;
import org.geocraft.core.model.datatypes.HeaderDefinition;
import org.geocraft.core.model.datatypes.HeaderEntry;
import org.geocraft.core.model.datatypes.Trace;
import org.geocraft.ui.common.GridLayoutHelper;


public class SegyPreviewDialog extends FormDialog {

  private SegyTraceIterator _iterator;

  private Text _ebcdicHeaderText;

  private Table _binaryHeaderTable;

  private Table _traceHeaderTable;

  private Button _nextTraceButton;

  public SegyPreviewDialog(Shell shell) {
    super(shell);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    _nextTraceButton = createButton(parent, IDialogConstants.NEXT_ID, "Next Trace ->", false);
    _nextTraceButton.setEnabled(false);
    _nextTraceButton.setLayoutData(GridLayoutHelper.createLayoutData(false, true, SWT.FILL, SWT.FILL, 1, 1));
    _nextTraceButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(Event event) {
        updateTraceHeader();
      }

    });
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  @Override
  public void createFormContent(IManagedForm mform) {
    Composite parent = mform.getForm().getBody();
    parent.setLayout(GridLayoutHelper.createLayout(1, true));
    TabFolder tabFolder = new TabFolder(mform.getForm().getBody(), SWT.TOP);
    tabFolder.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL));

    TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
    _ebcdicHeaderText = new Text(tabFolder, SWT.MULTI);
    _ebcdicHeaderText.setEditable(false);
    tabItem1.setControl(_ebcdicHeaderText);
    tabItem1.setText("EBCDIC Header");

    TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
    _binaryHeaderTable = new Table(tabFolder, SWT.BORDER);
    TableColumn tc1 = new TableColumn(_binaryHeaderTable, SWT.CENTER);
    tc1.setText("Byte #");
    tc1.setWidth(100);
    TableColumn tc2 = new TableColumn(_binaryHeaderTable, SWT.CENTER);
    tc2.setText("Value");
    tc2.setWidth(100);
    TableColumn tc3 = new TableColumn(_binaryHeaderTable, SWT.LEFT);
    tc3.setText("Description");
    tc3.setWidth(300);
    _binaryHeaderTable.setHeaderVisible(true);
    tabItem2.setControl(_binaryHeaderTable);
    tabItem2.setText("Binary Header");

    TabItem tabItem3 = new TabItem(tabFolder, SWT.NONE);
    Composite traceComposite = new Composite(tabFolder, SWT.NONE);
    traceComposite.setLayout(GridLayoutHelper.createLayout(2, false));
    _traceHeaderTable = new Table(traceComposite, SWT.BORDER | SWT.V_SCROLL);
    _traceHeaderTable.setLayoutData(GridLayoutHelper.createLayoutData(true, true, SWT.FILL, SWT.FILL, 2, 1));
    TableColumn tc4 = new TableColumn(_traceHeaderTable, SWT.CENTER);
    tc4.setText("Byte #");
    tc4.setWidth(50);
    TableColumn tc5 = new TableColumn(_traceHeaderTable, SWT.CENTER);
    tc5.setText("Value");
    tc5.setWidth(50);
    TableColumn tc6 = new TableColumn(_traceHeaderTable, SWT.LEFT);
    tc6.setText("Description");
    tc6.setWidth(200);
    _traceHeaderTable.setHeaderVisible(true);
    tabItem3.setControl(traceComposite);
    tabItem3.setText("Trace Headers");

  }

  public void setFile(VolumeMapperModel model) {
    _binaryHeaderTable.removeAll();
    _iterator = new SegyTraceIterator(model);
    SegyEbcdicHeader ebcdicHeader = _iterator.getEbcdicHeader();
    _ebcdicHeaderText.setText(ebcdicHeader.asMultiLineString());
    SegyBinaryHeader binaryHeader = _iterator.getBinaryHeader();
    HeaderDefinition binaryHeaderDef = binaryHeader.getHeaderDefinition();
    for (HeaderEntry headerEntry : binaryHeaderDef.getEntries()) {
      TableItem item = new TableItem(_binaryHeaderTable, SWT.NONE);
      int byteOffset = SegyBinaryHeaderCatalog.getByteOffset(headerEntry);
      int byteLoc = 3201 + byteOffset;
      Object value = binaryHeader.getValueObject(headerEntry);
      item.setText(new String[] { "" + byteLoc, "" + value, headerEntry.getDescription() });
    }
    updateTraceHeader();
  }

  private void updateTraceHeader() {
    if (_iterator.hasNext()) {
      _traceHeaderTable.removeAll();
      Trace trace = _iterator.next();
      Header traceHeader = trace.getHeader();
      HeaderDefinition traceHeaderDef = traceHeader.getHeaderDefinition();
      for (HeaderEntry headerEntry : traceHeaderDef.getEntries()) {
        TableItem item = new TableItem(_traceHeaderTable, SWT.NONE);
        int byteOffset = SegyTraceHeaderCatalog.getByteOffset(headerEntry);
        int byteLoc = 1 + byteOffset;
        Object value = traceHeader.getValueObject(headerEntry);
        item.setText(new String[] { "" + byteLoc, value.toString(), headerEntry.getDescription() });
      }
    }
    _nextTraceButton.setEnabled(_iterator.hasNext());
  }
}
