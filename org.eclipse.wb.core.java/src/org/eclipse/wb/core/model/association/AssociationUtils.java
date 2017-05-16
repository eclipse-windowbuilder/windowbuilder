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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Various utilities for {@link Association}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class AssociationUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AssociationUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the new parent reference in {@link MethodInvocation} or {@link ClassInstanceCreation}
   * arguments.
   */
  public static void updateParentAssociation(AbstractInvocationDescription description,
      List<Expression> arguments,
      JavaInfo newParent) throws Exception {
    for (ParameterDescription parameter : description.getParameters()) {
      if (parameter.isParent()) {
        Expression argument = arguments.get(parameter.getIndex());
        String replacement = TemplateUtils.getExpression(newParent);
        newParent.replaceExpression(argument, replacement);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Templates
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the source with replaced templates. Following templates are supported:
   *
   * <ol>
   * <li>%parent% - replaced with
   * <code>parent.getVariableSupport().getReferenceExpression(target).</code></li>
   * <li>%child% - replaced with
   * <code>child.getVariableSupport().getReferenceExpression(target).</code></li>
   * <li>%index% - replaced with next child number in parent.</li>
   * </ol>
   */
  public static String replaceTemplates(JavaInfo child, String source, StatementTarget target)
      throws Exception {
    return replaceTemplates(child, source, new NodeTarget(target));
  }

  /**
   * Returns the source with replaced templates. Following templates are supported:
   *
   * <ol>
   * <li>%parent% - replaced with
   * <code>parent.getVariableSupport().getReferenceExpression(target).</code></li>
   * <li>%child% - replaced with
   * <code>child.getVariableSupport().getReferenceExpression(target).</code></li>
   * <li>%index% - replaced with next child number in parent.</li>
   * </ol>
   */
  public static String replaceTemplates(JavaInfo child, String source, NodeTarget target)
      throws Exception {
    // send broadcast
    {
      String[] sourceArray = new String[]{source};
      child.getBroadcastJava().associationTemplate(child, sourceArray);
      source = sourceArray[0];
    }
    // replace parent expressions
    {
      if (source.contains("%parent%")) {
        source =
            StringUtils.replace(
                source,
                "%parent%",
                TemplateUtils.getExpression(child.getParentJava()));
      }
    }
    // replace child expressions
    if (source.contains("%child%")) {
      source = StringUtils.replace(source, "%child%", TemplateUtils.getExpression(child));
    }
    // replace index expressions
    if (source.contains("%index%")) {
      ObjectInfo parentInfo = child.getParent();
      if (parentInfo != null) {
        int index = parentInfo.getChildren(JavaInfo.class).size();
        source = StringUtils.replace(source, "%index%", Integer.toString(index));
      }
    }
    // replace other templates
    Map<String, String> templateArguments = child.getTemplateArguments();
    if (templateArguments != null) {
      for (Entry<String, String> template : templateArguments.entrySet()) {
        source = StringUtils.replace(source, "%" + template.getKey() + "%", template.getValue());
      }
    }
    // OK, final result
    source = TemplateUtils.resolve(target, source);
    return source;
  }
}
