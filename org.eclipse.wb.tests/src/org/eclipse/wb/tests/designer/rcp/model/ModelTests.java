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
package org.eclipse.wb.tests.designer.rcp.model;

import org.eclipse.wb.tests.designer.rcp.model.e4.E4Tests;
import org.eclipse.wb.tests.designer.rcp.model.forms.FormsTests;
import org.eclipse.wb.tests.designer.rcp.model.jface.JFaceTests;
import org.eclipse.wb.tests.designer.rcp.model.layout.LayoutTests;
import org.eclipse.wb.tests.designer.rcp.model.rcp.TheRcpTests;
import org.eclipse.wb.tests.designer.rcp.model.util.UtilTests;
import org.eclipse.wb.tests.designer.rcp.model.widgets.WidgetTests;
import org.eclipse.wb.tests.designer.rcp.resource.ResourceTests;

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
    ResourceTests.class,
    LayoutTests.class,
    JFaceTests.class,
    FormsTests.class, 
    TheRcpTests.class, 
    E4Tests.class,

})
public class ModelTests {
}