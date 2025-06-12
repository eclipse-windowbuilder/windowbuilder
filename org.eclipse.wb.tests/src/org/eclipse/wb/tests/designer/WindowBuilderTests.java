/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.tests.designer;

import org.eclipse.wb.tests.designer.core.CoreTests;
import org.eclipse.wb.tests.designer.editor.EditorTests;
import org.eclipse.wb.tests.designer.example.SwingExampleTest;
import org.eclipse.wb.tests.designer.rcp.RcpTests;
import org.eclipse.wb.tests.designer.swing.SwingTests;
import org.eclipse.wb.tests.designer.swt.SwtTests;
import org.eclipse.wb.tests.draw2d.Draw2dTests;
import org.eclipse.wb.tests.gef.GefTests;
import org.eclipse.wb.tests.utils.CommonTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * All WindowBuilder tests.
 *
 * @author scheglov_ke
 */

@Suite
@SelectClasses({
	Draw2dTests.class, //
	GefTests.class, //
	SwtTests.class,
	CommonTests.class,
	CoreTests.class,
	EditorTests.class,
	SwingTests.class,
	RcpTests.class,
	SwingExampleTest.class
})

public class WindowBuilderTests {
}
