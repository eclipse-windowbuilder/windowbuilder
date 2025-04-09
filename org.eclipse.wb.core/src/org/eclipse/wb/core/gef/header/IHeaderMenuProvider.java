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
package org.eclipse.wb.core.gef.header;

import org.eclipse.jface.action.IMenuManager;

/**
 * Provider for context menu of header.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public interface IHeaderMenuProvider {
	/**
	 * Adds menu items into given {@link IMenuManager}.
	 */
	void buildContextMenu(IMenuManager manager);
}
