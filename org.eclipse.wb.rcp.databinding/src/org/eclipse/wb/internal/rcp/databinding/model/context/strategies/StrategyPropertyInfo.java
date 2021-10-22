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
package org.eclipse.wb.internal.rcp.databinding.model.context.strategies;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Abstract model for any strategy properties: converter, validator and etc.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public abstract class StrategyPropertyInfo extends SimpleClassObjectInfo {
  private boolean m_anonymous = false;
  private String m_anonymousSource;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public StrategyPropertyInfo(String className) {
    super(className);
  }

  public StrategyPropertyInfo(AstEditor editor, ClassInstanceCreation creation) {
    String className = AstNodeUtils.getFullyQualifiedName(creation, false);
    if (creation.arguments().isEmpty()) {
      ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(creation);
      if (typeBinding.isAnonymous()) {
        m_anonymous = true;
        m_anonymousSource = editor.getSource(creation);
        className += " <<Anonymous>>";
      }
    } else {
      String source = editor.getSource(creation);
      int index = source.indexOf('(');
      className += source.substring(index);
    }
    setClassName(className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isAnonymous() {
    return m_anonymous;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  public final String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    String variable = getVariableIdentifier();
    // check anonymous
    if (m_anonymous) {
      // check variable
      if (variable == null) {
        // no variable
        return m_anonymousSource;
      }
      // variable mode
      lines.add(getBaseClassName() + " " + variable + " = " + m_anonymousSource + ";");
      return variable;
    }
    // normal
    String defaultCostructor = m_className.indexOf('(') == -1 ? "()" : "";
    // check variable
    if (variable == null) {
      // no variable
      return "new " + m_className + defaultCostructor;
    }
    // variable mode
    lines.add(getBaseClassName()
        + " "
        + variable
        + " = new "
        + m_className
        + defaultCostructor
        + ";");
    return variable;
  }

  /**
   * @return the model class name.
   */
  protected abstract String getBaseClassName();
}