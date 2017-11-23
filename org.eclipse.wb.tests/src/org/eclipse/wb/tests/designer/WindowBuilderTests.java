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
package org.eclipse.wb.tests.designer;

import org.eclipse.wb.tests.designer.XML.XmlTests;
import org.eclipse.wb.tests.designer.XWT.XwtTests;
import org.eclipse.wb.tests.designer.core.CoreTests;
import org.eclipse.wb.tests.designer.editor.EditorTests;
import org.eclipse.wb.tests.designer.rcp.RcpTests;
import org.eclipse.wb.tests.designer.swing.SwingTests;
import org.eclipse.wb.tests.designer.swt.SwtTests;
import org.eclipse.wb.tests.utils.CommonTests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * All WindowBuilder tests.
 *
 * @author scheglov_ke
 */
public class WindowBuilderTests extends TestCase {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb");
    suite.addTest(CommonTests.suite());
    suite.addTest(CoreTests.suite());
    suite.addTest(SwingTests.suite());
    suite.addTest(SwtTests.suite());
    suite.addTest(RcpTests.suite());
    suite.addTest(XmlTests.suite());
    suite.addTest(XwtTests.suite());
    suite.addTest(EditorTests.suite());
    //suite.addTestSuite(WaitForMemoryProfilerTest.class);
    return suite;
  }
}
