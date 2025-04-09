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
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoChildGraphical {
	/**
	 * This method is invoked from {@link DefaultObjectPresentation#getChildrenGraphical()} to check
	 * if given {@link ObjectInfo} can be displayed on design canvas.
	 *
	 * @param object
	 *          the {@link ObjectInfo} to check.
	 * @param visible
	 *          the array with single boolean flag, with initial <code>true</code> value, any can
	 *          listener set it to <code>false</code>.
	 */
	void invoke(ObjectInfo object, boolean[] visible) throws Exception;
}
