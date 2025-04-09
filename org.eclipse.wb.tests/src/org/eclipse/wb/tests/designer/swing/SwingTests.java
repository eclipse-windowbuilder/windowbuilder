/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.tests.designer.swing.ams.AmsTests;
import org.eclipse.wb.tests.designer.swing.laf.LookAndFeelTest;
import org.eclipse.wb.tests.designer.swing.model.ModelTests;
import org.eclipse.wb.tests.designer.swing.swingx.SwingXTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
	LookAndFeelTest.class,
	ConvertersTest.class,
	CustomizeTest.class,
	ModelTests.class,
	AmsTests.class,
	SwingXTests.class,
	//  WaitForMemoryProfilerTest.class,
})
public class SwingTests {
}
