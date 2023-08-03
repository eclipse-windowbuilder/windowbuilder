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
package org.eclipse.wb.tests.designer.swing.model.layout;

import org.eclipse.wb.tests.designer.swing.model.layout.FormLayout.FormLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.MigLayout.MigLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.gbl.GridBagLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.gef.GefLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.group.GroupLayoutTests;
import org.eclipse.wb.tests.designer.swing.model.layout.model.BorderLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.BoxLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.CardLayoutGefTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.CardLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.FlowLayoutGefTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.FlowLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.model.GridLayoutTest;
import org.eclipse.wb.tests.designer.swing.model.layout.spring.SpringLayoutTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for Swing layouts.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		LayoutManagersTest.class,
		ImplicitLayoutTest.class,
		AbsoluteLayoutTest.class,
		AbsoluteLayoutSelectionActionsTest.class,
		AbsoluteLayoutGefTest.class,
		ConstraintsAbsoluteLayoutTest.class,
		BorderLayoutTest.class,
		FlowLayoutTest.class,
		FlowLayoutGefTest.class,
		GridLayoutTest.class,
		BoxLayoutTest.class,
		CardLayoutTest.class,
		CardLayoutGefTest.class,
		FormLayoutTests.class,
		MigLayoutTests.class,
		GridBagLayoutTests.class,
		SpringLayoutTests.class,
		GroupLayoutTests.class,
		GefLayoutTests.class,
		LayoutGefTest.class
})
public class LayoutTests {
}
