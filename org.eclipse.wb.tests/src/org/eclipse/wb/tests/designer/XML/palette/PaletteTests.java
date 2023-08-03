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
package org.eclipse.wb.tests.designer.XML.palette;

import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.tests.designer.XML.palette.ui.PaletteUiTests;

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
		IPaletteSiteTest.class,
		AttributesProvidersTest.class,
		AbstractElementInfoTest.class,
		EntryInfoTest.class,
		ToolEntryInfoTest.class,
		PaletteInfoTest.class,
		CategoryInfoTest.class,
		SelectionToolEntryInfoTest.class,
		MarqueeSelectionToolEntryInfoTest.class,
		ChooseComponentEntryInfoTest.class,
		ComponentEntryInfoTest.class,
		PaletteManagerTest.class,
		CategoryCommandsTest.class,
		ComponentCommandsTest.class,
		PaletteUiTests.class
})
public class PaletteTests {
}
