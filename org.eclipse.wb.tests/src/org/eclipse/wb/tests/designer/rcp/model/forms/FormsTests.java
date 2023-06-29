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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.tests.designer.core.DesignerSuiteTests;
import org.eclipse.wb.tests.designer.rcp.model.forms.table.TableWrapLayoutTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for "Forms API" widgets.
 *
 * @author scheglov_ke
 */
public class FormsTests extends DesignerSuiteTests {
	public static Test suite() {
		TestSuite suite = new TestSuite("org.eclipse.wb.rcp.model.forms");
		suite.addTest(createSingleSuite(FormToolkitTest.class));
		suite.addTest(createSingleSuite(FormTextTest.class));
		suite.addTest(createSingleSuite(ExpandableCompositeTest.class));
		suite.addTest(createSingleSuite(SectionTest.class));
		suite.addTest(createSingleSuite(FormTest.class));
		suite.addTest(createSingleSuite(ScrolledFormTest.class));
		suite.addTest(createSingleSuite(FormPageTest.class));
		suite.addTest(createSingleSuite(SectionPartTest.class));
		suite.addTest(createSingleSuite(FormToolkitAccessTest.class));
		suite.addTest(createSingleSuite(DetailsPageTest.class));
		suite.addTest(createSingleSuite(MasterDetailsBlockTest.class));
		suite.addTest(createSingleSuite(ColumnLayoutTest.class));
		suite.addTest(TableWrapLayoutTests.suite());
		return suite;
	}
}