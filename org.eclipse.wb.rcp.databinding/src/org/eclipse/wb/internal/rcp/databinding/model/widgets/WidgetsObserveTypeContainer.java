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
package org.eclipse.wb.internal.rcp.databinding.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.ControllerSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.SwtProperties;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.FiltersObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.IDelayValueProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.ItemsSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertyCheckedElementsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertyFiltersCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertyMultiSelectionCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.ViewerPropertySingleSelectionCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertiesCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertyItemsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties.WidgetPropertyTextCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.CheckedElementsObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.FiltersObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.MultiSelectionObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SingleSelectionObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableItemsCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.standard.SwtObservableTextCodeSupport;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.ArrayUtils;

import java.util.Collections;
import java.util.List;

/**
 * Widgets container with type {@link ObserveType#WIDGETS}. Works on <code>SWT</code> widgets and
 * <code>JFace</code> viewers.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class WidgetsObserveTypeContainer extends ObserveTypeContainer {
  private static final String SWT_DELAY_OBSERVABLE_METHOD =
      "org.eclipse.jface.databinding.swt.SWTObservables.observeDelayedValue(int,org.eclipse.jface.databinding.swt.ISWTObservableValue)";
  private static final String TEXT_OBSERVABLE_METHOD_1 =
      "org.eclipse.jface.databinding.swt.SWTObservables.observeText(org.eclipse.swt.widgets.Control,int)";
  private static final String TEXT_OBSERVABLE_METHOD_2 =
      "org.eclipse.jface.databinding.swt.SWTObservables.observeText(org.eclipse.swt.widgets.Control,int[])";
  private static final String TEXT_OBSERVABLE_METHOD_3 =
      "org.eclipse.jface.databinding.swt.SWTObservables.observeText(org.eclipse.swt.widgets.Widget)";
  private static final String TEXT_OBSERVABLE_METHOD_4 =
      "org.eclipse.jface.databinding.swt.SWTObservables.observeText(org.eclipse.swt.widgets.Control)";
  private static final String ITEMS_OBSERVABLE_METHOD =
      "org.eclipse.jface.databinding.swt.SWTObservables.observeItems(org.eclipse.swt.widgets.Control)";
  private static final String[] OBSERVABLE_METHODS =
      {
          "org.eclipse.jface.databinding.swt.SWTObservables.observeEnabled(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeVisible(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeTooltipText(org.eclipse.swt.widgets.Widget)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeTooltipText(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeSelection(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeMin(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeMax(org.eclipse.swt.widgets.Control)",
          TEXT_OBSERVABLE_METHOD_1,
          TEXT_OBSERVABLE_METHOD_2,
          TEXT_OBSERVABLE_METHOD_3,
          TEXT_OBSERVABLE_METHOD_4,
          ITEMS_OBSERVABLE_METHOD,
          "org.eclipse.jface.databinding.swt.SWTObservables.observeMessage(org.eclipse.swt.widgets.Widget)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeImage(org.eclipse.swt.widgets.Widget)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeSingleSelectionIndex(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeForeground(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeBackground(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeFont(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeSize(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeLocation(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeFocus(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeBounds(org.eclipse.swt.widgets.Control)",
          "org.eclipse.jface.databinding.swt.SWTObservables.observeEditable(org.eclipse.swt.widgets.Control)"};
  private static final String VIEWER_DELAY_OBSERVABLE_METHOD =
      "org.eclipse.jface.databinding.viewers.ViewersObservables.observeDelayedValue(int,org.eclipse.jface.databinding.viewers.IViewerObservableValue)";
  private static final String[] OBSERVABLE_VIEWER_SINGLE_SELECTION_METHODS =
      {
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeSingleSelection(org.eclipse.jface.viewers.ISelectionProvider)",
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeSingleSelection(org.eclipse.jface.viewers.Viewer)"};
  private static final String[] OBSERVABLE_VIEWER_MULTI_SELECTION_METHODS =
      {
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeMultiSelection(org.eclipse.jface.viewers.ISelectionProvider)",
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeMultiSelection(org.eclipse.jface.viewers.Viewer)"};
  private static final String[] OBSERVABLE_VIEWER_CHECKED_ELEMENTS_METHODS =
      {
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(org.eclipse.jface.viewers.ICheckable,java.lang.Object)",
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(org.eclipse.jface.viewers.CheckboxTableViewer,java.lang.Object)",
          "org.eclipse.jface.databinding.viewers.ViewersObservables.observeCheckedElements(org.eclipse.jface.viewers.CheckboxTreeViewer,java.lang.Object)"};
  private static final String OBSERVABLE_VIEWER_FILTERS_METHOD =
      "org.eclipse.jface.databinding.viewers.ViewersObservables.observeFilters(org.eclipse.jface.viewers.StructuredViewer)";
  private static final String[] WIDGET_PROPERTIES_METHODS = {
      "org.eclipse.jface.databinding.swt.WidgetProperties.background()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.bounds()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.editable()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.enabled()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.focused()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.font()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.foreground()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.image()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.location()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.maximum()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.message()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.minimum()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.selection()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.singleSelectionIndex()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.size()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.tooltipText()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.visible()"};
  private static final String ITEMS_WIDGET_PROPERTY =
      "org.eclipse.jface.databinding.swt.WidgetProperties.items()";
  private static final String[] TEXT_WIDGET_PROPERTIES = {
      "org.eclipse.jface.databinding.swt.WidgetProperties.text()",
      "org.eclipse.jface.databinding.swt.WidgetProperties.text(int)",
      "org.eclipse.jface.databinding.swt.WidgetProperties.text(int[])"};
  private static final String SINGLE_SELECTION_VIEWER_PROPERTY =
      "org.eclipse.jface.databinding.viewers.ViewerProperties.singleSelection()";
  private static final String MULTI_SELECTION_VIEWER_PROPERTY =
      "org.eclipse.jface.databinding.viewers.ViewerProperties.multipleSelection()";
  private static final String CHECKED_ELEMENTS_VIEWER_PROPERTY =
      "org.eclipse.jface.databinding.viewers.ViewerProperties.checkedElements(java.lang.Object)";
  private static final String FILTERS_ELEMENTS_VIEWER_PROPERTY =
      "org.eclipse.jface.databinding.viewers.ViewerProperties.filters()";
  private List<WidgetBindableInfo> m_observables = Collections.emptyList();
  private DatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetsObserveTypeContainer() {
    super(ObserveType.WIDGETS, true, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void initialize(IDatabindingsProvider provider) throws Exception {
    m_provider = (DatabindingsProvider) provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveTypeContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<IObserveInfo> getObservables() {
    return CoreUtils.cast(m_observables);
  }

  @Override
  public void synchronizeObserves(JavaInfo root, AstEditor editor, TypeDeclaration rootNode)
      throws Exception {
    for (WidgetBindableInfo widget : m_observables) {
      widget.update(m_provider);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link WidgetBindableInfo} association with given {@link Expression}.
   */
  public WidgetBindableInfo getBindableWidget(Expression expression) throws Exception {
    expression =
        ControllerSupport.convertWidgetBindableExpression(m_provider, m_observables, expression);
    return m_observables.get(0).resolveReference(expression);
  }

  public WidgetBindableInfo resolve(JavaInfo javaInfo) {
    return m_observables.get(0).resolve(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createObservables(JavaInfo root,
      IModelResolver resolver,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    m_observables = Lists.newArrayList();
    m_observables.add(new WidgetBindableInfo(root, m_provider));
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    return null;
  }

  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    if (ArrayUtils.contains(OBSERVABLE_METHODS, signature)) {
      // SWTObservables.observeXXX(Control, ...)
      //
      // prepare widget
      WidgetBindableInfo bindableWidget = getBindableWidget(arguments[0]);
      if (bindableWidget == null) {
        AbstractParser.addError(
            editor,
            "Widget argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      // prepare property
      WidgetPropertyBindableInfo bindableProperty =
          bindableWidget.resolvePropertyReference(invocation.getName().getIdentifier());
      Assert.isNotNull(bindableProperty);
      //
      if (TEXT_OBSERVABLE_METHOD_1.equals(signature)) {
        int updateEvent = CoreUtils.evaluate(Integer.class, editor, arguments[1]);
        TextSwtObservableInfo observable =
            new TextSwtObservableInfo(bindableWidget, bindableProperty, updateEvent);
        observable.setCodeSupport(new SwtObservableTextCodeSupport());
        return observable;
      }
      if (TEXT_OBSERVABLE_METHOD_2.equals(signature)) {
        int[] updateEvents = CoreUtils.evaluate(int[].class, editor, arguments[1]);
        TextSwtObservableInfo observable =
            new TextSwtObservableInfo(bindableWidget, bindableProperty, updateEvents);
        observable.setCodeSupport(new SwtObservableTextCodeSupport());
        return observable;
      }
      Class<?> objectType = bindableWidget.getObjectType();
      if ((TEXT_OBSERVABLE_METHOD_3.equals(signature) || TEXT_OBSERVABLE_METHOD_4.equals(signature))
          && (Text.class.isAssignableFrom(objectType) || StyledText.class.isAssignableFrom(objectType))) {
        TextSwtObservableInfo observable =
            new TextSwtObservableInfo(bindableWidget, bindableProperty, ArrayUtils.EMPTY_INT_ARRAY);
        observable.setCodeSupport(new SwtObservableTextCodeSupport());
        return observable;
      }
      //
      if (ITEMS_OBSERVABLE_METHOD.equals(signature)) {
        ItemsSwtObservableInfo observable =
            new ItemsSwtObservableInfo(bindableWidget, bindableProperty);
        observable.setCodeSupport(new SwtObservableItemsCodeSupport());
        return observable;
      }
      //
      SwtObservableInfo observable = new SwtObservableInfo(bindableWidget, bindableProperty);
      observable.setCodeSupport(new SwtObservableCodeSupport());
      return observable;
    } else if (SWT_DELAY_OBSERVABLE_METHOD.equals(signature)
        || VIEWER_DELAY_OBSERVABLE_METHOD.equals(signature)) {
      // SWTObservables.observeDelayedValue(int, ISWTObservableValue)
      // ViewersObservables.observeDelayedValue(int, IViewerObservableValue)
      //
      // prepare observable
      ObservableInfo observableInfo = (ObservableInfo) resolver.getModel(arguments[1]);
      if (observableInfo == null) {
        AbstractParser.addError(
            editor,
            "Observable argument '" + arguments[1] + "' not found",
            new Throwable());
        return null;
      }
      Assert.isNull(observableInfo.getVariableIdentifier());
      Assert.instanceOf(IDelayValueProvider.class, observableInfo);
      //
      // prepare delay value
      int delayValue = CoreUtils.evaluate(Integer.class, editor, arguments[0]);
      IDelayValueProvider delayValueProvider = (IDelayValueProvider) observableInfo;
      delayValueProvider.setDelayValue(delayValue);
      //
      return observableInfo;
    } else if (ArrayUtils.contains(OBSERVABLE_VIEWER_SINGLE_SELECTION_METHODS, signature)) {
      // ViewersObservables.observeSingleSelection(Viewer)
      // ViewersObservables.observeSingleSelection(ISelectionProvider)
      //
      WidgetBindableInfo bindableWidget = getBindableWidget(arguments[0]);
      if (bindableWidget == null) {
        AbstractParser.addError(
            editor,
            "Viewer argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      SingleSelectionObservableInfo observable = new SingleSelectionObservableInfo(bindableWidget);
      observable.setCodeSupport(new SingleSelectionObservableCodeSupport());
      return observable;
    } else if (ArrayUtils.contains(OBSERVABLE_VIEWER_MULTI_SELECTION_METHODS, signature)) {
      // ViewersObservables.observeMultiSelection(Viewer)
      // ViewersObservables.observeMultiSelection(ISelectionProvider)
      //
      WidgetBindableInfo bindableWidget = getBindableWidget(arguments[0]);
      if (bindableWidget == null) {
        AbstractParser.addError(
            editor,
            "Viewer argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      MultiSelectionObservableInfo observable = new MultiSelectionObservableInfo(bindableWidget);
      observable.setCodeSupport(new MultiSelectionObservableCodeSupport());
      return observable;
    } else if (ArrayUtils.contains(OBSERVABLE_VIEWER_CHECKED_ELEMENTS_METHODS, signature)) {
      // ViewersObservables.observeCheckedElements(ICheckable, Object)
      // ViewersObservables.observeCheckedElements(CheckboxTableViewer, Object)
      // ViewersObservables.observeCheckedElements(CheckboxTreeViewer, Object)
      //
      // prepare viewer
      WidgetBindableInfo bindableWidget = getBindableWidget(arguments[0]);
      if (bindableWidget == null) {
        AbstractParser.addError(
            editor,
            "Viewer argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      // prepare element type
      Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[1]);
      //
      CheckedElementsObservableInfo observable =
          new CheckedElementsObservableInfo(bindableWidget, elementType);
      observable.setCodeSupport(new CheckedElementsObservableCodeSupport());
      return observable;
    } else if (OBSERVABLE_VIEWER_FILTERS_METHOD.equals(signature)) {
      // ViewersObservables.observeFilters(StructuredViewer)
      //
      WidgetBindableInfo bindableWidget = getBindableWidget(arguments[0]);
      if (bindableWidget == null) {
        AbstractParser.addError(
            editor,
            "Viewer argument '" + arguments[0] + "' not found",
            new Throwable());
        return null;
      }
      FiltersObservableInfo observable = new FiltersObservableInfo(bindableWidget);
      observable.setCodeSupport(new FiltersObservableCodeSupport());
      return observable;
    } else if (ArrayUtils.contains(WIDGET_PROPERTIES_METHODS, signature)) {
      // WidgetProperties.XXXX()
      return new WidgetPropertiesCodeSupport(SwtProperties.SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.get(invocation.getName().getIdentifier()));
    } else if (ITEMS_WIDGET_PROPERTY.equals(signature)) {
      // WidgetProperties.items()
      return new WidgetPropertyItemsCodeSupport();
    } else if (ArrayUtils.contains(TEXT_WIDGET_PROPERTIES, signature)) {
      // WidgetProperties.text(...)
      int[] events = ArrayUtils.EMPTY_INT_ARRAY;
      if (arguments.length == 1) {
        Object objectValue = CoreUtils.evaluateObject(editor, arguments[0]);
        if (objectValue instanceof Integer) {
          Integer value = (Integer) objectValue;
          events = new int[]{value.intValue()};
        } else if (objectValue instanceof int[]) {
          events = (int[]) objectValue;
        } else {
          Assert.fail("");
        }
      }
      return new WidgetPropertyTextCodeSupport(events);
    } else if (SINGLE_SELECTION_VIEWER_PROPERTY.equals(signature)) {
      // ViewerProperties.singleSelection()
      return new ViewerPropertySingleSelectionCodeSupport();
    } else if (MULTI_SELECTION_VIEWER_PROPERTY.equals(signature)) {
      // ViewerProperties.multipleSelection()
      return new ViewerPropertyMultiSelectionCodeSupport();
    } else if (CHECKED_ELEMENTS_VIEWER_PROPERTY.equals(signature)) {
      // ViewerProperties.checkedElements(...)
      Class<?> elementType = CoreUtils.evaluate(Class.class, editor, arguments[0]);
      return new ViewerPropertyCheckedElementsCodeSupport(elementType);
    } else if (FILTERS_ELEMENTS_VIEWER_PROPERTY.equals(signature)) {
      // ViewerProperties.filters()
      return new ViewerPropertyFiltersCodeSupport();
    }
    return null;
  }
}