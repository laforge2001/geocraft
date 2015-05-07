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
import org.geocraft.core.service.ServiceProvider;
import org.geocraft.core.service.message.IMessageService;
import org.geocraft.core.service.message.IMessageSubscriber;
import org.geocraft.core.service.message.Topic;
import org.geocraft.ui.form2.FormSection;


public class EntityListField extends OrderedListField implements IMessageSubscriber {

  private final ISpecification _filter;

  /**
   * Constructs an entity parameter list field.
   * 
   * @param parent the parent composite.
   * @param parameter the parameter key.
   * @param label the parameter label.
   * @param showToggle <i>true</i> to show a parameter toggle button; otherwise <i>false</i>.
   * @param filter the specification for filtering the entities.
   */
  public EntityListField(final Composite parent, IFieldListener listener, final String key, final String label, final boolean showToggle, final ISpecification filter) {
    super(parent, listener, key, label, showToggle);
    _filter = filter;

    final IMessageService messageService = ServiceProvider.getMessageService();
    messageService.subscribe(Topic.REPOSITORY_OBJECTS_ADDED, this);
    messageService.subscribe(Topic.REPOSITORY_OBJECTS_REMOVED, this);
    messageService.subscribe(Topic.REPOSITORY_OBJECT_UPDATED, this);
    _listViewer.getControl().addDisposeListener(new DisposeListener() {

      @Override
      public void widgetDisposed(DisposeEvent e) {
        messageService.unsubscribe(Topic.REPOSITORY_OBJECTS_ADDED, EntityListField.this);
        messageService.unsubscribe(Topic.REPOSITORY_OBJECTS_REMOVED, EntityListField.this);
        messageService.unsubscribe(Topic.REPOSITORY_OBJECT_UPDATED, EntityListField.this);
      }

    });
  }

  public void messageReceived(final String topic, final Object value) {
    if (topic.equals(Topic.REPOSITORY_OBJECTS_ADDED) || topic.equals(Topic.REPOSITORY_OBJECTS_REMOVED)
        || topic.equals(Topic.REPOSITORY_OBJECT_UPDATED)) {
      Display.getDefault().asyncExec(new Runnable() {

        public void run() {
          Entity[] entities = FormSection.getFilteredEntities(_filter);
          setOptions(entities);
        }
      });
    }
  }

}
