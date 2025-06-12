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

import org.eclipse.wb.core.editor.palette.model.entry.SelectionToolEntryInfo;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link SelectionToolEntryInfo}.
 *
 * @author scheglov_ke
 */
public class SelectionToolEntryInfoTest extends DesignerTestCase {
	@Test
	public void test() throws Exception {
		SelectionToolEntryInfo entry = new SelectionToolEntryInfo();
		assertEquals("Selection", entry.getName());
		assertNotNull(entry.getIcon());
		assertInstanceOf(SelectionTool.class, entry.createTool());
	}
}
