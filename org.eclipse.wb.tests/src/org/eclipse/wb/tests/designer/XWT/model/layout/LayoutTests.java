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
package org.eclipse.wb.tests.designer.XWT.model.layout;

import org.eclipse.wb.internal.xwt.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.XWT.model.layout.grid.GridLayoutTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for XWT {@link LayoutInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		LayoutTest.class,
		AbsoluteLayoutTest.class,
		AbsoluteLayoutGefTest.class,
		RowLayoutTest.class,
		RowLayoutGefTest.class,
		FillLayoutTest.class,
		FillLayoutGefTest.class,
		StackLayoutTest.class,
		StackLayoutGefTest.class,
		FormLayoutTest.class,
		FormLayoutGefTest.class,
		GridLayoutTests.class
})
public class LayoutTests {
}