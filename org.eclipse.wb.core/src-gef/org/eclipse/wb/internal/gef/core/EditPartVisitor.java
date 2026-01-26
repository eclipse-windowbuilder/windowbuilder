/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.gef.EditPart;

/**
 * Visitor for visiting {@link EditPart} hierarchy.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class EditPartVisitor {
	/**
	 * This method is invoked to check if given {@link EditPart} and its children should be visited.
	 */
	public boolean visit(EditPart editPart) {
		return true;
	}

	/**
	 * This method is invoked when all children of given {@link EditPart} were visited.
	 */
	public void endVisit(EditPart editPart) {
	}
}