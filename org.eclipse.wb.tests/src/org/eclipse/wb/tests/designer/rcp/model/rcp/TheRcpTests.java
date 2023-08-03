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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.tests.designer.WaitForMemoryProfilerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for RCP models.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		PropertyPageTest.class,
		PdeUtilsTest.class,
		ExtensionElementPropertyTest.class,
		ViewPartTest.class,
		ViewPartGefTest.class,
		ViewCategoryPropertyEditorTest.class,
		EditorPartTest.class,
		AbstractSplashHandlerTest.class,
		MultiPageEditorPartTest.class,
		PageTest.class,
		PageLayoutTest.class,
		PageLayoutGefTest.class,
		ActionBarAdvisorTest.class,
		ActionFactoryTest.class,
		FilteredItemsSelectionDialogTest.class,
		RcpWizardsTest.class,
		WaitForMemoryProfilerTest.class
})
public class TheRcpTests {
}