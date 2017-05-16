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
package org.eclipse.wb.internal.core.model.generation.statement.block;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Implementation of {@link StatementGenerator} that adds {@link Statement}'s in new {@link Block}
 * inside of target {@link Block}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class BlockStatementGenerator extends AbstractInsideStatementGenerator {
  public static final BlockStatementGenerator INSTANCE = new BlockStatementGenerator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private BlockStatementGenerator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StatementGenerator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void add(JavaInfo child, StatementTarget target, Association association) throws Exception {
    // prepare block
    Block block = (Block) child.getEditor().addStatement(ImmutableList.of("{", "}"), target);
    // add statements in block
    target = new StatementTarget(block, true);
    add(child, target, null, association);
  }
}
