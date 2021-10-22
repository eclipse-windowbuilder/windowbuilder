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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EObjectBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.EPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.DetailListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.emf.model.observables.ListEmfObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class EmfListPropertyCodeSupport extends EmfPropertiesCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EmfListPropertyCodeSupport() {
    super("org.eclipse.core.databinding.property.list.IListProperty");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ObservableInfo createObservable(EObjectBindableInfo eObject,
      EPropertyBindableInfo eProperty) {
    return new ListEmfObservableInfo(eObject, eProperty);
  }

  @Override
  protected ObservableInfo createDetailObservable(ObservableInfo masterObservable,
      PropertiesSupport propertiesSupport) throws Exception {
    Assert.isNotNull(m_parserPropertyReference);
    //
    DetailListEmfObservableInfo observeDetailList =
        new DetailListEmfObservableInfo(masterObservable, propertiesSupport);
    observeDetailList.setDetailPropertyReference(null, m_parserPropertyReference);
    observeDetailList.setCodeSupport(new EmfListPropertyDetailCodeSupport());
    //
    return observeDetailList;
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
      observable.setVariableIdentifier(generationSupport.generateLocalName(
          observable.getBindableObject().getReference(),
          observable.getBindableProperty().getPresentation().getText(),
          "ObserveList"));
    }
    //
    String sourceCode =
        PropertiesSupport.getEMFPropertiesCode(observable.getBindableObject(), "list(")
            + observable.getBindableProperty().getReference()
            + ")";
    if (getVariableIdentifier() != null) {
      if (generationSupport.addModel(this)) {
        if (generationSupport.addModel(this)) {
          lines.add("org.eclipse.core.databinding.beans.IBeanListProperty "
              + getVariableIdentifier()
              + sourceCode
              + ";");
        }
      }
      sourceCode = getVariableIdentifier();
    }
    // add code
    lines.add("org.eclipse.core.databinding.observable.list.IObservableList "
        + observable.getVariableIdentifier()
        + sourceCode
        + ".observe("
        + observable.getBindableObject().getReference()
        + ");");
  }
}