/*
 * Copyright (C) ConocoPhillips 2008 All Rights Reserved. 
 */
package org.geocraft.ui.form2.field;


import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.geocraft.core.model.Entity;
import org.geocraft.core.repository.specification.ISpecification;
import org.geocraft.core.repository.specification.TypeSpecification;
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.form2.FormSection;


public class EntityComboField extends ComboField implements IMessageSubscriber {

  private ISpecification _filter;

  public EntityComboField(final Composite parent, IFieldListener listener, String key, final String label, final boolean showToggle, final ISpecification filter) {
    super(parent, listener, key, label, showToggle, true);
    _filter = filter;
    final IMessageService messageService = ServiceProvider.getMessageService();
    messageService.subscribe(Topic.REPOSITORY_OBJECTS_ADDED, this);
    messageService.subscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
    messageService.subscribe(Topic.REPOSITORY_OBJECT_UPDATED, this);
    _combo.addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        unsubscribeFromMessageService();
      }
    });
  }

  public void unsubscribeFromMessageService() {
    final IMessageService messageService = ServiceProvider.getMessageService();
    messageService.unsubscribe(Topic.REPOSITORY_OBJECTS_ADDED, EntityComboField.this);
    messageService.unsubscribe(Topic.REPOSITORY_OBJECTS_REMOVED, EntityComboField.this);
    messageService.unsubscribe(Topic.REPOSITORY_OBJECT_UPDATED, EntityComboField.this);
  }

  public void messageReceived(final String topic, final Object value) {
    if (topic.equals(Topic.REPOSITORY_OBJECTS_ADDED) || topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)
        || topic.equals(Topic.REPOSITORY_OBJECT_UPDATED)) {
      updateOptions();
    }
  }

  public void setFilter(Class klass) {
    setFilter(new TypeSpecification(klass));
  }

  public void setFilter(ISpecification filter) {
    _filter = filter;
    updateOptions();
  }

  private void updateOptions() {
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        synchronized (_combo) {
          if (_combo != null && !_combo.isDisposed()) {
            Entity[] entities = FormSection.getFilteredEntities(_filter);
            setOptions(entities);
          }
        }
      }
    });
  }

}
