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
package org.eclipse.wb.core.model.broadcast;

import org.eclipse.wb.core.model.ObjectInfo;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoPresentationDecorateText {
	/**
	 * This method is used to support external decoration of object text.
	 *
	 * @param object
	 *          the {@link ObjectInfo} to decorate title.
	 * @param text
	 *          the array with single {@link String}, listener can replace it
	 */
	void invoke(ObjectInfo object, String[] text) throws Exception;
}
