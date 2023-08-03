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
package org.eclipse.wb.tests.gef;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author lobas_av
 *
 */

@RunWith(Suite.class)
@SuiteClasses({
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