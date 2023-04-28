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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.draw2d.border.MarginBorder;

import org.eclipse.draw2d.geometry.Insets;

/**
 * @author lobas_av
 *
 */
public class MarginBorderTest extends Draw2dFigureTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarginBorderTest() {
    super(MarginBorder.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test's
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_constructor() throws Exception {
    // check work constructor (int, int, int, int)
    assertEquals(new Insets(1, 2, 3, 4), new MarginBorder(new Insets(1, 2, 3, 4)).getInsets());
    //
    // check work constructor (int)
    assertEquals(new Insets(7, 7, 7, 7), new MarginBorder(7).getInsets());
  }
}