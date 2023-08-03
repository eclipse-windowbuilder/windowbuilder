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

import org.eclipse.wb.tests.designer.rcp.model.forms.table.TableWrapLayoutTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for "Forms API" widgets.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		FormToolkitTest.class,
		FormTextTest.class,
		ExpandableCompositeTest.class,
		SectionTest.class,
		FormTest.class,
		ScrolledFormTest.class,
		FormPageTest.class,
		SectionPartTest.class,
		FormToolkitAccessTest.class,
		DetailsPageTest.class,
		MasterDetailsBlockTest.class,
		ColumnLayoutTest.class,
		TableWrapLayoutTests.class
})
public class FormsTests {
}