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
package org.eclipse.wb.internal.core.model.property.editor;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.apache.commons.lang.StringUtils;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

import java.util.Map;

/**
 * The {@link PropertyEditor} for selecting single custom expression from given set.
 *
 * @author sablin_aa
 * @coverage core.model.property.editor
 */
public final class ExpressionListPropertyEditor extends AbstractListPropertyEditor {
  protected String[] m_expressions;
  protected String[] m_conditions;
  protected String[] m_titles;
  protected String m_functions;
  protected Object[] m_compiled;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access to list items
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected int getCount() {
    return m_compiled.length;
  }

  @Override
  protected int getValueIndex(Object value) {
    for (int i = 0; i < getCount(); i++) {
      Map<String, Object> variables = Maps.newTreeMap();
      variables.put("value", value);
      setVariables(variables, i);
      if ((Boolean) MVEL.executeExpression(m_compiled[i], variables)) {
        return i;
      }
    }
    return -1;
  }

  protected void setVariables(Map<String, Object> variables, int conditionIndex) {
  }

  @Override
  protected String getTitle(int index) {
    return m_titles[index];
  }

  @Override
  protected String getExpression(int index) throws Exception {
    return m_expressions[index];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IConfigurablePropertyObject
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditorState state, Map<String, Object> parameters) throws Exception {
    // initialize parameters
    initializeParameters(parameters);
    // prepare parameters
    prepareParameters(state);
    // sanity check
    Assert.isTrue(
        m_expressions.length == m_conditions.length && m_expressions.length == m_titles.length,
        "Count of expressions/conditions/titles should be same in %s",
        parameters);
    // compile conditions
    compileConditions();
  }

  protected static final String USER_functions = "functions";

  private void initializeParameters(Map<String, Object> parameters) {
    m_expressions = getParameterAsArray(parameters, "expressions");
    m_conditions = getParameterAsArray(parameters, "conditions");
    m_titles = getParameterAsArray(parameters, "titles", true);
    // init functions
    if (parameters.containsKey(USER_functions)) {
      Object functions = parameters.get(USER_functions);
      if (functions instanceof String) {
        m_functions = (String) functions;
      }
    } else {
      m_functions = "";
    }
  }

  private void prepareParameters(EditorState state) throws ClassNotFoundException {
    // if no titles specified, then copy them from expressions
    if (m_titles == null || m_titles.length < 1) {
      m_titles = new String[m_expressions.length];
      for (int i = 0; i < m_expressions.length; i++) {
        m_titles[i] = m_expressions[i];
      }
    }
  }

  /**
   * Compiling conditions using MVEL library
   */
  private static final String DEF_functions = StringUtils.join(new String[]{
      "def isType(t, c) {",
      "  if (c is String) {",
      "    return ReflectionUtils.isSuccessorOf(t, c);",
      "  } else {",
      "    return c.isAssignableFrom(v);",
      "  }",
      "};",
      "def isValueType(c) {",
      "  if (value == null) {",
      "    return false;",
      "  } else {",
      "    return isType(value.getClass(),c);",
      "  }",
      "};",}, "\n");

  private void compileConditions() {
    ParserContext parseContext = getParseContext();
    m_compiled = new Object[m_conditions.length];
    for (int i = 0; i < m_conditions.length; i++) {
      String expression = DEF_functions + m_functions + m_conditions[i];
      m_compiled[i] = MVEL.compileExpression(expression, parseContext);
    }
  }

  private static ParserContext getParseContext() {
    ParserContext context = new ParserContext();
    context.addImport("ReflectionUtils", ReflectionUtils.class);
    return context;
  }
}