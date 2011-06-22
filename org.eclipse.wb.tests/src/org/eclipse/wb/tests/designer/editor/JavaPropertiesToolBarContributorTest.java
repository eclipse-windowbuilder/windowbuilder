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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.multi.MultiMode;
import org.eclipse.wb.internal.core.editor.structure.property.JavaPropertiesToolBarContributor;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolItem;

/**
 * Test for {@link JavaPropertiesToolBarContributor}.
 * 
 * @author scheglov_ke
 */
public class JavaPropertiesToolBarContributorTest extends SwingGefTest {
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
   * Test for "Goto definition" action.
   */
  public void test_gotoDefinition() throws Exception {
    openContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    JavaInfo button = getJavaInfoByName("button");
    // prepare UiContext
    UiContext context = new UiContext();
    // no selection initially, so no action
    {
      ToolItem toolItem = context.getToolItem("Goto definition");
      assertNull(toolItem);
    }
    // select "button", show actions
    canvas.select(button);
    // use action
    {
      ToolItem toolItem = context.getToolItem("Goto definition");
      assertNotNull(toolItem);
      context.click(toolItem, SWT.NONE);
      waitEventLoop(0);
    }
    // assert that position in XML source was opened
    {
      // "Source" is active
      MultiMode multiMode = (MultiMode) m_designerEditor.getMultiMode();
      assertTrue(multiMode.getSourcePage().isActive());
      // selection in source
      int expectedPosition = button.getCreationSupport().getNode().getStartPosition();
      assertJavaSelection(expectedPosition, 0);
    }
  }

  /**
   * Test for "Local to field" action.
   */
  public void test_convertLocalToField() throws Exception {
    openContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    JavaInfo button = getJavaInfoByName("button");
    // prepare UiContext
    UiContext context = new UiContext();
    // no selection initially, so no action
    {
      ToolItem toolItem = context.getToolItem("Convert local to field");
      assertNull(toolItem);
    }
    // select "button", show actions
    canvas.select(button);
    // use action
    {
      ToolItem toolItem = context.getToolItem("Convert local to field");
      assertNotNull(toolItem);
      context.click(toolItem, SWT.NONE);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    {",
        "      button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // use action
    {
      ToolItem toolItem = context.getToolItem("Convert field to local");
      assertNotNull(toolItem);
      context.click(toolItem, SWT.NONE);
    }
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }
}
