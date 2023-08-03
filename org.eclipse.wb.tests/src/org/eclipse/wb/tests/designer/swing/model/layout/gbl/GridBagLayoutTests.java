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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link GridBagLayoutInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		GridBagLayoutTest.class,
		GridBagDimensionTest.class,
		GridBagColumnTest.class,
		GridBagRowTest.class,
		GridBagConstraintsTest.class,
		GridBagLayoutParametersTest.class,
		GridBagLayoutConverterTest.class,
		GridBagLayoutSelectionActionsTest.class,
		GridBagLayoutSurroundSupportTest.class,
		GridBagLayoutGefTest.class
})
public class GridBagLayoutTests {
}
