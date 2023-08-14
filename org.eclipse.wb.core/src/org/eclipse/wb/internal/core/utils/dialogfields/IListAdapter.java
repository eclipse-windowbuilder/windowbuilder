/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
