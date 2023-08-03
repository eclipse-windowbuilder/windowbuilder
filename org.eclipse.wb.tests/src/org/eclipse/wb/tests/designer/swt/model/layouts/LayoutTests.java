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
package org.eclipse.wb.tests.designer.swt.model.layouts;

import org.eclipse.wb.tests.designer.swt.model.layouts.grid.GridLayoutTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for SWT layouts models.
 *
 * @author lobas_av
 */
@RunWith(Suite.class)
@SuiteClasses({
		LayoutTest.class,
		LayoutDataTest.class,
		VirtualLayoutDataTest.class,
		ImplicitLayoutDataTest.class,
		AbsoluteLayoutTest.class,
		AbsoluteLayoutSelectionActionsTest.class,
		AbsoluteLayoutOrderingTest.class,
		FillLayoutTest.class,
		RowLayoutTest.class,
		LayoutLayoutDataCompatibilityTest.class,
		GridLayoutTests.class
})
public class LayoutTests {
}