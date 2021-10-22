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

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;

import java.util.List;

/**
 * Model for observable object <code>ViewerProperties.checkedElements(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ViewerPropertyCheckedElementsCodeSupport extends ViewerObservableCodeSupport {
  private final Class<?> m_parseElementType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerPropertyCheckedElementsCodeSupport() {
    this(null);
  }

  public ViewerPropertyCheckedElementsCodeSupport(Class<?> elementType) {
    super("observeCheckedElements",
        "org.eclipse.jface.databinding.viewers.IViewerSetProperty.observe(org.eclipse.jface.viewers.Viewer)",
        null);
    m_parseElementType = elementType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
      WidgetPropertyBindableInfo bindableProperty,
      int delayValue) throws Exception {
    Assert.isNotNull(m_parseElementType);
    return new CheckedElementsObservableInfo(bindableWidget, m_parseElementType);
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
    CheckedElementsObservableInfo checkedObservable = (CheckedElementsObservableInfo) observable;
    String sourceCode =
        "org.eclipse.jface.databinding.viewers.ViewerProperties.checkedElements("
            + CoreUtils.getClassName(checkedObservable.getElementType())
            + ".class)";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        lines.add("org.eclipse.jface.databinding.viewers.IViewerSetProperty "
            + getVariableIdentifier()
            + " = "
            + sourceCode
            + ";");
      }
      sourceCode = getVariableIdentifier();
    }
    lines.add("org.eclipse.core.databinding.observable.set.IObservableSet "
        + observable.getVariableIdentifier()
        + " = "
        + sourceCode
        + ".observe("
        + observable.getBindableObject().getReference()
        + ");");
  }
}