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

import org.eclipse.wb.internal.core.xml.editor.palette.model.IPaletteSite;

/**
 * Tests for {@link IPaletteSite}.
 *
 * @author scheglov_ke
 */
public class IPaletteSiteTest extends AbstractPaletteTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Empty() throws Exception {
    IPaletteSite site = new IPaletteSite.Empty();
    assertSame(null, site.getPalette());
    assertSame(null, site.getShell());
    // no-op
    site.editPalette();
    site.addCommand(null);
  }
}
