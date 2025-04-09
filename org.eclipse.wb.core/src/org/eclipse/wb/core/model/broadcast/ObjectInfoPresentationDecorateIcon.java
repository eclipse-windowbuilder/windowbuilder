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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoPresentationDecorateIcon {
	/**
	 * This method is used to support external decoration of "default" object icon.
	 *
	 * @param object
	 *          the {@link ObjectInfo} to decorate icon.
	 * @param icon
	 *          the array with single {@link ImageDescriptor}, listener can replace this image
	 */
	void invoke(ObjectInfo object, ImageDescriptor[] icon) throws Exception;
}
