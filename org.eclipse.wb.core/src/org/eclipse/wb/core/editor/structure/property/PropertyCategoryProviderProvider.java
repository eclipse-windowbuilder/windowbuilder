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
package org.eclipse.wb.core.editor.structure.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategoryProvider;

import java.util.List;

/**
 * Provider for {@link PropertyCategoryProvider}.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public interface PropertyCategoryProviderProvider {
	/**
	 * @return the {@link PropertyCategoryProvider} for properties of given {@link ObjectInfo}-s, may
	 *         be <code>null</code> so default provider will be used.
	 */
	PropertyCategoryProvider get(List<ObjectInfo> objects);
}
