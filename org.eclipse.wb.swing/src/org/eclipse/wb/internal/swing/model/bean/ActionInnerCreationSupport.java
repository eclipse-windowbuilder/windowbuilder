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
package org.eclipse.wb.internal.swing.model.bean;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

/**
 * Implementation of {@link CreationSupport} for {@link Action} as inner class.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ActionInnerCreationSupport extends ActionAbstractCreationSupport {
  private MethodDeclaration m_typeConstructor;
  private SuperConstructorInvocation m_superInvocation;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionInnerCreationSupport() throws Exception {
    super();
  }

  public ActionInnerCreationSupport(ClassInstanceCreation creation) throws Exception {
    super(creation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "innerAction";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setCreationEx() {
    super.setCreationEx();
    m_typeDeclaration = AstNodeUtils.getTypeDeclaration(m_creation);
    m_typeConstructor =
        AstNodeUtils.getMethodBySignature(
            m_typeDeclaration,
            AstNodeUtils.getCreationSignature(m_creation));
  }

  @Override
  protected void addInitializationBlocks() {
    super.addInitializationBlocks();
    if (m_typeConstructor != null) {
      m_initializingBlocks.add(m_typeConstructor.getBody());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractAction create_createObject(EvaluationContext context) throws Exception {
    return new AbstractAction() {
      private static final long serialVersionUID = 0L;

      public void actionPerformed(ActionEvent e) {
        String message = "Action \"" + m_javaInfo.getVariableSupport().getName() + "\" performed.";
        JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
      }
    };
  }

  @Override
  protected void create_evaluateStatement(EvaluationContext context,
      AbstractAction action,
      Statement statement) throws Exception {
    if (statement instanceof SuperConstructorInvocation) {
      SuperConstructorInvocation invocation = (SuperConstructorInvocation) statement;
      updateAction_SuperConstructorInvocation(context, action, invocation);
    } else {
      super.create_evaluateStatement(context, action, statement);
    }
  }

  /**
   * Updates {@link Action} instance using {@link SuperConstructorInvocation}.
   */
  private void updateAction_SuperConstructorInvocation(EvaluationContext context,
      AbstractAction action,
      SuperConstructorInvocation invocation) throws Exception {
    m_superInvocation = invocation;
    ConstructorDescription constructor = getConstructorDescription();
    if (constructor != null) {
      List<Expression> arguments = DomGenerics.arguments(invocation);
      evaluateConstructorArguments(context, action, constructor, arguments);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getSource(NodeTarget target) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // fill m_typeDeclaration with target type
    m_typeDeclaration = EditorState.get(editor).getFlowDescription().geTypeDeclaration();
    // prepare target for new Action type
    BodyDeclarationTarget innerTarget = new BodyDeclarationTarget(m_typeDeclaration, false);
    /*NewBodyDeclarationTarget target = new NewBodyDeclarationTarget(m_typeDeclaration, true);
    for (BodyDeclaration bodyDeclaration : DomGenerics.bodyDeclarations(m_typeDeclaration)) {
    	if (bodyDeclaration instanceof TypeDeclaration) {
    		TypeDeclaration typeDeclaration = (TypeDeclaration) bodyDeclaration;
    		ITypeBinding typeBinding = ASTNodeUtilities.getTypeBinding(typeDeclaration);
    		if (ASTNodeUtilities.isSuccessorOf(typeBinding, "javax.swing.AbstractAction")) {
    			target = new NewBodyDeclarationTarget(bodyDeclaration, false);
    			continue;
    		}
    	}
    }*/
    // add inner TypeDeclaration with Action
    String typeName = editor.getUniqueTypeName("SwingAction");
    {
      List<String> lines = Lists.newArrayList();
      lines.add("private class " + typeName + " extends javax.swing.AbstractAction {");
      lines.add("\tpublic " + typeName + "() {");
      lines.add("\t\tputValue(NAME, \"" + typeName + "\");");
      lines.add("\t\tputValue(SHORT_DESCRIPTION, \"Some short description\");");
      lines.add("\t}");
      lines.add("\tpublic void actionPerformed(java.awt.event.ActionEvent e) {");
      lines.add("\t}");
      lines.add("}");
      editor.addTypeDeclaration(lines, innerTarget);
    }
    // create instance of Action
    return "new " + typeName + "()";
  }

  @Override
  public void add_setSourceExpression(Expression expression) throws Exception {
    m_javaInfo.bindToExpression(expression);
    setCreation((ClassInstanceCreation) expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getCreation() {
    return m_superInvocation;
  }

  public ConstructorDescription getConstructorDescription() {
    if (m_superInvocation != null) {
      IMethodBinding binding = AstNodeUtils.getSuperBinding(m_superInvocation);
      return m_typeDescription.getConstructor(binding);
    }
    return null;
  }
}
