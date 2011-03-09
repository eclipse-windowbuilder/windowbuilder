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
package org.eclipse.wb.tests.designer.editor.action;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.editor.actions.CopyAction;
import org.eclipse.wb.internal.core.editor.actions.PasteAction;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.jface.action.IAction;

/**
 * Test for {@link CopyAction} and {@link PasteAction}.
 * 
 * @author scheglov_ke
 */
public class CopyActionTest extends SwingGefTest {
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
  /**
   * "Copy" action is disabled if no selection.
   */
  public void test_noSelection() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "  // filler filler filler",
        "}");
    // prepare "Copy" action
    IAction copyAction = getCopyAction();
    // no selection - disabled action
    canvas.select();
    assertFalse(copyAction.isEnabled());
  }

  /**
   * "This" component can not be copied.
   */
  public void test_thisSelection() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "  // filler filler filler",
            "}");
    // prepare "Copy" action
    IAction copyAction = getCopyAction();
    // "this" selected - disabled action
    canvas.select(panel);
    assertFalse(copyAction.isEnabled());
  }

  /**
   * Test for copy/paste single component.
   */
  public void test_copySingle() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton btn = new JButton();",
            "      add(btn);",
            "    }",
            "  }",
            "}");
    // select "btn"
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      canvas.select(button);
    }
    // copy
    {
      IAction copyAction = getCopyAction();
      assertTrue(copyAction.isEnabled());
      copyAction.run();
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
      // do paste
      {
        EditPart targetEditPart = m_contentEditPart;
        canvas.moveTo(targetEditPart, 10, 10);
        canvas.click();
      }
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    {",
          "      JButton button = new JButton();",
          "      add(button);",
          "    }",
          "    {",
          "      JButton btn = new JButton();",
          "      add(btn);",
          "    }",
          "  }",
          "}");
    }
  }

  /**
   * If container and its child are selected, then only ocntainer should be copied, it will copy
   * child automatically.
   */
  public void test_copyParentAndItsChild() throws Exception {
    ContainerInfo panel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      add(inner);",
            "      {",
            "        JButton button = new JButton();",
            "        inner.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    // select "inner" and "button"
    {
      ContainerInfo inner = (ContainerInfo) panel.getChildrenComponents().get(0);
      ComponentInfo button = inner.getChildrenComponents().get(0);
      canvas.select(inner, button);
    }
    // copy
    {
      IAction copyAction = getCopyAction();
      assertTrue(copyAction.isEnabled());
      copyAction.run();
    }
    // paste
    {
      IAction pasteAction = getPasteAction();
      assertTrue(pasteAction.isEnabled());
      pasteAction.run();
      // do paste
      {
        EditPart targetEditPart = m_contentEditPart;
        canvas.moveTo(targetEditPart, 10, 10);
        canvas.click();
      }
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    {",
          "      JPanel panel = new JPanel();",
          "      add(panel);",
          "      {",
          "        JButton button = new JButton();",
          "        panel.add(button);",
          "      }",
          "    }",
          "    {",
          "      JPanel inner = new JPanel();",
          "      add(inner);",
          "      {",
          "        JButton button = new JButton();",
          "        inner.add(button);",
          "      }",
          "    }",
          "  }",
          "}");
    }
  }
}
