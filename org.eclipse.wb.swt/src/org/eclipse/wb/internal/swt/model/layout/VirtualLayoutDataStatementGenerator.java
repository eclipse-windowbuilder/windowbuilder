/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.model.generation.statement.AbstractInsideStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;

import java.util.List;

/**
 * Implementation of {@link StatementGenerator} for virtual {@link LayoutDataInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class VirtualLayoutDataStatementGenerator extends AbstractInsideStatementGenerator {
	public static final StatementGenerator INSTANCE = new VirtualLayoutDataStatementGenerator();

	////////////////////////////////////////////////////////////////////////////
	//
	// StatementGenerator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void add(JavaInfo child, StatementTarget target, Association association) throws Exception {
		// prepare block
		Block block = (Block) child.getEditor().addStatement(List.of("{", "}"), target);
		// add statements in block
		target = new StatementTarget(block, true);
		add(child, target, null, association);
	}
}
