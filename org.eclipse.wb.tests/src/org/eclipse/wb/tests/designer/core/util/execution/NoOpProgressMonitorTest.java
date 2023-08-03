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
package org.eclipse.wb.tests.designer.core.util.execution;

import org.eclipse.wb.internal.core.utils.execution.NoOpProgressMonitor;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.Test;

/**
 * Test for {@link NoOpProgressMonitor}.
 *
 * @author scheglov_ke
 */
public class NoOpProgressMonitorTest extends DesignerTestCase {
	/**
	 * We just call all methods, implementation should just do nothing.
	 */
	@Test
	public void test_1() throws Exception {
		NoOpProgressMonitor monitor = new NoOpProgressMonitor();
		// task methods
		{
			monitor.beginTask(null, 0);
			monitor.done();
			monitor.setTaskName(null);
			monitor.subTask(null);
		}
		// canceled
		{
			// not canceled initially
			assertFalse(monitor.isCanceled());
			// mark as canceled
			monitor.setCanceled(true);
			assertTrue(monitor.isCanceled());
			// mark as not canceled
			monitor.setCanceled(false);
			assertFalse(monitor.isCanceled());
		}
		// worked
		{
			monitor.worked(0);
			monitor.internalWorked(0);
		}
	}
}
