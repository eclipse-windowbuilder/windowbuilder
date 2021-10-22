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
package org.eclipse.wb.tests.designer.rcp.model;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.rcp.model.e4.E4Tests;
import org.eclipse.wb.tests.designer.rcp.model.forms.FormsTests;
import org.eclipse.wb.tests.designer.rcp.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.rcp.model.layout.LayoutTests;
import org.eclipse.wb.tests.designer.rcp.model.property.PropertyTests;
import org.eclipse.wb.tests.designer.rcp.model.rcp.TheRcpTests;
import org.eclipse.wb.tests.designer.rcp.model.util.UtilTests;
import org.eclipse.wb.tests.designer.rcp.model.widgets.WidgetTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * RCP model tests.
 *
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model");
    suite.addTest(WidgetTests.suite());
    suite.addTest(UtilTests.suite());
    suite.addTest(PropertyTests.suite());
    suite.addTest(LayoutTests.suite());
    suite.addTest(JFaceTests.suite());
    suite.addTest(FormsTests.suite());
    suite.addTest(TheRcpTests.suite());
    suite.addTest(E4Tests.suite());
    return suite;
  }
}