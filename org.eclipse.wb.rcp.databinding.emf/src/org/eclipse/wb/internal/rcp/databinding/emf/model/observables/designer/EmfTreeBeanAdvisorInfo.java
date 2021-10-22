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
package org.eclipse.wb.internal.rcp.databinding.emf.model.observables.designer;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.emf.model.bindables.PropertiesSupport.PropertyInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;

import java.util.List;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.EMFTreeBeanAdvisor}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public class EmfTreeBeanAdvisorInfo extends TreeBeanAdvisorInfo {
  private static final String ADVISOR_CLASS = "org.eclipse.wb.rcp.databinding.EMFTreeBeanAdvisor";
  private final PropertiesSupport m_propertiesSupport;
  private PropertyInfo m_parentProperty;
  private PropertyInfo m_childrenProperty;
  private PropertyInfo m_hasChildrenProperty;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EmfTreeBeanAdvisorInfo(PropertiesSupport propertiesSupport) {
    m_propertiesSupport = propertiesSupport;
    setClassName(ADVISOR_CLASS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setParentProperty(String parentProperty) throws Exception {
    super.setParentProperty(parentProperty);
    //
    m_parentProperty = m_propertiesSupport.getProperty(getElementType(), parentProperty);
    Assert.isNotNull(m_parentProperty);
  }

  public void setEMFParentProperty(String parentPropertyReference) throws Exception {
    if (parentPropertyReference == null) {
      m_parentProperty = null;
      super.setParentProperty(null);
    } else {
      Object[] result = m_propertiesSupport.getClassInfoForProperty(parentPropertyReference);
      Assert.isNotNull(result);
      //
      m_parentProperty = (PropertyInfo) result[1];
      super.setParentProperty(m_parentProperty.name);
    }
  }

  @Override
  public void setChildrenProperty(String childrenProperty) throws Exception {
    super.setChildrenProperty(childrenProperty);
    //
    m_childrenProperty = m_propertiesSupport.getProperty(getElementType(), childrenProperty);
    Assert.isNotNull(m_childrenProperty);
  }

  public void setEMFChildrenProperty(String childrenPropertyReference) throws Exception {
    if (childrenPropertyReference == null) {
      m_childrenProperty = null;
      super.setChildrenProperty(null);
    } else {
      Object[] result = m_propertiesSupport.getClassInfoForProperty(childrenPropertyReference);
      Assert.isNotNull(result);
      //
      m_childrenProperty = (PropertyInfo) result[1];
      super.setChildrenProperty(m_childrenProperty.name);
    }
  }

  @Override
  public void setHasChildrenProperty(String hasChildrenProperty) throws Exception {
    super.setHasChildrenProperty(hasChildrenProperty);
    //
    m_hasChildrenProperty = m_propertiesSupport.getProperty(getElementType(), hasChildrenProperty);
    Assert.isNotNull(m_hasChildrenProperty);
  }

  public void setEMFHasChildrenProperty(String hasChildrenPropertyReference) throws Exception {
    if (hasChildrenPropertyReference == null) {
      m_hasChildrenProperty = null;
      super.setHasChildrenProperty(null);
    } else {
      Object[] result = m_propertiesSupport.getClassInfoForProperty(hasChildrenPropertyReference);
      Assert.isNotNull(result);
      //
      m_hasChildrenProperty = (PropertyInfo) result[1];
      super.setHasChildrenProperty(m_hasChildrenProperty.name);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configure(ChooseClassConfiguration configuration) {
    configuration.setValueScope(ADVISOR_CLASS);
    configuration.setClearValue(ADVISOR_CLASS);
    configuration.setBaseClassName(ADVISOR_CLASS);
    Class<?> eStructuralFeature = m_propertiesSupport.getEStructuralFeature();
    configuration.setConstructorParameters(new Class[]{
        eStructuralFeature,
        eStructuralFeature,
        eStructuralFeature});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addSourceCode(List<String> lines) throws Exception {
    String parentProperty = m_parentProperty == null ? "null" : m_parentProperty.reference;
    String childrenProperty = m_childrenProperty == null ? "null" : m_childrenProperty.reference;
    String hasChildrenProperty =
        m_hasChildrenProperty == null ? "null" : m_hasChildrenProperty.reference;
    lines.add(ADVISOR_CLASS
        + " "
        + getVariableIdentifier()
        + " = new "
        + m_className
        + "("
        + parentProperty
        + ", "
        + childrenProperty
        + ", "
        + hasChildrenProperty
        + ");");
  }
}