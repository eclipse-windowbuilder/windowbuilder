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
package org.eclipse.wb.internal.core.model.generation.statement.flat;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Implementation of {@link StatementGenerator} that adds {@link Statement}'s directly in target
 * {@link Block}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class FlatStatementGenerator extends AbstractInsideStatementGenerator {
  public static final FlatStatementGenerator INSTANCE = new FlatStatementGenerator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private FlatStatementGenerator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BASE = "statement.flat.";
  public static final String P_USE_PREFIX = BASE + "usePrefix";
  public static final String P_PREFIX_TEXT = BASE + "prefixText";

  ////////////////////////////////////////////////////////////////////////////
  //
  // StatementGenerator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo child, StatementTarget target, Association association) throws Exception {
    // prepare comments
    String[] leadingComments = null;
    {
      IPreferenceStore preferences = getPreferences(child);
      if (preferences.getBoolean(P_USE_PREFIX)) {
        leadingComments = new String[]{preferences.getString(P_PREFIX_TEXT)};
      }
    }
    // add
    add(child, target, leadingComments, association);
  }
}
