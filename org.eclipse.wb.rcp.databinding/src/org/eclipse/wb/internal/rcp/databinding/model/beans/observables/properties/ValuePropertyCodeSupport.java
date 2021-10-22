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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ValueBeanObservableInfo;

import java.util.List;

/**
 * Model for observable object {@code BeanProperties.value(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class ValuePropertyCodeSupport extends BeanPropertiesCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValuePropertyCodeSupport() {
    super("org.eclipse.core.databinding.property.value.IValueProperty");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ObservableInfo createObservable(BeanBindableInfo bindableObject,
      BeanPropertyBindableInfo bindableProperty) throws Exception {
    return new ValueBeanObservableInfo(bindableObject, bindableProperty);
  }

  @Override
  protected ObservableInfo createDetailObservable(ObservableInfo masterObservable) throws Exception {
    Assert.isNotNull(m_parserPropertyReference);
    Assert.isNotNull(m_parserPropertyType);
    DetailValueBeanObservableInfo observable =
        new DetailValueBeanObservableInfo(masterObservable,
            m_parserBeanType,
            m_parserPropertyReference,
            m_parserPropertyType);
    observable.setPojoBindable(parserIsPojo());
    observable.setCodeSupport(new ValuePropertyDetailCodeSupport());
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
    // special case - ValueProperty use without observable
    if (observable == null) {
      if (getVariableIdentifier() == null) {
        setVariableIdentifier(generationSupport.generateLocalName("valueProperty"));
      }
      if (generationSupport.addModel(this)) {
        String sourceCode =
            m_parserIsPojo
                ? "org.eclipse.core.databinding.beans.PojoProperties"
                : "org.eclipse.core.databinding.beans.BeanProperties";
        lines.add("org.eclipse.core.databinding.beans.IBeanValueProperty "
            + getVariableIdentifier()
            + " = "
            + sourceCode
            + ".value("
            + m_parserPropertyReference
            + ");");
      }
      return;
    }
    // prepare variable
    if (observable.getVariableIdentifier() == null) {
      observable.setVariableIdentifier(generationSupport.generateLocalName(
          observable.getBindableProperty().getReference(),
          observable.getBindableObject().getReference(),
          "ObserveValue"));
    }
    //
    String sourceCode =
        observable.isPojoBindable()
            ? "org.eclipse.core.databinding.beans.PojoProperties"
            : "org.eclipse.core.databinding.beans.BeanProperties";
    sourceCode += ".value(" + observable.getBindableProperty().getReference() + ")";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        if (generationSupport.addModel(this)) {
          lines.add("org.eclipse.core.databinding.beans.IBeanValueProperty "
              + getVariableIdentifier()
              + " = "
              + sourceCode
              + ";");
        }
      }
      sourceCode = getVariableIdentifier();
    }
    // add code
    lines.add("org.eclipse.core.databinding.observable.value.IObservableValue "
        + observable.getVariableIdentifier()
        + " = "
        + sourceCode
        + ".observe("
        + observable.getBindableObject().getReference()
        + ");");
  }

  @Override
  public String getDetailSourceCode(DetailBeanObservableInfo detailObservable,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    String sourceCode =
        m_parserIsPojo
            ? "org.eclipse.core.databinding.beans.PojoProperties"
            : "org.eclipse.core.databinding.beans.BeanProperties";
    sourceCode +=
        ".value("
            + detailObservable.getDetailPropertyReference()
            + ", "
            + CoreUtils.getClassName(detailObservable.getDetailPropertyType())
            + ".class)";
    if (getVariableIdentifier() == null) {
      return sourceCode;
    }
    if (generationSupport.addModel(this)) {
      lines.add("org.eclipse.core.databinding.beans.IBeanValueProperty "
          + getVariableIdentifier()
          + " = "
          + sourceCode
          + ";");
    }
    return getVariableIdentifier();
  }
}