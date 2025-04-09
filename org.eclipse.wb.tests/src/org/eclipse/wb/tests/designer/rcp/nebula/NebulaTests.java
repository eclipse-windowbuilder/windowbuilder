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
package org.eclipse.wb.tests.designer.rcp.nebula;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for Nebula widgets models.
 *
 * @author sablin_aa
 */
@RunWith(Suite.class)
@SuiteClasses({
	CollapsibleButtonsTest.class,
	GalleryTest.class,
	GanttChartTest.class,
	GridTest.class,
	PShelfTest.class,
})
public class NebulaTests {

}