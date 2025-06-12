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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.tests.designer.swing.model.component.menu.MenuTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for Swing components models.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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
