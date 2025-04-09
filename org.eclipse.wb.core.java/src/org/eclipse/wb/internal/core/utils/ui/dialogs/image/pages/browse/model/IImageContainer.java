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
 * Container for children {@link IImageElement}'s.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public interface IImageContainer extends IImageElement {
	/**
	 * @return the children {@link IImageElement}'s.
	 */
	IImageElement[] elements();
}
