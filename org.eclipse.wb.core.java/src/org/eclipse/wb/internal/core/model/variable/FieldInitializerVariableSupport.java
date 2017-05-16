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

import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * {@link VariableSupport} implementation for field that has initializer.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class FieldInitializerVariableSupport extends FieldVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldInitializerVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public FieldInitializerVariableSupport(JavaInfo javaInfo, Expression variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "field-initializer: " + getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setName(String newName) throws Exception {
    modifyName(newName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    List<Statement> statements = getRelatedStatements();
    if (!statements.isEmpty()) {
      // sort statements
      Collections.sort(statements, AstNodeUtils.SORT_BY_POSITION);
      // target = before first statement
      Statement targetStatement = statements.get(0);
      return new StatementTarget(targetStatement, true);
    } else {
      // prepare first method with compatible static/instance modifier
      MethodDeclaration targetMethod = getTargetMethod();
      Assert.isNotNull(targetMethod, "Unable to find target method for " + m_javaInfo);
      // if this method is constructor with super() invocation, add after it
      {
        List<Statement> targetStatements = DomGenerics.statements(targetMethod);
        Statement firstTargetStatement = GenericsUtils.getFirstOrNull(targetStatements);
        if (firstTargetStatement instanceof SuperConstructorInvocation) {
          return new StatementTarget(firstTargetStatement, false);
        }
      }
      // target = beginning of first method with compatible static/instance modifier
      return new StatementTarget(targetMethod, true);
    }
  }

  /**
   * @return the {@link MethodDeclaration} for {@link StatementTarget}. It should be compatible with
   *         static/instance modified of this {@link FieldDeclaration}.
   */
  private MethodDeclaration getTargetMethod() {
    // check if field is static/instance
    final boolean staticField;
    {
      FieldDeclaration field = AstNodeUtils.getEnclosingNode(m_variable, FieldDeclaration.class);
      staticField = Modifier.isStatic(field.getModifiers());
    }
    // first try to use method of "root" JavaInfo
    {
      int position = m_javaInfo.getRootJava().getCreationSupport().getNode().getStartPosition();
      MethodDeclaration enclosingMethod = m_javaInfo.getEditor().getEnclosingMethod(position);
      if (enclosingMethod != null
          && (staticField || !Modifier.isStatic(enclosingMethod.getModifiers()))) {
        return enclosingMethod;
      }
    }
    // well, visit execution flow and find first compatible method
    final MethodDeclaration[] targetMethod = new MethodDeclaration[1];
    ExecutionFlowUtils.visit(
        new VisitingContext(true),
        getFlowDescription(),
        new ExecutionFlowFrameVisitor() {
          @Override
          public boolean enterFrame(ASTNode node) {
            if (node instanceof MethodDeclaration) {
              MethodDeclaration methodDeclaration = (MethodDeclaration) node;
              if (staticField || !Modifier.isStatic(methodDeclaration.getModifiers())) {
                targetMethod[0] = methodDeclaration;
              }
            }
            return targetMethod[0] == null;
          }
        });
    return targetMethod[0];
  }

  @Override
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    moveStatements(target);
  }

  @Override
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    List<Statement> statements = getRelatedStatements();
    if (!statements.isEmpty()) {
      return getStatementTarget();
    } else {
      return target;
    }
  }

  /**
   * @return the related {@link Statement}'s, that should be moved with component.
   */
  private List<Statement> getRelatedStatements() {
    List<Statement> statements = Lists.newArrayList();
    // add association statement (when we move component, association may not exist)
    if (m_javaInfo.getAssociation() != null) {
      Statement associationStatement = m_javaInfo.getAssociation().getStatement();
      if (associationStatement != null) {
        statements.add(associationStatement);
      }
    }
    // add related statements
    for (ASTNode relatedNode : m_javaInfo.getRelatedNodes()) {
      // add Statement with MethodInvocation
      {
        MethodInvocation invocation = m_javaInfo.getMethodInvocation(relatedNode);
        if (invocation != null
            && invocation.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
          statements.add((Statement) invocation.getParent());
          continue;
        }
      }
      // add Statement with Assignment
      {
        ASTNode fieldAccess = AstNodeUtils.getFieldAssignment(relatedNode);
        if (fieldAccess != null) {
          Assignment fieldAssignment = (Assignment) fieldAccess.getParent();
          if (fieldAssignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
            statements.add((Statement) fieldAssignment.getParent());
            continue;
          }
        }
      }
    }
    return statements;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
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
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
    // add field
    VariableDeclarationFragment fragment;
    {
      NodeTarget creationTarget = getCreationTarget(associationTarget);
      String initializer = m_javaInfo.getCreationSupport().add_getSource(creationTarget);
      initializer = AssociationUtils.replaceTemplates(m_javaInfo, initializer, creationTarget);
      fragment = addUniqueField(associationTarget.getPosition(), initializer);
    }
    // initialize variable and creation
    add_setVariableAndInitializer(fragment.getName(), fragment.getInitializer());
    // no assignment statement
    return null;
  }

  /**
   * @return the {@link NodeTarget} that specifies location of new {@link FieldDeclaration}, i.e.
   *         location where {@link CreationSupport#add_getSource(NodeTarget)} will be used.
   */
  private static NodeTarget getCreationTarget(StatementTarget associationTarget) {
    TypeDeclaration targetTypeDeclaration =
        AstNodeUtils.getEnclosingType(associationTarget.getNode());
    BodyDeclarationTarget newFieldTarget = new BodyDeclarationTarget(targetTypeDeclaration, true);
    return new NodeTarget(newFieldTarget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deleteAfter() throws Exception {
    if (m_javaInfo.isRoot()) {
      return;
    }
    delete_removeDeclarationField();
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param position
   *          is location in source where this field will be referenced, used to decide if field
   *          should be static or instance. FIXME probably should be replaced with
   *          {@link StatementTarget}.
   * @param initializer
   *          the optional (can be <code>null</code>) initializer of field
   *
   * @return {@link VariableDeclarationFragment} of new field with unique name and given
   *         initializer.
   */
  private VariableDeclarationFragment addUniqueField(int position, String initializer)
      throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare field source
    String fieldSource;
    {
      // prepare class and field names
      String className = m_javaInfo.getDescription().getComponentClass().getName();
      String fieldName = editor.getUniqueVariableName(-1, NamesManager.getName(m_javaInfo), null);
      // prepare modifiers
      String modifiers = perfFieldModifier(m_javaInfo);
      if (m_forceStaticModifier || isStaticContext(position)) {
        modifiers += "static ";
      }
      modifiers += "final ";
      // compose field source
      fieldSource = modifiers + className + " " + fieldName;
      if (initializer != null) {
        initializer = StringUtils.replace(initializer, "%variable-name%", fieldName);
        fieldSource += " = " + initializer;
      }
    }
    // add field
    FieldDeclaration fieldDeclaration = addField(fieldSource + ";");
    return (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Static modifier
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_forceStaticModifier;

  /**
   * Specifies if <code>static</code> modifier should be used, even if position for component is not
   * a static context.
   */
  public void setForceStaticModifier(boolean forceStaticModifier) {
    m_forceStaticModifier = forceStaticModifier;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BASE = "variable.fieldInitializer.";
  public static final String P_PREFIX_THIS = BASE + "prefixThis";
  public static final String P_FIELD_MODIFIER = BASE + "fieldModifier";

  @Override
  protected boolean prefixThis() {
    return getPreferences().getBoolean(P_PREFIX_THIS);
  }

  public static String perfFieldModifier(JavaInfo javaInfo) {
    IPreferenceStore preferences = javaInfo.getDescription().getToolkit().getPreferences();
    return V_MODIFIER_CODE[preferences.getInt(P_FIELD_MODIFIER)];
  }
}
