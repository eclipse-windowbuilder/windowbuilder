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
package org.eclipse.wb.tests.designer.rcp.model;

import org.eclipse.wb.tests.designer.rcp.model.e4.E4Tests;
import org.eclipse.wb.tests.designer.rcp.model.forms.FormsTests;
import org.eclipse.wb.tests.designer.rcp.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.rcp.model.layout.LayoutTests;
import org.eclipse.wb.tests.designer.rcp.model.property.PropertyTests;
import org.eclipse.wb.tests.designer.rcp.model.rcp.TheRcpTests;
import org.eclipse.wb.tests.designer.rcp.model.util.UtilTests;
import org.eclipse.wb.tests.designer.rcp.model.widgets.WidgetTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * RCP model tests.
 *
 * @author scheglov_ke
 */

@RunWith(Suite.class)
@SuiteClasses({
		WidgetTests.class,
		UtilTests.class,
		LayoutTests.class,
		JFaceTests.class,
		FormsTests.class,
		TheRcpTests.class,
		E4Tests.class,
		PropertyTests.class

})
public class ModelTests {
}