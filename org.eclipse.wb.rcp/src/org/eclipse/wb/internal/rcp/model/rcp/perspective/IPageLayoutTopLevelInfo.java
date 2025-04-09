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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import org.eclipse.ui.IPageLayout;

/**
 * Interface for some top-level element of {@link IPageLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public interface IPageLayoutTopLevelInfo {
	/**
	 * @return the ID of this {@link IPageLayoutTopLevelInfo}.
	 */
	String getId();

	/**
	 * @return the source for ID of this {@link IPageLayoutTopLevelInfo}, it should be evaluated into
	 *         same value as {@link #getId()}.
	 */
	String getIdSource();
}
