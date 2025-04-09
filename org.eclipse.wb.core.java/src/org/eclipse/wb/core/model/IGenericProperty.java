/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH, Aachen and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    DSA GmbH, Aachen - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.model;

import org.eclipse.wb.internal.core.model.property.GenericProperty;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Public interface of {@link GenericProperty} used by {@link IImageProcessor}.
 * The property is used to access the image that is attached to the
 * {@link JavaInfo}.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be subclassed by clients.
 */
public interface IGenericProperty {
	/**
	 * @return the {@link Expression} that was used for calculating value of this
	 *         {@link GenericProperty}.
	 */
	Expression getExpression();

	/**
	 * @return the {@link JavaInfo} of this {@link GenericProperty}.
	 */
	JavaInfo getJavaInfo();
}
