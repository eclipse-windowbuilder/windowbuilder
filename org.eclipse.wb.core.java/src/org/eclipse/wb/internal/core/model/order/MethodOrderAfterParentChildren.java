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
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link MethodOrder} to add {@link MethodInvocation} after parent children {@link JavaInfo}'s of
 * specified types.
 *
 * @author sablin_aa
 * @coverage core.model.description
 */
public final class MethodOrderAfterParentChildren extends MethodOrderChildren {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MethodOrderAfterParentChildren(String childrenTypeNames) {
		super(childrenTypeNames);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MethodOrder
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected StatementTarget getSpecificTarget(JavaInfo javaInfo, String newSignature)
			throws Exception {
		JavaInfoUtils.materializeVariable(javaInfo);
		// calculate 'relative' target
		JavaInfo parentJavaInfo = javaInfo.getParentJava();
		JavaInfo parentNextChild =
				GenericsUtils.getNextOrNull(parentJavaInfo.getChildrenJava(), getLastChild(parentJavaInfo));
		StatementTarget target = JavaInfoUtils.getTarget(parentJavaInfo, parentNextChild);
		// check target
		if (!JavaInfoUtils.isCreatedAtTarget(javaInfo, new NodeTarget(target))) {
			target = JavaInfoUtils.getTarget(javaInfo);
		}
		return target;
	}
}
