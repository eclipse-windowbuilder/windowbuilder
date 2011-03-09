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
package org.eclipse.wb.tests.designer.ercp.model;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.ercp.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.ercp.model.widgets.WidgetTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * eRCP model tests.
 * 
 * @author scheglov_ke
 */
public class ModelTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.ercp.model");
    suite.addTest(WidgetTests.suite());
    suite.addTest(JFaceTests.suite());
    suite.addTest(createSingleSuite(ClipboardTest.class));
    return suite;
  }
}