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
package org.eclipse.wb.tests.designer.XML.palette.ui;

import org.eclipse.wb.internal.core.xml.editor.palette.DesignerPalette;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for palette UI.
 *
 * @author scheglov_ke
 */
public class AbstractPaletteUiTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    System.clearProperty(DesignerPalette.FLAG_NO_PALETTE);
  }

  @Override
  protected void tearDown() throws Exception {
    if (m_paletteManager != null) {
      m_paletteManager.commands_clear();
      m_paletteManager.commands_write();
    }
    super.tearDown();
  }
}
