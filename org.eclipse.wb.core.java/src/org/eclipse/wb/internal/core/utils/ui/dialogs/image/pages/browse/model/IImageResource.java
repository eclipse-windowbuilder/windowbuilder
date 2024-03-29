/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

/**
 * {@link IImageElement} for {@link ImageInfo}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public interface IImageResource extends IImageElement {
	/**
	 * @return the {@link ImageInfo} for this resource.
	 */
	ImageInfo getImageInfo();
}
