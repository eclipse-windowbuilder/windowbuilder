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
import com.google.common.collect.Maps;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Abstract {@link VariableSupport} implementation for variable with name.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class AbstractNamedVariableSupport extends VariableSupport {
  protected Expression m_variable;
  protected VariableDeclaration m_declaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractNamedVariableSupport(JavaInfo javaInfo, Expression variable) {
    super(javaInfo);
    m_variable = variable;
    rememberDeclaration();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean hasName() {
    return m_variable != null;
  }

  @Override
  public final String getName() {
    if (m_variable == null) {
      return "no-variable-yet";
    }
    return AstNodeUtils.getVariableName(m_variable);
  }

  @Override
  public String getTitle() throws Exception {
    return getName();
  }

  @Override
  public String getComponentName() {
    return getName();
  }

  /**
   * Sets new unique/non-conflicting name generated from given "base".
   */
  public final void setNameBase(String nameBase) throws Exception {
    int position = m_variable.getStartPosition();
    String newName = m_javaInfo.getEditor().getUniqueVariableName(position, nameBase, null);
    setName(newName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private final VariableProperty m_variableProperty = new VariableProperty(this);

  @Override
  public final void addProperties(List<Property> properties) {
    properties.add(m_variableProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils: environment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ExecutionFlowDescription} from {@link EditorState}.
   */
  protected final ExecutionFlowDescription getFlowDescription() {
    AstEditor editor = m_javaInfo.getEditor();
    EditorState state = EditorState.get(editor);
    return state.getFlowDescription();
  }

  /**
   * @return the {@link TypeDeclaration} of this component. XXX
   */
  protected final TypeDeclaration getTypeDeclaration() {
    return getFlowDescription().geTypeDeclaration();
  }

  /**
   * We are going to create/access field and need to know if at position of component creation (so
   * assignment) we work within static or instance context (for example in <code>main</code>).
   *
   * @return <code>true</code> if we work within static context.
   */
  protected final boolean isStaticContext() {
    int position = m_javaInfo.getCreationSupport().getNode().getStartPosition();
    return isStaticContext(position);
  }

  /**
   * We are going to create/access field and need to know if at position of assignment we work
   * within static or instance context (for example in <code>main</code>).
   *
   * @param position
   *          the position in source where we are going to access field.
   *
   * @return <code>true</code> if we work within static context.
   */
  protected final boolean isStaticContext(int position) {
    ASTNode enclosingNode = m_javaInfo.getEditor().getEnclosingNode(position);
    TypeDeclaration type = getTypeDeclaration();
    MethodDeclaration method = AstNodeUtils.getEnclosingMethod(type, enclosingNode);
    return AstNodeUtils.isStatic(method);
  }

  /**
   * Remembers into field {@link VariableDeclaration} for current {@link #m_variable}.
   */
  protected final void rememberDeclaration() {
    if (m_variable != null) {
      ExecutionFlowDescription flowDescription = getFlowDescription();
      if (flowDescription != null) {
        m_declaration = ExecutionFlowUtils.getDeclaration(flowDescription, m_variable);
      }
    }
  }

  /**
   * @return the references on this variable.
   */
  protected final List<Expression> getReferences() {
    return ExecutionFlowUtils.getReferences(getFlowDescription(), m_variable);
  }

  /**
   * @return the references on this {@link JavaInfo} using this variable.
   */
  protected final List<Expression> getComponentReferences() {
    List<Expression> componentReferences = Lists.newArrayList();
    for (Expression reference : getReferences()) {
      if (m_javaInfo.isRepresentedBy(reference)) {
        componentReferences.add(reference);
      }
    }
    return componentReferences;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils: name validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * XXX Kosta.20072024. Note, that this method was implemented (partly, without toLocal/toField),
   * but then I've decided that such strong validation is not a feature, as it will make user
   * operations harder.
   *
   * Validates that given combination of variables and their new names is valid.
   *
   * @param variablesNames
   *          the map: variable -> new name.
   * @param toLocalVariables
   *          the set variables that are currently fields, but will be converted to locals.
   * @param toFieldVariables
   *          the set variables that are currently locales, but will be converted to fields.
   *
   * @return the message with problem description, or <code>null</code> is variable name is valid.
   */
  public static String validateVariables(Map<AbstractNamedVariableSupport, String> variablesNames,
      Set<AbstractNamedVariableSupport> toLocalVariables,
      Set<AbstractNamedVariableSupport> toFieldVariables) {
    // check that identifier are valid
    for (String name : variablesNames.values()) {
      IStatus status = JavaConventions.validateIdentifier(name);
      if (status.matches(IStatus.ERROR)) {
        return status.getMessage();
      }
    }
    // prepare map: variable declaration -> name
    Map<VariableDeclaration, String> declarationsNames = Maps.newHashMap();
    for (Map.Entry<AbstractNamedVariableSupport, String> entry : variablesNames.entrySet()) {
      AbstractNamedVariableSupport variable = entry.getKey();
      String name = entry.getValue();
      declarationsNames.put(variable.m_declaration, name);
    }
    // check each variable
    for (Map.Entry<AbstractNamedVariableSupport, String> entry : variablesNames.entrySet()) {
      AbstractNamedVariableSupport variable = entry.getKey();
      String name = entry.getValue();
      // prepare environment
      CompilationUnit astUnit = variable.m_javaInfo.getEditor().getAstUnit();
      int position = variable.m_variable.getStartPosition();
      // check variables visible at this position
      {
        List<VariableDeclaration> declarations =
            AstNodeUtils.getVariableDeclarationsVisibleAt(astUnit, position);
        // validate visible declarations
        for (VariableDeclaration declaration : declarations) {
          if (getDeclarationName(declaration, declarationsNames).equals(name)) {
            return MessageFormat.format(
                "Variable \"{0}\" is already visible at this position.",
                name);
          }
        }
      }
      // check variables declared after this position
      {
        List<VariableDeclaration> declarations =
            AstNodeUtils.getVariableDeclarationsAfter(astUnit, position);
        // validate shadowed declarations
        for (VariableDeclaration declaration : declarations) {
          if (getDeclarationName(declaration, declarationsNames).equals(name)) {
            return MessageFormat.format(
                "Variable \"{0}\" conflicts with other variable declared after it.",
                name);
          }
        }
      }
    }
    // OK
    return null;
  }

  /**
   * Returns that name of {@link VariableDeclaration} when we know that some
   * {@link VariableDeclaration} will be requested to change name.
   *
   * @param declaration
   *          the {@link VariableDeclaration} to get name.
   * @param declarationsNames
   *          the {@link Map} of variables into new names.
   *
   * @return the actual name of {@link VariableDeclaration}.
   */
  private static String getDeclarationName(VariableDeclaration declaration,
      Map<VariableDeclaration, String> declarationsNames) {
    String newName = declarationsNames.get(declaration);
    return newName != null ? newName : declaration.getName().getIdentifier();
  }

  /**
   * Validates that given name is valid for this {@link VariableSupport}.
   *
   * @return the message with problem description, or <code>null</code> is variable name is valid.
   */
  public final String validateName(String name) throws Exception {
    // check that identifier is valid
    {
      IStatus status = JavaConventions.validateIdentifier(name);
      if (status.matches(IStatus.ERROR)) {
        return status.getMessage();
      }
    }
    // check that variable is unique
    {
      CompilationUnit astUnit = m_javaInfo.getEditor().getAstUnit();
      int position = m_variable.getStartPosition();
      // check variables visible at this position
      {
        // prepare visible declarations
        List<VariableDeclaration> declarations =
            AstNodeUtils.getVariableDeclarationsVisibleAt(astUnit, position);
        // check each declaration
        for (VariableDeclaration declaration : declarations) {
          if (declaration.getName().getIdentifier().equals(name)) {
            return MessageFormat.format(
                "Variable \"{0}\" is already visible at this position.",
                name);
          }
        }
      }
      // check variables declared after this position
      {
        // prepare shadowing declarations
        List<VariableDeclaration> declarations =
            AstNodeUtils.getVariableDeclarationsAfter(astUnit, position);
        // check each declaration
        for (VariableDeclaration declaration : declarations) {
          if (declaration.getName().getIdentifier().equals(name)) {
            return MessageFormat.format(
                "Variable \"{0}\" conflicts with other variable declared after it.",
                name);
          }
        }
      }
    }
    // OK
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils: name modifications
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new identifier for this variable.
   */
  protected final void modifyName(String newIdentifier) throws Exception {
    String oldName = getName();
    for (Expression reference : getReferences()) {
      modifyVariableName(reference, newIdentifier);
    }
    m_javaInfo.getBroadcastJava().variable_setName(this, oldName, newIdentifier);
  }

  /**
   * Changes identifier in given variable {@link Expression}.
   */
  protected final void modifyVariableName(Expression variable, String newIdentifier)
      throws Exception {
    SimpleName simpleName = AstNodeUtils.getVariableSimpleName(variable);
    m_javaInfo.getEditor().setIdentifier(simpleName, newIdentifier);
  }

  /**
   * Replaces name in all {@link Expression}'s referenced on this component with new name. We do
   * this after creating unique field for previously reused variable.
   */
  protected final void replaceComponentReferences(String newVariableName) throws Exception {
    for (Expression reference : getComponentReferences()) {
      modifyVariableName(reference, newVariableName);
    }
  }
}
