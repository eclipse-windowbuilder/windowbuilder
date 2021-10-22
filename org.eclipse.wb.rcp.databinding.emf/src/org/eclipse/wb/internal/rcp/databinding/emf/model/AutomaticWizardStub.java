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
package org.eclipse.wb.internal.rcp.databinding.emf.model;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.IAutomaticWizardStub;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * EMF implementation of {@link IAutomaticWizardStub}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class AutomaticWizardStub implements IAutomaticWizardStub {
  private final List<PropertyInfo> m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AutomaticWizardStub(IJavaProject javaProject,
      ClassLoader classLoader,
      Class<?> eObjectClass) throws Exception {
    List<VariableDeclarationFragment> fragments = Collections.emptyList();
    PropertiesSupport propertiesSupport =
        new PropertiesSupport(javaProject, classLoader, fragments);
    m_properties = propertiesSupport.getProperties(eObjectClass);
    EmfObserveTypeContainer.ensureDBLibraries(
        javaProject,
        "org.eclipse.emf.databinding.EMFObservables",
        "org.eclipse.emf.databinding");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAutomaticWizardStub
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addImports(Collection<String> importList) {
    importList.add("org.eclipse.emf.databinding.EMFObservables");
  }

  public String createSourceCode(String fieldName, String propertyName) {
    for (PropertyInfo property : m_properties) {
      if (propertyName.equals(property.name)) {
        return "\t\tIObservableValue "
            + propertyName
            + "ObserveValue = EMFObservables.observeValue("
            + fieldName
            + ", "
            + property.reference
            + ");";
      }
    }
    Assert.fail("Undefine property: " + propertyName);
    return null;
  }
}