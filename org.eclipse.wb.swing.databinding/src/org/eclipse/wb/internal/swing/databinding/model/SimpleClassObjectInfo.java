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
package org.eclipse.wb.internal.swing.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for abstract objects or interfaces and objects that supported extends.
 * 
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public abstract class SimpleClassObjectInfo extends AstObjectInfo {
  private final String m_abstractClassName;
  protected String m_className;
  private String m_parameters;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleClassObjectInfo(String abstractClassName, String className) {
    m_abstractClassName = abstractClassName;
    m_className = className;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getClassName() {
    return m_className;
  }

  public void setClassName(String className) {
    m_className = className;
  }

  protected IGenericType[] getTypeArguments() {
    return null;
  }

  public String getParameters() {
    return m_parameters;
  }

  public void setParameters(String parameters) {
    m_parameters = parameters;
  }

  public String getFullClassName() {
    return StringUtils.isEmpty(m_parameters) ? m_className : m_className + m_parameters;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    IGenericType[] typeArguments = getTypeArguments();
    boolean variable = getVariableIdentifier() != null;
    StringBuffer line = new StringBuffer();
    if (variable) {
      if (m_abstractClassName == null) {
        line.append(m_className);
      } else {
        line.append(m_abstractClassName);
      }
      if (generationSupport.useGenerics() && typeArguments != null) {
        line.append(GenericUtils.getTypesSource(typeArguments));
      }
      line.append(" " + getVariableIdentifier() + " = ");
    }
    line.append("new " + m_className);
    if (generationSupport.useGenerics() && typeArguments != null) {
      line.append(GenericUtils.getTypesSource(typeArguments));
    }
    if (StringUtils.isEmpty(m_parameters)) {
      line.append("()");
    } else {
      line.append(m_parameters);
    }
    if (variable) {
      line.append(";");
      lines.add(line.toString());
      return getVariableIdentifier();
    }
    return line.toString();
  }
}