/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field.aoi;


import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.geocraft.core.model.aoi.AreaOfInterest;
import org.geocraft.core.model.aoi.SeismicSurvey2dAOI;
import org.geocraft.core.model.aoi.SeismicSurvey3dAOI;
import org.geocraft.core.repository.IRepository;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.ui.common.TableWrapLayoutHelper;
import org.geocraft.ui.form2.field.EntityComboField;
import org.geocraft.ui.form2.field.IFieldListener;


public class AOIComboField extends EntityComboField {

  private int _dimension;

  private Composite _container;

  private Button _createButton;

  public AOIComboField(Composite parent, IFieldListener listener, String key, String label, int dimension) {
    super(parent, listener, key, label, true, new TypeSpecification(AreaOfInterest.class));
    _dimension = dimension;
  }

  public AOIComboField(Composite parent, IFieldListener listener, String key, String label, int dimension, Class aoiClass) {
    super(parent, listener, key, label, true, new TypeSpecification(aoiClass));
    _dimension = dimension;
  }

  public void setDimension(int dimension) {
    _dimension = dimension;
  }

  @Override
  public Control[] createControls(final Composite parent) {
    _container = new Composite(parent, SWT.NONE);
    _container.setLayoutData(TableWrapLayoutHelper
        .createLayoutData(true, false, TableWrapData.FILL, TableWrapData.FILL));
    _container.setLayout(TableWrapLayoutHelper.createLayout(2, false));
    Control[] controlsBase = super.createControls(_container);
    Control[] controls = new Control[controlsBase.length + 1];
    System.arraycopy(controlsBase, 0, controls, 0, controlsBase.length);
    _createButton = new Button(_container, SWT.PUSH);
    _createButton.setText("Create...");
    _createButton.setLayoutData(TableWrapLayoutHelper.createLayoutData(false, false, TableWrapData.FILL,
        TableWrapData.FILL));
    controls[controlsBase.length] = _createButton;

    _createButton.addListener(SWT.Selection, new Listener() {

      @Override
      public void handleEvent(Event event) {
        final IMessageService messageService = ServiceProvider.getMessageService();
        switch (_dimension) {
          case 2:
            IMessageSubscriber subscriber2 = new IMessageSubscriber() {

              @Override
              public void messageReceived(String topic, Object message) {
                if (topic.equals(AOI2dModel.SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC)) {
                  IRepository repository = ServiceProvider.getRepository();
                  Map<String, Object> results = repository.get(new UniqueIdSpecification(message.toString()));
                  if (results != null && results.values().size() == 1) {
                    SeismicSurvey2dAOI aoi = (SeismicSurvey2dAOI) results.values().iterator().next();
                    Object[] oldOptions = getOptions();
                    Object[] newOptions = new Object[oldOptions.length + 1];
                    int index = oldOptions.length;
                    System.arraycopy(oldOptions, 0, newOptions, 0, oldOptions.length);
                    newOptions[index] = aoi;
                    setOptions(newOptions);
                    _combo.select(index);
                    System.out.println("FIELD CHANGED1: " + _key + " AOI=" + aoi);
                    _listener.fieldChanged(_key, aoi);
                  }
                  messageService.unsubscribe(AOI2dModel.SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC, this);
                }
              }
            };
            messageService.subscribe(AOI2dModel.SEISMIC_SURVEY_2D_AOI_CREATED_TOPIC, subscriber2);
            AOI2dCreateDialog dialog2 = new AOI2dCreateDialog(parent.getShell());
            int result2 = dialog2.open();
            if (result2 == Window.OK) {
              AreaOfInterest aoi = dialog2.getAOI();
              IRepository repository = ServiceProvider.getRepository();
              if (repository != null && aoi != null) {
                Object[] oldOptions = getOptions();
                Object[] newOptions = new Object[oldOptions.length + 1];
                int index = oldOptions.length;
                System.arraycopy(oldOptions, 0, newOptions, 0, oldOptions.length);
                newOptions[index] = aoi;
                setOptions(newOptions);
                _combo.select(index);
                _listener.fieldChanged(_key, aoi);
                System.out.println("FIELD CHANGED2: " + _key + " AOI=" + aoi);
                repository.add(aoi);
              }
            }
            break;
          case 3:
            IMessageSubscriber subscriber3 = new IMessageSubscriber() {

              @Override
              public void messageReceived(String topic, Object message) {
                if (topic.equals(AOI3dModel.SEISMIC_SURVEY_3D_AOI_CREATED_TOPIC)) {
                  IRepository repository = ServiceProvider.getRepository();
                  Map<String, Object> results = repository.get(new UniqueIdSpecification(message.toString()));
                  if (results != null && results.values().size() == 1) {
                    SeismicSurvey3dAOI aoi = (SeismicSurvey3dAOI) results.values().iterator().next();
                    Object[] oldOptions = getOptions();
                    Object[] newOptions = new Object[oldOptions.length + 1];
                    int index = oldOptions.length;
                    System.arraycopy(oldOptions, 0, newOptions, 0, oldOptions.length);
                    newOptions[index] = aoi;
                    setOptions(newOptions);
                    _combo.select(index);
                    _listener.fieldChanged(_key, aoi);
                  }
                  messageService.unsubscribe(AOI3dModel.SEISMIC_SURVEY_3D_AOI_CREATED_TOPIC, this);
                }
              }
            };
            messageService.subscribe(AOI3dModel.SEISMIC_SURVEY_3D_AOI_CREATED_TOPIC, subscriber3);
            AOI3dCreateDialog dialog3 = new AOI3dCreateDialog(parent.getShell());
            int result3 = dialog3.open();
            if (result3 == Window.OK) {
              AreaOfInterest aoi = dialog3.getAOI();
              IRepository repository = ServiceProvider.getRepository();
              if (repository != null && aoi != null) {
                Object[] oldOptions = getOptions();
                Object[] newOptions = new Object[oldOptions.length + 1];
                int index = oldOptions.length;
                System.arraycopy(oldOptions, 0, newOptions, 0, oldOptions.length);
                newOptions[index] = aoi;
                setOptions(newOptions);
                _combo.select(index);
                _listener.fieldChanged(_key, aoi);
                repository.add(aoi);
              }
            }
            break;
          default:
            MessageDialog.openWarning(parent.getShell(), "AOI Creation", "Unable to create AOI for " + _dimension
                + "D yet.");
        }
      }

    });
    return controls;
  }

  @Override
  public void adapt(final FormToolkit toolkit) {
    super.adapt(toolkit);
    toolkit.adapt(_container);
  }
}
