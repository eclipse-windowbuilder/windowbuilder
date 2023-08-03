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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.tests.designer.swing.model.component.menu.MenuTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for Swing components models.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ComponentTest.class,
		ContainerTest.class,
		SwingLiveManagerTest.class,
		AbstractButtonTest.class,
		JSplitPaneTest.class,
		JScrollPaneTest.class,
		JTabbedPaneTest.class,
		JTabbedPaneGefTest.class,
		JToolBarTest.class,
		JLayeredPaneTest.class,
		JListTest.class,
		JComboBoxTest.class,
		JTableTest.class,
		JTreeTest.class,
		JSliderTest.class,
		JSpinnerTest.class,
		JLabelTest.class,
		JTextFieldTest.class,
		JDialogTest.class,
		JInternalFrameTest.class,
		AppletTest.class,
		SomeComponentsTest.class,
		BeanInfoTest.class,
		MenuTests.class
})
public class ComponentTests {
}
