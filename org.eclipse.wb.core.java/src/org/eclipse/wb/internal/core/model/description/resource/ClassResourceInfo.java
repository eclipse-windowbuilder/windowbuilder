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
package org.eclipse.wb.internal.core.model.description.resource;

/**
 * {@link ResourceInfo} for some component {@link Class}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ClassResourceInfo {
	public final Class<?> clazz;
	public final ResourceInfo resource;

	public ClassResourceInfo(Class<?> clazz, ResourceInfo resource) {
		this.clazz = clazz;
		this.resource = resource;
	}
}