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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model;

/**
 * @author lobas_av
 * @coverage core.ui
 */
public interface IHasChildren {
	/**
	 * @return <code>true</code> if container contains elements and <code>false</code> otherwise.
	 */
	boolean hasChildren();
}