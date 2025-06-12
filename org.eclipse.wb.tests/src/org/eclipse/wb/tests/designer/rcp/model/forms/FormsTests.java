/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.tests.designer.rcp.model.forms.table.TableWrapLayoutTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for "Forms API" widgets.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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