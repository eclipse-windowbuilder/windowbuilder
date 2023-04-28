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
package org.eclipse.wb.tests.designer.XWT.support;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.InsValue;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.swt.widgets.Group;

/**
 * Tests for {@link CoordinateUtils}.
 *
 * @author scheglov_ke
 */
public class CoordinateUtilsTest extends XwtModelTest {
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
  public void test_1() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Button wbp:name='button' bounds='10, 20, 50, 30'/>",
            "</Shell>");
    refresh();
    ControlInfo button = getObjectByName("button");
    // Shell display location
    Point shellLocation = CoordinateUtils.getDisplayLocation(shell.getComposite());
    assertEquals(new Point(-10000, -10000), shellLocation);
    // Shell client area insets
    Insets shellInsets = CoordinateUtils.getClientAreaInsets(shell.getComposite());
    assertTrue(shellInsets.left == shellInsets.right);
    assertTrue(shellInsets.left > 3);
    assertTrue(shellInsets.top > 20);
    // Button display location
    {
      Point buttonLocation = CoordinateUtils.getDisplayLocation(button.getControl());
      assertEquals(shellLocation.x + shellInsets.left + 10, buttonLocation.x);
      assertEquals(shellLocation.y + shellInsets.top + 20, buttonLocation.y);
    }
    // getBounds() for "button", model bounds
    {
      Rectangle bounds = CoordinateUtils.getBounds(button.getControl());
      assertEquals(new Rectangle(10, 20, 50, 30), bounds);
    }
  }

  /**
   * Test for {@link CoordinateUtils#getClientAreaInsets2(Object)} for {@link Group}.
   */
  public void test_withGroup() throws Exception {
    CompositeInfo group = parse("<Group/>");
    refresh();
    //
    Insets insets =
        Expectations.get(new Insets(15, 3, 3, 3), new InsValue[]{
            new InsValue("flanker-windows", new Insets(15, 3, 3, 3)),
            new InsValue("scheglov-win", new Insets(15, 3, 3, 3))});
    assertEquals(insets, CoordinateUtils.getClientAreaInsets2(group.getComposite()));
  }
}