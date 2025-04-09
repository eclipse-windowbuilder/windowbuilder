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
package org.eclipse.wb.internal.core.gefTree.policy.dblclick;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.util.RenameConvertSupport;

import java.util.Collections;

/**
 * An instance of {@link DoubleClickLayoutEditPolicy} which opens rename/convert dialog by
 * double-clicking in widgets tree.
 *
 * @author mitin_aa
 * @coverage core.gefTree.policy
 */
final class RenameJavaInfoLayoutEditPolicy extends DoubleClickLayoutEditPolicy {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public RenameJavaInfoLayoutEditPolicy(JavaInfo javaInfo) {
		super(javaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DoubleClickLayoutEditPolicy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void performDoubleClick() {
		RenameConvertSupport.rename(Collections.singletonList(m_javaInfo));
	}
}
