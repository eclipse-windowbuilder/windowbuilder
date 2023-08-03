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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		MigColumnTest.class,
		MigRowTest.class,
		MigLayoutConstraintsTest.class,
		MigLayoutConstraintsPropertiesTest.class,
		MigLayoutTest.class,
		MigLayoutAutoAlignmentTest.class,
		MigLayoutConverterTest.class,
		MigLayoutSurroundSupportTest.class,
		MigLayoutSelectionActionsTest.class,
		MigLayoutGefTest.class
})
public class MigLayoutTests {
}
