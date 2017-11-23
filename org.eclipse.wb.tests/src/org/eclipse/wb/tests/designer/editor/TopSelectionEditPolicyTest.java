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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link TopSelectionEditPolicy}.
 * 
 * @author scheglov_ke
 */
public class TopSelectionEditPolicyTest extends SwingGefTest {
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
  public void test_resizeBoth() throws Exception {
    ComponentInfo shell =
        openContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "	public Test() {",
            "	}",
            "}");
    // initial size
    {
      Rectangle bounds = shell.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // select end resize
    canvas.beginResize(shell, IPositionConstants.SOUTH_EAST);
    canvas.dragOn(50, 30).endDrag();
    {
      Rectangle bounds = shell.getBounds();
      assertEquals(500, bounds.width);
      assertEquals(330, bounds.height);
    }
  }

  public void test_resizeEast_toNegative() throws Exception {
    ComponentInfo shell =
        openContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "	public Test() {",
            "	}",
            "}");
    // initial size
    {
      Rectangle bounds = shell.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // drag so that size is negative, but no exception
    canvas.beginResize(shell, IPositionConstants.EAST);
    canvas.dragOn(-455, 0);
    canvas.endDrag();
    // size is reasonable
    {
      Rectangle bounds = shell.getBounds();
      assertThat(bounds.width).isGreaterThan(10);
      assertThat(bounds.height).isEqualTo(300);
    }
  }

  public void test_resizeSouth_toNegative() throws Exception {
    ComponentInfo shell =
        openContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "	public Test() {",
            "	}",
            "}");
    // initial size
    {
      Rectangle bounds = shell.getBounds();
      assertEquals(450, bounds.width);
      assertEquals(300, bounds.height);
    }
    // drag so that size is negative, but no exception
    canvas.beginResize(shell, IPositionConstants.SOUTH);
    canvas.dragOn(0, -305);
    canvas.endDrag();
    // size is reasonable
    {
      Rectangle bounds = shell.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isGreaterThan(0);
    }
  }
}
