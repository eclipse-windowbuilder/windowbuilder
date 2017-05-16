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

import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Separate class for accessing code generation constants/options for {@link AstEditor}.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class AstCodeGeneration {
  private final AstEditor m_editor;
  private String endOfLine = null;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstCodeGeneration(AstEditor editor) {
    m_editor = editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Forward for {@link AstEditor#getModelUnit()}.
   */
  private ICompilationUnit getModelUnit() {
    return m_editor.getModelUnit();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation constants
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The default end-of-line marker for the current platform. This value should (almost) never be
   * used directly. The end-of-line marker should always be queried from the editor because it can
   * differ from the platform default in some situations. The only exception is if code is being
   * constructed for a new file, in which case there is no editor to ask.
   */
  public static final String DEFAULT_END_OF_LINE = System.getProperty("line.separator", "\n");

  /**
   * Return the string of characters that is to be used to indent code the given number of levels.
   *
   * @param levels
   *          the number of levels of indentation to be returned
   *
   * @return the string of characters that is to be used to indent code
   */
  public String getIndentation(int levels) {
    Assert.isTrue(levels >= 0);
    if (levels == 0) {
      return "";
    }
    // prepare indentation character and count of them for single level
    String indentationChar;
    int count;
    {
      String tabChar = getTabChar();
      if (tabChar != null && tabChar.toLowerCase().equals("space")) {
        indentationChar = " ";
        count = 4;
        try {
          String tabSize = getTabSize();
          count = Integer.parseInt(tabSize);
        } catch (Throwable e) {
        }
      } else {
        indentationChar = "\t";
        count = 1;
      }
    }
    // return result
    return StringUtils.repeat(indentationChar, count * levels);
  }

  /**
   * @return the end-of-line marker for this compilation unit.
   */
  public String getEndOfLine() throws Exception {
    if (endOfLine == null) {
      endOfLine = DEFAULT_END_OF_LINE;
      endOfLine = getEndOfLineForBuffer(m_editor.getBuffer());
    }
    return endOfLine;
  }

  /**
   * @return the separator between method name and opening brace of method body.
   */
  public String getMethodBraceSeparator(String prefix) throws Exception {
    if (getInsertEndOfLineBeforeOpeningBrace()) {
      return getEndOfLine() + prefix;
    } else {
      return " ";
    }
  }

  /**
   * @return the end-of-line marker for given {@link StringBuffer}.
   */
  private static String getEndOfLineForBuffer(Document document) throws Exception {
    // prepare set of existing EOL's
    Set<String> existingMarkers = Sets.newTreeSet();
    {
      int numberOfLines = document.getNumberOfLines();
      for (int i = 0; i < numberOfLines; i++) {
        String delimiter = document.getLineDelimiter(i);
        if (delimiter != null) {
          existingMarkers.add(delimiter);
        }
      }
    }
    // return default or first EOL
    if (existingMarkers.isEmpty() || existingMarkers.contains(DEFAULT_END_OF_LINE)) {
      return DEFAULT_END_OF_LINE;
    } else {
      return existingMarkers.iterator().next();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preference Accessing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return <code>true</code> if the user wants to have an end-of-line marker inserted before each
   * opening brace.
   *
   * @return <code>true</code> if an end-of-line marker should be inserted before each opening brace
   */
  public boolean getInsertEndOfLineBeforeOpeningBrace() {
    Map<String, String> javaOptions = getJavaOptions();
    String value =
        javaOptions.get(DefaultCodeFormatterConstants.FORMATTER_BRACE_POSITION_FOR_METHOD_DECLARATION);
    return value != null && value.equals(DefaultCodeFormatterConstants.NEXT_LINE);
  }

  /**
   * Return a string indicating the number of spaces the user wants to use to indent their code. The
   * value is either <code>"space"</code> or <code>"tab"</code>.
   *
   * @return a string indicating the number of spaces the user wants to use
   */
  public String getTabChar() {
    Map<String, String> javaOptions = getJavaOptions();
    return javaOptions.get(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
  }

  /**
   * Return a string indicating whether the user wants to use spaces or tabs to indent their code.
   * The value is a sequence of digits encoding a positive integer value.
   *
   * @return a string indicating whether the user wants to use spaces or tabs
   */
  public String getTabSize() {
    Map<String, String> javaOptions = getJavaOptions();
    return javaOptions.get(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
  }

  /**
   * Return <code>true</code> if the user wants to have assignments formatted without a space before
   * the assignment operator.
   *
   * @return <code>true</code> assignments should be formatted without a space before the assignment
   *         operator
   */
  public boolean getUseCompactAssignment() {
    Map<String, String> javaOptions = getJavaOptions();
    String value =
        javaOptions.get(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR);
    return value != null && !value.equals(JavaCore.INSERT);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Map} with {@link IJavaProject} options.
   */
  private Map<String, String> getJavaOptions() {
    return ProjectUtils.getOptions(getModelUnit().getJavaProject());
  }
}
