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
package org.eclipse.wb.tests.utils;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test for "org.eclipse.wb.core.temp" project.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
    StringUtilitiesTest.class,
    ProjectClassLoaderTest.class})

public class CommonTests {
}
