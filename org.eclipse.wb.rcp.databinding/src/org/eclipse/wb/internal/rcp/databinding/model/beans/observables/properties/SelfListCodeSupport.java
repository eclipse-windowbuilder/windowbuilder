/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.CollectionPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.CollectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.WritableListBeanObservableInfo;

import java.util.List;

/**
 * Model for observable object {@code Properties.selfList(...)}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class SelfListCodeSupport extends BeanPropertiesCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getObservableType() {
    return "org.eclipse.core.databinding.property.list.IListProperty";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ObservableInfo createObservable(AstEditor editor, BeanBindableInfo bindableObject)
      throws Exception {
    CollectionPropertyBindableInfo bindableProperty =
        (CollectionPropertyBindableInfo) bindableObject.resolvePropertyReference(bindableObject.getReference());
    Assert.isNotNull(bindableProperty);
    WritableListBeanObservableInfo observable =
        new WritableListBeanObservableInfo(bindableObject, bindableProperty, m_parserPropertyType);
    observable.setCodeSupport(this);
    return observable;
  }

  @Override
  protected ObservableInfo createObservable(BeanBindableInfo bindableObject,
      BeanPropertyBindableInfo bindableProperty) throws Exception {
    throw new UnsupportedOperationException();
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
      observable.setVariableIdentifier(generationSupport.generateLocalName("SelfList"));
    }
    // add code
    CollectionObservableInfo collectionObservable = (CollectionObservableInfo) observable;
    String sourceCode =
        "org.eclipse.core.databinding.property.Properties.selfList("
            + CoreUtils.getClassName(collectionObservable.getElementType())
            + ".class)";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        lines.add("org.eclipse.core.databinding.property.list.IListProperty "
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