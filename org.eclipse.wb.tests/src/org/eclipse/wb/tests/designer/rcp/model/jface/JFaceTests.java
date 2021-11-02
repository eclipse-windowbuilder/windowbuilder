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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for RCP JFace models.
 *
 * @author scheglov_ke
 */
public class JFaceTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.jface");
    suite.addTest(createSingleSuite(TableViewerTest.class));
    suite.addTest(createSingleSuite(TableViewerColumnTest.class));
    suite.addTest(createSingleSuite(TreeViewerColumnTest.class));
    suite.addTest(createSingleSuite(ComboViewerTest.class));
    suite.addTest(createSingleSuite(AbstractColumnLayoutTest.class));
    suite.addTest(createSingleSuite(WindowTopBoundsSupportTest.class));
    suite.addTest(createSingleSuite(DialogTest.class));
    suite.addTest(createSingleSuite(TitleAreaDialogTest.class));
    suite.addTest(createSingleSuite(PopupDialogTest.class));
    suite.addTest(createSingleSuite(DialogPageTest.class));
    suite.addTest(createSingleSuite(ApplicationWindowTest.class));
    suite.addTest(createSingleSuite(ApplicationWindowGefTest.class));
    suite.addTest(createSingleSuite(ActionTest.class));
    suite.addTest(createSingleSuite(MenuManagerTest.class));
    suite.addTest(createSingleSuite(MenuManagerGefTest.class));
    suite.addTest(createSingleSuite(CoolBarManagerTest.class));
    suite.addTest(createSingleSuite(WizardPageTest.class));
    suite.addTest(createSingleSuite(WizardTest.class));
    suite.addTest(createSingleSuite(PreferencePageTest.class));
    suite.addTest(createSingleSuite(FieldEditorPreferencePageTest.class));
    suite.addTest(createSingleSuite(FieldEditorLabelsConstantsPropertyEditorTest.class));
    suite.addTest(createSingleSuite(DoubleFieldEditorEntryInfoTest.class));
    suite.addTest(createSingleSuite(FieldLayoutPreferencePageTest.class));
    suite.addTest(createSingleSuite(ControlDecorationTest.class));
    suite.addTest(createSingleSuite(FieldEditorPreferencePageGefTest.class));
    suite.addTest(createSingleSuite(CellEditorTest.class));
    suite.addTest(createSingleSuite(NoJFaceInClasspathTest.class));
    suite.addTest(createSingleSuite(GridLayoutFactoryTest.class));
    return suite;
  }
}