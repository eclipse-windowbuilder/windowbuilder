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
package org.eclipse.wb.tests.designer;

import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Fake {@link TestCase} that just wait long time, so that we have time to get memory snapshot.
 *
 * @author scheglov_ke
 */
@Ignore
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
