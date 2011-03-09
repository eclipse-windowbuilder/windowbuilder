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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.CollectionObservableInfo;

import java.util.List;

/**
 * Model for observable object {@link org.eclipse.core.databinding.observable.list.WritableList}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class WritableListCodeSupport extends ObservableCodeSupport {
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
      observable.setVariableIdentifier(generationSupport.generateLocalName("WritableList"));
    }
    // add code
    CollectionObservableInfo collectionObservable = (CollectionObservableInfo) observable;
    lines.add("org.eclipse.core.databinding.observable.list.WritableList "
        + observable.getVariableIdentifier()
        + " = new org.eclipse.core.databinding.observable.list.WritableList("
        + observable.getBindableObject().getReference()
        + ", "
        + CoreUtils.getClassName(collectionObservable.getElementType())
        + ".class);");
  }
}