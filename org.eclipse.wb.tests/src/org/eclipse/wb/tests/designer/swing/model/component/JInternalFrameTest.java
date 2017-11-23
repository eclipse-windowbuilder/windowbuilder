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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JInternalFrameInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JInternalFrame;

/**
 * Tests for {@link JInternalFrame} support.
 * 
 * @author scheglov_ke
 */
public class JInternalFrameTest extends SwingModelTest {
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
  public void test_this() throws Exception {
    JInternalFrameInfo frame =
        parseJavaInfo(
            "// filler filler filler",
            "public class Test extends JInternalFrame {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JInternalFrame} {this} {}",
        "  {method: public java.awt.Container javax.swing.JInternalFrame.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
    frame.refresh();
    assertNoErrors(frame);
    // check bounds of JInternalFrame and its "contentPane"
    ComponentInfo contentPane = frame.getChildrenComponents().get(0);
    {
      Rectangle bounds = frame.getBounds();
      assertThat(bounds.width).isEqualTo(450);
      assertThat(bounds.height).isEqualTo(300);
    }
    {
      Rectangle bounds = contentPane.getBounds();
      assertThat(bounds.x).isGreaterThan(0);
      assertThat(bounds.y).isGreaterThan(20);
      assertThat(bounds.width).isGreaterThan(420);
      assertThat(bounds.height).isGreaterThan(250);
    }
  }

  public void test_onJDesktopPane() throws Exception {
    ContainerInfo panel =
        parseJavaInfo(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JDesktopPane desktop = new JDesktopPane();",
            "    add(desktop);",
            "    {",
            "      JInternalFrame frame = new JInternalFrame();",
            "      desktop.add(frame);",
            "      frame.setBounds(5, 5, 200, 150);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(desktop)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JDesktopPane} {local-unique: desktop} {/new JDesktopPane()/ /add(desktop)/ /desktop.add(frame)/}",
        "    {implicit-layout: absolute} {implicit-layout} {}",
        "    {new: javax.swing.JInternalFrame} {local-unique: frame} {/new JInternalFrame()/ /desktop.add(frame)/ /frame.setBounds(5, 5, 200, 150)/}",
        "      {method: public java.awt.Container javax.swing.JInternalFrame.getContentPane()} {property} {}",
        "        {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
    panel.refresh();
    JInternalFrameInfo frame = getJavaInfoByName("frame");
    // JInternalFrame should be visible
    assertEquals(true, frame.getComponent().isVisible());
  }
}
