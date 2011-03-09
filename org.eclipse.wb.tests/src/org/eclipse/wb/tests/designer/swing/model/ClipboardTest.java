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
package org.eclipse.wb.tests.designer.swing.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMementoTransfer;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import javax.swing.JButton;

/**
 * Test for {@link JavaInfoMemento} and other clipboard related operations.
 * 
 * @author scheglov_ke
 */
public class ClipboardTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_hasMemento() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('111');",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    panel.refresh();
    // do checks
    assertFalse(JavaInfoMemento.hasMemento(panel));
    assertTrue(JavaInfoMemento.hasMemento(button));
  }

  public void test_getComponentClassName() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton('111');",
            "    add(button);",
            "  }",
            "}");
    panel.refresh();
    // check JavaInfoMemento
    ComponentInfo button = panel.getChildrenComponents().get(0);
    JavaInfoMemento memento = JavaInfoMemento.createMemento(button);
    assertEquals("javax.swing.JButton", memento.getComponentClassName());
  }

  public void test_transfer() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton('111');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // invoke just to cover
    {
      assertNotNull(ReflectionUtils.invokeMethod(
          JavaInfoMementoTransfer.getInstance(),
          "getTypeNames()"));
    }
    // copy to Clipboard
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      JavaInfoMemento memento = JavaInfoMemento.createMemento(button);
      Clipboard clipboard = new Clipboard(Display.getCurrent());
      try {
        clipboard.setContents(
            new Object[]{new JavaInfoMemento[]{memento}},
            new Transfer[]{JavaInfoMementoTransfer.getInstance()});
      } finally {
        clipboard.dispose();
      }
    }
    // get from Clipboard
    {
      JavaInfoMemento[] mementos;
      {
        Clipboard clipboard = new Clipboard(Display.getCurrent());
        try {
          mementos =
              (JavaInfoMemento[]) clipboard.getContents(JavaInfoMementoTransfer.getInstance());
        } finally {
          clipboard.dispose();
        }
      }
      //
      assertEquals(1, mementos.length);
      JavaInfo javaInfo = mementos[0].create(panel);
      assertInstanceOf(ComponentInfo.class, javaInfo);
    }
    // set text and try to ask JavaInfoMementoTransfer
    {
      Clipboard clipboard = new Clipboard(Display.getCurrent());
      try {
        clipboard.setContents(new Object[]{"some text"}, new Transfer[]{TextTransfer.getInstance()});
        assertNull(clipboard.getContents(JavaInfoMementoTransfer.getInstance()));
        // cover unsupported type
        assertNull(JavaInfoMementoTransfer.getInstance().nativeToJava(null));
      } finally {
        clipboard.dispose();
      }
    }
  }

  /**
   * Copy/paste one {@link JButton}.
   */
  public void test_singleComponent() throws Exception {
    String[] sourceLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton((Icon) null);",
            "      button.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);",
            "      button.setEnabled(false);",
            "      button.setText(\"222\");",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton((Icon) null);",
            "      button.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);",
            "      button.setEnabled(false);",
            "      button.setText(\"222\");",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton((Icon) null);",
            "      button.setText(\"222\");",
            "      button.setEnabled(false);",
            "      button.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    flow_doCopy(sourceLines, targetLines);
  }

  /**
   * Copy/paste container with exposed sub-component.
   */
  public void test_exposedSubComponent() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton button;",
            "  public MyPanel() {",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "  public JButton getButton() {",
            "    return button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // do copy/paste
    String[] sourceLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyPanel myPanel = new MyPanel();",
            "      myPanel.getButton().setText(\"exposed button\");",
            "      add(myPanel);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyPanel myPanel = new MyPanel();",
            "      myPanel.getButton().setText(\"exposed button\");",
            "      add(myPanel);",
            "    }",
            "    {",
            "      MyPanel myPanel = new MyPanel();",
            "      myPanel.getButton().setText(\"exposed button\");",
            "      add(myPanel);",
            "    }",
            "  }",
            "}"};
    flow_doCopy(sourceLines, targetLines);
  }

  public void test_factoryStatic() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(String _text) {",
            "    return new JButton(_text);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    String[] sourceLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = StaticFactory.createButton(\"button\");",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = StaticFactory.createButton(\"button\");",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = StaticFactory.createButton(\"button\");",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    flow_doCopy(sourceLines, targetLines);
  }

  public void test_factoryStatic_complex() throws Exception {
    setFileContentSrc(
        "test/StaticFactory.java",
        getTestSource(
            "public final class StaticFactory {",
            "  public static JButton createButton(String _text, int i, Object o) {",
            "    return new JButton(_text);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    String[] sourceLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = StaticFactory.createButton(\"button\", 1, new Object());",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = StaticFactory.createButton(\"button\", 1, new Object());",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = StaticFactory.createButton(\"button\", 1, (Object) null);",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    flow_doCopy(sourceLines, targetLines);
  }

  public void test_factoryInstance_single() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton(String _text) {",
            "    return new JButton(_text);",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    String[] sourceLines =
        new String[]{
            "public class Test extends JPanel {",
            "  private final InstanceFactory m_factory = new InstanceFactory();",
            "  public Test() {",
            "    {",
            "      JButton button = m_factory.createButton(\"button\");",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    String[] targetLines =
        new String[]{
            "public class Test extends JPanel {",
            "  private final InstanceFactory m_factory = new InstanceFactory();",
            "  public Test() {",
            "    {",
            "      JButton button = m_factory.createButton(\"button\");",
            "      add(button);",
            "    }",
            "    {",
            "      JButton button = m_factory.createButton(\"button\");",
            "      add(button);",
            "    }",
            "  }",
            "}"};
    flow_doCopy(sourceLines, targetLines);
  }

  public void test_factoryInstance_new() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public final class InstanceFactory {",
            "  public JButton createButton(String _text) {",
            "    return new JButton(_text);",
            "  }",
            "}"));
    waitForAutoBuild();
    // prepare memento
    JavaInfoMemento memento;
    {
      ContainerInfo panel =
          parseContainer(
              "public class Test extends JPanel {",
              "  private final InstanceFactory m_factory = new InstanceFactory();",
              "  public Test() {",
              "    {",
              "      JButton button = m_factory.createButton('button');",
              "      add(button);",
              "    }",
              "  }",
              "}");
      panel.refresh();
      //
      ComponentInfo component = panel.getChildrenComponents().get(0);
      memento = JavaInfoMemento.createMemento(component);
    }
    // use memento for paste
    ContainerInfo panel2 =
        (ContainerInfo) parseSource(
            "test",
            "Test2.java",
            getTestSource(
                "// filler filler filler filler filler",
                "// filler filler filler filler filler",
                "public class Test2 extends JPanel {",
                "  public Test2() {",
                "  }",
                "}"));
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel2.getLayout();
    //
    ComponentInfo component = (ComponentInfo) memento.create(panel2);
    flowLayout.add(component, null);
    memento.apply();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test2 extends JPanel {",
        "  private final InstanceFactory instanceFactory = new InstanceFactory();",
        "  public Test2() {",
        "    {",
        "      JButton button = instanceFactory.createButton('button');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Does copy/paste for first child of parsed {@link ContainerInfo} with {@link FlowLayoutInfo}.
   * 
   * @return the pasted {@link ComponentInfo}.
   */
  private ComponentInfo flow_doCopy(String[] sourceLines, String[] targetLines) throws Exception {
    final ContainerInfo container = parseContainer(sourceLines);
    final FlowLayoutInfo flowLayout = (FlowLayoutInfo) container.getLayout();
    container.refresh();
    // create memento for "child"
    final JavaInfoMemento memento;
    {
      ComponentInfo component = container.getChildrenComponents().get(0);
      memento = JavaInfoMemento.createMemento(component);
    }
    // do paste
    final ComponentInfo[] pastedComponent = new ComponentInfo[1];
    ExecutionUtils.run(container, new RunnableEx() {
      public void run() throws Exception {
        ComponentInfo component = (ComponentInfo) memento.create(container);
        flowLayout.add(component, null);
        memento.apply();
        pastedComponent[0] = component;
      }
    });
    // check result
    assertEditor(targetLines);
    return pastedComponent[0];
  }
}
