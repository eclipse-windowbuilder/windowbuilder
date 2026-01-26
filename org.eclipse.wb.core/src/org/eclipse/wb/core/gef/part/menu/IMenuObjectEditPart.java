/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.core.gef.part.menu;

import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;

import org.eclipse.gef.EditPart;

/**
 * Interface of {@link EditPart} for any {@link IMenuObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public interface IMenuObjectEditPart {
	/**
	 * @return the {@link IMenuObjectInfo} of this model.
	 */
	IMenuObjectInfo getMenuModel();
}
