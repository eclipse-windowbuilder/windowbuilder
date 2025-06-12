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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.tests.designer.WaitForMemoryProfilerTest;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for RCP models.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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