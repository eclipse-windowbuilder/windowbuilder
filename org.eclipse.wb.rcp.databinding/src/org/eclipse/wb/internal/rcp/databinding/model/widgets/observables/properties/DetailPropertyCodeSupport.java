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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.BeanPropertiesCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.text.MessageFormat;
import java.util.List;

/**
 * Abstract model for detail observable object <code>IValueProperty.XXX(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class DetailPropertyCodeSupport extends ObservableCodeSupport {
  private final String m_observeClass;
  private final String m_observeSignature;
  private final String m_observeRealmSignature;
  private final String m_detailObservableClass;
  private final String m_observeMethod;
  private final ViewerPropertySingleSelectionCodeSupport m_selectionProperty;
  protected final BeanPropertiesCodeSupport m_detailProperty;
  protected SingleSelectionObservableInfo m_masterObservable;
  private DetailBeanObservableInfo m_detailObservable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DetailPropertyCodeSupport(String observeClass,
      String detailObservableClass,
      String observeMethod,
      ViewerPropertySingleSelectionCodeSupport selectionProperty,
      BeanPropertiesCodeSupport detailProperty) {
    m_observeClass = observeClass;
    m_observeSignature = m_observeClass + ".observe(java.lang.Object)";
    m_observeRealmSignature =
        m_observeClass + ".observe(org.eclipse.core.databinding.observable.Realm,java.lang.Object)";
    m_detailObservableClass = detailObservableClass;
    m_observeMethod = observeMethod;
    m_selectionProperty = selectionProperty;
    m_detailProperty = detailProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    Expression widgetExpression = null;
    if (m_observeSignature.equals(signature)) {
      widgetExpression = arguments[0];
    } else if (m_observeRealmSignature.equals(signature)) {
      widgetExpression = arguments[1];
    } else {
      return null;
    }
    //
    WidgetsObserveTypeContainer container =
        (WidgetsObserveTypeContainer) DatabindingsProvider.cast(provider).getContainer(
            ObserveType.WIDGETS);
    // prepare widget
    WidgetBindableInfo bindableWidget = container.getBindableWidget(widgetExpression);
    if (bindableWidget == null) {
      AbstractParser.addError(editor, MessageFormat.format(
          Messages.DetailPropertyCodeSupport_widgetArgumentNotFound,
          widgetExpression), new Throwable());
      return null;
    }
    // prepare property
    WidgetPropertyBindableInfo bindableProperty =
        bindableWidget.resolvePropertyReference("observeSingleSelection");
    Assert.isNotNull(bindableProperty);
    //
    m_masterObservable = new SingleSelectionObservableInfo(bindableWidget, bindableProperty);
    m_masterObservable = (SingleSelectionObservableInfo) m_masterObservable.getMasterObservable();
    Assert.isNotNull(m_masterObservable);
    //
    m_masterObservable.setCodeSupport(new ObservableCodeSupport() {
      @Override
      public void addSourceCode(ObservableInfo observable,
          List<String> lines,
          CodeGenerationSupport generationSupport) throws Exception {
      }
    });
    //
    m_detailObservable = createDetailObservable();
    m_detailObservable.setCodeSupport(this);
    return m_detailObservable;
  }

  /**
   * @return {@link DetailBeanObservableInfo} observable with this code generator.
   */
  protected abstract DetailBeanObservableInfo createDetailObservable();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(ObservableInfo observable,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    String sourceCode =
        m_selectionProperty.getMasterSourceCode(lines, generationSupport)
            + "."
            + m_observeMethod
            + "("
            + m_detailProperty.getDetailSourceCode(m_detailObservable, lines, generationSupport)
            + ")";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        lines.add(m_observeClass + " " + getVariableIdentifier() + " = " + sourceCode + ";");
      }
      sourceCode = getVariableIdentifier();
    }
    lines.add(m_detailObservableClass
        + " "
        + m_detailObservable.getVariableIdentifier()
        + " = "
        + sourceCode
        + ".observe("
        + m_masterObservable.getBindableObject().getReference()
        + ");");
  }
}