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
package org.eclipse.wb.tests.designer.swt;

import org.eclipse.wb.tests.designer.swt.model.ModelTests;
import org.eclipse.wb.tests.designer.swt.support.CoordinateUtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * All SWT tests.
 *
 * @author sablin_aa
 */

@RunWith(Suite.class)
@SuiteClasses({
	ManagerUtilsTest.class,
	ModelTests.class,
	CoordinateUtilsTest.class,

})
public class SwtTests {
}
