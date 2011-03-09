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
package org.eclipse.wb.internal.swing.databinding.model.properties;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.ElPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

/**
 * Model for {@link org.jdesktop.beansbinding.ELProperty}.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model.properties
 */
public final class ElPropertyInfo extends PropertyInfo {
  private final PropertyInfo m_baseProperty;
  private String m_expression;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ElPropertyInfo(IGenericType sourceObjectType,
      IGenericType valueType,
      PropertyInfo baseProperty,
      String expression) {
    super(sourceObjectType, valueType);
    m_baseProperty = baseProperty;
    setExpression(expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getExpression() {
    return StringEscapeUtils.unescapeJava(m_expression);
  }

  public void setExpression(String expression) {
    Assert.isNotNull(expression);
    m_expression = StringUtilities.escapeJava(expression);
  }

  public PropertyInfo getBaseProperty() {
    return m_baseProperty;
  }

  @Override
  public ObserveInfo getObserveProperty(ObserveInfo observeObject) throws Exception {
    // configure observe object account with base property
    if (m_baseProperty != null) {
      Assert.instanceOf(BeanPropertyInfo.class, m_baseProperty);
      observeObject = m_baseProperty.getObserveProperty(observeObject);
    }
    // find property
    for (IObserveInfo observeProperty : observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable)) {
      if (observeProperty instanceof ElPropertyObserveInfo) {
        return (ObserveInfo) observeProperty;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    // configure variable
    if (getVariableIdentifier() == null) {
      setVariableIdentifier(generationSupport.generateLocalName(
          m_sourceObjectType.getSimpleTypeName(),
          "EvalutionProperty"));
    }
    // handle base property
    String base = "";
    if (m_baseProperty != null) {
      generationSupport.addSourceCode(m_baseProperty, lines);
      base = m_baseProperty.getVariableIdentifier() + ", ";
    }
    // add source code
    if (generationSupport.useGenerics()) {
      lines.add("org.jdesktop.beansbinding.ELProperty"
          + GenericUtils.getTypesSource(m_sourceObjectType, m_valueType)
          + " "
          + getVariableIdentifier()
          + " = org.jdesktop.beansbinding.ELProperty.create("
          + base
          + "\""
          + m_expression
          + "\");");
    } else {
      lines.add("org.jdesktop.beansbinding.Property "
          + getVariableIdentifier()
          + " = org.jdesktop.beansbinding.ELProperty.create("
          + base
          + "\""
          + m_expression
          + "\");");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    if (m_baseProperty != null) {
      m_baseProperty.accept(visitor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText(IObserveInfo observeObject,
      IObserveInfo observeProperty,
      boolean full) throws Exception {
    if (full && m_expression.length() > 0) {
      String basePresentationText = "";
      if (m_baseProperty != null) {
        basePresentationText = m_baseProperty.getPresentationText(null, null, true) + ".";
      }
      return observeObject.getPresentation().getTextForBinding()
          + ".["
          + basePresentationText
          + m_expression
          + "]";
    }
    return super.getPresentationText(observeObject, observeProperty, full);
  }
}