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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.parser.ISubParser;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.BeansObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.PropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansListObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansSetObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Parser for bindings input for <code>JFace</code> viewers.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ViewerInputParser implements ISubParser {
  private static final String VIEWER_SUPPORT_LIST_SIGNATURE =
      "org.eclipse.jface.databinding.viewers.ViewerSupport.bind(org.eclipse.jface.viewers.StructuredViewer,org.eclipse.core.databinding.observable.list.IObservableList,org.eclipse.core.databinding.property.value.IValueProperty[])";
  private static final String VIEWER_SUPPORT_SET_SIGNATURE =
      "org.eclipse.jface.databinding.viewers.ViewerSupport.bind(org.eclipse.jface.viewers.StructuredViewer,org.eclipse.core.databinding.observable.set.IObservableSet,org.eclipse.core.databinding.property.value.IValueProperty[])";
  private static final String BEAN_PROPERTIES_VALUES =
      "org.eclipse.core.databinding.beans.BeanProperties.values(java.lang.Class,java.lang.String[])";
  private static final String POJO_PROPERTIES_VALUES =
      "org.eclipse.core.databinding.beans.PojoProperties.values(java.lang.Class,java.lang.String[])";
  private static final String OBSERVABLE_VALUE_EDITING_SUPPORT =
      "org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport.create(org.eclipse.jface.viewers.ColumnViewer,org.eclipse.core.databinding.DataBindingContext,org.eclipse.jface.viewers.CellEditor,org.eclipse.core.databinding.property.value.IValueProperty,org.eclipse.core.databinding.property.value.IValueProperty)";
  private static final String VIEWER_COLUMN_SET_EDITING_SUPPORT =
      "org.eclipse.jface.viewers.ViewerColumn.setEditingSupport(org.eclipse.jface.viewers.EditingSupport)";
  private static final String CELL_EDITOR_PROPERTIES_CONTROL =
      "org.eclipse.jface.databinding.viewers.CellEditorProperties.control()";
  //
  private final Map<WidgetBindableInfo, AbstractViewerInputBindingInfo> m_viewers =
      Maps.newHashMap();
  private final DataBindingContextInfo m_contextInfo;
  private final BeansObserveTypeContainer m_beansContainer;
  private final WidgetsObserveTypeContainer m_widgetsContainer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerInputParser(DataBindingContextInfo contextInfo, DatabindingsProvider provider) {
    m_contextInfo = contextInfo;
    m_beansContainer = (BeansObserveTypeContainer) provider.getContainer(ObserveType.BEANS);
    m_widgetsContainer = (WidgetsObserveTypeContainer) provider.getContainer(ObserveType.WIDGETS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    ITypeBinding binding = AstNodeUtils.getTypeBinding(creation);
    if (binding == null) {
      return null;
    }
    String className = AstNodeUtils.getFullyQualifiedName(binding, false);
    //
    // IObservableFactory
    //
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.core.databinding.observable.masterdetail.IObservableFactory")) {
      // new BeansListObservableFactory(Class, String)
      //
      if (AstNodeUtils.isSuccessorOf(
          binding,
          "org.eclipse.wb.rcp.databinding.BeansListObservableFactory")) {
        Assert.isTrue(signature.endsWith("<init>(java.lang.Class,java.lang.String)"));
        // create factory
        BeansListObservableFactoryInfo observableFactory =
            new BeansListObservableFactoryInfo(className);
        // prepare element type
        observableFactory.setElementType(CoreUtils.evaluate(Class.class, editor, arguments[0]));
        // prepare property
        observableFactory.setPropertyName(CoreUtils.evaluate(String.class, editor, arguments[1]));
        //
        return observableFactory;
      }
      //
      // new BeansSetObservableFactory(Class, String)
      //
      if (AstNodeUtils.isSuccessorOf(
          binding,
          "org.eclipse.wb.rcp.databinding.BeansSetObservableFactory")) {
        Assert.isTrue(signature.endsWith("<init>(java.lang.Class,java.lang.String)"));
        // create factory
        BeansSetObservableFactoryInfo observableFactory =
            new BeansSetObservableFactoryInfo(className);
        // prepare element type
        observableFactory.setElementType(CoreUtils.evaluate(Class.class, editor, arguments[0]));
        // prepare property
        observableFactory.setPropertyName(CoreUtils.evaluate(String.class, editor, arguments[1]));
        //
        return observableFactory;
      }
      // create default factory
      Assert.isTrue(signature.endsWith("<init>()"));
      return new ObservableFactoryInfo(className);
    }
    //
    // TreeStructureAdvisor
    //
    // new TreeBeanAdvisor(Class, String, String, String)
    if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.wb.rcp.databinding.TreeBeanAdvisor")) {
      Assert.isTrue(signature.endsWith("<init>(java.lang.Class,java.lang.String,java.lang.String,java.lang.String)"));
      // create advisor
      TreeBeanAdvisorInfo advisorInfo = new TreeBeanAdvisorInfo();
      // prepare element type
      advisorInfo.setElementType(CoreUtils.evaluate(Class.class, editor, arguments[0]));
      // prepare parent property
      advisorInfo.setParentProperty(CoreUtils.evaluate(String.class, editor, arguments[1]));
      // prepare children property
      advisorInfo.setChildrenProperty(CoreUtils.evaluate(String.class, editor, arguments[2]));
      // prepare hasChildren property
      advisorInfo.setHasChildrenProperty(CoreUtils.evaluate(String.class, editor, arguments[3]));
      //
      return advisorInfo;
    }
    //
    // new TreeStructureAdvisor()
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.jface.databinding.viewers.TreeStructureAdvisor")) {
      Assert.isTrue(signature.endsWith("<init>()"));
      return new TreeStructureAdvisorInfo(className);
    }
    //
    // Content providers
    //
    // new ObservableListContentProvider()
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.jface.databinding.viewers.ObservableListContentProvider")) {
      return new ObservableListContentProviderInfo(className);
    }
    // new ObservableSetContentProvider()
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.jface.databinding.viewers.ObservableSetContentProvider")) {
      return new ObservableSetContentProviderInfo(className);
    }
    // new ObservableListTreeContentProvider(IObservableFactory, TreeStructureAdvisor)
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider")) {
      Assert.isTrue(signature.endsWith("<init>(org.eclipse.core.databinding.observable.masterdetail.IObservableFactory,org.eclipse.jface.databinding.viewers.TreeStructureAdvisor)"));
      // prepare factory
      ObservableFactoryInfo observableFactory =
          (ObservableFactoryInfo) resolver.getModel(arguments[0]);
      if (observableFactory == null) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_objectFactoryArgumentNotFound,
            arguments[0]), new Throwable());
        return null;
      }
      // prepare advisor
      TreeStructureAdvisorInfo advisorInfo =
          (TreeStructureAdvisorInfo) resolver.getModel(arguments[1]);
      if (advisorInfo == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_advisorArgumentNotFound, arguments[1]),
            new Throwable());
        return null;
      }
      // create content provider
      return new ObservableListTreeContentProviderInfo(className, observableFactory, advisorInfo);
    }
    // new ObservableSetTreeContentProvider(IObservableFactory, TreeStructureAdvisor)
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider")) {
      Assert.isTrue(signature.endsWith("<init>(org.eclipse.core.databinding.observable.masterdetail.IObservableFactory,org.eclipse.jface.databinding.viewers.TreeStructureAdvisor)"));
      // prepare factory
      ObservableFactoryInfo observableFactory =
          (ObservableFactoryInfo) resolver.getModel(arguments[0]);
      if (observableFactory == null) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_objectFactoryArgumentNotFound,
            arguments[0]), new Throwable());
        return null;
      }
      // prepare advisor
      TreeStructureAdvisorInfo advisorInfo =
          (TreeStructureAdvisorInfo) resolver.getModel(arguments[1]);
      if (advisorInfo == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_advisorArgumentNotFound, arguments[1]),
            new Throwable());
        return null;
      }
      // create content provider
      return new ObservableSetTreeContentProviderInfo(className, observableFactory, advisorInfo);
    }
    //
    // Label providers
    //
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider")
        || AstNodeUtils.isSuccessorOf(
            binding,
            "org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider")) {
      Assert.isTrue(signature.endsWith("<init>(org.eclipse.core.databinding.observable.map.IObservableMap[])")
          || signature.endsWith("<init>(org.eclipse.core.databinding.observable.map.IObservableMap)"));
      // prepare maps observable
      MapsBeanObservableInfo mapsObservable =
          (MapsBeanObservableInfo) resolver.getModel(arguments[0]);
      if (mapsObservable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_argumentNotFound, arguments[0]),
            new Throwable());
        return null;
      }
      // create label provider
      return new ObservableMapLabelProviderInfo(className, mapsObservable);
    }
    if (AstNodeUtils.isSuccessorOf(
        binding,
        "org.eclipse.wb.rcp.databinding.TreeObservableLabelProvider")) {
      Assert.isTrue(signature.endsWith("<init>(org.eclipse.core.databinding.observable.set.IObservableSet,java.lang.Class,java.lang.String,java.lang.String)"));
      // prepare allElements observable
      KnownElementsObservableInfo allElementsObservable =
          (KnownElementsObservableInfo) resolver.getModel(arguments[0]);
      if (allElementsObservable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_argumentNotFound, arguments[0]),
            new Throwable());
        return null;
      }
      // create label provider
      TreeObservableLabelProviderInfo labelProvider =
          new TreeObservableLabelProviderInfo(className, allElementsObservable);
      // prepare element type
      labelProvider.setElementType(CoreUtils.evaluate(Class.class, editor, arguments[1]));
      // prepare text property
      labelProvider.setTextProperty(CoreUtils.evaluate(String.class, editor, arguments[2]));
      // prepare image property
      labelProvider.setImageProperty(CoreUtils.evaluate(String.class, editor, arguments[3]));
      //
      return labelProvider;
    }
    // default label provider
    if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.jface.viewers.IBaseLabelProvider")) {
      Assert.isTrue(signature.endsWith("<init>()"));
      return new LabelProviderInfo(className);
    }
    //
    // CellEditor
    //
    if (AstNodeUtils.isSuccessorOf(binding, "org.eclipse.jface.viewers.CellEditor")) {
      return new CellEditorInfo(editor, creation, className);
    }
    //
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    if (signature.endsWith("setContentProvider(org.eclipse.jface.viewers.IContentProvider)")) {
      // prepare viewer
      WidgetBindableInfo viewerBindable =
          m_widgetsContainer.getBindableWidget(invocation.getExpression());
      if (viewerBindable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_viewerNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      // prepare binding
      AbstractViewerInputBindingInfo viewerBinding = getViewerBindingInfo(viewerBindable);
      if (viewerBinding == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_modelViewerNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      //
      if (viewerBinding instanceof TreeViewerInputBindingInfo) {
        // prepare content provider
        ObservableCollectionTreeContentProviderInfo contentProvider =
            (ObservableCollectionTreeContentProviderInfo) resolver.getModel(arguments[0]);
        if (contentProvider == null) {
          AbstractParser.addError(
              editor,
              MessageFormat.format(Messages.ViewerInputParser_contentProviderNotFound, arguments[0]),
              new Throwable());
          return null;
        }
        TreeViewerInputBindingInfo viewerInputBinding = (TreeViewerInputBindingInfo) viewerBinding;
        viewerInputBinding.setContentProvider(contentProvider);
      } else if (viewerBinding instanceof ViewerInputBindingInfo) {
        // prepare content provider
        ObservableCollectionContentProviderInfo contentProvider =
            (ObservableCollectionContentProviderInfo) resolver.getModel(arguments[0]);
        if (contentProvider == null) {
          AbstractParser.addError(
              editor,
              MessageFormat.format(Messages.ViewerInputParser_contentProviderNotFound, arguments[0]),
              new Throwable());
          return null;
        }
        ViewerInputBindingInfo viewerInputBinding = (ViewerInputBindingInfo) viewerBinding;
        viewerInputBinding.setContentProvider(contentProvider);
      } else {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_contentProviderNotFound, arguments[0]),
            new Throwable());
        return null;
      }
    } else if (signature.endsWith("setLabelProvider(org.eclipse.jface.viewers.IBaseLabelProvider)")) {
      // prepare viewer
      WidgetBindableInfo viewerBindable =
          m_widgetsContainer.getBindableWidget(invocation.getExpression());
      if (viewerBindable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_viewerNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      // prepare binding
      AbstractViewerInputBindingInfo viewerBinding = getViewerBindingInfo(viewerBindable);
      if (viewerBinding == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_modelViewerNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      //
      if (viewerBinding instanceof ViewerInputBindingInfo) {
        // prepare label provider
        ObservableMapLabelProviderInfo labelProvider =
            (ObservableMapLabelProviderInfo) resolver.getModel(arguments[0]);
        if (labelProvider == null) {
          AbstractParser.addError(editor, MessageFormat.format(
              Messages.ViewerInputParser_labelProviderArgumentNotFound,
              arguments[0]), new Throwable());
          //
          m_viewers.remove(viewerBindable);
          m_contextInfo.getBindings().remove(viewerBinding);
          //
          return null;
        }
        // sets label provider
        ViewerInputBindingInfo viewerInputBinding = (ViewerInputBindingInfo) viewerBinding;
        viewerInputBinding.setLabelProvider(labelProvider);
      } else if (viewerBinding instanceof TreeViewerInputBindingInfo) {
        // prepare label provider
        AbstractLabelProviderInfo labelProvider =
            (AbstractLabelProviderInfo) resolver.getModel(arguments[0]);
        if (labelProvider == null) {
          AbstractParser.addError(editor, MessageFormat.format(
              Messages.ViewerInputParser_labelProviderArgumentNotFound,
              arguments[0]), new Throwable());
          //
          m_viewers.remove(viewerBindable);
          m_contextInfo.getBindings().remove(viewerBinding);
          //
          return null;
        }
        // sets label provider
        TreeViewerInputBindingInfo viewerInputBinding = (TreeViewerInputBindingInfo) viewerBinding;
        viewerInputBinding.setLabelProvider(labelProvider);
      } else {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_labelProviderArgumentNotFound,
            arguments[0]), new Throwable());
        return null;
      }
    } else if (signature.endsWith("setInput(java.lang.Object)")) {
      // prepare viewer
      WidgetBindableInfo viewerBindable =
          m_widgetsContainer.getBindableWidget(invocation.getExpression());
      if (viewerBindable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_viewerNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      // prepare binding
      AbstractViewerInputBindingInfo viewerBinding = m_viewers.get(viewerBindable);
      if (viewerBinding == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_viewerInputModelNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      // prepare input observable
      ObservableInfo inputObservable = (ObservableInfo) resolver.getModel(arguments[0]);
      if (inputObservable == null) {
        //
        if (viewerBinding instanceof TreeViewerInputBindingInfo) {
          BeanBindableInfo bindableObject = m_beansContainer.getBindableObject(arguments[0]);
          if (bindableObject != null) {
            TreeViewerInputBindingInfo treeViewerBinding =
                (TreeViewerInputBindingInfo) viewerBinding;
            ObservableFactoryInfo factoryInfo =
                treeViewerBinding.getContentProvider().getFactoryInfo();
            //
            if (factoryInfo instanceof BeansObservableFactoryInfo) {
              BeansObservableFactoryInfo beansFactoryInfo =
                  (BeansObservableFactoryInfo) factoryInfo;
              String propertyName = beansFactoryInfo.getPropertyName();
              PropertyBindableInfo propertyObject =
                  propertyName == null
                      ? null
                      : bindableObject.resolvePropertyReference(propertyName);
              //
              if (propertyObject != null) {
                inputObservable = new BeanFieldInputObservableInfo(bindableObject, propertyObject);
              }
            }
          }
        }
        //
        if (inputObservable == null) {
          AbstractParser.addError(editor, MessageFormat.format(
              Messages.ViewerInputParser_viewerInputArgumentNotFound,
              arguments[0]), new Throwable());
          //
          m_viewers.remove(viewerBindable);
          m_contextInfo.getBindings().remove(viewerBinding);
          //
          return null;
        }
      }
      // sets input observable
      viewerBinding.setInputObservable(inputObservable);
    } else if (VIEWER_SUPPORT_LIST_SIGNATURE.equals(signature)
        || VIEWER_SUPPORT_SET_SIGNATURE.equals(signature)) {
      // prepare viewer
      WidgetBindableInfo viewerBindable = m_widgetsContainer.getBindableWidget(arguments[0]);
      if (viewerBindable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_viewerNotFound, arguments[0]),
            new Throwable());
        return null;
      }
      if (m_viewers.get(viewerBindable) != null) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_viewerDoubleInvocation,
            invocation,
            arguments[0]), new Throwable());
        return null;
      }
      // prepare input observable
      ObservableInfo inputObservable = (ObservableInfo) resolver.getModel(arguments[1]);
      if (inputObservable == null) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_viewerInputArgumentNotFound,
            arguments[1]), new Throwable());
        return null;
      }
      // prepare label provider properties
      Assert.instanceOf(MethodInvocation.class, arguments[2]);
      MethodInvocation labelPropertiesMethod = (MethodInvocation) arguments[2];
      String labelPropertiesSignature = CoreUtils.getMethodSignature(labelPropertiesMethod);
      Assert.isTrue(BEAN_PROPERTIES_VALUES.equals(labelPropertiesSignature)
          || POJO_PROPERTIES_VALUES.equals(labelPropertiesSignature));
      List<Expression> labelPropertiesArguments = DomGenerics.arguments(labelPropertiesMethod);
      // prepare element type
      Class<?> elementType =
          CoreUtils.evaluate(Class.class, editor, labelPropertiesArguments.get(0));
      // prepare properties
      String[] properties =
          CoreUtils.evaluate(String[].class, editor, labelPropertiesArguments.get(1));
      // create binding
      ViewerInputBindingInfo viewerBinding = new ViewerInputBindingInfo(viewerBindable);
      viewerBinding.setInputObservable(inputObservable);
      viewerBinding.setDefaultProviders(
          VIEWER_SUPPORT_LIST_SIGNATURE.equals(signature),
          elementType,
          true);
      viewerBinding.getLabelProvider().getMapsObservable().setProperties(properties);
      // add binding
      m_contextInfo.getBindings().add(viewerBinding);
      m_viewers.put(viewerBindable, viewerBinding);
    } else if (OBSERVABLE_VALUE_EDITING_SUPPORT.equals(signature)) {
      // prepare viewer
      WidgetBindableInfo viewerBindable = m_widgetsContainer.getBindableWidget(arguments[0]);
      if (viewerBindable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_viewerNotFound, arguments[0]),
            new Throwable());
        return null;
      }
      //
      AbstractViewerInputBindingInfo viewerBinding = m_viewers.get(viewerBindable);
      if (viewerBinding == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_viewerNotFound, arguments[0]),
            new Throwable());
        return null;
      }
      // prepare context
      DataBindingContextInfo bindingContextInfo =
          (DataBindingContextInfo) resolver.getModel(arguments[1]);
      if (bindingContextInfo != m_contextInfo) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_undefinedDataBindingContext,
            arguments[1]), new Throwable());
        return null;
      }
      // prepare CellEditor
      CellEditorInfo cellEditorInfo = (CellEditorInfo) resolver.getModel(arguments[2]);
      if (cellEditorInfo == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_cellEditorNotFound, arguments[2]),
            new Throwable());
        return null;
      }
      // prepare CellEditor property
      CellEditorValuePropertyCodeSupport cellEditorProperty = null;
      AstObjectInfo cellEditorPropertyInfo = resolver.getModel(arguments[3]);
      //
      if (cellEditorPropertyInfo instanceof CellEditorValuePropertyCodeSupport) {
        cellEditorProperty = (CellEditorValuePropertyCodeSupport) cellEditorPropertyInfo;
      } else if (cellEditorPropertyInfo instanceof ValuePropertyCodeSupport) {
        ValuePropertyCodeSupport value = (ValuePropertyCodeSupport) cellEditorPropertyInfo;
        cellEditorProperty = new CellEditorValuePropertyCodeSupport(value);
      }
      //
      if (cellEditorProperty == null) {
        AbstractParser.addError(editor, MessageFormat.format(
            Messages.ViewerInputParser_cellEditorPropertyNotFound,
            arguments[3]), new Throwable());
        return null;
      }
      // prepare element property
      ValuePropertyCodeSupport elementProperty =
          (ValuePropertyCodeSupport) resolver.getModel(arguments[4]);
      if (elementProperty == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_elementPropertyNotFound, arguments[4]),
            new Throwable());
        return null;
      }
      //
      return new EditingSupportInfo(viewerBinding,
          cellEditorInfo,
          cellEditorProperty,
          elementProperty);
    } else if (VIEWER_COLUMN_SET_EDITING_SUPPORT.equals(signature)) {
      // prepare viewer column
      WidgetBindableInfo viewerColumnBindable =
          m_widgetsContainer.getBindableWidget(invocation.getExpression());
      if (viewerColumnBindable == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(
                Messages.ViewerInputParser_viewerColumnNotFound,
                invocation.getExpression()),
            new Throwable());
        return null;
      }
      // prepare support
      EditingSupportInfo editingSupportInfo = (EditingSupportInfo) resolver.getModel(arguments[0]);
      if (editingSupportInfo == null) {
        AbstractParser.addError(
            editor,
            MessageFormat.format(Messages.ViewerInputParser_editingSupportNotFound, arguments[0]),
            new Throwable());
        return null;
      }
      //
      editingSupportInfo.setViewerColumn(viewerColumnBindable);
    } else if (CELL_EDITOR_PROPERTIES_CONTROL.equals(signature)) {
      // CellEditor
      return new CellEditorControlPropertyCodeSupport();
    }
    return null;
  }

  private AbstractViewerInputBindingInfo getViewerBindingInfo(WidgetBindableInfo viewerBindable)
      throws Exception {
    AbstractViewerInputBindingInfo viewerBinding = m_viewers.get(viewerBindable);
    if (viewerBinding == null) {
      // create binding
      if (ReflectionUtils.isSuccessorOf(
          viewerBindable.getObjectType(),
          "org.eclipse.jface.viewers.AbstractTreeViewer")) {
        viewerBinding = new TreeViewerInputBindingInfo(viewerBindable);
      } else {
        viewerBinding = new ViewerInputBindingInfo(viewerBindable);
      }
      // add binding
      m_contextInfo.getBindings().add(viewerBinding);
      m_viewers.put(viewerBindable, viewerBinding);
    }
    return viewerBinding;
  }
}