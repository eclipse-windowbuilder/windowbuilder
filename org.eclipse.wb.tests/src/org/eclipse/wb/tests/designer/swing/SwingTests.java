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
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.tests.designer.swing.ams.AmsTests;
import org.eclipse.wb.tests.designer.swing.jsr296.ApplicationFrameworkTests;
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
    ApplicationFrameworkTests.class,
//  WaitForMemoryProfilerTest.class,
})
public class SwingTests {
}
