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
package org.eclipse.wb.internal.core.model.menu;

/**
 * Listener that can be notified that some {@link IMenuObjectInfo} was deleted.
 *
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public interface IMenuObjectListener {
	/**
	 * Notifies refresh should be performed.
	 */
	void refresh();

	/**
	 * Notifies that toolkit object is going to be deleted.
	 */
	void deleting(Object toolkitModel);
}
