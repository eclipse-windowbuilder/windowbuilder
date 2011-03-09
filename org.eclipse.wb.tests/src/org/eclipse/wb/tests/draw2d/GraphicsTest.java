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

import org.eclipse.wb.draw2d.Graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

/**
 * @author lobas_av
 * 
 */
public class GraphicsTest extends Draw2dFigureTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GraphicsTest() {
    super(Graphics.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_initState() throws Exception {
    GC gc = new GC(Display.getDefault());
    Graphics graphics = new Graphics(gc);
    //
    Color background = graphics.getBackgroundColor();
    assertNotNull(background);
    assertFalse(background.isDisposed());
    //
    Color foreground = graphics.getForegroundColor();
    assertNotNull(foreground);
    assertFalse(foreground.isDisposed());
    //
    Font font = graphics.getFont();
    assertNotNull(font);
    assertFalse(font.isDisposed());
    //
    assertNotNull(graphics.getFontMetrics());
    assertNotNull(graphics.getClip());
    //
    assertEquals(1, graphics.getLineWidth());
    assertEquals(SWT.LINE_SOLID, graphics.getLineStyle());
  }
}