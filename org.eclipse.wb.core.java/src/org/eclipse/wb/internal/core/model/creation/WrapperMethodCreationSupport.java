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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.core.model.WrapperMethodInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.WrappedObjectAssociation;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Implementation of {@link CreationSupport} for creating {@link WrapperMethodInfo} during creating
 * corresponding {@link ControlInfo}.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public class WrapperMethodCreationSupport extends CreationSupport {
  protected final WrapperByMethod m_wrapper;
  private MethodInvocation m_invocation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperMethodCreationSupport(WrapperByMethod wrapper) {
    m_wrapper = wrapper;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "method: " + ReflectionUtils.toString(m_wrapper.getControlMethod());
  }

  @Override
  public ASTNode getNode() {
    return m_invocation;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return m_invocation != null && node == m_invocation;
  }

  @Override
  public Association getAssociation() throws Exception {
    return new WrappedObjectAssociation(m_wrapper);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    String wrapperSource = m_wrapper.getWrapperInfo().getCreationSupport().add_getSource(target);
    String controlName = m_wrapper.getControlMethod().getName();
    return TemplateUtils.format("{0}.{1}()", wrapperSource, controlName);
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    m_invocation = (MethodInvocation) expression;
    // prepare viewer expression
    Expression viewerCreation = m_invocation.getExpression();
    // configure viewer
    JavaInfo wrapperInfo = m_wrapper.getWrapperInfo();
    wrapperInfo.getCreationSupport().add_setSourceExpression(viewerCreation);
    wrapperInfo.addRelatedNode(viewerCreation);
    wrapperInfo.setAssociation(wrapperInfo.getCreationSupport().getAssociation());
    // ensure local/field variable for viewer
    {
      wrapperInfo.setVariableSupport(new EmptyVariableSupport(wrapperInfo, viewerCreation));
      if (shouldUseFieldForWrapper()) {
        wrapperInfo.getVariableSupport().convertLocalToField();
      } else {
        wrapperInfo.getVariableSupport().convertFieldToLocal();
      }
    }
    // add viewer to control
    m_javaInfo.addChild(wrapperInfo);
    // configure control
    m_javaInfo.setCreationSupport(newControlCreationSupport());
    m_javaInfo.bindToExpression(m_invocation);
  }

  private boolean shouldUseFieldForWrapper() {
    JavaInfo wrapperInfo = m_wrapper.getWrapperInfo();
    GenerationSettings settings = wrapperInfo.getDescription().getToolkit().getGenerationSettings();
    return settings.getVariable(wrapperInfo) instanceof FieldUniqueVariableDescription;
  }

  protected CreationSupport newControlCreationSupport() {
    return new WrapperMethodControlCreationSupport(m_wrapper);
  }
}
