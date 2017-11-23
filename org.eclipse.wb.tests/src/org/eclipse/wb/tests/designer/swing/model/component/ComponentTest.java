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
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.SwingToolkitDescription;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JLabel;

/**
 * Test for {@link ComponentInfo}.
 * 
 * @author scheglov_ke
 */
public class ComponentTest extends SwingModelTest {
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
   * Bad implementations of {@link Container#removeAll()} may throw {@link NullPointerException}.
   */
  public void test_clearSwingTree_removeAll_NPE() throws Exception {
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "public class MyContainer extends JPanel {",
            "  public void removeAll() {",
            "    throw new NullPointerException();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyContainer());",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * We can not create {@link java.awt.Image} with zero size, so we should check this.
   */
  public void test_zeroSize() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends Frame {",
            "  public Test() {",
            "    setUndecorated(true);",
            "    setSize(0, 0);",
            "  }",
            "}");
    panel.refresh();
    assertNoErrors(panel);
    // check bounds
    assertEquals(new Rectangle(0, 0, 0, 0), panel.getBounds());
    {
      org.eclipse.swt.graphics.Rectangle imageBounds = panel.getImage().getBounds();
      assertThat(imageBounds.width).isEqualTo(1);
      assertThat(imageBounds.height).isEqualTo(1);
    }
  }

  /**
   * We need special trick to render {@link JLabel} with HTML text.
   */
  public void test_JLabel_withHTML() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "    {",
            "      JPanel inner = new JPanel();",
            "      add(inner, BorderLayout.NORTH);",
            "      inner.setLayout(new GridLayout());",
            "      inner.setBackground(Color.GREEN);",
            "      {",
            "        JLabel label = new JLabel('<html>aaaaaaaaa bbbbbbbb cccccccccc ddddddddd"
                + " eeeeeeeeeee fffffffffffff ggggggggg hhhhhhhhhhhh</html>');",
            "        inner.add(label);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    panel.getTopBoundsSupport().setSize(175, 300);
    panel.refresh();
    // check bottom-right pixel of JLabel on "root" image
    ComponentInfo label = getJavaInfoByName("label");
    Rectangle bounds = label.getModelBounds();
    {
      Image rootImage = panel.getImage();
      ImageData imageData = rootImage.getImageData();
      int pixel = imageData.getPixel(bounds.width - 1, bounds.height - 1);
      RGB rgb = imageData.palette.getRGB(pixel);
      assertEquals(new RGB(0, 255, 0), rgb);
    }
  }

  /**
   * There was problem with SWT thread and time when exposed {@link ComponentInfo} is created.
   */
  public void test_addPanel_withExposedChildren() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private final JButton m_button = new JButton();",
            "  public MyPanel() {",
            "    add(m_button);",
            "  }",
            "  public JButton getButton() {",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    final ContainerInfo panel =
        parseJavaInfo(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    //
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        ComponentInfo myPanel = createJavaInfo("test.MyPanel");
        ((FlowLayoutInfo) panel.getLayout()).add(myPanel, null);
      }
    });
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyPanel myPanel = new MyPanel();",
        "      add(myPanel);",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(myPanel)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyPanel} {local-unique: myPanel} {/new MyPanel()/ /add(myPanel)/}",
        "    {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "    {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Variable name in component"
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that name of component is set in component using {@link Component#setName(String)}.
   */
  public void test_variableName_setName() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // no "variable name in component" configured, just name modification expected
    {
      button.getVariableSupport().setName("button2");
      assertEditor(
          "public class Test extends JPanel {",
          "  public Test() {",
          "    JButton button2 = new JButton();",
          "    add(button2);",
          "  }",
          "}");
    }
    // do with "variable name in component"
    PreferencesRepairer preferences =
        new PreferencesRepairer(panel.getDescription().getToolkit().getPreferences());
    try {
      preferences.setValue(IPreferenceConstants.P_VARIABLE_IN_COMPONENT, true);
      // no setName() for "button", new should be added
      {
        button.getVariableSupport().setName("button3");
        panel.refresh();
        assertEditor(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button3 = new JButton();",
            "    button3.setName('button3');",
            "    add(button3);",
            "  }",
            "}");
      }
      // setName() for "button" exists, should be updated
      {
        button.getVariableSupport().setName("button4");
        panel.refresh();
        assertEditor(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button4 = new JButton();",
            "    button4.setName('button4');",
            "    add(button4);",
            "  }",
            "}");
      }
    } finally {
      preferences.restore();
    }
  }

  /**
   * Test that name of component is set in component using {@link Component#setName(String)}.
   */
  public void test_variableName_setName_forLazy() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
    ComponentInfo newButton = createJButton();
    // do with "variable name in component"
    SwingToolkitDescription.INSTANCE.getPreferences().setValue(
        IPreferenceConstants.P_VARIABLE_IN_COMPONENT,
        true);
    // create new JButton
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    layout.add(newButton, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    add(getButton());",
        "  }",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "      button.setName('button');",
        "    }",
        "    return button;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getButton())/ /add(getButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton empty} {lazy: button getButton()} {/new JButton()/ /add(getButton())/ /button.setName('button')/}");
    // set new name
    newButton.getVariableSupport().setName("button2");
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private JButton button2;",
        "  public Test() {",
        "    add(getButton2());",
        "  }",
        "  private JButton getButton2() {",
        "    if (button2 == null) {",
        "      button2 = new JButton();",
        "      button2.setName('button2');",
        "    }",
        "    return button2;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getButton2())/ /add(getButton2())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton empty} {lazy: button2 getButton2()} {/new JButton()/ /add(getButton2())/ /button2.setName('button2')/}");
  }
}
