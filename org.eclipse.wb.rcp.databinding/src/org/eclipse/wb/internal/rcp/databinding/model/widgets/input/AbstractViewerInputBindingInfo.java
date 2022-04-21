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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.CollectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ViewerObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.InputElementUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.TreeInputElementUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ViewerColumnsConfiguration;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.ViewerColumnsUiContentProvider;
import org.eclipse.wb.internal.rcp.model.jface.viewers.ViewerColumnInfo;
import org.eclipse.wb.internal.swt.model.widgets.ItemInfo;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract model for binding input to <code>JFace</code> viewer.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class AbstractViewerInputBindingInfo extends AbstractBindingInfo {
  protected final WidgetBindableInfo m_viewerBindable;
  protected final WidgetPropertyBindableInfo m_viewerBindableProperty;
  protected ObservableInfo m_inputObservable;
  private CodeSupport m_codeSupport;
  private final boolean m_isColumnViewer;
  private final List<EditingSupportInfo> m_editingSupports;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractViewerInputBindingInfo(WidgetBindableInfo viewerBindable) throws Exception {
    this(viewerBindable, viewerBindable.resolvePropertyReference("setInput"));
    Assert.isNotNull(m_viewerBindableProperty);
  }

  public AbstractViewerInputBindingInfo(WidgetBindableInfo viewerBindable,
      WidgetPropertyBindableInfo viewerBindableProperty) throws Exception {
    m_viewerBindable = viewerBindable;
    m_viewerBindableProperty = viewerBindableProperty;
    //
    m_isColumnViewer = isColumnViewer(m_viewerBindable);
    m_editingSupports = m_isColumnViewer ? new ArrayList<EditingSupportInfo>() : null;
  }

  private static final boolean isColumnViewer(WidgetBindableInfo bindableWidget) throws Exception {
    return bindableWidget.getClassLoader().loadClass("org.eclipse.jface.viewers.ColumnViewer").isAssignableFrom(
        bindableWidget.getObjectType());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<EditingSupportInfo> getEditingSupports() {
    return m_editingSupports;
  }

  public final CodeSupport getCodeSupport() {
    return m_codeSupport;
  }

  public final void setCodeSupport(CodeSupport codeSupport) {
    m_codeSupport = codeSupport;
  }

  public final ObservableInfo getInputObservable() {
    return m_inputObservable;
  }

  public final void setInputObservable(ObservableInfo inputObservable) {
    m_inputObservable = inputObservable;
  }

  public final WidgetBindableInfo getViewer() {
    return m_viewerBindable;
  }

  /**
   * Sets providers for new binding (during edit time).
   */
  public final void setNewInputObservable(ObservableInfo input,
      DatabindingsProvider provider,
      boolean useViewerSupport) throws Exception {
    m_inputObservable = input;
    // calculate element type
    Class<?> elementType = null;
    //
    if (m_inputObservable instanceof DetailBeanObservableInfo) {
      DetailBeanObservableInfo inputObservable = (DetailBeanObservableInfo) m_inputObservable;
      elementType = inputObservable.getDetailPropertyType();
    } else if (m_inputObservable instanceof ViewerObservableInfo) {
      elementType = getViewerInutElementType(m_inputObservable, provider);
    }
    // sets providers
    if (m_inputObservable instanceof DetailBeanObservableInfo) {
      setDefaultProviders(true, elementType, useViewerSupport);
    } else {
      setDefaultProviders(
          List.class.isAssignableFrom(m_inputObservable.getBindableProperty().getObjectType()),
          elementType,
          useViewerSupport);
    }
  }

  /**
   * Sets default providers for new binding.
   */
  public abstract void setDefaultProviders(boolean asList,
      Class<?> elementType,
      boolean useViewerSupport) throws Exception;

  /**
   * @return {@link Class} of element into viewer input.
   */
  public Class<?> getElementType() {
    if (m_inputObservable instanceof DetailBeanObservableInfo) {
      DetailBeanObservableInfo inputObservable = (DetailBeanObservableInfo) m_inputObservable;
      return inputObservable.getDetailPropertyType();
    }
    if (m_inputObservable instanceof CheckedElementsObservableInfo) {
      CheckedElementsObservableInfo inputObservable =
          (CheckedElementsObservableInfo) m_inputObservable;
      return inputObservable.getElementType();
    }
    if (m_inputObservable instanceof CollectionObservableInfo) {
      CollectionObservableInfo inputObservable = (CollectionObservableInfo) m_inputObservable;
      return inputObservable.getElementType();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Definition
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getDefinitionSource(DatabindingsProvider provider) throws Exception {
    return m_viewerBindable.getReference() + ".setInput";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IBindingInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final IObserveInfo getTarget() {
    return m_viewerBindable;
  }

  @Override
  public final IObserveInfo getTargetProperty() {
    return m_viewerBindableProperty;
  }

  @Override
  public final IObserveInfo getModel() {
    return m_inputObservable.getBindableObject();
  }

  @Override
  public final IObserveInfo getModelProperty() {
    return m_inputObservable.getBindableProperty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    if (m_isColumnViewer) {
      List<WidgetBindableInfo> viewerColumns = Lists.newArrayList();
      // prepare viewer columns
      WidgetBindableInfo viewerControlBindable = (WidgetBindableInfo) m_viewerBindable.getParent();
      for (IObserveInfo observe : viewerControlBindable.getChildren(ChildrenContext.ChildrenForMasterTable)) {
        WidgetBindableInfo widgetBindable = (WidgetBindableInfo) observe;
        // extract columns
        if (widgetBindable.getJavaInfo() instanceof ItemInfo) {
          for (IObserveInfo subObserve : widgetBindable.getChildren(ChildrenContext.ChildrenForMasterTable)) {
            WidgetBindableInfo subWidgetBindable = (WidgetBindableInfo) subObserve;
            // extract viewer columns
            if (subWidgetBindable.getJavaInfo() instanceof ViewerColumnInfo) {
              viewerColumns.add(subWidgetBindable);
            }
          }
        }
      }
      //
      if (!viewerColumns.isEmpty()) {
        // prepare element type provider
        VirtualEditingSupportInfo.IElementTypeProvider elementTypeProvider = null;
        for (IUiContentProvider contentProvider : providers) {
          // wrap Viewer editor
          if (contentProvider instanceof InputElementUiContentProvider) {
            final InputElementUiContentProvider inputElementContentProvider =
                (InputElementUiContentProvider) contentProvider;
            elementTypeProvider = new VirtualEditingSupportInfo.IElementTypeProvider() {
              @Override
              public Class<?> getElementType() throws Exception {
                return inputElementContentProvider.getChoosenClass();
              }
            };
            break;
          }
          // wrap TreeViewer editor
          if (contentProvider instanceof TreeInputElementUiContentProvider) {
            final TreeInputElementUiContentProvider inputElementContentProvider =
                (TreeInputElementUiContentProvider) contentProvider;
            elementTypeProvider = new VirtualEditingSupportInfo.IElementTypeProvider() {
              @Override
              public Class<?> getElementType() throws Exception {
                return inputElementContentProvider.getCurrentElementType();
              }
            };
            break;
          }
        }
        // prepare configuration
        ViewerColumnsConfiguration configuration =
            new ViewerColumnsConfiguration(this,
                elementTypeProvider,
                viewerColumns,
                m_editingSupports);
        // prepare provider
        providers.add(new ViewerColumnsUiContentProvider(configuration));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return m_viewerBindable.getPresentation().getTextForBinding()
        + "."
        + m_viewerBindableProperty.getPresentation().getTextForBinding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void postParse() throws Exception {
    super.postParse();
    Assert.isNotNull(m_inputObservable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Generate source code association with this object and add to <code>lines</code>.
   */
  protected final void addSourceCode(DataBindingContextInfo context,
      List<String> lines,
      CodeGenerationSupport generationSupport,
      ObservableCollectionContentProviderInfo contentProvider,
      AbstractLabelProviderInfo labelProvider) throws Exception {
    if (m_codeSupport == null) {
      // prepare viewer reference
      String viewerReference = m_viewerBindable.getReference();
      // define content provider
      contentProvider.addSourceCode(lines, generationSupport);
      // add set label provider code
      lines.add(viewerReference
          + ".setLabelProvider("
          + labelProvider.getSourceCode(lines, generationSupport)
          + ");");
      // add set content provider code
      lines.add(viewerReference
          + ".setContentProvider("
          + contentProvider.getVariableIdentifier()
          + ");");
      lines.add("//");
      // add input source code
      generationSupport.addSourceCode(m_inputObservable, lines);
      lines.add(viewerReference + ".setInput(" + m_inputObservable.getVariableIdentifier() + ");");
    } else {
      m_codeSupport.addSourceCode(lines, generationSupport);
    }
    // add editing support objects
    if (!CollectionUtils.isEmpty(m_editingSupports)) {
      for (EditingSupportInfo editingSupport : m_editingSupports) {
        lines.add("//");
        editingSupport.addSourceCode(context, lines, generationSupport);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void accept(AstObjectInfoVisitor visitor,
      ObservableCollectionContentProviderInfo contentProvider,
      AbstractLabelProviderInfo labelProvider) throws Exception {
    super.accept(visitor);
    contentProvider.accept(visitor);
    labelProvider.accept(visitor);
    m_inputObservable.accept(visitor);
    if (m_codeSupport != null) {
      m_codeSupport.accept(visitor);
    }
    if (!CollectionUtils.isEmpty(m_editingSupports)) {
      for (EditingSupportInfo editingSupport : m_editingSupports) {
        editingSupport.accept(visitor);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IBindingInfo} for <code>JFace</code> viewer input.
   */
  public static IBindingInfo createBinding(BindableInfo targetBindable,
      BindableInfo targetBindableProperty,
      BindableInfo modelBindable,
      BindableInfo modelBindableProperty,
      DatabindingsProvider provider) throws Exception {
    // prepare observable factories
    IObservableFactory targetFactory = targetBindableProperty.getObservableFactory();
    IObservableFactory modelFactory = modelBindableProperty.getObservableFactory();
    // check create over "target"
    if (targetFactory.getType() == Type.Input) {
      return createBinding(
          targetBindable,
          targetBindableProperty,
          modelBindable,
          modelBindableProperty,
          modelFactory,
          provider);
    }
    // create over "model"
    return createBinding(
        modelBindable,
        modelBindableProperty,
        targetBindable,
        targetBindableProperty,
        targetFactory,
        provider);
  }

  private static IBindingInfo createBinding(BindableInfo bindable,
      BindableInfo bindableProperty,
      BindableInfo inputBindable,
      BindableInfo inputBindableProperty,
      IObservableFactory inputFactory,
      DatabindingsProvider provider) throws Exception {
    // prepare viewer
    WidgetBindableInfo viewerBindable = (WidgetBindableInfo) bindable;
    // prepare property
    WidgetPropertyBindableInfo viewerBindableProperty =
        (WidgetPropertyBindableInfo) bindableProperty;
    // create binding
    Class<?> treeViewerClass =
        viewerBindable.getClassLoader().loadClass("org.eclipse.jface.viewers.AbstractTreeViewer");
    //
    AbstractViewerInputBindingInfo binding =
        treeViewerClass.isAssignableFrom(viewerBindable.getObjectType())
            ? new TreeViewerInputBindingInfo(viewerBindable, viewerBindableProperty)
            : new ViewerInputBindingInfo(viewerBindable, viewerBindableProperty);
    // calculate type
    Type type = inputFactory.getType();
    switch (type) {
      case Detail :
      case List :
        type = Type.OnlyList;
        break;
      case Set :
        type = Type.OnlySet;
        break;
    }
    //
    boolean useViewerSupport =
        Activator.getStore().getBoolean(IPreferenceConstants.USE_VIEWER_SUPPORT);
    // sets default providers
    binding.setNewInputObservable(
        provider.createObservable(inputFactory, inputBindable, inputBindableProperty, type),
        provider,
        useViewerSupport);
    return binding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link Class} of element into given viewer input or <code>null</code> if its not set.
   */
  public static Class<?> getViewerInutElementType(ObservableInfo observable,
      DatabindingsProvider provider) throws Exception {
    if (observable.getBindableObject() instanceof WidgetBindableInfo) {
      // prepare widget
      WidgetBindableInfo widget = (WidgetBindableInfo) observable.getBindableObject();
      // prepare input property
      WidgetPropertyBindableInfo property = widget.resolvePropertyReference("setInput");
      // find input binding
      for (AbstractBindingInfo binding : provider.getBindings0()) {
        if (binding.getTarget() == widget && binding.getTargetProperty() == property) {
          AbstractViewerInputBindingInfo viewerBinding = (AbstractViewerInputBindingInfo) binding;
          return viewerBinding.getElementType();
        }
      }
    }
    //
    return null;
  }
}