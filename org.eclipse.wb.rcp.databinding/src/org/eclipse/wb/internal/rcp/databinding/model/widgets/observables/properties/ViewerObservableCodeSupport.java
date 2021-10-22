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

import java.util.List;

/**
 * Abstract model for observable object <code>ViewerProperties.XXX(...)<code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class ViewerObservableCodeSupport extends AbstractWidgetPropertiesCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerObservableCodeSupport(String propertyReference,
      String signatureObserve,
      String signatureObserveDelayed) {
    super(propertyReference, signatureObserve, signatureObserveDelayed);
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
          observable.getBindableObject().getReference()));
    }
  }
}