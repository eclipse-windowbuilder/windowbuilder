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
