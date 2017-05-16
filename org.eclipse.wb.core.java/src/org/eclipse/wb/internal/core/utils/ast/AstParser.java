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

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.binding.BindingContext;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Parser for {@link ASTNode}'s.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class AstParser {
  public static final String KEY_TYPE_BINDING = "TYPE_BINDING";
  public static final String KEY_METHOD_BINDING = "METHOD_BINDING";
  public static final String KEY_VARIABLE_BINDING = "VARIABLE_BINDING";
  public static final String KEY_IGNORE_THIS_METHOD = "Ignore this method for parsing context";
  private final AstEditor m_editor;
  private final BindingContext m_context;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  AstParser(AstEditor editor) {
    m_editor = editor;
    m_context = m_editor.getBindingContext();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the new {@link Expression} for given source and position.
   */
  public Expression parseExpression(int position, String src) throws Exception {
    // special handling for "null"
    if ("null".equals(src)) {
      Expression expression = getAst().newNullLiteral();
      expression.setSourceRange(position, "null".length());
      return expression;
    }
    // prepare expression
    Expression expression;
    {
      String source = "java.lang.System.out.println(" + src + ");";
      ExpressionStatement statement = (ExpressionStatement) parseStatement(position, source);
      MethodInvocation invocation = (MethodInvocation) statement.getExpression();
      expression = (Expression) invocation.arguments().get(0);
      // replace expression to make our expression free
      {
        SimpleName fakeExpression = getAst().newSimpleName("foo");
        DomGenerics.arguments(invocation).set(0, fakeExpression);
      }
    }
    // set correct position
    AstNodeUtils.moveNode(expression, position);
    return expression;
  }

  /**
   * @return the new {@link Statement} for given source and position.
   *
   * @param position
   *          the position when given <code>src</code> should start
   * @param src
   *          the source (possible with leading whitespaces) that contains {@link Statement}
   */
  public Statement parseStatement(int position, String src) throws Exception {
    String source = "";
    source += getSourceUnitHeader();
    // open class and constructor
    {
      TypeDeclaration typeDeclaration = m_editor.getEnclosingType(position);
      source += getClassDeclarationHeader(typeDeclaration);
      source += getOpenConstructorSource(typeDeclaration);
    }
    // add visible variables
    source += getVisibleVariablesCode(position);
    // open anonymous class instance, if present
    String anonymousClassSource = getAnonymousClassCode(position);
    source += anonymousClassSource;
    // add source of statement
    int statementPosition = source.length();
    source += src;
    // close anonymous class instance, if present
    if (!StringUtils.isEmpty(anonymousClassSource)) {
      source += "}};\n";
    }
    // close method and class
    source += "}\n";
    source += "}";
    // parse and find
    try {
      return (Statement) findNode(source, position, Statement.class, statementPosition);
    } catch (DesignerException e) {
      String problems = e.getParameters()[1];
      throw new DesignerException(ICoreExceptionConstants.AST_PARSE_ERROR, e, src, problems);
    }
  }

  /**
   * @return the new {@link BodyDeclaration} for given source and position.
   *
   * @param position
   *          the position when given <code>src</code> should start
   * @param src
   *          the source (possible with leading whitespace) that contains {@link BodyDeclaration}
   */
  public BodyDeclaration parseBodyDeclaration(int position, String src) throws Exception {
    String source = m_editor.getSource();
    source = source.substring(0, position) + src + source.substring(position);
    // parse and find
    try {
      return (BodyDeclaration) findNode(source, position, BodyDeclaration.class, position);
    } catch (DesignerException e) {
      String problems = e.getParameters()[1];
      throw new DesignerException(ICoreExceptionConstants.AST_PARSE_ERROR, e, src, problems);
    }
  }

  /**
   * @return the new {@link ImportDeclaration} for given name and position.
   */
  public ImportDeclaration parseImportDeclaration(int position, String qualifiedName)
      throws Exception {
    String source = "import " + qualifiedName + ";";
    source += "class Clazz {}";
    return (ImportDeclaration) findNode(source, position, ImportDeclaration.class, 0);
  }

  /**
   * @return the {@link Type} for given qualified name.
   */
  public Type parseQualifiedType(int position, String name) throws Exception {
    VariableDeclarationStatement statement =
        (VariableDeclarationStatement) parseStatement(position, name + " __parseName;");
    Type type = statement.getType();
    statement.setType(type.getAST().newPrimitiveType(PrimitiveType.VOID));
    return type;
  }

  /**
   * @return the copy of given {@link Type} on given position.
   */
  public Type parseType(int position, Type sourceType) throws Exception {
    // prepare new type
    Type newType;
    if (sourceType instanceof PrimitiveType) {
      PrimitiveType primitiveSourceType = (PrimitiveType) sourceType;
      newType = getAst().newPrimitiveType(primitiveSourceType.getPrimitiveTypeCode());
      newType.setSourceRange(position, newType.toString().length());
    } else {
      // prepare name of type
      Name newTypeName;
      {
        String typeString = m_editor.getSource(sourceType);
        if (typeString.indexOf('.') == -1) {
          newTypeName = parseSimpleName(position, typeString);
        } else {
          newTypeName = parseQualifiedName(position, typeString);
        }
      }
      // create type for name
      newType = getAst().newSimpleType(newTypeName);
      AstNodeUtils.copySourceRange(newType, newTypeName);
    }
    // copy type binding
    {
      ITypeBinding sourceTypeBinding = AstNodeUtils.getTypeBinding(sourceType);
      ITypeBinding newTypeBinding = m_context.get(sourceTypeBinding);
      newType.setProperty(KEY_TYPE_BINDING, newTypeBinding);
    }
    // return created type
    return newType;
  }

  /**
   * @return the {@link SimpleName} for given identifier.
   *
   *         NB! This method should not be used directly, use for example
   *         {@link #parseVariable(int, String, ITypeBinding, boolean, int)} instead.
   */
  public SimpleName parseSimpleName(int position, String identifier) {
    SimpleName simpleName = getAst().newSimpleName(identifier);
    simpleName.setSourceRange(position, identifier.length());
    return simpleName;
  }

  /**
   * @return the {@link QualifiedName} for given name.
   */
  public QualifiedName parseQualifiedName(int startPosition, String src) {
    String parts[] = StringUtils.split(src, '.');
    Assert.isTrue(parts.length >= 2);
    // build QualifiedName
    Name result = null;
    {
      int currentPosition = startPosition;
      for (int i = 0; i < parts.length; i++) {
        String part = parts[i];
        //
        if (result == null) {
          result = parseSimpleName(currentPosition, part);
        } else {
          currentPosition++;
          result = getAst().newQualifiedName(result, parseSimpleName(currentPosition, part));
        }
        //
        currentPosition += part.length();
        result.setSourceRange(startPosition, currentPosition - startPosition);
      }
    }
    // return result
    return (QualifiedName) result;
  }

  /**
   * @return the {@link SimpleName} for variable with given identifier, type and field/modifiers
   *         flags.
   */
  public SimpleName parseVariable(int position,
      String identifier,
      ITypeBinding declaringClass,
      ITypeBinding type,
      boolean field,
      int modifiers) {
    SimpleName simpleName = parseSimpleName(position, identifier);
    simpleName.setProperty(KEY_TYPE_BINDING, m_context.get(type));
    simpleName.setProperty(
        KEY_VARIABLE_BINDING,
        m_context.get(identifier, declaringClass, type, field, modifiers));
    return simpleName;
  }

  /**
   * @return the {@link SimpleType} with given identifier and binding.
   */
  public SimpleType parseSimpleType(int position, String identifier, ITypeBinding binding) {
    SimpleName simpleName = parseSimpleName(position, identifier);
    SimpleType simpleType = getAst().newSimpleType(simpleName);
    simpleType.setProperty(KEY_TYPE_BINDING, m_context.get(binding));
    AstNodeUtils.copySourceRange(simpleType, simpleName);
    return simpleType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "header" of {@link CompilationUnit} source - package and imports.
   */
  private String getSourceUnitHeader() throws Exception {
    CompilationUnit unit = m_editor.getAstUnit();
    String header = "";
    // add package
    if (unit.getPackage() != null) {
      header = "package " + unit.getPackage().getName().toString() + ";\n";
    }
    // add imports
    for (ImportDeclaration declaration : DomGenerics.imports(unit)) {
      header += m_editor.getSource(declaration) + "\n";
    }
    // return result
    return header;
  }

  /**
   * @return the source with declaring class that has same super-class, interfaces, declared inner
   *         types, fields and methods.
   */
  private String getClassDeclarationHeader(TypeDeclaration typeDeclaration) {
    String source = "";
    // class or interface
    {
      ITypeBinding binding = AstNodeUtils.getTypeBinding(typeDeclaration);
      if (binding.isInterface()) {
        source += "interface ";
      } else {
        if (AstNodeUtils.isAbstract(binding)) {
          source += "abstract ";
        }
        source += "class ";
      }
    }
    // declare class
    {
      int nameBegin = AstNodeUtils.getSourceBegin(typeDeclaration.getName());
      int openBrace = m_editor.indexOf("{", nameBegin);
      source += m_editor.getSourceBeginEnd(nameBegin, openBrace);
      source = source.trim();
    }
    // open class
    source += " {\n";
    // add inner types
    {
      TypeDeclaration[] innerTypes = typeDeclaration.getTypes();
      for (TypeDeclaration innerType : innerTypes) {
        if (AstNodeUtils.isStatic(innerType)) {
          source += "static ";
        }
        source += getClassDeclarationHeader(innerType) + "}\n";
      }
    }
    // add fields
    {
      FieldDeclaration[] fields = typeDeclaration.getFields();
      for (FieldDeclaration field : fields) {
        // ignore field with invisible type
        if (!isVisibleType(field.getType())) {
          continue;
        }
        // add source
        source += getFieldSourceForParsingContext(field) + "\n";
      }
    }
    // add enums (since they are not returned as inner types)
    {
      source += getEnumsDeclarations(typeDeclaration);
    }
    // add local methods
    {
      MethodDeclaration[] methods = typeDeclaration.getMethods();
      for (MethodDeclaration method : methods) {
        source += getMethodDeclarationSource(method);
      }
    }
    // return opened type declaration
    return source;
  }

  /**
   * @return the source for declaring {@link MethodDeclaration}.
   */
  private String getMethodDeclarationSource(MethodDeclaration method) {
    IMethodBinding binding = AstNodeUtils.getMethodBinding(method);
    if (binding == null) {
      return "";
    }
    // check if should be added
    if (method.getProperty(KEY_IGNORE_THIS_METHOD) == Boolean.TRUE) {
      return "";
    }
    if (binding.isConstructor() && isMethodOfTopType(method)) {
      return "";
    }
    // OK, prepare source
    String source = "";
    // declare method
    {
      int sourceBegin = getMethodDeclarationSourceBegin(method);
      int sourceEnd;
      if (method.getBody() != null) {
        sourceEnd = AstNodeUtils.getSourceBegin(method.getBody());
      } else {
        sourceEnd = AstNodeUtils.getSourceEnd(method);
      }
      source += m_editor.getSourceBeginEnd(sourceBegin, sourceEnd);
      source = StringUtilities.normalizeWhitespaces(source);
    }
    // abstract or body
    if (AstNodeUtils.isAbstract(binding)) {
      // no body
      source += "\n";
    } else {
      // open method
      source += "{";
      // constructor
      if (binding.isConstructor()) {
        source += getConstructorBodySource(method);
      }
      // add "return"
      String returnTypeName = AstNodeUtils.getFullyQualifiedName(binding.getReturnType(), false);
      if (!"void".equals(returnTypeName)) {
        source += "return " + getDefaultValue(returnTypeName) + ";";
      }
      // close method
      source += "}\n";
    }
    // result
    return source;
  }

  private static String getConstructorBodySource(MethodDeclaration method) {
    List<Statement> statements = DomGenerics.statements(method);
    if (!statements.isEmpty()) {
      if (statements.get(0) instanceof SuperConstructorInvocation) {
        SuperConstructorInvocation invocation = (SuperConstructorInvocation) statements.get(0);
        IMethodBinding binding = AstNodeUtils.getSuperBinding(invocation);
        return "super" + getMethodArgumentsSource(binding);
      }
      if (statements.get(0) instanceof ConstructorInvocation) {
        ConstructorInvocation invocation = (ConstructorInvocation) statements.get(0);
        IMethodBinding binding = AstNodeUtils.getBinding(invocation);
        return "this" + getMethodArgumentsSource(binding);
      }
    }
    return "";
  }

  private static boolean isMethodOfTopType(MethodDeclaration method) {
    ASTNode parentType = method.getParent();
    return parentType.getParent() instanceof CompilationUnit;
  }

  private int getMethodDeclarationSourceBegin(MethodDeclaration method) {
    // modifiers
    {
      List<ASTNode> modifiers = DomGenerics.modifiersNodes(method);
      if (!modifiers.isEmpty()) {
        return modifiers.get(0).getStartPosition();
      }
    }
    // type parameters <T>
    {
      List<TypeParameter> parameters = DomGenerics.typeParameters(method);
      if (!parameters.isEmpty()) {
        int begin = parameters.get(0).getStartPosition();
        begin = m_editor.indexOfCharBackward('<', begin);
        return begin;
      }
    }
    // return type
    {
      Type type = method.getReturnType2();
      if (type != null) {
        return type.getStartPosition();
      }
    }
    // name
    return method.getName().getStartPosition();
  }

  /**
   * @returns the enum declarations if given {@link TypeDeclaration} has any within.
   */
  private String getEnumsDeclarations(TypeDeclaration typeDeclaration) {
    String source = "";
    List<EnumDeclaration> enums = DomGenerics.getEnums(typeDeclaration);
    for (EnumDeclaration enumDeclaration : enums) {
      source += "enum " + enumDeclaration.getName().getIdentifier();
      // open enum
      source += " {";
      List<ASTNode> enumConstants = DomGenerics.getEnumConstants(enumDeclaration);
      for (int i = 0; i < enumConstants.size(); i++) {
        EnumConstantDeclaration declaration = (EnumConstantDeclaration) enumConstants.get(i);
        source += declaration.getName().getIdentifier();
        if (i == enumConstants.size() - 1) {
          source += ";";
        } else {
          source += ", ";
        }
      }
      source += "}\n";
    }
    return source;
  }

  /**
   * @return the source of {@link FieldDeclaration} that can be used as context for parsing some
   *         source.
   */
  private static String getFieldSourceForParsingContext(FieldDeclaration field) {
    String fieldSource = "";
    // add modifiers
    {
      int modifiers = field.getModifiers();
      if ((modifiers & Modifier.STATIC) != 0) {
        fieldSource += "static ";
      }
      if ((modifiers & Modifier.FINAL) != 0) {
        fieldSource += "final ";
      }
    }
    // add type
    String typeName = AstNodeUtils.getFullyQualifiedName(field.getType(), false);
    fieldSource += typeName + " ";
    // add fragments, with default values
    for (VariableDeclarationFragment fragment : DomGenerics.fragments(field)) {
      if (!fieldSource.endsWith(" ")) {
        fieldSource += ", ";
      }
      fieldSource += fragment.getName().getIdentifier() + "=" + getDefaultValue(typeName);
    }
    // finalize
    fieldSource += ";";
    return fieldSource;
  }

  private String getOpenConstructorSource(TypeDeclaration typeDeclaration) {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(typeDeclaration);
    ITypeBinding superTypeBinding = typeBinding.getSuperclass();
    String constructorCode = typeDeclaration.getName().getIdentifier() + "(Object __wbp_param) {\n";
    // prepare declared methods
    IMethodBinding[] declaredMethods;
    try {
      declaredMethods = superTypeBinding.getDeclaredMethods();
    } catch (Throwable e) {
      declaredMethods = new IMethodBinding[0];
    }
    // try to find constructor in superclass
    for (IMethodBinding methodBinding : declaredMethods) {
      int modifiers = methodBinding.getModifiers();
      // handle only public or protected constructor
      if (methodBinding.isConstructor()
          && (Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers))) {
        constructorCode += "super" + getMethodArgumentsSource(methodBinding);
        break;
      }
    }
    return constructorCode;
  }

  private static String getMethodArgumentsSource(IMethodBinding methodBinding) {
    String source = "(";
    // add parameters for constructor
    ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      ITypeBinding parameterType = parameterTypes[i];
      // add comma
      if (i != 0) {
        source += ", ";
      }
      // add default value for parameter
      String parameterTypeName = AstNodeUtils.getFullyQualifiedName(parameterType, false);
      source += getDefaultValue(parameterTypeName);
    }
    // OK, constructor found
    source += ");";
    return source;
  }

  /**
   * @return the source that defines local variables with same type/name as all
   *         {@link VariableDeclaration} 's visible at given position. We need this to be able to
   *         resolve later type bindings for all expressions.
   */
  private String getVisibleVariablesCode(int position) {
    StringBuffer sb = new StringBuffer();
    List<VariableDeclaration> declarations =
        AstNodeUtils.getVariableDeclarationsVisibleAt(m_editor.getAstUnit(), position);
    for (VariableDeclaration declaration : declarations) {
      // prepare type of variable
      Type type = null;
      if (declaration instanceof SingleVariableDeclaration) {
        SingleVariableDeclaration parameter = (SingleVariableDeclaration) declaration;
        type = parameter.getType();
      } else if (declaration instanceof VariableDeclarationFragment) {
        VariableDeclarationFragment fragment = (VariableDeclarationFragment) declaration;
        if (fragment.getParent() instanceof FieldDeclaration) {
          // we add all fields into TypeDeclaration
          continue;
        } else if (fragment.getParent() instanceof VariableDeclarationStatement) {
          VariableDeclarationStatement statement =
              (VariableDeclarationStatement) fragment.getParent();
          type = statement.getType();
        }
      }
      Assert.isNotNull(type);
      // ignore invisible types
      if (!isVisibleType(type)) {
        continue;
      }
      // add declaration of local variable
      {
        ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(type);
        String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
        //
        sb.append(typeName);
        sb.append(' ');
        sb.append(declaration.getName().getIdentifier());
        sb.append(" = ");
        sb.append(getDefaultValue(typeName));
        sb.append(";\n");
      }
    }
    return sb.toString();
  }

  /**
   * @return <code>true</code> if given {@link Type} is visible externally.
   */
  private static boolean isVisibleType(Type type) {
    ITypeBinding fieldTypeBinding = AstNodeUtils.getTypeBinding(type);
    if (fieldTypeBinding == null) {
      return false;
    }
    return true;
  }

  /**
   * @return the source of default value for class with given name ('0' for primitives) and'null'
   *         for objects.
   */
  public static String getDefaultValue(String className) {
    // check for primitive type
    if ("boolean".equals(className)) {
      return "false";
    } else if ("byte".equals(className)) {
      return "(byte)0";
    } else if ("char".equals(className)) {
      return "'0'";
    } else if ("short".equals(className)) {
      return "(short)0";
    } else if ("int".equals(className)) {
      return "0";
    } else if ("long".equals(className)) {
      return "0L";
    } else if ("float".equals(className)) {
      return "0.0f";
    } else if ("double".equals(className)) {
      return "0.0";
    }
    // use "null" for Object
    return "(" + className + ") null";
  }

  /**
   * @return the source that opens anonymous class instance with initializer.
   */
  private String getAnonymousClassCode(int position) {
    ASTNode node = m_editor.getEnclosingNode(position);
    Initializer initializer = null;
    AnonymousClassDeclaration declaration = null;
    ClassInstanceCreation creation = null;
    while (node != null) {
      if (initializer != null && declaration != null && node instanceof Statement) {
        break;
      }
      if (initializer == null && node instanceof Initializer) {
        initializer = (Initializer) node;
      }
      if (declaration == null && node instanceof AnonymousClassDeclaration) {
        declaration = (AnonymousClassDeclaration) node;
        creation = (ClassInstanceCreation) declaration.getParent();
      }
      node = node.getParent();
    }
    // generate source
    if (initializer != null && declaration != null) {
      ITypeBinding typeBinding = declaration.resolveBinding().getSuperclass();
      if (typeBinding != null) {
        String typeName = AstNodeUtils.getFullyQualifiedName(typeBinding, false);
        String argumentsSource = "";
        List<Expression> arguments = DomGenerics.arguments(creation);
        for (Expression argument : arguments) {
          if (!StringUtils.isEmpty(argumentsSource)) {
            argumentsSource += ", ";
          }
          argumentsSource += m_editor.getSource(argument);
        }
        return "new " + typeName + "(" + argumentsSource + ") { {";
      }
    }
    // TODO
    return "";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Compilation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses given Java source and returns corresponding {@link CompilationUnit}.
   */
  private CompilationUnit parseCompilationUnit(String source) throws Exception {
    return CodeUtils.parseCompilationUnit(source, m_editor.getJavaProject(), "Fake");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AST} for this editor.
   */
  private AST getAst() {
    return m_editor.getAstUnit().getAST();
  }

  /**
   * Finds in {@link CompilationUnit} with given source an {@link ASTNode} at given position with
   * given class.
   *
   * @param source
   *          the source of {@link CompilationUnit} to parse
   * @param targetPosition
   *          we should selected {@link ASTNode} nearest to this position
   * @param nodeClass
   *          the class of {@link ASTNode} to select. We need it because several nodes with
   *          different classes can begin in one position for example {@link ExpressionStatement}
   *          and its {@link Expression}.
   * @param nodePosition
   *          the position in AST that corresponds <code>targetPosition</code>
   */
  private ASTNode findNode(String source,
      int targetPosition,
      Class<? extends ASTNode> nodeClass,
      int nodePosition) throws Exception {
    CompilationUnit compilationUnit = parseCompilationUnit(source);
    try {
      return findNode0(compilationUnit, targetPosition, nodeClass, nodePosition);
    } catch (Throwable e) {
      String problemsString = getProblemsString(compilationUnit);
      throw new DesignerException(ICoreExceptionConstants.AST_PARSE_ERROR,
          e,
          source,
          problemsString);
    }
  }

  private static String getProblemsString(CompilationUnit compilationUnit) throws Exception {
    StringBuilder problemsString = new StringBuilder();
    for (IProblem problem : compilationUnit.getProblems()) {
      // we parse in context of "fake" file, so this is expected problem
      if (problem.getID() == IProblem.PublicClassMustMatchFileName) {
        continue;
      }
      // append line break
      if (problemsString.length() != 0) {
        problemsString.append("\r\n");
      }
      // append problem
      problemsString.append("line: ");
      problemsString.append(problem.getSourceLineNumber());
      problemsString.append(" ");
      problemsString.append(problem.getMessage());
    }
    return problemsString.toString();
  }

  // XXX
  private ASTNode findNode0(CompilationUnit compilationUnit,
      final int targetPosition,
      final Class<? extends ASTNode> nodeClass,
      final int nodePosition) throws Exception {
    // find node
    final List<ASTNode> nodes = Lists.newArrayList();
    compilationUnit.accept(new ASTVisitor(true) {
      @Override
      public void preVisit(ASTNode node) {
        int pos = node.getStartPosition();
        // check for required node
        if (nodes.isEmpty() && pos >= nodePosition && nodeClass.isInstance(node)) {
          nodes.add(node);
        }
        // update positions of required (and following) nodes
        if (pos >= nodePosition) {
          int length = node.getLength();
          node.setSourceRange(targetPosition + pos - nodePosition, length);
        }
      }
    });
    // exactly one node should be found
    Assert.isTrue(nodes.size() == 1);
    // get node and copy it into current AST
    ASTNode externalNode = nodes.get(0);
    ASTNode internalNode = ASTNode.copySubtree(m_editor.getAstUnit().getAST(), externalNode);
    copyBindings(externalNode, internalNode);
    return internalNode;
  }

  /**
   * Copies {@link ITypeBinding}'s and {@link IMethodBinding}'s from source to target
   * {@link ASTNode}'s.
   */
  private final void copyBindings(ASTNode source, ASTNode target) {
    final Map<Integer, IBinding> indexToBinding = Maps.newTreeMap();
    // fetch original binding information
    {
      final int finalIndex[] = new int[]{0};
      source.accept(new ASTVisitor() {
        @Override
        public void postVisit(ASTNode node) {
          if (node instanceof SimpleName) {
            SimpleName simpleName = (SimpleName) node;
            IBinding binding = simpleName.resolveBinding();
            if (binding instanceof IVariableBinding) {
              saveBinding(binding);
            } else {
              saveBinding(null);
            }
          }
          if (node instanceof VariableDeclaration) {
            VariableDeclaration variableDeclaration = (VariableDeclaration) node;
            IVariableBinding binding = variableDeclaration.resolveBinding();
            saveBinding(binding);
          }
          if (node instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) node;
            saveBinding(fieldAccess.resolveFieldBinding());
          }
          // ITypeBinding
          {
            if (node instanceof Expression) {
              Expression expression = (Expression) node;
              ITypeBinding binding = expression.resolveTypeBinding();
              saveBinding(binding);
            }
            if (node instanceof Type) {
              Type type = (Type) node;
              ITypeBinding binding = type.resolveBinding();
              saveBinding(binding);
            }
            if (node instanceof TypeDeclaration) {
              TypeDeclaration typeDeclaration = (TypeDeclaration) node;
              ITypeBinding binding = typeDeclaration.resolveBinding();
              saveBinding(binding);
            }
            if (node instanceof AnonymousClassDeclaration) {
              AnonymousClassDeclaration anonymousClass = (AnonymousClassDeclaration) node;
              ITypeBinding binding = anonymousClass.resolveBinding();
              saveBinding(binding);
            }
          }
          // IMethodBinding
          {
            if (node instanceof MethodDeclaration) {
              MethodDeclaration method = (MethodDeclaration) node;
              IMethodBinding binding = method.resolveBinding();
              saveBinding(binding);
            }
            if (node instanceof MethodInvocation) {
              MethodInvocation invocation = (MethodInvocation) node;
              IMethodBinding binding = invocation.resolveMethodBinding();
              saveBinding(binding);
            }
            if (node instanceof SuperConstructorInvocation) {
              SuperConstructorInvocation invocation = (SuperConstructorInvocation) node;
              IMethodBinding binding = invocation.resolveConstructorBinding();
              saveBinding(binding);
            }
            if (node instanceof SuperMethodInvocation) {
              SuperMethodInvocation invocation = (SuperMethodInvocation) node;
              IMethodBinding binding = invocation.resolveMethodBinding();
              saveBinding(binding);
            }
            if (node instanceof ClassInstanceCreation) {
              ClassInstanceCreation creation = (ClassInstanceCreation) node;
              IMethodBinding binding = creation.resolveConstructorBinding();
              saveBinding(binding);
            }
          }
        }

        public void saveBinding(IBinding binding) {
          int index = finalIndex[0]++;
          if (binding != null) {
            indexToBinding.put(index, binding);
          }
        }
      });
    }
    // store binding information to target
    {
      final int finalIndex[] = new int[]{0};
      target.accept(new ASTVisitor() {
        @Override
        public void postVisit(ASTNode node) {
          if (node instanceof SimpleName
              || node instanceof FieldAccess
              || node instanceof VariableDeclaration) {
            int index = finalIndex[0]++;
            IVariableBinding binding = (IVariableBinding) indexToBinding.get(index);
            // binding can be null if this is not variable
            if (binding != null) {
              node.setProperty(KEY_VARIABLE_BINDING, m_context.get(binding));
            }
          }
          // ITypeBinding
          if (node instanceof Expression
              || node instanceof Type
              || node instanceof TypeDeclaration
              || node instanceof AnonymousClassDeclaration) {
            int index = finalIndex[0]++;
            ITypeBinding binding = (ITypeBinding) indexToBinding.get(index);
            if (binding != null) {
              node.setProperty(KEY_TYPE_BINDING, m_context.get(binding, true));
            }
          }
          // IMethodBinding
          if (node instanceof MethodDeclaration
              || node instanceof MethodInvocation
              || node instanceof SuperConstructorInvocation
              || node instanceof SuperMethodInvocation
              || node instanceof ClassInstanceCreation) {
            int index = finalIndex[0]++;
            IMethodBinding binding = (IMethodBinding) indexToBinding.get(index);
            Assert.isNotNull(binding);
            node.setProperty(KEY_METHOD_BINDING, m_context.get(binding));
          }
        }
      });
    }
  }
}
