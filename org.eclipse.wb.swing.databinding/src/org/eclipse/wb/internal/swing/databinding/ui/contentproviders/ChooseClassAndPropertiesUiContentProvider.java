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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.CheckboxTreeViewerWrapper;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ICheckboxViewerWrapper;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveDecoratingLabelProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.BeanSupport;
import org.eclipse.wb.internal.swing.databinding.model.beans.ElPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.ClassGenericType;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.model.properties.ElPropertyInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el.ElPropertyUiConfiguration;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Content provider for choose properties from Tree (properties + sub properties).
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public abstract class ChooseClassAndPropertiesUiContentProvider
    extends
      org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider {
  private CheckboxTreeViewer m_treeViewer;
  private ElPropertyUiContentProvider m_elPropertyUIContentProvider;
  private boolean m_elProperty;
  private final ChooseClassAndPropertiesConfiguration m_configuration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassAndPropertiesUiContentProvider(ChooseClassAndPropertiesConfiguration configuration) {
    super(configuration);
    m_configuration = configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContent(Composite parent, int columns) {
    super.createContent(parent, columns);
    //
    if (m_configuration.isWorkWithELProperty()) {
      ElPropertyUiConfiguration configuration = new ElPropertyUiConfiguration();
      configuration.setTitle(Messages.ChooseClassAndPropertiesUiContentProvider_title);
      m_elPropertyUIContentProvider = new ElPropertyUiContentProvider(configuration, null);
      m_elPropertyUIContentProvider.setCompleteListener(new ICompleteListener() {
        public void calculateFinish() {
          calculateSubFinish();
        }
      });
      m_elPropertyUIContentProvider.createContent(parent, columns);
      //
      m_treeViewer.addCheckStateListener(new ICheckStateListener() {
        public void checkStateChanged(CheckStateChangedEvent event) {
          handleELProperty();
        }
      });
    }
  }

  @Override
  protected ICheckboxViewerWrapper createPropertiesViewer(Composite parent) {
    m_treeViewer =
        new CheckboxTreeViewer(parent, SWT.BORDER
            | SWT.FULL_SELECTION
            | SWT.H_SCROLL
            | SWT.V_SCROLL);
    m_treeViewer.setContentProvider(new PropertyAdapterContentProvider());
    m_treeViewer.setLabelProvider(new PropertyAdapterLabelProvider(m_treeViewer));
    return new CheckboxTreeViewerWrapper(m_treeViewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void calculateFinish() {
    super.calculateFinish();
    if (getErrorMessage() == null && m_elProperty) {
      setErrorMessage(m_elPropertyUIContentProvider.getErrorMessage());
    }
  }

  private void calculateSubFinish() {
    if (m_elProperty) {
      setErrorMessage(m_elPropertyUIContentProvider.getErrorMessage());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void handleELProperty() {
    try {
      Object[] elements = m_treeViewer.getCheckedElements();
      if (m_elProperty) {
        if (elements.length == 1) {
          ObservePropertyAdapter adapter = (ObservePropertyAdapter) elements[0];
          if (adapter.getProperty() instanceof ElPropertyObserveInfo) {
            m_elPropertyUIContentProvider.setProperty((ElPropertyInfo) convertAdapterToProperty(
                new BeanSupport(),
                m_choosenClass,
                adapter));
            return;
          }
        }
        m_elProperty = false;
        m_elPropertyUIContentProvider.setProperty(null);
      } else if (elements.length == 1) {
        ObservePropertyAdapter adapter = (ObservePropertyAdapter) elements[0];
        if (adapter.getProperty() instanceof ElPropertyObserveInfo) {
          m_elProperty = true;
          m_elPropertyUIContentProvider.setProperty((ElPropertyInfo) convertAdapterToProperty(
              new BeanSupport(),
              m_choosenClass,
              adapter));
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  @Override
  protected List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
    List<PropertyAdapter> adapters = Lists.newArrayList();
    BeanSupport beanSupport = new BeanSupport();
    beanSupport.doAddELProperty(m_configuration.isWorkWithELProperty());
    ClassGenericType objectType = new ClassGenericType(choosenClass, null, null);
    for (ObserveInfo property : beanSupport.createProperties(null, objectType)) {
      adapters.add(new ObservePropertyAdapter(null, property));
    }
    return adapters;
  }

  private PropertyInfo convertAdapterToProperty(BeanSupport beanSupport,
      Class<?> objectClass,
      ObservePropertyAdapter adapter) throws Exception {
    ClassGenericType objectType = new ClassGenericType(objectClass, null, null);
    ObserveInfo observe =
        new SubBeanObserveInfo(beanSupport, null, objectType, StringReferenceProvider.EMPTY);
    return adapter.getProperty().createProperty(observe);
  }

  private ObservePropertyAdapter convertPropertyToAdapter(BeanSupport beanSupport,
      Class<?> objectClass,
      PropertyInfo property) throws Exception {
    ClassGenericType objectType = new ClassGenericType(objectClass, null, null);
    ObserveInfo observe =
        property.getObserveProperty(new SubBeanObserveInfo(beanSupport,
            null,
            objectType,
            StringReferenceProvider.EMPTY));
    Assert.isNotNull(observe);
    return convertObserveToAdapter(observe);
  }

  private ObservePropertyAdapter convertObserveToAdapter(ObserveInfo observe) throws Exception {
    if (observe != null) {
      ObservePropertyAdapter adapter =
          new ObservePropertyAdapter(convertObserveToAdapter((ObserveInfo) observe.getParent()),
              observe);
      adapter.addToParent();
      return adapter;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void setClassNameAndProperty(String className, PropertyInfo property)
      throws Exception {
    setClassName(className);
    if (m_configuration.isWorkWithELProperty()) {
      m_elProperty = property instanceof ElPropertyInfo;
      m_elPropertyUIContentProvider.setProperty(m_elProperty ? (ElPropertyInfo) property : null);
    }
    setCheckedAdExpand(new Object[]{convertPropertyToAdapter(
        new BeanSupport(),
        loadClass(className),
        property)});
    calculatePropertiesFinish();
  }

  protected final void setClassNameAndProperties0(String className, List<PropertyInfo> properties)
      throws Exception {
    setClassName(className);
    BeanSupport beanSupport = new BeanSupport();
    Class<?> objectClass = loadClass(className);
    Object[] adapters = new Object[properties.size()];
    for (int i = 0; i < adapters.length; i++) {
      adapters[i] = convertPropertyToAdapter(beanSupport, objectClass, properties.get(i));
    }
    setCheckedAdExpand(adapters);
    calculatePropertiesFinish();
  }

  private void setCheckedAdExpand(Object[] adapters) {
    for (int i = 0; i < adapters.length; i++) {
      m_treeViewer.expandToLevel(adapters[i], 0);
    }
    m_treeViewer.setCheckedElements(adapters);
    if (adapters.length > 0) {
      m_treeViewer.setSelection(new StructuredSelection(adapters[0]), true);
    }
  }

  @Override
  protected final void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenAdapters)
      throws Exception {
    BeanSupport beanSupport = new BeanSupport();
    List<PropertyInfo> choosenProperties = Lists.newArrayList();
    if (m_elProperty) {
      m_elPropertyUIContentProvider.saveToObject();
      choosenProperties.add(m_elPropertyUIContentProvider.getProperty());
    } else {
      for (PropertyAdapter propertyAdapter : choosenAdapters) {
        choosenProperties.add(convertAdapterToProperty(
            beanSupport,
            choosenClass,
            (ObservePropertyAdapter) propertyAdapter));
      }
    }
    saveToObject0(choosenClass, choosenProperties);
  }

  /**
   * Invoke for save changes class and properties.
   */
  protected abstract void saveToObject0(Class<?> choosenClass, List<PropertyInfo> choosenProperties)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ObservePropertyAdapter extends PropertyAdapter {
    private final ObservePropertyAdapter m_parent;
    private final ObserveInfo m_property;
    private List<ObservePropertyAdapter> m_children;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public ObservePropertyAdapter(ObservePropertyAdapter parent, ObserveInfo property)
        throws Exception {
      super(property.getPresentation().getText(), property.getObjectClass());
      m_parent = parent;
      m_property = property;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public ObservePropertyAdapter getParent() {
      return m_parent;
    }

    public ObserveInfo getProperty() {
      return m_property;
    }

    public List<ObservePropertyAdapter> getChildren() {
      if (m_children == null) {
        m_children = Lists.newArrayList();
        List<ObserveInfo> properties =
            CoreUtils.cast(m_property.getChildren(ChildrenContext.ChildrenForPropertiesTable));
        for (ObserveInfo property : properties) {
          try {
            m_children.add(new ObservePropertyAdapter(this, property));
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      }
      return m_children;
    }

    public void addToParent() {
      if (m_parent != null) {
        m_parent.m_children = Lists.newArrayList();
        m_parent.m_children.add(this);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Object
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public int hashCode() {
      int parentHash = m_parent == null ? 1 : m_parent.hashCode();
      return parentHash * super.hashCode();
    }

    @Override
    public boolean equals(Object object) {
      // self check
      if (object == this) {
        return true;
      }
      // compare with other adapter
      if (object instanceof ObservePropertyAdapter) {
        ObservePropertyAdapter adapter = (ObservePropertyAdapter) object;
        if (m_parent == null && adapter.m_parent == null) {
        } else if (m_parent != null
            && adapter.m_parent == null
            || m_parent == null
            && adapter.m_parent != null) {
          return false;
        } else if (m_parent != null && adapter.m_parent != null) {
          if (!m_parent.equals(adapter.m_parent)) {
            return false;
          }
        }
        return m_name.equals(adapter.m_name) && m_type == adapter.m_type;
      }
      // default
      return false;
    }
  }
  private static class PropertyAdapterContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object input) {
      return ((List<?>) input).toArray();
    }

    public Object getParent(Object element) {
      return getAdapter(element).getParent();
    }

    public boolean hasChildren(Object element) {
      return !getAdapter(element).getChildren().isEmpty();
    }

    public Object[] getChildren(Object element) {
      return getAdapter(element).getChildren().toArray();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }
  }
  private static class PropertyAdapterLabelProvider extends LabelProvider
      implements
        IColorProvider,
        IFontProvider {
    private final ObserveDecoratingLabelProvider m_labelProvider;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PropertyAdapterLabelProvider(TreeViewer viewer) {
      m_labelProvider = new ObserveDecoratingLabelProvider(viewer);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // LabelProvider
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void dispose() {
      super.dispose();
      m_labelProvider.dispose();
    }

    @Override
    public String getText(Object element) {
      return getAdapter(element).getName();
    }

    @Override
    public Image getImage(Object element) {
      try {
        return getAdapterProperty(element).getPresentation().getImage();
      } catch (Throwable e) {
      }
      return super.getImage(element);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Decoration
    //
    ////////////////////////////////////////////////////////////////////////////
    public Color getForeground(Object element) {
      return m_labelProvider.getForeground(getAdapterProperty(element));
    }

    public Color getBackground(Object element) {
      return m_labelProvider.getBackground(getAdapterProperty(element));
    }

    public Font getFont(Object element) {
      return m_labelProvider.getFont(getAdapterProperty(element));
    }
  }

  private static ObservePropertyAdapter getAdapter(Object element) {
    return (ObservePropertyAdapter) element;
  }

  private static ObserveInfo getAdapterProperty(Object element) {
    return getAdapter(element).getProperty();
  }

  private static class SubBeanObserveInfo extends BeanObserveInfo {
    public SubBeanObserveInfo(BeanSupport beanSupport,
        ObserveInfo parent,
        IGenericType objectType,
        IReferenceProvider referenceProvider) {
      super(beanSupport, parent, objectType, referenceProvider);
    }

    public IObservePresentation getPresentation() {
      return null;
    }
  }
}