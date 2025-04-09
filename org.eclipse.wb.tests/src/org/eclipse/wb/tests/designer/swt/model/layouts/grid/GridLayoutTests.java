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
package org.eclipse.wb.tests.designer.swt.model.layouts.grid;

import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		GridDataTest.class,
		GridLayoutSelectionActionsTest.class,
		GridLayoutTest.class,
		GridLayoutParametersTest.class,
		GridLayoutConverterTest.class,
		GridLayoutExposedTest.class,
		GridLayoutGefTest.class
})
public class GridLayoutTests {
}