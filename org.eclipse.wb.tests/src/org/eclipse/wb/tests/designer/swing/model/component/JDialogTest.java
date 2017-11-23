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
import org.eclipse.wb.internal.swing.model.component.JDialogInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Tests for {@link JDialog} support.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 */
public class JDialogTest extends SwingModelTest {
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
  public void test_0() throws Exception {
    JDialogInfo dialog =
        (JDialogInfo) parseContainer(
            "public class Test extends JDialog {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JDialog} {this} {}",
        "  {method: public java.awt.Container javax.swing.JDialog.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
    // refresh()
    dialog.refresh();
    assertNoErrors(dialog);
    // check bounds for JDialog and its "contentPane"
    ComponentInfo contentPane = dialog.getChildrenComponents().get(0);
    {
      Rectangle bounds = dialog.getBounds();
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rewrite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We don't support parsing <code>innstanceOfJDialog.add()</code>, because it adds components on
   * "contentPane", so we rewrite source (and AST) to use "canonical" pattern.
   */
  public void test_parseWithout_getContentPane_this() throws Exception {
    parseJavaInfo(
        "public class Test extends JDialog {",
        "  public Test() {",
        "    setModal(true);",
        "    setLayout(new FlowLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      this.add(button);",
        "    }",
        "  }",
        "}");
    // rewritten to use getContentPane()
    assertEditor(
        "public class Test extends JDialog {",
        "  public Test() {",
        "    setModal(true);",
        "    getContentPane().setLayout(new FlowLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      getContentPane().add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JDialog} {this} {/setModal(true)/}",
        "  {method: public java.awt.Container javax.swing.JDialog.getContentPane()} {property} {/getContentPane().setLayout(new FlowLayout())/ /getContentPane().add(button)/}",
        "    {new: java.awt.FlowLayout} {empty} {/getContentPane().setLayout(new FlowLayout())/}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /getContentPane().add(button)/}");
  }

  /**
   * We don't support parsing <code>innstanceOfJDialog.add()</code>, because it adds components on
   * "contentPane", so we rewrite source (and AST) to use "canonical" pattern.
   */
  public void test_parseWithout_getContentPane_instance() throws Exception {
    parseJavaInfo(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JDialog dialog = new JDialog();",
        "    dialog.setLayout(new FlowLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      dialog.add(button);",
        "    }",
        "  }",
        "}");
    // rewritten to use getContentPane()
    assertEditor(
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JDialog dialog = new JDialog();",
        "    dialog.getContentPane().setLayout(new FlowLayout());",
        "    {",
        "      JButton button = new JButton();",
        "      dialog.getContentPane().add(button);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JDialog} {local-unique: dialog} {/new JDialog()/ /dialog.getContentPane()/ /dialog.getContentPane()/}",
        "  {method: public java.awt.Container javax.swing.JDialog.getContentPane()} {property} {/dialog.getContentPane().setLayout(new FlowLayout())/ /dialog.getContentPane().add(button)/}",
        "    {new: java.awt.FlowLayout} {empty} {/dialog.getContentPane().setLayout(new FlowLayout())/}",
        "    {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /dialog.getContentPane().add(button)/}");
  }

  /**
   * We should sometimes rewrite code to use <code>getContentPane()</code>, but not in this case,
   * because here <code>setLayout()</code> has no parameters and is just local method.
   */
  public void test_rewrite_setLayout_noCorrectSignature() throws Exception {
    parseJavaInfo(
        "public class Test extends JDialog {",
        "  public Test() {",
        "    setLayout();",
        "  }",
        "  private void setLayout() {",
        "    getContentPane().setLayout(new FlowLayout());",
        "  }",
        "}");
    // no rewrite
    assertEditor(
        "public class Test extends JDialog {",
        "  public Test() {",
        "    setLayout();",
        "  }",
        "  private void setLayout() {",
        "    getContentPane().setLayout(new FlowLayout());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JDialog} {this} {}",
        "  {method: public java.awt.Container javax.swing.JDialog.getContentPane()} {property} {/getContentPane().setLayout(new FlowLayout())/}",
        "    {new: java.awt.FlowLayout} {empty} {/getContentPane().setLayout(new FlowLayout())/}");
  }

  /**
   * We should do code rewrite for {@link JDialog#setLayout(java.awt.LayoutManager)} but only if
   * this is really invocation for {@link JDialog}. However if inner {@link JPanel} uses
   * {@link JPanel#setLayout(java.awt.LayoutManager)}, then we should not change this code.
   */
  public void test_rewrite_setLayout_ofInnerJPanel() throws Exception {
    parseJavaInfo(
        "public class Test extends JDialog {",
        "  private static class Inner extends JPanel {",
        "    public Inner() {",
        "      setLayout(new FlowLayout());",
        "    }",
        "  }",
        "  public Test() {",
        "  }",
        "}");
    // no changes
    assertEditor(
        "public class Test extends JDialog {",
        "  private static class Inner extends JPanel {",
        "    public Inner() {",
        "      setLayout(new FlowLayout());",
        "    }",
        "  }",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JDialog} {this} {}",
        "  {method: public java.awt.Container javax.swing.JDialog.getContentPane()} {property} {}",
        "    {implicit-layout: java.awt.BorderLayout} {implicit-layout} {}");
  }
}
