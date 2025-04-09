/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model.layout.absolute;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Interface which provides the image by relative path. See the all of the our Activator instances.
 *
 * @author mitin_aa
 */
public interface IImageProvider {
	/**
	 * @return the Image instance by given path.
	 */
	ImageDescriptor getImageDescriptor(String path);
}
