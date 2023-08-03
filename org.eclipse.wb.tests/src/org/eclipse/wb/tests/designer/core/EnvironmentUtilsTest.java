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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.Test;

/**
 * Test for {@link EnvironmentUtils}.
 *
 * @author scheglov_ke
 */
public class EnvironmentUtilsTest extends DesignerTestCase {
	/**
	 * Test for known state of host and development flag.
	 */
	@Test
	public void test_DEVELOPER_HOST() throws Exception {
		if ("SCHEGLOV-KE".equals(EnvironmentUtils.HOST_NAME)) {
			assertTrue(EnvironmentUtils.DEVELOPER_HOST);
		}
	}

	/**
	 * Test for {@link EnvironmentUtils#getJavaVersion()}.
	 */
	@Test
	public void test_getJavaVersion() throws Exception {
		{
			// specify version 1.8
			EnvironmentUtils.setForcedJavaVersion(1.8f);
			try {
				assertEquals(1.8, EnvironmentUtils.getJavaVersion(), 0.001);
			} finally {
				EnvironmentUtils.setForcedJavaVersion(null);
			}
		}
	}

	/**
	 * Test for {@link EnvironmentUtils#isTestingTime()}.
	 */
	@Test
	public void test_isDevelopmentTime() throws Exception {
		assertTrue(EnvironmentUtils.isTestingTime());
		{
			// switch development time
			EnvironmentUtils.setTestingTime(false);
			try {
				assertFalse(EnvironmentUtils.isTestingTime());
			} finally {
				EnvironmentUtils.setTestingTime(true);
			}
		}
		assertTrue(EnvironmentUtils.isTestingTime());
	}
}
