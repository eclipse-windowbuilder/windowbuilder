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
package org.eclipse.wb.tests.designer.rcp.model.layout;

import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.rcp.model.layout.form.FormLayoutTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for RCP {@link LayoutInfo}'s.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 */
public class LayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.layout");
    suite.addTest(createSingleSuite(ControlSelectionPropertyEditorTest.class));
    suite.addTest(FormLayoutTests.suite());
    suite.addTest(createSingleSuite(GridLayoutTest.class));
    suite.addTest(createSingleSuite(StackLayoutTest.class));
    suite.addTest(createSingleSuite(StackLayoutGefTest.class));
    suite.addTest(createSingleSuite(AbsoluteLayoutGefTest.class));
    return suite;
  }
}