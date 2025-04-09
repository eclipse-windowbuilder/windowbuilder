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
 * This interface describe storage for abstract data.
 *
 * @author lobas_av
 */
public interface IDataProvider {
	/**
	 * @return the current or default value.
	 */
	Object getValue(boolean def);

	/**
	 * Sets current value.
	 */
	void setValue(Object value);
}