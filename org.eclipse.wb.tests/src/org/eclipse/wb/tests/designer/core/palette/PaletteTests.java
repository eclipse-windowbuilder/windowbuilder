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
package org.eclipse.wb.tests.designer.core.palette;

import org.eclipse.wb.core.editor.palette.model.PaletteInfo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Tests for {@link PaletteInfo}.
 *
 * @author scheglov_ke
 */
@RunWith(Suite.class)
@SuiteClasses({
		AttributesProvidersTest.class,
		AbstractElementInfoTest.class,
		PaletteInfoTest.class,
		CategoryInfoTest.class,
		SelectionToolEntryInfoTest.class,
		MarqueeSelectionToolEntryInfoTest.class,
		TabOrderToolEntryInfoTest.class,
		ChooseComponentEntryInfoTest.class,
		ToolEntryInfoTest.class,
		ComponentEntryInfoTest.class,
		StaticFactoryEntryInfoTest.class,
		InstanceFactoryEntryInfoTest.class,
		PaletteManagerTest.class,
		CategoryCommandsTest.class,
		ComponentCommandsTest.class,
		FactoryCommandsTest.class
})
public class PaletteTests {
}
