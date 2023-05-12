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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Test for {@link TopBoundsSupport}.
 *
 * @author scheglov_ke
 */
public class TopBoundsSupportTest extends AbstractCoreTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_0() throws Exception {
    AbstractComponentInfo shell = parse("<Shell/>");
    shell.refresh();
    // initial size
    {
      Rectangle bounds = shell.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // set new size
    TopBoundsSupport topBoundsSupport = shell.getTopBoundsSupport();
    topBoundsSupport.setSize(500, 400);
    shell.refresh();
    {
      Rectangle bounds = shell.getBounds();
      assertEquals(500, bounds.width);
      assertEquals(400, bounds.height);
    }
  }
}