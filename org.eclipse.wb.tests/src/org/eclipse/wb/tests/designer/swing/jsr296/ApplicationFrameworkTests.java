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
package org.eclipse.wb.tests.designer.swing.jsr296;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test for "Swing Application Framework", JSR-296.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		ActivatorTest.class,
		LoadResourcesTest.class,
		FrameViewTest.class,
		FrameViewGefTest.class
})
public class ApplicationFrameworkTests {
}
