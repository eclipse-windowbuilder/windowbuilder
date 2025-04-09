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

import org.eclipse.wb.core.editor.palette.model.entry.MarqueeSelectionToolEntryInfo;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.Test;

/**
 * Test for {@link MarqueeSelectionToolEntryInfo}.
 *
 * @author scheglov_ke
 */
public class MarqueeSelectionToolEntryInfoTest extends DesignerTestCase {
	@Test
	public void test() throws Exception {
		MarqueeSelectionToolEntryInfo entry = new MarqueeSelectionToolEntryInfo();
		assertEquals("Marquee", entry.getName());
		assertNotNull(entry.getIcon());
		assertInstanceOf(MarqueeSelectionTool.class, entry.createTool());
	}
}
