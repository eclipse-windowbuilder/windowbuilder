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

import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * {@link VariableSupport} implementation for unique field.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class FieldUniqueVariableSupport extends FieldVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldUniqueVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public FieldUniqueVariableSupport(JavaInfo javaInfo, Expression variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "field-unique: " + getName();
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
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canConvertFieldToLocal() {
    // prepare set of methods that reference this field
    Set<MethodDeclaration> methods = Sets.newHashSet();
    {
      for (Expression reference : getReferences()) {
        // ignore field declaration
        if (reference.getParent() instanceof VariableDeclarationFragment) {
          continue;
        }
        // add method
        MethodDeclaration method = AstNodeUtils.getEnclosingMethod(reference);
        if (method != null) {
          methods.add(method);
        }
      }
    }
    // we can convert field to local only if it is referenced from single method (where it is assigned)
    return methods.size() == 1;
  }

  @Override
  public void convertFieldToLocal() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare current information
    VariableDeclaration oldFragment = m_declaration;
    FieldDeclaration oldField = (FieldDeclaration) oldFragment.getParent();
    String typeString = editor.getSource(oldField.getType());
    // variable is first place where JavaInfo is assigned
    Assignment assignment = (Assignment) m_variable.getParent();
    Assert.isTrue(assignment.getLeftHandSide() == m_variable);
    Expression oldInitializer = assignment.getRightHandSide();
    Statement oldStatement = (Statement) assignment.getParent();
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(oldField.getType());
    // rename variable to make it local-like
    String localName =
        m_utils.convertName(
            assignment.getStartPosition(),
            getName(),
            JavaCore.CODEASSIST_FIELD_PREFIXES,
            JavaCore.CODEASSIST_FIELD_SUFFIXES,
            JavaCore.CODEASSIST_LOCAL_PREFIXES,
            JavaCore.CODEASSIST_LOCAL_SUFFIXES,
            m_declaration);
    setName(localName);
    // replace "this.fieldName" with "localName"
    {
      for (Expression reference : getReferences()) {
        if (reference instanceof FieldAccess) {
          SimpleName simpleReference =
              parseVariableSimpleName(reference.getStartPosition(), localName, typeBinding);
          editor.replaceSubstring(reference, localName);
          AstEditor.replaceNode(reference, simpleReference);
          if (reference == m_variable) {
            m_variable = simpleReference;
          }
        }
      }
    }
    // add type source (before changes in AST because we insert new nodes)
    Type newType;
    {
      int oldStart = m_variable.getStartPosition();
      editor.replaceSubstring(oldStart, 0, typeString + " ");
      newType = editor.getParser().parseType(oldStart, oldField.getType());
    }
    // replace assignment with variable declaration
    SimpleName localVariable;
    {
      AST ast = m_variable.getAST();
      // prepare new fragment, reuse variable and initializer
      VariableDeclarationFragment newFragment = ast.newVariableDeclarationFragment();
      {
        assignment.setLeftHandSide(ast.newSimpleName("__foo"));
        editor.replaceSubstring(m_variable, localName);
        localVariable =
            parseVariableSimpleName(m_variable.getStartPosition(), localName, typeBinding);
        m_variable = localVariable;
        newFragment.setName(localVariable);
      }
      {
        assignment.setRightHandSide(ast.newSimpleName("__bar"));
        newFragment.setInitializer(oldInitializer);
      }
      AstNodeUtils.setSourceRange(newFragment, m_variable, oldInitializer);
      // prepare new statement
      VariableDeclarationStatement newStatement = ast.newVariableDeclarationStatement(newFragment);
      newStatement.setType(newType);
      AstNodeUtils.setSourceRange(newStatement, newType, oldStatement);
      // replace old statement in AST
      {
        List<Statement> statements = DomGenerics.statements((Block) oldStatement.getParent());
        int index = statements.indexOf(oldStatement);
        statements.set(index, newStatement);
      }
    }
    // remove old field
    editor.removeVariableDeclaration(oldFragment);
    // use local variable support
    m_javaInfo.setVariableSupport(new LocalUniqueVariableSupport(m_javaInfo, localVariable));
  }

  /**
   * @return the {@link SimpleName} variable with given name and type.
   */
  private SimpleName parseVariableSimpleName(int position,
      String localName,
      ITypeBinding typeBinding) {
    AstEditor editor = m_javaInfo.getEditor();
    return editor.getParser().parseVariable(
        position,
        localName,
        null,
        typeBinding,
        false,
        Modifier.NONE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
    boolean isStatic = isStaticContext(associationTarget.getPosition());
    // add field
    String fieldName;
    {
      String name = NamesManager.getName(m_javaInfo);
      fieldName = m_utils.getUniqueFieldName(name, null);
      String modifiers = perfFieldModifier(m_javaInfo);
      if (isStatic) {
        modifiers += "static ";
      }
      // add field
      String className = m_javaInfo.getDescription().getComponentClass().getName();
      addField(modifiers + className + " " + fieldName + ";");
    }
    // prepare code for field reference
    String fieldReference = fieldName;
    if (!isStatic && prefixThis()) {
      fieldReference = "this." + fieldName;
    }
    // prepare assignment statement
    NodeTarget creationTarget = new NodeTarget(associationTarget);
    String initializer = m_javaInfo.getCreationSupport().add_getSource(creationTarget);
    initializer = StringUtils.replace(initializer, "%variable-name%", fieldName);
    return fieldReference + " = " + initializer + ";";
  }

  @Override
  public void add_setVariableStatement(Statement statement) throws Exception {
    ExpressionStatement expressionStatement = (ExpressionStatement) statement;
    Assignment assignment = (Assignment) expressionStatement.getExpression();
    add_setVariableAndInitializer(assignment.getLeftHandSide(), assignment.getRightHandSide());
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
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BASE = "variable.fieldUnique.";
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
