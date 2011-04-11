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
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.AbstractParser;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.WidgetsObserveTypeContainer;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.text.MessageFormat;

/**
 * Abstract source code generator for widget observables.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class AbstractWidgetPropertiesCodeSupport extends ObservableCodeSupport {
  private final String m_propertyReference;
  private final String m_signatureObserve;
  private final String m_signatureObserveDelayed;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractWidgetPropertiesCodeSupport(String propertyReference,
      String signatureObserve,
      String signatureObserveDelayed) {
    m_propertyReference = propertyReference;
    m_signatureObserve = signatureObserve;
    m_signatureObserveDelayed = signatureObserveDelayed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getPropertyReference() {
    return m_propertyReference;
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
    int delayValue = 0;
    Expression widgetExpression = null;
    //
    if (m_signatureObserve != null && m_signatureObserve.equals(signature)) {
      widgetExpression = arguments[0];
    } else if (m_signatureObserveDelayed != null && m_signatureObserveDelayed.equals(signature)) {
      delayValue = CoreUtils.evaluate(Integer.class, editor, arguments[0]);
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
          Messages.AbstractWidgetPropertiesCodeSupport_widgetArgumentNotFound,
          widgetExpression), new Throwable());
      return null;
    }
    // prepare property
    WidgetPropertyBindableInfo bindableProperty =
        bindableWidget.resolvePropertyReference(m_propertyReference);
    Assert.isNotNull(bindableProperty);
    //
    ObservableInfo observable = createObservable(bindableWidget, bindableProperty, delayValue);
    observable.setCodeSupport(this);
    return observable;
  }

  /**
   * @return {@link ObservableInfo} model for given widget and property.
   */
  protected abstract ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
      WidgetPropertyBindableInfo bindableProperty,
      int delayValue) throws Exception;
}