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
package org.eclipse.wb.draw2d;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

/**
 * A collection of cursors.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface ICursorConstants {
  /**
   * System arrow cursor.
   */
  Cursor ARROW = new Cursor(null, SWT.CURSOR_ARROW);
  /**
   * System resize north cursor.
   */
  Cursor SIZEN = new Cursor(null, SWT.CURSOR_SIZEN);
  /**
   * System resize north-east cursor.
   */
  Cursor SIZENE = new Cursor(null, SWT.CURSOR_SIZENE);
  /**
   * System resize east cursor.
   */
  Cursor SIZEE = new Cursor(null, SWT.CURSOR_SIZEE);
  /**
   * System resize south-east cursor.
   */
  Cursor SIZESE = new Cursor(null, SWT.CURSOR_SIZESE);
  /**
   * System resize south cursor.
   */
  Cursor SIZES = new Cursor(null, SWT.CURSOR_SIZES);
  /**
   * System resize south-west cursor.
   */
  Cursor SIZESW = new Cursor(null, SWT.CURSOR_SIZESW);
  /**
   * System resize west cursor.
   */
  Cursor SIZEW = new Cursor(null, SWT.CURSOR_SIZEW);
  /**
   * System resize north-west cursor.
   */
  Cursor SIZENW = new Cursor(null, SWT.CURSOR_SIZENW);
  /**
   * System resize north-south cursor
   */
  Cursor SIZENS = new Cursor(null, SWT.CURSOR_SIZENS);
  /**
   * System resize west-east cursor
   */
  Cursor SIZEWE = new Cursor(null, SWT.CURSOR_SIZEWE);
  /**
   * System app startup cursor.
   */
  Cursor APPSTARTING = new Cursor(null, SWT.CURSOR_APPSTARTING);
  /**
   * System cross hair cursor.
   */
  Cursor CROSS = new Cursor(null, SWT.CURSOR_CROSS);
  /**
   * System hand cursor.
   */
  Cursor HAND = new Cursor(null, SWT.CURSOR_HAND);
  /**
   * System help cursor.
   */
  Cursor HELP = new Cursor(null, SWT.CURSOR_HELP);
  /**
   * System i-beam cursor.
   */
  Cursor IBEAM = new Cursor(null, SWT.CURSOR_IBEAM);
  /**
   * System "not allowed" cursor.
   */
  Cursor NO = new Cursor(null, SWT.CURSOR_NO);
  /**
   * System resize all directions cursor.
   */
  Cursor SIZEALL = new Cursor(null, SWT.CURSOR_SIZEALL);
  /**
   * System resize north-east-south-west cursor.
   */
  Cursor SIZENESW = new Cursor(null, SWT.CURSOR_SIZENESW);
  /**
   * System resize north-west-south-east cursor.
   */
  Cursor SIZENWSE = new Cursor(null, SWT.CURSOR_SIZENWSE);
  /**
   * System up arrow cursor.
   */
  Cursor UPARROW = new Cursor(null, SWT.CURSOR_UPARROW);
  /**
   * System wait cursor.
   */
  Cursor WAIT = new Cursor(null, SWT.CURSOR_WAIT);

  /**
   * Returns the cursor corresponding to the given direction.
   */
  class Directional implements IPositionConstants {
    public static final Cursor getCursor(int direction) {
      switch (direction) {
        case NORTH :
          return SIZEN;
        case SOUTH :
          return SIZES;
        case EAST :
          return SIZEE;
        case WEST :
          return SIZEW;
        case SOUTH_EAST :
          return SIZESE;
        case SOUTH_WEST :
          return SIZESW;
        case NORTH_EAST :
          return SIZENE;
        case NORTH_WEST :
          return SIZENW;
        default :
          break;
      }
      return null;
    }
  }
}