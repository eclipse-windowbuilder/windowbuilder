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

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Tests for {@link PaletteInfo}.
 *
 * @author scheglov_ke
 */
@Suite
@SelectClasses({
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
