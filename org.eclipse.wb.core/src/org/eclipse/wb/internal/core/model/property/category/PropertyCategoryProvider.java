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
package org.eclipse.wb.internal.core.model.property.category;

import org.eclipse.wb.internal.core.model.property.Property;

/**
 * This interface is used to get {@link PropertyCategory} for {@link Property}.
 *
 * @author scheglov_ke
 * @coverage core.model.property
 */
public interface PropertyCategoryProvider {
	/**
	 * @return the {@link PropertyCategory} of given Property, not <code>null</code>.
	 */
	PropertyCategory getCategory(Property property);
}
