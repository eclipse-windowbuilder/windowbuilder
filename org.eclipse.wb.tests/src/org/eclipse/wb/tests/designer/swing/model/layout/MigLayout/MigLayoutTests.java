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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests {@link MigLayoutInfo}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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
