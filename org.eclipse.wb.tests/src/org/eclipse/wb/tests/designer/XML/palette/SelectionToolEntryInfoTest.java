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

import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.internal.core.xml.editor.palette.model.SelectionToolEntryInfo;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Test for {@link SelectionToolEntryInfo}.
 *
 * @author scheglov_ke
 */
public class SelectionToolEntryInfoTest extends DesignerTestCase {
  public void test() throws Exception {
    SelectionToolEntryInfo entry = new SelectionToolEntryInfo();
    assertEquals("Selection", entry.getName());
    assertNotNull(entry.getIcon());
    assertInstanceOf(SelectionTool.class, entry.createTool());
  }
}
