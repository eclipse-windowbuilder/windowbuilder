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
package org.eclipse.wb.tests.designer;

import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Fake test case that just wait long time, so that we have time to get memory
 * snapshot.
 *
 * @author scheglov_ke
 */
@Disabled
public class WaitForMemoryProfilerTest extends DesignerTestCase {
	@Test
	public void test_waitForProfiler() throws InterruptedException {
		EditorState.setActiveJavaInfo(null);
		GlobalState.setActiveObject(null);
		// wait long time...
		System.out.println("**** Profiler ****, take snapshot!");
		waitEventLoop(1000 * 1000 * 1000, 100);
	}
}
