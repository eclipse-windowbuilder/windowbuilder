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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ICreationSupportPermissions;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ICreationSupportPermissions}.
 *
 * @author scheglov_ke
 */
public class ICreationSupportPermissionsTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ICreationSupportPermissions#FALSE}.
	 */
	@Test
	public void test_FALSE() throws Exception {
		JavaInfo javaInfo = null;
		ICreationSupportPermissions permissions = ICreationSupportPermissions.FALSE;
		assertFalse(permissions.canDelete(javaInfo));
		try {
			permissions.delete(javaInfo);
			fail();
		} catch (NotImplementedException e) {
		}
		assertFalse(permissions.canReorder(javaInfo));
		assertFalse(permissions.canReparent(javaInfo));
	}
}
