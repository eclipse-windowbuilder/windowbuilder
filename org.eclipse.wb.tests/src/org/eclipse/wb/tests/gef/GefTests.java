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
package org.eclipse.wb.tests.gef;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author lobas_av
 *
 */

@Suite
@SelectClasses({
		RequestsTest.class,
		CommandsTest.class,
		EditPartTest.class,
		EditPolicyTest.class,
		ResizeTrackerTest.class,
		SelectAndDragEditPartTrackerTest.class,
		MarqueeSelectionToolTest.class,
		CreationToolTest.class,
		PasteToolTest.class,
		SelectionToolTest.class,
		GraphicalViewerTest.class,
		TreeCreateToolTest.class,
		TreeDragToolTest.class,
		CursorTests.class
})
public class GefTests {
}