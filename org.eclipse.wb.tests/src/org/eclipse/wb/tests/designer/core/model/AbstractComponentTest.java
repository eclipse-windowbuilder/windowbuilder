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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.swt.graphics.Image;

/**
 * Test for {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 */
public class AbstractComponentTest extends SwingModelTest {
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
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we deleted {@link AbstractComponentInfo}, then it will not participate in next "refresh",
   * so will not able to dispose its {@link Image}. We need some solution to clean up
   * {@link AbstractComponentInfo} during delete.
   */
  public void test_disposeImage_whenDeleteModel() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    refresh();
    ComponentInfo button = getJavaInfoByName("button");
    // remember image
    Image image = button.getImage();
    assertFalse(image.isDisposed());
    // do delete
    button.delete();
    assertTrue(image.isDisposed());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getComponentObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractComponentInfo#getComponentObject()}.
   */
  public void test_getComponentObject() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // objects
    assertSame(panel.getObject(), panel.getComponentObject());
    assertSame(button.getObject(), button.getComponentObject());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getAbsoluteBounds()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AbstractComponentInfo#getAbsoluteBounds()} when we have some bounds for top
   * level.
   */
  public void test_getAbsolute_topLevel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setSize(450, 300);",
            "  }",
            "}");
    panel.refresh();
    //
    assertEquals(new Rectangle(0, 0, 450, 300), panel.getAbsoluteBounds());
  }

  /**
   * Test for {@link AbstractComponentInfo#getAbsoluteBounds()} when we have some bounds for inner
   * component.
   */
  public void test_getAbsolute_innerComponent() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setSize(450, 300);",
            "    setLayout(null);",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "      innerPanel.setBounds(10, 10, 300, 300);",
            "      innerPanel.setLayout(null);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "        button.setBounds(1, 2, 100, 50);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    //
    assertEquals(new Rectangle(11, 12, 100, 50), button.getAbsoluteBounds());
  }
}
