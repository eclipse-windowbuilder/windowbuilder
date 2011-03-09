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
package org.eclipse.wb.tests.designer.XML.palette;

import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.tests.designer.XML.palette.ui.PaletteUiTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link PaletteInfo}.
 * 
 * @author scheglov_ke
 */
public class PaletteTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xml.palette");
    suite.addTest(createSingleSuite(IPaletteSiteTest.class));
    suite.addTest(createSingleSuite(AttributesProvidersTest.class));
    suite.addTest(createSingleSuite(AbstractElementInfoTest.class));
    suite.addTest(createSingleSuite(EntryInfoTest.class));
    suite.addTest(createSingleSuite(ToolEntryInfoTest.class));
    suite.addTest(createSingleSuite(PaletteInfoTest.class));
    suite.addTest(createSingleSuite(CategoryInfoTest.class));
    suite.addTest(createSingleSuite(SelectionToolEntryInfoTest.class));
    suite.addTest(createSingleSuite(MarqueeSelectionToolEntryInfoTest.class));
    suite.addTest(createSingleSuite(ChooseComponentEntryInfoTest.class));
    suite.addTest(createSingleSuite(ComponentEntryInfoTest.class));
    suite.addTest(createSingleSuite(PaletteManagerTest.class));
    suite.addTest(createSingleSuite(CategoryCommandsTest.class));
    suite.addTest(createSingleSuite(ComponentCommandsTest.class));
    suite.addTest(PaletteUiTests.suite());
    return suite;
  }
}
