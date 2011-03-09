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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsCodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for observable object <code>BeansObservables.observeMaps(...)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class MapsBeanObservableInfo extends ObservableInfo {
  private final ObservableInfo m_domainObservable;
  private Class<?> m_elementType;
  private String[] m_properties;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MapsBeanObservableInfo(ObservableInfo domainObservable,
      Class<?> elementType,
      String[] properties) {
    m_domainObservable = domainObservable;
    m_elementType = elementType;
    m_properties = properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableInfo getDomainObservable() {
    return m_domainObservable;
  }

  public Class<?> getElementType() {
    return m_elementType;
  }

  public void setElementType(Class<?> elementType) {
    m_elementType = elementType;
  }

  public String[] getProperties() {
    return m_properties;
  }

  public void setProperties(String[] properties) throws Exception {
    m_properties = properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public BindableInfo getBindableObject() {
    return null;
  }

  @Override
  public BindableInfo getBindableProperty() {
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
    // prepare variable
    if (getVariableIdentifier() == null) {
      if (m_properties.length == 1) {
        setVariableIdentifier(generationSupport.generateLocalName("observeMap"));
      } else {
        setVariableIdentifier(generationSupport.generateLocalName("observeMaps"));
      }
    }
    //
    KnownElementsObservableInfo domainObservable = (KnownElementsObservableInfo) m_domainObservable;
    //
    if (m_properties.length == 1) {
      String observeMethod =
          isPojoBean(m_elementType)
              ? " = " + DataBindingsCodeUtils.getPojoObservablesClass() + ".observeMap("
              : " = org.eclipse.core.databinding.beans.BeansObservables.observeMap(";
      // add code
      lines.add("org.eclipse.core.databinding.observable.map.IObservableMap "
          + getVariableIdentifier()
          + observeMethod
          + domainObservable.getSourceCode()
          + ", "
          + CoreUtils.getClassName(m_elementType)
          + ".class, \""
          + m_properties[0]
          + "\");");
    } else {
      String observeMethod =
          isPojoBean(m_elementType)
              ? " = " + DataBindingsCodeUtils.getPojoObservablesClass() + ".observeMaps("
              : " = org.eclipse.core.databinding.beans.BeansObservables.observeMaps(";
      // add code
      lines.add("org.eclipse.core.databinding.observable.map.IObservableMap[] "
          + getVariableIdentifier()
          + observeMethod
          + domainObservable.getSourceCode()
          + ", "
          + CoreUtils.getClassName(m_elementType)
          + ".class, new java.lang.String[]{\""
          + StringUtils.join(m_properties, "\", \"")
          + "\"});");
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
    m_domainObservable.accept(visitor);
  }
}