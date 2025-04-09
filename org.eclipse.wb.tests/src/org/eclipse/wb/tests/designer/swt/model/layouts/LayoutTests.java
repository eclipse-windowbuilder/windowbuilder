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