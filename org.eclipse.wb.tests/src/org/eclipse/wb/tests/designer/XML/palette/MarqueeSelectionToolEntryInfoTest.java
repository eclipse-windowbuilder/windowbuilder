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

import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.internal.core.xml.editor.palette.model.MarqueeSelectionToolEntryInfo;
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
