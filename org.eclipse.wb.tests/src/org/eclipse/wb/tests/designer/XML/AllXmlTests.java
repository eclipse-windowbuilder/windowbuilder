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
package org.eclipse.wb.tests.designer.XML;

import org.eclipse.wb.tests.designer.XWT.XwtTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All XML tests.
 * 
 * @author scheglov_ke
 */
public class AllXmlTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("All XML");
    suite.addTest(XmlTests.suite());
    suite.addTest(XwtTests.suite());
    return suite;
  }
}
