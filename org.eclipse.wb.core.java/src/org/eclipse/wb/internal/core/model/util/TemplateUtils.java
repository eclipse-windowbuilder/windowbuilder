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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Statement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Utils for evaluating simple {@link JavaInfo} based expressions.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class TemplateUtils {
  public static final String ID_PREFIX = "__wbpId:";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Low level expression utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the deferred reference on given {@link JavaInfo}.
   */
  public static String getExpression(JavaInfo javaInfo) {
    return ID_PREFIX + ObjectInfoUtils.getId(javaInfo);
  }

  /**
   * Formats text using {@link MessageFormat}, and replaces {@link JavaInfo} references with
   * "delayed" references, in form <code>"__wbpId:id"</code>. These references should be resolved
   * later, when {@link NodeTarget} will be known.
   *
   * @return the formatted text.
   */
  public static String format(String pattern, Object... arguments) {
    Object[] updatedArguments = new Object[arguments.length];
    for (int i = 0; i < arguments.length; i++) {
      Object argument = arguments[i];
      if (argument instanceof JavaInfo) {
        argument = getExpression((JavaInfo) argument);
      }
      updatedArguments[i] = argument;
    }
    return MessageFormat.format(pattern, updatedArguments);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resolve
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * See {@link #resolve(JavaInfo, NodeTarget, String)}.
   */
  public static String resolve(StatementTarget target, String text) throws Exception {
    return resolve(new NodeTarget(target), text);
  }

  /**
   * See {@link #resolve(JavaInfo, NodeTarget, String)}.
   */
  public static String resolve(BodyDeclarationTarget target, String text) throws Exception {
    return resolve(new NodeTarget(target), text);
  }

  /**
   * Replaces "delayed" {@link JavaInfo} references in form <code>"__wbpId:id"</code> with real
   * references, good in given {@link NodeTarget}.
   *
   * @return the text with resolved {@link JavaInfo} references.
   */
  public static String resolve(NodeTarget target, String text) throws Exception {
    while (true) {
      int jIndex = text.indexOf(ID_PREFIX);
      if (jIndex == -1) {
        break;
      }
      // prepare ID range
      int idBegin = jIndex + ID_PREFIX.length();
      int idEnd = idBegin;
      while (idEnd < text.length() && Character.isDigit(text.charAt(idEnd))) {
        idEnd++;
      }
      // prepare ID
      String id = text.substring(idBegin, idEnd);
      // check for ".", i.e. for access expression
      boolean askAccess = false;
      if (idEnd < text.length() && text.charAt(idEnd) == '.') {
        askAccess = true;
        idEnd++;
      }
      // prepare JavaInfo expression
      String expr;
      {
        JavaInfo javaInfo = (JavaInfo) ObjectInfoUtils.getById(id);
        VariableSupport variableSupport = javaInfo.getVariableSupport();
        if (askAccess) {
          expr = variableSupport.getAccessExpression(target);
        } else {
          expr = variableSupport.getReferenceExpression(target);
        }
      }
      // replace ID with expression
      text = text.substring(0, jIndex) + expr + text.substring(idEnd);
    }
    return text;
  }

  /**
   * @return the {@link List} with all resolved {@link String}'s.
   */
  public static List<String> resolve(NodeTarget target, List<String> lines) throws Exception {
    List<String> resolvedLines = Lists.newArrayList();
    for (String line : lines) {
      line = resolve(target, line);
      resolvedLines.add(line);
    }
    return resolvedLines;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Format + resolve
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * See {@link #resolve(NodeTarget, String, Object...)}.
   */
  public static String resolve(StatementTarget target, String pattern, Object... arguments)
      throws Exception {
    return resolve(new NodeTarget(target), pattern, arguments);
  }

  /**
   * See {@link #resolve(NodeTarget, String, Object...)}.
   */
  public static String resolve(BodyDeclarationTarget target, String pattern, Object... arguments)
      throws Exception {
    return resolve(new NodeTarget(target), pattern, arguments);
  }

  /**
   * Performs {@link #format(String, Object...)} and
   * {@link #resolve(JavaInfo, BodyDeclarationTarget, String)}, expects that one of the arguments is
   * {@link JavaInfo}.
   */
  public static String resolve(NodeTarget target, String pattern, Object... arguments)
      throws Exception {
    String text = format(pattern, arguments);
    return resolve(target, text);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ASTNode operations for source with templates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link Statement} with template source.
   */
  public static Statement addStatement(JavaInfo javaInfo, StatementTarget target, List<String> lines)
      throws Exception {
    List<String> resolvedLines = resolve(new NodeTarget(target), lines);
    Statement statement = javaInfo.getEditor().addStatement(resolvedLines, target);
    javaInfo.addRelatedNodes(statement);
    return statement;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // OLD templates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * See {@link #evaluate(String, JavaInfo, Map)}.
   */
  public static String evaluate(String source, JavaInfo javaInfo) throws Exception {
    return evaluate(source, javaInfo, null);
  }

  /**
   * Evaluates source {@link String}, with <code>"${variable}"</code> variables and
   * <code>"${expression}"</code> expressions.
   * <p>
   * Typical expression is <code>"${parent.firstChild[java.awt.LayoutManager].expression}"</code>.
   *
   * @param source
   *          the source {@link String} to evaluate.
   * @param javaInfo
   *          the starting {@link JavaInfo}, relative to which expressions should be evaluated.
   * @param templateVariables
   *          the {@link Map} to variable names into values, may be <code>null</code>.
   *
   * @return the source with replaced template variables/expressions.
   */
  public static String evaluate(String source,
      JavaInfo javaInfo,
      Map<String, String> templateVariables) throws Exception {
    // replace template variables
    if (templateVariables != null) {
      source = StrSubstitutor.replace(source, templateVariables);
    }
    // replace template expressions
    while (true) {
      // extract single template expression
      String expression = StringUtils.substringBetween(source, "${", "}");
      if (expression == null) {
        break;
      }
      // replace template expression
      try {
        String result = evaluateExpression(expression, javaInfo);
        source = StringUtils.replace(source, "${" + expression + "}", result);
      } catch (Throwable e) {
        String message = String.format("Exception during evaluation |%s|.", source);
        throw new IllegalArgumentException(message, e);
      }
    }
    return source;
  }

  /**
   * Evaluates single {@link JavaInfo} expression.
   */
  private static String evaluateExpression(String expression, JavaInfo javaInfo) throws Exception {
    String originalExpression = expression;
    expressionLoop : while (expression.length() != 0) {
      // remove leading "."
      if (expression.startsWith(".")) {
        expression = expression.substring(1);
        continue;
      }
      // analyze simple expression part
      if (expression.startsWith("parent")) {
        javaInfo = javaInfo.getParentJava();
        expression = expression.substring("parent".length());
      } else if (expression.startsWith("firstChild[")) {
        // prepare selector
        String selectorClassName;
        {
          int openBracketIndex = expression.indexOf("[");
          int closeBracketIndex = expression.indexOf("]");
          selectorClassName = expression.substring(openBracketIndex + 1, closeBracketIndex);
          expression = expression.substring(closeBracketIndex + "]".length());
        }
        // select child by component class
        for (JavaInfo child : javaInfo.getChildrenJava()) {
          Class<?> childComponentClass = child.getDescription().getComponentClass();
          if (ReflectionUtils.isSuccessorOf(childComponentClass, selectorClassName)) {
            javaInfo = child;
            continue expressionLoop;
          }
        }
        // no such child found
        throw new IllegalArgumentException(String.format(
            "Can not find |%s| in |%s|.",
            selectorClassName,
            javaInfo));
      } else if (expression.startsWith("expression")) {
        return getExpression(javaInfo);
      } else {
        throw new IllegalArgumentException(String.format("Incorrect expression |%s|.", expression));
      }
    }
    // should not happen
    throw new IllegalArgumentException(String.format(
        "Expression have not evaluated |%s|.",
        originalExpression));
  }
}
