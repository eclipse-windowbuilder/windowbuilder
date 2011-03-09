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

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.swing.model.component.menu.MenuTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Swing components models.
 * 
 * @author scheglov_ke
 */
public class ComponentTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.swing.model.component");
    suite.addTest(createSingleSuite(ComponentTest.class));
    suite.addTest(createSingleSuite(ContainerTest.class));
    suite.addTest(createSingleSuite(SwingLiveManagerTest.class));
    suite.addTest(createSingleSuite(AbstractButtonTest.class));
    suite.addTest(createSingleSuite(JSplitPaneTest.class));
    suite.addTest(createSingleSuite(JScrollPaneTest.class));
    suite.addTest(createSingleSuite(JTabbedPaneTest.class));
    suite.addTest(createSingleSuite(JTabbedPaneGefTest.class));
    suite.addTest(createSingleSuite(JToolBarTest.class));
    suite.addTest(createSingleSuite(JLayeredPaneTest.class));
    suite.addTest(createSingleSuite(JListTest.class));
    suite.addTest(createSingleSuite(JComboBoxTest.class));
    suite.addTest(createSingleSuite(JTableTest.class));
    suite.addTest(createSingleSuite(JTreeTest.class));
    suite.addTest(createSingleSuite(JSliderTest.class));
    suite.addTest(createSingleSuite(JSpinnerTest.class));
    suite.addTest(createSingleSuite(JLabelTest.class));
    suite.addTest(createSingleSuite(JTextFieldTest.class));
    suite.addTest(createSingleSuite(JDialogTest.class));
    suite.addTest(createSingleSuite(JInternalFrameTest.class));
    suite.addTest(createSingleSuite(AppletTest.class));
    suite.addTest(createSingleSuite(SomeComponentsTest.class));
    suite.addTest(createSingleSuite(BeanInfoTest.class));
    suite.addTest(MenuTests.suite());
    return suite;
  }
}
