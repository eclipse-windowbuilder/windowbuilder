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
package org.eclipse.wb.tests.designer.swing.model.layout.gbl;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagLayoutInfo;
import org.eclipse.wb.os.OSSupport;
import org.eclipse.wb.tests.designer.swing.SwingGefTest;

import org.eclipse.swt.graphics.Image;

/**
 * Test {@link GridBagLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class GridBagLayoutGefTest extends SwingGefTest {
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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    AbstractGridBagLayoutTest.configureForTest();
  }

  @Override
  protected void tearDown() throws Exception {
    AbstractGridBagLayoutTest.configureDefaults();
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If we run events loop at moment when model already changed, so {@link GridBagLayoutInfo} is not
   * active, then headers may be will try to paint. But header needs information from model, which
   * is not active anymore.
   */
  public void test_replaceWithOther_andPaintDuringThis() throws Exception {
    mainPanel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    GridBagLayout gridBagLayout = new GridBagLayout();",
            "    gridBagLayout.columnWeights = new double[]{1.0};",
            "    gridBagLayout.rowWeights = new double[]{1.0};",
            "    setLayout(gridBagLayout);",
            "    {",
            "      JButton button = new JButton('New JButton');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    button = getJavaInfoByName("button");
    canvas.select(button);
    waitEventLoop(0);
    // emulate paint loop during edit
    // Mitin implemented Swing correctly, using AWT thread, but this requires SWT event loop
    ExecutionUtils.runAsync(new RunnableEx() {
      public void run() throws Exception {
        ExecutionUtils.runLogUI(new RunnableEx() {
          public void run() throws Exception {
            forcePaint(m_headerHorizontal);
            forcePaint(m_headerVertical);
          }

          private void forcePaint(GraphicalViewer viewer) throws Exception {
            FigureCanvas control = viewer.getControl();
            Image image = OSSupport.get().makeShot(control);
            image.dispose();
          }
        });
      }
    });
    // replace layout
    LayoutInfo layout = createJavaInfo("java.awt.FlowLayout");
    mainPanel.setLayout(layout);
  }

  /**
   * {@link JPopupMenuInfo} is not managed by {@link LayoutInfo}.
   */
  public void test_JPopupMenu_select() throws Exception {
    openContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button = new JButton('button');",
        "      add(button, new GridBagConstraints());",
        "    }",
        "    {",
        "      JPopupMenu popupMenu = new JPopupMenu();",
        "      addPopup(this, popupMenu);",
        "    }",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
    ComponentInfo popup = getJavaInfoByName("popupMenu");
    //
    canvas.select(popup);
    // no exceptions expected
  }

  /**
   * Test for dropping {@link JPopupMenuInfo}.
   */
  public void test_JPopupMenu_drop() throws Exception {
    mainPanel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    //
    ComponentInfo newPopup = loadCreationTool("javax.swing.JPopupMenu");
    {
      canvas.moveTo(mainPanel, 100, 100);
      canvas.assertFeedbacks(canvas.getTargetPredicate(mainPanel));
      canvas.click();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPopupMenu popupMenu = new JPopupMenu();",
        "      addPopup(this, popupMenu);",
        "    }",
        "    setLayout(new GridBagLayout());",
        "  }",
        "  private static void addPopup(Component component, JPopupMenu popup) {",
        "  }",
        "}");
    canvas.assertPrimarySelected(newPopup);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  ContainerInfo mainPanel;
  ContainerInfo panel_1;
  ComponentInfo button;

  public void test_CREATE_inTree_empty() throws Exception {
    mainPanel =
        openContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridBagLayout());",
            "  }",
            "}");
    // create JButton
    loadCreationTool("javax.swing.JButton", "empty");
    tree.moveOn(mainPanel);
    tree.assertCommandNotNull();
    tree.click();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridBagLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      GridBagConstraints gbc = new GridBagConstraints();",
        "      gbc.gridx = 0;",
        "      gbc.gridy = 0;",
        "      add(button, gbc);",
        "    }",
        "  }",
        "}");
  }
}
