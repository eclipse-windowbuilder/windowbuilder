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
package org.eclipse.wb.tests.designer.swing.jsr296;

import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import javax.swing.JLabel;

/**
 * Test for <code>org.jdesktop.application.ResourceMap.injectComponents(Component)</code>.
 * 
 * @author sablin_aa
 */
public class LoadResourcesTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_testProject.addBundleJars("org.eclipse.wb.tests.support", "/resources/Swing/jsr296");
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
  /**
   * Tests that <code>ResourceMap.injectComponents(Component)</code> is invoked.
   */
  public void test_parse() throws Exception {
    setFileContentSrc("test/resources/Test.properties", "label.text = TestLabel");
    m_waitForAutoBuild = true;
    JPanelInfo panel =
        parseJavaInfo(
            "import org.jdesktop.application.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = new JLabel();",
            "      label.setName('label');",
            "      add(label);",
            "    }",
            "    Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);",
            "  }",
            "}");
    refresh();
    assertNoErrors(panel);
    // check "label"
    {
      ComponentInfo label = getJavaInfoByName("label");
      // "text" property has default value
      assertEquals("TestLabel", label.getPropertyByTitle("text").getValue());
      // "text" is applied to JLabel instance
      assertEquals("TestLabel", ((JLabel) label.getObject()).getText());
    }
  }

  /**
   * Tests that <code>ResourceMap.injectComponents(Component)</code> is terminate statement for
   * children.
   */
  public void test_CREATE() throws Exception {
    m_waitForAutoBuild = true;
    JPanelInfo panel =
        parseJavaInfo(
            "import org.jdesktop.application.*;",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JLabel label = new JLabel();",
            "      add(label);",
            "    }",
            "    Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);",
            "  }",
            "}");
    refresh();
    //
    {
      ComponentInfo newButton = createComponent("javax.swing.JButton");
      FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
      layout.add(newButton, null);
    }
    assertEditor(
        "import org.jdesktop.application.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JLabel label = new JLabel();",
        "      add(label);",
        "    }",
        "    {",
        "      JButton button = new JButton('New button');",
        "      add(button);",
        "    }",
        "    Application.getInstance().getContext().getResourceMap(getClass()).injectComponents(this);",
        "  }",
        "}");
  }
}