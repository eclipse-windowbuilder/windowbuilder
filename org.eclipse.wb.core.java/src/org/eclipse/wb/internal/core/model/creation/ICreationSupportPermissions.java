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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;

import org.apache.commons.lang3.NotImplementedException;

/**
 * Provides for {@link CreationSupport} delete/reorder/reparent permissions.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public interface ICreationSupportPermissions {
	/**
	 * Implementation of {@link ICreationSupportPermissions} that does not allow any operation.
	 */
	ICreationSupportPermissions FALSE = new ICreationSupportPermissions() {
		@Override
		public boolean canDelete(JavaInfo javaInfo) {
			return false;
		}

		@Override
		public void delete(JavaInfo javaInfo) throws Exception {
			throw new NotImplementedException();
		}

		@Override
		public boolean canReorder(JavaInfo javaInfo) {
			return false;
		}

		@Override
		public boolean canReparent(JavaInfo javaInfo) {
			return false;
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	boolean canDelete(JavaInfo javaInfo);

	void delete(JavaInfo javaInfo) throws Exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Permissions
	//
	////////////////////////////////////////////////////////////////////////////
	boolean canReorder(JavaInfo javaInfo);

	boolean canReparent(JavaInfo javaInfo);
}
