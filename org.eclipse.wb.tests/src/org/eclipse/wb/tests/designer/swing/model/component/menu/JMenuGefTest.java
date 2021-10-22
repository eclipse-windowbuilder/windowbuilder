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
package org.eclipse.wb.tests.designer.swing.model.component.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuBarInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JMenuInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import javax.swing.JMenu;

/**
 * Test for {@link JMenuInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class JMenuGefTest extends SwingGefTest {
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
  public void test_drop_JMenuBar_onJFrame() throws Exception {
    ContainerInfo frame =
        openContainer(
            "// filler filler filler filler filler",
            "public class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    //
    JMenuBarInfo menuBar = loadCreationTool("javax.swing.JMenuBar");
    canvas.moveTo(frame, 100, 5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JFrame {",
        "  public Test() {",
        "    {",
        "      JMenuBar menuBar = new JMenuBar();",
        "      setJMenuBar(menuBar);",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(menuBar);
  }

  public void test_drop_JMenuBar_onJInternalFrame() throws Exception {
    ContainerInfo frame =
        openContainer(
            "// filler filler filler filler filler",
            "public class Test extends JInternalFrame {",
            "  public Test() {",
            "  }",
            "}");
    //
    JMenuBarInfo menuBar = loadCreationTool("javax.swing.JMenuBar");
    canvas.moveTo(frame, 100, 5).click();
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JInternalFrame {",
        "  public Test() {",
        "    {",
        "      JMenuBar menuBar = new JMenuBar();",
        "      setJMenuBar(menuBar);",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(menuBar);
  }

  /**
   * Don't allow to move "item" of {@link JMenu} on its "popup".
   */
  public void test_dontMoveMove_onItsItem() throws Exception {
    openContainer(
        "// filler filler filler filler filler",
        "public class Test extends JFrame {",
        "  public Test() {",
        "    JMenuBar menuBar = new JMenuBar();",
        "    setJMenuBar(menuBar);",
        "    {",
        "      JMenu menu = new JMenu('Test');",
        "      menuBar.add(menu);",
        "    }",
        "  }",
        "}");
    JavaInfo menu = getJavaInfoByName("menu");
    //
    canvas.moveTo(menu, 0.5, -1).beginDrag().dragOn(0, 20);
    canvas.assertCommandNull();
  }
}
