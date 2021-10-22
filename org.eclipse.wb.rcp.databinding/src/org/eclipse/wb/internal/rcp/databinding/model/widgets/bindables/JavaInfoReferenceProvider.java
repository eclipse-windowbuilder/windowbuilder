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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedFieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.WrapperMethodControlVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.ControllerSupport;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * {@link IReferenceProvider} for provider reference on {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class JavaInfoReferenceProvider implements IReferenceProvider {
  private final DatabindingsProvider m_provider;
  private JavaInfo m_javaInfo;
  private String m_controllerReference;
  private boolean m_isControllerReference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoReferenceProvider(JavaInfo javaInfo, DatabindingsProvider provider)
      throws Exception {
    m_provider = provider;
    setJavaInfo(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    m_javaInfo = javaInfo;
    //
    m_isControllerReference = m_provider.isController();
    m_controllerReference =
        m_isControllerReference ? ControllerSupport.getReference(m_provider, m_javaInfo) : null;
  }

  public String getControllerReference() {
    return m_controllerReference;
  }

  public void ensureControllerReference() throws Exception {
    if (m_isControllerReference && m_controllerReference == null) {
      m_controllerReference =
          ControllerSupport.ensureControllerReference(m_provider, m_javaInfo, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReferenceProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getReference() throws Exception {
    return m_isControllerReference && m_controllerReference != null
        ? m_controllerReference
        : getReference(m_javaInfo);
  }

  public static String getReference(JavaInfo javaInfo) throws Exception {
    VariableSupport variableSupport = javaInfo.getVariableSupport();
    // handle this
    if (variableSupport instanceof ThisVariableSupport) {
      return "this";
    }
    // handle named variable
    if (variableSupport instanceof AbstractSimpleVariableSupport && variableSupport.hasName()) {
      return variableSupport.getName();
    }
    // handle exposed
    if (variableSupport instanceof ExposedPropertyVariableSupport
        || variableSupport instanceof ExposedFieldVariableSupport) {
      try {
        for (ASTNode node : javaInfo.getRelatedNodes()) {
          if (AstNodeUtils.isVariable(node)) {
            return CoreUtils.getNodeReference(node);
          }
        }
      } catch (Throwable e) {
      }
      ObjectInfo parent = javaInfo.getParent();
      if (parent instanceof JavaInfo) {
        JavaInfo parentJava = (JavaInfo) parent;
        if (javaInfo instanceof ViewerInfo
            && parentJava.getVariableSupport() instanceof WrapperMethodControlVariableSupport) {
          parentJava = parentJava.getParentJava();
        }
        String reference = getReference(parentJava);
        if (reference != null) {
          return reference + "." + variableSupport.getTitle();
        }
      }
    }
    return null;
  }
}