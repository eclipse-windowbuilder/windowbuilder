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
package org.eclipse.wb.tests.designer.swing.ams;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

/**
 * Test for <code>VarmenuLayout</code> support.
 *
 * @author scheglov_ke
 */
public class VarmenuLayoutTest extends SwingGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    setFileContentSrc(
        "ams/zpointcs/components/VarmenuConstraints.java",
        IOUtils2.readString(getClass().getResourceAsStream("VarmenuConstraints.txt")));
    setFileContentSrc(
        "ams/zpointcs/components/VarmenuLayout.java",
        IOUtils2.readString(getClass().getResourceAsStream("VarmenuLayout.txt")));
    prepareBox();
  }

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
  public void test_CREATE() throws Exception {
    ContainerInfo panel =
        openContainer(
            "import ams.zpointcs.components.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new VarmenuLayout());",
            "  }",
            "}");
    panel.refresh();
    //
    loadCreationBox();
    canvas.sideMode().create(100, 50);
    canvas.target(panel).in(150, 100).move();
    canvas.click();
    assertEditor(
        "import ams.zpointcs.components.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new VarmenuLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new VarmenuConstraints(150, 100, 0, 0));",
        "    }",
        "  }",
        "}");
  }

  public void test_RESIZE_width() throws Exception {
    ContainerInfo panel =
        openContainer(
            "import ams.zpointcs.components.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new VarmenuLayout());",
            "    {",
            "      Box box = new Box();",
            "      add(box, new VarmenuConstraints(150, 100, 0, 0));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo box = panel.getChildrenComponents().get(0);
    // drag to non-default width
    canvas.beginResize(box, IPositionConstants.EAST).dragOn(30, 0).endDrag();
    assertEditor(
        "import ams.zpointcs.components.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new VarmenuLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new VarmenuConstraints(150, 100, 130, 0));",
        "    }",
        "  }",
        "}");
    // drag to default width
    canvas.beginResize(box, IPositionConstants.EAST).dragOn(-30, 0).endDrag();
    assertEditor(
        "import ams.zpointcs.components.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new VarmenuLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new VarmenuConstraints(150, 100, 0, 0));",
        "    }",
        "  }",
        "}");
  }

  public void test_RESIZE_height() throws Exception {
    ContainerInfo panel =
        openContainer(
            "import ams.zpointcs.components.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new VarmenuLayout());",
            "    {",
            "      Box box = new Box();",
            "      add(box, new VarmenuConstraints(150, 100, 0, 0));",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo box = panel.getChildrenComponents().get(0);
    // drag to non-default width
    canvas.beginResize(box, IPositionConstants.SOUTH).dragOn(0, 50).endDrag();
    assertEditor(
        "import ams.zpointcs.components.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new VarmenuLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new VarmenuConstraints(150, 100, 0, 100));",
        "    }",
        "  }",
        "}");
    // drag to default width
    canvas.beginResize(box, IPositionConstants.SOUTH).dragOn(0, -50).endDrag();
    assertEditor(
        "import ams.zpointcs.components.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new VarmenuLayout());",
        "    {",
        "      Box box = new Box();",
        "      add(box, new VarmenuConstraints(150, 100, 0, 0));",
        "    }",
        "  }",
        "}");
  }
}