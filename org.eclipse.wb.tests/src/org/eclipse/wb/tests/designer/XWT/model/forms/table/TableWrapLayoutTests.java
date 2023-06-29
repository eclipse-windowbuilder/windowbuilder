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
package org.eclipse.wb.tests.designer.XWT.model.forms.table;

import org.eclipse.wb.internal.xwt.model.forms.layout.table.TableWrapLayoutInfo;
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
		TestSuite suite = new TestSuite("org.eclipse.wb.xwt.model.forms.TableWrapLayout");
		suite.addTest(createSingleSuite(TableWrapDataTest.class));
		suite.addTest(createSingleSuite(TableWrapLayoutSelectionActionsTest.class));
		suite.addTest(createSingleSuite(TableWrapLayoutTest.class));
		suite.addTest(createSingleSuite(TabelWrapLayoutParametersTest.class));
		suite.addTest(createSingleSuite(TableWrapLayoutConverterTest.class));
		suite.addTest(createSingleSuite(TableWrapLayoutGefTest.class));
		return suite;
	}
}