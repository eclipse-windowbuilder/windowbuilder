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
