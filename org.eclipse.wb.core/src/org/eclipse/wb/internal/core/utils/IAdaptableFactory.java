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
package org.eclipse.wb.internal.core.utils;

/**
 * External implementation of {@link IAdaptable}.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public interface IAdaptableFactory {
	/**
	 * @param object
	 *          the {@link Object} to adapt.
	 * @param adapter
	 *          the type of adapter.
	 *
	 * @return the adapter of required type.
	 */
	<T> T getAdapter(Object object, Class<T> adapter);
}
