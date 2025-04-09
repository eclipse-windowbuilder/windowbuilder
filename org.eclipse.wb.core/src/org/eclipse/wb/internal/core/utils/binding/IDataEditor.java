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
package org.eclipse.wb.internal.core.utils.binding;

/**
 * This interface describe editor for abstract data.
 *
 * @author lobas_av
 */
public interface IDataEditor {
	/**
	 * Returns current editor value.
	 */
	Object getValue();

	/**
	 * Sets new editor value.
	 */
	void setValue(Object value);
}