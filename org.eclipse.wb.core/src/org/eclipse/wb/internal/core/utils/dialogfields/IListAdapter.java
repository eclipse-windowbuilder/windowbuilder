/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.dialogfields;

/**
 * Change listener used by <code>ListDialogField</code> and <code>CheckedListDialogField</code>
 */
public interface IListAdapter<T> {
	/**
	 * A button from the button bar has been pressed.
	 */
	void customButtonPressed(ListDialogField<T> field, int index);

	/**
	 * The selection of the list has changed.
	 */
	void selectionChanged(ListDialogField<T> field);

	/**
	 * An entry in the list has been double clicked.
	 */
	void doubleClicked(ListDialogField<T> field);
}
