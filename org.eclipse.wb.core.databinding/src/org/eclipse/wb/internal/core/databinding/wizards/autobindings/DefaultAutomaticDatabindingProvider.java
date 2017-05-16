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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Provider for bindings API based on bind property to property with update strategies.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public abstract class DefaultAutomaticDatabindingProvider implements IAutomaticDatabindingProvider {
  private final DescriptorContainer m_widgetContainer;
  private final DescriptorContainer m_strategyContainer;
  protected final Map<PropertyAdapter, AbstractDescriptor[]> m_propertyToEditor = Maps.newHashMap();
  protected final Map<IJavaProject, ClassLoader> m_classLoaders = Maps.newHashMap();
  protected ClassLoader m_classLoader;
  protected Class<?> m_beanClass;
  protected IJavaProject m_javaProject;
  protected ICompleteListener m_pageListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DefaultAutomaticDatabindingProvider(DescriptorContainer widgetContainer,
      DescriptorContainer strategyContainer) {
    m_widgetContainer = widgetContainer;
    m_strategyContainer = strategyContainer;
    m_propertiesFilter = new PropertiesFilter(m_widgetContainer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // WizardPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCurrentWizardData(AutomaticDatabindingFirstPage firstPage,
      ICompleteListener pageListener) {
    m_javaProject = firstPage.getJavaProject();
    m_pageListener = pageListener;
    try {
      // prepare class loader
      m_classLoader = m_classLoaders.get(m_javaProject);
      // check create new class loader
      if (m_classLoader == null) {
        m_classLoader = ProjectClassLoader.create(null, m_javaProject);
        m_classLoaders.put(m_javaProject, m_classLoader);
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return CoreUtils.load(m_classLoader, className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
    m_beanClass = choosenClass;
    m_propertyToEditor.clear();
    setWidgetCompositeEnabled(false);
    return getProperties0(m_beanClass);
  }

  protected abstract List<PropertyAdapter> getProperties0(Class<?> choosenClass) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final ViewerFilter m_propertiesFilter;
  protected CheckboxTableViewer m_propertiesViewer;
  protected Composite m_widgetComposite;
  protected Label m_editorLabel;
  protected Label m_strategyLabel;
  protected CheckboxTableViewer m_editorsViewer;
  protected CheckboxTableViewer m_strategiesViewer;

  public void configurePropertiesViewer(CheckboxTableViewer viewer) {
    m_propertiesViewer = viewer;
    m_propertiesViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        handlePropertySelection();
      }
    });
  }

  public ViewerFilter getPropertiesViewerFilter() {
    return m_propertiesFilter;
  }

  public boolean getPropertiesViewerFilterInitState() {
    return true;
  }

  public void fillWidgetComposite(Composite widgetComposite) {
    // configure widget composite
    m_widgetComposite = widgetComposite;
    GridLayoutFactory.create(widgetComposite).columns(m_strategyContainer == null ? 1 : 2).noMargins();
    // editor title
    m_editorLabel = new Label(widgetComposite, SWT.NONE);
    m_editorLabel.setText(Messages.DefaultAutomaticDatabindingProvider_editorLabel);
    // strategy title
    if (m_strategyContainer != null) {
      m_strategyLabel = new Label(widgetComposite, SWT.NONE);
      m_strategyLabel.setText(Messages.DefaultAutomaticDatabindingProvider_strategyLabel);
    }
    // editor viewer
    m_editorsViewer =
        CheckboxTableViewer.newCheckList(widgetComposite, SWT.BORDER
            | SWT.FULL_SELECTION
            | SWT.H_SCROLL
            | SWT.V_SCROLL);
    // create columns
    GridDataFactory.create(m_editorsViewer.getControl()).hintVC(20).fill().grabH();
    TableFactory.modify(m_editorsViewer).standard().newColumn().width(170).text(
        Messages.DefaultAutomaticDatabindingProvider_widgetColumn);
    TableFactory.modify(m_editorsViewer).standard().newColumn().width(150).text(
        Messages.DefaultAutomaticDatabindingProvider_propertyColumn);
    // configure viewer
    m_editorsViewer.setContentProvider(new ArrayContentProvider());
    m_editorsViewer.setLabelProvider(new DescriptorTableLabelProvider());
    m_editorsViewer.setInput(m_widgetContainer.getDescriptors());
    new SingleCheckSelectionListener(m_editorsViewer, 0);
    // strategy viewer
    if (m_strategyContainer != null) {
      m_strategiesViewer =
          CheckboxTableViewer.newCheckList(widgetComposite, SWT.BORDER
              | SWT.FULL_SELECTION
              | SWT.H_SCROLL
              | SWT.V_SCROLL);
      GridDataFactory.create(m_strategiesViewer.getControl()).fill().grab();
      m_strategiesViewer.setContentProvider(new ArrayContentProvider());
      m_strategiesViewer.setLabelProvider(new DescriptorLabelProvider());
      m_strategiesViewer.setInput(m_strategyContainer.getDescriptors());
      new SingleCheckSelectionListener(m_strategiesViewer, 1);
    }
    //
    setWidgetCompositeEnabled(false);
  }

  private void setWidgetCompositeEnabled(boolean enabled) {
    // viewers state
    if (!enabled) {
      m_editorsViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
      if (m_strategiesViewer != null) {
        m_strategiesViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
      }
    }
    // buttons state
    m_editorLabel.setEnabled(enabled);
    m_editorsViewer.getControl().setEnabled(enabled);
    if (m_strategiesViewer != null) {
      m_strategyLabel.setEnabled(enabled);
      m_strategiesViewer.getControl().setEnabled(enabled);
    }
    m_widgetComposite.setEnabled(enabled);
  }

  public void handlePropertyChecked(PropertyAdapter property, boolean checked) {
    if (checked) {
      AbstractDescriptor[] editorData = m_propertyToEditor.get(property);
      if (editorData == null) {
        editorData =
            new AbstractDescriptor[]{
                m_widgetContainer.getDefaultDescriptor(property, true),
                m_strategyContainer == null ? null : m_strategyContainer.getDefaultDescriptor(
                    property,
                    true)};
        m_propertyToEditor.put(property, editorData);
      }
    }
  }

  protected void handlePropertySelection() {
    IStructuredSelection selection = UiUtils.getSelection(m_propertiesViewer);
    boolean enabled =
        !UiUtils.isEmpty(selection) && m_propertiesViewer.getChecked(selection.getFirstElement());
    setWidgetCompositeEnabled(enabled);
    //
    if (enabled) {
      AbstractDescriptor[] editorData = m_propertyToEditor.get(selection.getFirstElement());
      m_editorsViewer.setCheckedElements(new Object[]{editorData[0]});
      m_editorsViewer.setSelection(new StructuredSelection(editorData[0]), true);
      if (m_strategiesViewer != null) {
        m_strategiesViewer.setCheckedElements(new Object[]{editorData[1]});
        m_strategiesViewer.setSelection(new StructuredSelection(editorData[1]), true);
      }
    }
  }

  protected void handleChangeEditor(AbstractDescriptor newValue, int index) {
    // prepare selection
    IStructuredSelection selection = UiUtils.getSelection(m_propertiesViewer);
    AbstractDescriptor[] editorData = m_propertyToEditor.get(selection.getFirstElement());
    // sets new value
    editorData[index] = newValue;
    // update viewer
    m_pageListener.calculateFinish();
  }

  public String calculateFinish() {
    Object[] elements = m_propertiesViewer.getCheckedElements();
    //
    for (Object element : elements) {
      PropertyAdapter property = (PropertyAdapter) element;
      Object[] editorData = m_propertyToEditor.get(property);
      //
      if (editorData[0] == null) {
        return MessageFormat.format(
            Messages.DefaultAutomaticDatabindingProvider_validateNoWidgetForProperty,
            property.getName());
      }
      if (m_strategyContainer != null && editorData[1] == null) {
        return MessageFormat.format(
            Messages.DefaultAutomaticDatabindingProvider_validateNoStrategyForProperty,
            property.getName());
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Single checked listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private class SingleCheckSelectionListener implements ICheckStateListener {
    private final CheckboxTableViewer m_viewer;
    private final int m_index;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SingleCheckSelectionListener(CheckboxTableViewer viewer, int index) {
      m_viewer = viewer;
      m_index = index;
      m_viewer.addCheckStateListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ICheckStateListener
    //
    ////////////////////////////////////////////////////////////////////////////
    public void checkStateChanged(CheckStateChangedEvent event) {
      if (event.getChecked()) {
        Object selection = event.getElement();
        Object[] elements = m_viewer.getCheckedElements();
        // unchecked other elements
        for (int i = 0; i < elements.length; i++) {
          Object element = elements[i];
          if (element != selection) {
            m_viewer.setChecked(element, false);
          }
        }
      }
      // sets new value
      AbstractDescriptor descriptor = (AbstractDescriptor) event.getElement();
      handleChangeEditor(event.getChecked() ? descriptor : null, m_index);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // LabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class DescriptorLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
      AbstractDescriptor descriptor = (AbstractDescriptor) element;
      return descriptor.getName();
    }

    @Override
    public Image getImage(Object element) {
      AbstractDescriptor descriptor = (AbstractDescriptor) element;
      return descriptor.getImage();
    }
  }
  private static class DescriptorTableLabelProvider extends LabelProvider
      implements
        ITableLabelProvider {
    public String getColumnText(Object element, int columnIndex) {
      AbstractDescriptor descriptor = (AbstractDescriptor) element;
      return descriptor.getName(columnIndex);
    }

    public Image getColumnImage(Object element, int columnIndex) {
      AbstractDescriptor descriptor = (AbstractDescriptor) element;
      return descriptor.getImage(columnIndex);
    }
  }
}