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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;

/**
 * Common interface for {@link ClasspathImageRoot} top level elements.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
interface IClasspathImageContainer extends IImageContainer {
	/**
	 * @return the children {@link IImageContainer}'s.
	 */
	@Override
	IImageContainer[] elements();

	/**
	 * @return <code>true</code> if this jar does not have any images.
	 */
	boolean isEmpty();

	/**
	 * Disposes any allocated resources.
	 */
	void dispose();
}
