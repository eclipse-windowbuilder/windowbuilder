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
package org.eclipse.wb.gef.graphical.policies;

/**
 * Listener for {@link SelectionEditPolicy}.
 *
 * @author scheglov_ke
 * @coverage gef.graphical
 */
public interface ISelectionEditPolicyListener {
	/**
	 * Notifies that {@link SelectionEditPolicy#showSelection()} was executed.
	 */
	void showSelection(SelectionEditPolicy policy);

	/**
	 * Notifies that {@link SelectionEditPolicy#hideSelection()} was executed.
	 */
	void hideSelection(SelectionEditPolicy policy);
}