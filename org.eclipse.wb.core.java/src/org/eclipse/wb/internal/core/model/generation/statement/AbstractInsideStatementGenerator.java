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
package org.eclipse.wb.internal.core.model.generation.statement;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.AssociationUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Abstract implementation of {@link StatementGenerator} that adds {@link Statement}'s in given
 * {@link StatementTarget}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public abstract class AbstractInsideStatementGenerator extends StatementGenerator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds variable and association {@link Statement}'s into given {@link StatementTarget}.
   *
   * @param child
   *          the {@link JavaInfo} that should be added
   * @param target
   *          the target for statements
   * @param leadingComments
   *          the optional (can be <code>null</code>) array of lines that should be added before
   *          first statement
   * @param association
   *          the {@link Association} that asked for generating parent/child association in source
   *          code. For Swing/GWT there is usually some separate association code, in SWT parent is
   *          passed in constructor, so usually no additional association required.
   *
   * @return the {@link Statement} for given association source.
   */
  protected static void add(JavaInfo child,
      StatementTarget target,
      String[] leadingComments,
      Association association) throws Exception {
    AstEditor editor = child.getEditor();
    VariableSupport variableSupport = child.getVariableSupport();
    // add optional variable statement
    {
      String statementSource = variableSupport.add_getVariableStatementSource(target);
      if (statementSource != null) {
        // prepare lines
        List<String> lines;
        {
          statementSource = AssociationUtils.replaceTemplates(child, statementSource, target);
          lines = Lists.newArrayList();
          if (leadingComments != null) {
            Collections.addAll(lines, leadingComments);
          }
          Collections.addAll(lines, StringUtils.split(statementSource, '\n'));
          leadingComments = null;
        }
        // add statement
        Statement variableStatement = editor.addStatement(lines, target);
        variableSupport.add_setVariableStatement(variableStatement);
        addRelatedNodes(child, variableStatement);
        // modify target
        target = variableSupport.getAssociationTarget(target);
      }
    }
    // add association
    association.add(child, target, leadingComments);
  }

  /**
   * Adds related {@link ASTNode}'s in subtree of given {@link ASTNode}.
   */
  public static void addRelatedNodes(JavaInfo child, ASTNode statement) {
    child.addRelatedNodes(statement);
    child.getParentJava().addRelatedNodes(statement);
  }
}
