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
package org.eclipse.wb.tests.designer.swing.model.layout.FormLayout;

import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests {@link FormLayoutInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		FormSizeInfoTest.class,
		FormDimensionInfoTest.class,
		FormLayoutTest.class,
		CellConstraintsSupportTest.class,
		FormLayoutGroupsTest.class,
		FormColumnInfoTest.class,
		FormRowInfoTest.class,
		FormLayoutParametersTest.class,
		FormLayoutConverterTest.class,
		FormLayoutSelectionActionsTest.class,
		FormLayoutGefTest.class,
		DefaultComponentFactoryTest.class
})
public class FormLayoutTests {
}
