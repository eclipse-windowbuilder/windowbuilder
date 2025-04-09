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
 * Listener for {@link ObjectInfo} delete events.
 *
 * @author mitin_aa
 * @coverage core.model
 */
public abstract class ObjectInfoDelete {
	/**
	 * Before {@link ObjectInfo} deleted from its parent.
	 */
	public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
	}

	/**
	 * After {@link ObjectInfo} deleted from its parent.
	 */
	public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
	}
}
