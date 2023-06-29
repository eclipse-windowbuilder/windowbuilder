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
package org.eclipse.wb.tests.designer.XWT.model.widgets.menu;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import org.eclipse.swt.widgets.Menu;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link Menu}.
 *
 * @author scheglov_ke
 */
public class MenuTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model.widgets.menu");
		suite.addTest(createSingleSuite(MenuItemTest.class));
		suite.addTest(createSingleSuite(MenuTest.class));
		suite.addTest(createSingleSuite(MenuGefTest.class));
		return suite;
	}
}