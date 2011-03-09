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
package org.eclipse.wb.tests.designer.ercp.model.widgets;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.ercp.model.widgets.mobile.CaptionedControlTest;
import org.eclipse.wb.tests.designer.ercp.model.widgets.mobile.CommandTest;
import org.eclipse.wb.tests.designer.ercp.model.widgets.mobile.ListBoxTest;
import org.eclipse.wb.tests.designer.ercp.model.widgets.mobile.SortedListTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for eRCP widgets models.
 * 
 * @author lobas_av
 */
public class WidgetTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.ercp.model.widgets");
    suite.addTest(createSingleSuite(ControlTest.class));
    suite.addTest(createSingleSuite(SortedListTest.class));
    suite.addTest(createSingleSuite(CommandTest.class));
    suite.addTest(createSingleSuite(CaptionedControlTest.class));
    suite.addTest(createSingleSuite(ListBoxTest.class));
    return suite;
  }
}