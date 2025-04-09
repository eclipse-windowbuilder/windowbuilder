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
package org.eclipse.wb.internal.core.model.generation.statement;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Implementation of {@link StatementGenerator} that adds {@link Statement}'s directly inside of
 * parent {@link Block} without any enclosing {@link Block}'s or comments.
 *
 * @author lobas_av
 * @coverage core.model.generation
 */
public final class PureFlatStatementGenerator extends AbstractInsideStatementGenerator {
	public static final PureFlatStatementGenerator INSTANCE = new PureFlatStatementGenerator();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private PureFlatStatementGenerator() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StatementGenerator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void add(JavaInfo child, StatementTarget target, Association association) throws Exception {
		add(child, target, null, association);
	}
}