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
package org.eclipse.wb.tests.designer.rcp.model.layout;

import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.tests.designer.rcp.model.layout.form.FormLayoutTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for RCP {@link LayoutInfo}'s.
 *
 * @author scheglov_ke
 * @author mitin_aa
 */
@RunWith(Suite.class)
@SuiteClasses({
		ControlSelectionPropertyEditorTest.class,
		FormLayoutTests.class,
		GridLayoutTest.class,
		StackLayoutTest.class,
		StackLayoutGefTest.class,
		AbsoluteLayoutGefTest.class
})
public class LayoutTests {
}