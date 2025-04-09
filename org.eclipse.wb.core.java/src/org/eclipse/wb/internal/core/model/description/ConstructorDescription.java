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
package org.eclipse.wb.internal.core.model.description;

/**
 * Description for single constructor of {@link ComponentDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ConstructorDescription extends AbstractInvocationDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ConstructorDescription(Class<?> declaringClass) {
		super(declaringClass);
		setName("<init>");
	}
}