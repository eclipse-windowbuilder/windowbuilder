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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.databinding.viewers.ObservableSetContentProvider}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ObservableSetContentProviderInfo extends ObservableCollectionContentProviderInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableSetContentProviderInfo() {
    super("org.eclipse.jface.databinding.viewers.ObservableSetContentProvider");
  }

  public ObservableSetContentProviderInfo(String className) {
    super(className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    // prepare variable
    if (getVariableIdentifier() == null) {
      setVariableIdentifier(generationSupport.generateLocalName("setContentProvider"));
    }
    // add code
    lines.add("org.eclipse.jface.databinding.viewers.ObservableSetContentProvider "
        + getVariableIdentifier()
        + " = new "
        + m_className
        + "();");
  }
}