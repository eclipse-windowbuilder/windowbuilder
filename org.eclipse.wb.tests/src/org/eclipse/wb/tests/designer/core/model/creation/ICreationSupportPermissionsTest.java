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
package org.eclipse.wb.tests.designer.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ICreationSupportPermissions;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang.NotImplementedException;

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
