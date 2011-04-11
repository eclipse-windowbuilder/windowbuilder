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
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.text.MessageFormat;
import java.util.List;

/**
 * Model for observable object <code>ViewerProperties.singleSelection()<code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ViewerPropertySingleSelectionCodeSupport extends ViewerObservableCodeSupport {
  private static final String VALUE_DETAIL =
      "org.eclipse.core.databinding.property.value.IValueProperty.value(org.eclipse.core.databinding.property.value.IValueProperty)";
  private static final String LIST_DETAIL =
      "org.eclipse.core.databinding.property.value.IValueProperty.list(org.eclipse.core.databinding.property.list.IListProperty)";
  private static final String SET_DETAIL =
      "org.eclipse.core.databinding.property.value.IValueProperty.set(org.eclipse.core.databinding.property.set.ISetProperty)";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerPropertySingleSelectionCodeSupport() {
    super("observeSingleSelection",
        "org.eclipse.jface.databinding.viewers.IViewerValueProperty.observe(org.eclipse.jface.viewers.Viewer)",
        "org.eclipse.jface.databinding.viewers.IViewerValueProperty.observeDelayed(int,org.eclipse.jface.viewers.Viewer)");
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
    // IValueProperty.value(IValueProperty)
    if (VALUE_DETAIL.equals(signature)) {
      ValuePropertyCodeSupport detailProperty =
          (ValuePropertyCodeSupport) resolver.getModel(arguments[0]);
      Assert.isNotNull(detailProperty, MessageFormat.format(
          Messages.ViewerPropertySingleSelectionCodeSupport_argumentNotFound,
          arguments[0]));
      return new ValueValuePropertyCodeSupport(this, detailProperty);
    }
    // IValueProperty.list(IListProperty)
    if (LIST_DETAIL.equals(signature)) {
      ListPropertyCodeSupport detailProperty =
          (ListPropertyCodeSupport) resolver.getModel(arguments[0]);
      Assert.isNotNull(detailProperty, MessageFormat.format(
          Messages.ViewerPropertySingleSelectionCodeSupport_argumentNotFound,
          arguments[0]));
      return new ValueListPropertyCodeSupport(this, detailProperty);
    }
    // IValueProperty.set(ISetProperty)
    if (SET_DETAIL.equals(signature)) {
      SetPropertyCodeSupport detailProperty =
          (SetPropertyCodeSupport) resolver.getModel(arguments[0]);
      Assert.isNotNull(detailProperty, MessageFormat.format(
          Messages.ViewerPropertySingleSelectionCodeSupport_argumentNotFound,
          arguments[0]));
      return new ValueSetPropertyCodeSupport(this, detailProperty);
    }
    return super.parseExpression(editor, signature, invocation, arguments, resolver, provider);
  }

  @Override
  protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
      WidgetPropertyBindableInfo bindableProperty,
      int delayValue) throws Exception {
    SingleSelectionObservableInfo observable =
        new SingleSelectionObservableInfo(bindableWidget, bindableProperty);
    observable.setDelayValue(delayValue);
    return observable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(ObservableInfo observable,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    super.addSourceCode(observable, lines, generationSupport);
    String sourceCode = "org.eclipse.jface.databinding.viewers.ViewerProperties.singleSelection()";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        lines.add("org.eclipse.jface.databinding.viewers.IViewerValueProperty "
            + getVariableIdentifier()
            + " = "
            + sourceCode
            + ";");
      }
      sourceCode = getVariableIdentifier();
    }
    SingleSelectionObservableInfo selectionObservable = (SingleSelectionObservableInfo) observable;
    if (selectionObservable.getDelayValue() == 0) {
      // no delay
      lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
          + observable.getVariableIdentifier()
          + " = "
          + sourceCode
          + ".observe("
          + observable.getBindableObject().getReference()
          + ");");
    } else {
      // with delay
      lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
          + observable.getVariableIdentifier()
          + " = "
          + sourceCode
          + ".observeDelayed("
          + Integer.toString(selectionObservable.getDelayValue())
          + ", "
          + observable.getBindableObject().getReference()
          + ");");
    }
  }

  public String getMasterSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    String sourceCode = "org.eclipse.jface.databinding.viewers.ViewerProperties.singleSelection()";
    if (getVariableIdentifier() == null) {
      return sourceCode;
    }
    if (generationSupport.addModel(this)) {
      lines.add("org.eclipse.jface.databinding.viewers.IViewerValueProperty "
          + getVariableIdentifier()
          + " = "
          + sourceCode
          + ";");
    }
    return getVariableIdentifier();
  }
}