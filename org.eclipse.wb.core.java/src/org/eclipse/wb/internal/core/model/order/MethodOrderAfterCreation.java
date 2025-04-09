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
package org.eclipse.wb.internal.core.model.order;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.Pair;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Target {@link MethodInvocation}'s at place where instance of component become accessible (in
 * simple case - where component created and assigned to variable).
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderAfterCreation extends MethodOrder {
	////////////////////////////////////////////////////////////////////////////
	//
	// MethodOrder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canReference(JavaInfo javaInfo) {
		return true;
	}

	@Override
	protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
			throws Exception {
		// check for "first"
		{
			Statement lastFirstStatement = getLastFirstStatement(javaInfo);
			if (lastFirstStatement != null) {
				return new StatementTarget(lastFirstStatement, false);
			}
		}
		// OK, use specific
		return javaInfo.getVariableSupport().getStatementTarget();
	}

	private static Statement getLastFirstStatement(JavaInfo javaInfo) {
		Statement lastFirstStatement = null;
		for (Pair<MethodInvocation, MethodOrder> pair : getInvocationOrders(javaInfo)) {
			MethodInvocation existingInvocation = pair.getLeft();
			MethodOrder existingOrder = pair.getRight();
			if (existingOrder instanceof MethodOrderFirst) {
				lastFirstStatement = AstNodeUtils.getEnclosingStatement(existingInvocation);
			}
		}
		return lastFirstStatement;
	}
}
