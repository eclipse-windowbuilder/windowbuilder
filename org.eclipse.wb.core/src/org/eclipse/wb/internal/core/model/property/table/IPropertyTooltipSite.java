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
package org.eclipse.wb.internal.core.model.property.table;

/**
 * Interface that allows control of {@link PropertyTooltipProvider} interact with
 * {@link PropertyTableTooltipHelper}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.table
 */
public interface IPropertyTooltipSite {
	/**
	 * @return the {@link PropertyTable} of this site.
	 */
	PropertyTable getTable();

	/**
	 * Hides current tooltip.
	 */
	void hideTooltip();
}
