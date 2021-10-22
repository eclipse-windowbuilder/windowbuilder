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
package org.eclipse.wb.tests.designer.XWT.model.forms;

import org.eclipse.wb.tests.designer.XWT.model.forms.table.TableWrapLayoutTests;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Forms API support.
 *
 * @author scheglov_ke
 */
public class FormsTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model.forms");
    suite.addTest(createSingleSuite(FormsTest.class));
    suite.addTest(createSingleSuite(FormTest.class));
    suite.addTest(createSingleSuite(FormGefTest.class));
    suite.addTest(createSingleSuite(ScrolledFormTest.class));
    suite.addTest(createSingleSuite(ExpandableCompositeTest.class));
    suite.addTest(createSingleSuite(ExpandableCompositeGefTest.class));
    suite.addTest(createSingleSuite(ColumnLayoutTest.class));
    suite.addTest(TableWrapLayoutTests.suite());
    return suite;
  }
}
