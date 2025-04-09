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
package org.eclipse.wb.internal.swing.databinding.model.generic;

import java.util.List;

/**
 * Model for describe class generic information.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.generic
 */
public interface IGenericType {
	/**
	 * @return {@link Class} for this object.
	 */
	Class<?> getRawType();

	/**
	 * @return the class full name with all generic parameters.
	 */
	String getFullTypeName();

	/**
	 * @return the class name without the package name.
	 */
	String getSimpleTypeName();

	/**
	 * @return the list of {@link IGenericType} sub generic information models.
	 */
	List<IGenericType> getSubTypes();

	/**
	 * @return {@link IGenericType} sub generic information model for given index.
	 */
	IGenericType getSubType(int index);

	/**
	 * @return <code>true</code> if this model not contains sub generic information models.
	 */
	boolean isEmpty();
}