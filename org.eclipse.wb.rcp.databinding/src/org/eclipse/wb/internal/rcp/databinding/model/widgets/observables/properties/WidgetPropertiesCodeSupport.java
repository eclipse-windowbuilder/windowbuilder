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
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.SwtProperties;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SwtObservableInfo;

import java.util.List;

/**
 * Model for observable object <code>WidgetProperties.XXX()</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class WidgetPropertiesCodeSupport extends AbstractWidgetPropertiesCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WidgetPropertiesCodeSupport(String propertyReference) {
    super(propertyReference,
        "org.eclipse.jface.databinding.swt.IWidgetValueProperty.observe(org.eclipse.swt.widgets.Widget)",
        "org.eclipse.jface.databinding.swt.IWidgetValueProperty.observeDelayed(int,org.eclipse.swt.widgets.Widget)");
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
    SwtObservableInfo observable = new SwtObservableInfo(bindableWidget, bindableProperty);
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
    // prepare variable
    if (observable.getVariableIdentifier() == null) {
      String propertyReference = observable.getBindableProperty().getReference();
      if (propertyReference.startsWith("Observe")) {
        propertyReference = propertyReference.substring(7);
      }
      observable.setVariableIdentifier(generationSupport.generateLocalName(
          propertyReference,
          observable.getBindableObject().getReference(),
          "ObserveWidget"));
    }
    String sourceCode =
        "org.eclipse.core.databinding.observable.value.IObservableValue "
            + observable.getVariableIdentifier()
            + " = ";
    if (getVariableIdentifier() == null) {
      sourceCode += getSourceCode(observable);
    } else {
      if (generationSupport.addModel(this)) {
        lines.add("org.eclipse.jface.databinding.swt.IWidgetValueProperty "
            + getVariableIdentifier()
            + " = "
            + getSourceCode(observable)
            + ";");
      }
      sourceCode += getVariableIdentifier();
    }
    //
    SwtObservableInfo swtObservable = (SwtObservableInfo) observable;
    if (swtObservable.getDelayValue() == 0) {
      // no delay
      lines.add(sourceCode + ".observe(" + observable.getBindableObject().getReference() + ");");
    } else {
      // with delay
      lines.add(sourceCode
          + ".observeDelayed("
          + Integer.toString(swtObservable.getDelayValue())
          + ", "
          + observable.getBindableObject().getReference()
          + ");");
    }
  }

  /**
   * @return the source code for create this observable.
   */
  protected String getSourceCode(ObservableInfo observable) throws Exception {
    return "org.eclipse.jface.databinding.swt.typed.WidgetProperties."
        + SwtProperties.SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.get(observable.getBindableProperty().getReference())
        + "()";
  }

  /**
   * @return the source code.
   */
  public String getSourceCode() throws Exception {
    return "org.eclipse.jface.databinding.swt.typed.WidgetProperties."
        + SwtProperties.SWT_OBSERVABLES_TO_WIDGET_PROPERTIES.get(getPropertyReference())
        + "()";
  }
}