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
package org.eclipse.wb.internal.core.utils.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.GenericsUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper that "generifies" JDT Core DOM API.
 * <p>
 * It allows to have all unsafe casts in single place.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public class DomGenerics {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private DomGenerics() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CompilationUnit
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Comment> getCommentList(CompilationUnit unit) {
    return unit.getCommentList();
  }

  @SuppressWarnings("unchecked")
  public static List<TagElement> tags(Javadoc javadoc) {
    return javadoc.tags();
  }

  @SuppressWarnings("unchecked")
  public static List<ASTNode> fragments(TagElement tagElement) {
    return tagElement.fragments();
  }

  @SuppressWarnings("unchecked")
  public static List<ImportDeclaration> imports(CompilationUnit unit) {
    return unit.imports();
  }

  @SuppressWarnings("unchecked")
  public static List<TypeDeclaration> types(CompilationUnit unit) {
    return unit.types();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TypeDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Type> superInterfaces(TypeDeclaration typeDeclaration) {
    return typeDeclaration.superInterfaceTypes();
  }

  @SuppressWarnings("unchecked")
  public static List<BodyDeclaration> bodyDeclarations(TypeDeclaration typeDeclaration) {
    return typeDeclaration.bodyDeclarations();
  }

  /**
   * @return {@link Initializer}'s of given {@link TypeDeclaration}.
   */
  public static List<Initializer> initializers(TypeDeclaration typeDeclaration, boolean aStatic) {
    List<Initializer> initializers = Lists.newArrayList();
    for (BodyDeclaration bodyDeclaration : bodyDeclarations(typeDeclaration)) {
      if (bodyDeclaration instanceof Initializer) {
        Initializer initializer = (Initializer) bodyDeclaration;
        boolean isStatic = java.lang.reflect.Modifier.isStatic(initializer.getModifiers());
        if (aStatic && isStatic || !aStatic && !isStatic) {
          initializers.add(initializer);
        }
      }
    }
    return initializers;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Name> thrownExceptions(MethodDeclaration methodDeclaration) {
    return methodDeclaration.thrownExceptions();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AnonymousClassDeclaration
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<BodyDeclaration> bodyDeclarations(AnonymousClassDeclaration anonymousDeclaration) {
    return anonymousDeclaration.bodyDeclarations();
  }

  public static List<MethodDeclaration> methodDeclarations(AnonymousClassDeclaration anonymousDeclaration) {
    List<BodyDeclaration> bodyDeclarations = bodyDeclarations(anonymousDeclaration);
    return GenericsUtils.select(bodyDeclarations, MethodDeclaration.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Block
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Statement> statements(Block block) {
    return block.statements();
  }

  public static List<Statement> statements(MethodDeclaration method) {
    if (method == null) {
      return ImmutableList.of();
    }
    Block body = method.getBody();
    if (body == null) {
      return ImmutableList.of();
    }
    return statements(body);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Annotations
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<IExtendedModifier> modifiers(BodyDeclaration bodyDeclaration) {
    return bodyDeclaration.modifiers();
  }

  @SuppressWarnings("unchecked")
  public static List<ASTNode> modifiersNodes(BodyDeclaration bodyDeclaration) {
    return bodyDeclaration.modifiers();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // parameters
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<SingleVariableDeclaration> parameters(MethodDeclaration methodDeclaration) {
    return methodDeclaration.parameters();
  }

  @SuppressWarnings("unchecked")
  public static List<TypeParameter> typeParameters(MethodDeclaration methodDeclaration) {
    return methodDeclaration.typeParameters();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // arguments
  //
  ////////////////////////////////////////////////////////////////////////////
  public static List<Expression> arguments(ASTNode node) {
    if (node instanceof MethodInvocation) {
      return arguments((MethodInvocation) node);
    }
    if (node instanceof SuperMethodInvocation) {
      return arguments((SuperMethodInvocation) node);
    }
    if (node instanceof ClassInstanceCreation) {
      return arguments((ClassInstanceCreation) node);
    }
    if (node instanceof ConstructorInvocation) {
      return arguments((ConstructorInvocation) node);
    }
    if (node instanceof SuperConstructorInvocation) {
      return arguments((SuperConstructorInvocation) node);
    }
    throw new IllegalArgumentException(node + " does not have arguments.");
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> arguments(MethodInvocation invocation) {
    return invocation.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> arguments(SuperMethodInvocation invocation) {
    return invocation.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> arguments(ClassInstanceCreation creation) {
    return creation.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> arguments(ConstructorInvocation invocation) {
    return invocation.arguments();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> arguments(SuperConstructorInvocation invocation) {
    return invocation.arguments();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // fragments
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<VariableDeclarationFragment> fragments(FieldDeclaration fieldDeclaration) {
    return fieldDeclaration.fragments();
  }

  @SuppressWarnings("unchecked")
  public static List<VariableDeclarationFragment> fragments(VariableDeclarationStatement statement) {
    return statement.fragments();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Expression> expressions(ArrayInitializer arrayInitializer) {
    return arrayInitializer.expressions();
  }

  @SuppressWarnings("unchecked")
  public static List<Expression> extendedOperands(InfixExpression infixExpression) {
    return infixExpression.extendedOperands();
  }

  public static List<Expression> allOperands(InfixExpression infixExpression) {
    List<Expression> operands = new ArrayList<Expression>();
    operands.add(infixExpression.getLeftOperand());
    operands.add(infixExpression.getRightOperand());
    operands.addAll(extendedOperands(infixExpression));
    return operands;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // types
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Type> typeArguments(ParameterizedType parameterizedType) {
    return parameterizedType.typeArguments();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ArrayCreation
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<Expression> dimensions(ArrayCreation arrayCreation) {
    return arrayCreation.dimensions();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Enums
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the list of {@link EnumDeclaration}'s if the given {@link TypeDeclaration} has declared
   *         enums within.
   */
  @SuppressWarnings("rawtypes")
  public static List<EnumDeclaration> getEnums(TypeDeclaration typeDeclaration) {
    List<EnumDeclaration> enumDeclarations = Lists.newArrayList();
    List declarations = typeDeclaration.bodyDeclarations();
    for (Object object : declarations) {
      if (object instanceof EnumDeclaration) {
        enumDeclarations.add((EnumDeclaration) object);
      }
    }
    return enumDeclarations;
  }

  /**
   * @return the list of enum constants.
   */
  @SuppressWarnings("unchecked")
  public static List<ASTNode> getEnumConstants(EnumDeclaration enumDeclaration) {
    return enumDeclaration.enumConstants();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TryStatement
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static List<CatchClause> catchClauses(TryStatement tryStatement) {
    return tryStatement.catchClauses();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Expression} of given {@link ExpressionStatement}.
   */
  public static Expression getExpression(Statement statement) {
    return ((ExpressionStatement) statement).getExpression();
  }
}
