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
package org.eclipse.wb.tests.designer.swt;

import org.eclipse.wb.tests.designer.swt.model.ModelTests;
import org.eclipse.wb.tests.designer.swt.support.SupportTests;

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
	SupportTests.class,

})
public class SwtTests {
}
