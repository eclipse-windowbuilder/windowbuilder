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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.AbstractNoNameVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Implementation of {@link VariableSupport} for virtual {@link FormAttachmentInfo}.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public final class VirtualFormAttachmentVariableSupport extends AbstractNoNameVariableSupport {
  private final FormSide m_side;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VirtualFormAttachmentVariableSupport(JavaInfo javaInfo, FormSide side) {
    super(javaInfo);
    m_side = side;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // VariableSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public String getTitle() throws Exception {
    return "(no attachment)";
  }

  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    throw new IllegalStateException();
  }

  /**
   * Makes the attachment 'real': changes the variable support and creation support.
   */
  public void materialize() throws Exception {
    // create expression statement and variable support
    JavaInfo formData = (JavaInfo) m_javaInfo.getParent();
    Expression expressionStatement =
        formData.addExpressionStatement(TemplateUtils.format(
            "{0}.{1} = new org.eclipse.swt.layout.FormAttachment(0, 0)",
            formData,
            m_side.getField()));
    VariableSupport variableSupport = new EmptyVariableSupport(m_javaInfo, expressionStatement);
    m_javaInfo.setVariableSupport(variableSupport);
    // creation support
    {
      Assignment assignment = (Assignment) expressionStatement;
      ClassInstanceCreation creation = (ClassInstanceCreation) assignment.getRightHandSide();
      m_javaInfo.setCreationSupport(new ConstructorCreationSupport(creation));
      m_javaInfo.bindToExpression(creation);
    }
    m_javaInfo.addRelatedNodes(expressionStatement);
    formData.addRelatedNodes(expressionStatement);
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public StatementTarget getStatementTarget() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "form-attachment";
  }
}
