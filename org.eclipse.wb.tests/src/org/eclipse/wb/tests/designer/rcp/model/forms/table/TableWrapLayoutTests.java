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
package org.eclipse.wb.tests.designer.rcp.model.forms.table;

import org.eclipse.wb.internal.rcp.model.forms.layout.table.TableWrapLayoutInfo;
import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for {@link TableWrapLayoutInfo}.
 *
 * @author scheglov_ke
 */
public class TableWrapLayoutTests extends DesignerSuiteTests {
  public static Test suite() {
    TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.forms.table");
    suite.addTest(createSingleSuite(TableWrapDataTest.class));
    suite.addTest(createSingleSuite(TableWrapLayoutSelectionActionsTest.class));
    suite.addTest(createSingleSuite(TableWrapLayoutTest.class));
    suite.addTest(createSingleSuite(TabelWrapLayoutParametersTest.class));
    suite.addTest(createSingleSuite(TableWrapLayoutExposedTest.class));
    suite.addTest(createSingleSuite(TableWrapLayoutClipboardTest.class));
    suite.addTest(createSingleSuite(TableWrapLayoutGefTest.class));
    return suite;
  }
}