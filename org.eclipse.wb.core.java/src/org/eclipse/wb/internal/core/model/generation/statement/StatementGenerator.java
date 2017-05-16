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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * {@link StatementGenerator} lays out given statements according its rules. For example in
 * "flat mode" statements should be added directly in given position, in block mode - they should be
 * wrapped in block and added only then as single block, in "lazy creation mode" - wrapped in
 * method.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public abstract class StatementGenerator {
  /**
   * Adds given statements according rules of this {@link StatementGenerator}.
   *
   * @param child
   *          the child {@link JavaInfo} that will be added to the given parent
   * @param target
   *          the target for association statement
   * @param associationStatementSource
   *          the source of statement for parent/child association
   * @param variableStatementSource
   *          the source of variable statement (can be <code>null</code>)
   */
  public abstract void add(JavaInfo child, StatementTarget target, Association association)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IPreferenceStore} for given {@link JavaInfo}.
   */
  protected final IPreferenceStore getPreferences(JavaInfo javaInfo) {
    return javaInfo.getDescription().getToolkit().getPreferences();
  }
}
