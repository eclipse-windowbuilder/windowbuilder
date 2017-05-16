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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.apache.commons.lang.StringUtils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Contains different read-only AST operations.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public class AstNodeUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AstNodeUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comparators
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * A comparator that can be used to sort {@link ASTNode}'s by their start position, with the
   * left-most nodes sorting to the front of the list.
   */
  public static final Comparator<ASTNode> SORT_BY_POSITION = new Comparator<ASTNode>() {
    public int compare(ASTNode o1, ASTNode o2) {
      return o1.getStartPosition() - o2.getStartPosition();
    }
  };
  /**
   * A comparator that can be used to sort {@link ASTNode}'s by their start position, with the
   * left-most nodes sorting to the back of the list.
   */
  public static final Comparator<ASTNode> SORT_BY_REVERSE_POSITION = new Comparator<ASTNode>() {
    public int compare(ASTNode o1, ASTNode o2) {
      return o2.getStartPosition() - o1.getStartPosition();
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMethodBinding access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param invocation
   *          the not <code>null</code> {@link MethodInvocation}
   * @return not <code>null</code> {@link IMethodBinding} for given {@link MethodInvocation}.
   */
  public static IMethodBinding getMethodBinding(MethodInvocation invocation) {
    Assert.isNotNull(invocation);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      IMethodBinding binding =
          (IMethodBinding) invocation.getProperty(AstParser.KEY_METHOD_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding
    return invocation.resolveMethodBinding();
  }

  /**
   * @param invocation
   *          the not <code>null</code> {@link SuperMethodInvocation}.
   *
   * @return not <code>null</code> {@link IMethodBinding} for given {@link SuperMethodInvocation}.
   */
  public static IMethodBinding getMethodBinding(SuperMethodInvocation invocation) {
    Assert.isNotNull(invocation);
    // try to get binding from property (copy of binding added earlier)
    {
      IMethodBinding binding =
          (IMethodBinding) invocation.getProperty(AstParser.KEY_METHOD_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding
    return invocation.resolveMethodBinding();
  }

  /**
   * @param methodDeclaration
   *          the not <code>null</code> {@link MethodDeclaration}
   *
   * @return the {@link IMethodBinding} for given {@link MethodDeclaration}, may be
   *         <code>null</code> if compilation errors.
   */
  public static IMethodBinding getMethodBinding(MethodDeclaration methodDeclaration) {
    Assert.isNotNull(methodDeclaration);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      IMethodBinding binding =
          (IMethodBinding) methodDeclaration.getProperty(AstParser.KEY_METHOD_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding, may be "null" (if compilation errors)
    return methodDeclaration.resolveBinding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVariableBinding access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param simpleName
   *          the not <code>null</code> {@link SimpleName}
   *
   * @return {@link IVariableBinding} or <code>null</code> for given {@link SimpleName}.
   */
  public static IVariableBinding getVariableBinding(ASTNode node) {
    Assert.isNotNull(node);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      IVariableBinding binding =
          (IVariableBinding) node.getProperty(AstParser.KEY_VARIABLE_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // VariableDeclaration
    if (node instanceof VariableDeclaration) {
      VariableDeclaration variableDeclaration = (VariableDeclaration) node;
      IVariableBinding binding = variableDeclaration.resolveBinding();
      if (binding != null) {
        return binding;
      }
    }
    // check for SimpleName
    if (node instanceof SimpleName) {
      SimpleName simpleName = (SimpleName) node;
      // get standard binding
      {
        IBinding binding = simpleName.resolveBinding();
        if (binding instanceof IVariableBinding) {
          return (IVariableBinding) binding;
        }
      }
    }
    // check for FieldAccess
    if (node instanceof FieldAccess) {
      FieldAccess fieldAccess = (FieldAccess) node;
      return fieldAccess.resolveFieldBinding();
    }
    // not a variable
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type binding access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param expression
   *          the not <code>null</code> {@link Expression}
   * @return not <code>null</code> {@link ITypeBinding} for given {@link Expression}.
   */
  public static ITypeBinding getTypeBinding(Expression expression) {
    Assert.isNotNull(expression);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      ITypeBinding binding = (ITypeBinding) expression.getProperty(AstParser.KEY_TYPE_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding
    return expression.resolveTypeBinding();
  }

  /**
   * @param declaration
   *          the not <code>null</code> {@link AnonymousClassDeclaration}
   * @return not <code>null</code> {@link ITypeBinding} for given {@link AnonymousClassDeclaration}.
   */
  public static ITypeBinding getTypeBinding(AnonymousClassDeclaration declaration) {
    Assert.isNotNull(declaration);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      ITypeBinding binding = (ITypeBinding) declaration.getProperty(AstParser.KEY_TYPE_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding
    return declaration.resolveBinding();
  }

  /**
   * @param type
   *          the not <code>null</code> {@link Type}
   *
   * @return the {@link ITypeBinding} for given {@link Type}, may be <code>null</code> if unknown.
   */
  public static ITypeBinding getTypeBinding(Type type) {
    Assert.isNotNull(type);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      ITypeBinding binding = (ITypeBinding) type.getProperty(AstParser.KEY_TYPE_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding, may be "null"
    return type.resolveBinding();
  }

  /**
   * @param type
   *          the not <code>null</code> {@link TypeDeclaration}
   *
   * @return not <code>null</code> {@link ITypeBinding} for given {@link TypeDeclaration}.
   */
  public static ITypeBinding getTypeBinding(TypeDeclaration typeDeclaration) {
    return getTypeBinding((AbstractTypeDeclaration) typeDeclaration);
  }

  /**
   * @param type
   *          the not <code>null</code> {@link AbstractTypeDeclaration}
   *
   * @return not <code>null</code> {@link ITypeBinding} for given {@link AbstractTypeDeclaration}.
   */
  public static ITypeBinding getTypeBinding(AbstractTypeDeclaration typeDeclaration) {
    Assert.isNotNull(typeDeclaration);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      ITypeBinding binding = (ITypeBinding) typeDeclaration.getProperty(AstParser.KEY_TYPE_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding, should not return "null"
    {
      ITypeBinding binding = typeDeclaration.resolveBinding();
      Assert.isTrueException(
          binding != null,
          ICoreExceptionConstants.AST_NO_TYPE_BINDING,
          typeDeclaration);
      return binding;
    }
  }

  /**
   * @param parameter
   *          the not <code>null</code> {@link SingleVariableDeclaration}.
   *
   * @return not <code>null</code> {@link ITypeBinding} for given {@link SingleVariableDeclaration}.
   */
  public static ITypeBinding getTypeBinding(SingleVariableDeclaration parameter) {
    Assert.isNotNull(parameter);
    IVariableBinding variableBinding = getVariableBinding(parameter);
    if (variableBinding == null) {
      return null;
    }
    return variableBinding.getType();
  }

  /**
   * @param variable
   *          the not <code>null</code> {@link VariableDeclaration}.
   *
   * @return not <code>null</code> {@link ITypeBinding} for given {@link VariableDeclaration}.
   */
  public static ITypeBinding getTypeBinding(VariableDeclaration variable) {
    Assert.isNotNull(variable);
    // may be FieldDeclaration
    if (variable.getParent() instanceof FieldDeclaration) {
      FieldDeclaration fieldDeclaration = (FieldDeclaration) variable.getParent();
      return getTypeBinding(fieldDeclaration.getType());
    }
    // may be SingleVariableDeclaration
    if (variable instanceof SingleVariableDeclaration) {
      return getTypeBinding((SingleVariableDeclaration) variable);
    }
    // may be VariableDeclarationStatement
    if (variable.getParent() instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement statement = (VariableDeclarationStatement) variable.getParent();
      return getTypeBinding(statement.getType());
    }
    // only known alternative is VariableDeclarationExpression
    VariableDeclarationExpression statement = (VariableDeclarationExpression) variable.getParent();
    return getTypeBinding(statement.getType());
  }

  /**
   * @param typeBinding
   *          the {@link ITypeBinding} for generic type instance.
   * @param typeArgumentIndex
   *          the type argument index.
   * @return the {@link ITypeBinding} for argument (or bound) of generic type instance at specified
   *         index.
   */
  public static ITypeBinding getTypeBindingArgument(ITypeBinding typeBinding, int typeArgumentIndex) {
    ITypeBinding[] typeArgumentBindings = typeBinding.getTypeArguments();
    if (typeArgumentBindings.length != 0) {
      ITypeBinding typeArgumentBinding = typeArgumentBindings[typeArgumentIndex];
      return typeArgumentBinding;
    } else {
      ITypeBinding[] typeParameters = typeBinding.getTypeDeclaration().getTypeParameters();
      return typeParameters[typeArgumentIndex].getTypeBounds()[0];
    }
  }

  /**
   * @param typeBinding
   *          the {@link ITypeBinding} for generic type instance.
   * @param baseClassName
   *          the super class name (parent) whose parameter is to look.
   * @param typeParameterIndex
   *          the parameter index.
   * @return the {@link ITypeBinding} for argument (or bound) of generic type instance at specified
   *         index.
   */
  public static ITypeBinding getTypeBindingArgument(ITypeBinding typeBinding,
      String baseClassName,
      int typeParameterIndex) {
    while (typeBinding != null) {
      String qualifiedName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
      if (baseClassName.equals(qualifiedName)) {
        return getTypeBindingArgument(typeBinding, typeParameterIndex);
      }
      typeBinding = typeBinding.getSuperclass();
    }
    throw new IllegalArgumentException(baseClassName + " is not super class for given binding.");
  }

  /**
   * @param typeBinding
   *          the {@link ITypeBinding} of type variable.
   *
   * @return the declared type bounds, may be <code>null</code> if not specified.
   */
  private static ITypeBinding getTypeVariableBound(ITypeBinding typeBinding) {
    Assert.isLegal(typeBinding.isTypeVariable());
    ITypeBinding[] typeBounds = typeBinding.getTypeBounds();
    if (typeBounds.length != 0) {
      return typeBounds[0];
    } else {
      return null;
    }
  }

  public static ITypeBinding getGenericDeclaringClass(ITypeBinding typeBinding) {
    ITypeBinding declaringClass = typeBinding.getDeclaringClass();
    if (declaringClass != null) {
      return declaringClass.getTypeDeclaration();
    }
    return declaringClass;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Type binding operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The constant to return from {@link #getFullyQualifiedName(ITypeBinding, boolean)} when given
   * {@link ITypeBinding} is <code>null</code>, i.e. no type binding information found, for example
   * because of compilation errors.
   */
  public static String NO_TYPE_BINDING_NAME = "__WBP_NO_TYPE_BINDING";

  /**
   * Same as {@link #getFullyQualifiedName(ITypeBinding, boolean)}, but analyzes also
   * {@link AnonymousClassDeclaration}'s.
   */
  public static String getFullyQualifiedName(Expression expression, boolean runtime) {
    ITypeBinding binding = getTypeBinding(expression);
    String fullyQualifiedName = getFullyQualifiedName(binding, runtime);
    if (binding != null && binding.isAnonymous()) {
      fullyQualifiedName =
          getFullyQualifiedName_appendAnonymous(expression, runtime, fullyQualifiedName);
    }
    return fullyQualifiedName;
  }

  /**
   * For {@link AnonymousClassDeclaration} we can not get full name using only {@link ITypeBinding},
   * so we use AST to find necessary <code>$1</code>, <code>$2</code>, etc suffixes.
   *
   * @return the fully qualified name with anonymous suffix.
   */
  private static String getFullyQualifiedName_appendAnonymous(Expression expression,
      final boolean runtime,
      String fullyQualifiedName) {
    final AnonymousClassDeclaration acd =
        ((ClassInstanceCreation) expression).getAnonymousClassDeclaration();
    final String[] suffix = new String[1];
    expression.getRoot().accept(new ASTVisitor() {
      private final int[] m_counts = new int[32];
      private int m_level = 0;

      @Override
      public boolean visit(AnonymousClassDeclaration node) {
        m_counts[m_level]++;
        if (node == acd) {
          StringBuilder sb = new StringBuilder();
          String separator = runtime ? "$" : ".";
          for (int i = 0; i <= m_level; i++) {
            sb.append(separator);
            sb.append(m_counts[i]);
          }
          suffix[0] = sb.toString();
        }
        m_counts[++m_level] = 0;
        return suffix[0] == null;
      }

      @Override
      public void endVisit(AnonymousClassDeclaration node) {
        m_level--;
      }
    });
    fullyQualifiedName = StringUtils.stripEnd(fullyQualifiedName, "$.");
    fullyQualifiedName += suffix[0];
    return fullyQualifiedName;
  }

  /**
   * @see #getFullyQualifiedName(ITypeBinding, boolean).
   */
  public static String getFullyQualifiedName(Type type, boolean runtime) {
    ITypeBinding binding = getTypeBinding(type);
    return getFullyQualifiedName(binding, runtime);
  }

  /**
   * @see #getFullyQualifiedName(ITypeBinding, boolean).
   */
  public static String getFullyQualifiedName(final TypeDeclaration typeDeclaration,
      final boolean runtime) {
    String key = "getFullyQualifiedName_TypeDeclaration";
    return getValue(typeDeclaration, key, new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        ITypeBinding binding = getTypeBinding(typeDeclaration);
        return getFullyQualifiedName(binding, runtime);
      }
    });
  }

  /**
   * @see #getFullyQualifiedName(ITypeBinding, boolean).
   */
  public static String getFullyQualifiedName(SingleVariableDeclaration parameter, boolean runtime) {
    ITypeBinding binding = getTypeBinding(parameter);
    return getFullyQualifiedName(binding, runtime);
  }

  /**
   * Returns the fully qualified name of given {@link ITypeBinding}, or
   * {@link #NO_TYPE_BINDING_NAME} if <code>null</code> binding given.
   *
   * @param binding
   *          the binding representing the type.
   * @param runtime
   *          flag <code>true</code> if we need name for class loading, <code>false</code> if we
   *          need name for source generation.
   *
   * @return the fully qualified name of given {@link ITypeBinding}, or
   *         {@link #NO_TYPE_BINDING_NAME}.
   */
  public static String getFullyQualifiedName(ITypeBinding binding, boolean runtime) {
    return getFullyQualifiedName(binding, runtime, false);
  }

  /**
   * @param binding
   *          the {@link ITypeBinding} to analyze.
   * @param runtime
   *          flag <code>true</code> if we need name for class loading, <code>false</code> if we
   *          need name for source generation.
   * @param withGenerics
   *          flag <code>true</code> if generics type arguments should be appended.
   *
   * @return the fully qualified name of given {@link ITypeBinding}, or
   *         {@link #NO_TYPE_BINDING_NAME}.
   */
  public static String getFullyQualifiedName(ITypeBinding binding,
      boolean runtime,
      boolean withGenerics) {
    // check if no binding
    if (binding == null) {
      return NO_TYPE_BINDING_NAME;
    }
    // check for primitive type
    if (binding.isPrimitive()) {
      return binding.getName();
    }
    // array
    if (binding.isArray()) {
      StringBuilder sb = new StringBuilder();
      // append element type qualified name
      ITypeBinding elementType = binding.getElementType();
      String elementTypeQualifiedName = getFullyQualifiedName(elementType, runtime);
      sb.append(elementTypeQualifiedName);
      // append dimensions
      for (int i = 0; i < binding.getDimensions(); i++) {
        sb.append("[]");
      }
      // done
      return sb.toString();
    }
    // object
    {
      String scope;
      ITypeBinding declaringType = binding.getDeclaringClass();
      if (declaringType == null) {
        IPackageBinding packageBinding = binding.getPackage();
        if (packageBinding == null || packageBinding.isUnnamed()) {
          scope = "";
        } else {
          scope = packageBinding.getName() + ".";
        }
      } else if (binding.isTypeVariable()) {
        return binding.getName();
      } else {
        // use '$', because we use this class name for loading class
        scope = getFullyQualifiedName(declaringType, runtime);
        if (runtime) {
          scope += "$";
        } else {
          scope += ".";
        }
      }
      // prepare "simple" name, without scope
      String jdtName = binding.getName();
      String name = StringUtils.substringBefore(jdtName, "<");
      if (withGenerics) {
        ITypeBinding[] typeArguments = binding.getTypeArguments();
        if (typeArguments.length != 0) {
          StringBuilder sb = new StringBuilder(name);
          sb.append("<");
          for (ITypeBinding typeArgument : typeArguments) {
            if (sb.charAt(sb.length() - 1) != '<') {
              sb.append(",");
            }
            String typeArgumentName = getFullyQualifiedName(typeArgument, runtime, withGenerics);
            sb.append(typeArgumentName);
          }
          sb.append(">");
          name = sb.toString();
        }
      }
      // qualified name is scope plus "simple" name
      return scope + name;
    }
  }

  /**
   * Checks if given {@link Expression} type is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(Expression expression, Class<?> clazz) {
    return isSuccessorOf(getTypeBinding(expression), clazz);
  }

  /**
   * Checks if given {@link Expression} type is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(Expression expression, ITypeBinding requiredBinding) {
    return isSuccessorOf(getTypeBinding(expression), requiredBinding);
  }

  /**
   * Checks if given {@link Expression} type is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(Expression expression, String className) {
    return isSuccessorOf(getTypeBinding(expression), className);
  }

  /**
   * Checks if given {@link SingleVariableDeclaration} type is extends/implements given
   * class/interface.
   */
  public static boolean isSuccessorOf(SingleVariableDeclaration parameter, String className) {
    return isSuccessorOf(getTypeBinding(parameter), className);
  }

  /**
   * Checks if given {@link Type} type is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(Type type, String className) {
    return isSuccessorOf(getTypeBinding(type), className);
  }

  /**
   * Checks if given {@link TypeDeclaration} is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(TypeDeclaration type, String className) {
    return isSuccessorOf(getTypeBinding(type), className);
  }

  /**
   * Checks if given {@link AnonymousClassDeclaration} is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(AnonymousClassDeclaration declaration, String className) {
    return isSuccessorOf(getTypeBinding(declaration), className);
  }

  /**
   * Checks if given {@link ITypeBinding} is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(ITypeBinding binding, Class<?> clazz) {
    Assert.isNotNull(clazz);
    return isSuccessorOf(binding, clazz.getName());
  }

  /**
   * Checks if given {@link ITypeBinding} is extends/implements given class/interface.
   */
  public static boolean isSuccessorOf(ITypeBinding binding, ITypeBinding requiredBinding) {
    Assert.isNotNull(requiredBinding);
    return isSuccessorOf(binding, getFullyQualifiedName(requiredBinding, false));
  }

  private static final Map<ITypeBinding, Map<String, Boolean>> m_isSuccessorOf =
      new WeakHashMap<ITypeBinding, Map<String, Boolean>>();

  /**
   * Checks if given {@link ITypeBinding} is extends class or implements interface with given name.
   *
   * @param className
   *          the class name in source format (i.e. with '.' as packages separator)
   */
  public static boolean isSuccessorOf(ITypeBinding binding, String className) {
    Assert.isNotNull(className);
    // check for Object
    if (binding == null) {
      return "java.lang.Object".equals(className);
    }
    // use cache
    Map<String, Boolean> classNameResults = m_isSuccessorOf.get(binding);
    if (classNameResults == null) {
      classNameResults = Maps.newTreeMap();
      m_isSuccessorOf.put(binding, classNameResults);
    }
    Boolean result = classNameResults.get(className);
    if (result == null) {
      result = isSuccessorOf0(binding, className);
      classNameResults.put(className, result);
    }
    return result;
  }

  /**
   * Implementation for {@link #isSuccessorOf(Expression, String)}.
   */
  private static boolean isSuccessorOf0(ITypeBinding binding, String className) {
    // check this binding
    if (getFullyQualifiedName(binding, false).equals(className)) {
      return true;
    }
    // check for array and Object
    if (binding.isArray() && "java.lang.Object".equals(className)) {
      return true;
    }
    // check superclass
    {
      boolean result = isSuccessorOf(binding.getSuperclass(), className);
      if (result) {
        return true;
      }
    }
    // check interfaces
    for (ITypeBinding interfaceBinding : binding.getInterfaces()) {
      if (isSuccessorOf(interfaceBinding, className)) {
        return true;
      }
    }
    // no
    return false;
  }

  /**
   * Checks if given {@link ITypeBinding} is extends one of the given classes or implements
   * interface with given name.
   *
   * @param classNames
   *          the array of class names in source format (i.e. with '.' as packages separator).
   */
  public static boolean isSuccessorOf(ITypeBinding binding, String... classNames) {
    for (String className : classNames) {
      if (isSuccessorOf(binding, className)) {
        return true;
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TypeDeclaration utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param typeName
   *          the simple type name.
   *
   * @return the top-level {@link TypeDeclaration} with given name from given
   *         {@link CompilationUnit}.
   */
  public static TypeDeclaration getTypeByName(CompilationUnit compilationUnit, String typeName) {
    Assert.isNotNull(compilationUnit);
    Assert.isNotNull(typeName);
    // check each top-level type
    for (TypeDeclaration typeDeclaration : DomGenerics.types(compilationUnit)) {
      ITypeBinding typeBinding = typeDeclaration.resolveBinding();
      if (typeBinding != null && typeBinding.getName().equals(typeName)) {
        return typeDeclaration;
      }
    }
    // not found
    return null;
  }

  /**
   * @param typeName
   *          the fully qualified type name.
   *
   * @return the {@link TypeDeclaration} with given fully qualified name, or <code>null</code> if
   *         not found.
   */
  public static TypeDeclaration getTypeByQualifiedName(CompilationUnit compilationUnit,
      final String typeName) {
    Assert.isNotNull(compilationUnit);
    Assert.isNotNull(typeName);
    final TypeDeclaration[] typeDeclaration = new TypeDeclaration[1];
    compilationUnit.accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        if (typeDeclaration[0] == null) {
          if (node.getLocationInParent() == ClassInstanceCreation.TYPE_PROPERTY
              && ((ClassInstanceCreation) node.getParent()).getAnonymousClassDeclaration() != null) {
            ClassInstanceCreation creation = (ClassInstanceCreation) node.getParent();
            String _typeName = getFullyQualifiedName(creation, true);
            if (_typeName.equals(typeName)) {
              AnonymousClassDeclaration anonymousClassDeclaration =
                  creation.getAnonymousClassDeclaration();
              typeDeclaration[0] = AnonymousTypeDeclaration.create(anonymousClassDeclaration);
            }
          }
        }
      }

      @Override
      public boolean visit(SimpleName node) {
        if (typeDeclaration[0] == null) {
          if (node.getLocationInParent() == TypeDeclaration.NAME_PROPERTY) {
            String _typeName = getFullyQualifiedName(node, true);
            if (_typeName.equals(typeName)) {
              typeDeclaration[0] = (TypeDeclaration) node.getParent();
            }
          }
        }
        return typeDeclaration[0] == null;
      }
    });
    // OK, return found TypeDeclaration
    return typeDeclaration[0];
  }

  /**
   * @return the inner {@link TypeDeclaration} for given {@link ClassInstanceCreation} in same
   *         {@link CompilationUnit}, or <code>null</code> if there is no such
   *         {@link TypeDeclaration}.
   */
  public static TypeDeclaration getTypeDeclaration(ClassInstanceCreation creation) {
    final String typeName = getFullyQualifiedName(getTypeBinding(creation), false);
    ListGatherer<TypeDeclaration> gatherer = new ListGatherer<TypeDeclaration>() {
      @Override
      public boolean visit(TypeDeclaration node) {
        if (getFullyQualifiedName(node, false).equals(typeName)) {
          addResult(node);
          return false;
        }
        return true;
      }
    };
    // visit all types in compilation unit
    CompilationUnit compilationUnit = (CompilationUnit) creation.getRoot();
    compilationUnit.accept(gatherer);
    return gatherer.getUniqueResult();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodDeclaration utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The constant to return from {@link #getMethodSignature(IMethodBinding)} when given
   * {@link IMethodBinding} is <code>null</code>, i.e. no method binding information found, for
   * example because of compilation errors.
   */
  public static String NO_METHOD_BINDING_SIGNATURE = "__WBP_NO_METHOD_BINDING";

  /**
   * @return signature for given {@link MethodDeclaration}. This signature is not same signature as
   *         in JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static String getMethodSignature(MethodInvocation methodInvocation) {
    Assert.isNotNull(methodInvocation);
    IMethodBinding methodBinding = getMethodBinding(methodInvocation);
    return getMethodSignature(methodBinding);
  }

  /**
   * @return signature for given {@link MethodDeclaration}. This signature is not same signature as
   *         in JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static String getMethodSignature(MethodDeclaration methodDeclaration) {
    Assert.isNotNull(methodDeclaration);
    IMethodBinding methodBinding = getMethodBinding(methodDeclaration);
    return getMethodSignature(methodBinding);
  }

  /**
   * @return signature for given {@link IMethodBinding}. This signature is not same signature as in
   *         JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static String getMethodSignature(IMethodBinding methodBinding) {
    return getMethodSignature(methodBinding, false);
  }

  /**
   * @return signature for given {@link IMethodBinding} with generic type names.
   */
  public static String getMethodGenericSignature(IMethodBinding methodBinding) {
    return getMethodSignature(methodBinding.getMethodDeclaration(), false);
  }

  /**
   * @return signature for given {@link IMethodBinding} with base-declaration types.
   */
  public static String getMethodDeclarationSignature(IMethodBinding methodBinding) {
    return getMethodSignature(methodBinding.getMethodDeclaration(), true);
  }

  /**
   *
   * @param methodBinding
   *          the method binding.
   * @param declaration
   *          set <code>true</code> if need type variables replaced with a base types.
   *
   * @return signature for given {@link IMethodBinding}.
   */
  private static String getMethodSignature(IMethodBinding methodBinding, boolean declaration) {
    // check if no binding
    if (methodBinding == null) {
      return NO_METHOD_BINDING_SIGNATURE;
    }
    // signature
    StringBuilder buffer = new StringBuilder();
    // name
    if (methodBinding.isConstructor()) {
      buffer.append("<init>");
    } else {
      buffer.append(methodBinding.getName());
    }
    // parameters
    buffer.append('(');
    {
      ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
      for (int i = 0; i < parameterTypes.length; i++) {
        ITypeBinding parameterType = parameterTypes[i];
        if (i != 0) {
          buffer.append(',');
        }
        if (declaration && parameterType.isTypeVariable()) {
          ITypeBinding variableBinding = getTypeVariableBound(parameterType);
          if (variableBinding == null) {
            buffer.append("java.lang.Object");
          } else {
            buffer.append(getFullyQualifiedName(variableBinding, false));
          }
        } else {
          buffer.append(getFullyQualifiedName(parameterType, false));
        }
      }
    }
    buffer.append(')');
    // return result
    return buffer.toString();
  }

  /**
   * @return signatures for given {@link MethodDeclaration}'s. These signature is not same signature
   *         as in JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static List<String> getMethodSignatures(List<MethodDeclaration> methodDeclarations) {
    List<String> signatures = Lists.newArrayList();
    for (MethodDeclaration methodDeclaration : methodDeclarations) {
      signatures.add(getMethodSignature(methodDeclaration));
    }
    return signatures;
  }

  /**
   * @param signature
   *          the signature of method in same format as
   *          {@link #getMethodSignature(MethodDeclaration)}.
   *
   * @return the {@link MethodDeclaration} for given signature or <code>null</code> if not method
   *         found.
   */
  public static MethodDeclaration getMethodBySignature(TypeDeclaration typeDeclaration,
      String signature) {
    Assert.isNotNull(typeDeclaration);
    return getMethodBySignature(DomGenerics.bodyDeclarations(typeDeclaration), signature);
  }

  /**
   * @param bodyDeclarations
   *          the {@link List} of {@link BodyDeclaration} to check
   * @param signature
   *          the signature of method in same format as
   *          {@link #getMethodSignature(MethodDeclaration)}.
   *
   * @return the {@link MethodDeclaration} for given signature or <code>null</code> if no method
   *         found.
   */
  public static MethodDeclaration getMethodBySignature(List<BodyDeclaration> bodyDeclarations,
      String signature) {
    Assert.isNotNull(bodyDeclarations);
    Assert.isNotNull(signature);
    // check each declaration
    for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
      if (bodyDeclaration instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
        if (getMethodSignature(methodDeclaration).equals(signature)) {
          return methodDeclaration;
        }
      }
    }
    // not found
    return null;
  }

  /**
   * Returns that {@link IMethodBinding} of method declared in given {@link ITypeBinding} or any of
   * its super-classes.
   *
   * @param typeBinding
   *          the {@link ITypeBinding} to search methods in.
   * @param signature
   *          the signature of method in same format as
   *          {@link #getMethodSignature(MethodDeclaration)}.
   *
   * @return the {@link IMethodBinding} with given signature, or <code>null</code> if no method
   *         found.
   */
  public static IMethodBinding getMethodBySignature(ITypeBinding typeBinding, String signature) {
    for (; typeBinding != null; typeBinding = typeBinding.getSuperclass()) {
      for (IMethodBinding method : typeBinding.getDeclaredMethods()) {
        if (getMethodSignature(method).equals(signature)) {
          return method;
        }
      }
    }
    // not found
    return null;
  }

  /**
   * @return the local {@link MethodDeclaration} (i.e. declared in same {@link TypeDeclaration})
   *         that is invoked by given {@link MethodInvocation} or <code>null</code> if this method
   *         is not local.
   */
  public static MethodDeclaration getLocalMethodDeclaration(final MethodInvocation invocation) {
    // quick check if there is local name with such name (just name, not signature)
    {
      ASTNode unit = invocation.getRoot();
      String key = "getLocalMethodDeclaration.allMethods";
      Set<String> allNames = getValue(unit, key, new RunnableObjectEx<Set<String>>() {
        public Set<String> runObject() throws Exception {
          Set<String> names = Sets.newTreeSet();
          TypeDeclaration typeDeclaration = getEnclosingType(invocation);
          MethodDeclaration[] methods = typeDeclaration.getMethods();
          for (MethodDeclaration method : methods) {
            names.add(method.getName().getIdentifier());
          }
          return names;
        }
      });
      if (!allNames.contains(invocation.getName().getIdentifier())) {
        return null;
      }
    }
    // perform precise search
    String key = "getLocalMethodDeclaration";
    return getValue(invocation, key, new RunnableObjectEx<MethodDeclaration>() {
      public MethodDeclaration runObject() throws Exception {
        return getLocalMethodDeclaration0(invocation);
      }
    });
  }

  /**
   * Implementation for {@link #getLocalMethodDeclaration(MethodInvocation)}.
   */
  private static MethodDeclaration getLocalMethodDeclaration0(MethodInvocation invocation) {
    Assert.isNotNull(invocation);
    TypeDeclaration typeDeclaration = getEnclosingType(invocation);
    // check for local method
    while (true) {
      Expression targetExpression = invocation.getExpression();
      // check for unqualified or "this." invocation
      if (targetExpression == null || targetExpression instanceof ThisExpression) {
        break;
      }
      // check for qualified invocation
      {
        String enclosedTypeName = getFullyQualifiedName(typeDeclaration, false);
        String targetTypeName = getFullyQualifiedName(targetExpression, false);
        if (enclosedTypeName.equals(targetTypeName)) {
          break;
        }
      }
      // no, not a local method
      return null;
    }
    // OK, local method
    {
      // prepare signature, can be "null", if invalid invocation
      IMethodBinding methodBinding = getMethodBinding(invocation);
      if (methodBinding == null) {
        return null;
      }
      // get method by signature
      String methodSignature = getMethodSignature(methodBinding);
      return getMethodBySignature(typeDeclaration, methodSignature);
    }
  }

  /**
   * @return {@link List} of {@link MethodInvocation}'s of given method.
   */
  public static List<MethodInvocation> getMethodInvocations(final MethodDeclaration methodDeclaration) {
    String key = "getMethodInvocations";
    return getValue(methodDeclaration, key, new RunnableObjectEx<List<MethodInvocation>>() {
      public List<MethodInvocation> runObject() throws Exception {
        return getMethodInvocations0(methodDeclaration);
      }
    });
  }

  /**
   * Implementation for {@link #getMethodInvocations(MethodDeclaration)}.
   */
  private static List<MethodInvocation> getMethodInvocations0(MethodDeclaration methodDeclaration) {
    final List<MethodInvocation> invocations = Lists.newArrayList();
    // prepare required values
    IMethodBinding requiredBinding = getMethodBinding(methodDeclaration);
    if (requiredBinding == null) {
      return invocations;
    }
    final String requiredType = getFullyQualifiedName(requiredBinding.getDeclaringClass(), false);
    final String requiredSignature = getMethodSignature(methodDeclaration);
    // visit type and check each method invocation
    TypeDeclaration typeDeclaration = getEnclosingType(methodDeclaration);
    typeDeclaration.accept(new ASTVisitor() {
      @Override
      public void endVisit(MethodInvocation node) {
        IMethodBinding binding = getMethodBinding(node);
        if (binding != null) {
          String signature = getMethodSignature(binding);
          String type = getFullyQualifiedName(binding.getDeclaringClass(), false);
          if (signature.equals(requiredSignature) && type.equals(requiredType)) {
            invocations.add(node);
          }
        }
      }
    });
    //
    return invocations;
  }

  /**
   * @param signature
   *          the signature of method in same format as
   *          {@link #getMethodSignature(MethodDeclaration)}.
   *
   * @return the {@link MethodDeclaration} for given signature or <code>null</code> if not method
   *         found.
   */
  public static MethodDeclaration getMethodByName(TypeDeclaration typeDeclaration, String name) {
    Assert.isNotNull(typeDeclaration);
    Assert.isNotNull(name);
    List<BodyDeclaration> bodyDeclarations = DomGenerics.bodyDeclarations(typeDeclaration);
    for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
      if (bodyDeclaration instanceof MethodDeclaration) {
        MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
        if (methodDeclaration.getName().getIdentifier().equals(name)) {
          return methodDeclaration;
        }
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ConstructorInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return signature for given {@link ConstructorInvocation}. This signature is not same signature
   *         as in JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static String getSignature(ConstructorInvocation invocation) {
    Assert.isNotNull(invocation);
    IMethodBinding methodBinding = getBinding(invocation);
    return getMethodSignature(methodBinding);
  }

  /**
   * @param invocation
   *          the not <code>null</code> {@link ConstructorInvocation}
   * @return not <code>null</code> {@link IMethodBinding} for given {@link ConstructorInvocation}.
   */
  public static IMethodBinding getBinding(ConstructorInvocation invocation) {
    Assert.isNotNull(invocation);
    return invocation.resolveConstructorBinding();
  }

  /**
   * @return the {@link MethodDeclaration} that is invoked by given {@link ConstructorInvocation}.
   */
  public static MethodDeclaration getConstructor(ConstructorInvocation invocation) {
    TypeDeclaration typeDeclaration = getEnclosingType(invocation);
    String signature = getSignature(invocation);
    return getMethodBySignature(typeDeclaration, signature);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SuperConstructorInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return signature for given {@link SuperConstructorInvocation}. This signature is not same
   *         signature as in JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static String getSuperSignature(SuperConstructorInvocation invocation) {
    Assert.isNotNull(invocation);
    IMethodBinding methodBinding = getSuperBinding(invocation);
    return getMethodSignature(methodBinding);
  }

  /**
   * @param invocation
   *          the not <code>null</code> {@link SuperConstructorInvocation}
   * @return not <code>null</code> {@link IMethodBinding} for given
   *         {@link SuperConstructorInvocation}.
   */
  public static IMethodBinding getSuperBinding(SuperConstructorInvocation invocation) {
    Assert.isNotNull(invocation);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      IMethodBinding binding =
          (IMethodBinding) invocation.getProperty(AstParser.KEY_METHOD_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding, should not return "null"
    return invocation.resolveConstructorBinding();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ClassInstanceCreation utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return signature for given {@link ClassInstanceCreation}. This signature is not same signature
   *         as in JVM or JDT, just some string that unique identifies method in its
   *         {@link TypeDeclaration}.
   */
  public static String getCreationSignature(ClassInstanceCreation creation) {
    Assert.isNotNull(creation);
    IMethodBinding methodBinding = getCreationBinding(creation);
    return getMethodSignature(methodBinding);
  }

  /**
   * @param creation
   *          the not <code>null</code> {@link ClassInstanceCreation}
   * @return not <code>null</code> {@link IMethodBinding} for given {@link ClassInstanceCreation}.
   */
  public static IMethodBinding getCreationBinding(ClassInstanceCreation creation) {
    Assert.isNotNull(creation);
    // try to get binding from property (copy of binding added by DesignerAST)
    {
      IMethodBinding binding = (IMethodBinding) creation.getProperty(AstParser.KEY_METHOD_BINDING);
      if (binding != null) {
        return binding;
      }
    }
    // get standard binding, should not return "null"
    return creation.resolveConstructorBinding();
  }

  /**
   * @return the local constructor {@link MethodDeclaration} (i.e. declared in same
   *         {@link TypeDeclaration}) that is invoked by given {@link ClassInstanceCreation} or
   *         <code>null</code> if this method is not local.
   */
  public static MethodDeclaration getLocalConstructorDeclaration(final ClassInstanceCreation creation) {
    String key = "getLocalConstructorDeclaration";
    return getValue(creation, key, new RunnableObjectEx<MethodDeclaration>() {
      public MethodDeclaration runObject() throws Exception {
        return getLocalConstructorDeclaration0(creation);
      }
    });
  }

  /**
   * Implementation for {@link #getLocalConstructorDeclaration(ClassInstanceCreation)}.
   */
  private static MethodDeclaration getLocalConstructorDeclaration0(ClassInstanceCreation creation) {
    Assert.isNotNull(creation);
    TypeDeclaration typeDeclaration = getEnclosingType(creation);
    // check for local
    {
      String enclosedTypeName = getFullyQualifiedName(typeDeclaration, false);
      String createdTypeName = getFullyQualifiedName(creation, false);
      if (!enclosedTypeName.equals(createdTypeName)) {
        return null;
      }
    }
    //
    IMethodBinding constructorBinding = getCreationBinding(creation);
    String constructorSignature = getMethodSignature(constructorBinding);
    return getMethodBySignature(typeDeclaration, constructorSignature);
  }

  /**
   * @return {@link List} of {@link MethodInvocation}'s of given method.
   */
  public static List<ConstructorInvocation> getConstructorInvocations(final MethodDeclaration methodDeclaration) {
    String key = "getConstructorInvocations";
    return getValue(methodDeclaration, key, new RunnableObjectEx<List<ConstructorInvocation>>() {
      public List<ConstructorInvocation> runObject() throws Exception {
        return getConstructorInvocations0(methodDeclaration);
      }
    });
  }

  /**
   * Implementation for {@link #getConstructorInvocation2(MethodDeclaration)}.
   */
  private static List<ConstructorInvocation> getConstructorInvocations0(MethodDeclaration methodDeclaration) {
    final List<ConstructorInvocation> invocations = Lists.newArrayList();
    // prepare required values
    IMethodBinding requiredBinding = getMethodBinding(methodDeclaration);
    if (requiredBinding == null) {
      return invocations;
    }
    final String requiredSignature = getMethodSignature(methodDeclaration);
    // visit type and check each method invocation
    TypeDeclaration typeDeclaration = getEnclosingType(methodDeclaration);
    typeDeclaration.accept(new ASTVisitor() {
      @Override
      public void endVisit(ConstructorInvocation node) {
        IMethodBinding binding = getBinding(node);
        if (binding != null) {
          String signature = getMethodSignature(binding);
          if (signature.equals(requiredSignature)) {
            invocations.add(node);
          }
        }
      }
    });
    //
    return invocations;
  }

  /**
   * @return {@link List} of {@link ClassInstanceCreation}'s of given constructor in this
   *         {@link CompilationUnit}.
   */
  public static List<ClassInstanceCreation> getClassInstanceCreations(MethodDeclaration constructor) {
    final List<ClassInstanceCreation> creations = Lists.newArrayList();
    final IMethodBinding constructorBinding = getMethodBinding(constructor);
    constructor.getRoot().accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation node) {
        IMethodBinding creationBinding = getCreationBinding(node);
        if (creationBinding == constructorBinding) {
          creations.add(node);
        }
      }
    });
    return creations;
  }

  /**
   * @return constructor {@link MethodDeclaration}'s of given {@link TypeDeclaration}.
   */
  public static List<MethodDeclaration> getConstructors(TypeDeclaration typeDeclaration) {
    List<MethodDeclaration> constructors = Lists.newArrayList();
    for (MethodDeclaration method : typeDeclaration.getMethods()) {
      if (method.isConstructor()) {
        constructors.add(method);
      }
    }
    return constructors;
  }

  /**
   * @return {@link List} of {@link ClassInstanceCreation}'s of given {@link TypeDeclaration} in
   *         this {@link CompilationUnit}.
   */
  public static List<ClassInstanceCreation> getClassInstanceCreations(TypeDeclaration type) {
    final List<ClassInstanceCreation> creations = Lists.newArrayList();
    final String typeName = getFullyQualifiedName(type, false);
    type.getRoot().accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation node) {
        String nodeName = getFullyQualifiedName(node, false);
        if (typeName.equals(nodeName)) {
          creations.add(node);
        }
      }
    });
    return creations;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaDoc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link Javadoc} of given {@link BodyDeclaration} has
   *         {@link TagElement} with given name.
   */
  public static boolean hasJavaDocTag(BodyDeclaration declaration, String tagName) {
    return getJavaDocTag(declaration, tagName) != null;
  }

  /**
   * @return the JavaDoc {@link TagElement} with given name, may be <code>null</code>.
   */
  public static TagElement getJavaDocTag(BodyDeclaration declaration, String tagName) {
    Javadoc javadoc = declaration.getJavadoc();
    if (javadoc != null) {
      for (TagElement tagElement : DomGenerics.tags(javadoc)) {
        if (tagName.equals(tagElement.getTagName())) {
          return tagElement;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Matching
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param node
   *          the {@link ASTNode} to check.
   * @param signature
   *          the signature of {@link MethodInvocation}.
   *
   * @return <code>true</code> if given {@link ASTNode} is {@link MethodInvocation} with given
   *         signature. Note, no check for type, so use only when really required.
   */
  public static boolean isMethodInvocation(ASTNode node, String signature) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      return getMethodSignature(invocation).equals(signature);
    }
    return false;
  }

  /**
   * @param node
   *          the {@link ASTNode} to check.
   * @param expressionType
   *          the type of {@link MethodInvocation} {@link Expression}.
   * @param signature
   *          the signature of {@link MethodInvocation}.
   *
   * @return <code>true</code> if given {@link ASTNode} is {@link MethodInvocation} of given
   *         {@link Expression} type and signature.
   */
  public static boolean isMethodInvocation(ASTNode node, String expressionType, String signature) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      if (getMethodSignature(invocation).equals(signature)) {
        Expression expression = invocation.getExpression();
        if (expression == null) {
          return expressionType == null;
        }
        return isSuccessorOf(expression, expressionType);
      }
    }
    if (node instanceof SuperMethodInvocation) {
      SuperMethodInvocation invocation = (SuperMethodInvocation) node;
      IMethodBinding methodBinding = getMethodBinding(invocation);
      return isSuccessorOf(methodBinding.getDeclaringClass(), expressionType)
          && getMethodSignature(methodBinding).equals(signature);
    }
    return false;
  }

  /**
   * @param node
   *          the {@link ASTNode} to check.
   * @param expressionType
   *          the type of {@link MethodInvocation} {@link Expression}.
   * @param signatures
   *          the array with signatures of {@link MethodInvocation}.
   *
   * @return <code>true</code> if given {@link ASTNode} is {@link MethodInvocation} of given
   *         {@link Expression} type and and one of the signatures.
   */
  public static boolean isMethodInvocation(ASTNode node, String expressionType, String[] signatures) {
    if (node instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) node;
      // check Expression
      {
        Expression expression = invocation.getExpression();
        if (expression == null) {
          if (expressionType != null) {
            return false;
          }
        } else if (!isSuccessorOf(expression, expressionType)) {
          return false;
        }
      }
      // check signature
      {
        String signature = getMethodSignature(invocation);
        for (String testSignature : signatures) {
          if (signature.equals(testSignature)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * @param node
   *          the {@link ASTNode} to check.
   * @param typeName
   *          the name of type instantiated by {@link ClassInstanceCreation}.
   * @param signature
   *          the signature of constructor, including "&lt;init&gt;".
   *
   * @return <code>true</code> if given {@link ASTNode} is the {@link ClassInstanceCreation} of
   *         given type using given constructor.
   */
  public static boolean isCreation(ASTNode node, String typeName, String signature) {
    if (node instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) node;
      if (getFullyQualifiedName(creation, false).equals(typeName)) {
        return getCreationSignature(creation).equals(signature);
      }
    }
    return false;
  }

  /**
   * @param node
   *          the {@link ASTNode} to check.
   * @param typeName
   *          the name of type instantiated by {@link ClassInstanceCreation}.
   * @param signature
   *          the array of constructor signatures, including "&lt;init&gt;".
   *
   * @return <code>true</code> if given {@link ASTNode} is the {@link ClassInstanceCreation} of
   *         given type using one of the given constructor.
   */
  public static boolean isCreation(ASTNode node, String typeName, String[] signatures) {
    if (node instanceof ClassInstanceCreation) {
      for (String signature : signatures) {
        if (isCreation(node, typeName, signature)) {
          return true;
        }
      }
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modifiers
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int VISIBILITY_MASK = Modifier.PUBLIC
      | Modifier.PROTECTED
      | Modifier.PRIVATE;

  /**
   * Returns <code>true</code> if given modifiers have one of the required visibility mask. For
   * example, if <code>modifiers</code> is <code>protected static final</code>, and and
   * <code>visibilityMask</code> is <code>public | protected</code>, then <code>true</code>
   * returned.
   *
   * @return <code>true</code> if given modifiers have one of the required visibility mask.
   */
  public static boolean hasVisibility(int modifiers, int visibilityMask) {
    return (modifiers & VISIBILITY_MASK & visibilityMask) != 0;
  }

  /**
   * @return <code>true</code> if given {@link BodyDeclaration} has "static" modifier.
   */
  public static boolean isStatic(BodyDeclaration bodyDeclaration) {
    int modifiers = bodyDeclaration.getModifiers();
    return Modifier.isStatic(modifiers);
  }

  /**
   * @return <code>true</code> if given {@link IMethodBinding} has "static" modifier.
   */
  public static boolean isStatic(IMethodBinding methodBinding) {
    int modifiers = methodBinding.getModifiers();
    return Modifier.isStatic(modifiers);
  }

  /**
   * @return <code>true</code> if given {@link ITypeBinding} has "static" modifier.
   */
  public static boolean isStatic(ITypeBinding typeBinding) {
    int modifiers = typeBinding.getModifiers();
    return Modifier.isStatic(modifiers);
  }

  /**
   * @return <code>true</code> if given {@link IMethodBinding} has "abstract" modifier.
   */
  public static boolean isAbstract(IMethodBinding methodBinding) {
    int modifiers = methodBinding.getModifiers();
    return Modifier.isAbstract(modifiers);
  }

  /**
   * @return <code>true</code> if given {@link MethodDeclaration} has "abstract" modifier.
   */
  public static boolean isAbstract(MethodDeclaration method) {
    IMethodBinding methodBinding = getMethodBinding(method);
    return isAbstract(methodBinding);
  }

  /**
   * @return <code>true</code> if given {@link ITypeBinding} has "abstract" modifier.
   */
  public static boolean isAbstract(ITypeBinding typeBinding) {
    int modifiers = typeBinding.getModifiers();
    return Modifier.isAbstract(modifiers);
  }

  /**
   * @return <code>true</code> if given {@link TypeDeclaration} has "abstract" modifier.
   */
  public static boolean isAbstract(TypeDeclaration type) {
    ITypeBinding typeBinding = getTypeBinding(type);
    return isAbstract(typeBinding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Searching in bindings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param typeBinding
   *          the {@link ITypeBinding} to get methods from.
   * @param visibilityMask
   *          the mask of JDT {@link Modifier} flags, to limit methods visibility. If
   *          {@link Modifier#PRIVATE} is included, it will be used only for given
   *          {@link ITypeBinding}, but not for its super classes.
   *
   * @return the {@link IMethodBinding} for all methods in given {@link ITypeBinding} and its super
   *         classes.
   */
  public static List<IMethodBinding> getMethodBindings(ITypeBinding typeBinding, int visibilityMask) {
    List<IMethodBinding> methods = Lists.newArrayList();
    addMethodBindings(methods, typeBinding, visibilityMask);
    return methods;
  }

  /**
   * @param typeBinding
   *          the {@link ITypeBinding} to get fields from.
   * @param visibilityMask
   *          the mask of JDT {@link Modifier} flags, to limit fields visibility. If
   *          {@link Modifier#PRIVATE} is included, it will be used only for given
   *          {@link ITypeBinding}, but not for its super classes.
   *
   * @return the {@link IVariableBinding} for all fields in given {@link ITypeBinding} and its super
   *         classes.
   */
  public static List<IVariableBinding> getFieldBindings(ITypeBinding typeBinding, int visibilityMask) {
    List<IVariableBinding> fields = Lists.newArrayList();
    addFieldBindings(fields, typeBinding, visibilityMask);
    return fields;
  }

  /**
   * @param methods
   *          the {@link IMethodBinding}'s container to add methods.
   * @param typeBinding
   *          the {@link ITypeBinding} to get methods from.
   * @param visibilityMask
   *          the mask of JDT {@link Modifier} flags, to limit methods visibility. If
   *          {@link Modifier#PRIVATE} is included, it will be used only for given
   *          {@link ITypeBinding}, but not for its super classes.
   *
   * @return the {@link IMethodBinding} for all methods in given {@link ITypeBinding} and its super
   *         classes.
   */
  private static void addMethodBindings(List<IMethodBinding> methods,
      ITypeBinding typeBinding,
      int visibilityMask) {
    if (typeBinding != null) {
      for (IMethodBinding methodBinding : typeBinding.getDeclaredMethods()) {
        if (hasVisibility(methodBinding.getModifiers(), visibilityMask)) {
          methods.add(methodBinding);
        }
      }
      // process super Class
      visibilityMask &= ~Modifier.PRIVATE;
      addMethodBindings(methods, typeBinding.getSuperclass(), visibilityMask);
    }
  }

  /**
   * @param fields
   *          the {@link IVariableBinding}'s container to add fields.
   * @param typeBinding
   *          the {@link ITypeBinding} to get fields from.
   * @param visibilityMask
   *          the mask of JDT {@link Modifier} flags, to limit fields visibility. If
   *          {@link Modifier#PRIVATE} is included, it will be used only for given
   *          {@link ITypeBinding}, but not for its super classes.
   *
   * @return the {@link IVariableBinding} for all fields in given {@link ITypeBinding} and its super
   *         classes.
   */
  private static void addFieldBindings(List<IVariableBinding> fields,
      ITypeBinding typeBinding,
      int visibilityMask) {
    if (typeBinding != null) {
      for (IVariableBinding fieldBinding : typeBinding.getDeclaredFields()) {
        if (hasVisibility(fieldBinding.getModifiers(), visibilityMask)) {
          fields.add(fieldBinding);
        }
      }
      // process super Class
      visibilityMask &= ~Modifier.PRIVATE;
      addFieldBindings(fields, typeBinding.getSuperclass(), visibilityMask);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param node
   *          the {@link ASTNode} that represents should be qualifier of field.
   *
   * @return the {@link Expression} ({@link QualifiedName} or {@link FieldAccess}) used as left side
   *         of {@link Assignment} to some field. May return <code>null</code>, if given node is not
   *         left part of {@link Assignment}.
   */
  public static Expression getFieldAssignment(ASTNode node) {
    // FieldAccess = value
    if (node.getLocationInParent() == FieldAccess.EXPRESSION_PROPERTY) {
      FieldAccess fieldAccess = (FieldAccess) node.getParent();
      if (fieldAccess.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
        return fieldAccess;
      }
    }
    // QualifiedName = value
    if (node.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
      QualifiedName qualifiedName = (QualifiedName) node.getParent();
      if (qualifiedName.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
        return qualifiedName;
      }
    }
    // not an Assignment part
    return null;
  }

  /**
   * @param fieldAccess
   *          the {@link QualifiedName} or {@link FieldAccess} used to access some field.
   *
   * @return the "qualifier" part of accessed field.
   */
  public static Expression getFieldAccessQualifier(Expression fieldAccess) {
    if (fieldAccess instanceof FieldAccess) {
      return ((FieldAccess) fieldAccess).getExpression();
    } else if (fieldAccess instanceof QualifiedName) {
      return ((QualifiedName) fieldAccess).getQualifier();
    }
    return null;
  }

  /**
   * @param fieldAccess
   *          the {@link QualifiedName} or {@link FieldAccess} used to access some field.
   *
   * @return the {@link SimpleName} part of accessed field.
   */
  public static SimpleName getFieldAccessName(Expression fieldAccess) {
    if (fieldAccess instanceof FieldAccess) {
      return ((FieldAccess) fieldAccess).getName();
    } else if (fieldAccess instanceof QualifiedName) {
      return ((QualifiedName) fieldAccess).getName();
    }
    return null;
  }

  /**
   * @param fieldName
   *          the name of field.
   *
   * @return the {@link VariableDeclarationFragment} for given field name or <code>null</code> if
   *         not field found.
   */
  public static VariableDeclarationFragment getFieldFragmentByName(TypeDeclaration typeDeclaration,
      String fieldName) {
    for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
      for (VariableDeclarationFragment fragment : DomGenerics.fragments(fieldDeclaration)) {
        if (fieldName.equals(fragment.getName().getIdentifier())) {
          return fragment;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Enclosing" utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ASTNode} that encloses given position, such that there are no any child
   *         {@link ASTNode} that also encloses same position.
   */
  public static ASTNode getEnclosingNode(ASTNode root, final int position) {
    final ASTNode result[] = new ASTNode[1];
    root.accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        int start = node.getStartPosition();
        int length = node.getLength();
        if (result[0] == null && start <= position && position < start + length) {
          result[0] = node;
        }
      }
    });
    return result[0];
  }

  /**
   * @return the {@link Statement} that encloses given {@link ASTNode}.
   */
  public static Statement getEnclosingStatement(ASTNode node) {
    return getEnclosingNode(node, Statement.class);
  }

  /**
   * @return the {@link Block} that encloses given {@link ASTNode}.
   */
  public static Block getEnclosingBlock(ASTNode node) {
    return getEnclosingNode(node, Block.class);
  }

  /**
   * @return the {@link FieldDeclaration} that encloses given {@link ASTNode}.
   */
  public static FieldDeclaration getEnclosingFieldDeclaration(ASTNode node) {
    return getEnclosingNode(node, FieldDeclaration.class);
  }

  /**
   * @return the {@link MethodDeclaration} that encloses given {@link ASTNode}.
   */
  public static MethodDeclaration getEnclosingMethod(ASTNode node) {
    return getEnclosingNode(node, MethodDeclaration.class);
  }

  /**
   * @return the {@link MethodDeclaration} that encloses given {@link ASTNode} and is child of given
   *         {@link TypeDeclaration}.
   */
  public static MethodDeclaration getEnclosingMethod(TypeDeclaration type, ASTNode node) {
    MethodDeclaration[] methods = type.getMethods();
    for (MethodDeclaration method : methods) {
      if (contains(method, node)) {
        return method;
      }
    }
    return null;
  }

  /**
   * @return the {@link TypeDeclaration} that encloses given {@link ASTNode}.
   */
  public static TypeDeclaration getEnclosingType(ASTNode node) {
    return getEnclosingNode(node, TypeDeclaration.class);
  }

  /**
   * @return the top-level {@link TypeDeclaration} that encloses given {@link ASTNode}.
   */
  public static TypeDeclaration getEnclosingTypeTop(ASTNode node) {
    TypeDeclaration typeDeclaration = getEnclosingType(node);
    while (true) {
      TypeDeclaration declaringType = getEnclosingType(typeDeclaration.getParent());
      if (declaringType == null) {
        break;
      }
      typeDeclaration = declaringType;
    }
    return typeDeclaration;
  }

  /**
   * @return the parent of {@link ASTNode} with given class (or its subclass) or <code>null</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getEnclosingNode(ASTNode node, Class<T> resultClass) {
    while (node != null) {
      if (resultClass.isAssignableFrom(node.getClass())) {
        return (T) node;
      }
      node = node.getParent();
    }
    return null;
  }

  /**
   * @return the {@link TypeDeclaration} that contains given {@link MethodDeclaration}.
   */
  public static TypeDeclaration getParentType(MethodDeclaration methodDeclaration) {
    ASTNode parentNode = methodDeclaration.getParent();
    if (parentNode instanceof AnonymousClassDeclaration) {
      return AnonymousTypeDeclaration.create((AnonymousClassDeclaration) parentNode);
    } else {
      return (TypeDeclaration) parentNode;
    }
  }

  /**
   * @return the {@link ASTNode} which is parent for both given nodes and not equal either. Can not
   *         be <code>null</code>.
   */
  public static ASTNode getCommonParent(ASTNode node_1, ASTNode node_2) {
    Assert.isNotNull(node_1, "Null node_1");
    Assert.isNotNull(node_2, "Null node_2");
    // prepare path from node_1 to CompilationUnit
    Set<ASTNode> ancestors = Sets.newHashSet();
    {
      ASTNode node = node_1.getParent();
      while (node != null) {
        ancestors.add(node);
        node = node.getParent();
      }
    }
    // find first parent of node_2 on this path
    ASTNode node = node_2.getParent();
    while (true) {
      if (ancestors.contains(node)) {
        return node;
      }
      node = node.getParent();
    }
  }

  /**
   * @return the {@link Block} which contains given nodes and not equal either. May be
   *         <code>null</code>.
   */
  public static Block getCommonBlock(ASTNode node_1, ASTNode node_2) {
    ASTNode commonParent = getCommonParent(node_1, node_2);
    return getEnclosingBlock(commonParent);
  }

  /**
   * @return the {@link Statement} which is direct child of {@link Block} and contains given node.
   */
  public static Statement getStatementWithinBlock(Block block, ASTNode node) {
    List<Statement> statements = DomGenerics.statements(block);
    while (node != null) {
      if (statements.contains(node)) {
        return (Statement) node;
      }
      node = node.getParent();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Statements
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Statement} directly after given in same {@link Block}, may be
   *         <code>null</code>.
   */
  public static Statement getNextStatement(Statement statement) {
    Block block = (Block) statement.getParent();
    List<Statement> statements = DomGenerics.statements(block);
    return GenericsUtils.getNextOrNull(statements, statement);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Checks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link ASTNode} is dangling, i.e. disconnected from
   *         {@link CompilationUnit}.
   */
  public static boolean isDanglingNode(ASTNode node) {
    return !(node.getRoot() instanceof CompilationUnit);
  }

  /**
   * @return <code>true</code> if <code>node_1</code> contains <code>node_2</code>.
   */
  public static boolean contains(ASTNode node_1, ASTNode node_2) {
    while (node_2 != null) {
      node_2 = node_2.getParent();
      if (node_1 == node_2) {
        return true;
      }
    }
    return false;
  }

  /**
   * Removes "dangling" {@link ASTNode}-s.
   */
  public static void removeDanglingNodes(Iterable<? extends ASTNode> nodes) {
    Iterator<? extends ASTNode> I = nodes.iterator();
    while (I.hasNext()) {
      ASTNode node = I.next();
      if (isDanglingNode(node)) {
        I.remove();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source range utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the index of beginning source for given node.
   */
  public static int getSourceBegin(ASTNode node) {
    return node.getStartPosition();
  }

  /**
   * @return the index of ending source for given node.
   */
  public static int getSourceEnd(ASTNode node) {
    return node.getStartPosition() + node.getLength();
  }

  /**
   * Sets the index of beginning source for given node.
   */
  public static void setSourceBegin(ASTNode node, int begin) {
    node.setSourceRange(begin, node.getLength());
  }

  /**
   * Sets the index of beginning source for given node, with keeping end position (i.e. update also
   * length).
   */
  public static void setSourceBegin_keepEnd(ASTNode node, int begin) {
    int delta = node.getStartPosition() - begin;
    int newLength = node.getLength() + delta;
    node.setSourceRange(begin, newLength);
  }

  /**
   * Sets source length to match end position of "source" node.
   */
  public static void setSourceEnd(ASTNode targetNode, ASTNode sourceNode) {
    int begin = targetNode.getStartPosition();
    int end = getSourceEnd(sourceNode);
    targetNode.setSourceRange(begin, end - begin);
  }

  /**
   * Sets the length of source for given node.
   */
  public static void setSourceLength(ASTNode node, int length) {
    node.setSourceRange(node.getStartPosition(), length);
  }

  /**
   * Move given {@link ASTNode} to the new position. This methods just changes start positions for
   * given node and its children, it does not modifies source.
   */
  public static void moveNode(ASTNode nodeToMove, int targetPosition) {
    int sourcePosition = nodeToMove.getStartPosition();
    final int moveDelta = targetPosition - sourcePosition;
    nodeToMove.accept(new ASTVisitor(true) {
      @Override
      public void postVisit(ASTNode node) {
        node.setSourceRange(node.getStartPosition() + moveDelta, node.getLength());
      }
    });
  }

  /**
   * Copies source range from given source node to the given target {@link ASTNode}.
   */
  public static void copySourceRange(ASTNode targetNode, ASTNode sourceNode) {
    targetNode.setSourceRange(sourceNode.getStartPosition(), sourceNode.getLength());
  }

  /**
   * Sets source range for given target node same as beginning of "beginNode" and end of "endNode".
   */
  public static void setSourceRange(ASTNode targetNode, ASTNode beginNode, ASTNode endNode) {
    int beginPosition = getSourceBegin(beginNode);
    int endPosition = getSourceEnd(endNode);
    targetNode.setSourceRange(beginPosition, endPosition - beginPosition);
  }

  /**
   * Sets beginning of source range for given target node to same as position as given source node,
   * and length - same as length of source plus given "delta".
   *
   * @param delta
   *          is used mostly for {@link Statement} creation, when we should add ';' at the end.
   */
  public static void setSourceRange(ASTNode targetNode, ASTNode sourceNode, int delta) {
    targetNode.setSourceRange(sourceNode.getStartPosition(), sourceNode.getLength() + delta);
  }

  /**
   * Sets beginning of source range for given target node to same as position as given begin node,
   * and length - same as length of source plus given "delta".
   *
   * @param delta
   *          is used mostly for {@link Statement} creation, when we should add ';' at the end.
   */
  public static void setSourceRange(ASTNode targetNode,
      ASTNode beginNode,
      ASTNode endNode,
      int delta) {
    int begin = getSourceBegin(beginNode);
    int end = getSourceEnd(endNode) + delta;
    targetNode.setSourceRange(begin, end - begin);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param root
   *          the starting node
   *
   * @return all {@link VariableDeclaration}'s that exists in {@link ASTNode}'s hierarchy.
   */
  public static List<VariableDeclaration> getVariableDeclarationsAll(ASTNode root) {
    // prepare gatherer
    ListGatherer<VariableDeclaration> gatherer = new ListGatherer<VariableDeclaration>() {
      @Override
      public void postVisit(ASTNode node) {
        if (node instanceof VariableDeclaration) {
          addResult((VariableDeclaration) node);
        }
      }
    };
    // gather variables
    root.accept(gatherer);
    // OK, final result
    return gatherer.getResultList();
  }

  /**
   * Return {@link VariableDeclaration}'s such that at their positions they can see new
   * {@link VariableDeclaration} declared at given position. So, new {@link VariableDeclaration} can
   * be shadowed by existing one, if it will have same name.
   *
   * @param root
   *          the starting node
   * @param position
   *          the position to search visible variable declarations
   *
   * @return an possibly shadowing {@link VariableDeclaration}'s.
   */
  public static List<VariableDeclaration> getVariableDeclarationsAfter(ASTNode root, int position) {
    ASTNode targetNode = getEnclosingNode(root, position);
    // prepare gatherer
    ListGatherer<VariableDeclaration> gatherer = new ListGatherer<VariableDeclaration>() {
      @Override
      public void postVisit(ASTNode node) {
        if (node instanceof VariableDeclaration) {
          addResult((VariableDeclaration) node);
        }
      }
    };
    // prepare block
    Block targetBlock = getEnclosingBlock(targetNode);
    TypeDeclaration targetType = getEnclosingType(targetNode);
    if (targetBlock != null) {
      for (Statement statement : DomGenerics.statements(targetBlock)) {
        if (statement.getStartPosition() >= position) {
          statement.accept(gatherer);
        }
      }
    } else if (targetType != null) {
      // when target it type, possible shadows are in methods (with recursion)
      for (BodyDeclaration bodyDeclaration : DomGenerics.bodyDeclarations(targetType)) {
        if (!(bodyDeclaration instanceof FieldDeclaration)) {
          bodyDeclaration.accept(gatherer);
        }
      }
    }
    // OK, final result
    return gatherer.getResultList();
  }

  /**
   * @param root
   *          the starting node
   * @param position
   *          the position to search visible variable declarations
   *
   * @return the {@link VariableDeclaration}'s visible at given position.
   */
  public static List<VariableDeclaration> getVariableDeclarationsVisibleAt(ASTNode root,
      int position) {
    List<VariableDeclaration> declarations = Lists.newArrayList();
    ASTNode node = getEnclosingNode(root, position);
    // if we hit the empty space inside of block, process all statements before given position
    if (node instanceof Block) {
      Block block = (Block) node;
      for (Statement statement : DomGenerics.statements(block)) {
        if (statement.getStartPosition() < position) {
          addStatementVariableDeclarations(declarations, statement);
        }
      }
    }
    // go up along hierarchy and remember variable declarations
    while (node != null) {
      // type - add fields
      if (node instanceof TypeDeclaration) {
        TypeDeclaration type = (TypeDeclaration) node;
        for (FieldDeclaration fieldDeclaration : type.getFields()) {
          declarations.addAll(DomGenerics.fragments(fieldDeclaration));
        }
      }
      // method - add parameters
      if (node instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) node;
        declarations.addAll(DomGenerics.parameters(method));
      }
      // statement - process siblings above
      if (node instanceof Statement && node.getParent() instanceof Block) {
        Statement statement = (Statement) node;
        Block block = (Block) statement.getParent();
        for (Statement siblingStatement : DomGenerics.statements(block)) {
          if (siblingStatement == statement) {
            break;
          }
          addStatementVariableDeclarations(declarations, siblingStatement);
        }
      }
      // go to parent
      node = node.getParent();
    }
    // OK, final result
    return declarations;
  }

  /**
   * Add {@link VariableDeclarationFragment}'s if given statement is
   * {@link VariableDeclarationStatement}.
   */
  private static void addStatementVariableDeclarations(List<VariableDeclaration> declarations,
      Statement statement) {
    if (statement instanceof VariableDeclarationStatement) {
      VariableDeclarationStatement variableStatement = (VariableDeclarationStatement) statement;
      declarations.addAll(DomGenerics.fragments(variableStatement));
    }
  }

  /**
   * @return <code>true</code> if given {@link ASTNode} is single variable {@link SimpleName} or
   *         {@link FieldAccess} like "this.fieldName".
   */
  public static boolean isVariable(ASTNode variable) {
    // FieldAccess
    if (variable instanceof FieldAccess) {
      FieldAccess fieldAccess = (FieldAccess) variable;
      return fieldAccess.getExpression() instanceof ThisExpression;
    }
    // SimpleName
    if (variable instanceof SimpleName) {
      StructuralPropertyDescriptor locationInParent = variable.getLocationInParent();
      if (locationInParent == MethodInvocation.NAME_PROPERTY
          || locationInParent == SimpleType.NAME_PROPERTY
          || locationInParent == FieldAccess.NAME_PROPERTY
          || locationInParent == QualifiedName.NAME_PROPERTY
          || locationInParent == MethodDeclaration.NAME_PROPERTY
          || locationInParent == TypeDeclaration.NAME_PROPERTY) {
        return false;
      }
      // variable has binding
      return getVariableBinding(variable) != null;
    }
    // unknown ASTNode
    return false;
  }

  /**
   * @return the name of given variable.
   */
  public static String getVariableName(ASTNode variable) {
    return getVariableSimpleName(variable).getIdentifier();
  }

  /**
   * @return the {@link SimpleName} of given variable.
   */
  public static SimpleName getVariableSimpleName(ASTNode variable) {
    if (variable instanceof FieldAccess) {
      return ((FieldAccess) variable).getName();
    }
    return (SimpleName) variable;
  }

  /**
   * Calculate actual value for reusable variables & fields.
   *
   * @param expression
   *          the {@link Expression} for actualization.
   * @return the {@link Expression} identified actual value for original expression.
   */
  public static Expression getActualVariableExpression(Expression expression) {
    if (AstNodeUtils.isVariable(expression)) {
      MethodDeclaration enclosingMethod = AstNodeUtils.getEnclosingMethod(expression);
      ASTNode lastAssignment =
          ExecutionFlowUtils.getLastAssignment(
              new ExecutionFlowDescription(enclosingMethod),
              expression);
      if (lastAssignment != null) {
        MethodDeclaration method = AstNodeUtils.getEnclosingMethod(lastAssignment);
        // Note: we need assignment from enclosing method or field initializer
        if (method == null || method == enclosingMethod) {
          if (lastAssignment instanceof Assignment) {
            Assignment assignment = (Assignment) lastAssignment;
            return assignment.getRightHandSide();
          } else if (lastAssignment instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) lastAssignment;
            Expression initializer = fragment.getInitializer();
            if (initializer != null) {
              return initializer;
            }
          }
        }
      }
    }
    return expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Package
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the name of package, may be empty for default package.
   */
  public static String getPackageName(CompilationUnit unit) {
    PackageDeclaration packageDeclaration = unit.getPackage();
    return packageDeclaration != null ? packageDeclaration.getName().getFullyQualifiedName() : "";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Literals
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if all given {@link Expression}'s is literals, see
   *         {@link #isLiteral(Expression)}.
   */
  public static boolean areLiterals(List<Expression> expressions) {
    for (Expression expression : expressions) {
      if (!isLiteral(expression)) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return <code>true</code> if given {@link Expression} is literal, i.e. may be evaluated without
   *         outer context.
   */
  public static boolean isLiteral(Expression expression) {
    if (expression instanceof BooleanLiteral
        || expression instanceof NumberLiteral
        || expression instanceof StringLiteral
        || expression instanceof NullLiteral
        || expression instanceof QualifiedName) {
      return true;
    }
    if (expression instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) expression;
      return isLiteral(castExpression.getExpression());
    }
    if (expression instanceof PrefixExpression) {
      PrefixExpression prefixExpression = (PrefixExpression) expression;
      return isLiteral(prefixExpression.getOperand());
    }
    if (expression instanceof InfixExpression) {
      InfixExpression infixExpression = (InfixExpression) expression;
      List<Expression> operands = DomGenerics.allOperands(infixExpression);
      return areLiterals(operands);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Caching
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  private static <T> T getValue(ASTNode node, String key, RunnableObjectEx<T> evaluator) {
    // if "null", don't use caching
    if (node == null) {
      return ExecutionUtils.runObject(evaluator);
    }
    // prepare keys
    String keyValue = "ASTNodeUtilities." + key;
    String keyHas = keyValue + ".has";
    // check if has cached value
    if (node.getProperty(keyHas) == Boolean.TRUE && isCacheUpToDate(node, key)) {
      T value = (T) node.getProperty(keyValue);
      return value;
    }
    // prepare value
    T value = ExecutionUtils.runObject(evaluator);
    node.setProperty(keyValue, value);
    node.setProperty(keyHas, Boolean.TRUE);
    // done
    return value;
  }

  /**
   * @return <code>false</code> if {@link AST} was modified since last request. Then marks it as not
   *         modified, so next call will say that it is not modified.
   */
  private static boolean isCacheUpToDate(ASTNode node, String key) {
    String keyStamp = "ASTNodeUtilities." + key + ".stamp";
    long currentStamp = node.getAST().modificationCount();
    Long nodeStamp = (Long) node.getProperty(keyStamp);
    node.setProperty(keyStamp, currentStamp);
    return nodeStamp != null && nodeStamp.longValue() == currentStamp;
  }
}
