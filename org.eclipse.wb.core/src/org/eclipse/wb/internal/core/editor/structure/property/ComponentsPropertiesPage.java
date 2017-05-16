/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.structure.property;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.editor.structure.property.PropertyCategoryProviderProvider;
import org.eclipse.wb.core.editor.structure.property.PropertyListProcessor;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDeactivePropertyEditor;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.structure.IPage;
import org.eclipse.wb.internal.core.model.ObjectReferenceInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.PropertyManager;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProviders;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.IPropertyExceptionHandler;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.MenuItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link IPage} for displaying {@link Property}'s of {@link ObjectInfo}'s.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public final class ComponentsPropertiesPage implements IPage {
  private static final Property[] NO_PROPERTIES = new Property[0];
  private Composite m_container;
  private StackLayout m_stackLayout;
  private PropertyTable m_propertyTable;
  private PropertyTable m_eventsTable;
  private ObjectInfo m_rootObject;
  private final List<ObjectInfo> m_objects = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    Control control = getControl();
    if (control != null && !control.isDisposed()) {
      control.dispose();
    }
  }

  public void createControl(Composite parent) {
    {
      m_container = new Composite(parent, SWT.NONE);
      m_stackLayout = new StackLayout();
      m_container.setLayout(m_stackLayout);
    }
    {
      IPropertyExceptionHandler exceptionHandler = new IPropertyExceptionHandler() {
        public void handle(Throwable e) {
          IDesignPageSite site = IDesignPageSite.Helper.getSite(m_rootObject);
          site.handleException(e);
        }
      };
      {
        m_propertyTable = new PropertyTable(m_container, SWT.NONE);
        m_propertyTable.setExceptionHandler(exceptionHandler);
      }
      {
        m_eventsTable = new PropertyTable(m_container, SWT.NONE);
        m_eventsTable.setExceptionHandler(exceptionHandler);
      }
    }
    // show "properties" table
    {
      m_stackLayout.topControl = m_propertyTable;
      m_container.layout();
    }
    // actions
    {
      createActions();
      setPropertyTableContextMenu();
    }
  }

  public Control getControl() {
    return m_container;
  }

  public void setFocus() {
    getControl().setFocus();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private Property m_activeProperty;
  private IToolBarManager m_toolBarManager;

  public void setToolBar(IToolBarManager toolBarManager) {
    m_toolBarManager = toolBarManager;
    updateActions();
  }

  /**
   * Creates {@link Action}'s.
   */
  private void createActions() {
    create_showEventsAction();
    create_showAdvancedPropertiesAction();
    create_setCategoryAction();
    create_defaultValueAction();
    trackPropertySelection();
  }

  /**
   * Updates {@link Action}'s.
   */
  private void updateActions() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // update standard items
        update_showEventsAction();
        update_categoryAction();
        update_defaultValueAction();
        // update toolbar
        Control toolBarControl = ((ToolBarManager) m_toolBarManager).getControl();
        try {
          toolBarControl.setRedraw(false);
          m_toolBarManager.removeAll();
          // add standard items
          m_toolBarManager.add(m_showEventsAction);
          m_toolBarManager.add(new Separator(IPropertiesToolBarContributor.GROUP_EDIT));
          m_toolBarManager.add(new Separator(IPropertiesToolBarContributor.GROUP_ADDITIONAL));
          m_toolBarManager.add(m_showAdvancedPropertiesAction);
          m_toolBarManager.add(m_defaultValueAction);
          // use external contributors
          List<IPropertiesToolBarContributor> contributors =
              ExternalFactoriesHelper.getElementsInstances(
                  IPropertiesToolBarContributor.class,
                  "org.eclipse.wb.core.propertiesPageActions",
                  "toolbar");
          for (final IPropertiesToolBarContributor contributor : contributors) {
            ExecutionUtils.runLog(new RunnableEx() {
              public void run() throws Exception {
                contributor.contributeToolBar(m_toolBarManager, m_objects);
              }
            });
          }
          // done
          m_toolBarManager.update(false);
        } finally {
          toolBarControl.setRedraw(true);
        }
      }
    });
  }

  /**
   * Sets the context menu for {@link #m_propertyTable}.
   */
  private void setPropertyTableContextMenu() {
    final MenuManager manager = new MenuManager();
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(new IMenuListener() {
      public void menuAboutToShow(IMenuManager _manager) {
        // dispose items to avoid their caching
        for (MenuItem item : manager.getMenu().getItems()) {
          item.dispose();
        }
        // apply new items
        fillContextMenu();
      }

      private void fillContextMenu() {
        manager.add(new Separator(IPropertiesMenuContributor.GROUP_TOP));
        manager.add(new Separator(IPropertiesMenuContributor.GROUP_EDIT));
        manager.add(m_defaultValueAction);
        manager.add(m_showAdvancedPropertiesAction);
        {
          manager.add(new Separator(IPropertiesMenuContributor.GROUP_PRIORITY));
          manager.add(m_setCategoryAction_default);
          manager.add(m_setCategoryAction_preferred);
          manager.add(m_setCategoryAction_normal);
          manager.add(m_setCategoryAction_advanced);
        }
        manager.add(new Separator(IPropertiesMenuContributor.GROUP_ADDITIONAL));
        // use external contributors
        List<IPropertiesMenuContributor> contributors =
            ExternalFactoriesHelper.getElementsInstances(
                IPropertiesMenuContributor.class,
                "org.eclipse.wb.core.propertiesPageActions",
                "menu");
        for (final IPropertiesMenuContributor contributor : contributors) {
          ExecutionUtils.runLog(new RunnableEx() {
            public void run() throws Exception {
              contributor.contributeMenu(manager, m_activeProperty);
            }
          });
        }
      }
    });
    m_propertyTable.setMenu(manager.createContextMenu(m_propertyTable));
  }

  /**
   * Tracks {@link Property} selection in {@link PropertyTable}'s.
   */
  private void trackPropertySelection() {
    ISelectionChangedListener listener = new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        StructuredSelection selection = (StructuredSelection) event.getSelection();
        m_activeProperty = (Property) selection.getFirstElement();
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            update_defaultValueAction();
            update_categoryAction();
          }
        });
      }
    };
    m_propertyTable.addSelectionChangedListener(listener);
    m_eventsTable.addSelectionChangedListener(listener);
  }

  /**
   * Sets same text for "text" and "toolTipText" properties of {@link IAction}.
   */
  private static void setTexts(IAction action, String text) {
    action.setText(text);
    action.setToolTipText(text);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link ISelection} with {@link ObjectInfo}'s to display.
   */
  public void setSelection(ISelection selection) {
    // prepare new array of objects
    {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      m_objects.clear();
      //
      for (Iterator<?> I = structuredSelection.iterator(); I.hasNext();) {
        ObjectInfo objectInfo = (ObjectInfo) I.next();
        if (objectInfo instanceof ObjectReferenceInfo) {
          objectInfo = ((ObjectReferenceInfo) objectInfo).getObject();
        }
        if (objectInfo.isDeleted()) {
          continue;
        }
        m_objects.add(objectInfo);
      }
    }
    // add listener to new root, show new objects
    if (m_objects.isEmpty()) {
      m_rootObject = null;
      m_propertyTable.setInput(null);
    } else {
      m_rootObject = m_objects.get(0).getRoot();
      m_rootObject.addBroadcastListener(new ObjectEventListener() {
        @Override
        public void refreshed() throws Exception {
          refreshProperties();
        }

        @Override
        public void presentationChanged() throws Exception {
          refreshProperties();
        }
      });
      deactivatePropertyEditor_whenDelete();
      deactivatePropertyEditor_whenDispose();
      deactivatePropertyEditor_whenExplicitlyRequested();
    }
    // refresh properties
    refreshProperties();
  }

  /**
   * We should deactivate {@link PropertyEditor} and ignore changes when user deletes one of the
   * components that provide displayed properties.
   */
  private void deactivatePropertyEditor_whenDelete() {
    m_rootObject.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        boolean deleteDisplayedObject = m_objects.contains(child);
        if (deleteDisplayedObject) {
          m_propertyTable.deactivateEditor(false);
        }
      }
    });
  }

  /**
   * We should deactivate {@link PropertyEditor} and ignore changes when dispose components
   * hierarchy.
   */
  private void deactivatePropertyEditor_whenDispose() {
    m_rootObject.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void dispose_beforePresentation() throws Exception {
        m_propertyTable.deactivateEditor(false);
      }
    });
  }

  /**
   * Sometimes we know that we changed model in the way which may cause changes in list of
   * properties.
   */
  private void deactivatePropertyEditor_whenExplicitlyRequested() {
    m_rootObject.addBroadcastListener(new ObjectInfoDeactivePropertyEditor() {
      public void invoke() throws Exception {
        m_propertyTable.deactivateEditor(false);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action: Filter advanced properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private Action m_showAdvancedPropertiesAction;

  /**
   * Creates the {@link #m_showAdvancedPropertiesAction}.
   */
  private void create_showAdvancedPropertiesAction() {
    m_showAdvancedPropertiesAction = new Action("", IAction.AS_CHECK_BOX) {
      @Override
      public void run() {
        boolean show = m_showAdvancedPropertiesAction.isChecked();
        m_propertyTable.setShowAdvancedProperties(show);
      }
    };
    m_showAdvancedPropertiesAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/filter_advanced_properties.gif"));
    setTexts(m_showAdvancedPropertiesAction, Messages.ComponentsPropertiesPage_showAdvancedAction);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action: set category
  //
  ////////////////////////////////////////////////////////////////////////////
  private SetCategoryAction m_setCategoryAction_default;
  private SetCategoryAction m_setCategoryAction_preferred;
  private SetCategoryAction m_setCategoryAction_normal;
  private SetCategoryAction m_setCategoryAction_advanced;

  /**
   * Creates category actions.
   */
  private void create_setCategoryAction() {
    m_setCategoryAction_default =
        new SetCategoryAction(Messages.ComponentsPropertiesPage_useDefaultCategoryAction, null);
    m_setCategoryAction_preferred =
        new SetCategoryAction(Messages.ComponentsPropertiesPage_markAsPreferredAction,
            PropertyCategory.PREFERRED);
    m_setCategoryAction_normal =
        new SetCategoryAction(Messages.ComponentsPropertiesPage_markAsNormalAction,
            PropertyCategory.NORMAL);
    m_setCategoryAction_advanced =
        new SetCategoryAction(Messages.ComponentsPropertiesPage_markAsAdvancedAction,
            PropertyCategory.ADVANCED);
  }

  /**
   * Updates state of category actions.
   */
  private void update_categoryAction() throws Exception {
    m_setCategoryAction_default.update();
    m_setCategoryAction_preferred.update();
    m_setCategoryAction_normal.update();
    m_setCategoryAction_advanced.update();
  }

  private class SetCategoryAction extends Action {
    private final PropertyCategory m_category;

    public SetCategoryAction(String text, PropertyCategory category) {
      super(text, category != null ? AS_RADIO_BUTTON : 0);
      m_category = category;
    }

    @Override
    public void run() {
      PropertyManager.setCategory(m_activeProperty, m_category);
      m_propertyTable.redraw();
    }

    private void update() {
      setEnabled(m_activeProperty != null);
      if (m_activeProperty != null) {
        setChecked(PropertyManager.getCategory(m_activeProperty) == m_category);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action: Restore default value
  //
  ////////////////////////////////////////////////////////////////////////////
  private Action m_defaultValueAction;

  /**
   * Creates the {@link #m_defaultValueAction}.
   */
  private void create_defaultValueAction() {
    m_defaultValueAction = new Action() {
      @Override
      public void run() {
        ExecutionUtils.run(m_rootObject, new RunnableEx() {
          public void run() throws Exception {
            m_activeProperty.setValue(Property.UNKNOWN_VALUE);
          }
        });
      }
    };
    m_defaultValueAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/properties_default.gif"));
    setTexts(m_defaultValueAction, Messages.ComponentsPropertiesPage_restoreDefaultAction);
  }

  /**
   * Updates the state of {@link #m_defaultValueAction}.
   */
  private void update_defaultValueAction() throws Exception {
    if (m_activeProperty != null) {
      m_defaultValueAction.setEnabled(m_activeProperty.isModified());
    } else {
      m_defaultValueAction.setEnabled(false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action: show events
  //
  ////////////////////////////////////////////////////////////////////////////
  private Action m_showEventsAction;
  private boolean m_showEvents;

  /**
   * Creates the {@link #m_showEventsAction}.
   */
  private void create_showEventsAction() {
    m_showEventsAction = new Action() {
      @Override
      public void run() {
        m_showEvents = !m_showEvents;
        m_showEventsAction.setChecked(m_showEvents);
        refreshProperties();
        // set focus
        if (m_showEvents) {
          m_eventsTable.setFocus();
        } else {
          m_propertyTable.setFocus();
        }
      }
    };
    m_showEventsAction.setImageDescriptor(DesignerPlugin.getImageDescriptor("structure/events.gif"));
    setTexts(m_showEventsAction, Messages.ComponentsPropertiesPage_showEventsAction);
    m_showEventsAction.setChecked(m_showEvents);
  }

  /**
   * Updates the state of {@link #m_showEventsAction}.
   */
  private void update_showEventsAction() throws Exception {
    boolean isValid = m_objects.size() == 1;
    m_showEventsAction.setEnabled(isValid);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Shows {@link Property}'s of current objects.
   */
  private void refreshProperties() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        if (m_showEvents) {
          showEvents();
        } else {
          showProperties();
        }
        // update actions
        updateActions();
      }
    });
  }

  /**
   * Shows "event" {@link Property}'s in {@link #m_eventsTable}.
   */
  private void showEvents() throws Exception {
    Property[] properties = null;
    if (m_objects.size() == 1) {
      Property eventsProperty = m_objects.get(0).getPropertyByTitle("Events");
      properties = PropertyUtils.getChildren(eventsProperty);
    }
    // set properties
    m_eventsTable.setInput(properties);
    // show "events" table
    {
      m_stackLayout.topControl = m_eventsTable;
      m_container.layout();
    }
  }

  /**
   * Shows "usual" {@link Property}'s in {@link #m_propertyTable}.
   */
  private void showProperties() throws Exception {
    List<Property> propertyList = Lists.newArrayList();
    Collections.addAll(propertyList, getAllProperties());
    preparePropertiesForDisplaying(propertyList);
    // set properties
    {
      Property[] properties = propertyList.toArray(new Property[propertyList.size()]);
      m_propertyTable.setInput(properties);
    }
    // show "property" table
    {
      m_stackLayout.topControl = m_propertyTable;
      m_container.layout();
    }
  }

  private void preparePropertiesForDisplaying(List<Property> properties) {
    final PropertyCategoryProvider provider = getPropertyCategoryProvider();
    m_propertyTable.setPropertyCategoryProvider(provider);
    // move system properties on top
    Collections.sort(properties, new Comparator<Property>() {
      public int compare(Property property_1, Property property_2) {
        PropertyCategory category_1 = provider.getCategory(property_1);
        PropertyCategory category_2 = provider.getCategory(property_2);
        boolean system_1 = category_1.isSystem();
        boolean system_2 = category_2.isSystem();
        if (system_1 && system_2) {
          return category_1.getPriority() - category_2.getPriority();
        } else if (system_1) {
          return -1;
        } else if (system_2) {
          return 1;
        }
        return 0;
      }
    });
    // apply processors
    {
      List<PropertyListProcessor> processors =
          ExternalFactoriesHelper.getElementsInstances(
              PropertyListProcessor.class,
              "org.eclipse.wb.core.propertiesPageProcessors",
              "processor");
      for (PropertyListProcessor processor : processors) {
        processor.process(m_objects, properties);
      }
    }
  }

  /**
   * @return the {@link PropertyCategoryProvider} to use for properties of {@link #m_objects}.
   */
  private PropertyCategoryProvider getPropertyCategoryProvider() {
    List<PropertyCategoryProviderProvider> providers2 =
        ExternalFactoriesHelper.getElementsInstances(
            PropertyCategoryProviderProvider.class,
            "org.eclipse.wb.core.propertiesPageCategoryProviders",
            "provider");
    for (PropertyCategoryProviderProvider provider2 : providers2) {
      PropertyCategoryProvider provider = provider2.get(m_objects);
      if (provider != null) {
        return provider;
      }
    }
    return PropertyCategoryProviders.def();
  }

  /**
   * @return array of {@link Property} with {@link Property}'s of current objects.
   */
  private Property[] getAllProperties() throws Exception {
    if (m_objects.size() == 0) {
      return NO_PROPERTIES;
    } else if (m_objects.size() == 1) {
      return getProperties(m_objects.get(0));
    } else {
      // intersect properties
      PropertyListIntersector intersector = new PropertyListIntersector();
      for (ObjectInfo objectInfo : m_objects) {
        intersector.intersect(getProperties(objectInfo));
      }
      // return composite properties
      return intersector.getProperties();
    }
  }

  private static Property[] getProperties(ObjectInfo object) throws Exception {
    if (!object.isDeleted()) {
      return object.getProperties();
    } else {
      return NO_PROPERTIES;
    }
  }
}
