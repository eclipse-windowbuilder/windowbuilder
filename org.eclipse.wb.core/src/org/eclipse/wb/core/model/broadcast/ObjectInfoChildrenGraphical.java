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

import java.util.List;

/**
 * Listener for {@link ObjectInfo} events.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public interface ObjectInfoChildrenGraphical {
	/**
	 * This method is invoked from {@link DefaultObjectPresentation#getChildrenGraphical()} to allow
	 * processing all prepared children. Subscribers may, for example, reorder children.
	 *
	 * @param children
	 *          the {@link ObjectInfo} children to process.
	 */
	void invoke(List<ObjectInfo> children) throws Exception;
}
