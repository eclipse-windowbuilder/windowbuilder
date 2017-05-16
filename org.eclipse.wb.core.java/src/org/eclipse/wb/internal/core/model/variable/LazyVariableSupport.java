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
package org.eclipse.wb.internal.core.model.variable;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.preference.IPreferenceStore;

import java.util.List;

/**
 * {@link VariableSupport} implementation for "lazy creation" pattern.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class LazyVariableSupport extends AbstractSimpleVariableSupport {
  public MethodDeclaration m_accessor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LazyVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public LazyVariableSupport(JavaInfo javaInfo, LazyVariableInformation lazyVariableInformation) {
    super(javaInfo, lazyVariableInformation.m_variable);
    m_accessor = lazyVariableInformation.m_accessor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "lazy: " + m_variable + " " + getAccessorSignature();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isJavaInfo(ASTNode node) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      String signature = AstNodeUtils.getMethodSignature(invocation);
      if (getAccessorSignature().equals(signature)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isValidStatementForChild(Statement statement) {
    // children should be added directly in accessor
    return AstNodeUtils.getEnclosingMethod(statement) == m_accessor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setName(String newName) throws Exception {
    // check may be we should rename method
    {
      String actualMethodName = m_accessor.getName().getIdentifier();
      String expectedMethodName =
          LazyVariableSupportUtils.getExpectedMethodName(m_javaInfo, getName());
      if (expectedMethodName.equals(actualMethodName)) {
        String newMethodName = LazyVariableSupportUtils.getExpectedMethodName(m_javaInfo, newName);
        List<MethodInvocation> invocations = AstNodeUtils.getMethodInvocations(m_accessor);
        AstEditor editor = m_javaInfo.getEditor();
        // change name in declaration
        editor.replaceMethodName(m_accessor, newMethodName);
        // change name in invocations
        for (MethodInvocation invocation : invocations) {
          editor.replaceInvocationName(invocation, newMethodName);
        }
      }
    }
    // rename field
    modifyName(newName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasExpression(NodeTarget target) {
    return true;
  }

  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    if (isTargetInAccessor(target)) {
      return getName();
    } else {
      return getAccessorReferenceExpression();
    }
  }

  /**
   * @return the "external" reference on component, using accessor.
   */
  public String getAccessorReferenceExpression() {
    return m_accessor.getName().getIdentifier() + "()";
  }

  /**
   * @return <code>true</code> if given {@link NodeTarget} specifies node in {@link #m_accessor}.
   */
  private boolean isTargetInAccessor(NodeTarget target) {
    StatementTarget statementTarget = target.getStatementTarget();
    if (statementTarget != null) {
      return AstNodeUtils.contains(m_accessor, statementTarget.getNode());
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canConvertLocalToField() {
    return false;
  }

  @Override
  public void convertLocalToField() throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public boolean canConvertFieldToLocal() {
    return false;
  }

  @Override
  public void convertFieldToLocal() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    // lazy component is ready at any place
  }

  @Override
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    return target;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    boolean isStatic = isStaticContext(associationTarget.getPosition());
    // prepare component class
    String className =
        ReflectionUtils.getCanonicalName(m_javaInfo.getDescription().getComponentClass());
    // add field
    String fieldName;
    FieldDeclaration field;
    {
      // prepare modifiers
      String modifiers = "private ";
      if (isStatic) {
        modifiers += "static ";
      }
      // do add
      fieldName = editor.getUniqueVariableName(-1, NamesManager.getName(m_javaInfo), null);
      field = addField(modifiers + className + " " + fieldName + ";");
    }
    // add method
    {
      String methodName = LazyVariableSupportUtils.getExpectedMethodName(m_javaInfo, fieldName);
      methodName = editor.getUniqueMethodName(methodName);
      // prepare target
      BodyDeclarationTarget bodyTarget;
      {
        TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingType(field);
        bodyTarget = new BodyDeclarationTarget(typeDeclaration, false);
      }
      // add accessor method
      {
        NodeTarget creationTarget = new NodeTarget(bodyTarget);
        String initializer = m_javaInfo.getCreationSupport().add_getSource(creationTarget);
        initializer = AssociationUtils.replaceTemplates(m_javaInfo, initializer, creationTarget);
        // prepare modifiers
        String modifiers = prefMethodModifier(m_javaInfo);
        if (isStatic) {
          modifiers += "static ";
        }
        //
        String header = modifiers + className + " " + methodName + "()";
        List<String> bodyLines = Lists.newArrayList();
        bodyLines.add("if (" + fieldName + " == null) {");
        bodyLines.add("\t" + fieldName + " = " + initializer + ";");
        bodyLines.add("}");
        bodyLines.add("return " + fieldName + ";");
        m_accessor = editor.addMethodDeclaration(header, bodyLines, bodyTarget);
      }
      // include accessor into execution flow, to allow visiting its nodes
      JavaInfoUtils.getState(m_javaInfo).getFlowDescription().addStartMethod(m_accessor);
      // initialize variable and creation
      {
        IfStatement ifStatement = (IfStatement) m_accessor.getBody().statements().get(0);
        Block thenBlock = (Block) ifStatement.getThenStatement();
        ExpressionStatement expressionStatement =
            (ExpressionStatement) thenBlock.statements().get(0);
        Assignment assignment = (Assignment) expressionStatement.getExpression();
        add_setVariableAndInitializer(assignment.getLeftHandSide(), assignment.getRightHandSide());
      }
    }
    // no variable statement
    return null;
  }

  @Override
  protected void add_setVariableParameterizedType(Expression initializer) throws Exception {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(initializer);
    if (typeBinding.isParameterizedType()) {
      AstEditor editor = m_javaInfo.getEditor();
      String genericTypeName = editor.getTypeBindingSource(typeBinding);
      editor.replaceVariableType(m_declaration, genericTypeName);
      editor.replaceMethodType(m_accessor, genericTypeName);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deleteAfter() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // remove field
    editor.removeVariableDeclaration(m_declaration);
    // remove accessor method
    editor.removeBodyDeclaration(m_accessor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setType(String newTypeName) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    editor.replaceVariableType(m_declaration, newTypeName);
    editor.replaceMethodType(m_accessor, newTypeName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the signature for accessor method.
   */
  private String getAccessorSignature() {
    return AstNodeUtils.getMethodSignature(m_accessor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LazyVariableInformation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Holder for information about lazy creation variable.
   *
   * @author scheglov_ke
   */
  public static final class LazyVariableInformation {
    private final MethodDeclaration m_accessor;
    private final Expression m_variable;
    private final Expression m_creation;

    public LazyVariableInformation(MethodDeclaration accessor,
        Expression variable,
        Expression creation) {
      m_accessor = accessor;
      m_variable = variable;
      m_creation = creation;
    }

    public Expression getCreation() {
      return m_creation;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BASE = "variable.lazy.";
  public static final String[] V_MODIFIER_CODE = {"private ", "", "protected ", "public "};
  public static final String P_METHOD_MODIFIER = BASE + "methodModifier";
  public static final int V_METHOD_MODIFIER_PRIVATE = 0;
  public static final int V_METHOD_MODIFIER_PACKAGE = 1;
  public static final int V_METHOD_MODIFIER_PROTECTED = 2;
  public static final int V_METHOD_MODIFIER_PUBLIC = 3;

  /**
   * @return method modifier for source code.
   */
  public static String prefMethodModifier(JavaInfo javaInfo) {
    IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
    return V_MODIFIER_CODE[preferences.getInt(P_METHOD_MODIFIER)];
  }
}
