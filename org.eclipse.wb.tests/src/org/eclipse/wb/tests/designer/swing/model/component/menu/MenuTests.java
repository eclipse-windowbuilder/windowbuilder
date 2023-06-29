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
package org.eclipse.wb.tests.designer.swing.model.component.menu;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Swing menu.
 *
 * @author scheglov_ke
 */
public class MenuTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.swing.model.component.menu");
		suite.addTest(createSingleSuite(JMenuBarTest.class));
		suite.addTest(createSingleSuite(JPopupMenuTest.class));
		suite.addTest(createSingleSuite(JMenuTest.class));
		suite.addTest(createSingleSuite(JMenuItemTest.class));
		suite.addTest(createSingleSuite(JMenuGefTest.class));
		return suite;
	}
}
