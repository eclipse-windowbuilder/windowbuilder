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
package org.eclipse.wb.internal.core.model.property;

/**
 * Interface for {@link Property} which has type.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public interface ITypedProperty {
	/**
	 * @return the type of this property, may be <code>null</code> if type is unknown.
	 */
	Class<?> getType();
}
