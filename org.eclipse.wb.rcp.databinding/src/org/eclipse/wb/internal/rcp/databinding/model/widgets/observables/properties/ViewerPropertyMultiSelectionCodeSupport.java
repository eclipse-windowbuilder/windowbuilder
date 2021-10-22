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
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.MultiSelectionObservableInfo;

import java.util.List;

/**
 * Model for observable object <code>ViewerProperties.multiSelection()</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ViewerPropertyMultiSelectionCodeSupport extends ViewerObservableCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerPropertyMultiSelectionCodeSupport() {
    super("observeMultiSelection",
        "org.eclipse.jface.databinding.viewers.IViewerListProperty.observe(org.eclipse.jface.viewers.Viewer)",
        null);
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
    return new MultiSelectionObservableInfo(bindableWidget, bindableProperty);
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
    String sourceCode =
        "org.eclipse.jface.databinding.viewers.ViewerProperties.multipleSelection()";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        lines.add("org.eclipse.jface.databinding.viewers.IViewerListProperty "
            + getVariableIdentifier()
            + " = "
            + sourceCode
            + ";");
      }
      sourceCode = getVariableIdentifier();
    }
    lines.add("org.eclipse.core.databinding.observable.list.IObservableList "
        + observable.getVariableIdentifier()
        + " = "
        + sourceCode
        + ".observe("
        + observable.getBindableObject().getReference()
        + ");");
  }
}