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
package org.eclipse.wb.internal.core.databinding.model;

import java.util.Map;

/**
 *
 * @author lobas_av
 *
 */
public interface ISynchronizeProcessor<T, V> {
	/**
	 * @return {@code true} if processor will work with given {@code object}.
	 */
	boolean handleObject(V object);

	/**
	 * @return key object for given {@code object}.
	 */
	T getKeyObject(V object);

	/**
	 * Check equal for given keys.
	 */
	boolean equals(T key0, T key1);

	/**
	 * XXX
	 */
	V findObject(Map<T, V> keyObjectToObject, T key) throws Exception;

	/**
	 * Create new object for given key.
	 */
	V createObject(T keyObject) throws Exception;

	/**
	 * Update old {@link BeanBindableInfo} object.
	 */
	void update(V object) throws Exception;
}