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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.IDisposable;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

/**
 * Common image container.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public abstract class ImageContainer implements IImageContainer, IDisposable {
	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void dispose() {
		IImageElement[] elements = directElements();
		if (elements != null) {
			for (int i = 0; i < elements.length; i++) {
				IImageElement element = elements[i];
				if (element instanceof IDisposable disposable) {
					disposable.dispose();
				}
			}
		}
	}

	/**
	 * @return the children {@link IImageElement}'s without preparing.
	 */
	protected abstract IImageElement[] directElements();

	/**
	 * @return array with path to <code>imagePath</code> relative to given plugin if resource exist
	 *         otherwise <code>null</code>.
	 */
	public abstract Object[] findResource(String symbolicName, String imagePath);
}