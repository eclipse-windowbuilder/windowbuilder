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
package org.eclipse.wb.internal.core.utils.jdt.core;

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.IMethodBinding;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Reader;
import java.util.List;

/**
 * Helper for accessing JavaDoc for Java elements.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt
 */
public class JavaDocUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaDocUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tooltip
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the JavaDoc for method with given {@link IMethodBinding} or <code>null</code> if no
   *         source found.
   */
  public static String getTooltip(IJavaProject project, IMethodBinding methodBinding)
      throws Exception {
    String declaringTypeName =
        AstNodeUtils.getFullyQualifiedName(methodBinding.getDeclaringClass(), false);
    String signature = AstNodeUtils.getMethodSignature(methodBinding);
    return getTooltip(project, declaringTypeName, signature);
  }

  /**
   * @return the JavaDoc for given method signature or <code>null</code> if no source found.
   */
  public static String getTooltip(IJavaProject project, String declaringTypeName, String signature)
      throws Exception {
    IMethod method = CodeUtils.findMethod(project, declaringTypeName, signature);
    return getTooltip(method);
  }

  /**
   * @return the tooltip for given {@link IMethod} or <code>null</code> if no source found.
   */
  public static String getTooltip(IMember method) throws Exception {
    List<String> javaDocLines = getJavaDocLines(method, true);
    if (javaDocLines == null) {
      return null;
    }
    // read tooltip
    String tooltip = StringUtils.join(javaDocLines.iterator(), " ");
    tooltip = StringUtilities.normalizeWhitespaces(tooltip);
    // remove other meta data
    tooltip = StringUtils.replace(tooltip, "{@inheritDoc}", "");
    tooltip = StringUtils.substringBefore(tooltip, "@param");
    tooltip = StringUtils.substringBefore(tooltip, "@since");
    tooltip = StringUtils.substringBefore(tooltip, "@see");
    tooltip = StringUtils.substringBefore(tooltip, "@author");
    tooltip = getTooltip_useShortTypeNames(tooltip);
    tooltip = tooltip.trim();
    // done
    return tooltip;
  }

  private static String getTooltip_useShortTypeNames(String tooltip) {
    int index = 0;
    while (true) {
      index = tooltip.indexOf("{@link", index);
      if (index == -1) {
        break;
      }
      int endIndex = tooltip.indexOf("}", index);
      if (endIndex == -1) {
        break;
      }
      index += "{@link".length();
      String link = tooltip.substring(index, endIndex).trim();
      {
        int lastDot = -1;
        for (int i = 0; i < link.length(); i++) {
          char c = link.charAt(i);
          // '.'
          if (c == '.') {
            lastDot = i;
            continue;
          }
          // part of identifier
          if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z') {
            continue;
          }
          // end of qualified type
          break;
        }
        if (lastDot != -1) {
          link = link.substring(lastDot + 1);
        }
      }
      tooltip = tooltip.substring(0, index) + " " + link + tooltip.substring(endIndex);
      index = endIndex;
    }
    return tooltip;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaDoc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the JavaDoc for given {@link IMember} or <code>null</code> if no source found.
   */
  public static List<String> getJavaDocLines(IMember member, boolean allowInherited)
      throws Exception {
    // prepare reader
    Reader reader = JavadocContentAccess.getContentReader(member, allowInherited);
    if (reader == null) {
      return null;
    }
    // read JavaDoc
    return IOUtils.readLines(reader);
  }
}
