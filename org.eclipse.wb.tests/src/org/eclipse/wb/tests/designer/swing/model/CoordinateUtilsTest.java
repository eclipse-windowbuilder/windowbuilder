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
package org.eclipse.wb.tests.designer.swing.model;

import org.eclipse.wb.internal.swing.model.CoordinateUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

/**
 * Test for {@link CoordinateUtils}.
 * 
 * @author scheglov_ke
 */
public class CoordinateUtilsTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT -> draw2d
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CoordinateUtils#get(java.awt.Point)}.
   */
  public void test_toDraw2d_Point() throws Exception {
    assertEquals(
        new org.eclipse.wb.draw2d.geometry.Point(1, 2),
        CoordinateUtils.get(new java.awt.Point(1, 2)));
  }

  /**
   * Test for {@link CoordinateUtils#get(java.awt.Rectangle)}.
   */
  public void test_toDraw2d_Rectangle() throws Exception {
    assertEquals(
        new org.eclipse.wb.draw2d.geometry.Rectangle(1, 2, 3, 4),
        CoordinateUtils.get(new java.awt.Rectangle(1, 2, 3, 4)));
  }

  /**
   * Test for {@link CoordinateUtils#get(java.awt.Dimension)}.
   */
  public void test_toDraw2d_Dimension() throws Exception {
    assertEquals(
        new org.eclipse.wb.draw2d.geometry.Dimension(1, 2),
        CoordinateUtils.get(new java.awt.Dimension(1, 2)));
  }

  /**
   * Test for {@link CoordinateUtils#get(java.awt.Insets)}.
   */
  public void test_toDraw2d_Insets() throws Exception {
    assertEquals(
        new org.eclipse.wb.draw2d.geometry.Insets(1, 2, 3, 4),
        CoordinateUtils.get(new java.awt.Insets(1, 2, 3, 4)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // draw2d -> AWT
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CoordinateUtils#get(org.eclipse.wb.draw2d.geometry.Insets)}.
   */
  public void test_toAWT_Insets() throws Exception {
    assertEquals(
        new java.awt.Insets(1, 2, 3, 4),
        CoordinateUtils.get(new org.eclipse.wb.draw2d.geometry.Insets(1, 2, 3, 4)));
  }
}
