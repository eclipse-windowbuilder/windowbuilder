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